import React, { useState, useEffect } from 'react';
import { PanelProps } from '@grafana/data';
import { SimpleOptions } from 'types';
import { PanelWizard } from 'grafana-plugin-support';
import { GoogleCharts } from './GoogleCharts';
import { Trend } from 'Trend';


interface Props extends PanelProps<SimpleOptions> { }

export const SimplePanel: React.FC<Props> = ({
  options,
  data,
  width,
  height,
  timeRange,
  onChangeTimeRange,
  timeZone,
  onOptionsChange,
  id
}) => {

  const [trends, setTrends] = useState([]);

  const usage = {
    schema: [
    ],
    url: '',
  };

  const frame = data.series[0];

  useEffect(() => {
    let dataArray: any = [];
    if (data && options.chartFields?.enableTrend && options.chartFields?.chartType === "LineChart") {
      data.series[0].fields.forEach((field) => {
        if (field.type === "number") {
          let currentValue = field.values.get(field.values.length - 1);
          let previousValue = field.values.length > 1 ? field.values.get(field.values.length - 2) : currentValue;
          let trend = {
            name: field.name,
            difference: ((currentValue - previousValue) / previousValue) * 100
          }
          dataArray.push(trend)
        }
      });
    }

    setTrends(dataArray);

  }, [data, options]);


  if (!frame) {
    return (
      <div style={{ width, height }}>
        <PanelWizard {...usage} />
      </div>
    );
  }
  let rootID = "insights_" + id;
  const queryString = window.location.search;
  const urlParams = new URLSearchParams(queryString);
  const editMode = urlParams.has('editPanel');



  return (
    <>
      <div style={{
            display : "flex",
            flexDirection : "row",
            gap : "20px",
            position : "absolute",
            top : "-2px",
            zIndex: 100,
            fontWeight : '500',
        }}>
        {options.chartFields?.enableTrend && options.chartFields?.chartType === "LineChart" && trends.map((trend: any) => <Trend key={trend.name} trend={trend} />)}
      </div>
      <GoogleCharts chartType={options.chartFields?.chartType}
        chartOptions={options.chartFields?.chartOptions}
        transformDataInstruction={options.chartFields?.transformDataInstruction}
        joinInstructions={options.chartFields?.joinInstructions}
        container={options.chartFields?.container} rootId={rootID} data={data} isEdit={editMode} columnModel={options.chartFields.columnModel}></GoogleCharts>
    </>
  );
};
