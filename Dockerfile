FROM fedora:latest AS fedora

RUN dnf -y install https://dl.k6.io/rpm/repo.rpm && dnf -y install k6

FROM maven:3.8.5-openjdk-17 AS build

COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN mvn install -DskipTests && mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

FROM openjdk:17-oracle

VOLUME /tmp

ARG DEPENDENCY=target/dependency

COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app
COPY --from=fedora /usr/bin/k6 /usr/bin/k6

ENTRYPOINT ["java","-cp","app:app/lib/*","poc.JavaK6Application"]