export function formatMultiSeriesChartType(props: any,options:any,config:any): { categories: any, dataset: any } {
    let categories: any = new Array();
    let dataset: any = new Array();
    let category: any = new Array();
    let data: any = new Array();
    if (props.data.state === 'Done') {
        let labelFlag: boolean = false;
        let array = props.data.series[0].fields;
        array.forEach((obj: any) => {
            let array = obj.values.buffer;
            array.forEach((val: any) => {
                /*First element of the array are lables operating using labelFlag*/
                if (!labelFlag) {
                    category.push({ "label": val })
                }
                else {
                    data.push({ "value": val })
                }
            })
            if (labelFlag) {
                dataset.push({ "seriesname": obj.name, "data": data })
            }
            labelFlag = true;
            data = new Array();
        });
        categories.push({ "category": category });
    }
    return { categories: categories, dataset: dataset };

}

export function formatZoomlineTypeChart(props: any): { categories: any, dataset: any } {
    // this.state = { categories: [], dataset: [] };
    let categories: any = new Array();
    let dataset: any = new Array();
    if (props.data.state === 'Done') {
        let labelFlag: boolean = false;
        let array = props.data.series[0].fields;
        let data: any = ''
        let category: any = ''
        array.forEach((obj: any) => {
            let array = obj.values.buffer;
            array.forEach((val: any) => {
                /*First element of the array are lables operating using labelFlag*/
                if (!labelFlag) {
                    category = val + '|' + category;
                }
                else {
                    data = val + '|' + data;
                }
            })
            if (labelFlag) {
                dataset.push({ "seriesname": obj.name, "data": data })
            }
            labelFlag = true;
            data = new Array();
        });
        categories.push({ "category": category });
    }
    return { categories: categories, dataset: dataset }
}