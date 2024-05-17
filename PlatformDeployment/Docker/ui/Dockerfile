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
    apk add --update nodejs npm && \
    set -ex && apk --no-cache add sudo 

#Install jq - json processor
RUN wget -O jq https://github.com/stedolan/jq/releases/download/jq-1.6/jq-linux64 && \
    chmod +x ./jq && \
    cp jq /usr/bin
    

WORKDIR /opt
RUN wget https://github.com/CognizantOneDevOps/Insights/releases/download/v${INSIGHTSRELEASE}/PlatformUI4-${INSIGHTSRELEASE}.zip -O /opt/UI.zip && \
    unzip /opt/UI.zip -d /opt/UI && \
    rm -rf /opt/UI.zip && \
    cd /opt/UI/UI && \
    npm install
EXPOSE 8081/tcp

# Running entry script
WORKDIR /
ADD dockerentry.sh dockerentry.sh
RUN chmod -R 755 dockerentry.sh && dos2unix dockerentry.sh
ENTRYPOINT ["/bin/sh","/dockerentry.sh"]
