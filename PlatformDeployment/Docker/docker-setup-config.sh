#!/bin/bash
#-------------------------------------------------------------------------------
# Copyright 2024 Cognizant Technology Solutions
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

# Function to check if a host is reachable
check_reachability() {
  host="$1"
  timeout 1 bash -c "ping -c 1 $host" >/dev/null 2>&1 && echo "$host is reachable" || echo "$host is unreachable"
}

# Function to check if a port is open
check_port() {
  host="$1"
  port="$2"
  timeout 1 bash -c "</dev/tcp/$host/$port" >/dev/null 2>&1 && echo "$port is open" || echo "$port is closed"
}

generate_ssl_conf() {
APP_SERVER_PRIVATE_IP=$1
APP_SERVER_PUBLIC_IP=$2
read -p "Enter SSL_CERTIFICATE_PATH (Default: /etc/ssl/certs/apache-selfsigned.crt): " SSL_CERTIFICATE_PATH
SSL_CERTIFICATE_PATH=${SSL_CERTIFICATE_PATH:-/etc/ssl/certs/apache-selfsigned.crt}
read -p "Enter SSL_KEY_PATH (Default: /etc/ssl/private/apache-selfsigned.key): " SSL_KEY_PATH
SSL_KEY_PATH=${SSL_KEY_PATH:-/etc/ssl/private/apache-selfsigned.key}

# Write to config.env file
cat <<EOF >ssl.conf
#
# When we also provide SSL we have to listen to the 
# standard HTTPS port in addition.
#
Listen 443 https

##
##  SSL Global Context
##
##  All SSL configuration in this context applies both to
##  the main server and all SSL-enabled virtual hosts.
##

#   Pass Phrase Dialog:
#   Configure the pass phrase gathering process.
#   The filtering dialog program ('builtin' is a internal
#   terminal dialog) has to provide the pass phrase on stdout.
SSLPassPhraseDialog exec:/usr/libexec/httpd-ssl-pass-dialog

#   Inter-Process Session Cache:
#   Configure the SSL Session Cache: First the mechanism 
#   to use and second the expiring timeout (in seconds).
SSLSessionCache         shmcb:/run/httpd/sslcache(512000)
SSLSessionCacheTimeout  300

#
# Use "SSLCryptoDevice" to enable any supported hardware
# accelerators. Use "openssl engine -v" to list supported
# engine names.  NOTE: If you enable an accelerator and the
# server does not start, consult the error logs and ensure
# your accelerator is functioning properly. 
#
SSLCryptoDevice builtin
#SSLCryptoDevice ubsec

##
## SSL Virtual Host Context
##

<VirtualHost _default_:443>

# General setup for the virtual host, inherited from global configuration
DocumentRoot "/var/www/html"
ServerName $APP_SERVER_PUBLIC_IP:443
Header always unset X-Frame-Options
Header set X-Frame-Options "SAMEORIGIN"
# Use separate log files for the SSL virtual host; note that LogLevel
# is not inherited from httpd.conf.
ErrorLog logs/ssl_error_log
TransferLog logs/ssl_access_log
LogLevel warn

#   SSL Engine Switch:
#   Enable/Disable SSL for this virtual host.
SSLEngine on

#   List the protocol versions which clients are allowed to connect with.
#   The OpenSSL system profile is used by default.  See
#   update-crypto-policies(8) for more details.
#SSLProtocol all -SSLv3
#SSLProxyProtocol all -SSLv3

#   User agents such as web browsers are not configured for the user's
#   own preference of either security or performance, therefore this
#   must be the prerogative of the web server administrator who manages
#   cpu load versus confidentiality, so enforce the server's cipher order.
SSLHonorCipherOrder on

#   SSL Cipher Suite:
#   List the ciphers that the client is permitted to negotiate.
#   See the mod_ssl documentation for a complete list.
#   The OpenSSL system profile is configured by default.  See
#   update-crypto-policies(8) for more details.
SSLCipherSuite PROFILE=SYSTEM
SSLProxyCipherSuite PROFILE=SYSTEM

#   Point SSLCertificateFile at a PEM encoded certificate.  If
#   the certificate is encrypted, then you will be prompted for a
#   pass phrase.  Note that restarting httpd will prompt again.  Keep
#   in mind that if you have both an RSA and a DSA certificate you
#   can configure both in parallel (to also allow the use of DSA
#   ciphers, etc.)
#   Some ECC cipher suites (http://www.ietf.org/rfc/rfc4492.txt)
#   require an ECC certificate which can also be configured in
#   parallel.
SSLCertificateFile $SSL_CERTIFICATE_PATH

#   Server Private Key:
#   If the key is not combined with the certificate, use this
#   directive to point at the key file.  Keep in mind that if
#   you've both a RSA and a DSA private key you can configure
#   both in parallel (to also allow the use of DSA ciphers, etc.)
#   ECC keys, when in use, can also be configured in parallel
SSLCertificateKeyFile $SSL_KEY_PATH

#   Server Certificate Chain:
#   Point SSLCertificateChainFile at a file containing the
#   concatenation of PEM encoded CA certificates which form the
#   certificate chain for the server certificate. Alternatively
#   the referenced file can be the same as SSLCertificateFile
#   when the CA certificates are directly appended to the server
#   certificate for convenience.
#SSLCertificateChainFile /etc/pki/tls/certs/server-chain.crt

#   Certificate Authority (CA):
#   Set the CA certificate verification path where to find CA
#   certificates for client authentication or alternatively one
#   huge file containing all of them (file must be PEM encoded)
#SSLCACertificateFile /etc/pki/tls/certs/ca-bundle.crt

#   Client Authentication (Type):
#   Client certificate verification type and depth.  Types are
#   none, optional, require and optional_no_ca.  Depth is a
#   number which specifies how deeply to verify the certificate
#   issuer chain before deciding the certificate is not valid.
#SSLVerifyClient require
#SSLVerifyDepth  10

#   Access Control:
#   With SSLRequire you can do per-directory access control based
#   on arbitrary complex boolean expressions containing server
#   variable checks and other lookup directives.  The syntax is a
#   mixture between C and Perl.  See the mod_ssl documentation
#   for more details.
#<Location />
#SSLRequire (    %{SSL_CIPHER} !~ m/^(EXP|NULL)/ \\
#            and %{SSL_CLIENT_S_DN_O} eq "Snake Oil, Ltd." \\
#            and %{SSL_CLIENT_S_DN_OU} in {"Staff", "CA", "Dev"} \\
#            and %{TIME_WDAY} >= 1 and %{TIME_WDAY} <= 5 \\
#            and %{TIME_HOUR} >= 8 and %{TIME_HOUR} <= 20       ) \\
#           or %{REMOTE_ADDR} =~ m/^192\.76\.162\.[0-9]+$/
#</Location>

#   SSL Engine Options:
#   Set various options for the SSL engine.
#   o FakeBasicAuth:
#     Translate the client X.509 into a Basic Authorisation.  This means that
#     the standard Auth/DBMAuth methods can be used for access control.  The
#     user name is the 'one line' version of the client's X.509 certificate.
#     Note that no password is obtained from the user. Every entry in the user
#     file needs this password: 'xxj31ZMTZzkVA'.
#   o ExportCertData:
#     This exports two additional environment variables: SSL_CLIENT_CERT and
#     SSL_SERVER_CERT. These contain the PEM-encoded certificates of the
#     server (always existing) and the client (only existing when client
#     authentication is used). This can be used to import the certificates
#     into CGI scripts.
#   o StdEnvVars:
#     This exports the standard SSL/TLS related 'SSL_*' environment variables.
#     Per default this exportation is switched off for performance reasons,
#     because the extraction step is an expensive operation and is usually
#     useless for serving static content. So one usually enables the
#     exportation for CGI and SSI requests only.
#   o StrictRequire:
#     This denies access when "SSLRequireSSL" or "SSLRequire" applied even
#     under a "Satisfy any" situation, i.e. when it applies access is denied
#     and no other module can change it.
#   o OptRenegotiate:
#     This enables optimized SSL connection renegotiation handling when SSL
#     directives are used in per-directory context. 
#SSLOptions +FakeBasicAuth +ExportCertData +StrictRequire
<FilesMatch "\.(cgi|shtml|phtml|php)$">
	SSLOptions +StdEnvVars
</FilesMatch>
<Directory "/var/www/cgi-bin">
	SSLOptions +StdEnvVars
</Directory>

#   SSL Protocol Adjustments:
#   The safe and default but still SSL/TLS standard compliant shutdown
#   approach is that mod_ssl sends the close notify alert but doesn't wait for
#   the close notify alert from client. When you need a different shutdown
#   approach you can use one of the following variables:
#   o ssl-unclean-shutdown:
#     This forces an unclean shutdown when the connection is closed, i.e. no
#     SSL close notify alert is sent or allowed to be received.  This violates
#     the SSL/TLS standard but is needed for some brain-dead browsers. Use
#     this when you receive I/O errors because of the standard approach where
#     mod_ssl sends the close notify alert.
#   o ssl-accurate-shutdown:
#     This forces an accurate shutdown when the connection is closed, i.e. a
#     SSL close notify alert is sent and mod_ssl waits for the close notify
#     alert of the client. This is 100% SSL/TLS standard compliant, but in
#     practice often causes hanging connections with brain-dead browsers. Use
#     this only for browsers where you know that their SSL implementation
#     works correctly. 
#   Notice: Most problems of broken clients are also related to the HTTP
#   keep-alive facility, so you usually additionally want to disable
#   keep-alive for those clients, too. Use variable "nokeepalive" for this.
#   Similarly, one has to force some clients to use HTTP/1.0 to workaround
#   their broken HTTP/1.1 implementation. Use variables "downgrade-1.0" and
#   "force-response-1.0" for this.
BrowserMatch "MSIE [2-5]" \\
		 nokeepalive ssl-unclean-shutdown \\
		 downgrade-1.0 force-response-1.0

#   Per-Server Logging:
#   The home of a custom SSL log file. Use this when you want a
#   compact non-error SSL logfile on a virtual host basis.
CustomLog logs/ssl_request_log \\
		  "%t %h %{SSL_PROTOCOL}x %{SSL_CIPHER}x \"%r\" %b"
ProxyPreserveHost On
<Proxy balancer://grafanaHome>
BalancerMember http://$APP_SERVER_PRIVATE_IP:30000 route=route1
ProxySet lbmethod=bybusyness
</Proxy>
<Location /grafana>
Order allow,deny
Allow from all
ProxyPass balancer://grafanaHome stickysession=JSESSIONID
</Location>


<Proxy balancer://rabbitMqHome>
BalancerMember http://$APP_SERVER_PRIVATE_IP:15672 route=route1
ProxySet lbmethod=bybusyness
</Proxy>
<Location /mq>
Order allow,deny
Allow from all
ProxyPass balancer://rabbitMqHome stickysession=JSESSIONID
</Location>

<Proxy balancer://neo4jHome>
BalancerMember http://$APP_SERVER_PRIVATE_IP:7474/browser route=route1
ProxySet lbmethod=bybusyness
</Proxy>
<Location /neo4j>
Order allow,deny
Allow from all
ProxyPass balancer://neo4jHome stickysession=JSESSIONID
</Location>



<Proxy balancer://webhookHome>
BalancerMember http://$APP_SERVER_PRIVATE_IP:8981 route=route1
ProxySet lbmethod=bybusyness
</Proxy> 	  
<Location /webhook>
Order allow,deny
Allow from all
ProxyPass balancer://webhookHome stickysession=JSESSIONID
</Location>

<Proxy balancer://OneDevOpsHome>
BalancerMember http://$APP_SERVER_PRIVATE_IP:38081/insights
ProxySet lbmethod=bybusyness
</Proxy>
<Location /insights>
Order allow,deny
Allow from all
ProxyPass balancer://OneDevOpsHome stickysession=JSESSIONID
</Location>

ProxyPass "/PlatformService/" "http://$APP_SERVER_PRIVATE_IP:38080/PlatformService/"
ProxyPass /wss wss://$APP_SERVER_PUBLIC_IP:30000
ProxyPass /wss wss://$APP_SERVER_PUBLIC_IP/grafana
</VirtualHost>

# Begin copied text
# from https://cipherli.st/
# and https://raymii.org/s/tutorials/Strong_SSL_Security_On_Apache2.html

SSLCipherSuite EECDH+AESGCM:EDH+AESGCM:AES256+EECDH:AES256+EDH
SSLProtocol All -SSLv2 -SSLv3
SSLHonorCipherOrder On
# Disable preloading HSTS for now.  You can use the commented out header line that includes
# the "preload" directive if you understand the implications.
#Header always set Strict-Transport-Security "max-age=63072000; includeSubdomains; preload"
Header always set Strict-Transport-Security "max-age=63072000; includeSubdomains"
Header always set X-Frame-Options DENY
Header always set X-Content-Type-Options nosniff
# Requires Apache >= 2.4
SSLCompression off 
SSLUseStapling on 
SSLStaplingCache "shmcb:logs/stapling-cache(150000)" 
# Requires Apache >= 2.4.11
# SSLSessionTickets Off
EOF
}

#=================================== MAIN OPERATIONS ===================================
read -p "Enter APP_server_IP (private ip): " APP_server_IP
echo "If you don't have public ip give private ip below"
read -p "Enter APP_server_Domain (public ip or domain name): " APP_server_Domain
echo "If neo4j is installed in App_Server, give App_Sever private ip. Else provide Neo4j_Server ip."
read -p "Enter DB_server_IP: " DB_server_IP
Neo4j_pwd=$(systemd-ask-password "Enter Neo4j password: ")
GrafanaDB_pwd=$(systemd-ask-password "Enter Grafana password: ")
Potstgre_Pwd=$(systemd-ask-password "Enter Potstgre password: ")

# Check if servers are reachable
check_reachability "$APP_server_IP"
check_reachability "$DB_server_IP"

# Write to config.env file
cat <<EOF > config.env
INSIGHTS_NEO4J_ENDPOINT=http://$DB_server_IP:7474
INSIGHTS_NEO4J_BOLTENDPOINT=http://$DB_server_IP:7687
INSIGHTS_NEO4J_HOST=$DB_server_IP
INSIGHTS_NEO4J_PORT=7474
INSIGHTS_NEO4J_USERNAME=neo4j
INSIGHTS_NEO4J_PASSWORD=$Neo4j_pwd
NEO4J_AUTH=neo4j/$Neo4j_pwd
NEO4JLABS_PLUGINS=["apoc"]
NEO4J_apoc_trigger_enabled=true

POSTGRES_HOST=$APP_server_IP
POSTGRES_PORT=35432
POSTGRES_PASSWORD=$Potstgre_Pwd

RABBITMQ_HOST=$APP_server_IP
RABBITMQ_PORT=5672
RABBITMQ_UI_PORT=15672
RABBITMQ_USERNAME=iSight
RABBITMQ_PASSWORD=iSight

GRAFANA_ENDPOINT=https://$APP_server_Domain/grafana
GRAFANA_ENDPOINT_PRIVATE=https://$APP_server_IP/grafana
GRAFANA_HOST=$APP_server_IP
GRAFANA_PORT=30000
GRAFANA_DB_USERNAME=grafana
GRAFANA_DB_PASSWORD=$GrafanaDB_pwd

UI_ENDPOINT=https://$APP_server_Domain/insights
SERVICE_ENDPOINT=https://$APP_server_Domain
SERVICE_HOST=$APP_server_IP
SERVICE_HOST_PUBLIC=$APP_server_Domain
SERVICE_PORT=38080

PROMTAIL_LISTEN_PORT=9080
PROMTAIL_ENABLE=true
LOKI_ENABLE=true
LOKI_ENDPOINT=http://$APP_server_IP:31000
EOF

echo "config.env file updated successfully"


generate_ssl_conf "$APP_server_IP" "$APP_server_Domain"

echo "ssl.conf file generated successfully."


# Check ports on APP_server
echo "Checking ports on APP_server ($APP_server_IP)"
check_port "$APP_server_IP" 38080
check_port "$APP_server_IP" 38081
check_port "$APP_server_IP" 30000
check_port "$APP_server_IP" 38000
check_port "$APP_server_IP" 15672
check_port "$APP_server_IP" 35432
check_port "$APP_server_IP" 443

# Check ports on DB_server for APP_server
echo "Checking ports on DB_server ($DB_server_IP) for APP_server ($APP_server_IP)"
check_port "$DB_server_IP" 7474
check_port "$DB_server_IP" 7687
