import { ChangeEvent, MouseEvent } from 'react';

export function onWidgetTypeChanged({ value }: any,props:any){
    if (value === 'angulargauge' ) {
      props.onOptionsChange({ ...props.options, 'charttype': value, 'isGauge':true ,'isArea': false, 
      'isDualAxis': false, 'isScrollCombi2D': false,'isZoomlineChart': false, 'isLineChart': false, 'isBarAndColumnChart': false});
    }else if (value === 'dragnode'){
        props.onOptionsChange({ ...props.options, 'charttype': value, 'isGauge':false ,'isArea': false, 
      'isDualAxis': false, 'isScrollCombi2D': false,'isZoomlineChart': false, 'isLineChart': false, 'isBarAndColumnChart': false});
    }
  }

export function onPropsTextChange(event: ChangeEvent<HTMLTextAreaElement>, props: any) {
    if (event.target.value == "") {
        props.onOptionsChange({ ...props.options, 'staticProps': event.target.value, 'SbtnEnabled': true })
    }
    else {
        props.onOptionsChange({ ...props.options, 'staticProps': event.target.value, 'SbtnEnabled': false })
    }
}

export function onPropsApplyClick(event: MouseEvent<HTMLButtonElement>, props: any) {
    let staticChartOptions = JSON.parse(props.options.statiProps)
    props.onOptionsChange({ ...props.options, 'staticProps': staticChartOptions, 'SbtnEnabled': false })
}