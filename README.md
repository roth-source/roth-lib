# roth-lib-java

## Testing your changes on Docker container

```bash
# From the `roth-lib-java` project root.
docker build . -t roth-lib-java-11:latest --no-cache --build-arg APP_PATH="roth-lib-java-pom"
# Now run the container on interactive mode
docker run --name roth-lib-java-11  -it  roth-lib-java-11
#
mvn clean package;
mvn clean install;
```

## Publishing a new version in CodeArtifact

The new artifacts built with JDK 11 are hosted in AWS CodeArtifact.

- Account: Common Services (891376916128)
- Repo: arn:aws:codeartifact:us-west-2:891376916128:repository/aptexx/roth-lib
- Region: us-west-2


To deploy the dependencies, make sure the terminal is connected to the Common Service account:

```shell
export AWS_DEFAULT_PROFILE=apx-common-svs
```

Get the CodeArtifact token:
```shell
export CODEARTIFACT_AUTH_TOKEN=`aws codeartifact get-authorization-token --domain aptexx --domain-owner 891376916128 --region us-west-2 --query authorizationToken --output text`
```

- Change the version in the file roth-lib-java-pom/pom.xml <global.version>
- Change the version in the JS files:
    - /roth-lib-java/roth-lib-java-template/src/main/resources/roth-lib-js-template.js
    - /roth-lib-java/roth-lib-java-template/src/main/resources/roth-lib-js.js
    - /roth-lib-java/roth-lib-java-web/src/main/resources/roth-lib-js-env.js

Run Maven Deploy
```shell
cd roth-lib-java-pom

mvn deploy --settings ../.mvn/local-settings.xml
```