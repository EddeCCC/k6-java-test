FROM loadimpact/k6:latest AS k6official
FROM openjdk:17-jdk-slim
MAINTAINER haninator

COPY target/java-k6-0.0.1-SNAPSHOT.jar k6-java-test.jar
COPY --from=k6official  /usr/bin/k6 /usr/bin/k6

ENTRYPOINT ["java","-jar","/k6-java-test.jar"]