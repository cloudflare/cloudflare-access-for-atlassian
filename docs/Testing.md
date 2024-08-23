# Testing with Atlassian Docker images

Atlassian provides docker images of JIRA, Confluence and Bitbucket which are great for 
quickly testing the plugin features.

To test the Cloudflare Access setup with this plugin you need:
- a Cloudflare account with Access configuration
- an Atlassian account for getting evaluation licenses for the applications
- a running instance of the service you want to test
  - the service needs to be exposed so Access can redirect properly to it, use a reverse proxy like `caddy` or `nginx`. 

In this page you find a quick-start to start a testing instance of supported Atlassian applications, for more details 
and troubleshooting refer to Atlassian documentation.

All applications supported need `upm.plugin.upload.enabled` to enable uploading the plugin through the Plugin Manager UI.

All quick-starts provided in this page expose services on port `8080` for simplification. 


## JIRA

JIRA can be started as a docker container using:

```bash
docker run \
  -e JVM_SUPPORT_RECOMMENDED_ARGS="-Dupm.plugin.upload.enabled=true" \
  -v jiraVolume:/var/atlassian/application-data/jira \
  --name="jira" \
  -d -p 8080:8080 atlassian/jira-software
```


## Confluence

Confluence in the latest verssions also needs a database, which can be a docker container alongside the Confluence 
container.

As quick-start create a `docker-compose.yml` file with the contents below and run `docker compose up`:

```yaml
services:
  postgresql:
    image: postgres:16-alpine
    environment:
      - POSTGRES_PASSWORD=test#setup
      - POSTGRES_USER=atlassian
      - POSTGRES_DB=confluence

  confluence:
    image: atlassian/confluence:8.5.6-ubuntu-jdk17
    ports:
      - 8080:8090
      - 8081:8091
    volumes:
      - confluence-data-8:/var/atlassian/application-data/confluence
    environment:
      - JVM_SUPPORT_RECOMMENDED_ARGS="-Dupm.plugin.upload.enabled=true"

volumes:
  confluence-data-8:
```

This will take care of starting both the DB and Confluence, then follow the setup instructions using a test license and 
database configuration as:
- DB Type: Postgresql
- JDBC Url: `jdbc:postgresql://postgresql:5432/confluence`
- User: `atlassian`
- Password: `test#setup`

Depending on the host machine the setup process will take a while (> 3 minutes), if the setup wizard does not move after
the DB setup step, try opening the URL for Confluence again as it should detect it got stuck and continue from 
where it stopped.

## Bitbucket

Bitbucket can be started as a docker container using:

```bash

docker run \
  -e JVM_SUPPORT_RECOMMENDED_ARGS="-Dupm.plugin.upload.enabled=true" \
  -v bitbucketVolume:/var/atlassian/application-data/bitbucket \
  --name="bitbucket" \
  -d -p 8080:7990 -p 8089:7999 atlassian/bitbucket:8.19-ubuntu-jdk17
```
