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

import elasticsearch
import elasticsearch.helpers

from ....core.BaseAgent import BaseAgent
#from com.cognizant.devops.platformagents.core.BaseAgent import BaseAgent

from dateutil import parser

#################################################################
def getCSVHeaderFromSchemaIndex(esHelperObj,indexName):
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
            #print("\n***************************")
            #print("NEO4j_Schema_Index...",str(my_dict))
    
    #1. Modify Data type headers -eg 'insightsTime' ==> 'insightsTime:DOUBLE'
    for k,v in my_dict.items():

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
        #print("Neo4J_Schema_Read ---> Key:",k,"....New Value:",csvHeaderList[k])

    print("Neo4J_Schema_Read Done...",str(indexName))
    return csvHeaderList


#################################################################
def getCSVHeaderFromESIndex(esHelperObj,indexName1,indexType1):

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
        #print("Property...:"+k+"...Type...:"+ temp[k]['type'])

        # If NEO4J type substitue exists, rename col header for eg copyToEs becomes copyToEs:BOOLEAN
        if type in datatypeMapping and k not in excludeList:
           
            #get corresponding datatype for Neo4j
            colAppendType = datatypeMapping[type]

            # Edge Case - If new header not created  already, create
            if k not in headerCreatedList:
                temp[k+colAppendType] = temp.pop( k )
                headerCreatedList[k+colAppendType] = 1
                csvHeaderList[k] = k+colAppendType
                print('Old Type....'+k+'...new Type......'+csvHeaderList[k])

    print("Headers from ES Index::",csvHeaderList)

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

def getAllIndices (elasticsearch_hostname_uri,csv_download_path):

    allIndices={}
    
    neo4jImportCmd = "/opt/NEO4J_HOME/neo4j-Insights/bin/neo4j-import  --delimiter=\",\" --array-delimiter=\"^\"  --multiline-fields=true --ignore-empty-strings=true  --into ./graph.db/"

    esHelperObj = elasticsearch.Elasticsearch(hosts=elasticsearch_hostname_uri)

    indices = esHelperObj.indices.get_alias("*")

    for k,v in indices.items():

        #Get only the node and relation indices, ignore scheme indices
        if "_neo4j_schema" not in k:
            index_map = esHelperObj.indices.get_mapping(k)
            temp=index_map[k]['mappings']['_doc']['properties']

            if temp.get("uuid")!=None and temp.get("_label")!=None and temp.get("inSightsTime")!=None:
                allIndices[k] = "node"
                neo4jImportCmd = neo4jImportCmd +" "+"--nodes="+k+".csv"
            elif temp.get("_start")!=None and temp.get("_end")!=None and temp.get("relationshipName")!=None:
                allIndices[k] = "relationship"
                neo4jImportCmd = neo4jImportCmd +" "+"--relationships="+k+".csv"


    esHelperObj.transport.close()

    f = open(csv_download_path +  'neo4jImport.sh', 'w')
    f.write( neo4jImportCmd)
    f.close()

    return allIndices
#############################################################
def es_write_csv(MainClass,ESIndex, ESIndexType, elasticsearch_hostname_uri,fetch_all_data, start_time, finish_time, csv_download_path):
    try:

        _es = elasticsearch.Elasticsearch(hosts=elasticsearch_hostname_uri)
        if _es.ping():
            print('ES Server Connected..',elasticsearch_hostname_uri)
        else:
            print('ERROR - ES CONNECT FAIL....',elasticsearch_hostname_uri)
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
        
        csvHeader = getCSVHeaderFromSchemaIndex(_es,ESIndex)

        #get the CSV Header from Index
        #csvHeader = getCSVHeaderFromESIndex(_es,ESIndex,ESIndexType)

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

        
        with open(csv_download_path + ESIndex + '.csv', 'w') as f:
            header_present = False
            for doc in ESResponse:
                                
                my_dict = doc['_source']
                #print("\n***************************")
                #print("Writing Index...",str(my_dict))
                
                # 1.Modify Dict Key Names to Neo4j Standard , append Data types
                # eg. 'uuid' --> ':ID', '_labels' --> ':LABEL'
                # Append Datatype , eg:  'insightsTime' --> 'insightsTime:DATE'
                my_dict = modifyDictKeys(my_dict, ESIndexType, csvHeader)

                # 2. First time Only - Write CSV Header
                if not header_present:                
                    #w = csv.DictWriter(f, csvHeader.values())  #,  extrasaction='ignore')
                    #w = csv.DictWriter(f,fieldnames=csvHeader.values(),delimiter=",",escapechar="\\",quoting=csv.QUOTE_NONE)
                    
                    #use QUOTE_ALL for to  cover quotes within fields, change escape char
                    w = csv.DictWriter(f,fieldnames=csvHeader.values(),escapechar="\\",quoting=csv.QUOTE_ALL)                    
                    w.writeheader()
                    header_present = True

                # 3. Write Row 
                w.writerow(my_dict)
                i=i+1  

    
    except Exception as ex:
        logging.error(ex)

        #ex_type, ex_value, ex_traceback = sys.exc_info()

        
        #Write Error
        f = open(csv_download_path + ESIndex + '_ERROR.csv', 'w')
        f.write(str(ex.__class__)+"\n")        
        f.write( str(ex))
        f.close()

                
        MainClass.logIndicator(MainClass.SETUP_ERROR, MainClass.config.get('isDebugAllowed', False))

    print("Write CSV  Done....index::",ESIndex,"....Number of Row Written:",i)
class ElasticTransferAgent(BaseAgent):
    def process(self):
        start = time.time()

        elasticsearch_hostname_uri = self.config.get('elasticsearch_hostname_uri', '')
        time_format = self.config.get('time_format', '')
        start_time = self.get_epoch(self.config.get('start_time', ''), time_format)
        finish_time = self.get_epoch(self.config.get('finish_time', ''), time_format)
        es_indexes = self.config.get('es_indexes', {})
        no_of_processes = self.config.get('no_of_processes', 8)
        csv_download_path = self.config.get('csv_download_path', '')
        fetch_all_data = self.config.get('fetch_all_data', 1)

        #if "*":"*" get all the indices
        if es_indexes.get("*")!=None:
            es_indexes = getAllIndices(elasticsearch_hostname_uri,csv_download_path)

        
        try:
            pool = multiprocessing.Pool(no_of_processes)
            result_async = [pool.apply_async(es_write_csv,(self,k, v, elasticsearch_hostname_uri,fetch_all_data, start_time, finish_time, csv_download_path))
                            for k, v in es_indexes.items()]
            results = [r.get() for r in result_async]
            
           
            pool.close()
            pool.join()
            
        except Exception as ex:
            logging.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))
        print('Exit  Main Process...Time Elapased:',time.time()-start)


    def es_write_csv(self):
        print("in process")
    def get_epoch(self, time_string, time_format):
        try:
            startFrom = parser.parse(time_string, ignoretz=True)
            epoch = str(int(time.mktime(time.strptime(str(startFrom), time_format))))
            return epoch
        except Exception as ex:
            logging.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))


if __name__ == "__main__":
    ElasticTransferAgent()
