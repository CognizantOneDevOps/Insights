import { DataQuery } from '@grafana/data';
export interface MyQuery extends DataQuery {
  queryText?: string;
  constant: number;
  cypherQuery: string;
  graph: any;
  timeseries: any;
  table: any;
  stats: any;
  cache: any;
  raw: any;
  varTime: any;
  fixTime: any;
  cacheType: any;
  cacheValue: any;
}
