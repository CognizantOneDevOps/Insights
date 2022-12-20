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

# Creating cofig folders

RUN apk update && \
    apk add wget unzip vim && \
    apk add netcat-openbsd && \
    apk --no-cache add procps && \
    apk add --no-cache bash && \
    apk add --update openjdk11 && \
    set -ex && apk --no-cache add sudo && \
    mkdir /usr/INSIGHTS_HOME/ /usr/INSIGHTS_HOME/logs

RUN wget http://h2o-release.s3.amazonaws.com/h2o/rel-zipf/4/h2o-3.32.1.4.zip -O /opt/h2o-3.32.1.4.zip && \
    unzip /opt/h2o-3.32.1.4.zip -d /opt && \
    rm -rf /opt/h2o-3.32.1.4.zip  
EXPOSE 54321/tcp


# Running entry script
WORKDIR /
ADD dockerentry.sh dockerentry.sh
RUN chmod -R 755 dockerentry.sh && dos2unix dockerentry.sh
ENTRYPOINT ["/bin/sh","/dockerentry.sh"]
