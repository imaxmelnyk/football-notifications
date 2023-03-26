# Football notifications
This project is a [telegram](https://telegram.org) bot that sends football notifications.

### Subscribe
In order to subscribe to the desired team, send the following command to the bot:
```
/subscribe [team name]
```
This going to trigger team search, where you can choose the exact team you want to subscribe to.

### Un-subscribe
In order to unsubscribe from the undesired team, send the following command to the bot:
```
/unsubscribe
```
This going to show the list of the teams you are subscribed to,
where you can choose the exact team you want to unsubscribe from.


## Local Run
There is a [docker compose](https://docs.docker.com/compose/) you can use to run project dependencies locally.  
To run it (assuming you are in the project root folder):
```bash
docker-compose -f local/docker-compose.yaml up -d
```
After this, you can run [the project main file](src/main/scala/dev/maxmelnyk/footballnotifications/Main.scala)
directly in your IDEA
or run [the docker container](https://hub.docker.com/repository/docker/imaxmelnyk/football-notifications/general).  
Don't forget to specify [environment variables](src/main/resources/application.conf).
