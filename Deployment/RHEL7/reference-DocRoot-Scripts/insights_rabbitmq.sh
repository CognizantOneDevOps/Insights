# install erlang
echo "#################### Installing Erlang , required for Rabbit MQ ####################"
sudo mkdir erlang && cd erlang
sudo wget http://platform.cogdevops.com/InSightsV1.0/rabbitmq/erlang-20.0.5-1.el6.x86_64.rpm
sudo yum install -y erlang-20.0.5-1.el6.x86_64.rpm
echo "#################### Installing Rabbit MQ with configs and user creation ####################"
sudo mkdir rabbitmq && cd rabbitmq
sudo wget http://platform.cogdevops.com/InSightsV1.0/rabbitmq/rabbitmq-server-3.6.5-1.noarch.rpm
sudo rpm --import http://platform.cogdevops.com/InSightsV1.0/rabbitmq/rabbitmq-signing-key-public.asc
sudo yum install -y rabbitmq-server-3.6.5-1.noarch.rpm
sudo wget http://platform.cogdevops.com/InSightsV1.0/rabbitmq/RabbitMQ-3.6.5.zip
sudo unzip RabbitMQ-3.6.5.zip && cd RabbitMQ-3.6.5 && sudo cp rabbitmq.config /etc/rabbitmq/
sudo chkconfig rabbitmq-server on && sudo service rabbitmq-server start
sudo rabbitmq-plugins enable rabbitmq_management
sleep 15
curl -X PUT -u guest:guest -H "Content-Type: application/json" -d '{"password":"iSight","tags":"administrator"}' "http://localhost:15672/api/users/iSight"
sleep 15
curl -X PUT -u guest:guest -H "Content-Type: application/json" -d '{"configure":".*","write":".*","read":".*"}' "http://localhost:15672/api/permissions/%2f/iSight"