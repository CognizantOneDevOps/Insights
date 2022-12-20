import { DataSourceJsonData } from '@grafana/data';
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
  serviceUrl:any;
}


export const defaultQuery: Partial<MyQuery> = {
  queryText: undefined,
  constant: 6.5,
  cypherQuery: undefined,
  graph: false,
  timeseries: false,
  table: false,
  stats: false,
  cache: false,
  raw: false,
  varTime: false,
  fixTime: false,
  cacheType: undefined,
  cacheValue: undefined
};
 

/**
 * These are options configured for each DataSource instance
 */
export interface MyDataSourceOptions extends DataSourceJsonData {
  path?: string;
  basicPassword: string;
  logging: any ;
  serviceUrl: any;
  authToken: any;
}

/**
 * Value that is used in the backend, but never sent over HTTP to the frontend
 */
export interface MySecureJsonData {
  apiKey?: string;
  basicPassword: string;
}

/** changes*/
export const defaults: MyDataSourceOptions = {
  logging :true,
  path: '',
  serviceUrl : '',
  authToken : true,
  basicPassword: ''
}
