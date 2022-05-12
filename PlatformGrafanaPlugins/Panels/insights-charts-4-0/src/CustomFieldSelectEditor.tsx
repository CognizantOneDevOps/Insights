import React, { FormEvent, useState, useEffect } from "react";
import { StandardEditorProps, FieldType, SelectableValue, PanelModel } from "@grafana/data";
import { Select, TextArea, Button, useTheme2 } from "@grafana/ui";
import jquery from 'jquery';
import _ from 'lodash';
import { ColumnModel, ChartData } from 'models/ChartModel';
import { InsightsChartTargetModel } from 'models/InsightsChartEditorModel';
import { googlechartutilities } from "GoogleChartUtilities";

interface Settings {
  filterByType: FieldType[];
  multi: boolean;
}

interface ChartModelMapping {
  chartType: string,
  chartOptions: any,
  columnModel: ColumnModel[];
  transformDataInstruction: string;
  joinInstructions: string;
  dataArray: ChartData[];
  panel: PanelModel;
}

interface Props
  extends StandardEditorProps<ChartModelMapping, Settings> {
}

export const CustomFieldSelectEditor: React.FC<Props> = ({
  item,
  value,
  onChange,
  context
}) => {
  const theme = useTheme2();

  let chartEditor: any;
  let google = (window as any).google

  const [dataTransform, setDataTransform] = useState<string>("");
  const [dataJoin, setDataJoin] = useState<string>("");
  const [chartOptions, setChartOptions] = useState<string>("");
  const [chartType, setChartType] = useState<string>("");
  const [columnModel, setColumnModel] = useState<ColumnModel[]>([]);
  const [chartDataArray, setChartDataArray] = useState<ChartData[]>([]);

  useEffect(() => {
    if (context.options.hasOwnProperty("chartFields")) {
      setChartValue();
      for (let dt of context.data) {
        let refId: string = dt.refId || "";
        let columnModelArr: ColumnModel[] = new Array();
        for (let field of dt.fields) {
          let columnModelObj: ColumnModel;
          columnModelObj = columnModel.find(o => o.name === field.name && o.refId === refId)!;
          columnModelArr.push(columnModelObj);
        }
        let chartDataObjIndx = chartDataArray.findIndex(obj => obj.id === refId);
        if (chartDataObjIndx != -1) {
          chartDataArray[chartDataObjIndx].data = context.data;
          chartDataArray[chartDataObjIndx].columns = columnModelArr
        } else {
          chartDataArray.push(new ChartData(refId, context.data, columnModelArr));
        }
      }
      setChartDataArray(chartDataArray);
    }

    function setChartValue() {
      if ('chartOptions' in context.options.chartFields) {
        setChartOptions(context.options.chartFields.chartOptions);
      }
      if ('chartType' in context.options.chartFields) {
        setChartType(context.options.chartFields.chartType);
      }
      if ('joinInstructions' in context.options.chartFields) {
        setDataJoin(context.options.chartFields.joinInstructions);
      }
      if ('transformDataInstruction' in context.options.chartFields) {
        setDataTransform(context.options.chartFields.transformDataInstruction);
      }
      if ('columnModel' in context.options.chartFields) {
        setColumnModel(context.options.chartFields.columnModel);
      }
      else {
        setColumnModel([new ColumnModel("", "")]);
      }
    }
  }, [])

  const onDataTransformChange = (index: number) => (event: FormEvent<HTMLTextAreaElement>,) => {
    setDataTransform(event.currentTarget.value);
    onChange({ ...value, transformDataInstruction: event.currentTarget.value });
  };

  const onDataJoinChange = (index: number) => (event: FormEvent<HTMLTextAreaElement>) => {
    setDataJoin(event.currentTarget.value);
    onChange({ ...value, joinInstructions: event.currentTarget.value });
  };

  const onChartOptionsChange = (index: number) => (event: FormEvent<HTMLTextAreaElement>) => {
    setChartOptions(event.currentTarget.value);
    onChange({ ...value, chartOptions: event.currentTarget.value });
  };

  const onColumnModelChange = (columnName: string, refId: string) => (option: SelectableValue<string>) => {
    const i = columnModel.findIndex(element => element.name === columnName && element.refId === refId);
    if (i > -1) columnModel[i].type = option.value;
    else {
      columnModel.push(new ColumnModel(columnName, option.value, refId))
    }
    let filterColModel: any = []
    for (let dt of context.data) {
      for (let field of dt.fields) {
        filterColModel.push({ "refId": dt.refId, "label": field.name })
      }
    }

    let filteredList = columnModel.filter((el) => {
      return filterColModel.some((f: any) => {
        return f.refId === el.refId && f.label === el.name;
      });
    })
    setColumnModel(filteredList)
    onChange({ ...value, columnModel: filteredList });

  };

  const { buildDataTables, joinDataTables, transformData, innerDimensions, applyTheme } = googlechartutilities(theme, dataTransform, dataJoin, google, true);

  const renderChart = (isEditChart: boolean) => {
    if (context.data && context.data.length > 0) {
      let containerElem = document.getElementById("googleId");
      if (containerElem && google && google.charts) {
        if (isEditChart) {
          google.charts.load('46', { 'packages': ['corechart', 'charteditor'] });
          google.charts.setOnLoadCallback(executeEditChart());
        } else {
          if (chartOptions) {
            google.charts.load('46', { 'packages': ['corechart'] });
          }
        }
      } else {
        setTimeout(function () {
          renderChart(isEditChart);
        }, 50);
      }
    }
  }

  const saveChartOpts = () => {
    let opts = (document.getElementById("chartsOptId") as HTMLTextAreaElement).value;
    setChartOptions(opts);
    onChange({ ...value, chartOptions: opts });
  }

  const executeEditChart = () => {

    let data = buildChartData();
    let { defchartOptions, contId, contIdView }: { defchartOptions: any; contId: string; contIdView: string; } = buildChartOptions();
    var wrapper = new google.visualization.ChartWrapper({
      'chartType': chartType,
      'dataTable': data,
      'options': defchartOptions
    });
    chartEditor = new google.visualization.ChartEditor();
    let redrawChart = function () {

      let chartWrapperTemp = chartEditor.getChartWrapper();
      let editorChartOptions = chartWrapperTemp.getOptions();
      let containerDimTemp = innerDimensions(document.getElementById(contId)?.parentNode?.parentNode);
      let tempOptions = chartWrapperTemp.getOptions();
      tempOptions["height"] = containerDimTemp.height;
      tempOptions["width"] = containerDimTemp.width;
      tempOptions = _.defaults(tempOptions, editorChartOptions);
      setChartOptions(JSON.stringify(tempOptions));
      onChange({ ...value, chartOptions: JSON.stringify(tempOptions) });
      setChartType(chartWrapperTemp.getChartType());
      onChange({ ...value, chartType: chartWrapperTemp.getChartType() });
      setTimeout(function () {
        chartWrapperTemp.draw(document.getElementById(contId), tempOptions);
      }, 2000
      );

      var viewElement = document.getElementById(contIdView);
      if (viewElement !== null) {
        let chartWrapperview = chartEditor.getChartWrapper();
        let containerDimView = innerDimensions(document.getElementById(contId)?.parentNode?.parentNode);
        let tempViewOptions = tempOptions
        tempViewOptions["height"] = containerDimView.height;
        tempViewOptions["width"] = containerDimView.width;
        setChartOptions(JSON.stringify(tempViewOptions));
        onChange({ ...value, chartOptions: JSON.stringify(tempViewOptions) });
        setChartType(chartWrapperview.getChartType());
        onChange({ ...value, chartType: chartWrapperview.getChartType() });
        chartWrapperview.draw(viewElement, tempViewOptions);

      }
      chartEditor.openDialog(chartEditor.getChartWrapper(), {});
      appendChartContainer();
    };

    chartEditor.openDialog(wrapper, {});
    appendChartContainer();
    google.visualization.events.addListener(chartEditor, 'ok', redrawChart);
    google.visualization.events.addListener(chartEditor, 'cancel', () => { jquery('#googleId').empty(); });

    function buildChartOptions() {
      if (chartType === null || chartType === undefined) {
        setChartType('PieChart');
      }
      let defchartOptions: any;
      if (chartOptions === undefined || chartOptions === "") {
        defchartOptions = {};
      } else {
        defchartOptions = JSON.parse(chartOptions);
      }
      let search = window.location.search;
      const searchParams = new URLSearchParams(search);
      let panelId = searchParams.get("editPanel");
      let contId = "containerIdDivEdit_insights_" + panelId;
      let contIdView = "containerIdDiv_insights_" + panelId;
      let containerDim = innerDimensions(document.getElementById(contId)?.parentNode?.parentNode);
      defchartOptions["height"] = containerDim.height;
      defchartOptions["width"] = containerDim.width;
      defchartOptions = applyTheme(defchartOptions);
      setChartOptions(JSON.stringify(defchartOptions));
      return { defchartOptions, contId, contIdView };
    }

    function buildChartData() {
      let targetModelArr: InsightsChartTargetModel[] = new Array();

      for (let dt of context.data) {

        let refId: string = dt.refId || "";
        let columnModelArr: ColumnModel[] = new Array();
        for (let field of dt.fields) {

          let columnModelObj: ColumnModel;
          columnModelObj = columnModel.find(o => o.name === field.name && o.refId === refId)!;
          columnModelArr.push(columnModelObj);
        }
        let chartDataObjIndx = chartDataArray.findIndex(obj => obj.id === refId);
        if (chartDataObjIndx != -1) {
          chartDataArray[chartDataObjIndx].data = context.data;
          chartDataArray[chartDataObjIndx].columns = columnModelArr
        } else {
          chartDataArray.push(new ChartData(refId, context.data, columnModelArr));
        }

        let targetModel = new InsightsChartTargetModel(refId, columnModelArr);
        targetModelArr.push(targetModel);
      }
      setChartDataArray(chartDataArray);
      onChange({ ...value, dataArray: chartDataArray });
      let datatab = buildDataTables(chartDataArray);
      let data = joinDataTables(datatab);
      data = transformData(data);
      return data;
    }
  }

  const appendChartContainer = () => {
    var dialog = jquery('.google-visualization-charteditor-dialog');
    if (dialog.length === 0) {
      setTimeout(function () {
        appendChartContainer();
      }, 50);
    } else {
      jquery('#googleId').empty();
      dialog.children().each(function () {
        jquery('#googleId').append(this);
      });
      jquery(".modal-dialog-title").css({ "background-color": theme.colors.background.primary, "color": theme.colors.text.primary });
      jquery(".modal-dialog-content").css({ "background-color": theme.colors.background.primary, "color": theme.colors.text.primary });
      dialog.hide();
    }
  }
  if (context.data && context.data.length > 0) {

    const selectOptions = [
      { label: 'string', value: 'string' },
      { label: 'number', value: 'number' },
      { label: 'boolean', value: 'boolean' },
      { label: 'date', value: 'date' },
      { label: 'datetime', value: 'datetime' },
      { label: 'timeofday', value: 'timeofday' }
    ];
    const css = `      
      .google-visualization-charteditor-preview-td{
        position: relative !important;
        top: 0px;
        left: 0px
      }
      #google-visualization-charteditor-panel-navigate-div{
        width: 350px 
      }
      .marlft{
        margin-left:20px
      }
      `
    if (context.data) {
      const itemrows = []
      for (let dt of context.data) {
        const refId = (<div>{dt.refId}</div>);
        const fieldItems = dt.fields.map((field) =>
          <div>
            <span>{field.name}</span>
            <Select isLoading={false} value={columnModel.find(obj => obj.name == field.name && obj.refId === dt.refId)?.type} allowCustomValue onChange={onColumnModelChange(field.name, dt.refId || "")} options={selectOptions} />
          </div>
        );
        itemrows.push(refId);
        itemrows.push(fieldItems);
      }
      return (
        <div>
          <style>
            {css}
          </style>
          <div>
            {itemrows}
          </div>
          <div style={{ paddingBottom: "20px;" }}>
            <label>Data Transformation Instructions</label>
            <TextArea placeholder="Data Transformation Instructions" cols={10} value={dataTransform}
              onChange={onDataTransformChange(1)} name="dataTransform" ></TextArea>
          </div>
          <div style={{ paddingBottom: "20px;" }}>
            <label>Data Join Instructions</label>
            <TextArea placeholder="Data Join Instructions" cols={10} value={dataJoin}
              onChange={onDataJoinChange(1)} ></TextArea>
          </div>
          <div style={{ paddingBottom: "20px;" }}>
            <label>Chart Option</label>
            <TextArea placeholder="Chart Options" cols={10} value={chartOptions}
              onChange={onChartOptionsChange(1)} id="chartsOptId"></TextArea>
          </div>
          <div>
            <Button
              size="md"
              onClick={() => {
                renderChart(true);
              }}
            >
              Load Chart
            </Button>
            <Button className="marlft"
              size="md"
              onClick={() => {
                saveChartOpts();
              }}
            >
              Save Chart
            </Button>
          </div>
          <div>

          </div>

          <div id="chartEditorContainer"></div>
          <div id="googleId"></div>
        </div>
      );
    }

  }

  return <Select onChange={() => { }} disabled={true} />;
};
