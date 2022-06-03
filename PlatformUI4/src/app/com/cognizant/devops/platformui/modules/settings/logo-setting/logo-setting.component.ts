/*******************************************************************************
 * Copyright 2022 Cognizant Technology Solutions
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
import { Component, OnInit, ViewChild, ElementRef } from "@angular/core";
import { LogoSettingService } from "@insights/app/modules/settings/logo-setting/logo-setting.service";
import {
  HttpClientModule,
  HttpHeaders,
  HttpClient,
} from "@angular/common/http";
import { RestAPIurlService } from "@insights/common/rest-apiurl.service";
import { CookieService } from "ngx-cookie-service";
import { Constructor } from "@angular/cdk/table";
import { MessageDialogService } from "@insights/app/modules/application-dialog/message-dialog-service";
import { BulkUploadComponent } from "../../bulkupload/bulkupload.component";
import { FormArray } from "@angular/forms";

@Component({
  selector: "app-logo-setting",
  templateUrl: "./logo-setting.component.html",
  styleUrls: [
    "./logo-setting.component.scss",
    "./../../home.module.scss",
    "../../bulkupload/bulkupload.component.scss",
  ],
})
export class LogoSettingComponent implements OnInit {
  trackingUploadedFileContentStr: string = "";
  @ViewChild("fileUpload", { static: true }) myFileDiv: ElementRef;
  files: any;
  response: any;
  size: boolean = false;
  buttonEnable: boolean = false;
  url = "";
  fileName: any = "";
  fileNameArr = [];
  selectedFile: File = null;
  rows: FormArray;
  index: number = 0;
  row: any = 0;
  flag: boolean;

  constructor(
    private logoSettingService: LogoSettingService,
    private http: HttpClient,
    private restAPIUrlService: RestAPIurlService,
    private cookieService: CookieService,
    public messageDialog: MessageDialogService
  ) {}

  ngOnInit() {}

  cancelUpload(index) {
    this.cancelFileUpload();
    console.log("Inside cancelUpload");
    this.fileNameArr[index] = null;
    this.rows[index] = null;
  }

  onFileChanged(event, index) {
    console.log("Inside onFileChanged");
    this.selectedFile = <File>event.target.files[0];
    var size = this.selectedFile.size;
    var exe = this.selectedFile.name;
    let img = document.querySelector("img");
    let img_width = img.width;
    let img_hieght = img.height;
    console.log("img hieght:", img_hieght, "img width:", img_width);
    this.fileNameArr[index] = this.selectedFile.name;
    this.onSelectFile(event);
  }

  onSelectFile(event) {
    console.log("Inside onSelectFile");
    this.buttonEnable = true;
    if (event.target.files && event.target.files[0]) {
      var reader = new FileReader();
      reader.readAsDataURL(event.target.files[0]);
      reader.onload = (event: any) => {
        this.url = event.target.result;
      };
    }
  }

  uploadFile() {
    console.log("Inside uploadFile");
    var file = this.myFileDiv.nativeElement.files[0];
    var dummy = <HTMLInputElement>document.getElementById("file");
    var bytes = file["size"];
    var fileName = file["name"];
    var testFileExt = this.checkFile(file, ".png");
    if (bytes > 1048576) {
      this.size = true;
      this.messageDialog.openSnackBar(
        "Please select a of file size less than 1Mb",
        "error"
      );
      this.buttonEnable = false;
      dummy.value = "";
    } else if (!testFileExt) {
      this.messageDialog.openSnackBar(
        "Please select a valid .PNG file",
        "ERROR"
      );
      this.buttonEnable = false;
      dummy.value = "";
    } else if (testFileExt && !this.size) {
      this.logoSettingService.uploadLogo(file).subscribe((event) => {
        if (event.status == "success") {
          dummy.value = "";
          this.buttonEnable = true;
          this.messageDialog.openSnackBar(
            "<b>" +
              fileName +
              "</b> uploaded successfully. Please LOGOUT and LOGIN again in to the Insights Application to see the uploaded logo.",
            "success"
          );
        } else if (event.status == "failure") {
          dummy.value = "";
          this.buttonEnable = false;
          this.messageDialog.openSnackBar(
            "Unable to upload file, Please try with different file :  " +
              event.message,
            "error"
          );
        }
      });
    }
  }

  checkFile(sender, validExts) {
    console.log("Inside checkFile");
    if (sender) {
      var fileExt = sender.name;
      fileExt = fileExt.substring(fileExt.lastIndexOf("."));
      fileExt = fileExt.toLowerCase();
      if (validExts.indexOf(fileExt) < 0 && fileExt != "") {
        return false;
      } else {
        return true;
      }
    }
  }

  cancelFileUpload() {
    console.log("Inside cancelFileUpload");
    var dummy = <HTMLInputElement>document.getElementById("file");
    console.log(dummy);
    dummy.value = "";
    this.buttonEnable = false;
  }
}
