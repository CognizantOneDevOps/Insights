import React, { PureComponent } from 'react';
import { PanelEditorProps } from '@grafana/data';
import { ChartOptions } from './ChartOptions';
import { Select, InlineFormLabel, LegacyForms, ColorPicker, Button, Drawer, JSONFormatter, TabsBar, TabContent, Tab, Spinner } from '@grafana/ui';
import ClipboardJS from 'clipboard';
import { CopyToClipboard } from 'react-copy-to-clipboard';
//import classNames from 'classnames';

import OptionsGroup from './OptionsGroup';
//import { ResponsiveButton } from './ResponsiveButton';
import { UseState } from './UseState';


import { chartTypes, fontFamily, FontSizes, captionAlignment, axisPositions, themes, rotateValues, labels, pieAndDoughnutCharts, multiSeries, valuePositions, multiSeriesChats, combinationCharts, combinationWithLine, widgetCharts } from 'types';
import { onTextChange, onClick, onChartTypeChanged, onValueChanged, onChanged, onSwitched, onDrillChange, onLevel2ChartTypeChanged, onLevel3ChartTypeChanged, onLevel3DrillChange, onLevel2ChartPropertiesChange, onLevel3ChartPropertiesChange } from './SingleSeriesEditorUtils';
import { onGraphModeChanged, onLevel2GraphModeChanged, onLevel3GraphModeChanged } from 'MultiSeriesEditorUtils';
import { onCombinationTypeChanged, onLevel2CombinationTypeChanged, onLevel3CombinationTypeChanged } from 'CombinationSeriesEditorUtils';
import { onPropsTextChange, onWidgetTypeChanged } from 'WidgetEditorUtils';
import { onAddRemoveChange } from 'AddRemoveEditorUtils';
import { getTemplateSrv, getBackendSrv } from '@grafana/runtime';
import { addTimestampToQuery, convertResponseToDataFramesTable } from 'DrillDownUtil';


interface State {
    close: boolean;
    copied: boolean;
    loader: boolean;
}
export class FusionEditor extends PureComponent<PanelEditorProps<ChartOptions>, State> {
    clipboardjs?: ClipboardJS;

    constructor(props: any) {
        super(props);
        this.state = {
            close: false,
            copied: false,
            loader: false,
        }
    }


    render() {
        const { FormField, Switch } = LegacyForms;
        const { options } = this.props;
        const tabs = [
            { label: 'Level-2', key: 'first', active: true },
            { label: 'Level-3', key: 'second', active: false }
        ];
        const drillLabels = fetchDrillDownLabels(this.props);
        //console.log('drillLabels--', drillLabels);

        return (<>

            {/*DrillDown*/}
            <OptionsGroup title="Drill down" key="Drill down" defaultToClosed={true}>

                <Switch label="Enable DrillDown" labelClass="width-8" checked={options.enableDrillDown} onChange={(e) => onSwitched('enableDrillDown', e, this.props)} />
                
                {this.props.options.enableDrillDown && <div>
                    <Switch label="Enable Level3" labelClass="width-8" checked={options.enableLevel3} onChange={(e) => onSwitched('enableLevel3', e, this.props)} />
                    {this.state.close &&
                        <Drawer title="Neo4j Response" subtitle="Raw response from neo4j database." onClose={() => { this.openResult() }}>
                            <CopyToClipboard onCopy={() => this.setState({ copied: true })} text={JSON.stringify(options.drillDownResponse)}>
                                <Button icon="copy" variant="secondary">Copy to clipboard</Button>
                            </CopyToClipboard>
                            {this.state.copied ? <span style={{ color: 'green', paddingLeft: '15px', fontSize: '15px', fontWeight: 'bolder' }}>Response Copied.</span> : null}
                            {!this.state.loader && <div style={{ border: '1px solid gray', borderRadius: '4px', paddingLeft: '5%', paddingTop: '5%' }}><JSONFormatter json={options.drillDownResponse} /></div>}
                            {this.state.loader && <div style={{ border: '0px solid gray', borderRadius: '4px', padding: '20%', paddingLeft: '50%' }}><Spinner /></div>}
                        </Drawer>
                    }
                    <UseState initialState={tabs}>
                        {(state, updateState) => {
                            return (
                                <div>
                                    <TabsBar>
                                        {state.map((tab, index) => {
                                            return (
                                                <Tab
                                                    key={index}
                                                    label={tab.label}
                                                    active={tab.active}
                                                    onChangeTab={() => updateState(state.map((tab, idx) => ({ ...tab, active: idx === index })))}
                                                //counter={index}
                                                />
                                            );
                                        })}
                                    </TabsBar>
                                    <TabContent>
                                        {state[0].active && <div>
                                            <div className="form-field">
                                                <InlineFormLabel width={12}>Single Series Chart Type</InlineFormLabel>
                                                <Select width={25} options={chartTypes} defaultValue={chartTypes[0]}
                                                    onChange={(e) => onLevel2ChartTypeChanged(e, this.props)}
                                                    value={chartTypes.find(item => item.value === options.level2ChartType)}></Select>
                                            </div>
                                            <div className="form-field">
                                                <InlineFormLabel width={12}>MultiSeries Chart Type</InlineFormLabel>
                                                <Select width={25} options={multiSeries}
                                                    onChange={(e) => onLevel2GraphModeChanged(e, this.props)}
                                                    value={multiSeries.find(item => item.value === options.level2ChartType)}></Select>

                                            </div>
                                            <div className="form-field">
                                                <InlineFormLabel width={12}>Combination Series Chart Type</InlineFormLabel>
                                                <Select width={40} options={combinationCharts}
                                                    onChange={(e) => onLevel2CombinationTypeChanged(e, this.props)}
                                                    value={combinationCharts.find(item => item.value === options.level2ChartType)}></Select>
                                            </div>
                                            <div className="form-field">
                                                <InlineFormLabel width={10}>Level2 Chart Properties</InlineFormLabel>
                                                <textarea rows={11} className="gf-form-input width-100 max-height-400px"
                                                    onChange={(e) => onLevel2ChartPropertiesChange(e, this.props)}
                                                    value={options.level2ChartProperties}
                                                />
                                            </div>


                                            {drillLabels && drillLabels.map((name: any, index: any) => (

                                                <div className="gf-form-inline">
                                                    <div className="gf-form gf-form--grow">
                                                        <InlineFormLabel width={10}>{name}</InlineFormLabel>
                                                        <Button id={"button" + index} icon="sync" onClick={(e => this.testQuery(e, index, this.props, name))}>Test Query</Button>
                                                        <textarea rows={11} className="gf-form-input width-100 max-height-400px"
                                                            onChange={(e) => onDrillChange(e, this.props, index, name)}
                                                            value={options.drillObj[name]}
                                                        />
                                                        {/*<div style={{ border: '1px solid green', borderRadius: '4px', padding: '10px', cursor: 'pointer' }}
                                                                onClick={() => this.openResult()}> Neo4j Response </div>*/}
                                                    </div>
                                                </div>
                                            ))}
                                        </div>}
                                        
                                        {state[1].active && this.props.options.enableLevel3 && <div>
                                            <div className="form-field">
                                                <InlineFormLabel width={12}>Single Series Chart Type</InlineFormLabel>
                                                <Select width={25} options={chartTypes} defaultValue={chartTypes[0]}
                                                    onChange={(e) => onLevel3ChartTypeChanged(e, this.props)}
                                                    value={chartTypes.find(item => item.value === options.level3ChartType)}></Select>
                                            </div>
                                            <div className="form-field">
                                                <InlineFormLabel width={12}>MultiSeries Chart Type</InlineFormLabel>
                                                <Select width={25} options={multiSeries}
                                                    onChange={(e) => onLevel3GraphModeChanged(e, this.props)}
                                                    value={multiSeries.find(item => item.value === options.level3ChartType)}></Select>

                                            </div>
                                            <div className="form-field">
                                                <InlineFormLabel width={12}>Combination Series Chart Type</InlineFormLabel>
                                                <Select width={40} options={combinationCharts}
                                                    onChange={(e) => onLevel3CombinationTypeChanged(e, this.props)}
                                                    value={combinationCharts.find(item => item.value === options.level3ChartType)}></Select>
                                            </div>

                                            <div className="form-field">
                                                <InlineFormLabel width={10}>Level3 Chart Properties</InlineFormLabel>
                                                <textarea rows={10} className="gf-form-input width-100 max-height-400px"
                                                    onChange={(e) => onLevel3ChartPropertiesChange(e, this.props)}
                                                    value={options.level3ChartProperties}
                                                />
                                            </div>
                                            {this.props.options.level0 && this.props.options.level0.map((name: any, index: any) => (

                                                <div className="gf-form-inline">
                                                    <div className="gf-form gf-form--grow">
                                                        <InlineFormLabel width={10}>{name}</InlineFormLabel>
                                                        <Button id={"button" + index} icon="sync" onClick={(e => this.testQuery(e, index, this.props, name))}>Test Query</Button>

                                                        <textarea id={"query" + index} rows={11} className="gf-form-input width-100 max-height-400px"
                                                            onChange={(e) => onLevel3DrillChange(e, this.props, index, name)}
                                                            required value={this.props.options.level3drillObj[name]}
                                                        />

                                                        {/*<div style={{ border: '1px solid green', borderRadius: '4px', padding: '10px', cursor: 'pointer' }}
                                                     onClick={() => this.openResult()}> Neo4j Response </div>*/}
                                                    </div>
                                                </div>
                                            ))}

                                        </div>}
                                        {/* {state[2].active && <div>Third tab content</div>}*/}
                                    </TabContent>
                                </div>
                            );
                        }}
                    </UseState>
                </div>}
            </OptionsGroup>

            {/*Chart attributes*/}
            <OptionsGroup title="Chart Attributes" key="Chart Attributes" defaultToClosed={true}>
                <div className="form-field" >Default chart Type - Single Series column 2d</div>
                <div className="form-field">------------------------------------------------------------------------------</div>
                <div className="form-field">Chart Selected - {options.charttype}</div>
                <div className="form-field">------------------------------------------------------------------------------</div>
                <div className="form-field">
                    <InlineFormLabel width={12}>Single Series Chart Type</InlineFormLabel>
                    <Select width={25} options={chartTypes} defaultValue={chartTypes[0]}
                        onChange={(e) => onChartTypeChanged(e, this.props)}
                        value={chartTypes.find(item => item.value === options.charttype)}></Select>
                </div>
                <div className="form-field">
                    <InlineFormLabel width={12}>MultiSeries Chart Type</InlineFormLabel>
                    <Select width={25} options={multiSeries} defaultValue={multiSeries[0]}
                        onChange={(e) => onGraphModeChanged(e, this.props)}
                        value={multiSeries.find(item => item.value === options.charttype)}></Select>

                </div>
                <div className="form-field">
                    <InlineFormLabel width={12}>Combination Series Chart Type</InlineFormLabel>
                    <Select width={40} options={combinationCharts} defaultValue={combinationCharts[0]}
                        onChange={(e) => onCombinationTypeChanged(e, this.props)}
                        value={combinationCharts.find(item => item.value === options.charttype)}></Select>
                </div>
                <div className="form-field">
                    <InlineFormLabel width={12}>Widget Chart Type</InlineFormLabel>
                    <Select width={20} options={widgetCharts} defaultValue={widgetCharts[0]}
                        onChange={(e) => onWidgetTypeChanged(e, this.props)}
                        value={widgetCharts.find(item => item.value === options.charttype)}></Select>
                </div>
                {options.isGauge &&
                    <div>

                        <FormField label="Major TM Number" labelWidth={12} inputWidth={11} type="text" onChange={(e) => onChanged('majorTMNumber', e, this.props)} value={options.majorTMNumber || ''} />
                        <FormField label="Minor TM Number" labelWidth={12} inputWidth={11} type="text" onChange={(e) => onChanged('minorTMNumber', e, this.props)} value={options.minorTMNumber || ''} />
                        <div className="form-field">
                            <InlineFormLabel width={12}>Major TM Color</InlineFormLabel>
                            <div className="thresholds-row-input-inner-color" style={{ padding: '7px' }}>
                                <div className="thresholds-row-input-inner-color-colorpicker">
                                    <ColorPicker color={options.majorTMColor} onChange={(e) => onValueChanged('majorTMColor', e, this.props)}></ColorPicker>
                                </div>
                            </div>
                        </div>
                        <FormField label="Major TM Alpha" labelWidth={12} inputWidth={11} type="text" onChange={(e) => onChanged('majorTMAlpha', e, this.props)} value={options.majorTMAlpha || ''} />
                        <FormField label="Major TM Height" labelWidth={12} inputWidth={11} type="text" onChange={(e) => onChanged('majorTMHeight', e, this.props)} value={options.majorTMHeight || ''} />
                        <FormField label="Major TM Thickness" labelWidth={12} inputWidth={11} type="text" onChange={(e) => onChanged('majorTMThickness', e, this.props)} value={options.majorTMThickness || ''} />
                        <div className="form-field">
                            <InlineFormLabel width={12}>Minor TM Color</InlineFormLabel>
                            <div className="thresholds-row-input-inner-color" style={{ padding: '7px' }}>
                                <div className="thresholds-row-input-inner-color-colorpicker">
                                    <ColorPicker color={options.minorTMColor} onChange={(e) => onValueChanged('minorTMColor', e, this.props)}></ColorPicker>
                                </div>
                            </div>
                        </div>
                        <FormField label="Minor TM Alpha" labelWidth={12} inputWidth={11} type="text" onChange={(e) => onChanged('minorTMAlpha', e, this.props)} value={options.minorTMAlpha || ''} />
                        <FormField label="Minor TM Height" labelWidth={12} inputWidth={11} type="text" onChange={(e) => onChanged('minorTMHeight', e, this.props)} value={options.minorTMHeight || ''} />
                        <FormField label="Minor TM Thickness" labelWidth={12} inputWidth={11} type="text" onChange={(e) => onChanged('minorTMThickness', e, this.props)} value={options.minorTMThickness || ''} />

                    </div>

                }

                {options.isArea &&
                    <div className="form-field">
                        <InlineFormLabel width={8}>Area Fill</InlineFormLabel>
                        <div className="thresholds-row-input-inner-color" style={{ padding: '7px' }}>
                            <div className="thresholds-row-input-inner-color-colorpicker">
                                <ColorPicker color={options.plotFillColor} onChange={(e) => onValueChanged('plotFillColor', e, this.props)}></ColorPicker>
                            </div>
                        </div>
                    </div>
                }
                {options.isDualAxis &&
                    <div>
                        <FormField label="SY-Axis Name" labelWidth={8} inputWidth={19} type="text" onChange={(e) => onChanged('syaxisname', e, this.props)} value={options.syaxisname || ''} />
                        <FormField label="Format Number" labelWidth={8} inputWidth={19} type="text" onChange={(e) => onChanged('formatnumberscale', e, this.props)} value={options.formatnumberscale || ''} />
                        <FormField label="SNumber Suffix" labelWidth={8} inputWidth={19} type="text" onChange={(e) => onChanged('snumbersuffix', e, this.props)} value={options.snumbersuffix || ''} />
                        <FormField label="Plot Tool Text" labelWidth={8} inputWidth={19} type="text" onChange={(e) => onChanged('plottooltext', e, this.props)} value={options.plottooltext || ''} />
                    </div>
                }

                {(options.isScrollCombi2D || options.isScrollCharts) &&
                    <div>
                        <FormField label="No Visible Plot" labelWidth={12} inputWidth={10} type="text" onChange={(e) => onChanged('numVisiblePlot', e, this.props)} value={options.numVisiblePlot || ''} />
                        <FormField label="Scroll Height" labelWidth={12} inputWidth={10} type="text" onChange={(e) => onChanged('scrollheight', e, this.props)} value={options.scrollheight || ''} />
                        <FormField label="Scroll Color" labelWidth={12} inputWidth={10} type="text" onChange={(e) => onChanged('scrollColor', e, this.props)} value={options.scrollColor || ''} />
                        <Switch label="Flat ScrollBars" labelClass="width-12" checked={options.flatScrollBars} onChange={(e) => onSwitched('flatScrollBars', e, this.props)} />
                        <Switch label="Scroll Buttons" labelClass="width-12" checked={options.scrollShowButtons} onChange={(e) => onSwitched('scrollShowButtons', e, this.props)} />
                    </div>
                }
                <div>
                    {options.isBarAndColumnChart &&
                        <div>
                            <Switch label="Show Values Inside" labelClass="width-12" checked={options.placeValuesInside} onChange={(e) => onSwitched('placeValuesInside', e, this.props)} />
                        </div>
                    }
                    {(options.isParetoChart || options.isLineChart || options.charttype === 'scrollline2d' ||
                        combinationWithLine.includes(options.charttype)) &&
                        <div>
                            <Switch label="Show/Hide Line Value" labelClass="width-12" checked={options.showLineValues} onChange={(e) => onSwitched('showLineValues', e, this.props)} />
                            <div className="form-field">
                                <InlineFormLabel width={12}>Line Color</InlineFormLabel>
                                <div className="thresholds-row-input-inner-color" style={{ padding: '7px' }}>
                                    <div className="thresholds-row-input-inner-color-colorpicker">
                                        <ColorPicker color={options.lineColor} onChange={(e) => onValueChanged('lineColor', e, this.props)}></ColorPicker>
                                    </div>
                                </div>
                            </div>
                            <FormField label="Line Alpha" labelWidth={12} inputWidth={10} type="text" onChange={(e) => onChanged('lineAlpha', e, this.props)} value={options.lineAlpha || ''} />
                            <FormField label="Line Thickness" labelWidth={12} inputWidth={10} type="text" onChange={(e) => onChanged('lineThickness', e, this.props)} value={options.lineThickness || ''} />
                            <Switch label="Line Dashed" labelClass="width-12" checked={options.lineDashed} onChange={(e) => onSwitched('lineDashed', e, this.props)} />
                            <FormField label="Line Dash Len" labelWidth={12} inputWidth={10} type="text" onChange={(e) => onChanged('lineDashLen', e, this.props)} value={options.lineDashLen || ''} />
                            <FormField label="Line Dash Gap" labelWidth={12} inputWidth={10} type="text" onChange={(e) => onChanged('lineDashGap', e, this.props)} value={options.lineDashGap || ''} />
                        </div>
                    }
                    {options.isPieOrDoughnutChart &&
                        <div>
                            <Switch label="Show/Hide Per Values" labelClass="width-12" checked={options.showPercentValues} onChange={(e) => onSwitched('showPercentValues', e, this.props)} />
                            <FormField label="Default Center Label" labelWidth={12} inputWidth={10} type="text" onChange={(e) => onChanged('defaultCenterLabel', e, this.props)} value={options.defaultCenterLabel || ''} />
                            <FormField label="Center Label" labelWidth={12} inputWidth={10} type="text" onChange={(e) => onChanged('centerLabel', e, this.props)} value={options.centerLabel || ''} />
                            <FormField label="Min Angle For Value" labelWidth={12} inputWidth={10} type="text" onChange={(e) => onChanged('minAngleForValue', e, this.props)} value={options.minAngleForValue || ''} />
                            <Switch label="Enable Multi-Slicing" labelClass="width-12" checked={options.enableMultiSlicing} onChange={(e) => onSwitched('enableMultiSlicing', e, this.props)} />
                            <div className="form-field">
                                <InlineFormLabel width={12}>Label Position</InlineFormLabel>
                                <Select
                                    width={10}
                                    options={valuePositions}
                                    defaultValue={valuePositions[0]}
                                    onChange={(e) => onValueChanged('labelPosition', e.value, this.props)}
                                    value={valuePositions.find(item => item.value === options.labelPosition)}></Select>
                            </div>
                            <div className="form-field">
                                <InlineFormLabel width={12}>Value Position</InlineFormLabel>
                                <Select
                                    width={10}
                                    options={valuePositions}
                                    defaultValue={valuePositions[0]}
                                    onChange={(e) => onValueChanged('valuePosition', e.value, this.props)}
                                    value={valuePositions.find(item => item.value === options.valuePosition)}></Select>
                            </div>

                        </div>
                    }
                </div>
                {options.isWaterfallChart &&
                    <div>
                        <div className="form-field">
                            <InlineFormLabel width={12}>Positive Color</InlineFormLabel>
                            <div className="thresholds-row-input-inner-color" style={{ padding: '7px' }}>
                                <div className="thresholds-row-input-inner-color-colorpicker">
                                    <ColorPicker color={options.positiveColor} onChange={(e) => onValueChanged('positiveColor', e, this.props)}></ColorPicker>
                                </div>
                            </div>
                        </div>
                        <div className="form-field">
                            <InlineFormLabel width={12}>Negative Color</InlineFormLabel>
                            <div className="thresholds-row-input-inner-color" style={{ padding: '7px' }}>
                                <div className="thresholds-row-input-inner-color-colorpicker">
                                    <ColorPicker color={options.negativeColor} onChange={(e) => onValueChanged('negativeColor', e, this.props)}></ColorPicker>
                                </div>
                            </div>
                        </div>
                        <Switch label="Show Sum At End" labelClass="width-12" checked={options.showSumAtEnd} onChange={(e) => onSwitched('showSumAtEnd', e, this.props)} />
                        {options.showSumAtEnd &&
                            <div>
                                <FormField label="Sum Label" labelWidth={12} inputWidth={10} type="text" onChange={(e) => onChanged('sumlabel', e, this.props)} value={options.sumlabel || ''} />
                            </div>
                        }

                        <div className="form-field">
                            <InlineFormLabel width={12}>Connector Color</InlineFormLabel>
                            <div className="thresholds-row-input-inner-color" style={{ padding: '7px' }}>
                                <div className="thresholds-row-input-inner-color-colorpicker">
                                    <ColorPicker color={options.connectorColor} onChange={(e) => onValueChanged('connectorColor', e, this.props)}></ColorPicker>
                                </div>
                            </div>
                        </div>
                        <FormField label="Connector Opacity" labelWidth={12} inputWidth={10} type="text" onChange={(e) => onChanged('connectorAlpha', e, this.props)} value={options.connectorAlpha || ''} />
                        <FormField label="Connector Thikness" labelWidth={12} inputWidth={10} type="text" onChange={(e) => onChanged('connectorThickness', e, this.props)} value={options.connectorThickness || ''} />
                    </div>
                }
                {options.isZoomlineChart &&
                    <div>
                        <FormField label="Pixel Per Point" labelWidth={12} inputWidth={10} type="text" onChange={(e) => onChanged('pixelsPerPoint', e, this.props)} value={options.pixelsPerPoint || ''} />
                        <FormField label="Pixel Per Label" labelWidth={12} inputWidth={10} type="text" onChange={(e) => onChanged('pixelsPerLabel', e, this.props)} value={options.pixelsPerLabel || ''} />
                        <FormField label="No of Visible Labels" labelWidth={12} inputWidth={10} type="text" onChange={(e) => onChanged('numVisibleLabels', e, this.props)} value={options.numVisibleLabels || ''} />
                        <div className="form-field">
                            <InlineFormLabel width={12}>Zoom Pane Bg Color</InlineFormLabel>
                            <div className="thresholds-row-input-inner-color" style={{ padding: '7px' }}>
                                <div className="thresholds-row-input-inner-color-colorpicker">
                                    <ColorPicker color={options.zoomPaneBgColor} onChange={(e) => onValueChanged('zoomPaneBgColor', e, this.props)}></ColorPicker>
                                </div>
                            </div>
                        </div>
                        <LegacyForms.FormField label="Zoom Pane Opacity" labelWidth={12} inputWidth={10} type="text" onChange={(e) => onChanged('zoomPaneBgAlpha', e, this.props)} value={options.zoomPaneBgAlpha || ''} />
                    </div>
                }

            </OptionsGroup>

            {/*Advance View*/}
            <OptionsGroup title="Advanced view" key="Advanced view" defaultToClosed={true}>
                <div className="gf-form-inline">
                    <div className="gf-form gf-form--grow">
                        <textarea rows={11} className="gf-form-input width-100 max-height-400px"
                            onChange={(e) => onTextChange(e, this.props)}
                            placeholder="{'caption':'Test Caption'}" value={options.atext} required />
                    </div>
                </div>
                <button className="btn btn-success" onClick={(e) => onClick(e, this.props)} type="submit" disabled={options.btnEnabled}> Apply</button>
            </OptionsGroup>

            {/*Caption * subcaption*/}
            <OptionsGroup title="Caption/Sub-Caption" key="Caption/Sub-Caption" defaultToClosed={true}>
                <FormField label="Caption" labelWidth={9} inputWidth={13} type="text" onChange={(e) => onChanged('caption', e, this.props)} value={options.caption || ""} />
                <div className="form-field">
                    <InlineFormLabel width={9}>Caption Font</InlineFormLabel>
                    <Select width={13} options={fontFamily}
                        onChange={(e) => onValueChanged('captionFont', e.value, this.props)}
                        value={fontFamily.find(item => item.value === options.captionFont)}></Select>
                </div>
                <div className="form-field">
                    <InlineFormLabel width={9}>Caption Size</InlineFormLabel>
                    <Select width={13} options={FontSizes}
                        onChange={(e) => onValueChanged('captionFontSize', e.value, this.props)}
                        value={FontSizes.find(item => item.value === options.captionFontSize)}></Select>
                </div>
                <Switch label="Caption Bold" labelClass="width-9" checked={options.captionFontBold} onChange={(e) => onSwitched('captionFontBold', e, this.props)} />
                <div className="form-field">
                    <InlineFormLabel width={9}>Caption Color</InlineFormLabel>
                    <div className="thresholds-row-input-inner-color" style={{ padding: '7px' }}>
                        <div className="thresholds-row-input-inner-color-colorpicker">
                            <ColorPicker color={options.captionFontColor} onChange={(e: any) => onValueChanged('captionFontColor', e, this.props)}></ColorPicker>
                        </div>
                    </div>
                </div>
                <FormField label="Sub Caption" labelWidth={9} inputWidth={13} type="text" onChange={(e) => onChanged('subCaption', e, this.props)} value={options.subCaption || ""} />
                <div className="form-field">
                    <InlineFormLabel width={9}>Sub Caption Font</InlineFormLabel>
                    <Select width={13} options={fontFamily}
                        onChange={(e) => onValueChanged('subcaptionFont', e.value, this.props)}
                        value={fontFamily.find(item => item.value === options.subcaptionFont)}></Select>
                </div>
                <div className="form-field">
                    <InlineFormLabel width={9}>Sub Caption Size</InlineFormLabel>
                    <Select width={13} options={FontSizes}
                        onChange={(e) => onValueChanged('subcaptionFontSize', e.value, this.props)}
                        value={FontSizes.find(item => item.value === options.subcaptionFontSize)}></Select>
                </div>
                <div className="form-field">
                    <InlineFormLabel width={9}>Sub-Caption Color</InlineFormLabel>
                    <div className="thresholds-row-input-inner-color" style={{ padding: '7px' }}>
                        <div className="thresholds-row-input-inner-color-colorpicker">
                            <ColorPicker color={options.subcaptionFontColor} onChange={(e) => onValueChanged('subcaptionFontColor', e, this.props)}></ColorPicker>
                        </div>
                    </div>
                </div>
                <Switch label="Sub Caption Bold" labelClass="width-9" checked={options.subcaptionFontBold} onChange={(e) => onSwitched('subcaptionFontBold', e, this.props)} />
                <div className="form-field">
                    <InlineFormLabel width={9}>Caption Alignment</InlineFormLabel>
                    <Select width={10} options={captionAlignment} defaultValue={captionAlignment[0]}
                        onChange={(e) => onValueChanged('captionAlignment', e.value, this.props)}
                        value={captionAlignment.find(item => item.value === options.captionAlignment)}></Select>
                </div>
            </OptionsGroup>

            {/*X-Axis*/}
            <OptionsGroup title="X-Axis Label" key="X-Axis Label" defaultToClosed={true}>
                {/*<PanelOptionsGroup title="X-Axis">*/}
                <FormField label="X-Axis Label" labelWidth={12} inputWidth={10} type="text" onChange={(e) => onChanged('xAxisName', e, this.props)} value={options.xAxisName || ''} />
                <div className="form-field">
                    <InlineFormLabel width={12}>X-Axis Font</InlineFormLabel>
                    <Select width={10} options={fontFamily}
                        onChange={(e) => onValueChanged('xAxisNameFont', e.value, this.props)}
                        value={fontFamily.find(item => item.value === options.xAxisNameFont)}></Select>
                </div>
                <div className="form-field">
                    <InlineFormLabel width={12}>X-Axis Font-Size</InlineFormLabel>
                    <Select width={10} options={FontSizes}
                        onChange={(e) => onValueChanged('xAxisNameFontSize', e.value, this.props)}
                        value={FontSizes.find(item => item.value === options.xAxisNameFontSize)}></Select>
                </div>
                <Switch label="X-Axis Font-Bold" labelClass="width-12" checked={options.xAxisNameFontBold} onChange={(e) => onSwitched('xAxisNameFontBold', e, this.props)} />
                <div className="form-field">
                    <InlineFormLabel width={12}>X-Axis Bg-Color</InlineFormLabel>
                    <div className="thresholds-row-input-inner-color" style={{ padding: '7px' }}>
                        <div className="thresholds-row-input-inner-color-colorpicker">
                            <ColorPicker color={options.xAxisNameBgColor} onChange={(e) => onValueChanged('xAxisNameBgColor', e, this.props)}></ColorPicker>
                        </div>
                    </div>
                </div>
                <div className="form-field">
                    <InlineFormLabel width={12}>X-Axis Border Color</InlineFormLabel>
                    <div className="thresholds-row-input-inner-color" style={{ padding: '7px' }}>
                        <div className="thresholds-row-input-inner-color-colorpicker">
                            <ColorPicker color={options.xAxisNameBorderColor} onChange={(e) => onValueChanged('xAxisNameBorderColor', e, this.props)}></ColorPicker>
                        </div>
                    </div>
                </div>
                <FormField label="X-Axis Border Thickness" labelWidth={12} inputWidth={10} type="text" onChange={(e) => onChanged('xAxisNameBorderThickness', e, this.props)} value={options.xAxisNameBorderThickness || ''} />
                <FormField label="X-Axis Border Radius" labelWidth={12} inputWidth={10} type="text" onChange={(e) => onChanged('xAxisNameBorderRadius', e, this.props)} value={options.xAxisNameBorderRadius || ''} />
                <FormField label="X-Axis Border padding" labelWidth={12} inputWidth={10} type="text" onChange={(e) => onChanged('xAxisNameBorderPadding', e, this.props)} value={options.xAxisNameBorderPadding || ''} />
                <div className="form-field">
                    <InlineFormLabel width={12}>X-Axis Position</InlineFormLabel>
                    <Select width={10} options={axisPositions}
                        defaultValue={axisPositions[0]}
                        onChange={(e) => onValueChanged('xAxisPosition', e.value, this.props)}
                        value={axisPositions.find(item => item.value === options.xAxisPosition)}></Select>
                </div>
            </OptionsGroup>

            {/*Y-Axis*/}
            <OptionsGroup title="Y-Axis Label" key="Y-Axis Label" defaultToClosed={true}>
                <FormField label="Y-Axis Label" labelWidth={12} inputWidth={10} type="text" onChange={(e) => onChanged('yAxisName', e, this.props)} value={options.yAxisName || ''} />
                <div className="form-field">
                    <InlineFormLabel width={12}>Y-Axis Font</InlineFormLabel>
                    <Select
                        width={10}
                        options={fontFamily}
                        onChange={(e) => onValueChanged('yAxisNameFont', e.value, this.props)}
                        value={fontFamily.find(item => item.value === options.yAxisNameFont)}></Select>
                </div>
                <div className="form-field">
                    <InlineFormLabel width={12}>Y-Axis Font-Size</InlineFormLabel>
                    <Select
                        width={10}
                        options={FontSizes}
                        onChange={(e) => onValueChanged('yAxisNameFontSize', e.value, this.props)}
                        value={FontSizes.find(item => item.value === options.yAxisNameFontSize)}></Select>
                </div>
                <Switch label="Y-Axis Font-Bold" labelClass="width-12" checked={options.yAxisNameFontBold} onChange={(e) => onSwitched('yAxisNameFontBold', e, this.props)} />
                <div className="form-field">
                    <InlineFormLabel width={12}>Y-Axis Bg-Color</InlineFormLabel>
                    <div className="thresholds-row-input-inner-color" style={{ padding: '7px' }}>
                        <div className="thresholds-row-input-inner-color-colorpicker">
                            <ColorPicker color={options.yAxisNameBgColor} onChange={(e) => onValueChanged('yAxisNameBgColor', e, this.props)}></ColorPicker>
                        </div>
                    </div>
                </div>
                <div className="form-field">
                    <InlineFormLabel width={12}>Y-Axis Border Color</InlineFormLabel>
                    <div className="thresholds-row-input-inner-color" style={{ padding: '7px' }}>
                        <div className="thresholds-row-input-inner-color-colorpicker">
                            <ColorPicker color={options.yAxisNameBorderColor} onChange={(e) => onValueChanged('yAxisNameBorderColor', e, this.props)}></ColorPicker>
                        </div>
                    </div>
                </div>
                <FormField label="Y-Axis Border Thickness" labelWidth={12} inputWidth={10} type="text" onChange={(e) => onChanged('yAxisNameBorderThickness', e, this.props)} value={options.yAxisNameBorderThickness || ''} />
                <div className="form-field">
                    <InlineFormLabel width={12}>Y-Axis Label Color</InlineFormLabel>
                    <div className="thresholds-row-input-inner-color" style={{ padding: '7px' }}>
                        <div className="thresholds-row-input-inner-color-colorpicker">
                            <ColorPicker color={options.yAxisNameFontColor} onChange={(e) => onValueChanged('yAxisNameFontColor', e, this.props)}></ColorPicker>
                        </div>
                    </div>
                </div>
                <FormField label="X-Axis Label Color" labelWidth={12} inputWidth={10} type="text" onChange={(e) => onChanged('xAxisNameFontColor', e, this.props)} value={options.xAxisNameFontColor} />
                <FormField label="Y-Axis Border Radius" labelWidth={12} inputWidth={10} type="text" onChange={(e) => onChanged('yAxisNameBorderRadius', e, this.props)} value={options.yAxisNameBorderRadius || ''} />
                <FormField label="Y-Axis Border padding" labelWidth={12} inputWidth={10} type="text" onChange={(e) => onChanged('yAxisNameBorderPadding', e, this.props)} value={options.yAxisNameBorderPadding || ''} />
                <div className="form-field">
                    <InlineFormLabel width={12}>Y-Axis Position</InlineFormLabel>
                    <Select width={10} options={axisPositions}
                        onChange={(e) => onValueChanged('yAxisPosition', e.value, this.props)}
                        value={axisPositions.find(item => item.value === options.yAxisPosition)}></Select>
                </div>
                <FormField label="Y-Axis Min Value" labelWidth={12} inputWidth={10} type="text" onChange={(e) => onChanged('yAxisMinValue', e, this.props)} value={options.yAxisMinValue || ''} />
                <FormField label="Y-Axis Max Value" labelWidth={12} inputWidth={10} type="text" onChange={(e) => onChanged('yAxisMaxValue', e, this.props)} value={options.yAxisMaxValue || ''} />
            </OptionsGroup>

            {/*colors*/}
            <OptionsGroup title="Color & Themes" key="Color" defaultToClosed={true}>
                <FormField label="Palette colors" labelWidth={8} inputWidth={13} type="text" onChange={(e) => onChanged('palettecolors', e, this.props)} value={options.palettecolors || ''} />
                <div className="form-field">
                    <InlineFormLabel width={8}>Theme</InlineFormLabel>
                    <Select width={13} options={themes} defaultValue={themes[0]}
                        onChange={(e) => onValueChanged('theme', e.value, this.props)}
                        value={themes.find(item => item.value === options.theme)}></Select>
                </div>
                <div className="form-field">
                    <InlineFormLabel width={8}>Toolbar Color</InlineFormLabel>
                    <div className="thresholds-row-input-inner-color" style={{ padding: '7px' }}>
                        <div className="thresholds-row-input-inner-color-colorpicker">
                            <ColorPicker color={options.toolbarButtonColor} onChange={(e) => onValueChanged('toolbarButtonColor', e, this.props)}></ColorPicker>
                        </div>
                    </div>
                </div>
                {options.isGauge &&
                    <div className="form-field">
                        <FormField label="Gauge Fill Mix" labelWidth={8} inputWidth={13} type="text" onChange={(e) => onChanged('gaugeFillMix', e, this.props)} value={options.gaugeFillMix || ''} />
                        <FormField label="Gauge Fill Ratio" labelWidth={8} inputWidth={13} type="text" onChange={(e) => onChanged('gaugeFillRatio', e, this.props)} value={options.gaugeFillRatio || ''} />
                    </div>}
            </OptionsGroup>

            {/*Display & cosmetics */}
            <OptionsGroup title="Display & Value Cosmetics" key="Display & Value Cosmetics" defaultToClosed={true}>
                <Switch label="Export Enabled" labelClass="width-12" checked={options.exportEnabled} onChange={(e) => onSwitched('exportEnabled', e, this.props)} />
                <Switch label="ToolTip Show/Hide" labelClass="width-12" checked={options.showToolTip} onChange={(e) => onSwitched('showToolTip', e, this.props)} />
                {options.showToolTip &&
                    <div>
                        <div className="form-field">
                            <InlineFormLabel width={12}>Tooltip Color</InlineFormLabel>
                            <div className="thresholds-row-input-inner-color" style={{ padding: '7px' }}>
                                <div className="thresholds-row-input-inner-color-colorpicker">
                                    <ColorPicker color={options.toolTipColor} onChange={(e) => onValueChanged('toolTipColor', e, this.props)}></ColorPicker>
                                </div>
                            </div>
                        </div>
                        <div className="form-field">
                            <InlineFormLabel width={12}>Tooltip Bg Color</InlineFormLabel>
                            <div className="thresholds-row-input-inner-color" style={{ padding: '7px' }}>
                                <div className="thresholds-row-input-inner-color-colorpicker">
                                    <ColorPicker color={options.toolTipBgColor} onChange={(e) => onValueChanged('toolTipBgColor', e, this.props)}></ColorPicker>
                                </div>
                            </div>
                        </div>
                        <FormField label="Tooltip Border Thikness" labelWidth={12} inputWidth={10} type="text" onChange={(e) => onChanged('toolTipBorderThickness', e, this.props)} value={options.toolTipBorderThickness || ''} />
                        <FormField label="Tooltip Bg Opacity" labelWidth={12} inputWidth={10} type="text" onChange={(e) => onChanged('toolTipBgAlpha', e, this.props)} value={options.toolTipBgAlpha || ''} />
                        <FormField label="Tooltip Border Radius" labelWidth={12} inputWidth={10} type="text" onChange={(e) => onChanged('toolTipBorderRadius', e, this.props)} value={options.toolTipBorderRadius || ''} />
                        <FormField label="Tooltip Padding" labelWidth={12} inputWidth={10} type="text" onChange={(e) => onChanged('toolTipPadding', e, this.props)} value={options.toolTipPadding || ''} />
                    </div>
                }
                <Switch label="Display Values" labelClass="width-12" checked={options.showValues} onChange={(e) => onSwitched('showValues', e, this.props)} />
                {options.showValues &&
                    <div>
                        <FormField label="Number Prefix" labelWidth={12} inputWidth={10} type="text" onChange={(e) => onChanged('numberPrefix', e, this.props)} value={options.numberPrefix || ''} />

                        <div className="form-field">
                            <InlineFormLabel width={12}>Rotate Values</InlineFormLabel>
                            <Select width={10} options={rotateValues} defaultValue={rotateValues[0]}
                                onChange={(e) => onValueChanged('rotateValues', e.value, this.props)}
                                value={rotateValues.find(item => item.value === options.rotateValues)}></Select>
                        </div>
                        <div className="form-field">
                            <InlineFormLabel width={12}>Value Font Size</InlineFormLabel>
                            <Select width={10} options={FontSizes}
                                onChange={(e) => onValueChanged('valueFontSize', e.value, this.props)}
                                value={FontSizes.find(item => item.value === options.valueFontSize)}></Select>
                        </div>
                        <Switch label="Value Font Bold" labelClass="width-12" checked={options.valueFontBold} onChange={(e) => onSwitched('valueFontBold', e, this.props)} />
                    </div>

                }
            </OptionsGroup>

            <OptionsGroup title="Labels" key="Labels" defaultToClosed={true}>
                <div className="form-field">
                    <InlineFormLabel width={9}>Label Display</InlineFormLabel>
                    <Select
                        width={11}
                        options={labels}
                        defaultValue={labels[0]}
                        onChange={(e) => onValueChanged('labelDisplay', e.value, this.props)}
                        value={labels.find(item => item.value === options.labelDisplay)}></Select>
                </div>
                <div className="form-field">
                    <InlineFormLabel width={9}>Label Font</InlineFormLabel>
                    <Select
                        width={11}
                        options={fontFamily}
                        defaultValue={fontFamily[0]}
                        onChange={(e) => onValueChanged('labelFont', e.value, this.props)}
                        value={fontFamily.find(item => item.value === options.labelFont)}></Select>
                </div>
                <div className="form-field">
                    <InlineFormLabel width={9}>Label Font Size</InlineFormLabel>
                    <Select
                        width={11}
                        options={FontSizes}
                        defaultValue={FontSizes[7]}
                        onChange={(e) => onValueChanged('labelFontSize', e.value, this.props)}
                        value={FontSizes.find(item => item.value === options.labelFontSize)}></Select>
                </div>
                <Switch label="Label Font Bold" labelClass="width-9" checked={options.labelFontBold} onChange={(e) => onSwitched('labelFontBold', e, this.props)} />
                <FormField label="Pivot Radius" labelWidth={9} inputWidth={11} type="text" onChange={(e) => onChanged('pivotRadius', e, this.props)} value={options.pivotRadius || ''} />
                <FormField label="Pivot Fill Mix" labelWidth={9} inputWidth={11} type="text" onChange={(e) => onChanged('pivotFillMix', e, this.props)} value={options.pivotFillMix || ''} />
                <FormField label="Pivot Fill Ratio" labelWidth={9} inputWidth={11} type="text" onChange={(e) => onChanged('pivotFillRatio', e, this.props)} value={options.pivotFillRatio || ''} />
                <div className="form-field">
                    <InlineFormLabel width={9}>Pivot Fill Color</InlineFormLabel>
                    <div className="thresholds-row-input-inner-color" style={{ padding: '7px' }}>
                        <div className="thresholds-row-input-inner-color-colorpicker">
                            <ColorPicker color={options.pivotFillColor} onChange={(e) => onValueChanged('pivotFillColor', e, this.props)}></ColorPicker>
                        </div>
                    </div>
                </div>
            </OptionsGroup>

            {/*Border and background*/}
            <OptionsGroup title="Border And Background" key="Border And Background" defaultToClosed={true}>
                <Switch label="Chart Border" labelClass="width-10" checked={options.showBorder} onChange={(e) => onSwitched('showBorder', e, this.props)} />
                {options.showBorder &&
                    <div>
                        <div className="form-field">
                            <InlineFormLabel width={10}>Border Color</InlineFormLabel>
                            <div className="thresholds-row-input-inner-color" style={{ padding: '7px' }}>
                                <div className="thresholds-row-input-inner-color-colorpicker">
                                    <ColorPicker color={options.borderColor} onChange={(e) => onValueChanged('borderColor', e, this.props)}></ColorPicker>
                                </div>
                            </div>
                        </div>
                        <FormField label="Border Thickness" labelWidth={10} inputWidth={11} type="text" onChange={(e) => onChanged('borderThickness', e, this.props)} value={options.borderThickness || ''} />
                        <FormField label="Border Opacity" labelWidth={10} inputWidth={11} type="text" onChange={(e) => onChanged('borderAlpha', e, this.props)} value={options.borderAlpha || ''} />
                    </div>
                }
                <Switch label="Crossline Show/Hide" labelClass="width-10" checked={options.drawCrossLine} onChange={(e) => onSwitched('drawCrossLine', e, this.props)} />
                {options.drawCrossLine &&
                    <div>
                        <div className="form-field">
                            <InlineFormLabel width={10}>Crossline Color</InlineFormLabel>
                            <div className="thresholds-row-input-inner-color" style={{ padding: '7px' }}>
                                <div className="thresholds-row-input-inner-color-colorpicker">
                                    <ColorPicker color={options.crosslinecolor} onChange={(e) => onValueChanged('crosslinecolor', e, this.props)}></ColorPicker>
                                </div>
                            </div>
                        </div>
                        <FormField label="Crossline Opacity" labelWidth={10} inputWidth={11} type="text" onChange={(e) => onChanged('crossLineAlpha', e, this.props)} value={options.crossLineAlpha || ''} />
                    </div>
                }
            </OptionsGroup>


            <div>
                {(pieAndDoughnutCharts.includes(options.charttype) || multiSeriesChats.includes(options.charttype)) &&
                    <div>
                        <OptionsGroup title="Legends" key="Legends" defaultToClosed={true}>
                            <Switch label="Legends Show/Hide" labelClass="width-12" checked={options.showLegend} onChange={(e) => onSwitched('showLegend', e, this.props)} />
                            {options.showLegend &&
                                <div>
                                    <FormField label="Legend Caption" labelWidth={12} inputWidth={10} type="text" onChange={(e) => onChanged('legendCaption', e, this.props)} value={options.legendCaption || ''} />
                                    <Switch label="Legend Caption Font Bold" labelClass="width-12" checked={options.legendCaptionBold} onChange={(e) => onSwitched('legendCaptionBold', e, this.props)} />
                                    <div className="form-field">
                                        <InlineFormLabel width={12}>Legend Caption Font</InlineFormLabel>
                                        <Select width={10} options={fontFamily}
                                            onChange={(e) => onValueChanged('legendCaptionFont', e.value, this.props)}
                                            value={fontFamily.find(item => item.value === options.legendCaptionBold)}></Select>
                                    </div>
                                    <div className="form-field">
                                        <InlineFormLabel width={12}>Legend Caption Font Size</InlineFormLabel>
                                        <Select width={10} options={FontSizes}
                                            onChange={(e) => onValueChanged('legendCaptionFontSize', e.value, this.props)}
                                            value={FontSizes.find(item => item.value === options.legendCaptionFontSize)}></Select>
                                    </div>
                                    <Switch label="Legend Item Font Bold" labelClass="width-12" checked={options.legendItemFontBold} onChange={(e) => onSwitched('legendItemFontBold', e, this.props)} />
                                    <div className="form-field">
                                        <InlineFormLabel width={12}>Legend Item Font</InlineFormLabel>
                                        <Select width={10} options={fontFamily}
                                            defaultValue={fontFamily[0]}
                                            onChange={(e) => onValueChanged('legendItemFont', e.value, this.props)}
                                            value={fontFamily.find(item => item.value === options.legendItemFont)}></Select>
                                    </div>
                                    <FormField label="Legend Item Font Size" labelWidth={12} inputWidth={10} type="text" onChange={(e) => onChanged('legendItemFontSize', e, this.props)} value={options.legendItemFontSize || ''} />
                                    <div className="form-field">
                                        <InlineFormLabel width={12}>Legend Item Font Size</InlineFormLabel>
                                        <Select
                                            width={10}
                                            options={FontSizes}
                                            defaultValue={FontSizes[7]}
                                            onChange={(e) => onValueChanged('legendItemFontSize', e.value, this.props)}
                                            value={FontSizes.find(item => item.value === options.legendItemFontSize)}></Select>
                                    </div>
                                </div>
                            }
                        </OptionsGroup>
                    </div>
                }
            </div>

            {(options.isParetoChart || options.charttype == 'line' || options.charttype === 'scrollline2d' ||
                options.isLineChart || options.charttype == 'isZoomlineChart' || options.charttype == 'radar') &&
                <div>
                    <OptionsGroup title="Anchors" key="Anchors" defaultToClosed={true}>
                        <FormField label="Anchor Radius" labelWidth={12} inputWidth={10} type="text" onChange={(e) => onChanged('anchorRadius', e, this.props)} value={options.anchorRadius || ''} />
                        <FormField label="Anchor Border Thikness" labelWidth={12} inputWidth={10} type="text" onChange={(e) => onChanged('anchorBorderThickness', e, this.props)} value={options.anchorBorderThickness || ''} />
                        <div className="form-field">
                            <InlineFormLabel width={12}>Anchor Bg Color</InlineFormLabel>
                            <div className="thresholds-row-input-inner-color" style={{ padding: '7px' }}>
                                <div className="thresholds-row-input-inner-color-colorpicker">
                                    <ColorPicker color={options.anchorBgColor} onChange={(e) => onValueChanged('anchorBgColor', e, this.props)}></ColorPicker>
                                </div>
                            </div>
                        </div>
                        <div className="form-field">
                            <InlineFormLabel width={12}>Anchor Border Color</InlineFormLabel>
                            <div className="thresholds-row-input-inner-color" style={{ padding: '7px' }}>
                                <div className="thresholds-row-input-inner-color-colorpicker">
                                    <ColorPicker color={options.anchorBorderColor} onChange={(e) => onValueChanged('anchorBorderColor', e, this.props)}></ColorPicker>
                                </div>
                            </div>
                        </div>
                    </OptionsGroup>
                </div>
            }

            {/*Add Remove Node*/}
            <OptionsGroup title="Add & Remove" key="Add & Remove" defaultToClosed={true}>
                <div>
                    <div className="gf-form-inline">
                        <div className="gf-form gf-form--grow">
                            <textarea rows={20} className="gf-form-input width-100" onChange={(e) => onAddRemoveChange(e, this.props)}
                                placeholder="Please refer help section for boilerplate code." value={options.addRemove} required />
                        </div>

                    </div>
                   {/* <div className="gf-form gf-form--v-stretch">
                        <button className="btn btn-success" onClick={(e) => onNodeClick(e, this.props)} type="submit" disabled={options.btnEnabled}>Apply </button>
                    </div>*/}
                </div>
            </OptionsGroup>

            <OptionsGroup title="Gauge Properties" key="Gauge Properties" defaultToClosed={true}>
                <div className="gf-form-inline">
                    <div className="gf-form gf-form--grow">
                        <textarea rows={11} className="gf-form-input width-100 max-height-400px"
                            onChange={(e) => onPropsTextChange(e, this.props)}
                            value={options.staticProps} required
                        />
                    </div>
                </div>
               {/* <button className="btn btn-success" onClick={(e) => onPropsApplyClick(e, this.props)} type="submit" disabled={options.SbtnEnabled}>Apply</button>*/}
            </OptionsGroup>
        </>);
    }
    testQuery(e: any, index: any, props: any, name: any) {
        this.setState({ loader: !this.state.loader });
        this.setState({ close: !this.state.close });
        //console.log('p--', props.options[index]);
        getBackendSrv().datasourceRequest({ url: 'api/datasources/name/' + props.options.datasource, method: 'GET' }).then((res: any) => {
            let data = res.data;
            if (res.status === 200) {
                props.options.datasourceId = data.id;
                this.fetchDrillDownData(props, true, index, name);
            }
            else {
                return { status: 'error', message: res.error };
            }
            return res;
        }).catch((err: any) => {
            console.log(err);
        });

    }

    applyTemplateVariables(value: any, variable: any, formatValue: any) {
        if (typeof value === 'string') {
            let values = [] as any;
            values.push(value);
            value = values;
        }
        return JSON.stringify(value);
    }


    fetchDrillDownData(props: any, drilldown: any, index: any, name: any) {
        let queryText = props.options.drillObj[name] ? props.options.drillObj[name] : props.options.level3drillObj[name];
        let dquery = getTemplateSrv().replace(queryText, {}, this.applyTemplateVariables);
        dquery = addTimestampToQuery(dquery, props.timeRange);
        //console.log('dquery--', dquery);
        const queryJson = {
            "statements": [
                {
                    "statement": dquery,
                    "includeStats": true,
                    "resultDataContents": ["row", "graph"]
                }
            ],
            "metadata": [{
                "testDB": true
            }]
        };
        let testQuery = JSON.stringify(queryJson);
        return getBackendSrv().datasourceRequest({ url: 'api/datasources/proxy/' + this.props.options.datasourceId, method: 'POST', data: testQuery }).then((res: any) => {
            let data = res.data;
            if (drilldown) {
                props.options.drillDownResponse = data;
                this.setState({ loader: !this.state.loader });
                // this.openResult();
                return data;
            }
            if (res.status === 200) {
                return convertResponseToDataFramesTable(res, props, '');
            }
            else {
                return { status: 'error', message: res.error };
            }
        }).catch((err: any) => {
            console.log(err);
            if (err.data && err.data.message) {
                return { status: 'error', message: err.data.message };
            }
            else {
                return { status: 'error', message: err.status };
            }
        });

    }
    copyToClipboard() {
        this.setState({ copied: true });
    }

    openResult(): void {
        this.setState({ close: !this.state.close });
    }


};


function fetchDrillDownLabels(props: any) {
    if (props.data) {
        if (props.data.state === 'Done') {
            return props.data.series[0].fields[0].values.buffer;
        }
    }

}