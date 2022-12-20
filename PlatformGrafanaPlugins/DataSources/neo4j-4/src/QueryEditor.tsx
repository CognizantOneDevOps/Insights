import defaults from 'lodash/defaults';

import React, { PureComponent, ChangeEvent } from 'react';
import { QueryEditorProps, SelectableValue } from '@grafana/data';
import { DataSource } from './DataSource';
import { MyDataSourceOptions, defaultQuery } from './types';
import { MyQuery } from "./MyQuery";
import { LegacyForms, Select } from '@grafana/ui';

type Props = QueryEditorProps<DataSource, MyQuery, MyDataSourceOptions>;

interface State {

}

const cacheTypes: Array<SelectableValue> = [
  { value: 'Fixed Time', label: 'Fixed Time' },
  { value: 'Variance Time', label: 'Variance Time' }
];

const fixedTime: Array<SelectableValue> = [
  { value: '1', label: '1' },
  { value: '2', label: '2' },
  { value: '3', label: '3' }
];

const varianeTime: Array<SelectableValue> = [
  { value: '5', label: '5' },
  { value: '10', label: '10' },
  { value: '15', label: '15' }
];

export class QueryEditor extends PureComponent<Props, State> {

  state = {
    graph: false,
    timeseries: false,
    table: false,
    stats: false,
    cache: false,
    raw: false,
    fixTime: false,
    varTime: false,
    cacheType: '',
    cacheValue: ''
  }

  componentDidMount() {
    this.setState(this.props.query);
  }

  onQueryTextChange = (event: ChangeEvent<HTMLTextAreaElement>) => {
    const { onChange, query, onRunQuery } = this.props;
    onChange({ ...query, queryText: event.target.value });
    onRunQuery();
  };

  onConstantChange = (event: ChangeEvent<HTMLInputElement>) => {
    const { onChange, query, onRunQuery } = this.props;
    onChange({ ...query, constant: parseFloat(event.target.value) });
    onRunQuery(); // executes the query
  };

  onChanged(type: string, e: React.SyntheticEvent<HTMLInputElement, Event> | undefined): void {
    //const { query } = this.props;
    //throw new Error("Method not implemented.");
    let obj = {};
    if (type === 'cache') {
      obj['cache'] = !this.props.query[type];
      this.props.query['cache'] = !this.props.query[type];
    } else {
      Object.keys(this.props.query).forEach(c => {
        if (c == type) {
          obj[type] = !this.props.query[type];
          this.props.query[type] = !this.props.query[type];
        } else if (c == 'constant' || c == 'queryText' || c == 'cypherQuery' || c == 'refId') {
          // do nothing
        }
        else {
          if (c !== 'cache') {
            obj[c] = false;
            this.props.query[c] = false;
          }
        }
      })
    }
    this.setState(obj);
  }

  onCacheChanged = ({ value }: any) => {
    if (value === 'Fixed Time' && this.props.query["cache"]) {
      this.props.query["fixTime"] = true;
      this.props.query["varTime"] = false;
      this.props.query["cacheType"] = 'Fixed Time';
      this.setState({ fixTime: true, varTime: false, cacheType: 'Fixed Time'});
    } else if (value === 'Variance Time' && this.props.query["cache"]) {
      this.props.query["varTime"] = true;
      this.props.query["fixTime"] = false;
      this.props.query["cacheType"] = 'Variance Time';
      this.setState({ varTime: true, fixTime: false, cacheType: 'Variance Time'});
    }
  }

  onVarianceChanged(e: SelectableValue<any>): void | {} {
    this.props.query["cacheValue"] = e.value;
    this.setState({ cacheValue: e.value });
    
  }
  onFixChanged(e: SelectableValue<any>): void | {} {
   // const params = this.props.query;
    this.props.query["cacheValue"] = e.value;
    this.setState({ cacheValue: e.value });
    
  }

  render() {
    const query = defaults(this.props.query, defaultQuery);
    const { queryText } = query;

    return (
      <>
        <div className="gf-form-inline">
          <div className="gf-form gf-form--v-stretch">
            <label className="gf-form-label width-5">Cypher</label>
          </div>
          <div className="gf-form gf-form--grow">
            <textarea
              rows={5}
              className="gf-form-input width-100"
              onChange={this.onQueryTextChange}
              placeholder="Example: match (n) return n limit 1"
              value={queryText}
              required
            />
          </div>
          {/*<LegacyForms.FormField width={4} value={constant} onChange={this.onConstantChange} label="Constant" type="number" step="0.1"></LegacyForms.FormField>*/}
        </div>
        <div className="gf-form-inline">
          <div className="gf-form gf-form--v-stretch">
            <label className="gf-form-label width-5">Type</label>
          </div>
          <LegacyForms.Switch label="Graph" labelClass="width-5" checked={this.state.graph} onChange={(e) => this.onChanged('graph', e)} />
          {/*<LegacyForms.Switch label="TimeSeries" labelClass="width-7" checked={this.state.timeseries} onChange={(e) => this.onChanged('timeseries', e)} />*/}
          <LegacyForms.Switch label="Table" labelClass="width-5" checked={this.state.table} onChange={(e) => this.onChanged('table', e)} />
          {/*<LegacyForms.Switch label="Stats" labelClass="width-5" checked={this.state.stats} onChange={(e) => this.onChanged('stats', e)} />*/}
          <LegacyForms.Switch label="Cache" labelClass="width-5" checked={this.state.cache} onChange={(e) => this.onChanged('cache', e)} />
          {/*<LegacyForms.Switch label="Neo4j" labelClass="width-5" checked={this.state.raw} onChange={(e) => this.onChanged('raw', e)} />*/}

        </div >
        <div className="gf-form-inline">

          {this.props.query.cache &&
            (<div className="gf-form gf-form--v-stretch">
                 <label className="gf-form-label width-5">Cache Type</label>
              <Select
                width={20}
                options={cacheTypes}
                onChange={(e) => this.onCacheChanged(e)}
                value={cacheTypes.find(item => item.value === this.state.cacheType)}></Select>
            </div>)
          }
          {this.state.fixTime && this.props.query.cache &&
            (<div className="gf-form gf-form--v-stretch">
            <label className="gf-form-label width-10">Duration (Hours)</label>
              <Select
                width={10}
                options={fixedTime}
                onChange={(e) => this.onFixChanged(e)}
                value={fixedTime.find(item => item.value === this.state.cacheValue)}></Select>
            </div>)}
          {this.state.varTime && this.props.query.cache &&
            (<div className="gf-form gf-form--v-stretch">
            <label className="gf-form-label width-10">Variance (%)</label>
              <Select
                width={10}
                options={varianeTime}
                onChange={(e) => this.onVarianceChanged(e)}
                value={varianeTime.find(item => item.value === this.state.cacheValue)}></Select>
            </div>)}
        </div>
        {this.state.fixTime && this.props.query.cache &&
          (<div><label className="gf-form-label width-50">Please note that when Fixed Time is selected, the time selector would be disabled for selected fixed time interval.</label></div>)}
      </>
    );
  }


}
