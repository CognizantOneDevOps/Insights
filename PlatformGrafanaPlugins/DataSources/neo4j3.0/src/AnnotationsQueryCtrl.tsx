const query = {
    "statements": [
      {
        "statement": "match (n) return n limit 1",
        "includeStats": true,
        "resultDataContents": ["row", "graph"]
      }
    ],
    "metadata": [{
      "testDB": true
    }]
  };

export class AnnotationsQueryCtrl {
    static templateUrl = 'partials/annotations.editor.html';
    annotation: any;
    
    /** @ngInject */
    constructor() {
      this.annotation.query = this.annotation.query || JSON.stringify(query);
     // this.onQueryChange = this.onQueryChange.bind(this);
    }
  
   /* onQueryChange(expr: string) {
      this.annotation.expr = expr;
    }*/
  }