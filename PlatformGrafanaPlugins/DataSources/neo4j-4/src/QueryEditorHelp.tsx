import React, { PureComponent } from 'react';

import { QueryEditorHelpProps } from '@grafana/data';
import { MyQuery } from './types';
import CheatSheet from './CheatSheet';

export default class QueryEditorHelp extends PureComponent<QueryEditorHelpProps<MyQuery>>{
    render() {
        return <CheatSheet onClickExample={this.props.onClickExample} />;
    }
};
