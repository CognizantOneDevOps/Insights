import { DataSourcePlugin } from '@grafana/data';
import { DataSource } from './DataSource';
import { ConfigEditor } from './ConfigEditor';
import { QueryEditor } from './QueryEditor';
//import { AnnotationsQueryCtrl } from './AnnotationsQueryCtrl';
import { AnnotationQueryEditor } from './AnnotationQueryEditor';
import ExploreQueryEditor from './ExploreQueryEditor';
import ExploreStartPage from './ExploreStartPage';

import { MyDataSourceOptions } from './types';
import { MyQuery } from "./MyQuery";

export const plugin = new DataSourcePlugin<DataSource, MyQuery, MyDataSourceOptions>(DataSource)
  .setConfigEditor(ConfigEditor)
  .setQueryEditor(QueryEditor)
  .setAnnotationQueryCtrl(AnnotationQueryEditor)
  .setExploreQueryField(ExploreQueryEditor)
  .setExploreStartPage(ExploreStartPage);