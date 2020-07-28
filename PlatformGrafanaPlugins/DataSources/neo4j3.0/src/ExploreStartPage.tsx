import React from 'react';
import { ExploreStartPageProps, DataQuery } from '@grafana/data';

const examples = [
  {
    title: 'Fetch top 25 nodes of JIRA',
    expression: 'MATCH (n:JIRA) RETURN n LIMIT 25',
    label: 'TOP JIRA NODES',
  }
];

export default (props: ExploreStartPageProps) => {
  return (
    <div>
      <h2>Cheat Sheet</h2>
      {examples.map((item, index) => (
        <div className="cheat-sheet-item" key={index}>
          <div className="cheat-sheet-item__title">{item.title}</div>
          {item.expression ? (
            <div
              className="cheat-sheet-item__example"
              onClick={e => props.onClickExample({ refId: 'A', queryText: item.expression } as DataQuery)}
            >
              <code>{item.expression}</code>
            </div>
          ) : null}
          <div className="cheat-sheet-item__label">{item.label}</div>
        </div>
      ))}
    </div>
  );
};