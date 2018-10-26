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
Created on Jun 16, 2016
@author: 476693
'''
from dateutil import parser
import datetime
from com.cognizant.devops.platformagents.core.BaseAgent import BaseAgent
import logging
import json

class GitLabAgent(BaseAgent):
    def process(self):
        baseUrl = self.config.get("getProjects", '')
        baseUrl = baseUrl + '/api/v4'
        accessToken = self.config.get("accessToken", '')
        commitsBaseEndPoint = self.config.get("CommitsBaseEndPoint", '')
        relationMetadata = self.config.get('dynamicTemplate', {}).get('relationMetadata', None)
        commitMetadata = self.config.get('dynamicTemplate', {}).get('commitMetadata', None)
        tagMetadata = self.config.get('dynamicTemplate', {}).get('tagMetadata', None)
        mergeMetadata = self.config.get('dynamicTemplate', {}).get('mergeMetadata', None)
        branchDeleteMetadata = self.config.get('dynamicTemplate', {}).get('branchDeleteMetadata', None)
        startFrom = self.config.get("startFrom", '')
        startFrom = parser.parse(startFrom)
        enableBranches = self.config.get("enableBranches", False)
        enableCommitRelation = self.config.get("enableCommitRelation", False)
        projects = self.getResponse(getProjectsUrl, 'GET', None, None, None)
        restricted_projects = self.config.get('dynamicTemplate', {}).get("restrictedProjects", None)
        getGroupsUrl = baseUrl+"/groups?private_token="+accessToken
        groups = self.getResponse(getGroupsUrl, 'GET', None, None, None)
        data_groups = []
        if len(groups) > 0 and self.config.get("enableGroup"):
            for group in groups:
                groupName = group['name']
                groupId = group['id']
                trackingDetails = self.tracking.get(groupName,None)
                if trackingDetails is None or trackingDetails is False:
                    trackingDetails = {}
                    self.tracking[groupName] = trackingDetails
                    getSubGroupUrl = baseUrl+"/groups/"+str(group['id'])+"/subgroups?private_token="+accessToken
                    subgroups = self.getResponse(getSubGroupUrl, 'GET', None, None, None)
                    for subgroup in subgroups:
                        subgroup_name = subgroup.get("name")
                        injectData = {}
                        if trackingDetails.get(subgroup_name) is None or trackingDetails.get(subgroup_name) is False:
                            trackingDetails[subgroup_name] = True
                            injectData['subGroupId'] = subgroup['id']
                            injectData['subGroupName'] = subgroup_name
                            injectData['description'] = subgroup['description']
                            injectData['webUrl'] = subgroup['web_url']
                            injectData['groupName'] = groupName
                            injectData['groupId'] = groupId
                            data_groups.append(injectData)
                self.updateTrackingJson(self.tracking)
        if len(data_groups) > 0:
            self.publishToolsData(data_groups)
        data = []
        start_index_page= 1
        data_in_a_page = 1
        increase_count = 1#This flag will pull number pf pages from api response.
        getProjectsUrl = baseUrl+"/projects?private_token="+accessToken+"&page="+str(start_index_page)+"&per_page="+str(data_in_a_page)
        projects = self.getResponse(getProjectsUrl, 'GET', None, None, None)
        project_count_exist = True
        while project_count_exist:
            data = []
            for project in projects:
                if project['name'] in restricted_projects:
                    continue
                projectName = project.get('name', None)
                trackingDetails = self.tracking.get(projectName,None)
                if trackingDetails is None:
                    trackingDetails = {}
                    self.tracking[projectName] = trackingDetails
                repoUpdatedAt = parser.parse(project['last_activity_at'], ignoretz=True)
                projectModificationTime = trackingDetails.get('projectModificationTime', None)
                if projectModificationTime is None:
                    trackingDetails['projectModificationTime'] = project['last_activity_at']
                    projectModificationTime = startFrom
                else:
                    projectModificationTime = parser.parse(projectModificationTime, ignoretz=True)
                if startFrom < repoUpdatedAt:
                    getBranchesUrl = baseUrl+"/projects/"+str(project['id'])+"/repository/branches?private_token="+accessToken
                    branches = self.getResponse(getBranchesUrl, 'GET', None, None, None)
                    active_branches = []
                    for branch in branches:
                        data_commit = []
                        data_commit_relation = []
                        branchName = branch['name']
                        active_branches.append(branchName)
                        since = trackingDetails.get(branchName, {}).get('latestCommitDate', None)
                        if since is None:
                            since = startFrom
                        else:
                            since = parser.parse(since, ignoretz=True)
                        branchUpdatedAt = parser.parse(branch['commit']['created_at'], ignoretz=True)
                        getCommitsUrl = baseUrl+"/projects/"+str(project['id'])+"/repository/commits?&private_token="+accessToken+"&since="+str(since)+"&ref_name="+branchName+"&page=1&per_page=50"
                        commits = self.getResponse(getCommitsUrl, 'GET', None, None, None)
                        commitPageNum = 1
                        fetchNextPage = True
                        while fetchNextPage:
                            if len(commits) == 0:
                                fetchNextPage = False
                                break;
                            self.updateTrackingForBranch(trackingDetails, branchName, branch, False)
                            for commit in commits:
                                if since < parser.parse(commit['created_at'], ignoretz=True):
                                    injectData = {}
                                    injectData['commitId'] = commit['id']
                                    injectData['title'] = commit['title']
                                    injectData['commitTime'] = commit['created_at']
                                    injectData['message'] = commit['message']
                                    injectData['committerName'] = commit['committer_name']
                                    #injectData['committerEmail'] = commit['committer_email']
                                    injectData['authorEmail'] = commit['author_email']
                                    injectData['projectName'] = project['name']
                                    injectData['projectId'] = project['id']
                                    injectData['branchName'] = branchName
                                    injectData['projectName'] = projectName
                                    data_commit_relation += self.getCommitInformation(commit, projectName)
                                    data_commit.append(injectData)
                            commitPageNum = commitPageNum + 1
                            getcommitsUrl = baseUrl+"/projects/"+str(project['id'])+"/repository/commits?private_token="+accessToken+"&since="+str(since)+"&page="+str(commitPageNum)+"&per_page=50"
                            commits = self.getResponse(getcommitsUrl, 'GET', None, None, None)
                        if len(data_commit_relation) > 0:
                            self.publishToolsData(data_commit_relation, commitMetadata)
                            self.publishToolsData(data_commit, relationMetadata)
                        self.updateTrackingJson(self.tracking)
                    activeBranchesPublish = [{ 'projectName' : projectName, 'activeBranches' : active_branches, 'gitLabType' : 'metadata'}]
                    metadata = {"dataUpdateSupported" : True,"uniqueKey" : ["projectName", "gitLabType"]}
                    self.publishToolsData(activeBranchesPublish, metadata)
                    data_branch_delete = []
                    for keys in self.tracking.get(projectName,None):
                        if "ModificationTime" not in keys:                 
                            branch_delete_status = self.tracking.get(projectName).get(keys, {}).get('delete_status', None)
                            if keys not in active_branches and branch_delete_status != True and branch_delete_status == False:
                                injectDataBranchDelete = {}
                                injectDataBranchDelete['branchName'] = keys
                                injectDataBranchDelete['projectName'] = projectName
                                injectDataBranchDelete['activeStatus'] = False
                                injectDataBranchDelete['latestCommitDate'] = trackingDetails.get(keys, {}).get('latestCommitDate', None)
                                injectDataBranchDelete['latestCommitId'] = trackingDetails.get(keys, {}).get('latestCommitId', None)
                                data_branch_delete.append(injectDataBranchDelete)
                                self.updateTrackingForBranch(trackingDetails, keys, branch, True)
                        self.updateTrackingJson(self.tracking)
                    if len(data_branch_delete) > 0:
                        self.publishToolsData(data_branch_delete, branchDeleteMetadata)
                    projectTagModificationTime = startFrom
                    getTagsUrl = baseUrl+"/projects/"+str(project['id'])+"/repository/tags?private_token="+accessToken+"&since="+str(projectTagModificationTime)
                    tags = self.getResponse(getTagsUrl, 'GET', None, None, None)
                    if len(tags) > 0:
                        data_tag = []
                        projectTagModificationTime = trackingDetails.get('projectTagModificationTime', None)
                        if projectTagModificationTime is None:
                            trackingDetails['projectTagModificationTime'] = tags[0]['commit']['authored_date']
                            projectTagModificationTime = startFrom
                        else:
                            projectTagModificationTime = parser.parse(projectTagModificationTime, ignoretz=True)
                        self.updateTrackingJson(self.tracking)
                        for tag in tags:
                            if projectTagModificationTime < parser.parse(tag['commit']['committed_date'], ignoretz=True):
                                injectData = {}
                                injectData['type'] = 'tag'
                                responseTemplateTag = self.config.get('dynamicTemplate', {}).get('responseTemplateTag', None)
                                data_tag += self.parseResponse(responseTemplateTag, tag, injectData)
                        if len(data_tag) > 0:
                            self.publishToolsData(data_tag, tagMetadata)
                    if self.config.get("enableMerge"):
                        data_merge = []
                        projectMergeModificationTime = startFrom
                        getMergesUrl = baseUrl+"/projects/"+str(project['id'])+"/merge_requests?private_token="+accessToken+"&updated_after="+str(projectMergeModificationTime)
                        merges = self.getResponse(getMergesUrl, 'GET', None, None, None)
                        if len(merges) > 0:
                            projectMergeModificationTime = trackingDetails.get('projectMergeModificationTime', None)
                            if projectMergeModificationTime is None:
                                trackingDetails['projectMergeModificationTime'] = merges[0]['updated_at']
                                projectMergeModificationTime = startFrom
                            else:
                                projectMergeModificationTime = parser.parse(projectMergeModificationTime, ignoretz=True)
                            self.updateTrackingJson(self.tracking)
                            for merge in merges:
                                if projectMergeModificationTime < parser.parse(merge['commit']['committed_date'], ignoretz=True):
                                    injectData = {}
                                    injectData['type'] = 'merge'
                                    responseTemplateMerge = self.config.get('dynamicTemplate', {}).get('responseTemplateMerge', None)
                                    data_merge += self.parseResponse(responseTemplateMerge, merge, injectData)
                        if len(data_merge) > 0:
                            self.publishToolsData(data_merge, mergeMetadata)
            start_index_page = start_index_page + increase_count
            getProjectsUrl = baseUrl+"/projects?private_token="+accessToken+"&page="+str(start_index_page)+"&per_page="+str(data_in_a_page)
            projects = self.getResponse(getProjectsUrl, 'GET', None, None, None)
            if len(projects) > 0:
                project_count_exist = True
            else:
                project_count_exist = False
            if len(data) > 0:
                self.publishToolsData(data)
    def updateTrackingForBranch(self, trackingDetails, branchName, branch, delete_status):
        trackingDetails[branchName] = { 'latestCommitDate' : branch['commit']['created_at'], 'latestCommitId' : branch['commit']['id'], 'delete_status': delete_status}
    def getCommitInformation(self, commit, projectName):
        injectData = {}
        injectData['projectName'] = projectName
        responseTemplate = self.config.get('dynamicTemplate', {}).get('responseTemplate', None)
        return self.parseResponse(responseTemplate, commit, injectData)
if __name__ == "__main__":
    GitLabAgent()


