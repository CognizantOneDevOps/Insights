#-------------------------------------------------------------------------------
# Copyright 2022 Cognizant Technology Solutions
# 
# Licensed under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License.  You may obtain a copy
# of the License at
# 
#   http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
# License for the specific language governing permissions and limitations under
# the License.
#-------------------------------------------------------------------------------

import logging.handlers
import math
import boto3
import botocore 
import json
import os.path
import sys

from .MessageAbstract import MessageAbstract

class AWSSQSConnectionProvider(MessageAbstract):
    
    def __init__(self,config):

        self.config = config
        self.mqConfig = self.config.get('mqConfig', None)
        self.AWSRegion = self.mqConfig.get('awsRegion',None)
        self.AWSKey = self.mqConfig.get('awsAccessKey',None)
        self.AWSSecretKey = self.mqConfig.get('awsSecretKey',None)
        self.enableDeadLetterQueue = self.mqConfig.get('enableDeadLetterExchange','False')
        self.SQS_MAX_MSG_SIZE = 256 * 1024
       
        self.proxies = {}
        enableProxy = self.config.get("enableProxy",False)
        if enableProxy:
            self.proxies = self.config.get("proxies",{})

        self.sqs_client = boto3.client("sqs",aws_access_key_id = self.AWSKey,
                                    aws_secret_access_key = self.AWSSecretKey,
                                    region_name=self.AWSRegion,
                                    verify=False, config=botocore.client.Config(proxies=self.proxies))
        
        if (self.enableDeadLetterQueue):
            self.sqs_resource = boto3.resource("sqs", region_name=self.AWSRegion)

        self.mqQueue = {}

        self.Attributes = {
            'DelaySeconds':"0",
            'VisibilityTimeout':"900",
            'FifoQueue' : 'true',
            "ContentBasedDeduplication" : "true"
                            }
    
    def publish(self, routingKey, data, batchSize=None, metadata=None):
        if data != None:

            if not self.mqQueue.get(routingKey,''):
                 self.createSQSQueue(routingKey)
                 self.mqQueue[routingKey] = self.getQueueURL(routingKey)

            publish_queue_URL =  self.mqQueue.get(routingKey,'')
 
            if batchSize is None:
                batchSize=100
            batches = list(self.chunks(data, batchSize))
            self.processBatches(batches, metadata, routingKey, publish_queue_URL, batchSize)              
    
    def sendMessages(self, routingKey, dataJson, publish_queue_URL):
          response = self.sqs_client.send_message(
                    QueueUrl=publish_queue_URL,
                    MessageBody=dataJson,
                    MessageGroupId = routingKey
                )
        
    def subscribe(self, routingKey, callback=None):
        try:
            self.mqQueue[routingKey] = self.getQueueURL(routingKey)
            if not self.mqQueue.get(routingKey,''):
                self.createSQSQueue(routingKey)
        except Exception as ex:
            logging.exception("Couldn't receive messages from queue: %s", routingKey)
            raise ex

    def subscribeMessages(self,routingKey, sub_queue_URL):
        messages = self.sqs_client.receive_message(
                    QueueUrl=sub_queue_URL,
                    MessageAttributeNames=['All'],
                    MaxNumberOfMessages= self.config.get('mqConfig').get('prefetchCount',10),
                    WaitTimeSeconds=10 
                )

        return messages.get("Messages",[])

    def getQueueURL(self,routingKey):
        queueName = routingKey.replace('.','_') + '.fifo'
        return self.sqs_client.get_queue_url(QueueName = queueName).get('QueueUrl')

    def acknowledgeMessages(self, sub_queue_URL, receipt_handle):
        self.sqs_client.delete_message(
                    QueueUrl=sub_queue_URL,
                    ReceiptHandle=receipt_handle
                )

    def createSQSQueue(self, routingKey):
        try:
           queueName = routingKey.replace('.','_') + '.fifo'
           originalAttribute = self.Attributes
           response = self.sqs_client.create_queue(QueueName=queueName,
                                                Attributes = self.Attributes)
           source_queue_url = self.getQueueURL(routingKey)           
           if (self.enableDeadLetterQueue):
               queueNameDL = routingKey.replace('.','_') + '_dl_queue.fifo'
               self.createDLQueue( source_queue_url,queueNameDL)            
           self.Attributes = originalAttribute
        except Exception as ex:
                logging.debug("Error while creating createSQSQueue ")
                logging.debug(ex)
                
    def createDLQueue(self,source_queue_url, queueName):
        try:
            dl_queue = self.sqs_client.create_queue(
                    QueueName = queueName,
                    Attributes = {"FifoQueue" : "true","ContentBasedDeduplication" : "true"}
                    )    
            dl_arn = self.sqs_client.get_queue_attributes(
                QueueUrl = dl_queue["QueueUrl"],
                AttributeNames = ["QueueArn"]
            )   
            policy = {"maxReceiveCount": "3", "deadLetterTargetArn": dl_arn["Attributes"]["QueueArn"]}
            policy = json.dumps(policy)                        
            redrive = {"RedrivePolicy": policy}
            self.sqs_client.set_queue_attributes(
                QueueUrl= source_queue_url,
                Attributes= redrive
            )
            
        except Exception as ex:
                logging.debug("Error while creating createDLQueue ")
                logging.debug(ex)
        
    def processBatches(self, batches, metadata, routingKey, publish_queue_URL, batchSize):
        for batch in batches:
            messageSize = self.getMessageSize(batch)
            if messageSize > self.SQS_MAX_MSG_SIZE:
                subBatches = list(self.chunks(batch, math.ceil(len(batch) / 2)))
                self.processBatches(subBatches, metadata, routingKey, publish_queue_URL, batchSize)
            else:
                dataJson = self.buildMessageJson(batch, metadata)
                self.sendMessages(routingKey, dataJson, publish_queue_URL )
             