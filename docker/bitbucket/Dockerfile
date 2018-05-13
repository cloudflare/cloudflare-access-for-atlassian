FROM cptactionhank/atlassian-bitbucket:latest

USER root

ENV X_PATH /bitbucket
ENV SERVER_XML_PATH ${BITBUCKET_INSTALL}/conf/server.xml

COPY "add_unproxied_connector.sh" "/"
RUN chmod +x /add_unproxied_connector.sh

COPY "docker-combined-entrypoint.sh" "/"
RUN chmod +x /docker-combined-entrypoint.sh

ENTRYPOINT ["/docker-combined-entrypoint.sh"]

CMD ["/opt/atlassian/bitbucket/bin/catalina.sh", "run"]
