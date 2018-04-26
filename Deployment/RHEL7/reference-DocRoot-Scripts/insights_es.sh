# install ES  - Some Issues here test on new server
echo "#################### Installing Eleastic Search with configs ####################"
sudo mkdir elasticsearch
cd elasticsearch
sudo wget http://platform.cogdevops.com/insights_install/installationScripts/latest/RHEL/es/elasticsearch-5.6.4.rpm
sudo rpm -Uvh elasticsearch-5.6.4.rpm
sudo wget http://platform.cogdevops.com/insights_install/installationScripts/latest/RHEL/es/ElasticSearch-5.6.4.zip
sudo unzip ElasticSearch-5.6.4.zip
sudo cp ElasticSearch-5.6.4/elasticsearch.yml /etc/elasticsearch/elasticsearch.yml
sudo cp ElasticSearch-5.6.4/log4j2.properties /etc/elasticsearch/log4j2.properties
sudo systemctl daemon-reload
sudo systemctl enable elasticsearch.service
sudo systemctl start elasticsearch.service
sleep 20
curl -XPUT 'localhost:9200/_template/neo4j-*' -d '{"order" : 0, "template": "neo4j-*","settings": {"index.number_of_shards": "5"},"mappings": {"_default_": {"dynamic_templates": [{"string_fields" : {"mapping" : {"index" : "analyzed","omit_norms" : true,"type" : "string","fields" : {"raw" : {"ignore_above" : 256,"index" : "not_analyzed","type" : "string"}}},"match_mapping_type" : "string","match" : "*"}}]}},"aliases": {}}}'

