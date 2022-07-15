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



FROM alpine:3.16
MAINTAINER Onedevops Insights

ARG INSIGHTSRELEASE=9.5

# Creating cofig folders

RUN apk update && \
    apk add wget unzip vim dos2unix && \
    apk add netcat-openbsd && \
    apk --no-cache add procps && \
    apk add --update openjdk11 && \
    apk add --no-cache bash && \
    set -ex && apk --no-cache add sudo && \
    mkdir /usr/INSIGHTS_HOME  /opt/insightsengine/ /opt/InSights/ /opt/insightsWorkflow/ /usr/INSIGHTS_HOME/workflowjar/ /opt/InSights/Promtail/

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
ENV INSIGHTS_WORKFLOW=/opt/insightsWorkflow
# installing Insights Engine

WORKDIR /opt/insightsengine/
RUN wget https://github.com/CognizantOneDevOps/Insights/releases/download/v${INSIGHTSRELEASE}/PlatformEngine-${INSIGHTSRELEASE}.jar -O /opt/insightsengine/PlatformEngine.jar && \
    chmod -R 755 /opt/insightsengine/PlatformEngine.jar


# installing Insights Reports - Reports jar must be inside INSIGHTS_HOME path. Used as classpath for Workflow jar
WORKDIR /usr/INSIGHTS_HOME
RUN wget https://github.com/CognizantOneDevOps/Insights/releases/download/v${INSIGHTSRELEASE}/PlatformReports-${INSIGHTSRELEASE}.jar -O /usr/INSIGHTS_HOME/workflowjar/PlatformReports.jar && \
    chmod -R 755 /usr/INSIGHTS_HOME/workflowjar/PlatformReports.jar

# installing Insights Workflow
WORKDIR /opt/insightsWorkflow/
RUN wget https://github.com/CognizantOneDevOps/Insights/releases/download/v${INSIGHTSRELEASE}/PlatformWorkflow-${INSIGHTSRELEASE}.jar -O /opt/insightsWorkflow/PlatformWorkflow.jar && \
    chmod -R 755 /opt/insightsWorkflow/ && \
    wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformDeployment/RHEL8/initscripts/InSightsWorkflow.sh && \
    mv InSightsWorkflow.sh /etc/init.d/InSightsWorkflow && \
    chmod +x /etc/init.d/InSightsWorkflow && dos2unix /etc/init.d/InSightsWorkflow

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

ENTRYPOINT ["/bin/bash","/dockerentry.sh"]
