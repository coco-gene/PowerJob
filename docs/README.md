```shell
mvn package -Dmaven.test.skip=true
# java -jar --spring.profiles.active=daily

cp powerjob-server/powerjob-server-starter/target/powerjob-server-starter-4.0.1.jar docker/pumpkinjob/powerjob-server.jar
cp powerjob-worker-agent/target/powerjob-worker-agent-4.0.1.jar docker/pumpkinjob-agent/powerjob-agent.jar

sudo docker network create --subnet=172.22.0.0/16 pumpkinjob
```