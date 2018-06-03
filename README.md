# Cloudflare Access For Atlassian

This project impements a set of plugins for authenticating users through Cloudflare Access on Atlassian products.

Currently supported products are:
- JIRA
- Confluence
- Bitbucket

## Installation

This instructions applies to all supported Atlassian products, installed locally.
 
1. Download product plugin from [Releases](https://github.com/cloudflare/cloudflare-access-for-atlassian/releases)
1. Add the environment variables below to your server with the value from Cloudflare Access settings :
    - `CF_ACCESS_ATLASSIAN_AUDIENCE`: Token audience from your Access configuration
    - `CF_ACCESS_ATLASSIAN_ISSUER`: Token issuer, your authentication domain. Something like: `https://<Your Authentication Domain>`
    - `CF_ACCESS_ATLASSIAN_CERTS_URL`: Certificates URL. Something like `https://<Your Authentication Domain>/cdn-cgi/access/certs`
    - `CF_ACCESS_ATLASSIAN_LOGOUT_URL`: Logout URL to redirect users. Something like `https://<Your Authentication Domain>/cdn-cgi/access/logout`
    - `CF_ACCESS_ATLASSIAN_SERVICE_LOCAL_ADDRESS`: (optional) Local IP or hostname where the service is listening to. Default value is `localhost`.
    - `CF_ACCESS_ATLASSIAN_SERVICE_LOCAL_PORT`: Local Port where the service is listening to.
1. Restart the application
1. Login in the Atlassian application as administrator
1. Go to *Manage add-ons* on the administration page or menu
1. Select *Upload add-on* and upload the JAR you downloaded

After installing the plugin, you need to add the proxy certificate to your product in order to enable internal HTTPS calls:

1. Go to your Atlassian application home directory
1. Go to `cloudflare-access-atlassian-plugin`
1. Install the certificate `cfaccess-plugin.pem` into your keystore, example:  

    ```
    keytool -noprompt -import -alias "cloudflare-access-local-proxy" -file cfaccess-plugin.pem -keystore <JAVA_HOME>/lib/security/cacerts -storepass changeit        
    ```

1. Restart the Atlassian application


### Setting up Application Links

If you are using Application Links like JIRA + Bitbucket or JIRA + Confluence, you need to setup the [Bypassing Reverse Proxy](https://confluence.atlassian.com/kb/common-application-link-layouts-718835602.html) applications link layout.

1. Setup one additional unproxied connector in both applications [explained here](https://confluence.atlassian.com/kb/how-to-bypass-a-reverse-proxy-or-ssl-in-application-links-719095724.html). **Note that this connector should not be secured by Cloudflare Access**.
1. Setup the application link following [this KB](https://confluence.atlassian.com/kb/how-to-create-an-unproxied-application-link-719095740.html)  
1. When creating the link with applications already behind Access you will receive a warning asking to replace the URL as id redirected once. When this happens just replace the URL in the field with the unsecured URL.




### Helpful links

- Home Directory: [JIRA](https://confluence.atlassian.com/adminjiraserver073/jira-application-home-directory-861253888.html) [Confluence](https://confluence.atlassian.com/doc/confluence-home-and-other-important-directories-590259707.html) [Bitbucket](https://confluence.atlassian.com/bitbucketserver/bitbucket-server-home-directory-776640890.html)
- [Keytool](https://docs.oracle.com/javase/8/docs/technotes/tools/unix/keytool.html)
- Internal Proxy Motivation: [How to fix gadget titles showing as __MSG_gadget](https://confluence.atlassian.com/jirakb/how-to-fix-gadget-titles-showing-as-__msg_gadget-813697086.html)


# Troubleshooting

## Plugin upload/installation never complete

**Symptoms:**

- Plugin installation progress stuck

**Cause:**

Most likely you have a reverser proxy in front of the applicatio with a small limit for 
uploading files.

**Solution:**

Check the network panel while uploading the plugin looking for `4xx` HTTP errors. 

If you see a `HTTP 413`, you need to increase the upload file size limit on your reverse proxy.

For NGINX see [this](https://www.cyberciti.biz/faq/linux-unix-bsd-nginx-413-request-entity-too-large/). 

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
- Check that you installed the certificate on the JVM keystore

**Important:**

- Currently the internal proxy replaces any JVM proxy configuration for HTTP, soon it will chain the proxies together

## Confluence macros showing *__MSG_xxxx* title or description

**Symptoms:**

- Some macros are displaying title or description with a text starting with *__MSG_xxxx*

**Cause:**

Very likelly the macros plugin was unable to load the proper message bundles and cached the texts with placeholders.

**Solution:**

- Go to Confluence Administration page
- Go to Cache Management
- Clear all caches which name contanins "Macro" or "Gadget"

## Application Links - Network error

**Symptoms:**

- Application link shows Network Error label
- You are seeing "Connection Refused" exceptions on the log when trying to setup the link

**Cause:**

This usually would happen after a plugin update as Application Links logic caches the proxy configuration and the proxy port will change after updating the plugin.

**Solution:**

Restart the application after the update will clean the proxy cache and the application link should be back to normal.


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

# Local development/testing with Docker and NGINX

The images below are available on Docker hub for development and testing.

These images are configured to:

- Setup the context path on Tomcat
- Create a secondary connector to enable application links

**These images do not have the plugin installed, it should be installed/updated after starting them.**

I recommend having a reverse proxy in front of the Atlassian containers, with distinct paths forwarding to JIRA, Confluence and Bitbucket.

## Images

- [JIRA](https://hub.docker.com/r/felipebn/jira-cf-access-plugin-dev/)
- [Bitbucket](https://hub.docker.com/r/felipebn/bitbucket-cf-access-plugin-dev/)

## Plugin testing
 
1. Start the desired Atlassian application container ([JIRA](https://hub.docker.com/r/felipebn/jira-cf-access-plugin-dev/), [Confluence](https://hub.docker.com/r/felipebn/confluence-cf-access-plugin-dev/), [Bitbucket](https://hub.docker.com/r/felipebn/bitbucket-cf-access-plugin-dev/))
1. Download product plugin from [Releases](https://github.com/cloudflare/cloudflare-access-for-atlassian/releases)
1. Login in the Atlassian application as administrator
1. Go to *Manage add-ons* on the administration page or menu
1. Select *Upload add-on* and upload the JAR you downloaded

After installing the plugin, you need to add the proxy certificate to your product in order to enable internal HTTPS calls:

1. Attach to the running container with `docker exec -it <container_id_or_name> /bin/bash`
1. Go to `/var/atlassian/<product name in lower case>/cloudflare-access-atlassian-plugin`
1. Install the certificate `cfaccess-plugin.pem` into container JVM keystore:

    ```
    # JIRA and Confluence images
    keytool -noprompt -import -alias "cloudflare-access-local-proxy" -file cfaccess-plugin.pem -keystore /usr/lib/jvm/java-1.8-openjdk/jre/lib/security/cacerts -storepass changeit
    
    # Bitbucket image
    keytool -noprompt -import -alias "cloudflare-access-local-proxy" -file cfaccess-plugin.pem -keystore /usr/lib/jvm/java-8-openjdk-amd64/jre/lib/security/cacerts -storepass changeit
    ```
1. Stop the container with `docker stop <container id or name>`
1. Start the container with `docker start <container id or name>`
