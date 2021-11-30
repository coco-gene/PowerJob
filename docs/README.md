```shell
mvn package -Dmaven.test.skip=true
# java -jar --spring.profiles.active=daily

cp powerjob-server/powerjob-server-starter/target/powerjob-server-starter-4.0.1.jar docker/pumpkinjob/powerjob-server.jar
cp powerjob-worker-agent/target/powerjob-worker-agent-4.0.1.jar docker/pumpkinjob-agent/powerjob-agent.jar

sudo docker-compose build
sudo docker-compose down
sudo docker-compose up
sudo docker-compose up -d

sudo docker-compose up -d pumpkinjob-mysql80
sudo docker-compose stop pumpkinjob-mysql80
sudo docker-compose rm pumpkinjob-mysql80

sudo docker-compose up -d pumpkinjob-mongodb
sudo docker-compose stop pumpkinjob-mongodb
sudo docker-compose rm pumpkinjob-mongodb

sudo docker-compose up -d pumpkinjob-mongo-express
sudo docker-compose stop pumpkinjob-mongo-express
sudo docker-compose rm pumpkinjob-mongo-express

sudo docker-compose up pumpkinjob-server
sudo docker-compose up -d pumpkinjob-server
sudo docker-compose stop pumpkinjob-server
sudo docker-compose rm pumpkinjob-server

sudo docker-compose up pumpkinjob-agent
sudo docker-compose up -d pumpkinjob-agent
sudo docker-compose stop pumpkinjob-agent
sudo docker-compose rm pumpkinjob-agent

sudo docker-compose up pumpkinjob-agent1
sudo docker-compose up -d pumpkinjob-agent1
sudo docker-compose stop pumpkinjob-agent1
sudo docker-compose rm pumpkinjob-agent1

sudo docker-compose logs -f pumpkinjob-server

mysql -h127.0.0.1 -P3307 -uroot -p
CREATE DATABASE IF NOT EXISTS `powerjob-daily` DEFAULT CHARSET utf8mb4;
mysql -h127.0.0.1 -P3307 -uroot -p powerjob-daily < ../others/powerjob-mysql.sql

sudo docker network create --subnet=172.22.0.0/16 pumpkinjob
```

```
docker-compose mongo-express
mongodb://root:123456@localhost/test

mongo --username "root" --password
123456
```