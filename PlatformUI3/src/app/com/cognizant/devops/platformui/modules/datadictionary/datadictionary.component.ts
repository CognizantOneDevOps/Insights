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
import { ShowJsonDialog } from '@insights/app/modules/relationship-builder/show-correlationjson';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { RelationshipBuilderService } from '@insights/app/modules/relationship-builder/relationship-builder.service';
import { RelationLabel } from '@insights/app/modules/relationship-builder/relationship-builder.label';




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
  
  element: any = undefined;
  relationmappingLabels: RelationLabel[] = [];
  neo4jRelationMappingLabels: RelationLabel[] = [];
  corelationResponseMaster = [];
  dataComponentColumns = [];
  flag: boolean = false;
  isSelfRelation: boolean = false;
 
  

  constructor(private dataDictionaryService: DataDictionaryService, private relationshipBuilderService: RelationshipBuilderService,private dataShare: DataSharedService,private dialog: MatDialog) { 
    this.dataDictionaryInfo();
    this.getCorrelationProperties();
  }

  ngOnInit() {
  }

  /*  This method will get all the correlations present in Postgres */
  async  getCorrelationProperties() {
    try {
      this.corelationResponseMaster = [];
      let correlationresponse = await this.relationshipBuilderService.loadUiServiceLocation()
      if (correlationresponse.status == "success") {
        this.corelationResponseMaster = correlationresponse.data;
        this.relationmappingLabels = [];
      }
      for (var element of this.corelationResponseMaster) {
        var destinationToolName = (element.destinationLabelName);
        var sourceToolName = (element.sourceLabelName);
        var flag = (element.enableCorrelation);
        //var detailProp = '<b>' + element.sourceToolName + '</b>:' + element.sourceFields + ':<b>' + element.destinationToolName + '</b>:' + element.destinationFields;
        let relationLabel = new RelationLabel(destinationToolName, sourceToolName, element.relationName, element.sourceFields, element.destinationFields, element.relationship_properties, flag, element.isSelfRelation);
        this.relationmappingLabels.push(relationLabel);
      }
    }
    catch (error) {
      console.log(error);
    }
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


  /*  This method will get correlation details between selected tools from neo4j */
 async getCorrelation(data1,data2){
    try{
      this.showDetail3=false;
      this.noShowDetailCorr=false;
      this.clicked=true;
      this.buttonOn=true;
      this.showNoToolsSelectedForCorrelation=true
      this.neo4jRelationMappingLabels = [];
      let usersResponseData3 = await this.dataDictionaryService.loadToolsRelationshipAndProperties(data1.labelName,data1.categoryName,data2.labelName,data2.categoryName);
      if (usersResponseData3.data != undefined && usersResponseData3.data.length > 0) {
        let relationsArray = usersResponseData3.data;
        for(var relation of relationsArray) {
            this.showDetail3 = true;
            this.noShowDetailCorr=false;
            this.corrprop=relation.relationName;
            var destinationToolName = data2.labelName;
            var sourceToolName = data1.labelName;
            var flag = true;
            let relationLabel = new RelationLabel(destinationToolName, sourceToolName, this.corrprop, null, null, null, flag, false);
            this.neo4jRelationMappingLabels.push(relationLabel);        
        }  
        this.dataComponentColumns = ['relationName'];
        
        } 
      else{
        this.noShowDetailCorr=true;
        this.showDetail3=false;
      }
    }catch (error) {
      console.log(error);
    }

  }

  /* This method will check whether the correlation exists in Postgres. */
  checkIfRelationExistsInPostGres(selectedRelation) {
    for(var relation of this.relationmappingLabels) {
      if (relation.relationName == selectedRelation.relationName) {
          return relation;
          
      }         
    }
    return selectedRelation;
  }

  

  showDetailsofCorrelation(selectedRelation) {
    selectedRelation = this.checkIfRelationExistsInPostGres(selectedRelation);
    if (selectedRelation != undefined) {
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
    }
  }


}
