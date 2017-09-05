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


// Lint and build CSS
module.exports = function(grunt) {
  'use strict';

  grunt.registerTask('css', [
    'sass',
    'concat:cssDark',
    'concat:cssLight',
    'concat:cssFonts',
    'concat:ngMaterialCss',
    'styleguide',
    'sasslint',
    'postcss',
    ]
  );

  grunt.registerTask('default', [
    'jscs',
    'jshint',
    'exec:tslint',
    'clean:gen',
    'copy:node_modules',
    'copy:public_to_gen',
    'phantomjs',
    'css',
    'exec:tscompile'
  ]);

  grunt.registerTask('test', ['default', 'karma:test', 'no-only-tests']);

  grunt.registerTask('no-only-tests', function() {
    var files = grunt.file.expand('public/**/*_specs\.ts', 'public/**/*_specs\.js');

    files.forEach(function(spec) {
      var rows = grunt.file.read(spec).split('\n');
      rows.forEach(function(row) {
        if (row.indexOf('.only(') > 0) {
          grunt.log.errorlns(row);
          grunt.fail.warn('found only statement in test: ' + spec)
        }
      });
    });
  });
};
