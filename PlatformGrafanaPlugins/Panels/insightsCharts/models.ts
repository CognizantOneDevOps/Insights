import { ColumnModel } from '../insightsCore/ChartModel';

export class InsightsChartEditorModel {
    constructor(public targets: InsightsChartTargetModel[],
        public transformInstrctions: string,
        public joinInstructions: string,
        public chartOptions: string) { };
}

export class InsightsChartTargetModel {
    constructor(public id: string, public columnModel: ColumnModel[]) { }
}
