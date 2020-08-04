import React from 'react';
import { PanelProps } from '@grafana/data';
import { ChartOptions } from './ChartOptions';
import { css, cx } from 'emotion';
import { config } from '@grafana/runtime';

import { dynamicProps, fetchChartData } from './SingleSeriesParser';

interface Props extends PanelProps<ChartOptions> { }

let rootID: string | undefined;

export class FusionPanel extends React.Component<Props>{

  render() {
    const { options } = this.props;
    rootID = 'insights-fusion-' + this.props.id;
    let data: any;
    //console.log('ChartType---', options.charttype);
    data = fetchChartData(data, options,this.props);
    return (
      <div id={rootID} style={{ height: '100%' }} className={cx(`position:relative`, css`width: ${this.props.width}px;height: ${this.props.height}px;`)}>
        {this.processChartAfterScriptLoad(data)}
      </div>
    );
  }


  processChartAfterScriptLoad(data: any): React.ReactNode {
    let FusionCharts = (window as any).FusionCharts;
    const { theme } = config;
    const { options } = this.props;
    const chartConfig = dynamicProps(options, theme, data, rootID);
    FusionCharts.ready(function () {
      var fusioncharts = new FusionCharts(chartConfig);
      fusioncharts.resizeTo('100%', '100%');
      fusioncharts.render();
    });

    return <div id={rootID} style={{ height: '100%' }} className={cx(`position:relative`, css`width: ${this.props.width}px;height: ${this.props.height}px;`)}></div>;
  }
};


