# Cloudflare Access For Atlassian

This project impements a set of plugins for authenticating users through Cloudflare Access on Atlassian products.

Currently supported products are:
- JIRA
- Confluence
- Bitbucket

## Server Setup

This setup applies to all supported Atlassian products.

To authorize authenticated users  from Access you need to provide the following environment variables:
- `CF_ACCESS_ATLASSIAN_AUDIENCE`: Token audience from you Access configuration
- `CF_ACCESS_ATLASSIAN_ISSUER`: Token issuer, your authentication domain. Something like: `https://<Your Authentication Domain>`
- `CF_ACCESS_ATLASSIAN_CERTS_URL`: Certificates URL. Something like `https://<Your Authentication Domain>/cdn-cgi/access/certs`
- `CF_ACCESS_ATLASSIAN_LOGOUT_URL`: Logout URL to redirect users. Something like `https://<Your Authentication Domain>/cdn-cgi/access/logout`

For **JIRA** you also need to provide the following configuration in order to have Gadgets working properly:

- `CF_ACCESS_ATLASSIAN_SERVICE_LOCAL_ADDRESS`: (optional) Local IP or hostname where the service is listening to. Default value is `localhost`.
- `CF_ACCESS_ATLASSIAN_SERVICE_LOCAL_PORT`: Local Port where the service is listening to.

This environment variables may be set on your application `setenv.sh` (Tomcat) or system wide.

Remember to restart the application after setting up the environment.

## JIRA Setup

Follow these steps to install the add-on manually:

- Download `cloudflare-access-jira-plugin-*.jar` from [Releases](https://github.com/cloudflare/cloudflare-access-for-atlassian/releases/) 
- Login on JIRA as administrator
- Go to *JIRA Administration* > *Add-ons* > *Manage add-ons*
- Click on *Upload add-on*
- Upload the jar

## Confluence Setup

Follow these steps to install the add-on manually:

- Download `cloudflare-access-confluence-plugin-*.jar` from [Releases](https://github.com/cloudflare/cloudflare-access-for-atlassian/releases/) 
- Login on Confluence as administrator
- Go to *Confluence Administration* > *Add-ons* > *Manage add-ons*
- Click on *Upload add-on*
- Upload the jar


# Troubleshooting

## JIRA Gadgets not working after installing the plugin

**Symptoms:**

- *System Dashboard* page is empty
- User profile page displays an error message on the *Activity Stream* gadget

**Cause:**

JIRA requests some URL internally through HTTP. Using Access this requests will require authentication but JIRA does not provide any means of passing authentication for this requests.

The plugin includes a HTTP proxy that should be able to forward this requests to a local address and port, set on environment.

**Solution:**

- Check your environment settings, ensure that you provided the right local address and port for the server
- Try to access the URL presented in the logs from the application server (e.g. using `curl -L <URL>`)

**Important:**

- Currently the internal proxy replaces any JVM proxy configuration for HTTP, soon it will chain the proxies together
- The proxy is HTTP only, so if your application is sending HTTPS requests they won't be proxied to the local address

## CSRF configuration for REST calls

**Symptoms:**

- Some images/css/js are not loaded properly
- You are seeing similar messages on the log:

```
2015-09-01 17:25:46.530585500 2015-09-01 07:25:46,530 ajp-nio-127.0.0.104-8009-exec-23 WARN anonymous 1045x1465x1 sibktb 127.0.0.1 /rest/auth/latest/session [c.a.p.r.c.security.jersey.XsrfResourceFilter] Additional XSRF checks failed for request: https://example.domain/rest/auth/latest/session , origin: https://another-origin.domain , referrer: null , credentials in request: true , allowed via CORS: false}}
```
**Cause:**

REST calls are protected against Cross Site Request Forgery (CSRF) and as requests are proxied through Cloudflare, the REST calls will fail with similar messages on Atlassian products.

**Solution:**

Please refer to the following links for more details and how to fix this problem on your server:

- [Cross Site Request Forgery (CSRF) protection changes in Atlassian REST](https://confluence.atlassian.com/kb/cross-site-request-forgery-csrf-protection-changes-in-atlassian-rest-779294918.html)
- [Unable to create issue after upgrading to JIRA 7](https://confluence.atlassian.com/jirakb/unable-to-create-issue-after-upgrading-to-jira-7-815588418.html)

### Development Only - CSRF configuration when using SSL and Atlassian SDK

The `atlas-run` command currently is not copying the scheme defined on the base Tomcat when starting the application.

The solution is to configure the local proxy (Nginx/Apache) to set the `Origin` header with a URL like `http://<Your testing domain>` 

Also this leads to other CSRF checks where content is not returned, in that case is best to install and manage the Atlassian product manually.

# Building


Install the Atlassian SDK following instructions on [Set up the Atlassian Plugin SDK and build a project](https://developer.atlassian.com/server/framework/atlassian-sdk/set-up-the-atlassian-plugin-sdk-and-build-a-project/).
 
Install the Bintray repository for custom dependency by including the snippet below in your Maven `settings.xml`.

<details><summary>Click to expand snippet</summary>
<p>

```
<?xml version="1.0" encoding="UTF-8" ?>
<settings xsi:schemaLocation='http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd'
          xmlns='http://maven.apache.org/SETTINGS/1.0.0' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>
    <!-- ... -->
    <profiles>
        <profile>
            <repositories>
                <repository>
                    <snapshots>
                        <enabled>false</enabled>
                    </snapshots>
                    <id>bintray-felipebn-maven</id>
                    <name>bintray</name>
                    <url>https://dl.bintray.com/felipebn/maven</url>
                </repository>
            </repositories>
            <pluginRepositories>
                <pluginRepository>
                    <snapshots>
                        <enabled>false</enabled>
                    </snapshots>
                    <id>bintray-felipebn-maven</id>
                    <name>bintray-plugins</name>
                    <url>https://dl.bintray.com/felipebn/maven</url>
                </pluginRepository>
            </pluginRepositories>
            <id>bintray</id>
        </profile>
    </profiles>
    <activeProfiles>
        <activeProfile>bintray</activeProfile>
    </activeProfiles>
    <!-- ... -->
</settings>
```

</p>
</details>


To build the modules `common` and `base-plugin`:

```
atlas-mvn clean package -PnoProduct
```

To build all modules:

```
atlas-mvn clean package
```

