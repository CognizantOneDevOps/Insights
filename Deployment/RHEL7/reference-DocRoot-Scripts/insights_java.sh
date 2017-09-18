# Install Java
echo "#################### Installing Java with Env Variable ####################"
cd /opt/
sudo wget  http://platform.cogdevops.com/InSightsV1.0/java/jdk-8u144-linux-x64.tar.gz
sudo tar xzf jdk-8u144-linux-x64.tar.gz
export JAVA_HOME=/opt/jdk1.8.0_144
sudo echo JAVA_HOME=/opt/jdk1.8.0_144  | sudo tee -a /etc/environment
sudo echo "export" JAVA_HOME=/opt/jdk1.8.0_144 | sudo tee -a /etc/profile
export JRE_HOME=/opt/jdk1.8.0_144/jre
sudo echo JRE_HOME=/opt/jdk1.8.0_144/jre | sudo tee -a /etc/environment
sudo echo "export" JRE_HOME=/opt/jdk1.8.0_144/jre | sudo tee -a /etc/profile
export PATH=$PATH:/opt/jdk1.8.0_144/bin:/opt/jdk1.8.0_144/jre/bin
sudo echo PATH=$PATH:/opt/jdk1.8.0_144/bin:/opt/jdk1.8.0_144/jre/bin | sudo tee -a /etc/environment
sudo alternatives --install /usr/bin/java java /opt/jdk1.8.0_144/bin/java 20000
source /etc/environment
source /etc/profile

