#!/bin/bash

# Creates a new unproxied connector like:
#
# <Connector
#               port="17990"
#               connectionTimeout="20000"
#               maxThreads="200"
#               minSpareThreads="10"
#               enableLookups="false"
#               acceptCount="10"
#               URIEncoding="UTF-8" />

xmlstarlet ed \
-P -S -L \
--subnode /Server/Service \
--type elem \
-n ProxyConnector \
-v "" \
-i /Server/Service/ProxyConnector -t attr -n "port" -v "17990" \
-i /Server/Service/ProxyConnector -t attr -n "connectionTimeout" -v "20000" \
-i /Server/Service/ProxyConnector -t attr -n "maxThreads" -v "200" \
-i /Server/Service/ProxyConnector -t attr -n "minSpareThreads" -v "10" \
-i /Server/Service/ProxyConnector -t attr -n "enableLookups" -v "false" \
-i /Server/Service/ProxyConnector -t attr -n "acceptCount" -v "10" \
-i /Server/Service/ProxyConnector -t attr -n "URIEncoding" -v "UTF-8" \
-r /Server/Service/ProxyConnector -v Connector \
${SERVER_XML_PATH}
