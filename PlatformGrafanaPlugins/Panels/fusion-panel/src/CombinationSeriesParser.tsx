

  export function formatChartType_MultiSeries_Stacked_Column2D_Line_Dual_Y_Axis(props: any): { categories: any, dataset: any, lineset: any } {
    let categories: any = new Array();
    let dataset: any = new Array();
    let lineset: any = new Array();
    let category: any = new Array();
    let data: any = new Array();
    if (props.data.state === 'Done') {
      let labelFlag: boolean = false;
      let array = props.data.series[0].fields;
      let counter = 0;
      let size = array.length;
      array.forEach((obj:any) => {
        let array = obj.values.buffer;
        array.forEach((val:any) => {
          /*First element of the array are lables operating using labelFlag*/
          if (!labelFlag) {
            category.push({ "label": val, })
          }
          else {
            data.push({ "value": val })
          }
        })
        if (labelFlag) {
          // when Counter value 0 label are fetched , when Counter value 1 column values are fetched         
          if (counter === size) {
            lineset.push({ "seriesname": obj.name, "data": data })
          }
          else {
            dataset.push({ "seriesname": obj.name, "data": data })
          }
        }
        labelFlag = true;
        counter++;
        data = new Array();
      });
      categories.push({ "category": category });
    }
    return { categories: categories, dataset: dataset, lineset: lineset };
  }

  export function formatChartType_Multi_Series_Stacked_Column_Single_Dual_Y_Axis(props: any): { categories: any, dataset: any, lineset: any } {

    let categories: any = new Array();
    let dataset: any = new Array();
    let category: any = new Array();
    let data: any = new Array();
    let lineset: any = new Array();
    if (props.data.state === 'Done') {
      let labelFlag: boolean = false;
      let array = props.data.series[0].fields;
      let counter = 0;
      array.forEach((obj:any) => {
        let array = obj.values.buffer;
        array.forEach((val:any) => {
          /*First element of the array are lables operating using labelFlag*/
          if (!labelFlag) {
            category.push({ "label": val, })
          }
          else {
            data.push({ "value": val })
          }
        })
        if (labelFlag) {
          // when Counter value 0 label are fetched , when Counter value 1 column values are fetched         
          if (counter === 3) {
            // For Dual Y-Axis
            if (props.options.charttype === 'stackedcolumn2dlinedy' || props.options.charttype === 'stackedcolumn3dlinedy'
              || props.options.charttype === 'mscolumn3dlinedy') {
              dataset.push({ "seriesname": obj.name, "renderAs": "line", "parentyaxis": "S", "data": data })
            }
            else // For single Y-Axis
            {
              dataset.push({ "seriesname": obj.name, "renderAs": "line", "data": data })
            }
          }
          else {
            dataset.push({ "seriesname": obj.name, "data": data })
          }
        }
        labelFlag = true;
        counter++;
        data = new Array();
      });
      categories.push({ "category": category });
    }
    return { categories: categories, dataset: dataset, lineset: lineset };

  }

  export function format_StackedArea_Line_Dual_Y_Axis(props: any) {
    let categories: any = new Array();
    let dataset: any = new Array();
    let category: any = new Array();
    let data: any = new Array();
    let lineset: any = new Array();
    if (props.data.state === 'Done') {
      let labelFlag: boolean = false;
      let array = props.data.series[0].fields;
      let counter = 0;
      let size = array.length;
      array.forEach((obj:any) => {
        let array = obj.values.buffer;
        array.forEach((val:any) => {
          /*First element of the array are lables operating using labelFlag*/
          if (!labelFlag) {
            category.push({ "label": val, })
          }
          else {
            data.push({ "value": val })
          }
        })
        if (labelFlag) {
          // when Counter value 0 label are fetched , when Counter value 1 column values are fetched         
          if (counter === size) {

            dataset.push({ "seriesname": obj.name, "renderAs": "line", "showanchors": "1", "parentYAxis": "S", "data": data })

          }
          else {
            dataset.push({ "seriesname": obj.name, "data": data })
          }
        }
        labelFlag = true;
        counter++;
        data = new Array();
      });
      categories.push({ "category": category });
    }
    return { categories: categories, dataset: dataset, lineset: lineset };
  }

  export function formatChartType_Multi_Series_Column_Line_Area_Single_Dual_Y_Axis(props: any): { categories: any, dataset: any, lineset: any } {
    let categories: any = new Array();
    let dataset: any = new Array();
    let category: any = new Array();
    let lineset: any = new Array();
    let data: any = new Array();
    if (props.data.state === 'Done') {
      let labelFlag: boolean = false;
      let array = props.data.series[0].fields;
      let counter = 0;
      array.forEach((obj:any) => {
        let array = obj.values.buffer;
        array.forEach((val:any) => {
          /*First element of the array are lables operating using labelFlag*/
          if (!labelFlag) {
            category.push({ "label": val, })
          }
          else {
            data.push({ "value": val })
          }
        })
        if (labelFlag) {
          // when Counter value 0 label are fetched , when Counter value 1 column values are fetched
          if (counter === 2) {
            if (props.options.charttype === 'mscombidy2d' || props.options.charttype === 'mscombidy3d') {
              dataset.push({ "seriesname": obj.name, "renderAs": "line", "parentyaxis": "S", "data": data })
            }
            else // For single Y-Axis
            {
              dataset.push({ "seriesname": obj.name, "renderAs": "line", "data": data })
            }
          }
          else if (counter === 3) {
            dataset.push({ "seriesname": obj.name, "renderAs": "area", "data": data })
          }
          else {
            dataset.push({ "seriesname": obj.name, "data": data })
          }
        }
        labelFlag = true;
        counter++;
        data = new Array();
      });
      categories.push({ "category": category });
    }
    return { categories: categories, dataset: dataset, lineset: lineset };

  }