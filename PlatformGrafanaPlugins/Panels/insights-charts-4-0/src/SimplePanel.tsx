import React from 'react';
import { PanelProps } from '@grafana/data';
import { SimpleOptions } from 'types';
import { PanelWizard } from 'grafana-plugin-support';
import { GoogleCharts } from './GoogleCharts';


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
  const usage = {
    schema: [
    ],
    url: '',
  };

  const frame = data.series[0];

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
    <GoogleCharts chartType={options.chartFields?.chartType}
      chartOptions={options.chartFields?.chartOptions}
      transformDataInstruction={options.chartFields?.transformDataInstruction}
      joinInstructions={options.chartFields?.joinInstructions}
      container={options.chartFields?.container} rootId={rootID} data={data} isEdit={editMode} columnModel={options.chartFields.columnModel}></GoogleCharts>
  );
};
