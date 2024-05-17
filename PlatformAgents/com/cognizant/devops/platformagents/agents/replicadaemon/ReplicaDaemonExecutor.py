#copyright 2024 Cognizant Technology Solutions
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
Created on March 05, 2024

@author: 2116386
'''

import sys
import os
import subprocess
import logging.handlers
import json
from datetime import datetime
from pytz import timezone
from ...core.MessageQueueProvider3 import MessageFactory
import uuid
from apscheduler.schedulers.blocking import BlockingScheduler
import tzlocal
import shlex
from pathvalidate import sanitize_filepath

class ReplicaDaemonExecutor:
    def __init__(self):
        self.loadConfig()
        self.setupLogging()
        self.loadVersionConfig()
        self.initializeDataProvider()
        self.subscribeForAgentPackage()
        if (self.mqProviderName != "RabbitMQ"):
            self.scheduleAgent()
        
        
        
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

    def loadVersionConfig(self):
        filePresent = os.path.isfile('version.json')
        self.version=''
        if filePresent:
            self.versionConfigFilePath = 'version.json'
        else:
            agentDir = os.path.dirname(sys.modules[self.__class__.__module__].__file__) + os.path.sep
            self.versionConfigFilePath = agentDir+'version.json'
            self.logFilePath = 'log_'+type(self).__name__+'.log'
            
        with open(self.versionConfigFilePath, 'r') as versionConfig_file:    
            self.versionConfig = json.load(versionConfig_file)
            #logging.info(' Version of Replica Demon ',self.versionConfig.get("version"))
            self.version=self.versionConfig.get("version")
        if self.versionConfig == None:
            self.version=''
            raise ValueError('BaseAgent: unable to load version config JSON')        

    def setupLogging(self):
        loggingSetting = self.config.get('loggingSetting',{})
        maxBytes = loggingSetting.get('maxBytes', 1000 * 1000 * 5)
        backupCount = loggingSetting.get('backupCount', 1000)
        handler = logging.handlers.RotatingFileHandler(self.logFilePath, maxBytes=maxBytes, backupCount=backupCount)
        formatter = logging.Formatter('%(asctime)s - %(levelname)s - %(filename)s - %(funcName)s - %(message)s')
        handler.setFormatter(formatter)
        logging.getLogger().setLevel(loggingSetting.get('logLevel',logging.WARN))
        logging.getLogger().addHandler(handler)
        
        default_sysout_formatter =logging.Formatter('t=%(asctime)s - lvl=%(levelname)s - filename=%(filename)s  - funcName=%(funcName)s - lineno=%(lineno)s message=%(message)s')
        sysoutHandler = logging.StreamHandler(sys.stdout)
        sysoutHandler.setLevel(loggingSetting.get('logLevel', logging.INFO))
        sysoutHandler.setFormatter(default_sysout_formatter)
        logging.getLogger().addHandler(sysoutHandler)
        
    def initializeDataProvider(self):
        logging.info('Inside initializeDataProvider')
        mqConfig = self.config.get('mqConfig', None)
        
        if mqConfig == None:
            raise ValueError('BaseAgent: unable to initialize Data Provide. mqConfig is not found')
        
        self.mqProviderName = mqConfig.get('providerName','RabbitMQ')

        self.messageFactory = MessageFactory.messageQueueHandler(self, self.config)
        
        if self.messageFactory == None:
            raise ValueError('BaseAgent: unable to initialize MQ. messageFactory is Null')

    def generateHealthData(self, ex=None, systemFailure=False,note=None):
        data = []
        currentTime = self.getRemoteDateTime(datetime.now())
        health = {'version':self.version, 'inSightsTime' : currentTime['epochTime'] ,'inSightsTimeX' : currentTime['time']}
        if systemFailure:
            health['status'] = 'failure'
            health['message'] = 'Agent is shutting down'
        elif ex != None:
            health['status'] = 'failure'
            health['message'] = 'Error occurred: '+str(ex)
            logging.error(ex)
        else:
            health['status'] = 'success'
            if note != None:
                health['message'] = note
        health['message'] = 'Replica Demon Version : '+self.version+' '+health['message']
        data.append(health)
        return data
               
    def publishDaemonHealthData(self, data):
        
        logging.info('Inside publishDaemonHealthData')
        self.executionId = str(uuid.uuid1())
        self.healthRoutingKey = self.config.get('publish').get('health')
        self.healthQueue = self.healthRoutingKey.replace('.','_')
        self.addExecutionId(data, self.executionId)
        self.messageFactory.publish(self.healthQueue, data)

    def subscribeForAgentPackage(self):
        logging.info('Inside subscribeForAgentPackage method')
        self.agentPkgroutingKey = self.config.get('subscribe').get('agentPkgQueue').replace('.','_')
        if (self.mqProviderName != "RabbitMQ"):
            self.messageFactory.subscribe(self.agentPkgroutingKey, self.callbackForAgentPackage)
        else:
            self.messageFactory.subscribe(self.agentPkgroutingKey, self.callbackForAgentPackage, seperateThread=False)

    
    def callbackForAgentPackage(self, ch, method, properties, body):
            try:
                self.process(body)
                ch.basic_ack(delivery_tag = method.delivery_tag)
            except Exception as ex:
                ch.basic_nack(delivery_tag = method.delivery_tag)
                logging.error(ex)
                self.publishDaemonHealthData(self.generateHealthData(ex))

    def process(self, body):
        try:
            replicas = []
            replicas_files = []
            dataReceived = json.loads(body)
            action = dataReceived.get('action')
            payload = dataReceived.get('payload')
            installagentFilePath = os.path.abspath(os.environ['INSIGHTS_AGENT_HOME'])  
            installagentFilePath = installagentFilePath + os.path.sep + 'ReplicaDaemon' 
            if action == 'RESYNC':
                source_ip = payload.get('source', {}).get('ip', None)
                kafka_endpoint = payload.get('source', {}).get('kafka_endpoint', None)
                for replica in payload.get('replicas', {}):
                    replicas.append(replica.get('ip', None))
                    replicas_files.append(replica.get('fileName', None))
                replicas=' '.join(replicas)
                replicas_files=' '.join(replicas_files)
                scriptFile = installagentFilePath + os.path.sep + 'resynch_all_replica.sh'
                scriptFile = sanitize_filepath(scriptFile)
                quotedScriptFile = shlex.quote(scriptFile)
                p = subprocess.run(['chmod', '777', quotedScriptFile])
                command = [scriptFile, source_ip, kafka_endpoint, replicas, replicas_files]
                p = subprocess.run(command, cwd=installagentFilePath)
                logging.info('Process id - ' + str(p.returncode))
            elif action == 'DELETE':
                replicas.append(payload.get('ip', None))
                replicas=' '.join(replicas)
                scriptFile = installagentFilePath + os.path.sep + 'delete_replica.sh'
                scriptFile = sanitize_filepath(scriptFile)
                quotedScriptFile = shlex.quote(scriptFile)
                p = subprocess.run(['chmod', '777', quotedScriptFile])
                command = [scriptFile, replicas]
                p = subprocess.run(command, cwd=installagentFilePath)
                logging.info('Process id - ' + str(p.returncode))
            else:
                logging.error('Invalid Action: ' + action)

        except Exception as ex:
            logging.error("Error While processing message/packet in processMessage")
            logging.error(body)
            logging.error(ex)


    def getRemoteDateTime(self, time):
        self.toolsTimeZone = timezone("UTC") #GMT
        self.epochStartDateTime = datetime(1970, 1, 1, tzinfo=self.toolsTimeZone)
        localDateTime = self.toolsTimeZone.localize(time)
        remoteDateTime = localDateTime.astimezone(self.toolsTimeZone)
        response = {
                    'epochTime' : (remoteDateTime - self.epochStartDateTime).total_seconds(),
                    'time' : remoteDateTime.strftime('%Y-%m-%dT%H:%M:%SZ')
                    }
        return response; 
    
    def scheduleAgent(self):
        logging.info('Inside scheduleAgent')
        
        scheduler = BlockingScheduler(timezone=str(tzlocal.get_localzone()))
        self.scheduler = scheduler
        self.scheduledJob = scheduler.add_job(self.fetchAndProcessPackage, 'interval', seconds=60 * 1)
        try:
            scheduler.start()
        except (KeyboardInterrupt, SystemExit):
            self.publishHealthData(self.generateHealthData(systemFailure=True))
        
    
    def fetchAndProcessPackage(self):
        logging.info('fetchAndProcessPackage ============== ')
        sub_queue_URL = self.messageFactory.getQueueURL(self.agentPkgroutingKey)
        fetchData= True
        
        while fetchData:
            messages = self.messageFactory.subscribeMessages(self.agentPkgroutingKey, sub_queue_URL)
        
            if (len(messages) == 0):
                logging.info('No message pending for process in processFetchData')
                fetchData = False
            for msg in messages:
                try:
                    messageBody = msg["Body"]
                    receipt_handle = msg['ReceiptHandle']
                    self.processMessage(messageBody)
                    self.messageFactory.acknowledgeMessages(sub_queue_URL, receipt_handle)
                except Exception as ex:
                    logging.error(ex)
                    self.publishHealthDataForExceptions(ex)
         
    def addExecutionId(self, data, executionId):
        logging.info('Inside addExecutionId')
        for d in data:
            d['execId'] = executionId

if __name__=="__main__":
    ReplicaDaemonExecutor()
