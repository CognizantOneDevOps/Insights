# -------------------------------------------------------------------------------
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
# -------------------------------------------------------------------------------
"""
Created on 15 May 2020

@author: 302683
"""
import csv
import logging.handlers
import multiprocessing
import sys
import time
import json
import docker
import elasticsearch
import elasticsearch.helpers
import thread
import pika
import Queue
import os
import shutil
import stat
import tarfile

from ....core.MessageQueueProvider import MessageFactory
from ....core.BaseAgent import BaseAgent


#from com.cognizant.devops.platformagents.core.MessageQueueProvider import MessageFactory
#from com.cognizant.devops.platformagents.core.BaseAgent import BaseAgent

from dateutil import parser

#################################################################
global myqueue


def getCSVHeaderFromSchemaIndex(esHelperObj,indexName,logs):
    datatypeMapping = {'String':'','Boolean': ':BOOLEAN', 'Integer':':INT', 'Long':':LONG','Double':':DOUBLE','StringArray':':STRING[]','IntegerArray':':INT[]','DoubleArray':':DOUBLE[]'}
    neo4j_Meta_Types = {'uuid':'uuid:ID' ,'_label':':LABEL' ,'_start':':START_ID' ,'_end':':END_ID','relationshipName':':TYPE' }

    forceDataConversion = {'inSightsTime':u'inSightsTime:DOUBLE'}

    csvHeaderList = {}

    nodeBody = {
                "size": 1,
                "query": {
                        "match_all": {}
                    }

            }
    schemaIndex = indexName+"_neo4j_schema"

    ESResponse = elasticsearch.helpers.scan(esHelperObj, index=schemaIndex, doc_type="_doc",
                                query={"query": nodeBody["query"]},
                                scroll='10s', raise_on_error=True, preserve_order=False,
                                size=1,
                                request_timeout=None)


    for doc in ESResponse:

            my_dict = doc['_source']

    #1. Modify Data type headers -eg 'insightsTime' ==> 'insightsTime:DOUBLE'
    for k,v in my_dict.items():
        if v:
            #forced conversion? Eg inSightsTime ==> inSightstime:DOUBLE
            if forceDataConversion.get(k) != None:
                #Take first element in data array and append. If other values are there, ignored
                csvHeaderList[k] = forceDataConversion[k]

            #Neo4j meta type? Eg _label ==> :LABEL,etc
            elif neo4j_Meta_Types.get(k) != None:
                csvHeaderList[k] = neo4j_Meta_Types[k]

            #Standard Data conversion - eg jiraKeys ==> jiraKeys:String[]
            else:
                #ERROR:::: IF Data type not found exit
                if datatypeMapping.get(v[0]) == None:
                    raise Exception('ERROR - SCHEMA DATA TYPE NOT FOUND',v[0])

                #All Clear,
                csvHeaderList[k] = k+datatypeMapping.get(v[0])
                logs.append("Neo4J_Schema_Read ---> Key: "+k+"....New Value: "+csvHeaderList[k])

    logs.append("Neo4J_Schema_Read Done..."+str(indexName))
    return csvHeaderList


#################################################################
def getCSVHeaderFromESIndex(esHelperObj,indexName1,indexType1,logs):
    datatypeMapping = {'boolean': ':BOOLEAN', 'integer':':INT','float': ':FLOAT', 'date':':DATETIME','long':':LONG','double':':DOUBLE'}
    excludeList = {'uuid:ID':0 ,':LABEL':0 ,':START_ID':0 ,':END_ID':0,':TYPE':0, 'inSightsTime:DOUBLE':0 }
    headerCreatedList = {}
    csvHeaderList = {}
    indexMapping = esHelperObj.indices.get_mapping(index=indexName1)

    #1. Start building our new headers
    temp=indexMapping[indexName1]['mappings']['_doc']['properties']

    #Int dict to old value = old value "insightsTime":"insightsTime", "uuid":"ID"
    csvHeaderList = {key: key for key in temp}

    #1. Neo4j META Tyes -  Rename colums for neo4j CSV compatibility
    # these are added in excluded list
    if indexType1=="node":
        temp['uuid:ID'] = temp.pop('uuid')
        temp[':LABEL'] = temp.pop('_label')
        temp['inSightsTime:DOUBLE'] = temp.pop('inSightsTime')

        #store the new label names
        csvHeaderList['_label'] = ':LABEL'
        csvHeaderList['uuid'] = 'uuid:ID'
        csvHeaderList['inSightsTime'] = 'inSightsTime:DOUBLE'

    elif indexType1=="relationship":
        temp[':END_ID'] = temp.pop('_end')
        temp[':START_ID'] = temp.pop('_start')
        temp[':TYPE'] = temp.pop('relationshipName')

        #store the new label names
        csvHeaderList['_start'] = ':START_ID'
        csvHeaderList['_end'] = ':END_ID'
        csvHeaderList['relationshipName'] = ':TYPE'

    #2. Append Data types to the non - neo4j column "starttime"-->"starttime:DATE"
    #   neo4j  cols are deined in excludeList[]

    for i, k in enumerate(temp):
        type = temp[k]['type']

        # If NEO4J type substitue exists, rename col header for eg copyToEs becomes copyToEs:BOOLEAN
        if type in datatypeMapping and k not in excludeList:

            #get corresponding datatype for Neo4j
            colAppendType = datatypeMapping[type]

            # Edge Case - If new header not created  already, create
            if k not in headerCreatedList:
                temp[k+colAppendType] = temp.pop( k )
                headerCreatedList[k+colAppendType] = 1
                csvHeaderList[k] = k+colAppendType
                logs.append('Old Type....'+k+'...new Type......'+csvHeaderList[k])

    logs.append("Headers from ES Index::{0}".format(csvHeaderList))
    
    return csvHeaderList
    #exit()
#*********************************************************

def modifyDictKeys(dict,indexType1,csvHeaders):
    headerCreatedList = {}

    #Take  copy of the dict, and modify original dict keys
    #Python does not allow to run thro same dict and moodify

    dictTemp=dict.copy()

    for headerVal, v in dictTemp.items():

        #Get the new headerval from dict
        #Change header to new values  -eg "uuid"--> ":ID", "starttime"-->"starttime:DATE"

        if csvHeaders.get(headerVal) == None:
            raise Exception('ERROR - SCHEMA FIELD   NOT FOUND',headerVal)

        newHeaderVal = csvHeaders[headerVal]
        dict[newHeaderVal] = dict.pop(headerVal)

        #Convert  "True"==>"true"
        if ":BOOLEAN" in  newHeaderVal:
            dict[newHeaderVal] = str(dict[newHeaderVal]).lower()

        #Delimit arrays, eg..["1","2"]==>1^2
        elif "[]" in newHeaderVal and v!=None:
            dict[newHeaderVal] = "^".join(v)


    return dict

def getAllIndices (self,elasticsearch_hostname_uri,csv_download_path):
    self.baseLogger.info("INFO: Get all ES indices")
    allIndices={}
    global neo4jImportCmd
    #neo4jImportCmd = "./bin/neo4j-admin import  --delimiter=\",\" --array-delimiter=\"^\"  --multiline-fields=true "
    neo4jImportCmd = "./bin/neo4j-import  --delimiter=\",\" --array-delimiter=\"^\"  --multiline-fields=true --ignore-empty-strings=true  --into data/databases/graph.db/"

    esHelperObj = elasticsearch.Elasticsearch(hosts=elasticsearch_hostname_uri,http_auth=(self.getCredential('elasticsearch_username'), self.getCredential('elasticsearch_passwd')))
    
    indices = esHelperObj.indices.get_alias("*")

    for k,v in indices.items():

        #Get only the node and relation indices, ignore scheme indices
        if "_neo4j_schema" not in k:
            index_map = esHelperObj.indices.get_mapping(k)
            if int(esHelperObj.info().get('version').get('number').split('.')[0]) > 6:
                temp=index_map[k]['mappings']['properties']
            if int(esHelperObj.info().get('version').get('number').split('.')[0]) == 6:
                temp=index_map[k]['mappings']['_doc']['properties']

            if temp.get("uuid")!=None and temp.get("_label")!=None and temp.get("inSightsTime")!=None:
                allIndices[k] = "node"
                neo4jImportCmd = neo4jImportCmd +" "+"--nodes=import/"+k+".csv"
            elif temp.get("_start")!=None and temp.get("_end")!=None and temp.get("relationshipName")!=None:
                allIndices[k] = "relationship"
                neo4jImportCmd = neo4jImportCmd +" "+"--relationships=import/"+k+".csv"


    esHelperObj.transport.close()

    #DEBUG - WRITE import command
    #f = open(csv_download_path +  'neo4jImport.sh', 'w')
    #f.write( neo4jImportCmd)
    #f.close()

    return allIndices

#############################################################
def es_write_csv(es_username,es_password,ESIndex, ESIndexType, elasticsearch_hostname_uri,fetch_all_data, start_time, finish_time, csv_download_path,ns,nodeErrors,logs):
    try:

        #_es = elasticsearch.Elasticsearch(hosts=elasticsearch_hostname_uri)
        _es = elasticsearch.Elasticsearch(hosts=elasticsearch_hostname_uri,http_auth=(es_username, es_password))
        if not _es.ping():
            raise Exception("ERROR:: FAILED CONNECT TO ES....",elasticsearch_hostname_uri)



        #If fetch all data ignore time range
        if fetch_all_data==0:
            relationshipBody = {
                "size": 10000,
                "query": {
                    "bool": {
                        "must": [
                            {"range": {"time_node_start": {"gte": start_time, "lte": finish_time}}},
                            {"range": {"time_node_end": {"gte": start_time, "lte": finish_time}}}
                        ]
                    }
                }
            }
            nodeBody = {
                "size": 10000,
                "query": {
                    "bool": {
                        "must": [
                            {"range": {"inSightsTime": {"gte": start_time, "lte": finish_time}}}
                        ]
                    }
                }
            }

        else :

            relationshipBody = {
                "size": 10000,
                  "query": {
                        "match_all": {}
                    }
            }
            nodeBody = {
                "size": 10000,
                "query": {
                        "match_all": {}
                    }

            }

        csvHeader = getCSVHeaderFromSchemaIndex(_es,ESIndex,logs)

        #get the CSV Header from Index
        #csvHeader = getCSVHeaderFromESIndex(_es,ESIndex,ESIndexType,logs)

        if ESIndexType == 'node':
            ESResponse = elasticsearch.helpers.scan(_es, index=ESIndex, doc_type="_doc",
                                                    query={"query": nodeBody["query"]},
                                                    scroll='10s', raise_on_error=True, preserve_order=False,
                                                    size=10000,
                                                    request_timeout=None)
        elif ESIndexType == 'relationship':
            ESResponse = elasticsearch.helpers.scan(_es, index=ESIndex, doc_type="_doc",
                                                    query={"query": relationshipBody["query"]},
                                                    scroll='10s', raise_on_error=True, preserve_order=False,
                                                    size=10000,
                                                    request_timeout=None)
        else:
            raise Exception("ERROR::Please provide Node or Relation")

        i=0

        ## To encode " character , in unicode, facing errors \u201
        # Fix as per - https://github.com/wireservice/agate/issues/624
        reload(sys)

        sys.setdefaultencoding('utf8')

        #Check valid download path
        if not os.path.exists(csv_download_path):
            raise Exception("Invalid CSV Download Path....",csv_download_path)

        with open(csv_download_path + "/"+ESIndex + '.csv', 'w') as f:
            w = csv.DictWriter(f,fieldnames=csvHeader.values(),escapechar="\\",quoting=csv.QUOTE_ALL)
            w.writeheader()
            for doc in ESResponse:
                my_dict = doc['_source']

                # 1.Modify Dict Key Names to Neo4j Standard , append Data types
                # eg. 'uuid' --> ':ID', '_labels' --> ':LABEL'
                # Append Datatype , eg:  'insightsTime' --> 'insightsTime:DATE'
                my_dict = modifyDictKeys(my_dict, ESIndexType, csvHeader)
                # 3. Write Row
                w.writerow(my_dict)
                i=i+1



    except Exception as ex:

        #ex_type, ex_value, ex_traceback = sys.exc_info()
        nodeErrors[ESIndex] = "Exception: "+str((ex.__class__))+"; Description: "+ str(ex)+";"

        #Write Error
        if os.path.exists(csv_download_path):
            f = open(csv_download_path +"/"+ ESIndex + '_ERROR.csv', 'w')
            f.write(nodeErrors[ESIndex]+"\n")
            f.close()

        #MUST Return in case of exception, Else process won't  clean up
        return

    if ESIndexType == 'node':
        ns["totalNode"]=ns["totalNode"]+i
    elif ESIndexType == 'relationship':
        ns["totalRel"]=ns["totalRel"]+i

    log = "Write CSV  Done....index::"+ str(ESIndex) + "....Number of Row Written:"+str(i)
    logs.append(log)
    return




def entryproccess(self):
    try:

        while True:
            global myqueue


            self.baseLogger.info("INFO: Wait for RabbitMQ Message...")
            ##BLOCKING CALL
            queueData=myqueue.get()

            global neo4jImportCmd

            start = time.time()
            elasticsearch_hostname_uri = self.config.get('elasticsearch_hostname_uri', '')
            time_format = self.config.get('time_format', '')


            #start_time = self.get_epoch(self.config.get('start_time', ''), time_format)
            #finish_time = self.get_epoch(self.config.get('finish_time', ''), time_format)

            task = queueData.get('task',None)
            if task:
              if task=='remove_container':
                container_id = queueData["containerID"]
                try:

                  self.remove_docker_container(container_id)

                except Exception as ex:
                  self.baseLogger.error(ex)
                  self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))
            else:
              start_time = queueData["start_time"]
              finish_time = queueData["finish_time"]
              archivalName = queueData["archival_name"]

              es_indexes = self.config.get('es_indexes', {})
              no_of_processes = self.config.get('no_of_processes', 8)
              #csv_download_path = self.config.get('csv_download_path', '')
              fetch_all_data = self.config.get('fetch_all_data', 1)

              try:

                ports,volumes,csv_download_path = self.checkPortAvailability()
                if  len(ports) == 0:
                    self.baseLogger.error("ERROR::Docker --> No Free Ports Available")

                    ex = archivalName+" \n Docker ERROR::--> No Free Ports Available "
                    additionalProperties = {'archivalName':archivalName}
                    self.publishHealthDataForExceptions(ex, additionalProperties=additionalProperties)

                    continue
                #if "*":"*" get all the indices
                if es_indexes.get("*")!=None:
                    es_indexes = getAllIndices(self,elasticsearch_hostname_uri,csv_download_path)

                es_username = self.getCredential('elasticsearch_username')
                es_password = self.getCredential('elasticsearch_passwd')
                pool = multiprocessing.Pool(no_of_processes)

                #Shared variable reset
                manager = multiprocessing.Manager()
                ns = manager.dict()
                ns["totalNode"] = 0
                ns["totalRel"] = 0
                logs = manager.list()
                nodeErrors = manager.dict()

                result_async = [pool.apply_async(es_write_csv,(es_username,es_password,k, v, elasticsearch_hostname_uri,fetch_all_data, start_time, finish_time, csv_download_path,ns,nodeErrors,logs))
                                for k, v in es_indexes.items()]

                results = [r.get() for r in result_async]


                pool.close()
                pool.join()
                
                #printing logs from the multiprocessing methods
                for log in logs:
                    self.baseLogger.debug(log)

                no_error=not bool(nodeErrors)

                if(no_error):
                    tar_path = self.tardir(csv_download_path,'import.tar')
                    self.docker_container_with_neo4j(neo4jImportCmd,ports,volumes,ns,nodeErrors,archivalName,tar_path)
                else:
                    #Build error message  
                    ex= archivalName
                    for key in nodeErrors.keys():
                        ex=ex+"\n IndexError:: "+key + "--->"+nodeErrors[key]

                    self.baseLogger.error("Error Downloading Index. ABORT Docker  Create")
                    additionalProperties = {'archivalName':archivalName}
                    self.publishHealthDataForExceptions(ex, additionalProperties=additionalProperties)
          
                neo4jImportCmd=''

              except Exception as ex:
                self.baseLogger.error(ex)
                self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))

              self.baseLogger.info('Exit  Main Process...Time Elapased:'+str(time.time()-start)+"...TotalNodes:"+str(ns["totalNode"])+"..REls:"+str(ns["totalRel"])+"....Errors"+str(nodeErrors.keys()))

              #Shutdown
              manager.shutdown()
              #queue_channel.basic_ack(delivery_tag = queue_method.delivery_tag)

    except (KeyboardInterrupt, SystemExit,EOFError):
        self.baseLogger.error("exception")
        self.publishHealthData(self.generateHealthData(systemFailure=True))


class ElasticTransferAgent(BaseAgent):
    global neo4jImportCmd,finish_time,start_time
    global myqueue

    myqueue = Queue.Queue()
    neo4jImportCmd=''
    start_time=''
    finish_time=''


    def configUpdateSubscriber(self):
        try: 

            # Create "dataArchivalQueue" when queue doen't exist.
            credentials = pika.PlainCredentials(self.config.get('mqConfig').get('user'), self.config.get('mqConfig').get('password'))
            connection = pika.BlockingConnection(pika.ConnectionParameters(credentials=credentials,host=self.config.get('mqConfig').get('host')))
            channel = connection.channel()
            queue=self.config.get('subscribe').get('dataArchivalQueue').replace('.','_')
            channel.queue_declare(queue=queue,durable=True,)

            # Consume "dataArchivalQueue"
            routingKey = self.config.get('subscribe').get('dataArchivalQueue').replace('.','_')
            self.messageFactory.subscribe(routingKey, self.callback)
            self.baseLogger.info("INFO: Connected to RabitMQ..")

        except Exception as ex:
            self.baseLogger.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))


    def subscriberForAgentControl(self):
        routingKey = self.config.get('subscribe').get('agentCtrlQueue')
        def callback(ch, method, properties, data):

            self.baseLogger.info("RabbitMQ -> Agent ctrl Queue")
            #Update the config file and cache.
            action = data

            ch.basic_ack(delivery_tag = method.delivery_tag)

            if "STOP" == action.decode():
                self.baseLogger.info("AGENT STOP Message Received")

                self.publishHealthData(self.generateHealthData(note="Agent is in STOP mode"))
                os._exit(0)

        self.agentCtrlMessageFactory.subscribe(routingKey, callback)

    @BaseAgent.timed    
    def process(self):
        
        self.publishHealthData(self.generateHealthData())

        ##Subscribe for Agent "STOP" message from UI
        self.subscriberForAgentControl()

        #Init Queue from TaskWorkflow
        self.configUpdateSubscriber()

        ###AGENT BLOCKS HERE - Control not returned to BaseAgent
        entryproccess(self)

    #Queue callback from RabbitMQ
    def callback(self,ch, method, properties, data):

        self.baseLogger.info("INFO:: RabbitMQ data Callback")
        data=json.loads(data)
    
        global myqueue
        if data.get('task',None):
          task = data.get('task',None)
          container_id = data.get('containerID',None)
          queueData={}
          queueData['task']=task
          queueData['containerID']=container_id
          myqueue.put(queueData)
          self.baseLogger.info("*************queue inserted************")
          #Acknowledge data received
          ch.basic_ack(delivery_tag = method.delivery_tag)
        else:
          start_time=data['startDate']
          finish_time=data['endDate']
          archival_name=data['archivalName']
          self.baseLogger.info(start_time+" "+finish_time)
          days_to_retain=data['daysToRetain']
          #days_to_retain=data.get('daysToRetain',None)

          time_format = self.config.get('time_format', '')
          start_time = self.get_epoch(start_time, time_format)
          finish_time = self.get_epoch(finish_time, time_format)

          #insert data into Global Queue for Es2CSV
          queueData={}
          queueData['start_time']=start_time
          queueData['finish_time']=finish_time
          queueData['archival_name']=archival_name

          myqueue.put(queueData)
          self.baseLogger.info("**************queue inserted**********")

          #Acknowledge data received
          ch.basic_ack(delivery_tag = method.delivery_tag)


    def get_epoch(self, time_string, time_format):
        try:
            startFrom = parser.parse(time_string, ignoretz=True)
            epoch = str(int(time.mktime(time.strptime(str(startFrom), time_format))))
            return epoch
        except Exception as ex:
            self.baseLogger.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))

    def checkPortAvailability(self):
        self.baseLogger.info("INFO::Checking port availability")
        bindPort=self.config.get('dynamicTemplate', {}).get('bindPort','')
        hostPort=self.config.get('dynamicTemplate', {}).get('hostPort','')
        hostVolume=self.config.get('dynamicTemplate', {}).get('hostVolume','')
        mountVolume=self.config.get('dynamicTemplate', {}).get('mountVolume','')
        hostAddress=self.config.get('hostAddress','')
        portlen=len(hostPort)
        volumelen=len(mountVolume)

        #Init return Vars
        port={}
        volume=[]
        csv_download_path = ''
        #volumeindex=1
        cli= docker.APIClient(base_url='tcp://'+self.config.get('dockerHost','')+':'+ str(self.config.get('dockerPort','')))
        containers = cli.containers()
        
        allocatedPorts =[]
        for container in containers:
            for each_port in container['Ports']:
                allocatedPorts.append(each_port['PublicPort'])

        for x in range(0,portlen,2):
            volumeindex = x
            if(hostPort[x] not in allocatedPorts):
                port[bindPort[0]]=(hostAddress,hostPort[x])
                port[bindPort[1]]=(hostAddress,hostPort[x+1])

                #Volumes - Port1 - "Data1", "import1", "log1", "conf1"
                # Port 2 -   "Data2", "conf2", etc

                self.baseLogger.info("INFO::Docker Port Allocated Success... HOST--->"+hostAddress+"-->Neo4j:"+str(hostPort[x]) +"...BOLT:"+str(hostPort[x+1]))

                for i in range(0,4):
                    if "import" in hostVolume[i]:
                        csv_download_path = os.path.join(os.environ['INSIGHTS_HOME'],"es_importcsv",hostVolume[i]+str(volumeindex))
                        if os.path.exists(csv_download_path):
                            os.chmod(csv_download_path, stat.S_IWRITE)
                            shutil.rmtree(csv_download_path)
                            os.makedirs(csv_download_path, mode=777)
                        else:
                            os.makedirs(csv_download_path, mode=777)
                    #e.g. (import0:/opt/NEO4J_HOME/neo4j-Insights/import)        
                    volume.append(hostVolume[i]+str(volumeindex)+":"+mountVolume[i])
                        # recreate volume
                    cli = docker.APIClient(base_url='tcp://'+self.config.get('dockerHost','')+':'+ str(self.config.get('dockerPort','')))
                        #To handle volume not found exception
                    cli.create_volume(hostVolume[i]+str(volumeindex))
                    cli.remove_volume(hostVolume[i]+str(volumeindex))
                    cli.create_volume(hostVolume[i]+str(volumeindex))
                    self.baseLogger.info("INFO::Docker New volume :: "+hostVolume[i]+str(volumeindex)) 

                break
            
        return port,volume,csv_download_path

    def remove_docker_container(self,container_id):
        self.baseLogger.info("INFO::Removing Docker Instance")
        responseTemplate = self.getResponseTemplate()
        dataTransferMetadata = self.config.get('dynamicTemplate', {}).get('dataTransferMetadata', None)

        mq_response={}

        try:

            cli = docker.APIClient(base_url='tcp://'+self.config.get('dockerHost','')+':'+ str(self.config.get('dockerPort','')))
            cli.remove_container(container_id,force=True)

            mq_response["task"]="remove_container"
            mq_response["containerID"]=container_id
            mq_response["status"]="Success"
            self.baseLogger.info("containerID "+container_id+" Removed Succesfully")
            data=list()
            data += self.parseResponse(responseTemplate, mq_response)
            metadata = {
                          "dataUpdateSupported" : False
                       }
            self.publishToolsData(data,metadata)

        except Exception as ex:
            self.baseLogger.error(ex)
    
    #to create a tarfile from the extracted csv
    def tardir(self,path, tar_name): 
        tar_path = os.path.join(path,tar_name)      
        with tarfile.open(tar_path, "w") as tar:
            for files in os.listdir(path):
                csv_path = os.path.join(path, files)
                tar.add(csv_path, arcname=files)
        
        self.baseLogger.info("Created tar file from csv.")
        return tar_path

    def docker_container_with_neo4j(self,neo4jImportCmd,port,volume,ns,nodeErrors,archivalName,tar_path):
        self.baseLogger.info("INFO::Creating Docker Instance")
        responseTemplate = self.getResponseTemplate()
        dataTransferMetadata = self.config.get('dynamicTemplate', {}).get('dataTransferMetadata', None)

        mq_response={}


        try:

            cli= docker.APIClient(base_url='tcp://'+self.config.get('dockerHost','')+':'+ str(self.config.get('dockerPort','')))

            docker_image_not_found_flag = True
            dockerImage = self.config.get('dockerImageName','')+':'+self.config.get('dockerImageTag','')
            client_images =  cli.images()
            for images in client_images:
                if images['RepoTags'][0] == dockerImage:
                    docker_image_not_found_flag = False
                    self.baseLogger.info("Docker Image Found.")
                     
            if docker_image_not_found_flag == True:
              self.baseLogger.error("Docker Image Not Found.")
              docker_auth={'username':'','password':''}
              docker_auth['username']=self.getCredential('docker_repo_username')
              docker_auth['password']=self.getCredential('docker_repo_passwd')
              for line in cli.pull(dockerImage, auth_config=docker_auth, stream=True):
                  self.baseLogger.error(json.dumps(json.loads(line), indent=4 ))
            
            #fetch the import volume mapping for temporary container      
            temp_volume_bind = []
            for volume_config in volume:
                if "import" in volume_config:
                    temp_volume_bind.append(volume_config)
                    break
            
            #fetch the import mount point for temporary container
            temp_mountVolume = []
            for mount in self.config.get('dynamicTemplate', {}).get('mountVolume',''):
                if "import" in mount:
                    temp_mountVolume.append(mount)
                    break
            
            #create temporary container to connect csv files to a volume
            temp_container_id = cli.create_container(self.config.get('dockerImageName','')+':'+self.config.get('dockerImageTag',''), 'ls',volumes=temp_mountVolume,host_config=cli.create_host_config(binds=temp_volume_bind))
            csv_data = ''
            with open(tar_path, 'rb') as file:
                csv_data = file.read()
            #copy data from tar file to import volume to be used in main container later
            cli.put_archive(temp_container_id,temp_mountVolume[0],csv_data)
            cli.remove_container(temp_container_id)

            #####creating conatiner and importing csv ######
            container_id = cli.create_container(self.config.get('dockerImageName','')+':'+self.config.get('dockerImageTag',''), 'ls', ports=self.config.get('dynamicTemplate', {}).get('bindPort',''), volumes=self.config.get('dynamicTemplate', {}).get('mountVolume',''),environment=['COMMAND_HERE='+neo4jImportCmd],host_config=cli.create_host_config(port_bindings=port,binds=volume,privileged=True))
            response = cli.start(container=container_id.get('Id'))

            self.baseLogger.info("For archival record "+archivalName)
            self.baseLogger.info("Container ID: "+container_id.get('Id'))
                        #####getting status of spawned container#####
            container_details=cli.inspect_container(container_id["Id"])
            
            mq_response["sourceUrl"]="http://"+container_details["NetworkSettings"]["Ports"][str(self.config.get('dynamicTemplate', {}).get('bindPort','')[0])+"/tcp"][0]["HostIp"]+":"+container_details["NetworkSettings"]["Ports"][str(self.config.get('dynamicTemplate', {}).get('bindPort','')[0])+"/tcp"][0]["HostPort"]
            mq_response["archivalName"]=archivalName
            mq_response["containerID"]=container_id.get('Id',None)
            mq_response["boltPort"]= container_details["NetworkSettings"]["Ports"][str(self.config.get('dynamicTemplate', {}).get('bindPort','')[1])+"/tcp"][0]["HostPort"]
            if container_details["State"]["Status"]=="running":
                mq_response["status"]="Success"

            '''
                        #####getting list of conatiners and its details#####
            containers_list=cli.containers()
            print("***************************")
            for x in range(len(containers_list)):
                print("Id : "+containers_list[x]["Id"])
                print("Status : "+containers_list[x]["State"])
                print("Ports : "+str(containers_list[x]["Ports"]))
                print("Mounts : "+str(containers_list[x]["Mounts"]))
                print("***************")
                        #####executing command inside a conatiner#####
            '''
            neo4j_user_id = self.getCredential('neo4j_user_id')
            neo4j_password = self.getCredential('neo4j_password')
            exec_id=cli.exec_create(container_id["Id"],cmd=['/bin/bash','-c','cd neo4j-Insights && bin/cypher-shell -a bolt://localhost:7687 -u '+neo4j_user_id+' -p '+neo4j_password+' "match(n) return count (n)"'])
            result=cli.exec_start(exec_id["Id"])
            self.baseLogger.info("INFO::Cypher Test Exec Result::"+result)

            while "Connection refused" in str(result):
                self.baseLogger.info("Waiting another 10 seconds for neo4j to start")
                time.sleep(10)
                exec_id=cli.exec_create(container_id["Id"],cmd=['/bin/bash','-c','cd neo4j-Insights && bin/cypher-shell -a bolt://localhost:7687 -u '+neo4j_user_id+' -p '+neo4j_password+' "match(n) return count (n)"'])
                result=cli.exec_start(exec_id["Id"])

            self.baseLogger.info("INFO::DockerContainer Creation Success..."+mq_response["sourceUrl"])
            _es = elasticsearch.Elasticsearch(hosts=self.config.get('elasticsearch_hostname_uri',''),http_auth=(self.getCredential('elasticsearch_username'), self.getCredential('elasticsearch_passwd')))

            nodeBody = {
                "size": 1,
                "query": {
                        "match_all": {}
                    }

            }
            neo4jIndex = "neo4j_index"

            ESResponse = elasticsearch.helpers.scan(_es, index=neo4jIndex, doc_type="_doc",
                                query={"query": nodeBody["query"]},
                                scroll='10s', raise_on_error=True, preserve_order=False,
                                size=1,
                                request_timeout=None,ignore=[404])


            #indexCreationCommand='cd neo4j-Insights && '
            indexCreationCommand='echo "'

            for doc in ESResponse:
                indexDict = doc['_source']
                for index in indexDict:
                  if indexDict[index] == 'ONLINE':
                    #indexCreationCommand += 'bin/cypher-shell -a bolt://localhost:7687 -u '+neo4j_user_id+' -p '+neo4j_password+' "'+'CREATE '+ index+'";'
                    indexCreationCommand += 'CREATE '+ index+';'
                break

            indexCreationCommand+='" | neo4j-Insights/bin/cypher-shell -a bolt://localhost:7687 -u '+neo4j_user_id+' -p '+neo4j_password
            exec_id=cli.exec_create(container_id["Id"],cmd=['/bin/bash','-c',indexCreationCommand])
            result=cli.exec_start(exec_id["Id"])
            
            self.baseLogger.info("Waiting for index creation")
            exec_id=cli.exec_create(container_id["Id"],cmd=['/bin/bash','-c','cd neo4j-Insights && bin/cypher-shell -a bolt://localhost:7687 -u '+neo4j_user_id+' -p '+neo4j_password+' "CALL db.awaitIndexes(36000)"'])
            result=cli.exec_start(exec_id["Id"])

            exec_id=cli.exec_create(container_id["Id"],cmd=['/bin/bash','-c','cd neo4j-Insights && bin/cypher-shell -a bolt://localhost:7687 -u '+neo4j_user_id+' -p '+neo4j_password+' "match(n) return count (n)"'])
            result=cli.exec_start(exec_id["Id"])
            self.baseLogger.info("INFO::Cypher Test Exec Result::"+result)

            if "Connection refused" in str(result):
                raise Exception('Docker ERROR::Cyhper Test Exec. Connection refused')

            nodecount=result.split("\n")[1]

            if(int(nodecount)!=ns["totalNode"]):
                mq_response["message"]="Mismatched node count : Expected Node Count = "+str(ns["totalNode"])+ "Imported Node Count = "+str(nodecount)

                parsedData = self.parseResponse(responseTemplate, mq_response, {})

                self.baseLogger.error("ERROR::Cont Mismatch. Neo4j  Import Error(s)")
                self.publishToolsData(parsedData)
            else:
                mq_response["message"]="Node Count = "+str(nodecount)
                self.baseLogger.info("Docker Container Succesfully started")
                data=list()
                data += self.parseResponse(responseTemplate, mq_response)
                metadata = {
                              "dataUpdateSupported" : False
                           }
                self.publishToolsData(data,metadata)

            ##comment these
        except Exception as ex:
            self.baseLogger.error(ex)

            #ex= archivalName+" \n Docker Exception--->"+ str(ex)
            additionalProperties = {'archivalName':archivalName}
            self.publishHealthDataForExceptions(ex, additionalProperties=additionalProperties)


if __name__ == "__main__":
   ElasticTransferAgent()

