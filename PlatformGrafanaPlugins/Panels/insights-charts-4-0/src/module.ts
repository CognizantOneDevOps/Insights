import { SimpleOptions } from './types';
import { SimplePanel } from './SimplePanel';
import {  PanelPlugin } from '@grafana/data';
import { CustomFieldSelectEditor } from './CustomFieldSelectEditor'

export const plugin = new PanelPlugin<SimpleOptions>(SimplePanel).setPanelOptions(builder => {
  return builder  
    .addCustomEditor({
      id: 'chartFields',
      path: 'chartFields',
      name: 'ChartFields',
      description: 'Fields required for Insights Charts',
      editor: CustomFieldSelectEditor     
    })
    ;
});
