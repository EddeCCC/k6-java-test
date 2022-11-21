FROM loadimpact/k6 AS k6
FROM maven:3.8.5-openjdk-17-slim AS build

COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN mvn install -DskipTests && mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

FROM openjdk:17-jdk-slim

ARG DEPENDENCY=target/dependency

COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app
COPY --from=k6 /usr/bin/k6 /usr/bin/k6

ENTRYPOINT ["java","-cp","app:app/lib/*","poc.JavaK6Application"]