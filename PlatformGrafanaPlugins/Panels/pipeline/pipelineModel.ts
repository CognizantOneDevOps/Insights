///<reference path="../../../headers/common.d.ts" />

/*export class PipelinePageModel {
constructor(
  public toolCategory: ToolsCategoryModel[],
  public pipelineDataModel: PipelineModel[]
  ) { }
}*/
/*export class ToolsCategoryModel {
constructor(
  public toolCategoryName: string,
  public toolCategoryIcon: string
  ) { }
}*/
export class PipelineModel {
constructor(
  public pipelineRefId: string,
  public pipelineName: string,
  public pipelineColor: string,
  public toolsList: ToolModel[]
  ){ }
}
export class ToolModel {
constructor(
  public position: number,
  public toolCategoryName: string,
  public toolCategoryIcon: string,
  public fieldList: FieldLevelModel[]
  ) { }
}
export class FieldLevelModel {
constructor(
  public dbName: string,
  public displayName: string
  //public value: number
  ) { }
}
