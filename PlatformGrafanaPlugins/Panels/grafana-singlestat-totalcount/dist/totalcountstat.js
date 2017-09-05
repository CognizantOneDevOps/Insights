'use strict';

System.register(['angular', 'lodash', 'app/core/utils/kbn', 'jquery', 'jquery.flot', 'jquery.flot.time'], function (_export, _context) {
  var angular, _, kbn, $;

  return {
    setters: [function (_angular) {
      angular = _angular.default;
    }, function (_lodash) {
      _ = _lodash.default;
    }, function (_appCoreUtilsKbn) {
      kbn = _appCoreUtilsKbn.default;
    }, function (_jquery) {
      $ = _jquery.default;
    }, function (_jqueryFlot) {}, function (_jqueryFlotTime) {}],
    execute: function () {

      angular.module('grafana.directives').directive('totalCount', function (popoverSrv, $timeout) {
        return {
          link: function link(scope, elem) {
            var $container = $('<section class="graph-legend"></section>');
            var firstRender = true;
            var ctrl = scope.ctrl;
            var panel = ctrl.panel;
            var data;
            var seriesList;
            var i;

            ctrl.events.on('render', function () {
              data = ctrl.series;
              if (data) {
              render();

              }
            });

            function render() {
              if (panel.legendType === 'On graph') {
                $container.empty();
                return;
              }

              if (firstRender) {
                elem.append($container);
                firstRender = false;
              }

              seriesList = data;
              $container.empty();
              var totalstatscount=0;
              for (i = 0; i < seriesList.length; i++)
               {
                var series = seriesList[i];
                // ignore empty series
                if (panel.legend.hideEmpty && series.allIsNull) {
                  continue;
                }
                // ignore series excluded via override
                if (!series.legend) {
                  continue;
                }
                if (panel.legend.values ) {
                  totalstatscount += series.stats[ctrl.panel.valueName];
                }


            }
            var html = '<div style="font-size:50px;padding-top:50px">' +  totalstatscount+ '</div>';
              $container.append($(html));


            }

          }
        };
      });
    }
  };
});
//# sourceMappingURL=legend.js.map
