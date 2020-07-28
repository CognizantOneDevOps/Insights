export declare class ChartModel {
    chartType: string;
    chartOptions: any;
    dataArray: ChartData[];
    container: ContainerModel;
    transformDataInstruction: string;
    joinInstructions: string;
    constructor(chartType: string, chartOptions: any, dataArray: ChartData[], container: ContainerModel, transformDataInstruction: string, joinInstructions: string);
}
export declare class ColumnModel {
    name: string;
    type: string;
    constructor(name: string, type?: string);
}
export declare class ContainerModel {
    id: string;
    height: number;
    constructor(id: string, height: number);
}
export declare class ChartData {
    id: string;
    data: any;
    columns: ColumnModel[];
    constructor(id: string, data: any, columns: ColumnModel[]);
}
