# install ES  - Some Issues here test on new server
echo "#################### Installing Eleastic Search with configs ####################"
sudo mkdir elasticserach
cd elasticserach
sudo wget http://platform.cogdevops.com/InSightsV1.0/es/elasticsearch-2.4.1.rpm
sudo rpm -Uvh elasticsearch-2.4.1.rpm
sudo wget http://platform.cogdevops.com/InSightsV1.0/es/ElasticSearch-2.4.1.zip
sudo unzip ElasticSearch-2.4.1.zip
sudo cp ElasticSearch-2.4.1/elasticsearch.yml /etc/elasticsearch/elasticsearch.yml
sudo systemctl daemon-reload
sudo systemctl enable elasticsearch.service
sudo systemctl start elasticsearch.service
sleep 20
curl -XPUT 'localhost:9200/_template/neo4j-*' -d '{"order" : 0, "template": "neo4j-*","settings": {"index.number_of_shards": "5"},"mappings": {"_default_": {"dynamic_templates": [{"string_fields" : {"mapping" : {"index" : "analyzed","omit_norms" : true,"type" : "string","fields" : {"raw" : {"ignore_above" : 256,"index" : "not_analyzed","type" : "string"}}},"match_mapping_type" : "string","match" : "*"}}]}},"aliases": {}}}'
