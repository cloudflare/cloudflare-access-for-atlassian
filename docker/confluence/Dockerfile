FROM cptactionhank/atlassian-confluence:latest

USER root

ENV X_PATH /confluence
ENV SERVER_XML_PATH ${CONF_INSTALL}/conf/server.xml

COPY "add_unproxied_connector.sh" "/"
RUN chmod +x /add_unproxied_connector.sh

COPY "docker-combined-entrypoint.sh" "/"
RUN chmod +x /docker-combined-entrypoint.sh

ENTRYPOINT ["/docker-combined-entrypoint.sh"]

CMD ["/opt/atlassian/confluence/bin/start-confluence.sh", "-fg"]
