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
#
# Originally written for OneDevops Insights by
#   513585 && 543825

FROM centos:centos7
MAINTAINER Onedevops Insights

# Creating cofig folders

RUN yum update -y && \
    yum install -y wget unzip && \
    mkdir /usr/INSIGHTS_HOME /opt/grafana /opt/insightsengine/ /opt/python /opt/agent20 /opt/agent20/download && \
    mkdir /opt/insightsagents /opt/insightsagents/AgentDaemon /opt/insightsagents/PlatformAgents /opt/insightsWebhook/

VOLUME [/usr/INSIGHTS_HOME]

# Installing Java with Env Variable

WORKDIR /opt
RUN wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/java/jdklinux.tar.gz && \
    tar xzf jdklinux.tar.gz && rm -rf jdklinux.tar.gz && \
    export JAVA_HOME=/opt/jdklinux && \
    echo JAVA_HOME=/opt/jdklinux  | tee -a /etc/environment && \
    echo "export" JAVA_HOME=/opt/jdklinux | tee -a /etc/profile && \
    export JRE_HOME=/opt/jdklinux/jre && \
    echo JRE_HOME=/opt/jdklinux/jre | tee -a /etc/environment && \
    echo "export" JRE_HOME=/opt/jdklinux/jre | tee -a /etc/profile && \
    export PATH=$PATH:/opt/jdklinux/bin:/opt/jdklinux/jre/bin && \
    echo PATH=$PATH:/opt/jdklinux/bin:/opt/jdklinux/jre/bin | tee -a /etc/environment && \
    update-alternatives --install /usr/bin/java java /opt/jdklinux/bin/java 20000 && \
    update-alternatives --install "/usr/bin/java" "java" "/opt/jdklinux/bin/java" 1 && \
    update-alternatives --install "/usr/bin/javac" "javac" "/opt/jdklinux/bin/javac" 1 && \
    update-alternatives --install "/usr/bin/javaws" "javaws" "/opt/jdklinux/bin/javaws" 1 && \
    update-alternatives --set java /opt/jdklinux/bin/java && \
    update-alternatives --set javac /opt/jdklinux/bin/javac && \
    update-alternatives --set javaws /opt/jdklinux/bin/javaws && \
    source /etc/environment && \
    source /etc/profile

# Installing Python

WORKDIR /opt/python

RUN yum install -y gcc openssl-devel bzip2-devel make && wget https://www.python.org/ftp/python/2.7.16/Python-2.7.16.tgz && \
    tar xzf Python-2.7.16.tgz && cd Python-2.7.16 && ./configure --enable-optimizations && make altinstall && \
    shopt -s expand_aliases && echo "alias python='/usr/local/bin/python2.7'" >> ~/.bashrc && \
    wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/python/get-pip.py && \
    python get-pip.py && pip install pika requests apscheduler python-dateutil xmltodict pytz requests_ntlm boto3

#installing Postgres Client

RUN yum install -y postgresql
EXPOSE 25672/tcp 4369/tcp 5672/tcp 15672/tcp

# Installing Agents config

WORKDIR /opt/insightsagents
RUN export INSIGHTS_AGENT_HOME=`pwd` && echo INSIGHTS_AGENT_HOME=`pwd` | tee -a /etc/environment && \
    echo "export" INSIGHTS_AGENT_HOME=`pwd` | tee -a /etc/profile && source /etc/environment && source /etc/profile && \
    chmod -R 755 AgentDaemon && chmod -R 755 PlatformAgents
WORKDIR /opt/insightsagents/AgentDaemon
RUN wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/release/latest/agentdaemon.zip -O agentdaemon.zip && \
    unzip agentdaemon.zip && rm -rf agentdaemon.zip && chmod +x installdaemonagent.sh && yum install sudo -y && \
    sed -i -e "s|extractionpath|/opt/insightsagents/PlatformAgents|g" /opt/insightsagents/AgentDaemon/com/cognizant/devops/platformagents/agents/agentdaemon/config.json && \
    sudo sed -i -e "s|\$INSIGHTS_AGENT_HOME|/opt/insightsagents|g" /opt/insightsagents/AgentDaemon/InSightsDaemonAgent.sh && \
    sudo chmod +x installdaemonagent.sh
VOLUME [/opt/insightsagents/PlatformAgents]

# installing Grafana

WORKDIR /opt/grafana
RUN chmod -R 766 /opt/grafana && wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/grafana/Grafana-6.1.6/grafana.tar.gz && \
    tar -zxvf grafana.tar.gz && export GRAFANA_HOME=`pwd` && echo GRAFANA_HOME=`pwd` | sudo tee -a /etc/environment && echo "export" GRAFANA_HOME=`pwd` | sudo tee -a /etc/profile && \
    wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/grafana/Grafana-6.1.6/ldap.toml && cp ldap.toml ./conf/ldap.toml && \
    wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/grafana/Grafana-6.1.6/defaults.ini && cp defaults.ini ./conf/defaults.ini && \
    wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/grafana/Grafana-6.1.6/custom.ini && cp custom.ini ./conf/custom.ini && \
    wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/initscripts/Grafana.sh && cp Grafana.sh /etc/init.d/Grafana && \
    chmod 755 /etc/init.d/Grafana && chkconfig Grafana on
VOLUME [/opt/grafana]
EXPOSE 3000/tcp 5432/tcp 7474/tcp

# installing Apache Tomcat

COPY PlatformService/target/*.war /opt/PlatformService.war
COPY PlatformUI3/target/*.zip /opt/PlatformUI3.zip
WORKDIR /opt/
RUN unzip PlatformUI3.zip && rm -rf PlatformUI3.zip && \
    wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/tomcat/apache-tomcat.tar.gz && \
    tar -zxvf apache-tomcat.tar.gz && rm -rf apache-tomcat.tar.gz
WORKDIR /opt/apache-tomcat
RUN cp -r /opt/app /opt/apache-tomcat/webapps/ && cp /opt/PlatformService.war /opt/apache-tomcat/webapps/ && \
    rm -rf /opt/PlatformService.war /opt/app && chmod -R 755 /opt/apache-tomcat/

# installing Insights Engine

WORKDIR /opt/insightsengine/
COPY PlatformEngine/target/PlatformEn*.jar /opt/insightsengine/PlatformEngine.jar
RUN chmod -R 755 /opt/insightsengine/

# installing Insights Webhook

WORKDIR /opt/insightsWebhook/
COPY PlatformInsightsWebHook/target/PlatformInsightsWeb*.jar /opt/insightsWebhook/PlatformInsightsWebHook.jar
RUN chmod -R 755 /opt/insightsWebhook/

#installing Apache httpd
RUN yum install httpd -y
WORKDIR /etc/httpd/conf
RUN rm -f httpd.conf && wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/httpd/RHEL/http/httpd.conf
WORKDIR /etc/httpd/conf.d
RUN rm -f httpd-vhosts.conf && wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/httpd/RHEL/http/httpd-vhosts.conf
EXPOSE 80/tcp

# Running entry script
WORKDIR /
ADD dockerentry.sh dockerentry.sh
RUN chmod -R 755 dockerentry.sh
ENTRYPOINT ["/dockerentry.sh"]
