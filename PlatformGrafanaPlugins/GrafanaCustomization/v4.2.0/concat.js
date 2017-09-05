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


module.exports = function(config) {
  "use strict";

  return {
    cssDark: {
      src: [
        '<%= genDir %>/vendor/css/timepicker.css',
        '<%= genDir %>/vendor/css/spectrum.css',
        '<%= genDir %>/css/grafana.dark.css',
        '<%= genDir %>/vendor/css/font-awesome.min.css'
      ],
      dest: '<%= genDir %>/css/grafana.dark.min.css'
    },

    cssLight: {
      src: [
        '<%= genDir %>/vendor/css/timepicker.css',
        '<%= genDir %>/vendor/css/spectrum.css',
        '<%= genDir %>/css/grafana.light.css',
        '<%= genDir %>/vendor/css/font-awesome.min.css'
      ],
      dest: '<%= genDir %>/css/grafana.light.min.css'
    },

    ngMaterialCss: {
      src: [ '<%= genDir %>/vendor/angular-material/angular-material.css' ],
      dest: '<%= genDir %>/css/angular-material.css'
    },

    cssFonts: {
      src: [ '<%= genDir %>/css/fonts.css' ],
      dest: '<%= genDir %>/css/fonts.min.css'
    },

    js: {
      src: [
        '<%= genDir %>/vendor/npm/es6-shim/es6-shim.js',
        '<%= genDir %>/vendor/npm/es6-promise/dist/es6-promise.js',
        '<%= genDir %>/vendor/npm/systemjs/dist/system-polyfills.js',
        '<%= genDir %>/vendor/npm/systemjs/dist/system.js',
        '<%= genDir %>/app/system.conf.js',
        '<%= genDir %>/app/boot.js',
      ],
      dest: '<%= genDir %>/app/boot.js'
    },

    bundle_and_boot: {
      src: [
        '<%= genDir %>/app/app_bundle.js',
        '<%= genDir %>/app/boot.js',
      ],
      dest: '<%= genDir %>/app/boot.js'
    },
  };
};
