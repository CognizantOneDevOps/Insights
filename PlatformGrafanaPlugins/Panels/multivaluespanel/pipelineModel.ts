///<reference path="../../../headers/common.d.ts" />


export class PipelineModel {
constructor(
  public pipelineRefId: string,
  public fieldsList: FieldModel[]
  ){ }
}
export class FieldModel {
constructor(

  public fieldName: string,
  public fieldMapName: string,
  public fieldColor: string,
  public fieldPosition: number
  ) { }

}
