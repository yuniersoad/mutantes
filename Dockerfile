FROM openjdk:8u121-jre-alpine

WORKDIR /var/app

ADD target/mutantes-1.0-SNAPSHOT.jar /var/app/mutantes.jar
ADD config.yml /var/app/config.yml

EXPOSE 8080 8081

ENTRYPOINT ["java", "-jar", "mutantes.jar", "server", "config.yml"]
