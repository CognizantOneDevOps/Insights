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
ARG BASE_IMAGE=grafana-oss:9.0.6
FROM grafana/${BASE_IMAGE} as grafanaalpine

FROM alpine:latest

RUN apk update && \
    apk add wget unzip vim && \
        apk --no-cache add procps && \
    apk add netcat-openbsd && \
    set -ex && apk --no-cache add sudo && \
    mkdir /opt/grafana /usr/INSIGHTS_HOME /usr/INSIGHTS_HOME/logs

COPY --from=grafanaalpine /usr/share /opt

ENV GRAFANA_HOME=/opt/grafana

WORKDIR /opt/grafana

RUN chmod -R 766 /opt/grafana && echo GRAFANA_HOME=`pwd` | tee -a /etc/environment && echo "export" GRAFANA_HOME=`pwd` | tee -a /etc/profile && \
    mkdir /opt/Insightsrepo && cd /opt/Insightsrepo && wget https://github.com/CognizantOneDevOps/Insights/archive/refs/heads/master.zip -O Insightsrepo.zip && \
    unzip Insightsrepo.zip && \
    mkdir /opt/grafana/data && \
    mkdir /opt/grafana/data/plugins && \
    chmod -R 777 /opt/grafana/data && \
    cp -r /opt/Insightsrepo/Insights-master/PlatformGrafanaPlugins/Panels/* /opt/grafana/data/plugins/ && \
    cp -r /opt/Insightsrepo/Insights-master/PlatformGrafanaPlugins/DataSources/* /opt/grafana/data/plugins/ && \
    rm -rf /opt/Insightsrepo/* && \
    cd /opt/grafana/public/dashboards && \
    wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformGrafanaPlugins/ScriptedDashboard/iSight_ui3.js && \
    cd /opt/grafana/conf && \
    wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformGrafanaPlugins/GrafanaConf/ldap.toml && \
    wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformDeployment/RHEL8/initscripts/Grafana.sh && mv Grafana.sh /etc/init.d/Grafana && \
    chmod 755 /etc/init.d/Grafana && \
    wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformDeployment/RHEL8/initscripts/GrafanaLogRotate.sh && \
    mv GrafanaLogRotate.sh /etc/logrotate.d/GrafanaLogRotate

WORKDIR /opt/Loki
RUN wget https://github.com/grafana/loki/releases/download/v2.4.2/loki-linux-amd64.zip && \
    unzip "loki-linux-amd64.zip" && \
    rm -r -f loki-linux-amd64.zip && \
    chmod a+x "loki-linux-amd64" && \
        echo LOKI_HOME=`pwd` |  tee -a /etc/environment && \
    echo "export" LOKI_HOME=`pwd` |  tee -a /etc/profile && \
        wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformDeployment/RHEL7/initscripts/InsightsLoki.sh && \
        mv InsightsLoki.sh /etc/init.d/InsightsLoki && \
    chmod +x /etc/init.d/InsightsLoki && \
    dos2unix /etc/init.d/InsightsLoki
ADD loki-local-config.yaml /opt/Loki/

ENV LOKI_HOME=/opt/Loki
ENV INSIGHTS_APP_ROOT_DIRECTORY=/opt

#VOLUME [/opt/grafana]
EXPOSE 3000/tcp

WORKDIR /
ADD dockerentry.sh dockerentry.sh
RUN chmod -R 755 dockerentry.sh && dos2unix dockerentry.sh
ENTRYPOINT ["/bin/sh","/dockerentry.sh"]
