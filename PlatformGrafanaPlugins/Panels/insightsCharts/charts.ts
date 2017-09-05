/********************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/


///<reference path="../../../headers/common.d.ts" />
import {BaseCharts} from '../insightsCore/BaseCharts';

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
