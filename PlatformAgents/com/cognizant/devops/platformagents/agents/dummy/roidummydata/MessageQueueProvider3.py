#-------------------------------------------------------------------------------
# Copyright 2017 Cognizant Technology Solutions
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
'''
Created on Jun 15, 2016

@author: 146414
'''
import pika
import _thread
import json
import sys

class MessageFactory:
        
    def __init__(self, user, cred, host, exchange, port=None,enableDeadLetterExchange=False):
        #credentials = pika.PlainCredentials(user, cred)
        #self.connection = pika.BlockingConnection(pika.ConnectionParameters(credentials=credentials,host=host))
        #self.exchange = exchange
        #self.channels = {}
        self.user = user
        self.cred = cred
        self.host = host
        self.exchange = exchange
        if port != None : 
            self.port = port
        else:
            self.port = 5672
        if enableDeadLetterExchange:
            self.declareDeadLetterExchange()
            self.arguments={"x-dead-letter-exchange" : "iRecover"}
        else:
            self.arguments={}
        
    def subscribe(self, routingKey, callback, seperateThread=True):
        def subscriberThread():
            credentials = pika.PlainCredentials(self.user, self.cred)
            connection = pika.BlockingConnection(pika.ConnectionParameters(credentials=credentials,host=self.host,port=self.port))
            channel = connection.channel()
            queueName = routingKey.replace('.','_')
            channel.exchange_declare(exchange=self.exchange, exchange_type='topic', durable=True)
            channel.queue_declare(queue=queueName, passive=False, durable=True, exclusive=False, auto_delete=False, arguments=self.arguments)
            channel.queue_bind(queue=queueName, exchange=self.exchange, routing_key=routingKey, arguments=None)
            channel.basic_qos(prefetch_count=5)
            channel.basic_consume(routingKey,callback)
            channel.start_consuming()
        if seperateThread:
            _thread.start_new_thread(subscriberThread, ())
        else:
            subscriberThread()
            
    def publish(self, routingKey, data, batchSize=None, metadata=None):
        if data != None:
            credentials = pika.PlainCredentials(self.user, self.cred)
            connection = pika.BlockingConnection(pika.ConnectionParameters(credentials=credentials,host=self.host,port=self.port))
            channel = connection.channel()
            
            queueName = routingKey.replace('.','_')
            channel.exchange_declare(exchange=self.exchange, exchange_type='topic', durable=True)
            channel.queue_declare(queue=queueName, passive=False, durable=True, exclusive=False, auto_delete=False, arguments=self.arguments)
            channel.queue_bind(queue=queueName, exchange=self.exchange, routing_key=routingKey, arguments=None)
            #channel.exchange_declare(exchange=self.exchange, type='topic', exchange_type='topic', durable=True)
            #self.exchange = exchange
            #self.channels = {}
            #channel = self.channels.get(routingKey, None)
            #if channel == None:
            #    channel = self.connection.channel()
            #    self.channels[routingKey] = channel
            #    channel.exchange_declare(exchange=self.exchange, type='topic')
            #sys.getsizeof(dataJson)
            if batchSize is None:
                dataJson = self.buildMessageJson(data, metadata)
                channel.basic_publish(exchange=self.exchange, 
                                    routing_key=routingKey, 
                                    body=dataJson,
                                    properties=pika.BasicProperties(
                                        delivery_mode=2 #make message persistent
                                    ))
            else:
                baches = list(self.chunks(data, batchSize))
                for batch in baches:
                    dataJson = self.buildMessageJson(batch, metadata)
                    channel.basic_publish(exchange=self.exchange, 
                                    routing_key=routingKey, 
                                    body=dataJson,
                                    properties=pika.BasicProperties(
                                        delivery_mode=2 #make message persistent
                                    ))
            connection.close()        
    
    def buildMessageJson(self, data, metadata=None):
        messageJson = data
        if metadata:
            messageJson = {
                    'data' : data,
                    'metadata' : metadata
                }
        return json.dumps(messageJson)
    
    def chunks(self, l, n):
        for i in range(0, len(l), n):
            yield l[i:i + n]
    
    def closeConnection(self):
        self.connection.close()
        
    def declareDeadLetterExchange(self):
        
        credentials = pika.PlainCredentials(self.user, self.cred)
        connection = pika.BlockingConnection(pika.ConnectionParameters(credentials=credentials,host=self.host,port=self.port))
       
        #Create dead letter queue 
        channel = connection.channel()
        channel.exchange_declare(exchange='iRecover',exchange_type='fanout', durable=True)
 
        channel.queue_declare(queue='INSIGHTS_RECOVER_QUEUE', passive=False, durable=True, exclusive=False, auto_delete=False, arguments=None)

        channel.queue_bind(exchange='iRecover',
                           routing_key='INSIGHTS.RECOVER.QUEUE', # x-dead-letter-routing-key
                           queue='INSIGHTS_RECOVER_QUEUE')
