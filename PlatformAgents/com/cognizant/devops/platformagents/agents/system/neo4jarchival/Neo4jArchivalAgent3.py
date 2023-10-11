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
'''
Created on 16 june 2020


@author: 543825
Description: push historical nodes from neo4j from certain days from currentdate given in config(timepriod) to elasticsearch
neo4j label = elasticsearch indexname
neo4jnodeid+insightstime = record id in es
resumes push and pull operation on exception  in next cycle
'''

from ....core.BaseAgent3 import BaseAgent
from datetime import datetime
from neo4j import GraphDatabase
import json
import elasticsearch
import time
import os


class Neo4jArchivalAgent(BaseAgent):
    @BaseAgent.timed
    def process(self):
        neo4j_host_uri = self.config.get("neo4j_host_uri", '')
        neo4j_query_limit = self.config.get("querylimit", '')
        neo4j_user_id = self.getCredential("neo4j_user_id")
        neo4j_cred = self.getCredential("neo4j_password")
        elasticsearch_hostname_uri = self.config.get("elasticsearch_hostname_uri", '')
        elasticsearch_username = self.getCredential('elasticsearch_username')
        elasticsearch_passwd = self.getCredential('elasticsearch_passwd')
        neo4j_label_csv = self.config.get("neo4j_label", '')
        neo4j_label = neo4j_label_csv.split(",")
        neo4j_data_delete = self.config.get("neo4j_data_delete", "")
        self._driver = GraphDatabase.driver(neo4j_host_uri, auth=(neo4j_user_id, neo4j_cred), max_connection_lifetime=200)
        _es = elasticsearch.Elasticsearch(hosts=elasticsearch_hostname_uri,http_auth=(elasticsearch_username, elasticsearch_passwd))
        if _es.ping():
            self.baseLogger.info('connected')
        else:
            self.baseLogger.info('could not connect!')
        try:
            if neo4j_label_csv == "*":
                with self._driver.session() as session:
                    list_of_labels = session.write_transaction(self.find_labels)
                    self.baseLogger.info(list_of_labels)
                    dctNode = list_of_labels[0]
                    tem = dict(dctNode)
                    values_view = list(tem.values())
                    value_iterator = iter(values_view)
                    first_value = next(value_iterator)
                    self.baseLogger.info(first_value)
                    list_lables = list(first_value)
                    self.baseLogger.info(list_lables)
            else:
                list_lables = neo4j_label
            for i in range(len(list_lables)):
                if list_lables[i]:
                    #self.baseLogger.info((list_lables[i]))
                    self.process_node_datatype(_es, list_lables[i])
                    self.process_rel_datatype(_es, list_lables[i])
                    self.resume_migrate_nodes(_es, list_lables[i], neo4j_query_limit)
                    self.resume_migrate_forward_relationship(_es, list_lables[i],neo4j_query_limit)
                    self.resume_migrate_backward_relationship(_es, list_lables[i],neo4j_query_limit)
                    self.migrate_nodes(_es, list_lables[i],neo4j_query_limit)
                    self.migrate_forward_relationship(_es, list_lables[i],neo4j_query_limit)
                    self.migrate_backward_relationship(_es, list_lables[i],neo4j_query_limit)
                    self.process_node_datatype(_es, list_lables[i])
                    self.process_rel_datatype(_es, list_lables[i])
                    if (neo4j_data_delete):
                        #self.baseLogger.info("deletion starts")
                        self.delete_batchof_relationships(_es, list_lables[i], neo4j_query_limit)
                        self.delete_batchof_nodes(_es, list_lables[i], neo4j_query_limit)
            self.process_indexes(_es)
            self.baseLogger.info("process complete")


        except Exception as ex:
            self.baseLogger.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))
            exit(1)

    def process_node_datatype(self, _es, label):
        try:
            self.baseLogger.info("INFO:: Inside Process Node Datatype")
            with self._driver.session() as session:
                schemadict ={}
                result = session.write_transaction(self.get_node_datatype)
                self.baseLogger.info(result)
                for i in range(len(result)):
                    #self.baseLogger.info((result[i]))
                    #self.baseLogger.info((result[i]["nodeLabels"]))
                    labellist = result[i]["nodeLabels"]
                    if labellist.count(label) >0 :
                        schemadict[result[i]["propertyName"]] = result[i]["propertyTypes"]
                        schemadict["_label"] = ["String"]
                self.baseLogger.info(schemadict)
                label= label+"_neo4j_schema"
                copy_schema_data_to_es_result =self.copy_schema_data_to_es(_es, schemadict, label)

        except Exception as ex:
            self.baseLogger.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))
            exit(1)

    def process_rel_datatype(self, _es, label):
        try:
            self.baseLogger.info("INFO:: Inside Process Rel Datatype")
            with self._driver.session() as session:
                schemadict ={}
                relationship_types = session.write_transaction(self.get_distinct_relationship_details,label)
                self.baseLogger.info(relationship_types)
                result = session.write_transaction(self.get_rel_datatype)
                self.baseLogger.info(result)
                for j in range(len(relationship_types)):
                    for i in range(len(result)):
                        #self.baseLogger.info((result[i]["relType"]))
                        relatioshipname = relationship_types[j]["relationshipName"]
                        relatioshipname_altered = ":`"+relatioshipname+"`"
                        #self.baseLogger.info(relatioshipname)
                        relType_val = result[i]["relType"]
                        if relatioshipname_altered == relType_val:
                            schemadict[result[i]["propertyName"]] = result[i]["propertyTypes"]
                            schemadict["relationshipName"] = ["String"]
                            schemadict["relationshipID"] = ["Long"]
                            schemadict["startNodeID"] = ["Long"]
                            schemadict["endNodeID"] = ["Long"]
                            schemadict["time_node_start"] = ["Double"]
                            schemadict["_start"] = ["String"]
                            schemadict["time_node_end"] = ["Double"]
                            schemadict["_end"] = ["String"]
                            schemadict["copy_to_ES"] = ["Boolean"]
                    #self.baseLogger.info(schemadict)
                    relatioshipname= relatioshipname+"_neo4j_schema"
                    copy_schema_data_to_es_result =self.copy_schema_data_to_es(_es, schemadict, relatioshipname)

        except Exception as ex:
            self.baseLogger.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))
            exit(1)

    def process_indexes(self, _es):
        #below index name is a reserved name in neo4j, it will be created by neo4j itself
        reservedIndex = "__org_neo4j_schema_index_label_scan_store_converted_to_token_index"
        try:
            self.baseLogger.info("INFO:: Inside Process Indexes")
            with self._driver.session() as session:
                result = session.write_transaction(self.get_indexes)
                if len(result)>0:
                    #creating seperate entries for each index in ES
                    for i in range(len(result)):
                        if result[i]["name"] == reservedIndex :
                            continue
                        indexDict = dict()  
                        indexDict["createStatement"] = result[i]["createStatement"]
                        indexDict["state"] = result[i]["state"]
                        indexDict["name"] = result[i]["name"]
                        label= "neo4j_index"
                        copy_index_data_to_es_result =self.copy_index_data_to_es(_es, indexDict, label)

        except Exception as ex:
            self.baseLogger.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))
            exit(1)

    def delete_batchof_nodes(self, _es, label,neo4j_query_limit):
        try:
            self.baseLogger.info("INFO:: Inside delete batch of nodes")
            with self._driver.session() as session:
                timeperiod_epoch = self.obtain_epoch_date()
                list_of_NodeID = []
                querycount = session.write_transaction(self.total_node_deletion_count, timeperiod_epoch, label)
                countofNodes = querycount[0]["Count"]
                no_of_loop = countofNodes / neo4j_query_limit
                no_of_loop = int(no_of_loop) + 1
                for k in range(no_of_loop):
                    session.write_transaction(self.delete_node, timeperiod_epoch, label,neo4j_query_limit)

        except Exception as ex:
            self.baseLogger.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))
            exit(1)

    def delete_batchof_relationships(self, _es, label,neo4j_query_limit):
        try:
            self.baseLogger.info("INFO:: Inside delete batch of relationships")
            with self._driver.session() as session:
                timeperiod_epoch = self.obtain_epoch_date()
                list_of_NodeID = []
                querycount = session.write_transaction(self.total_relationships_deletion_count, timeperiod_epoch, label)
                countofNodes = querycount[0]["Count"]
                no_of_loop = countofNodes / neo4j_query_limit
                no_of_loop = int(no_of_loop) + 1
                for k in range(no_of_loop):
                    session.write_transaction(self.delete_relationship, label,neo4j_query_limit)

        except Exception as ex:
            self.baseLogger.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))
            exit(1)

    """
    Function: resume_migrate_nodes
    Description: get node details add labels as seperated values seperated by  ^ which are incomplete in previous transaction
    Parameter: _es = holds elasticsearch obejct (object), label = neo4j labels (string),
                neo4j_query_limit = limit passed to query while pulling data (integer)

    """
    def resume_migrate_nodes(self, _es, label,neo4j_query_limit):
        try:
            self.baseLogger.info("INFO:: Inside resume migrate nodes")
            with self._driver.session() as session:
                timeperiod_epoch = self.obtain_epoch_date()
                list_of_NodeID = []
                list_of_elasticsearchId = []
                querycount = session.write_transaction(self.total_unmigrated_node_count_unack, timeperiod_epoch, label)
                countofNodes = querycount[0]["Count"]
                no_of_loop = countofNodes / neo4j_query_limit
                no_of_loop = int(no_of_loop) + 1
                for k in range(no_of_loop):
                    nodeData = session.write_transaction(self.get_node_details_unack, timeperiod_epoch, label,neo4j_query_limit)
                    for i in range(len(nodeData)):
                        #self.baseLogger.info((nodeData[i]['n']))
                        dictNode = dict(nodeData[i]['n'])
                        lstlabel = nodeData[i]["_label"]
                        insightstime = int(nodeData[i]["inSightsTime"])
                        nodeId = nodeData[i]["ID"]
                        elasticsearchID = str(nodeId)+str(insightstime)
                        convlabelstring = ""
                        for j in range(len(lstlabel)):
                            if(len(convlabelstring) > 0) :
                                convlabelstring = convlabelstring+"^"+lstlabel[j]
                            else :
                                convlabelstring = lstlabel[j]
                        #self.baseLogger.info(dictNode)

                        dictNode.update({'_label': convlabelstring})
                        copy_node_data_to_es_result = self.copy_node_data_to_es(_es, elasticsearchID, dictNode, label)
                        if copy_node_data_to_es_result:
                            list_of_NodeID.append(nodeId)
                            list_of_elasticsearchId.append(elasticsearchID)
                    verified_ids = []
                    for i in range(len(list_of_NodeID)):
                        node_details_from_ES = _es.get(index=label.lower(), id=list_of_elasticsearchId[i])
                        #self.baseLogger.info(node_details_from_ES)
                        if node_details_from_ES['found']:
                            verified_ids.append(list_of_NodeID[i])
                    session.write_transaction(self.set_flag_true_node, label, verified_ids)

        except Exception as ex:
            self.baseLogger.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))
            exit(1)

    """
        Function: resume_migrate_forward_relationship
        Description: get relationship detials, add rel name and push to es which are incomplete in previous transaction
        Parameter: _es = holds elasticsearch obejct (object), label = neo4j labels (string),
                    neo4j_query_limit = limit passed to query while pulling data (integer)

        """

    def resume_migrate_forward_relationship(self, _es, label,neo4j_query_limit):
        try:
            self.baseLogger.info("INFO:: Inside resume migrate forward relationship")
            timeperiod_epoch = self.obtain_epoch_date()
            with self._driver.session() as session:
                querycount = session.write_transaction(self.total_unmigrated_forward_realtionship_count_unack, timeperiod_epoch,
                                                       label)
                countof_rel = querycount[0]["Count"]
                no_of_loop = countof_rel / neo4j_query_limit
                no_of_loop = int(no_of_loop) + 1
                for k in range(no_of_loop):
                    verified_ids = []
                    relData = session.write_transaction(self.get_forward_relationship_details_unack, label, timeperiod_epoch,neo4j_query_limit)
                    for i in range(len(relData)):
                        dictNode = dict(relData[i]['r'])
                        dictNode.update({'relationshipName' : relData[i]["relationshipName"]})
                        dictNode.update({'relationshipID' : relData[i]["relationshipID"]})
                        dictNode.update({'startNodeID' : relData[i]["startNodeID"]})
                        dictNode.update({'endNodeID' : relData[i]["endNodeID"]})
                        dictNode.update({'time_node_start' : relData[i]["time_node_start"]})
                        dictNode.update({'_start' : relData[i]["_start"]})
                        dictNode.update({'time_node_end' : relData[i]["time_node_end"]})
                        dictNode.update({'_end': relData[i]["_end"]})
                        #self.baseLogger.info(dictNode)
                        copy_relationship_data_to_es_result = self.copy_relationship_data_to_es(_es, dictNode)
                        if copy_relationship_data_to_es_result:
                            relId = relData[i]["relationshipID"]
                            time_node_start = int(relData[i]["time_node_start"])
                            elasticsearchId = str(relId)+str(time_node_start)
                            realtioshipName_detail = relData[i]["relationshipName"]
                            rel_response = _es.get(index=realtioshipName_detail.lower(), id=elasticsearchId)
                            #self.baseLogger.info(rel_response)
                            if rel_response['found'] == True:
                                verified_ids.append(relId)
                    session.write_transaction(self.set_flag_true_rel, label, verified_ids )
        except Exception as ex:
            self.baseLogger.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))
            exit(1)


    """
            Function: resume_migrate_backward_relationship
            Description: get relationship detials, add rel name and push to es whichare incomplete in previous transaction
            Parameter: _es = holds elasticsearch obejct (object), label = neo4j labels (string),
                        neo4j_query_limit = limit passed to query while pulling data (integer)

    """

    def resume_migrate_backward_relationship(self, _es, label,neo4j_query_limit):
        try:
            self.baseLogger.info("INFO:: Inside resume migrate backward relationship")
            timeperiod_epoch = self.obtain_epoch_date()
            with self._driver.session() as session:
                querycount = session.write_transaction(self.total_unmigrated_backward_realtionship_count_unack, timeperiod_epoch,
                                                       label)
                countof_rel = querycount[0]["Count"]
                no_of_loop = countof_rel / neo4j_query_limit
                no_of_loop = int(no_of_loop) + 1
                for k in range(no_of_loop):
                    relData = session.write_transaction(self.get_backward_relationship_details_unack, label, timeperiod_epoch,neo4j_query_limit)
                    verified_ids  = []
                    for i in range(len(relData)):
                        dictNode = dict(relData[i]['r'])
                        dictNode.update({'relationshipName' : relData[i]["relationshipName"]})
                        dictNode.update({'relationshipID' : relData[i]["relationshipID"]})
                        dictNode.update({'startNodeID' : relData[i]["startNodeID"]})
                        dictNode.update({'endNodeID' : relData[i]["endNodeID"]})
                        dictNode.update({'time_node_start' : relData[i]["time_node_start"]})
                        dictNode.update({'_start' : relData[i]["_start"]})
                        dictNode.update({'time_node_end' : relData[i]["time_node_end"]})
                        dictNode.update({'_end': relData[i]["_end"]})
                        #self.baseLogger.info(dictNode)
                        copy_relationship_data_to_es_result = self.copy_relationship_data_to_es(_es, dictNode)
                        if copy_relationship_data_to_es_result:
                            relId = relData[i]["relationshipID"]
                            time_node_start = int(relData[i]["time_node_start"])
                            elasticsearchId = str(relId) + str(time_node_start)
                            realtioshipName_detail = relData[i]["relationshipName"]
                            rel_response = _es.get(index=realtioshipName_detail.lower(), id=elasticsearchId)
                            #self.baseLogger.info(rel_response)
                            if rel_response['found'] == True:
                                verified_ids.append(relId)
                    session.write_transaction(self.set_flag_true_rel, label, verified_ids )
        except Exception as ex:
            self.baseLogger.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))
            exit(1)


    def close(self):
        self._driver.close()

    """
                Function: migrate_nodes
                Description: get node details add labels as seperated values seperated by  ^
                Parameter: _es = holds elasticsearch obejct (object), label = neo4j labels (string),
                            neo4j_query_limit = limit passed to query while pulling data (integer)

    """
    def migrate_nodes(self, _es, label, neo4j_query_limit):
        try:
            self.baseLogger.info("INFO:: Inside migrate nodes")
            with self._driver.session() as session:
                timeperiod_epoch = self.obtain_epoch_date()
                list_of_NodeID = []
                list_of_elasticsearchId = []
                querycount = session.write_transaction(self.total_unmigrated_node_count, timeperiod_epoch, label)
                countofNodes = querycount[0]["Count"]
                no_of_loop = countofNodes / neo4j_query_limit
                no_of_loop = int(no_of_loop) + 1
                for k in range(no_of_loop):
                    nodeData = session.write_transaction(self.get_node_details, timeperiod_epoch, label,neo4j_query_limit)
                    for i in range(len(nodeData)):
                        #self.baseLogger.info(nodeData[i]['n'])
                        dictNode = dict(nodeData[i]['n'])
                        lstlabel = nodeData[i]["_label"]
                        nodeId = nodeData[i]["ID"]
                        insightstime = int(nodeData[i]["inSightsTime"])
                        elasticsearchID = str(nodeId)+str(insightstime)
                        convlabelstring=""
                        for j in range(len(lstlabel)):
                            if(len(convlabelstring) > 0) :
                                convlabelstring = convlabelstring+"^"+lstlabel[j]
                            else :
                                convlabelstring = lstlabel[j]
                        #self.baseLogger.info(dictNode)
                        dictNode.update({'_label': convlabelstring})
                        copy_node_data_to_es_result = self.copy_node_data_to_es(_es, elasticsearchID, dictNode, label)
                        if copy_node_data_to_es_result:
                            list_of_NodeID.append(nodeId)
                            list_of_elasticsearchId.append(elasticsearchID)
                    verified_ids = []
                    for i in range(len(list_of_NodeID)):
                        node_details_from_ES = _es.get(index=label.lower(), id=list_of_elasticsearchId[i])
                        #self.baseLogger.info(node_details_from_ES)
                        if node_details_from_ES['found']:
                            verified_ids.append(list_of_NodeID[i])
                    session.write_transaction(self.set_flag_true_node,label,verified_ids )

        except Exception as ex:
            self.baseLogger.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))
            exit(1)


    """
         Function: migrate_forward_relationship
         Description: get relationship detials, add rel name and push to es
         Parameter: _es = holds elasticsearch obejct (object), label = neo4j labels (string),
                     neo4j_query_limit = limit passed to query while pulling data (integer)

     """
    def migrate_forward_relationship(self,_es, label,neo4j_query_limit):
        try:
            self.baseLogger.info("INFO:: Inside migrate forward relationship")
            timeperiod_epoch = self.obtain_epoch_date()
            with self._driver.session() as session:
                querycount = session.write_transaction(self.total_forward_unmigrated_realtionship_count, timeperiod_epoch,label)
                countof_rel = querycount[0]["Count"]
                no_of_loop = countof_rel / neo4j_query_limit
                no_of_loop = int(no_of_loop) + 1
                for k in range(no_of_loop):
                    verified_ids = []
                    relData = session.write_transaction(self.get_forward_relationship_details, label, timeperiod_epoch,neo4j_query_limit)
                    for i in range(len(relData)):
                        dictNode = dict(relData[i]['r'])
                        dictNode.update({'relationshipName' : relData[i]["relationshipName"]})
                        dictNode.update({'relationshipID' : relData[i]["relationshipID"]})
                        dictNode.update({'startNodeID' : relData[i]["startNodeID"]})
                        dictNode.update({'endNodeID' : relData[i]["endNodeID"]})
                        dictNode.update({'time_node_start' : relData[i]["time_node_start"]})
                        dictNode.update({'_start' : relData[i]["_start"]})
                        dictNode.update({'time_node_end' : relData[i]["time_node_end"]})
                        dictNode.update({'_end': relData[i]["_end"]})
                        #self.baseLogger.info(dictNode)
                        copy_relationship_data_to_es_result = self.copy_relationship_data_to_es(_es, dictNode)
                        if copy_relationship_data_to_es_result:
                            relId = relData[i]["relationshipID"]
                            time_node_start = int(relData[i]["time_node_start"])
                            elasticsearchId = str(relId) + str(time_node_start)
                            realtioshipName_detail = relData[i]["relationshipName"]
                            rel_response = _es.get(index=realtioshipName_detail.lower(), id=elasticsearchId)
                            #self.baseLogger.info(rel_response)
                            if rel_response['found'] == True:
                                verified_ids.append(relId)
                    session.write_transaction(self.set_flag_true_rel, label, verified_ids )
        except Exception as ex:
            self.baseLogger.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))
            exit(1)


    """
            Function: migrate_backward_relationship
            Description: get relationship detials, add rel name and push to es
            Parameter: _es = holds elasticsearch obejct (object), label = neo4j labels (string),
                       neo4j_query_limit = limit passed to query while pulling data (integer)

    """

    def migrate_backward_relationship(self,_es, label,neo4j_query_limit):
        try:
            self.baseLogger.info("INFO:: Inside migrate backward relationship")
            timeperiod_epoch = self.obtain_epoch_date()
            with self._driver.session() as session:
                querycount = session.write_transaction(self.total_backward_unmigrated_realtionship_count, timeperiod_epoch,label)
                countof_rel = querycount[0]["Count"]
                no_of_loop = countof_rel / neo4j_query_limit
                no_of_loop = int(no_of_loop) + 1
                for k in range(no_of_loop):
                    verified_ids = []
                    relData = session.write_transaction(self.get_backward_relationship_details, label, timeperiod_epoch,neo4j_query_limit)
                    for i in range(len(relData)):
                        dictNode = dict(relData[i]['r'])
                        dictNode.update({'relationshipName' : relData[i]["relationshipName"]})
                        dictNode.update({'relationshipID' : relData[i]["relationshipID"]})
                        dictNode.update({'startNodeID' : relData[i]["startNodeID"]})
                        dictNode.update({'endNodeID' : relData[i]["endNodeID"]})
                        dictNode.update({'time_node_start' : relData[i]["time_node_start"]})
                        dictNode.update({'_start' : relData[i]["_start"]})
                        dictNode.update({'time_node_end' : relData[i]["time_node_end"]})
                        dictNode.update({'_end': relData[i]["_end"]})
                        #self.baseLogger.info(dictNode)
                        copy_relationship_data_to_es_result = self.copy_relationship_data_to_es(_es, dictNode)
                        if copy_relationship_data_to_es_result:
                            relId = relData[i]["relationshipID"]
                            time_node_start = int(relData[i]["time_node_start"])
                            elasticsearchId = str(relId) + str(time_node_start)
                            realtioshipName_detail = relData[i]["relationshipName"]
                            rel_response = _es.get(index=realtioshipName_detail.lower(), id=elasticsearchId)
                            #self.baseLogger.info(rel_response)
                            if rel_response['found'] == True:
                                verified_ids.append(relId)

                    session.write_transaction(self.set_flag_true_rel, label, verified_ids )

        except Exception as ex:
            self.baseLogger.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))
            exit(1)

    """
            Function: copy_schema_data_to_es
            Description: all the schema of nodes and relationship are stored as seperate index.
            Parameter: _es = holds elasticsearch obejct (object), indexName = index name named after neo4j label  (string),
                                nodeId = nodeid of neo4j created as id for es(integer)   , dctNode : dictionary of schema data

        """

    def copy_schema_data_to_es(self, _es, dctNode, indexName):
        try:
            self.baseLogger.info("INFO:: Inside copy schema data to es")
            if len(dctNode) > 0:
                jsonNode = json.dumps(dctNode)
                if (json.loads(jsonNode)):
                    jsonNode = json.loads(jsonNode)
                    #indexName = string.lower(indexName)
                    indexName = indexName.lower()
                    doc_type = "_doc"
                    deleteResponse =  _es.indices.delete(index=indexName, ignore=[400, 404])
                    response = _es.index(index=indexName, doc_type=doc_type, id=indexName, document=jsonNode)
                    self.baseLogger.info("Nodes schema dumped to Elastic search successfully")
                    if response['result'] == "created":
                        self.baseLogger.info("Nodes schema dumped to Elastic search successfully")
                        return True
                    elif response['result'] == "updated":
                        self.baseLogger.info("Nodes schema updated to Elastic search successfully")
                        return True
                    else:
                        return False
        except Exception as ex:
            self.baseLogger.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))
            exit(1)


    """
            Function: copy_index_data_to_es
            Description: all the indexes of noeo4j are stored as seperate index.
            Parameter: _es = holds elasticsearch obejct (object), indexName = index name
                             , dctNode : dictionary of index data

        """

    def copy_index_data_to_es(self, _es, dctNode, indexName):
        try:
            self.baseLogger.info("INFO:: Inside copy index data to es")
            if len(dctNode) > 0:
                jsonNode = json.dumps(dctNode)
                if (json.loads(jsonNode)):
                    jsonNode = json.loads(jsonNode)
                    #indexName = string.lower(indexName)
                    indexName = indexName.lower()
                    doc_type = "_doc"
                    response = _es.index(index=indexName, doc_type=doc_type, id=dctNode["name"], document=jsonNode)
                    self.baseLogger.info("Nodes schema dumped to Elastic search successfully")
                    if response['result'] == "created":
                        self.baseLogger.info("Nodes schema dumped to Elastic search successfully")
                        return True
                    elif response['result'] == "updated":
                        self.baseLogger.info("Nodes schema updated to Elastic search successfully")
                        return True
                    else:
                        return False
        except Exception as ex:
            self.baseLogger.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))
            exit(1)


    """
        Function: copy_node_data_to_es
        Description: esID = nodeID+insightstime , all labels stored in seperate index
        Parameter: _es = holds elasticsearch obejct (object), indexName = index name named after neo4j label  (string),
                            nodeId = nodeid of neo4j created as id for es(integer)   , dctNode : dictionary of node data

    """
    def copy_node_data_to_es(self, _es, nodeId, dctNode, indexName):
        try:
            self.baseLogger.info("INFO:: Inside copy node data to es")
            jsonNode = json.dumps(dctNode)
            if (json.loads(jsonNode)):
                jsonNode = json.loads(jsonNode)
                #indexName = string.lower(indexName)
                indexName = indexName.lower()
                doc_type = "_doc"
                response = _es.index(index=indexName, doc_type=doc_type, id=nodeId, document=jsonNode)
                self.baseLogger.info("Nodes dumped to Elastic search successfully")
        except Exception as ex:
            self.baseLogger.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))
            exit(1)

        if response['result'] == "created":
            self.baseLogger.info("Nodes dumped to Elastic search successfully")
            return True
        elif response['result'] == "updated":
            self.baseLogger.info("Nodes updated to Elastic search successfully")
            return True
        else:
            return False
    """
        Function: copy_relationship_data_to_es
        Description: all the forward and backward indexes are stored as seperate indexes in es elasticsearchID= neo4jrelationshipID+startNodeInsightsTIme
        Parameter: _es = holds elasticsearch obejct (object),
                            result : dictionary of relationship data

    """

    def copy_relationship_data_to_es(self, _es, result):
        try:
            self.baseLogger.info("INFO:: Inside copy relationship data to es")
            jsonNode = json.dumps(result)
            if (json.loads(jsonNode)):
                jsonNode = json.loads(jsonNode)
                relationshipName = result['relationshipName']
                #relationshipName = string.lower(relationshipName)
                relationshipName = relationshipName.lower()
                relationshipID = result['relationshipID']
                time_node_start = int(result['time_node_start'])
                elasticsearchID = str(relationshipID) + str(time_node_start)
                response = _es.index(index=relationshipName, doc_type="_doc", id=elasticsearchID,
                                     document=jsonNode)
                self.baseLogger.info((response['result']))
        except Exception as ex:
            self.baseLogger.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))
            exit(1)

        if response['result'] == "created":
            self.baseLogger.info("Relationship dumped to Elastic search successfully")
            return True
        elif response['result'] == "updated":
            self.baseLogger.info("Relationship updated to Elastic search successfully")
            return True
        else:
            return False

    """
           Function: obtain_epoch_date
           Description: converts the timeperiod in days to epoch date from the current date
           Parameter:

    """
    def obtain_epoch_date(self):
        try:
            days = self.config.get("timeperiod", "")
            archival_enddate =self.config.get("archival_enddate","")
            pattern = self.config.get("timeStampFormat", '')
            if days > 0:
                days_in_epoch = days * 24 * 60 * 60
                now = datetime.now()
                current_time = now.strftime("%d.%m.%Y %H:%M:%S")
                #self.baseLogger.info(("Current Time =", current_time))
                epoch = int(time.mktime(time.strptime(current_time, pattern)))
                date_result = epoch - days_in_epoch
                self.baseLogger.info(date_result)
            else:
                date_result = int(time.mktime(time.strptime(archival_enddate, pattern)))
                self.baseLogger.info(date_result)

            return date_result
        except Exception as ex:
            self.baseLogger.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))
            exit(1)


    def total_unmigrated_node_count(self, tx, timeperiod, label1):
        try:
            result = tx.run("MATCH (n:{label}:DATA) "
                            "WHERE toInteger(n.inSightsTime) < $timeperiod and n.copy_to_ES is null"
                            "RETURN count(n) as Count ".format(label=label1), timeperiod=timeperiod).data()
            self.baseLogger.info("count of Nodes to left************")
            self.baseLogger.info(result)

        except Exception as ex:
            self.baseLogger.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))
        return result

    def total_unmigrated_node_count_unack(self, tx, timeperiod, label1):
        try:
            result = tx.run("MATCH (n:{label}:DATA) "
                            "WHERE toInteger(n.inSightsTime) < $timeperiod and n.copy_to_ES = false "
                            "RETURN count(n) as Count ".format(label=label1), timeperiod=timeperiod).data()
            self.baseLogger.info("count of Nodes to left************")
            self.baseLogger.info(result)

        except Exception as ex:
            self.baseLogger.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))
        return result

    def total_node_deletion_count(self, tx, timeperiod, label1):
        try:
            result = tx.run("MATCH (n:{label}:DATA) "
                            "WHERE toInteger(n.inSightsTime) < $timeperiod and n.copy_to_ES = true "
                            "RETURN count(n) as Count ".format(label=label1), timeperiod=timeperiod).data()
            self.baseLogger.info("count of Nodes to left************")
            self.baseLogger.info(result)

        except Exception as ex:
            self.baseLogger.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))
        return result

    def total_relationships_deletion_count(self, tx, timeperiod, label1):
        try:
            result = tx.run("MATCH (n:{label}:DATA)-[r]-(b) "
                            "WHERE toInteger(n.inSightsTime) < $timeperiod and r.copy_to_ES = true and b.inSightsTime is not null "
                            "RETURN count(r) as Count ".format(label=label1), timeperiod=timeperiod).data()
            self.baseLogger.info("count of Relationships to left************")
            self.baseLogger.info(result)

        except Exception as ex:
            self.baseLogger.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))
        return result

    def total_forward_unmigrated_realtionship_count(self, tx, timeperiod, label1):
        try:
            result = tx.run("MATCH (n:{label}:DATA)-[r]->(b)"
                            "WHERE toInteger(n.inSightsTime) < $timeperiod and r.copy_to_ES is null and b.inSightsTime is not null "
                            "RETURN count(r) as Count ".format(label=label1), timeperiod=timeperiod).data()
            self.baseLogger.info("count of Relationships to left************")
            self.baseLogger.info(result)

        except Exception as ex:
            self.baseLogger.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))
        return result

    def total_backward_unmigrated_realtionship_count(self, tx, timeperiod, label1):
        try:
            result = tx.run("MATCH (n:{label}:DATA)<-[r]-(b)"
                            "WHERE toInteger(n.inSightsTime) < $timeperiod and r.copy_to_ES is null and b.inSightsTime is not null "
                            "RETURN count(r) as Count ".format(label=label1), timeperiod=timeperiod).data()
            self.baseLogger.info("count of Relationships to left************")
            self.baseLogger.info(result)

        except Exception as ex:
            self.baseLogger.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))
        return result



    def total_unmigrated_forward_realtionship_count_unack(self, tx, timeperiod, label1):
        try:
            result = tx.run("MATCH (n:{label}:DATA)-[r]->(b)"
                            "WHERE toInteger(n.inSightsTime) < $timeperiod and r.copy_to_ES = false and b.inSightsTime is not null "
                            "RETURN count(r) as Count ".format(label=label1), timeperiod=timeperiod).data()
            self.baseLogger.info("count of Relationships to left************")
            self.baseLogger.info(result)

        except Exception as ex:
            self.baseLogger.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))
        return result

    def total_unmigrated_backward_realtionship_count_unack(self, tx, timeperiod, label1):
        try:
            result = tx.run("MATCH (n:{label}:DATA)<-[r]-(b)"
                            "WHERE toInteger(n.inSightsTime) < $timeperiod and r.copy_to_ES = false and b.inSightsTime is not null "
                            "RETURN count(r) as Count ".format(label=label1), timeperiod=timeperiod).data()
            self.baseLogger.info("count of Relationships to left************")
            self.baseLogger.info(result)

        except Exception as ex:
            self.baseLogger.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))
        return result

    def run_node_query_fordeletion(self, tx, timeperiod, label1):
        try:
            result = tx.run("MATCH (n:{label}:DATA) "
                            "WHERE toInteger(n.inSightsTime) < $timeperiod and n.copy_to_ES = true "
                            "RETURN id(n) as ID ".format(label=label1), timeperiod=timeperiod).data()

        except Exception as ex:
            self.baseLogger.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))
        return result

    def get_node_details(self, tx, timeperiod, label1,neo4j_query_limit):
        try:
            result = tx.run("MATCH (n:{label}:DATA) "
                            "WHERE toInteger(n.inSightsTime) < $timeperiod and not n.copy_to_ES is not null "
                            "with n limit $neo4j_query_limit "
                            "SET n.copy_to_ES = false "
                            "RETURN n , id(n) as ID ,labels(n) as _label, n.inSightsTime as inSightsTime ".format(label=label1), timeperiod=timeperiod,neo4j_query_limit=neo4j_query_limit).data()

        except Exception as ex:
            self.baseLogger.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))

        return result

    def get_node_details_unack(self, tx, timeperiod, label1,neo4j_query_limit):
        try:
            result = tx.run("MATCH (n:{label}:DATA) "
                            "WHERE toInteger(n.inSightsTime) < $timeperiod and n.copy_to_ES = false "
                            "with n limit $neo4j_query_limit "
                            "RETURN n , id(n) as ID ,labels(n) as _label, n.inSightsTime as inSightsTime ".format(label=label1),
                            timeperiod=timeperiod,neo4j_query_limit=neo4j_query_limit).data()

        except Exception as ex:
            self.baseLogger.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))

        return result

    def get_node_details_for_deletion(self, tx, timeperiod, label1,neo4j_query_limit):
        try:
            result = tx.run("MATCH (n:{label}:DATA) "
                            "WHERE toInteger(n.inSightsTime) < $timeperiod and n.copy_to_ES = true "
                            "with n limit $neo4j_query_limit "
                            "RETURN id(n) as ID ".format(label=label1),
                            timeperiod=timeperiod,neo4j_query_limit=neo4j_query_limit).data()

        except Exception as ex:
            self.baseLogger.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))

        return result

    def find_relationship_deletion(self, tx, label1, nodeId):
        try:
            result = tx.run("MATCH (a:{label}:DATA)-[r]-(b) "
                            "WHERE id(a)= $nodeId and r.copy_to_ES = true "
                            "RETURN id(r) as relationshipID ".format(
                label=label1), nodeId=nodeId).data()
        except Exception as ex:
            self.baseLogger.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))

        return result

    def get_forward_relationship_details(self, tx, label1, timeperiod,neo4j_query_limit):
        records = []
        try:
            results = tx.run("MATCH (a:{label}:DATA)-[r]->(b) "
                            "WHERE r.copy_to_ES is null and  toInteger(a.inSightsTime) < $timeperiod and b.inSightsTime is not null  "
                            "SET r.copy_to_ES = false "
                            "RETURN distinct type(r) as relationshipName, id(r) as relationshipID, id(a) as startNodeID, id(b) as endNodeID ,a.inSightsTime as time_node_start , a.uuid as _start , b.inSightsTime as time_node_end, b.uuid as _end ,r "
                            "limit $neo4j_query_limit ".format(label=label1), timeperiod=timeperiod,neo4j_query_limit=neo4j_query_limit)
        except Exception as ex:
            self.baseLogger.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))
        for result in results:
            record = dict()
            # iterating through each key on the result and assigning the value due to 
            # tx.run().data() doesn't provide the proper value, when the type of the value is a json/object
            for key in result.keys():
                record[key] = result[key]
            records.append(record)

        return records

    def get_backward_relationship_details(self, tx, label1, timeperiod,neo4j_query_limit):
        records = []
        try:
            results = tx.run("MATCH (a:{label}:DATA)<-[r]-(b) "
                            "WHERE r.copy_to_ES is null and  toInteger(a.inSightsTime) < $timeperiod and b.inSightsTime is not null "
                            "SET r.copy_to_ES = false "
                            "RETURN distinct type(r) as relationshipName, id(r) as relationshipID, id(a) as endNodeID, id(b) as startNodeID ,a.inSightsTime as time_node_end , a.uuid as _end , b.inSightsTime as time_node_start, b.uuid as _start , r   "
                            "limit $neo4j_query_limit ".format(label=label1), timeperiod=timeperiod,neo4j_query_limit=neo4j_query_limit)
        except Exception as ex:
            self.baseLogger.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))

        for result in results:
            record = dict()
            # iterating through each key on the result and assigning the value due to 
            # tx.run().data() doesn't provide the proper value, when the type of the value is a json/object
            for key in result.keys():
                record[key] = result[key]
            records.append(record)

        return records

    def get_forward_relationship_details_unack(self, tx, label1, timeperiod,neo4j_query_limit):
        records = []
        try:
            results = tx.run("MATCH (a:{label}:DATA)-[r]->(b) "
                            "WHERE r.copy_to_ES = false and  toInteger(a.inSightsTime) < $timeperiod and b.inSightsTime is not null  "
                            "RETURN distinct type(r) as relationshipName, id(r) as relationshipID, id(a) as startNodeID, id(b) as endNodeID ,a.inSightsTime as time_node_start , a.uuid as _start , b.inSightsTime as time_node_end, b.uuid as _end, r "
                            "limit $neo4j_query_limit ".format(label=label1), timeperiod=timeperiod,neo4j_query_limit=neo4j_query_limit)
        except Exception as ex:
            self.baseLogger.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))

        for result in results:
            record = dict()
            # iterating through each key on the result and assigning the value due to 
            # tx.run().data() doesn't provide the proper value, when the type of the value is a json/object
            for key in result.keys():
                record[key] = result[key]
            records.append(record)

        return records
    def get_backward_relationship_details_unack(self, tx, label1, timeperiod,neo4j_query_limit):
        records = []
        try:
            results = tx.run("MATCH (a:{label}:DATA)<-[r]-(b) "
                            "WHERE r.copy_to_ES = false and  toInteger(a.inSightsTime) < $timeperiod and b.inSightsTime is not null "
                            "RETURN distinct type(r) as relationshipName, id(r) as relationshipID, id(a) as startNodeID, id(b) as endNodeID ,a.inSightsTime as time_node_start , a.uuid as _start , b.inSightsTime as time_node_end, b.uuid as _end , r  "
                            "limit $neo4j_query_limit ".format(label=label1), timeperiod=timeperiod,neo4j_query_limit=neo4j_query_limit)
        except Exception as ex:
            self.baseLogger.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))

        for result in results:
            record = dict()
            # iterating through each key on the result and assigning the value due to 
            # tx.run().data() doesn't provide the proper value, when the type of the value is a json/object
            for key in result.keys():
                record[key] = result[key]
            records.append(record)

        return records


    def set_flag_true_rel(self, tx, label1, relId):
        self.baseLogger.info(relId)
        try:
            result = tx.run("MATCH (a:{label}:DATA )-[r]-(b) "
                            "WHERE id(r) in $relId and r.copy_to_ES = false "
                            "SET r.copy_to_ES = true "
                            "RETURN id(r) as count ".format(label=label1), relId=relId).data()
        except Exception as ex:
            self.baseLogger.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))

        return result

    def set_flag_true_node(self, tx, label1, nodeId):
        try:
            result = tx.run("MATCH (n:{label} :DATA)"
                            "WHERE id(n) in $nodeId  and n.copy_to_ES = false "
                            "SET n.copy_to_ES = true "
                            "RETURN  count(n) as count ".format(label=label1), nodeId=nodeId).data()
        except Exception as ex:
            self.baseLogger.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))

        return result

    def delete_relationship(self, tx, label1,neo4j_query_limit):
        try:
            result = tx.run("MATCH (a:{label}:DATA )-[r]-(b) "
                            "WHERE r.copy_to_ES = true  "
                            "with r limit  $neo4j_query_limit "
                            "Delete r ".format(label=label1),neo4j_query_limit=neo4j_query_limit).data()
            self.baseLogger.info("Relationships deleted")
        except Exception as ex:
            self.baseLogger.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))

        return result

    def delete_node(self, tx,timeperiod, label1, neo4j_query_limit ):
        try:
            result = tx.run("MATCH (n:{label}:DATA )"
                            "WHERE n.copy_to_ES = true "
                            "with n limit $neo4j_query_limit "
                            "Detach Delete n ".format(label=label1), timeperiod=timeperiod,neo4j_query_limit=neo4j_query_limit).data()
            self.baseLogger.info("Nodes deleted")

        except Exception as ex:
            self.baseLogger.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))

        return result

    def run_match_query_count_acknowledged(self, tx, timeperiod, label1):
        try:
            result = tx.run("MATCH (n:{label}:DATA) "
                            "WHERE toInteger(n.inSightsTime) < $timeperiod and n.copy_to_ES = true "
                            "RETURN count(n) as Count ".format(label=label1), timeperiod=timeperiod).data()
            self.baseLogger.info("count of Nodes to left ack")
            self.baseLogger.info(result)

        except Exception as ex:
            self.baseLogger.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))
        return result

    def find_labels(self, tx):
        try:
            result = tx.run("match(n:DATA) where n.uuid is not null "
                            "with distinct labels(n) as labels "
                            "unwind labels as label "
                            "with distinct label "
                            "return [_Iter IN COLLECT(label) WHERE NOT _Iter IN ['RAW','ALM', 'DATA','DUMMY','DUMMYDATA','LATEST','SCM','CI','APPMONITORING','CODEQUALITY','DEPLOYMENT','ENVIRONMENT']] ").data()
        except Exception as ex:
            self.baseLogger.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))
        return result

    def get_node_datatype(self,tx):
        try:
            result = tx.run("call db.schema.nodeTypeProperties ").data()
            self.baseLogger.info(result)
        except Exception as ex:
            self.baseLogger.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))
        return result

    def get_rel_datatype(self, tx):
        try:
            result = tx.run("call db.schema.relTypeProperties ").data()
            self.baseLogger.info(result)
        except Exception as ex:
            self.baseLogger.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))
        return result

    def get_distinct_relationship_details(self, tx,label):
        try:
            result = tx.run("MATCH (a:{label}:DATA)-[r]-(b) "
                            "RETURN distinct type(r) as relationshipName ".format(label=label)).data()
        except Exception as ex:
            self.baseLogger.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))

        return result

    def get_indexes(self,tx):
        try:
            result = tx.run("SHOW INDEXES YIELD *").data()
        except Exception as ex:
            self.baseLogger.error(ex)
            self.logIndicator(self.SETUP_ERROR, self.config.get('isDebugAllowed', False))
        return result


if __name__ == "__main__":
    Neo4jArchivalAgent()

