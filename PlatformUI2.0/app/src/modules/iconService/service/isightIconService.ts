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
    export interface IIconService {
      getIcon(toolName: string): string;
    }

    export class IconService implements IIconService {
        constructor() { }

        toolsIconData = {
            'ALM': 'dist/icons/svg/ALM-new.svg',
            'SCM': 'dist/icons/svg/SCM-new.svg',
            'CI': 'dist/icons/svg/CI-new.svg',
            'Artifact_Management': 'dist/icons/svg/artifact-mangement-new.svg',
            'Code_Quality': 'dist/icons/svg/code-quality-new.svg',
            'Continuous Testing': 'dist/icons/svg/continuous-testing-new.svg',
            'Deployment': 'dist/icons/svg/deployment-new.svg',
            'Cloud': 'dist/icons/svg/cloud-new.svg',
            'JIRA': 'dist/icons/svg/toolsIcon/ALM/JIRA.svg',
            'Rally': 'dist/icons/svg/toolsIcon/ALM/Rally.svg',
            'GIT': 'dist/icons/svg/toolsIcon/SCM/GIT.svg',
            'BitBucket': 'dist/icons/svg/toolsIcon/SCM/BitBucket.svg',
            'CVS': 'dist/icons/svg/toolsIcon/SCM/CVS.svg',
            'TFS': 'dist/icons/svg/toolsIcon/SCM/TFS.svg',
            'Perforce': 'dist/icons/svg/toolsIcon/SCM/Perforce.svg',
            'Subversion': 'dist/icons/svg/toolsIcon/SCM/Subversion.svg',
            'Stash': 'dist/icons/svg/toolsIcon/SCM/stash.svg',
            'JENKINS': 'dist/icons/svg/toolsIcon/CI/Jenkins.svg',
            'Bamboo': 'dist/icons/svg/toolsIcon/CI/Bamboo.svg',
            'Teamcity': 'dist/icons/svg/toolsIcon/CI/Teamcity.svg',
            'Nexus': 'dist/icons/svg/toolsIcon/artifactManagement/Nexus.svg',
            'Artifactory': 'dist/icons/svg/toolsIcon/artifactManagement/Artifactory.svg',
            'SONAR': 'dist/icons/svg/toolsIcon/codeQuality/SONAR.svg',
            'FishEye': 'dist/icons/svg/toolsIcon/codeQuality/FishEye.svg',
            'Crucible': 'dist/icons/svg/toolsIcon/codeQuality/Crucible.svg',
            'Coverity': 'dist/icons/svg/toolsIcon/codeQuality/Coverity.svg',
            'QTP': 'dist/icons/svg/toolsIcon/testing/QTP.svg',
            'Selenium': 'dist/icons/svg/toolsIcon/testing/Selenium.svg',
            'Rundeck': 'dist/icons/svg/toolsIcon/deployment/Rundeck.svg',
            'Docker': 'dist/icons/svg/toolsIcon/deployment/Docker.svg',
            'Amazon': 'dist/icons/svg/toolsIcon/cloud/Amazon.svg',
            'Azure': 'dist/icons/svg/toolsIcon/cloud/Azure.svg',
            'Cloudstack': 'dist/icons/svg/toolsIcon/cloud/Cloudstack.svg',
            'Openstack': 'dist/icons/svg/toolsIcon/cloud/Openstack.svg',
            'Vmware': 'dist/icons/svg/toolsIcon/cloud/Vmware.svg',
        }
        iconSrc : string;

        getIcon(toolName: string): string {
    			   for(var tool in this.toolsIconData){
                if(tool === toolName){
                  this.iconSrc = this.toolsIconData[tool];
                }
             }
             return this.iconSrc;
    		}
    }
}
