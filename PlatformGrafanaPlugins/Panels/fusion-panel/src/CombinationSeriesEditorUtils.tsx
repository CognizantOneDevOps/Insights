/**
 * Change Chart type based on chart attribute.
 */
export function onCombinationTypeChanged({ value }: any,props:any){
    if (value === 'mscombi2d' || value === 'mscombi3d') {
      props.onOptionsChange({ ...props.options, 'charttype': value, 'isArea': true, 'isDualAxis': false, 'isScrollCombi2D': false, 'isGauge':false });
    }
    else if (value === 'scrollcombi2d') {
      props.onOptionsChange({ ...props.options, 'charttype': value, 'isArea': true, 'isDualAxis': false, 'isScrollCombi2D': true, 'isGauge':false });
    }
    else if (value === 'mscolumnline3d' || value === 'stackedcolumn2dline' || value === 'stackedcolumn3dline') {
      props.onOptionsChange({ ...props.options, 'charttype': value, 'isArea': false, 'isDualAxis': false, 'isScrollCombi2D': false, 'isGauge':false });
    }
    else if (value === 'mscombidy2d' || value === 'mscombidy3d') {
      props.onOptionsChange({ ...props.options, 'charttype': value, 'isArea': true, 'isDualAxis': true, 'isScrollCombi2D': false, 'isGauge':false });
    }
    else if (value === 'scrollcombidy2d') {
      props.onOptionsChange({ ...props.options, 'charttype': value, 'isArea': true, 'isDualAxis': true, 'isScrollCombi2D': true, 'isGauge':false })
    }
    else {
      props.onOptionsChange({ ...props.options, 'charttype': value, 'isDualAxis': true, 'isArea': false, 'isGauge':false });
    }
  }

  export function onLevel2CombinationTypeChanged({ value }: any,props:any){
    if (value === 'mscombi2d' || value === 'mscombi3d') {
      props.onOptionsChange({ ...props.options, 'level2ChartType': value, 'isArea': true, 'isDualAxis': false, 'isScrollCombi2D': false, 'isGauge':false });
    }
    else if (value === 'scrollcombi2d') {
      props.onOptionsChange({ ...props.options, 'level2ChartType': value, 'isArea': true, 'isDualAxis': false, 'isScrollCombi2D': true, 'isGauge':false });
    }
    else if (value === 'mscolumnline3d' || value === 'stackedcolumn2dline' || value === 'stackedcolumn3dline') {
      props.onOptionsChange({ ...props.options, 'level2ChartType': value, 'isArea': false, 'isDualAxis': false, 'isScrollCombi2D': false, 'isGauge':false });
    }
    else if (value === 'mscombidy2d' || value === 'mscombidy3d') {
      props.onOptionsChange({ ...props.options, 'level2ChartType': value, 'isArea': true, 'isDualAxis': true, 'isScrollCombi2D': false, 'isGauge':false });
    }
    else if (value === 'scrollcombidy2d') {
      props.onOptionsChange({ ...props.options, 'level2ChartType': value, 'isArea': true, 'isDualAxis': true, 'isScrollCombi2D': true, 'isGauge':false })
    }
    else {
      props.onOptionsChange({ ...props.options, 'level2ChartType': value, 'isDualAxis': true, 'isArea': false, 'isGauge':false });
    }
  }

  export function onLevel3CombinationTypeChanged({ value }: any,props:any){
    if (value === 'mscombi2d' || value === 'mscombi3d') {
      props.onOptionsChange({ ...props.options, 'level3ChartType': value, 'isArea': true, 'isDualAxis': false, 'isScrollCombi2D': false, 'isGauge':false });
    }
    else if (value === 'scrollcombi2d') {
      props.onOptionsChange({ ...props.options, 'level3ChartType': value, 'isArea': true, 'isDualAxis': false, 'isScrollCombi2D': true, 'isGauge':false });
    }
    else if (value === 'mscolumnline3d' || value === 'stackedcolumn2dline' || value === 'stackedcolumn3dline') {
      props.onOptionsChange({ ...props.options, 'level3ChartType': value, 'isArea': false, 'isDualAxis': false, 'isScrollCombi2D': false, 'isGauge':false });
    }
    else if (value === 'mscombidy2d' || value === 'mscombidy3d') {
      props.onOptionsChange({ ...props.options, 'level3ChartType': value, 'isArea': true, 'isDualAxis': true, 'isScrollCombi2D': false, 'isGauge':false });
    }
    else if (value === 'scrollcombidy2d') {
      props.onOptionsChange({ ...props.options, 'level3ChartType': value, 'isArea': true, 'isDualAxis': true, 'isScrollCombi2D': true, 'isGauge':false })
    }
    else {
      props.onOptionsChange({ ...props.options, 'level3ChartType': value, 'isDualAxis': true, 'isArea': false, 'isGauge':false });
    }
  }