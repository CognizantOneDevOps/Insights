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
import { RelationshipBuilderService } from '@insights/app/modules/relationship-builder/relationship-builder.service';
import { ShowJsonDialog } from '@insights/app/modules/relationship-builder/show-correlationjson';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { RelationLabel } from '@insights/app/modules/relationship-builder/relationship-builder.label';
import { from } from 'rxjs';
import { Router } from "@angular/router";
import { ActivatedRoute } from '@angular/router';
import { MessageDialogService } from '@insights/app/modules/application-dialog/message-dialog-service';
import { MatTableDataSource } from '@angular/material';
import { DataSharedService } from '@insights/common/data-shared-service';
import { count } from 'rxjs/operators';
//import { Control} from '@angular/common';
@Component({
  selector: 'app-relationship-builder',
  templateUrl: './relationship-builder.component.html',
  styleUrls: ['./relationship-builder.component.css', './../home.module.css']
})
export class RelationshipBuilderComponent implements OnInit {
  deleteRelation: any = undefined;
  element: any = undefined;
  readChange: boolean = false;
  readChange2: boolean = false;
  deleteRelationArray = [];
  relationmappingLabels: RelationLabel[] = [];
  prefixname: string = '';
  property1selected: boolean = false;
  searchValue: string = '';
  property2selected: boolean = false;
  isbuttonenabled: boolean = false;
  dictResponse: any;
  corelationResponseMaster = [];
  dataComponentColumns = [];
  agentDataSource = [];
  AddDestination = {};
  newSource = [];
  AddSource = {};
  corrprop = [];
  fieldDestProp = [];
  fieldSourceProp = [];
  saveRelationArray = [];
  selectedProperty2: any;
  selectedProperty1: any;
  selectedAgent1: any;
  isListView = false;
  corelationResponseMaster2: any;
  isEditData = false;
  isrefresh: boolean = false;
  isSaveEnabled: boolean = false;
  selectedAgent2: any;
  destinationcheck = [];
  sourcecheck = [];
  agent1TableData: any;
  agent2TableData: any;
  finalRelationName: string = '';
  showApplicationMessage: String = "";
  listFilter: any;
  showDetail: boolean = false;
  noShowDetail: boolean = false;
  noShowDetail2: boolean = false;
  showDetail2: boolean = false;
  isDisabledState: boolean = false;
  MAX_ROWS_PER_TABLE = 5;
  showDetail3: boolean = false;
  noShowDetailCorr: boolean = false;
  showNoToolsSelectedForCorrelation: boolean = false;
  buttonOn: boolean = false;
  clicked: boolean = false;
  startToolNullPropertiesMessage = ""
  endToolNullPropertiesMessage = ""
  agent1Tool: any;
  agent1Category: any;
  agent2Tool: any;
  count: any;
  agent2Category: any;
  public data: any;
  selectedRadio: any;
  regex = new RegExp("^[a-zA-Z0-9_]*$");
  relData: any;
  displayDataSource = [];
  toolsDatasource = [];
  toolSourceDataSource = [];
  radioRefresh:boolean= true;

  constructor(private router: Router, private relationshipBuilderService: RelationshipBuilderService, private dialog: MatDialog, public messageDialog: MessageDialogService, private dataShare: DataSharedService) {
    this.dataDictionaryInfo();
    this.getCorrelation();
  }

  ngOnInit() {
  }

  async dataDictionaryInfo() {
    try {
      this.dictResponse = await this.relationshipBuilderService.loadToolsAndCategories();
      if (this.dictResponse != null) {
        for (var key in this.dictResponse.data) {
          this.agentDataSource.push(this.dictResponse.data[key]);
        }

      }
    } catch (error) {
      console.log(error);
    }
  }
  async loadAgent1Info(selectedAgent1) {
    try {
      this.isrefresh = true;
      this.noShowDetail = true;
      this.clicked = false;
      this.buttonOn = false;
      let usersResponseData1 = await this.relationshipBuilderService.loadToolProperties(selectedAgent1.toolName, selectedAgent1.categoryName);
      //console.log(usersResponseData)
      this.agent1Tool = selectedAgent1.toolName;
      this.agent1Category = selectedAgent1.categoryName;
      if (usersResponseData1.data != undefined && usersResponseData1.status == "success") {
        this.showDetail = true;
        this.noShowDetail = false;
        this.agent1TableData = usersResponseData1.data;
        //console.log(this.agent1Category);

      } else {
        this.noShowDetail = true;
        this.showDetail = false;
        this.startToolNullPropertiesMessage = "No properties found"
        //console.log(this.startToolNullPropertiesMessage)
      }
    } catch (error) {
      console.log(error);
    }
  }
  async loadAgent1Info2(selectedAgent2) {
    try {
      this.isrefresh = true;
      this.noShowDetail2 = true;
      this.noShowDetailCorr = false;
      this.showDetail3 = false;
      this.buttonOn = false;
      this.clicked = false;
      //console.log(selectedAgent2)
      let usersResponseData2 = await this.relationshipBuilderService.loadToolProperties(selectedAgent2.toolName, selectedAgent2.categoryName);
      this.agent2Tool = selectedAgent2.toolName;
      this.agent2Category = selectedAgent2.categoryName;
      if (usersResponseData2.data != undefined && usersResponseData2.status == "success") {
        this.showDetail2 = true;
        this.noShowDetail2 = false;
        this.agent2TableData = usersResponseData2.data;
      } else {
        this.noShowDetail2 = true;
        this.showDetail2 = false;
        this.endToolNullPropertiesMessage = "No properties found"
      }
    } catch (error) {
      console.log(error);
    }
  }


  async  getCorrelation() {
    try {

      this.displayDataSource = [];
      this.toolsDatasource = [];
      this.saveRelationArray = [];
      this.toolSourceDataSource = [];
      this.corelationResponseMaster = [];
      //this.relationmappingLabels = [];


      let correlationresponse = await this.relationshipBuilderService.loadUiServiceLocation()
      // console.log("Line 182" + correlationresponse);
      if (correlationresponse.status == "success") {
        this.corelationResponseMaster = correlationresponse.data;
        this.relationmappingLabels = [];
      }
      // console.log("Line 186" + this.corelationResponseMaster);
      for (var element of this.corelationResponseMaster) {
        var destinationToolName = (element.destination.toolName);
        var sourceToolName = (element.source.toolName);
        var detailProp = '<b>' + element.source.toolName + '</b>:' + element.source.fields[0] + ':<b>' + element.destination.toolName + '</b>:' + element.destination.fields[0];
        //element['prop'] = detailProp;
        let relationLabel = new RelationLabel(destinationToolName, sourceToolName, element.relationName, detailProp);
        //console.log(element);
        this.relationmappingLabels.push(relationLabel);
        // this.displayDataSource.push(relationLabel);
        this.destinationcheck.push(destinationToolName);
        this.sourcecheck.push(sourceToolName);
      }
      // console.log("Line 198" + this.displayDataSource);
      this.dataComponentColumns = ['radio', 'relationName'];
    }
    catch (error) {
      console.log(error);
    }
  }

  async showDetailsDialogForNeo4j(data1, data2) {
    try {
      this.showDetail3 = false;
      this.noShowDetailCorr = false;
      this.clicked = true;
      this.buttonOn = true;
      this.showNoToolsSelectedForCorrelation = true
      let usersResponseData3 = await this.relationshipBuilderService.loadToolsRelationshipAndProperties(data1.toolName, data1.categoryName, data2.toolName, data2.categoryName);
      if (usersResponseData3.data != undefined && usersResponseData3.status == "success") {
        if (usersResponseData3.data["relationName"] != undefined) {
          this.showDetail3 = true;
          this.noShowDetailCorr = false;
          this.corrprop = usersResponseData3.data["relationName"];
          //  console.log(this.corrprop);
          var isSessionExpired = this.dataShare.validateSession();
          if (!isSessionExpired) {
            let showJsonDialog = this.dialog.open(ShowJsonDialog, {
              panelClass: 'showjson-dialog-container',
              height: '300px',
              width: '500px',
              disableClose: true,
              data:
              {
                message: this.corrprop,
                title: "Co-Relation in Neo4j"

              }
            });

          }
        }
      } else {
        this.noShowDetailCorr = true;
        this.showDetail3 = false;
        var isSessionExpired = this.dataShare.validateSession();
        if (!isSessionExpired) {
          let showJsonDialog = this.dialog.open(ShowJsonDialog, {
            panelClass: 'showjson-dialog-container',
            height: '300px',
            width: '500px',
            disableClose: true,
            data:
            {
              message: 'No Relations Found between ' + this.selectedAgent1.toolName + ' and ' + this.selectedAgent2.toolName,
              title: "Co-Relations in Neo4j"
            }

          });

        }

      }
    } catch (error) {
      console.log(error);

    }

  }

  showDetailsDialog() {
    var isSessionExpired = this.dataShare.validateSession();
    if (!isSessionExpired) {
      let showJsonDialog = this.dialog.open(ShowJsonDialog, {
        panelClass: 'showjson-dialog-container',
        height: '500px',
        width: '700px',
        disableClose: true,
        data:
        {
          message: this.corelationResponseMaster,
          title: "Correlation.json"
        }

      });
    }
  }
  addproperty() {

  }

  Refresh() {
    this.showDetail = false;
    this.showDetail2 = false;
    this.agentDataSource = [];
    this.selectedProperty1 = "";
    this.selectedProperty2 = "";
    this.searchValue = null;
    this.selectedRadio = "";
    this.isbuttonenabled = false;
    this.isSaveEnabled = false;
    this.listFilter = "";
    this.isrefresh = false;
    this.buttonOn = false;
    this.selectedAgent1 = "";
    this.dataDictionaryInfo();
    this.radioRefresh=false;
    this.deleteRelation="";
  }

  relationDelete() {
    this.isListView = true;
    this.isEditData = true;
    var title = "Delete Correlation";
    //  console.log(this.deleteRelation);
    var dialogmessage = "You are deleting a Co-Relation " + "<b>" + this.deleteRelation.relationName + "</b>" + ". The action of deleting a Co-Relation CANNOT be UNDONE. Moreover deleting an existing Co-Relation may impact other functionalities. Are you sure you want to DELETE the Co-Relation <b>" + this.deleteRelation.relationName + "</b> ?";
    const dialogRef = this.messageDialog.showConfirmationMessage(title, dialogmessage, this.deleteRelation.relationName, "ALERT", "40%");
    var relationName = this.deleteRelation.relationName;
    dialogRef.afterClosed().subscribe(result => {
      if (result == 'yes') {
        this.deleteRelationArray = [];
        for (var element of this.corelationResponseMaster) {
          if (element.relationName != this.deleteRelation.relationName) {
            this.deleteRelationArray.push(element);
          }
        }
        //console.log(this.deleteRelationArray)
        var deleteMappingJson = JSON.stringify({ 'data': this.deleteRelationArray });
        this.relationshipBuilderService.saveCorrelationConfig(deleteMappingJson).then(
          (corelationResponse2) => {
            if (corelationResponse2.status == "success") {
              this.getCorrelation();
              var dialogmessage = "<b>" + relationName + "</b> deleted successfully from Correlation.json."
              this.messageDialog.showApplicationsMessage(dialogmessage, "SUCCESS");
            }
          });
        this.Refresh();
      }
    });

    this.count = 0;
    this.destinationcheck = [];
    this.sourcecheck = [];
  }


  enableDelete() {
    this.isbuttonenabled = true;
    this.isrefresh = true;

  }

  enableSaveProperty1() {
    this.property1selected = true;
    if (this.property2selected == true) {
      this.isSaveEnabled = true;
    }
  }

  enableSaveProperty2() {
    this.property2selected = true;
    if (this.property1selected == true) {
      this.isSaveEnabled = true;
    }
  }

  PropertyAdd() {
  }
  saveData(newName) {
    this.saveRelationArray = [];
    this.isListView = true;
    this.isEditData = true;
    var counter = 0;
    var checkname = this.regex.test(newName.value);
    if (!checkname) {
      newName = undefined;
      this.messageDialog.showApplicationsMessage("Please enter valid name, and it contains only alphanumeric character and underscore ", "ERROR");
    }
    else {
      this.prefixname = "FROM_" + this.selectedAgent1.toolName + "_TO_" + this.selectedAgent2.toolName + "_";
      this.finalRelationName = this.prefixname + newName.value;

      this.count = 0;
      for (var x in this.destinationcheck) {

        if ((this.destinationcheck[x] == this.selectedAgent2.toolName) && (this.sourcecheck[x] == this.selectedAgent1.toolName)) {
          // console.log("present");
          this.count = this.count + 1;
          break;
        }
        else {
          //  console.log("not present");

        }
      }
      if (this.count == 0) {
        var title = "Save Co-Relation";
        var dialogmessage = "You are creating a new Co-Relation " + "<b>" + this.finalRelationName + "</b>" + " between <b>" + this.selectedAgent1.toolName + "</b> and  <b> " + this.selectedAgent2.toolName + "</b> . Are you sure do you want to build the Co-Relation <b>" + this.finalRelationName + "</b> ?";
        const dialogRef = this.messageDialog.showConfirmationMessage(title, dialogmessage, this.deleteRelation, "ALERT", "40%");

        dialogRef.afterClosed().subscribe(result => {
          if (result == 'yes') {
            //DESTINATION
            this.fieldDestProp.push(this.selectedProperty2);
            var res = [];
            for (var x in this.selectedAgent2) {
              this.selectedAgent2.hasOwnProperty(x) && res.push(this.selectedAgent2[x])
            }
            var toolname = res[0];
            var toolcatergory = res[1];
            this.AddDestination = { 'toolName': toolname, 'toolCategory': toolcatergory, 'fields': this.fieldDestProp };
            //FOR SOURCE 
            this.fieldSourceProp.push(this.selectedProperty1);
            var res1 = [];
            for (var x in this.selectedAgent2) {
              this.selectedAgent1.hasOwnProperty(x) && res1.push(this.selectedAgent1[x])
            }
            var toolname1 = res1[0];
            var toolcatergory1 = res1[1];
            this.AddSource = { 'toolName': toolname1, 'toolCategory': toolcatergory1, 'fields': this.fieldSourceProp };
            var newData = {
              'destination': this.AddDestination, 'source': this.AddSource, 'relationName': this.finalRelationName
            }

            for (var element of this.corelationResponseMaster) {
              this.saveRelationArray.push(element)
            }
            this.saveRelationArray.push(newData);
            // console.log(this.saveRelationArray);
            var addMappingJson = JSON.stringify({ 'data': this.saveRelationArray });
            this.relationshipBuilderService.saveCorrelationConfig(addMappingJson).then(
              (corelationResponse2) => {
                if (corelationResponse2.status == "success") {
                  this.getCorrelation();
                  var dialogmessage = "<b>" + this.finalRelationName + "</b> saved successfully in Correlation.json."
                  this.messageDialog.showApplicationsMessage(dialogmessage, "SUCCESS");
                  this.Refresh();
                }
              });
          }
        });
        // this.count = 0;
      }
      else if (this.count == 1) {
        this.showApplicationMessage = "Failed to save settings"
        var dialogmessage = "Co-Relation between <b>" + this.selectedAgent1.toolName + "</b> and <b>" + this.selectedAgent2.toolName + "</b> already exists in the Correlation.json.If you wish to create a new Co-Relation please delete the existing Co-relation and save it with a UNIQUE name."
        this.messageDialog.showApplicationsMessage(dialogmessage, "ERROR");
        this.count = 0;
        newName = undefined;
      }
      else {
        var dialogmessage = "Failed to create the Co-Relation <b>" + this.finalRelationName + "</b>. Please try again."
        this.messageDialog.showApplicationsMessage(dialogmessage, "ERROR");
      }
    }
  }
  deleteMapping() {
  }
}
