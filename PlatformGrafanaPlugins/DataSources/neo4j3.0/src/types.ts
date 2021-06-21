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
  constant: 6.5,
  graph: false,
  timeseries: false,
  table: false,
  stats: false,
  cache: false,
  raw: false,
  varTime: false,
  fixTime: false,
  cacheType: undefined,
  cacheValue: undefined,
  serviceUrl: undefined
};
 

/**
 * These are options configured for each DataSource instance
 */
export interface MyDataSourceOptions extends DataSourceJsonData {
  basicPassword: string;
  logging: any ;
  path?: string;
  serviceUrl?: any;
  authToken: any;
}

/**
 * Value that is used in the backend, but never sent over HTTP to the frontend
 */
export interface MySecureJsonData {
  basicPassword: string;
  apiKey?: string;
}

export const defaults: MyDataSourceOptions = {
  logging :true,
  path: '',
  serviceUrl : 'http://localhost:8080/PlatformService/datasource/logDashboardInfo',
  authToken : true,
  basicPassword: ''
}
