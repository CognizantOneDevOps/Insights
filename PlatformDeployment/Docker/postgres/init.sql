/*-------------------------------------------------------------------------------
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
*/

\set CRED `echo \'$USER_GRAFANA_PASSWORD\'`
CREATE USER grafana WITH PASSWORD :CRED SUPERUSER;
CREATE DATABASE grafana WITH OWNER grafana TEMPLATE template0 ENCODING 'SQL_ASCII' TABLESPACE  pg_default LC_COLLATE  'C' LC_CTYPE  'C' CONNECTION LIMIT  -1;
CREATE DATABASE insight WITH OWNER grafana TEMPLATE template0 ENCODING 'SQL_ASCII' TABLESPACE  pg_default LC_COLLATE  'C' LC_CTYPE  'C' CONNECTION LIMIT  -1;
