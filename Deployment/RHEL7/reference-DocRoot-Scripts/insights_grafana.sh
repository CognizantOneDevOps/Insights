# Install customized Grafana V4.0.2
echo "#################### Installing Grafana (running as BG process) with user creation ####################"
sudo mkdir grafana-v4.0.2
cd grafana-v4.0.2
sudo wget http://platform.cogdevops.com/InSightsV1.0/grafana/grafana-4.0.2.linux-x64.tar.gz
sudo tar -zxvf grafana-4.0.2.linux-x64.tar.gz
sudo wget http://platform.cogdevops.com/InSightsV1.0/grafana/ldap.toml
sudo cp ldap.toml ./grafana-4.0.2/conf/ldap.toml
sudo wget http://platform.cogdevops.com/InSightsV1.0/grafana/defaults.ini
sudo cp defaults.ini ./grafana-4.0.2/conf/defaults.ini
cd grafana-4.0.2
sudo nohup ./bin/grafana-server &
echo $! > grafana-pid.txt
sleep 10
curl -X POST -u admin:admin -H "Content-Type: application/json" -d '{"name":"PowerUser","email":"PowerUser@PowerUser.com","login":"PowerUser","password":"C0gnizant@1"}' http://localhost:3000/api/admin/users
sleep 10
cd ..
export GRAFANA_HOME=`pwd`
sudo echo GRAFANA_HOME=`pwd` | sudo tee -a /etc/environment
sudo echo "export" GRAFANA_HOME=`pwd` | sudo tee -a /etc/profile
source /etc/environment
source /etc/profile
