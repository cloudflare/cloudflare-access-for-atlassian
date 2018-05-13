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
