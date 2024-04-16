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