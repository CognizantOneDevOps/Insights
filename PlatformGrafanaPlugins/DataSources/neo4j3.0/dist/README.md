# Neo4j Data Source

This plugin helps to Query neo4j database and converts the response based on selection of response type in UI.

Features of this plugin as follows -

- It supports below response types provided by Grafana.

    -   Table 
            * Supports In-built Grafana Table panel.
            * Supports All Fusion Charts.
            * Supports In-built Stats panel.
            * Supports Insights Charts
    -   Graph 
            * Supports In-built Grafana graph panel.
            * Multi Graph
            * Supports in-built Stats,Gauge,Heatmap

- Cache has been enabled via Cache Results option.

There are 2 cache types to help speed up the results.

    - Fixed Time
    - Variance Time

In order to configure and use the cache of query response refer the below link
https://onedevops.atlassian.net/wiki/spaces/OI/pages/456097795/Query+Caching

- Build process

    * Install yarn corresponding to the operating system. (curl --compressed -o- -L https://yarnpkg.com/install.sh | bash)
    * Issue command - yarn build .
