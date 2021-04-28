import React, { PureComponent, ChangeEvent } from 'react';
//import { LegacyForms } from '@grafana/ui';
import { DataSourceHttpSettings, Field, Input, LegacyForms, Switch } from '@grafana/ui';
import { DataSourcePluginOptionsEditorProps } from '@grafana/data';
import { MyDataSourceOptions } from './types';

interface Props extends DataSourcePluginOptionsEditorProps<MyDataSourceOptions> { }

interface State { }

export class ConfigEditor extends PureComponent<Props, State> {

  onPathChange = (event: ChangeEvent<HTMLInputElement>) => {
    const { onOptionsChange, options } = this.props;
    const jsonData = {
      ...options.jsonData,
      path: event.target.value,
    };
    onOptionsChange({ ...options, jsonData });
  };

  // Secure field (only sent to th ebackend)
  onAPIKeyChange = (event: ChangeEvent<HTMLInputElement>) => {
    const { onOptionsChange, options } = this.props;
    onOptionsChange({
      ...options,
      secureJsonData: {
        apiKey: event.target.value,
      },
    });
  };

  onResetAPIKey = () => {
    const { onOptionsChange, options } = this.props;
    onOptionsChange({
      ...options,
      secureJsonFields: {
        ...options.secureJsonFields,
        apiKey: false,
      },
      secureJsonData: {
        ...options.secureJsonData,
        apiKey: '',
      },
    });
  };


  onServiceUrlChange = (event: ChangeEvent<HTMLInputElement>) => {
    const { onOptionsChange, options } = this.props;
    const jsonData = {
      ...options.jsonData,
      serviceUrl: event.target.value,
    };
    onOptionsChange({ ...options, jsonData });
  };

  onSwitched = (event: any) => {
    const { onOptionsChange, options } = this.props;
    const jsonData = {
      ...options.jsonData,
      logging: event.target.checked,
    };
    onOptionsChange({ ...options, jsonData });
}

  render() {
    console.log(this.props.options.jsonData);
    const { options, onOptionsChange } = this.props;

    return (
      <>
        <DataSourceHttpSettings
          defaultUrl="http://localhost:7474/db/data/transaction/commit?includeStats=true"
          dataSourceConfig={options}
          showAccessOptions={true}
          onChange={onOptionsChange}
        />
         <LegacyForms.Switch label="Enable Logging"  labelClass="width-8" checked={options.jsonData.logging} onChange={this.onSwitched} />
        {(options.jsonData.logging) &&<Field label="Service URL" description="This is required only when insights usage is collected.">
          <Input name="serviceUrl" onChange={this.onServiceUrlChange} value={options.jsonData.serviceUrl}/>
        </Field>}
      </>
    );
  }
  
}
