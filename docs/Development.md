# Plugin Development

The plugin is developed using Atlassian SDK and JDK 11 as per supported tooling.

To setup the local JVM you can use [SDKMAN!](https://sdkman.io/), if you have it installed 
run `sdk env` to switch to the right JDK.

To install the Atlassian SDK in your environment refer to Atlassian SDK setup docs.
https://developer.atlassian.com/server/framework/atlassian-sdk/set-up-the-atlassian-plugin-sdk-and-build-a-project/.

To build all modules:

```bash
atlas-mvn clean package
```

To build only the modules `common` and `base-plugin`:

```bash
atlas-mvn clean package -PnoProduct
```

This will generate the JARs for installing the plugin in each respective plugin folder e.g. for JIRA 
`./jira-plugin/target/cloudflare-access-jira-plugin-SEM_VER.jar`. This JAR file is the one that should be uploaded in
the plugin manager. The other generated files (`test.jar` and `*.obr`) can be ignored.

## Project Structure

The project is structured with each product plugin depending on the `base-plugin` module where the API and common plugin 
specific implementation exists.

The `common` module contains implementations that are unrelated with Atlassian APIs to simplify testing and the 
`base-plugin` depends on it.

For requirements specific to one of the supported Atlassian products, the implementation should exist in the respective
plugin module.


