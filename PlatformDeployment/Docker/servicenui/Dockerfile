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
# Originally written for OneDevops Insights
# To be updated as per latest software versions


FROM alpine:3.16
LABEL maintainer="Onedevops Insights"

ARG INSIGHTSRELEASE=9.7
# Creating cofig folders

RUN apk update && \
    apk add wget unzip vim && \
    apk add netcat-openbsd && \
    apk --no-cache add procps && \
    apk add --no-cache bash && \
    apk add --update openjdk11 && \
    set -ex && apk --no-cache add sudo && \
    mkdir /opt/offlineAgent && \
    mkdir /opt/PlatformAgent && \
    mkdir /usr/INSIGHTS_HOME /opt/grafana /opt/insightsengine/ /opt/InSights/  mkdir /opt/insightsWebhook/ /opt/InSights/Promtail

#Install jq - json processor
RUN wget -O jq https://github.com/stedolan/jq/releases/download/jq-1.6/jq-linux64 && \
    chmod +x ./jq && \
    cp jq /usr/bin


# Initial Setup
RUN cd /usr/INSIGHTS_HOME && \
    mkdir .InSights && \
    cd .InSights && \
    wget  https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformService/src/main/resources/server-config-template.json -O server-config.json && \
    export INSIGHTS_HOME=/usr/INSIGHTS_HOME && \
    echo INSIGHTS_HOME=/usr/INSIGHTS_HOME | tee -a /etc/environment && \
    echo "export INSIGHTS_HOME=/usr/INSIGHTS_HOME" | tee -a /etc/profile && \
    source /etc/environment && \
    source /etc/profile && \
        wget https://github.com/mikefarah/yq/releases/download/v4.11.1/yq_linux_amd64 -O /usr/bin/yq &&\
    chmod +x /usr/bin/yq
ENV INSIGHTS_HOME=/usr/INSIGHTS_HOME
RUN wget https://github.com/CognizantOneDevOps/Insights/releases/download/v${INSIGHTSRELEASE}/PlatformService-${INSIGHTSRELEASE}.war -O /opt/PlatformService.war && \
    wget https://github.com/CognizantOneDevOps/Insights/releases/download/v${INSIGHTSRELEASE}/PlatformUI3-${INSIGHTSRELEASE}.zip -O /opt/PlatformUI3.zip && \
    wget https://github.com/CognizantOneDevOps/Insights/releases/download/v${INSIGHTSRELEASE}/PlatformUI4-${INSIGHTSRELEASE}.zip -O /opt/PlatformUI4.zip && \
    unzip /opt/PlatformUI3.zip -d /opt && \
    rm -rf /opt/PlatformUI3.zip && \
    unzip /opt/PlatformUI4.zip -d /opt && \
    rm -rf /opt/PlatformUI4.zip && \
    wget -O /opt/apache-tomcat.tar.gz https://archive.apache.org/dist/tomcat/tomcat-9/v9.0.68/bin/apache-tomcat-9.0.68.tar.gz && \
    tar -zxvf /opt/apache-tomcat.tar.gz --directory /opt && rm -rf apache-tomcat.tar.gz && \
    mv /opt/apache-tomcat-9.0.68 /opt/apache-tomcat
EXPOSE 8080/tcp

WORKDIR /opt/apache-tomcat
RUN cp -r /opt/app /opt/apache-tomcat/webapps/app && cp /opt/PlatformService.war /opt/apache-tomcat/webapps/ && \
    cp -r /opt/insights /opt/apache-tomcat/webapps/  && \
    rm -rf /opt/PlatformService.war /opt/app && chmod -R 755 /opt/apache-tomcat/ && \
    rm -rf /opt/PlatformService.war /opt/insights

WORKDIR /opt/InSights/Promtail
RUN mkdir /lib64 && ln -s /lib/libc.musl-x86_64.so.1 /lib64/ld-linux-x86-64.so.2 && \
    wget https://github.com/grafana/loki/releases/download/v2.4.2/promtail-linux-amd64.zip && \
    unzip "promtail-linux-amd64.zip" && \
    rm -r -f promtail-linux-amd64.zip && \
    chmod a+x "promtail-linux-amd64" && \
        echo PROMTAIL_HOME=`pwd` |  tee -a /etc/environment && \
        echo "export" PROMTAIL_HOME=`pwd` |  tee -a /etc/profile && \
        wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformDeployment/RHEL8/initscripts/InsightsPromtail.sh && \
        mv InsightsPromtail.sh /etc/init.d/InsightsPromtail && \
        chmod +x /etc/init.d/InsightsPromtail && \
        dos2unix /etc/init.d/InsightsPromtail
ENV PROMTAIL_HOME=/opt/InSights/promtail
ENV INSIGHTS_APP_ROOT_DIRECTORY=/opt/InSights
ADD promtail-local-config.yaml /opt/InSights/Promtail/


# Running entry script
WORKDIR /
ADD dockerentry.sh dockerentry.sh
RUN chmod -R 755 dockerentry.sh && dos2unix dockerentry.sh
ENTRYPOINT ["/bin/sh","/dockerentry.sh"]
