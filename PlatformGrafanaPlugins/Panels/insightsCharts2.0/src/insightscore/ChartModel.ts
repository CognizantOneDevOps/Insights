export class ChartModel {
    constructor(public chartType: string,
        public chartOptions: any,
        public dataArray: ChartData[],
        public container: ContainerModel,
        public transformDataInstruction: string,
        public joinInstructions: string
    ) {
    }
}

export class ColumnModel {
    constructor(public name: string, public type?: string) {
    }
}

export class ContainerModel {
    constructor(public id: string, public height: number) {
    }
}

export class ChartData {
    constructor(public id: string, public data: any, public columns: ColumnModel[]) {
    }
}
