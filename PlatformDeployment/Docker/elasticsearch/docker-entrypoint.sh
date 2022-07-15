#!/bin/bash
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

set -e

umask 0002

declare -a es_opts

while IFS='=' read -r envvar_key envvar_value
do
    # Elasticsearch env vars need to have at least two dot separated lowercase words, e.g. `cluster.name`
    if [[ "$envvar_key" =~ ^[a-z0-9_]+\.[a-z0-9_]+ ]]; then
        if [[ ! -z $envvar_value ]]; then
          es_opt="-E${envvar_key}=${envvar_value}"
          es_opts+=("${es_opt}")
        fi
    fi
done < <(env)

export JAVA_HOME=$(dirname "$(dirname "$(readlink -f "$(which javac || which java)")")")
export ES_JAVA_OPTS="-Des.cgroups.hierarchy.override=/ $ES_JAVA_OPTS"

# Determine if x-pack is enabled
if bin/elasticsearch-plugin list -s | grep -q x-pack; then
    if [[ -n "$ELASTIC_PASSWORD" ]]; then
        [[ -f config/elasticsearch.keystore ]] ||  bin/elasticsearch-keystore create
        echo "$ELASTIC_PASSWORD" | bin/elasticsearch-keystore add -x 'bootstrap.password'
    fi
fi

# Add elasticsearch as command if needed
if [ "${1:0:1}" = '-' ]; then
	set -- elasticsearch "$@"
fi

# Drop root privileges if we are running elasticsearch
# allow the container to be started with `--user`
if [ "$1" = 'elasticsearch' -a "$(id -u)" = '0' ]; then
	# Change the ownership of user-mutable directories to elasticsearch
	chown -R elasticsearch:elasticsearch /usr/share/elasticsearch/{data,logs}

	set -- su-exec elasticsearch "$@" "${es_opts[@]}"
fi

exec "$@"