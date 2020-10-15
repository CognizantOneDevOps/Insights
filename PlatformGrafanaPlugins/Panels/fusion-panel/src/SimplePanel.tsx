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
    this.props.options.datasource = this.props.data.request?.targets[0].datasource;
    rootID = 'insights-fusio1n-' + this.props.id;
    let data: any;
    data = fetchChartData(data, options, this.props, config);
    options.parentData = data;
    return (
      <div id={rootID} style={{ height: '100%' }} className={cx(`position:relative`, css`width: ${this.props.width}px;height: ${this.props.height}px;`)}>
        {this.processChartAfterScriptLoad(data, this.props)}
      </div>
    );
  }

  processChartAfterScriptLoad(data: any, props: any): React.ReactNode {
    let FusionCharts = (window as any).FusionCharts;
    const { theme } = config;
    const { options } = this.props;
    dynamicProps(options, theme, data, rootID, props, FusionCharts);
    return <div id={rootID} style={{ height: '100%' }} className={cx(`position:relative`, css`width: ${this.props.width}px;height: ${this.props.height}px;`)}></div>;
  }

};


