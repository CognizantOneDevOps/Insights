///<reference path="../../../headers/common.d.ts" />
export class Transformers {
  FieldsMetaDataArray: string[] = [];
  constructor() {
    }
    //get fields for db object from real data
  getFields(data): any  {
    for (var i in data.results){
      this.FieldsMetaDataArray.push.apply(this.FieldsMetaDataArray,(data.results[i].columns));
    }
    return this.FieldsMetaDataArray;

  }
  //insert for property value in  clone object from real data
 insertValueProperty(data,cloneObject): any {
   if (data !== undefined && cloneObject.pipelineDataModel !== undefined){
      for (let x = 0 ; x < cloneObject.pipelineDataModel.length ; x++){
        let pipeline = cloneObject.pipelineDataModel[x];
          for (let i = 0 ; i < pipeline.toolsList.length ; i++){
            let toolList =  pipeline.toolsList[i];
            for ( let j = 0 ; j < toolList.fieldList.length ; j++){
              let fields = toolList.fieldList[j];
                for (let fieldName of data.results[x].columns) {
                  if (fields.dbName === fieldName){
                    fields['value'] = data.results[x].data[0].row[data.results[x].columns.indexOf(fieldName)];
                    break;
                  }
                }
            }
          }
        }
       }
  return cloneObject;
  }
}
