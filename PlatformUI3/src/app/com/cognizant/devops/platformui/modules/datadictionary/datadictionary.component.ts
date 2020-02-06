/*******************************************************************************
 * Copyright 2019 Cognizant Technology Solutions
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
import { Component, OnInit } from '@angular/core';
import { DataDictionaryService } from '@insights/app/modules/datadictionary/datadictionary.service';
import { DataSharedService } from '@insights/common/data-shared-service';


@Component({
  selector: 'app-datadictionary',
  templateUrl: './datadictionary.component.html',
  styleUrls: ['./datadictionary.component.css','./../home.module.css']
})
export class DatadictionaryComponent implements OnInit {
  dictResponse:any;
  toolDataSource = [];
  toolNodes=[];
  displayedToolColumns: string[];
  selectedTool1: any;
  selectedTool2: any;
  tool1TableData:any;
  tool2TableData:any;
  isrefresh:boolean=false;
  readChange: boolean = false;
  readChange2: boolean = false;
  showDetail: boolean = false;
  noShowDetail: boolean=false;
  noShowDetail2: boolean=false;
  showDetail2: boolean = false;
  showDetail3: boolean = false;
  noShowDetailCorr:boolean = false;
  relationPropertiesSize:boolean=false;
  showNoToolsSelectedForCorrelation:boolean =false;
  buttonOn:boolean= false;
  clicked:boolean=false;
  startToolNullPropertiesMessage=""
  endToolNullPropertiesMessage=""
  agent1Tool:any;
  agent1Category:any;
  agent1LabelName:any;
  agent2Tool:any;
  agent2Category:any;
  agent2LabelName:any;
  corrprop:any;
  corrData:any;
  labelSourceListDatasourceSelected: any = [];
  labelDestListDatasourceSelected: any = [];
  selectedSourceTool: any;
  selectedSourceLabel: any;
  responseDataSource = [];
  selectedDestinationTool: any;
  selectedDestinationLabel: any;
  labelListDatasourceSelected: any = [];

  
  constructor(private dataDictionaryService: DataDictionaryService, private dataShare: DataSharedService) { 
    this.dataDictionaryInfo();
  }

  ngOnInit() {
  }
  
  async dataDictionaryInfo() {
    try {
      // Loads Agent , Data Component and Services
      this.dictResponse = await this.dataDictionaryService.loadToolsAndCategories();
      if (this.dictResponse != null) {
        for (var key in this.dictResponse.data) {
          this.toolDataSource.push(this.dictResponse.data[key]);
        }
        
      }
      for (var data of this.toolDataSource) {
        this.labelListDatasourceSelected.push(data.toolName)
      }
      for (var data of this.labelListDatasourceSelected) {
        if (this.responseDataSource.indexOf(data) == -1) {
          this.responseDataSource.push(data);
        }
      }
    } catch (error) {
      console.log(error);
    }
  }
  async loadAgent1Info(selectedSourceTool) {
    try{
      this.noShowDetail = true;
      this.clicked=false;
      this.buttonOn=false;
      let usersResponseData1 = await this.dataDictionaryService.loadToolProperties(selectedSourceTool.labelName,selectedSourceTool.categoryName);
      //console.log(usersResponseData)
      this.agent1Tool=selectedSourceTool.toolName;
      this.agent1Category=selectedSourceTool.categoryName;
      this.agent1LabelName = selectedSourceTool.labelName;
      if (usersResponseData1.data != undefined && usersResponseData1.status == "success") {
        this.showDetail = true;
        this.noShowDetail = false;
        this.tool1TableData = usersResponseData1.data;
        //console.log(this.agent1Category);

      } else {
        this.noShowDetail = true;
        this.showDetail = false;
        this.startToolNullPropertiesMessage="No properties found"
        //console.log(this.startToolNullPropertiesMessage)
      }
    }catch (error) {
      console.log(error);
    }
  }
  async loadAgent1Info2(selectedDestinationLabel) {
    try {
      this.noShowDetail2=true;
      this.noShowDetailCorr=false;
      this.showDetail3=false;
      this.buttonOn=false;
      this.clicked=false;
      //console.log(selectedAgent2)
      let usersResponseData2 = await this.dataDictionaryService.loadToolProperties(selectedDestinationLabel.labelName,selectedDestinationLabel.categoryName);
      this.agent2Tool=selectedDestinationLabel.toolName;
      this.agent2Category=selectedDestinationLabel.categoryName;
      this.agent2LabelName = selectedDestinationLabel.labelName;
      if (usersResponseData2.data != undefined && usersResponseData2.status == "success") {
        //console.log(usersResponseData.data);
        this.showDetail2 = true;
        this.noShowDetail2=false;
        this.tool2TableData = usersResponseData2.data;
      } else{
        this.noShowDetail2=true;
        this.showDetail2=false;
        this.endToolNullPropertiesMessage="No properties found"
        //console.log(this.endToolNullPropertiesMessage)
      }
    }catch (error) {
      console.log(error);
    }
  }
  async loadAgent1Info3(selectedSourceLabel) {
    try {
      this.isrefresh = true;
      this.noShowDetail = true;
      this.clicked = false;
      this.buttonOn = false;
      let usersResponseData1 = await this.dataDictionaryService.loadToolProperties(selectedSourceLabel.labelName, selectedSourceLabel.categoryName);
      this.agent1Tool = selectedSourceLabel.toolName;
      this.agent1Category = selectedSourceLabel.categoryName;
      this.agent1LabelName = selectedSourceLabel.labelName;
      if (usersResponseData1.data != undefined && usersResponseData1.status == "success") {
        this.showDetail = true;
        this.noShowDetail = false;
        this.tool1TableData = usersResponseData1.data;
      } else {
        this.noShowDetail = true;
        this.showDetail = false;
        this.startToolNullPropertiesMessage = "No properties found"
      }
    } catch (error) {
      console.log(error);
    }
  }
  async loadAgent1Info4(selectedDestinationLabel) {
    try {
      this.isrefresh = true;
      this.noShowDetail2 = true;
      this.noShowDetailCorr = false;
      this.showDetail3 = false;
      this.buttonOn = false;
      this.clicked = false;
      let usersResponseData2 = await this.dataDictionaryService.loadToolProperties(selectedDestinationLabel.labelName, selectedDestinationLabel.toolCategory);
      this.agent2Tool = selectedDestinationLabel.toolName;
      this.agent2Category = selectedDestinationLabel.categoryName;
      this.agent2LabelName = selectedDestinationLabel.labelName;
      if (usersResponseData2.data != undefined && usersResponseData2.status == "success") {
        this.showDetail2 = true;
        this.noShowDetail2 = false;
        this.tool2TableData = usersResponseData2.data;
      } else {
        this.noShowDetail2 = true;
        this.showDetail2 = false;
        this.endToolNullPropertiesMessage = "No properties found"
      }
    } catch (error) {
      console.log(error);
    }
  }
  selectLabelforsource(selectedSourceTool) {
    var islabelsameastool = false;
    this.labelSourceListDatasourceSelected = []
    this.toolDataSource.filter(av => {
      if (av.toolName == selectedSourceTool) {
        if (av.labelName != null) {
          this.labelSourceListDatasourceSelected.push(av)
        }
      }
    }
    )
  }
  selectLabelfordestination(selectedDestinationTool) {
    this.labelDestListDatasourceSelected = []
    this.toolDataSource.filter(av => {
      if (av.toolName == selectedDestinationTool) {
        if (av.labelName != null) {
          this.labelDestListDatasourceSelected.push(av)
        }
      }
    }
    )
  }
  async getCorrelation(data1,data2){
    try{
      this.showDetail3=false;
      this.noShowDetailCorr=false;
      this.clicked=true;
      this.buttonOn=true;
      this.showNoToolsSelectedForCorrelation=true
      //console.log(data1,data2);
      let usersResponseData3 = await this.dataDictionaryService.loadToolsRelationshipAndProperties(data1.labelName,data1.categoryName,data2.labelName,data2.categoryName);
      if (usersResponseData3.data != undefined && usersResponseData3.status == "success") {
        //console.log(usersResponseData3)
        if (usersResponseData3.data["relationName"] != undefined){
          this.showDetail3 = true;
          this.noShowDetailCorr=false;
          this.corrprop=usersResponseData3.data["relationName"];
          //console.log(Object.keys(usersResponseData3.data["properties"]).length);
          if (usersResponseData3.data["properties"] != undefined && Object.keys(usersResponseData3.data["properties"]).length > 0){
            this.relationPropertiesSize=true;
            this.corrData=usersResponseData3.data["properties"];
          }else{
            this.relationPropertiesSize=false;
          }
        
        } 
      }else{
        this.noShowDetailCorr=true;
        this.showDetail3=false;
      }
    }catch (error) {
      console.log(error);
    }

  }

  /* showDetailsofCorrelation(selectedRelation) {
    var isSessionExpired = this.dataShare.validateSession();
    if (!isSessionExpired) {
      let showJsonDialog = this.dialog.open(ShowJsonDialog, {
        panelClass: 'showrelationship-dialog-container',
        height: '500px',
        width: '700px',
        disableClose: true,
        data:
        {
          message: selectedRelation,
          title: "Relationship Details"
        }

      });
    }
  } */

}
