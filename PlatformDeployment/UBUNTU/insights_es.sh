#!/bin/bash
#-------------------------------------------------------------------------------
# Copyright 2017 Cognizant Technology Solutions
#   
# Licensed under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License.  You may obtain a copy
# of the License at
# 
#   http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
# License for the specific language governing permissions and limitations under
# the License.
#-------------------------------------------------------------------------------
# install ES  - Some Issues here test on new server
echo "#################### Installing Eleastic Search with configs ####################"
mkdir elasticsearch
cd elasticsearch
wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/Ubuntu/packages/elasticsearch/elasticsearch.deb
sudo dpkg -i elasticsearch.deb
wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/Ubuntu/packages/elasticsearch/ElasticSearch.zip
unzip ElasticSearch.zip
sudo cp ElasticSearch/elasticsearch.yml /etc/elasticsearch/elasticsearch.yml
sudo cp ElasticSearch/log4j2.properties /etc/elasticsearch/log4j2.properties
sudo systemctl daemon-reload
sudo systemctl enable elasticsearch.service
sudo systemctl start elasticsearch.service
sleep 20
curl -XPUT 'localhost:9200/_template/neo4j-*' -d '{"order" : 0, "template": "neo4j-*","settings": {"index.number_of_shards": "5"},"mappings": {"_default_": {"dynamic_templates": [{"string_fields" : {"mapping" : {"index" : "analyzed","omit_norms" : true,"type" : "string","fields" : {"raw" : {"ignore_above" : 256,"index" : "not_analyzed","type" : "string"}}},"match_mapping_type" : "string","match" : "*"}}]}},"aliases": {}}}'
rm -rf elasticsearch
