# Testing with Atlassian Docker images

Atlassian provides docker images of JIRA, Confluence and Bitbucket which are great for 
quickly testing the plugin features.

To test the Cloudflare Access setup with this plugin you need:
- a Cloudflare account with Access configuration
- an Atlassian account for getting evaluation licenses for the applications
- a running instance of the service you want to test
  - the service needs to be exposed so Access can redirect properly to it, on a public server use a reverse proxy like 
`caddy` or `nginx`, on localhost `Cloudflare Tunnels` would be a good option. 

In this page you find a quick-start to start a testing instance of supported Atlassian applications, for more details 
and troubleshooting refer to Atlassian documentation and the chosen public expose tool (e.g. `caddy` or  `Cloudflare Tunnels`).

All applications supported need `upm.plugin.upload.enabled` to enable uploading the plugin through the Plugin Manager UI.

All quick-starts provided in this page expose services on port `8080` for simplification. 

## Docker

The project contains a `docker compose` configuration to quickstart services, use the respective application profile to
start, e.g. `docker compose --profile=confluence up`.

Supported profiles are: `jira`, `confluence`, `bitbucket`.

This will take care of starting both the DB and the application, then follow the setup instructions using a test license 
and database configuration as:
- DB Type: Postgresql
- Database name: `jira` OR `confluence` OR `bitbucket`
- JDBC URL (if requested): `jdbc:postgresql://postgresql:5432/<jira OR confluence OR bitbucket>`
- User: `atlassian`
- Password: `test#setup`

Depending on the host machine the setup process will take a while (> 3 minutes), if the setup wizard does not move after
the DB setup step, try opening the root URL again as it should detect it got stuck and continue from where it stopped.


### Bitbucket public configuration

For Bitbucket, the public URL needs to be configured in the `docker-compose.yml` file accordingly as if not operations
like login will fail with XSRF errors. See the file for the configuration.
