# -slim because of https://github.com/carlossg/docker-maven/issues/50
FROM maven:3-jdk-9-slim as build
MAINTAINER tra@kb.dk

# Mirror Nexus for caching half the internet.   -s /usr/share/maven/ref/settings-docker.xml not used.
COPY docker-settings.xml /root/.m2/settings.xml

WORKDIR /build

# Grab distribution jar from nexus
RUN mkdir bitrepository
RUN mvn -B -q -Dproject.basedir=. -Dmdep.stripClassifier -Dmdep.stripVersion -Dartifact=org.bitrepository.reference:bitrepository-reference-pillar:1.9.0.1:tar.gz:distribution dependency:copy
#RUN find . -type f -print
RUN tar --strip-components=1 -C bitrepository -xf target/dependency/bitrepository-reference-pillar.tar.gz
#RUN find . -type f -print



FROM openjdk:9-jdk-slim
WORKDIR /app
COPY --from=build /build/bitrepository .
#RUN find . -type f -print

CMD /bin/bash
