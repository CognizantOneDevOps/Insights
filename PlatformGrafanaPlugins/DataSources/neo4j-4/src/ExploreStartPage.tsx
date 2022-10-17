import React, { PureComponent } from 'react';
import { QueryEditorHelpProps } from '@grafana/data';


import CheatSheet from './CheatSheet';

export default class ExploreStartPage extends PureComponent<QueryEditorHelpProps> {
  render() {
    return <CheatSheet onClickExample={this.props.onClickExample} />;
  }
}
