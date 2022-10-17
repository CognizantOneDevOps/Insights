import React from 'react';

import { QueryEditorProps } from '@grafana/data';
import { QueryField } from '@grafana/ui';
import { DataSource } from './DataSource';
import { MyQuery, MyDataSourceOptions } from './types';

export type Props = QueryEditorProps<DataSource, MyQuery, MyDataSourceOptions>;

export default (props: Props) => {
  //console.log(props);
  const { query } = props;

  const onQueryChange = (value: string, override?: boolean) => {
    const { query, onChange, onRunQuery } = props;
    //console.log(value);

    if (onChange) {
      // Update the query whenever the query field changes.
      onChange({ ...query, queryText: value });

      // Run the query on Enter.
      if (override && onRunQuery) {
        onRunQuery();
      }
    }
  };

  return (
    <QueryField
      portalOrigin="mock-origin"
      onChange={onQueryChange}
      onRunQuery={props.onRunQuery}
      onBlur={props.onBlur}
      query={query.queryText || ''}
      placeholder="Enter a query"
    />
  );
};
