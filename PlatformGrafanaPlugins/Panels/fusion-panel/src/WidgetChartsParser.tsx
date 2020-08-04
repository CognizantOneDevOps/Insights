export function formatAnguarGuage(props: any) {
    let dials: any = new Object();
    let dial: any = new Array();
    if (props.data.state === 'Done') {
      let array = props.data.series[0].fields;
      let labels = array[0].values.buffer;
      let index = 0;
      let values = array[1].values.buffer;
      labels.forEach((obj:any) => {
        dial.push({ "value": values[index] })
        index++;
      });
      dials["dial"] = dial;
    }
    return { dials: dials }

  }