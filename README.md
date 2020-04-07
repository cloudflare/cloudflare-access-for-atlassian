# Cloudflare Access For Atlassian

This project impements a set of plugins for authenticating users through Cloudflare Access on Atlassian products.

Currently supported products are:
- JIRA >= 7.2
- Confluence >= 6.x
- Bitbucket >= 6.x

## Installation

This instructions applies to all supported Atlassian products, installed locally.
 
1. Download product plugin from [Releases](https://github.com/cloudflare/cloudflare-access-for-atlassian/releases)
1. Login in the Atlassian application as administrator
1. Go to *Manage add-ons* on the administration page or menu
1. Select *Upload add-on* and upload the JAR you downloaded
1. Go to *System configuration* or administration page
1. Go to *Cloudflare Access* menu on the left side menu
1. Setup your Cloudflare Access and server details


### Setting up Application Links

If you are using Application Links like JIRA + Bitbucket or JIRA + Confluence, you need to setup the [Bypassing Reverse Proxy](https://confluence.atlassian.com/kb/common-application-link-layouts-718835602.html) applications link layout.

1. Setup one additional unproxied connector in both applications [explained here](https://confluence.atlassian.com/kb/how-to-bypass-a-reverse-proxy-or-ssl-in-application-links-719095724.html). **Note that this connector should not be secured by Cloudflare Access**.
1. Setup the application link following [this KB](https://confluence.atlassian.com/kb/how-to-create-an-unproxied-application-link-719095740.html)  
1. When creating the link with applications already behind Access you will receive a warning asking to replace the URL as id redirected once. When this happens just replace the URL in the field with the unsecured URL.


### Helpful links

- Home Directory: [JIRA](https://confluence.atlassian.com/adminjiraserver073/jira-application-home-directory-861253888.html) [Confluence](https://confluence.atlassian.com/doc/confluence-home-and-other-important-directories-590259707.html) [Bitbucket](https://confluence.atlassian.com/bitbucketserver/bitbucket-server-home-directory-776640890.html)

# Troubleshooting

## Locked out: no user is able to access the application

**Symptoms:**

- No user, even the administrator, is able to access the Atlassian application even being authenticated on Cloudflare Access

**Cause:**

- Plugin misconfigutation on Atlassian application; OR
- Changes on Cloudflare Access configuration;

**Solution:**

Restart the application with `cloudflareAccessPlugin.filters.disabled` flag set to `true` and verify the plugin configuration against Cloudflare Access configuration.

To change the flag include the following in your system `JAVA_OPTS` environment variable:

```
-DcloudflareAccessPlugin.filters.disabled=true
```
After updating your system `JAVA_OPTS` restart the Atlassian application, you will be able to login with your application credentials and verify the configuration.

After verifying the configuration you should remove the flag and restart the application.

## Plugin upload/installation never complete

**Symptoms:**

- Plugin installation progress stuck

**Cause:**

Most likely you have a reverse proxy in front of the applicatio with a small limit for 
uploading files.

**Solution:**

Check the browser network panel while uploading the plugin looking for `4xx` HTTP errors. 

If you see a `HTTP 413`, you need to increase the upload file size limit on your reverse proxy.

For NGINX see [this](https://www.cyberciti.biz/faq/linux-unix-bsd-nginx-413-request-entity-too-large/). 

## JIRA Gadgets not working after installing the plugin

**Symptoms:**

- *System Dashboard* page is empty
- User profile page displays an error message on the *Activity Stream* gadget

**Cause:**

JIRA requests some URL internally through HTTP. Using Access this requests will require authentication but JIRA does not provide any means of passing authentication for this requests.

**Solution:**

Go to your Cloudflare Access configuration and create a policy to bypass requests containing `/rest/gadgets/`, example:

> If your main policy path is `/jira` you should create a new one setting the path as `/jira/rest/gadgets/` and containing a bypass policy for everyone.

## Confluence macros showing *__MSG_xxxx* title or description

**Symptoms:**

- Some macros are displaying title or description with a text starting with *__MSG_xxxx*

**Cause:**

Very likelly the macros plugin was unable to load the proper message bundles and cached the texts with placeholders.

**Solution:**

- Go to Confluence Administration page
- Go to Cache Management
- Clear all caches which name contanins "Macro" or "Gadget"

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

<details><summary>Sample NGINX configuration:</summary>
<p>

```
server {
    listen 80 default_server;
    server_name <yourservename.com>;

    location /jira {
        proxy_pass http://localhost:8080/jira;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /confluence {
        proxy_pass http://localhost:8090/confluence;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /bitbucket {
        proxy_pass http://localhost:7990/bitbucket;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

}
```

</p>
</details>

## Images

- [JIRA](https://hub.docker.com/r/felipebn/jira-cf-access-plugin-dev/)
- [Confluence](https://hub.docker.com/r/felipebn/confluence-cf-access-plugin-dev/)
- [Bitbucket](https://hub.docker.com/r/felipebn/bitbucket-cf-access-plugin-dev/)

## Plugin testing
 
1. Start the desired Atlassian application container ([JIRA](https://hub.docker.com/r/felipebn/jira-cf-access-plugin-dev/), [Confluence](https://hub.docker.com/r/felipebn/confluence-cf-access-plugin-dev/), [Bitbucket](https://hub.docker.com/r/felipebn/bitbucket-cf-access-plugin-dev/))
1. Download product plugin from [Releases](https://github.com/cloudflare/cloudflare-access-for-atlassian/releases)
1. Follow the [installation instructions](#installation)
