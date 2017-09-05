/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/

/// <reference path="../../../_all.ts" />


module ISightApp {

    export interface IPipelineService {
        loadPipelineData(): ng.IPromise<any>;
    }

    export class PipelineService implements IPipelineService {
        static $inject = ['$q', '$resource', 'graphService'];

        constructor(private $q: ng.IQService,
            private $resource,
            private graphService: IGraphService) { }

        loadPipelineData(): ng.IPromise<any> {
            var self = this;
            var orphanGitQuery = { "statements": [{ "statement": "match (GIT:GIT) OPTIONAL MATCH (GIT)-[:JENKINS_TRG_BY_GIT]->(JENKINS) with GIT, JENKINS where JENKINS is null AND exists(GIT.git_RepoName) return GIT", "includeStats": true, "resultDataContents": ["row", "graph"] }] };
            var gitToJenkinsQuery = { "statements": [{ "statement": "MATCH (GIT)-[r:JENKINS_TRG_BY_GIT]->(JENKINS) return GIT,JENKINS", "includeStats": true, "resultDataContents": ["row", "graph"] }] };
            var gitToSonarQuery = { "statements": [{ "statement": "MATCH (GIT)-[r1:JENKINS_TRG_BY_GIT]->(JENKINS), (JENKINS)-[r2:SONAR_TRG_BY_JENKINS]->(SONAR) RETURN GIT, JENKINS, SONAR", "includeStats": true, "resultDataContents": ["row", "graph"] }] };
            var gitToRundeckMissingSonarQuery = { "statements": [{ "statement": "MATCH (JENKINS)-[r3:RUNDECK_TRG_BY_JENKINS]->(RUNDECK), (GIT)-[r1:JENKINS_TRG_BY_GIT]->(JENKINS) OPTIONAL MATCH (JENKINS)-[:SONAR_TRG_BY_JENKINS]->(SONAR) with GIT, JENKINS, SONAR, RUNDECK where SONAR is null RETURN  DISTINCT GIT, JENKINS, SONAR, RUNDECK", "includeStats": true, "resultDataContents": ["row", "graph"] }] };
            var gitToRundeck = { "statements": [{ "statement": "MATCH (GIT)-[r1:JENKINS_TRG_BY_GIT]->(JENKINS), (JENKINS)-[r2:SONAR_TRG_BY_JENKINS]->(SONAR), (JENKINS)-[r3:RUNDECK_TRG_BY_JENKINS]->(RUNDECK) RETURN GIT, JENKINS, SONAR, RUNDECK", "includeStats": true, "resultDataContents": ["row", "graph"] }] };

            return this.$q.all([
                this.graphService.executeQuery(orphanGitQuery).then(function(data) {
                    data = self.buildPipelineData('Orphan Commits', data);
                    return data;
                }),
                this.graphService.executeQuery(gitToJenkinsQuery).then(function(data) {
                    data = self.buildPipelineData('Continuous Build', data);
                    return data;
                }),
                this.graphService.executeQuery(gitToSonarQuery).then(function(data) {
                    data = self.buildPipelineData('Cont. Build with Code Quality', data);
                    return data;
                }),
                this.graphService.executeQuery(gitToRundeckMissingSonarQuery).then(function(data) {
                    data = self.buildPipelineData('Deployments without Code Quality', data);
                    return data;
                }),
                this.graphService.executeQuery(gitToRundeck).then(function(data) {
                    data = self.buildPipelineData('Continuous Deployment', data);
                    return data;
                })
            ]);
        }

        private userPerspectiveMetaInfo = {
            'Total Commits': { 'type': 'uniqueCount', 'property': 'git_ScmRevisionNumber' },
            'Contributing to Projects': { 'type': 'uniqueCount', 'property': 'git_RepoName' },
            'Total Builds': { 'type': 'uniqueCount', 'property': 'jen_BuildNumber' },
            'Passed Builds': { 'type': 'uniqueCount', 'property': 'jen_BuildNumber', 'criteria_property': 'jen_Result', 'criteria': 'SUCCESS' },
            'Failed Builds': { 'type': 'uniqueCount', 'property': 'jen_BuildNumber', 'criteria_property': 'jen_Result', 'criteria': 'FAILURE' },
            'Total Deployments': { 'type': 'uniqueCount', 'property': 'run_ExecutionId' },
            'Successful Deployments': { 'type': 'uniqueCount', 'property': 'run_ExecutionId', 'criteria_property': 'run_Status', 'criteria': 'succeeded' },
            'Failed Deployments': { 'type': 'uniqueCount', 'property': 'run_ExecutionId', 'criteria_property': 'run_Status', 'criteria': 'failed' },
            'Aborted Deployments': { 'type': 'uniqueCount', 'property': 'run_ExecutionId', 'criteria_property': 'run_Status', 'criteria': 'aborted' },
        };

        private applicationPerspectiveMetaInfo = {
            'Developers Contributing': { 'type': 'uniqueCount', 'property': 'git_ScmAuthor' },
            'Total Commits': { 'type': 'uniqueCount', 'property': 'git_ScmRevisionNumber' },
            'Total Builds': { 'type': 'uniqueCount', 'property': 'jen_BuildNumber' },
            'Green Builds': { 'type': 'uniqueCount', 'property': 'jen_BuildNumber', 'criteria_property': 'jen_Result', 'criteria': 'SUCCESS' },
            'Red Builds': { 'type': 'uniqueCount', 'property': 'jen_BuildNumber', 'criteria_property': 'jen_Result', 'criteria': 'FAILURE' },
            'Total Deployments': { 'type': 'uniqueCount', 'property': 'run_ExecutionId' },
            'Sucessfull Deployments': { 'type': 'uniqueCount', 'property': 'run_ExecutionId', 'criteria_property': 'run_Status', 'criteria': 'succeeded' },
            'Failed Deployments': { 'type': 'uniqueCount', 'property': 'run_ExecutionId', 'criteria_property': 'run_Status', 'criteria': 'failed' },
            'Aborted Deployments': { 'type': 'uniqueCount', 'property': 'run_ExecutionId', 'criteria_property': 'run_Status', 'criteria': 'aborted' }
        };

        private labelDataSet = {
            'GIT': {
                'Total Commits': { 'type': 'uniqueCount', 'property': 'git_ScmRevisionNumber' },
                'Unique Authors': { 'type': 'uniqueCount', 'property': 'git_ScmAuthor' },
                'Unique Repositories': { 'type': 'uniqueCount', 'property': 'git_RepoName' }
            },
            'JENKINS': {
                'Total Jobs': { 'type': 'uniqueCount', 'property': 'jen_JobName' }
            },
            'SONAR': {
                'Unique Projects': { 'type': 'uniqueCount', 'property': 'son_Resourcekey' },
                'Unique Timestamps': { 'type': 'uniqueCount', 'property': 'son_Timestamp' }
            },
            'RUNDECK': {
                'Unique Projects': { 'type': 'uniqueCount', 'property': 'run_ProjectName' },
                'Unique Jobnames': { 'type': 'uniqueCount', 'property': 'run_JobName' },
                'Unique Execution Ids': { 'type': 'uniqueCount', 'property': 'run_ExecutionId' }
            }
        };

        private performAggregation(template: Object, pipelineRecords: PipelineRecord[], groupByProperty: string): Object {
            var result = {};
            var workObj = null;
            pipelineRecords.forEach(pipelineRecord => {
                if (groupByProperty) {
                    var groupByData = result[pipelineRecord.rowData[groupByProperty]];
                    if (groupByData == undefined) {
                        groupByData = {};
                        result[pipelineRecord.rowData[groupByProperty]] = groupByData;
                    }
                    workObj = groupByData;
                } else {
                    workObj = result;
                }

                for (var i in template) {
                    var data = workObj[i];
                    if (data === undefined) {
                        data = {};
                        workObj[i] = data;
                    }
                    //'Green Builds': {'type' : 'uniqueCount', 'property' :'JENKINS.jen_BuildNumber', 'criteria_property' : 'JENKINS.jen_Result', 'criteria': 'SUCCESS' },
                    if (pipelineRecord.rowData[template[i].property]) {
                        if (template[i].criteria_property && pipelineRecord.rowData[template[i].criteria_property]) {
                            if (pipelineRecord.rowData[template[i].criteria_property] === template[i].criteria) {
                                data[pipelineRecord.rowData[template[i].property]] = pipelineRecord;
                            }
                        } else {
                            data[pipelineRecord.rowData[template[i].property]] = pipelineRecord;
                        }
                    } else {
                        delete workObj[i];
                    }
                }
            });

            for (var i in result) {
                if (groupByProperty) {
                    var groupByData = result[i];
                    for (var j in groupByData) {
                        groupByData[j] = Object.keys(groupByData[j]).length;
                    }
                } else {
                    result[i] = Object.keys(result[i]).length;
                }
            }

            return result;
        }

        private buildDataArray(metainfo, pipelineRecords: Array<PipelineRecord>, groupBy: string, columnName : string): TabDialogData {
            var data = this.performAggregation(angular.merge({}, metainfo), pipelineRecords, groupBy);
            var headers = [columnName];
            for (var i in metainfo) {
                headers.push(i);
            }
            var dataRows = [];
            for (var i in data) {
                var dataRow = [];
                dataRow.push('' + i);
                var groupedData = data[i];
                for (var j in metainfo) {
                    dataRow.push(groupedData[j] === undefined ? '' : groupedData[j]);
                }
                dataRows.push(dataRow);
            }
            return new TabDialogData(headers, dataRows);
        }

        private buildPipelineData(pipelineName: string, data: any): PipelineData {
            if (data) {
                var results = data.results;
                try {
                    var pipelineRecords: Array<PipelineRecord> = [];
                    var data = results[0].data;
                    data.forEach(rowWrapper => {
                        var row = rowWrapper.row;
                        var compositeObj = {};
                        row.forEach(rowData => {
                            angular.merge(compositeObj, rowData);
                        });
                        pipelineRecords.push(new PipelineRecord(compositeObj));
                    });

                    var tools = [];
                    for (var tool in this.labelDataSet) {
                        tools.push(new ToolData(tool, this.performAggregation(angular.merge({}, this.labelDataSet[tool]), pipelineRecords, null)));
                    }

                    return new PipelineData(
                        pipelineName,
                        tools,
                        this.buildDataArray(this.applicationPerspectiveMetaInfo, pipelineRecords, 'git_RepoName', 'Application'),
                        this.buildDataArray(this.userPerspectiveMetaInfo, pipelineRecords, 'git_ScmAuthor', 'User'),
                        pipelineRecords);
                } catch (error) {
                    console.log(error);
                }
            }
            return;
        }
    }
}
