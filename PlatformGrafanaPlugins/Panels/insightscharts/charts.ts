///<reference path="../../../headers/common.d.ts" />
import {BaseCharts} from './insightscore/BaseCharts';

export class InsightsCharts {
    google: any;
    constructor() {
        //super();
        //this.google = super.getGooleChart();
    }

    /*renderChart() {
        console.log(this);
        var data = new this.google.visualization.DataTable();
        data.addColumn('string', 'Topping');
        data.addColumn('number', 'Slices');
        data.addRows([
          ['Mushrooms', 3],
          ['Onions', 1],
          ['Olives', 1],
          ['Zucchini', 1],
          ['Pepperoni', 2]
        ]);

        // Set chart options
        var options = {'title': 'How Much Pizza I Ate Last Night',
                       'width': 400,
                       'height': 300};

        // Instantiate and draw our chart, passing in some options.
        var chart = new this.google.visualization.PieChart(document.getElementById('chart_div'));
        chart.draw(data, options);
    }*/
}
