import React, { PureComponent, ChangeEvent } from 'react';
import { DataSourceHttpSettings, Field, Input, LegacyForms } from '@grafana/ui';
import { DataSourcePluginOptionsEditorProps } from '@grafana/data';
import { MyDataSourceOptions, MySecureJsonData } from './types';

//const { SecretFormField, FormField } = LegacyForms;

interface Props extends DataSourcePluginOptionsEditorProps<MyDataSourceOptions> { }

interface State { }

let logapi = window.location.protocol+`//localhost:8080/PlatformService/externalApi/Neo4jPluginLog/logDashboardInfo`
    if(document.domain  !== 'localhost') {
      logapi = window.location.protocol+`//`+document.domain+`/PlatformService/externalApi/Neo4jPluginLog/logDashboardInfo`;
    }
export class ConfigEditor extends PureComponent<Props, State> {
  onPathChange = (event: ChangeEvent<HTMLInputElement>) => {
    const { onOptionsChange, options } = this.props;
    const jsonData = {
      ...options.jsonData,
      path: event.target.value,
    };
    onOptionsChange({ ...options, jsonData });
  };

  // Secure field (only sent to the backend)
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

onBasicPwdChange = (event: ChangeEvent<HTMLInputElement>) => {
  const { onOptionsChange, options } = this.props;
  const jsonData = {
    ...options.jsonData,
    basicPassword: event.target.value,
  };
  onOptionsChange({ ...options, jsonData });
};

onAuthEnable = (event: any) => {
  const { onOptionsChange, options } = this.props;
  const jsonData = {
    ...options.jsonData,
    authToken: event.target.checked,
  };
  onOptionsChange({ ...options, jsonData });
}

  render() {
    console.log(this.props.options.jsonData);
    const { options, onOptionsChange } = this.props;
    const { jsonData, secureJsonFields } = options;
    const secureJsonData = (options.secureJsonData || {}) as MySecureJsonData;
    
    options.jsonData.serviceUrl = options.jsonData.serviceUrl === undefined ? logapi : options.jsonData.serviceUrl
    return (
      <>
        <div className="gf-form-group">
        <h3 className="page-heading">Enable Auth Token</h3>
         <LegacyForms.Switch label="Enable Token Auth"  labelClass="width-8" checked={options.jsonData.authToken} onChange={this.onAuthEnable} />
         {(options.jsonData.authToken) &&<><div className="gf-form">
          <LegacyForms.FormField
            label="Path"
            labelWidth={6}
            inputWidth={20}
            onChange={this.onPathChange}
            value={jsonData.path || ''}
            placeholder="External API url" />
        </div>

          <div className="gf-form-inline">
            <div className="gf-form">
              <LegacyForms.SecretFormField
                isConfigured={(secureJsonFields && secureJsonFields.apiKey) as boolean}
                value={secureJsonData.apiKey || ''}
                label="API Key"
                placeholder="secure json field (backend only)"
                labelWidth={6}
                inputWidth={20}
                onReset={this.onResetAPIKey}
                onChange={this.onAPIKeyChange} />
            </div>
          </div></>}</div>

          {(!options.jsonData.authToken) &&<>
        <DataSourceHttpSettings
          defaultUrl="http://localhost:7474/db/data/transaction/commit?includeStats=true"
          dataSourceConfig={options}
          showAccessOptions={true}
          onChange={onOptionsChange}
        />
        </>}

        <h3 className="page-heading">Enable Logging</h3>
         <LegacyForms.Switch label="Enable Logging"  labelClass="width-8" checked={options.jsonData.logging} onChange={this.onSwitched} />
        {(options.jsonData.logging) && <>
        <Field label="Service URL" description="This is required only when insights usage is collected.">
          <Input name="serviceUrl" onChange={this.onServiceUrlChange} value={options.jsonData.serviceUrl} placeholder="External API url"/>
        </Field>
        <LegacyForms.FormField
            label="Password"
            labelWidth={6}
            inputWidth={20}
            onChange={this.onBasicPwdChange}
            value={jsonData.basicPassword || ''}
            placeholder="Base64 password without Basic" />
       </>
        
      }
    </>
    );
  }
}
