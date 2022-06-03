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
import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'assetPipe'
})
export class AssetPipe implements PipeTransform {
  transform(val) {
    let data = '';
    Object.keys(val).map((f) => {
      const key = f[0].toUpperCase() + f.substring(1);
      if (key === 'Moddate' || key === 'TxID' || key === 'AssetID' || key === 'InnerObjAsset') {
        return;
      }else if(val[f]!='undefined' && val[f].toString().startsWith("http")){
        data += `<span class="propAsset""><b class="propAsset">${key}</b><span class="propAsset"> : <a href="${val[f]}">${val[f]}</a></span> </span> <br/>`;
      }else {
        data += `<span class="propAsset""> <b class="propAsset">${key}</b><span class="propAsset"> : ${(val[f] === 'null' || val[f] === '') ? 'N/A' : val[f].toString()}</span> </span> <br/>`;
      }
    })
    /* var(--backgnd-clr5); */
    return data;
  }
}
