/*******************************************************************************
 * Copyright 2020 Cognizant Technology Solutions
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

import { Injectable } from '@angular/core';
import { RestCallHandlerService } from '@insights/common/rest-call-handler.service';
import { Observable } from 'rxjs';


@Injectable()
export class FileSystemService {

    constructor(private restCallHandlerService: RestCallHandlerService) {
    }

    uploadFileWithDetails(file: File, fileName: string, fileType: string, module: string): Observable<any> {
        const fd: FormData = new FormData();
        fd.append('file',file);
        return this.restCallHandlerService.postFormDataWithParameter("UPLOAD_CONFIG_FILE", fd, { fileName:fileName, fileType:fileType, module:module });
    }

    loadFileType(): Promise<any> {
        return this.restCallHandlerService.get("GET_FILE_TYPE");
    }

    loadFileModule(): Promise<any> {
        return this.restCallHandlerService.get("GET_FILE_MODULE");
    }

    loadConfigFilesList(): Promise<any> {
        return this.restCallHandlerService.get("GET_CONFIG_FILES");
    }

    deleteConfigFile(fileName: string): Promise<any> {
        return this.restCallHandlerService.postWithParameter("DELETE_CONFIG_FILE", {'fileName':fileName}, { 'Content-Type': 'application/x-www-form-urlencoded' }).toPromise();
    }

    downloadConfigFile(FileDetailJson: string) {
        return this.restCallHandlerService.postWithPDFData("DOWNLOAD_CONFIG_FILE", FileDetailJson,"",{ 'Content-Type': 'application/json' },{'responseType':'blob'}).toPromise();
    }
    
} 