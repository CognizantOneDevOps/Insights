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
FROM python:3.10.0-alpine
MAINTAINER Onedevops Insights

ARG INSIGHTSRELEASE=9.5
# Creating cofig folders

RUN apk update && \
    apk add wget unzip  curl vim openjdk11 && \
    apk add netcat-openbsd && \
    apk add gcc musl-dev python3-dev libffi-dev openssl-dev cargo && \
    apk --no-cache add procps && \
    apk add --no-cache bash && \
    set -ex && apk --no-cache add sudo && \
    mkdir /usr/INSIGHTS_HOME /opt/insightsagents /opt/insightsagents/AgentDaemon /opt/insightsagents/PlatformAgents /opt/insightsWebhook

ENV INSIGHTS_AGENT_HOME=/opt/insightsagents

#Install jq - json processor
RUN wget -O jq https://github.com/stedolan/jq/releases/download/jq-1.6/jq-linux64 && \
    chmod +x ./jq && \
    cp jq /usr/bin

VOLUME [/usr/INSIGHTS_HOME]

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

RUN pip install pika  requests apscheduler python-dateutil xmltodict pytz requests_ntlm boto3 urllib3 neotime neo4j neobolt elasticsearch

# Installing Agents config
WORKDIR /opt/insightsagents
RUN  chmod -R 755  AgentDaemon && chmod -R 755 PlatformAgents

WORKDIR /opt/insightsagents/AgentDaemon
RUN wget https://github.com/CognizantOneDevOps/Insights/releases/download/v${INSIGHTSRELEASE}/agentdaemon.zip -O agentdaemon.zip && \
    unzip agentdaemon.zip && rm -rf agentdaemon.zip && chmod +x installdaemonagent.sh && \
    cp $INSIGHTS_AGENT_HOME/AgentDaemon/InSightsDaemonAgent.sh /etc/init.d/InSightsDaemonAgent && chmod +x /etc/init.d/InSightsDaemonAgent

# installing Insights Webhook

WORKDIR /opt/insightsWebhook/
RUN wget https://github.com/CognizantOneDevOps/Insights/releases/download/v${INSIGHTSRELEASE}/PlatformInsightsWebHook-${INSIGHTSRELEASE}.jar -O /opt/insightsWebhook/PlatformInsightsWebHook.jar  && \
    wget  https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformInsightsWebHook/src/main/resources/webhook_subscriber.properties  -O /opt/insightsWebhook/webhook_subscriber.properties && \
    chmod -R 755 /opt/insightsWebhook/

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
ENV PROMTAIL_HOME=/opt/InSights/Promtail
ENV INSIGHTS_APP_ROOT_DIRECTORY=/opt/InSights
ADD promtail-local-config.yaml /opt/InSights/Promtail/


# Running entry script
WORKDIR /
ADD dockeragent.sh dockeragent.sh
RUN chmod -R 755 dockeragent.sh && dos2unix dockeragent.sh


ENTRYPOINT [ "/bin/bash", "/dockeragent.sh" ]
#ENTRYPOINT ["/bin/sh","/dockeragent.sh"]
