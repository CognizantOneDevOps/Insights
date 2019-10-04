/// <reference path="../../../../../public/app/headers/common.d.ts" />
import { Fields } from './toolsInsightModel';
export declare class ToolsInsightEditorCtrl {
    private $q;
    private uiSegmentSrv;
    toolsInsightsPanel: any;
    toolsInsightsPanelCtrl: any;
    dataSourceResponse: any;
    toolListData: any[];
    addColumnSegment: any;
    fieldList: any[];
    selectedFieldList: any[];
    toolDataJson: {};
    selectedToolsDetailJson: {};
    showFieldDetails: boolean;
    fieldVal: Fields[];
    toolMappingJson: any[];
    defaultMappingJson: any[];
    defaultButtonOption: number;
    advanceSettingOption: number;
    /** @ngInject */
    constructor($scope: any, $q: any, uiSegmentSrv: any);
    render(): void;
    getToolListOptions(toolDataArray: any): void;
    getToolOptions(): any;
    addTool(): void;
    removeTool(tool: any, index: any): void;
    getDefaultTools(tool: any): void;
    getFieldOptions(selectedTool: any): any;
    getDefaultFieldMapping(tool: any): void;
    addDefaultFieldOption(tool: any, option: any): void;
    addFields(selectedToolNm: any): void;
    removeField(key: any, field: any, indexValue: any): void;
    onSubmitAction(): void;
    defaultValueAction(): void;
    customValueAction(): void;
    checkButtonForMapping(): boolean;
    checkFieldMapping(): boolean;
    checkValueMapping(): boolean;
    checkEmptyToolList(): boolean;
    advanceViewAccordian(): void;
    advanceViewAccordianHide(): void;
}
/** @ngInject */
export declare function toolsInsightEditor($q: any, uiSegmentSrv: any): {
    restrict: string;
    scope: boolean;
    templateUrl: string;
    controller: typeof ToolsInsightEditorCtrl;
    controllerAs: string;
};
