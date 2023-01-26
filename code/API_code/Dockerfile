####
# This Dockerfile is used in order to build a container that runs the Quarkus application in JVM mode
#
# Before building the container image run:
#
# ./gradlew build
#
# Then, build the image with:
#
# docker build -f src/main/docker/Dockerfile.jvm -t quarkus/camara-jvm .
#
# Then run the container using:
#
# docker run -i --rm -p 8080:8080 quarkus/camara-jvm
#
# If you want to include the debug port into your docker image
# you will have to expose the debug port (default 5005) like this :  EXPOSE 8080 5005
#
# Then run the container using :
#
# docker run -i --rm -p 8080:8080 quarkus/camara-jvm
#
###
FROM registry.access.redhat.com/ubi8/openjdk-11-runtime:1.14-11
ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en'

# Configure the JAVA_OPTIONS, you can add -XshowSettings:vm to also display the heap size.
ENV JAVA_OPTIONS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"

ARG version=0.1.0

LABEL maintainer="patrice.conil@orange.com"  \
    version="$version" \
    description="Camara QoD API server" \
    name="location"
    
# We make four distinct layers so if there are application changes the library layers can be re-used
COPY --chown=185 build/quarkus-app/lib/ /deployments/lib/
COPY --chown=185 build/quarkus-app/*.jar /deployments/
COPY --chown=185 build/quarkus-app/app/ /deployments/app/
COPY --chown=185 build/quarkus-app/quarkus/ /deployments/quarkus/

COPY certificates/CertificatsOrange.cer /tmp/

USER root
RUN  keytool -import -noprompt -trustcacerts -cacerts \
    -alias CertificatsOrange \
    -file /tmp/CertificatsOrange.cer \
    -storepass changeit \
     && rm -f /tmp/CertificatsOrange.cer

EXPOSE 8080
USER 185

ENTRYPOINT [ "java", "-jar", "/deployments/quarkus-run.jar" ]

