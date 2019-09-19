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
import thread
import json
import sys

class MessageFactory:
        
    def __init__(self, user, password, host, exchange):
        #credentials = pika.PlainCredentials(user, password)
        #self.connection = pika.BlockingConnection(pika.ConnectionParameters(credentials=credentials,host=host))
        #self.exchange = exchange
        #self.channels = {}
        self.user = user
        self.password = password
        self.host = host
        self.exchange = exchange
        
    def subscribe(self, routingKey, callback, seperateThread=True):
        def subscriberThread():
            credentials = pika.PlainCredentials(self.user, self.password)
            connection = pika.BlockingConnection(pika.ConnectionParameters(credentials=credentials,host=self.host))
            channel = connection.channel()
            channel.exchange_declare(exchange=self.exchange, exchange_type='topic', durable=True)
            channel.basic_consume(callback,queue=routingKey,no_ack=False)
            channel.start_consuming()
        if seperateThread:
            thread.start_new_thread(subscriberThread, ())
        else:
            subscriberThread()
            
    def publish(self, routingKey, data, batchSize=None, metadata=None):
        if data != None:
            credentials = pika.PlainCredentials(self.user, self.password)
            connection = pika.BlockingConnection(pika.ConnectionParameters(credentials=credentials,host=self.host))
            channel = connection.channel()
            
            queueName = routingKey.replace('.','_')
            channel.exchange_declare(exchange=self.exchange, exchange_type='topic', durable=True)
            channel.queue_declare(queue=queueName, passive=False, durable=True, exclusive=False, auto_delete=False, arguments=None)
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
