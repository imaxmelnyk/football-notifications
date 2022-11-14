FROM openjdk:17

LABEL maintainer = "Max Melnyk <max@maxmelnyk.dev>"

RUN groupadd -r app_user && useradd -r -g app_user app_user

WORKDIR /app

COPY target/scala-2.13/football-notifications-assembly.jar .

USER app_user

ENTRYPOINT java -jar football-notifications-assembly.jar
