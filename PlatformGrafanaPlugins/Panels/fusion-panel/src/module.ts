import { PanelPlugin } from '@grafana/data';
import { FusionPanel } from './SimplePanel';
import { FusionEditor } from './FusionEditor';
import { defaults, ChartOptions } from 'ChartOptions';

export const plugin = new PanelPlugin<ChartOptions>(FusionPanel).setDefaults(defaults).setEditor(FusionEditor);
