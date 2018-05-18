# Cloudflare Access For Atlassian

This project impements a set of plugins for authenticating users through Cloudflare Access on Atlassian products.

Currently supported products are:
- Jira
- Confluence
- Bitbucket

## Server Setup

To be able to authorize users initially authenticated on Access you need to provide the following environment variables:
- `CF_ACCESS_ATLASSIAN_AUDIENCE`: Token audience from you Access configuration
- `CF_ACCESS_ATLASSIAN_ISSUER`: Token issuer, your authentication domain. Something like: `https://<Your Authentication Domain>`
- `CF_ACCESS_ATLASSIAN_CERTS_URL`: Certificates URL. Something like `https://<Your Authentication Domain>/cdn-cgi/access/certs`


### CSRF configuration for REST calls

REST calls are protected agains Cross Site Request Forgery (CSRF) and due to proxying requests through Cloudflare, the REST calls will fail with similar messages on Atlassian services:

```
2015-09-01 17:25:46.530585500 2015-09-01 07:25:46,530 ajp-nio-127.0.0.104-8009-exec-23 WARN anonymous 1045x1465x1 sibktb 127.0.0.1 /rest/auth/latest/session [c.a.p.r.c.security.jersey.XsrfResourceFilter] Additional XSRF checks failed for request: https://example.domain/rest/auth/latest/session , origin: https://another-origin.domain , referrer: null , credentials in request: true , allowed via CORS: false}}
```

Please refer to the following links for more details and how to fix this problem on your server:

- https://confluence.atlassian.com/kb/cross-site-request-forgery-csrf-protection-changes-in-atlassian-rest-779294918.html
- https://confluence.atlassian.com/jirakb/unable-to-create-issue-after-upgrading-to-jira-7-815588418.html


#### Development - CSRF configuration when using SSL and Atlassian SDK

The `atlas-run` command currently is not copying the scheme we define on the base Tomcat when starting the application.

The solution is to configure the local proxy (Nginx/Apache) to set the `Origin` header with a URL like `http://<Your testing domain>` 