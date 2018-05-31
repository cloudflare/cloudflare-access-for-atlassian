FROM cptactionhank/atlassian-jira:latest

USER root

ENV X_PATH /jira
ENV SERVER_XML_PATH ${JIRA_INSTALL}/conf/server.xml

COPY "add_unproxied_connector.sh" "/"
RUN chmod +x /add_unproxied_connector.sh

COPY "docker-combined-entrypoint.sh" "/"
RUN chmod +x /docker-combined-entrypoint.sh

ENTRYPOINT ["/docker-combined-entrypoint.sh"]

CMD ["/opt/atlassian/jira/bin/start-jira.sh", "-fg"]
