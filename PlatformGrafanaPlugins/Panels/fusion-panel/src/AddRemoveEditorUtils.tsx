import { ChangeEvent, MouseEvent } from 'react';

export function onAddRemoveChange(event: ChangeEvent<HTMLTextAreaElement>, props: any) {

    if (event.target.value == "") {
        props.onOptionsChange({ ...props.options, 'addRemove': event.target.value, 'btnEnabled': true })
    }
    else {
        props.onOptionsChange({ ...props.options, 'addRemove': event.target.value, 'btnEnabled': false })
    }
}

export function onNodeClick(event: MouseEvent<HTMLButtonElement>, props: any) {
    let staicChartOptions = JSON.parse(props.options.addRemove)
    props.onOptionsChange({ ...props.options, 'data': staicChartOptions, 'btnEnabled': false })
}