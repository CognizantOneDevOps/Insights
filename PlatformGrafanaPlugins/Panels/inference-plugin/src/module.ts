import { PanelPlugin } from '@grafana/data';
import { SimpleOptions } from './types';
import { InferencePanel } from './InferencePanel';

export const plugin = new PanelPlugin<SimpleOptions>(InferencePanel).setPanelOptions(builder => {
  return builder
    /*.addTextInput({
      path: 'text',
      name: 'Simple text option',
      description: 'Description of panel option',
      defaultValue: 'Default value of text input option',
    })*/
    .addBooleanSwitch({
      path: 'enableFusion',
      name: 'Enable Fusion Chart',
      defaultValue: false,
    })
    .addRadio({
      path: 'fusionChartType',
      defaultValue: 'column2d',
      name: 'Fusion Chart Type',
      settings: {
        options: [
          { value: 'column2d', label: 'Column' },
          { value: 'bar2d', label: 'Bar' },
          { value: 'line', label: 'Line' },
          { value: 'area2d', label: 'Area' },
          { value: 'pie2d', label: 'Pie' },
          { value: 'doughnut2d', label: ' Doughnut' },
          { value: 'pareto2d', label: 'Pareto' },
        ],
      },
      showIf: config => config.enableFusion,
    })
    .addRadio({
      path: 'googleChartType',
      defaultValue: 'line',
      name: 'Google Chart Type',
      settings: {
        options: [
          { value: 'line', label: 'Line' },
          { value: 'bar', label: 'Bar' }
        ],
      },
      showIf: config => !config.enableFusion,
    })
    ;
});

