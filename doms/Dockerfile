# -slim because of https://github.com/carlossg/docker-maven/issues/50
FROM maven:3-jdk-9-slim as build
MAINTAINER tra@kb.dk

# Mirror Nexus for caching half the internet.   -s /usr/share/maven/ref/settings-docker.xml not used.
COPY docker-settings.xml /root/.m2/settings.xml

WORKDIR /build

# Grab distribution jar from nexus
RUN mvn -B -q -Dproject.basedir=. -Dmdep.stripClassifier -Dmdep.stripVersion -Dartifact=dk.statsbiblioteket.doms.installer:doms-installer:1.26:tar.gz:installer dependency:copy
RUN find . -print
RUN tar --strip-components=1 -xf target/dependency/doms-installer.tar.gz
RUN find . -print

RUN echo "POSTGRESQL_SERVER=postgres" >> bin/setenv.sh
RUN echo "UPDATETRACKER_POSTGRESQL_SERVER=postgres" >> bin/setenv.sh
RUN echo "MPTSTORE_SERVER=postgres" >> bin/setenv.sh
RUN echo "USE_REDIS=false" >> bin/setenv.sh
# https://jdbc.postgresql.org/documentation/80/connect.html
RUN echo "XMLTAPES_OBJECTS_JDBC=jdbc:postgresql://postgres/xmltapesObjectIndex" >> bin/setenv.sh
RUN echo "XMLTAPES_DATASTREAMS_JDBC=jdbc:postgresql://postgres/xmltapesDatastreamIndex" >> bin/setenv.sh
RUN mkdir package && bin/package.sh package
RUN find . -print


# DOMS not validated upstream for newer than Java 7
FROM openjdk:7-jdk-slim
WORKDIR /
# Unpack tomcat without top directory into /tomcat
COPY --from=build /build/data/tomcat/apache-tomcat-6.0.33.zip /t.zip
RUN unzip /t.zip && rm /t.zip && mv /*tomcat* /tomcat
RUN find /tomcat -print

COPY --from=build /build/package/base-objects-ingester /
COPY --from=build /build/package/services /build/package/services
COPY --from=build /build/package/tomcat/conf/*.xml   /tomcat/conf/
RUN ln -s /build/package/services/conf/setenv.sh /tomcat/bin/setenv.sh
RUN mkdir -p /tomcat/conf/Catalina/localhost
RUN ln -s /build/package/services/conf/tomcat-context-params.xml /tomcat/conf/Catalina/localhost/context.xml.default
RUN ln -s /build/package/services/tomcat-apps/schemaStore.xml /tomcat/conf/Catalina/localhost/


CMD /bin/bash
