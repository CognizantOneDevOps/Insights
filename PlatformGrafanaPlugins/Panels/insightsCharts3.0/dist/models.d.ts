import { ColumnModel } from './insightscore/ChartModel';
export declare class InsightsChartEditorModel {
    targets: InsightsChartTargetModel[];
    transformInstrctions: string;
    joinInstructions: string;
    chartOptions: string;
    constructor(targets: InsightsChartTargetModel[], transformInstrctions: string, joinInstructions: string, chartOptions: string);
}
export declare class InsightsChartTargetModel {
    id: string;
    columnModel: ColumnModel[];
    constructor(id: string, columnModel: ColumnModel[]);
}
