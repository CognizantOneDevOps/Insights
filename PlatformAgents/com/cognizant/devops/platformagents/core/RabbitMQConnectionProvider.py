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

import _thread
import logging.handlers
import pika
import json

from .MessageAbstract import MessageAbstract

class RabbitMQConnectionProvider(MessageAbstract):
    
    def __init__(self, config):
        
        logging.debug('Inside init of RabbitMQConnectionProvider =======')
        mqConfig = config.get('mqConfig','None')
        self.user = mqConfig.get('user', None)
        self.cred = mqConfig.get('password', None)
        self.host = mqConfig.get('host', None)
        self.exchange = mqConfig.get('exchange', None)
        port = mqConfig.get('port', 5672)
        enableDeadLetterExchange = mqConfig.get('enableDeadLetterExchange', False)
        self.prefetchCount = mqConfig.get('prefetchCount', 10)
        
        if port != None : 
            self.port = port
        else:
            self.port = 5672
            
        if enableDeadLetterExchange:
            self.declareDeadLetterExchange()
            self.arguments={"x-dead-letter-exchange" : "iRecover"}
        else:
            self.arguments={}
            
        try:    
            self.credentials = pika.PlainCredentials(self.user, self.cred)
            self.connection = pika.BlockingConnection(pika.ConnectionParameters(credentials=self.credentials,host=self.host,port=self.port))
        except pika.exceptions.ConnectionClosed as exc:
            logging.error('In init connection closed... and restarted '+exc.__class__.__name__)
            logging.error(str(exc))
        except Exception as ex:
            logging.error('In Exception connection closed... and restarted'+ex.__class__.__name__)
            logging.error(str(ex))
            
    def subscribe(self, routingKey, callback, seperateThread=True):
        def subscriberThread():
            credentials = pika.PlainCredentials(self.user, self.cred)
            subconnection = pika.BlockingConnection(pika.ConnectionParameters(credentials=credentials,host=self.host,port=self.port))
            channel = subconnection.channel()
            queueName = routingKey.replace('.','_')
            channel.exchange_declare(exchange=self.exchange, exchange_type='topic', durable=True)
            channel.queue_declare(queue=queueName, passive=False, durable=True, exclusive=False, auto_delete=False, arguments=self.arguments)
            channel.queue_bind(queue=queueName, exchange=self.exchange, routing_key=routingKey, arguments=None)
            channel.basic_qos(prefetch_count=self.prefetchCount)
            
            channel.basic_consume(routingKey,callback)

            channel.start_consuming()
            channel.close()
        if seperateThread:
            _thread.start_new_thread(subscriberThread, ())
        else:
            subscriberThread()
            
    
    def publish(self, routingKey, data, batchSize=None, metadata=None):
        if data != None:
            #credentials = pika.PlainCredentials(self.user, self.cred)
            #connection = pika.BlockingConnection(pika.ConnectionParameters(credentials=credentials,host=self.host,port=self.port))
            try:
                if self.connection.is_closed:
                    logging.debug('In publish block, Connection close .... restarting connection')
                    self.credentials = pika.PlainCredentials(self.user, self.cred)
                    self.connection = pika.BlockingConnection(pika.ConnectionParameters(credentials=self.credentials,host=self.host,port=self.port))
                    
                channelpub = self.connection.channel()
            except Exception as ex:
                logging.error('In publish block, for Exception connection closed and restarted ....'+ex.__class__.__name__)
                logging.error(str(ex))
                self.credentials = pika.PlainCredentials(self.user, self.cred)
                self.connection = pika.BlockingConnection(pika.ConnectionParameters(credentials=self.credentials,host=self.host,port=self.port))
                channelpub = self.connection.channel()
            
            queueName = routingKey.replace('.','_')
            channelpub.exchange_declare(exchange=self.exchange, exchange_type='topic', durable=True)
            channelpub.queue_declare(queue=queueName, passive=False, durable=True, exclusive=False, auto_delete=False, arguments=self.arguments)
            channelpub.queue_bind(queue=queueName, exchange=self.exchange, routing_key=routingKey, arguments=None)
            
            if batchSize is None:
                dataJson = self.buildMessageJson(data, metadata)
                channelpub.basic_publish(exchange=self.exchange, 
                                    routing_key=routingKey, 
                                    body=dataJson,
                                    properties=pika.BasicProperties(
                                        delivery_mode=2 #make message persistent
                                    ))
            else:
                baches = list(self.chunks(data, batchSize))
                for batch in baches:
                    dataJson = self.buildMessageJson(batch, metadata)
                    channelpub.basic_publish(exchange=self.exchange, 
                                    routing_key=routingKey, 
                                    body=dataJson,
                                    properties=pika.BasicProperties(
                                        delivery_mode=2 #make message persistent
                                    ))
            #connection.close()
            channelpub.close()
            
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
        #connection.close()

