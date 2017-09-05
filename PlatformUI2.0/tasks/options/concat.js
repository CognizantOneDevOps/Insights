module.exports = function(config) {
  "use strict";

  return {
    css: {
      src: [
        '<%= genDir %>/**/*.css'
      ],
      dest: '<%= genDir %>/css/insights.min.css'
    },

    js: {
      src: [
        '<%= genDir %>/**/*.js',
		'!<%= genDir %>/boot.js'
      ],
      dest: '<%= genDir %>/js/insights.js'
    }
  };
};
