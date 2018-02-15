#copyright 2017 Cognizant Technology Solutions
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
#from _ast import If
'''
Created on Jan 25, 2018

@author: 180852
'''

import pika
import sys
import zipfile
import tarfile
import os
import subprocess
import logging.handlers
import json

class AgentDaemonExecutor:
    def __init__(self):
        self.loadConfig()
        self.setupLogging()
        self.initializeMQ()
        self.subscribe()
        
    def loadConfig(self):
        filePresent = os.path.isfile('config.json')
        if filePresent:
            self.configFilePath = 'config.json'
            self.logFilePath = 'log_'+type(self).__name__+'.log'
        else:
            agentDir = os.path.dirname(sys.modules[self.__class__.__module__].__file__) + os.path.sep
            self.configFilePath = agentDir+'config.json'
            self.logFilePath = 'log_'+type(self).__name__+'.log'
            
        with open(self.configFilePath, 'r') as config_file:    
            self.config = json.load(config_file)
        if self.config == None:
            raise ValueError('BaseAgent: unable to load config JSON')

    def setupLogging(self):
        loggingSetting = self.config.get('loggingSetting',{})
        maxBytes = loggingSetting.get('maxBytes', 1000 * 1000 * 5)
        backupCount = loggingSetting.get('backupCount', 1000)
        handler = logging.handlers.RotatingFileHandler(self.logFilePath, maxBytes=maxBytes, backupCount=backupCount)
        formatter = logging.Formatter('%(asctime)s - %(levelname)s - %(filename)s - %(funcName)s - %(message)s')
        handler.setFormatter(formatter)
        logging.getLogger().setLevel(loggingSetting.get('logLevel',logging.WARN))
        logging.getLogger().addHandler(handler)

    def initializeMQ(self):
        mqConfig = self.config.get('mqConfig', None)
        if mqConfig == None:
            raise ValueError('BaseAgent: unable to initialize MQ. mqConfig is not found')
        
        user = mqConfig.get('user', None)
        password = mqConfig.get('password', None)
        host = mqConfig.get('host', None)
        exchangeName = mqConfig.get('exchange', None)
        credentials = pika.PlainCredentials(user, password)
        self.connection = pika.BlockingConnection(pika.ConnectionParameters(credentials=credentials,host=host))
        self.channel = self.connection.channel()
        self.channel.exchange_declare(exchange=exchangeName, exchange_type='topic', durable=True)
               
    def publish(self):
        jsonData = "abcdtest"
        self.channel.basic_publish(exchange='iAgent',
                              routing_key='Test_MQ_1',
                              body=jsonData,
         properties=pika.BasicProperties(
           delivery_mode=2 #make message persistent
         ))
        #self.connection.close() 
        
    def subscribe(self):
        routingKey = self.config.get('subscribe').get('agentPkgQueue')
        def callback(ch, method, properties, body):
             h = properties.headers
             fileName = h.get('fileName')
             osType = h.get('osType')
             agentToolName = h.get('agentToolName')
             #Code for handling subscribed messages
             ch.basic_ack(delivery_tag = method.delivery_tag)
             
             basePath = self.config.get('baseExtractionPath')
             f = open(basePath + fileName, 'wb')
             f.write(body)
             f.close()
             
             if osType == "WINDOWS":
                 zip_ref = zipfile.ZipFile(basePath + fileName, 'r')
                 zip_ref.extractall(basePath+agentToolName)
                 print('Zip File operation complete')
                 zip_ref.close()
             if osType == "UNIX":
                tar_ref = tarfile.open(basePath + fileName, 'r:gz')
                tar_ref.extractall(basePath+agentToolName)
                print('Tar file operation completed')
                tar_ref.close()
                
                '''
                 Give execution permission and then execute the script. Script should have all steps to handle Agent execution.
                 ''' 
                scriptPath = basePath + agentToolName + '/'+agentToolName+'.sh'
                print(scriptPath)
                p = subprocess.Popen(['chmod 777 '+scriptPath,scriptPath],shell=True)
                p = subprocess.Popen([scriptPath],cwd=basePath+agentToolName,shell=True)
                #stdout, stderr = p.communicate()
                print('Process id - '+ str(p.returncode))
                
             #self.channel.close(0, 'File Received')
        
        print('Inside subscribe method')    
        self.channel.basic_consume(callback,queue=routingKey)
        self.channel.start_consuming() 
        self.connection.close()    

if __name__=="__main__":
    AgentDaemonExecutor()
