```shell
mvn package -Dmaven.test.skip=true
# java -jar --spring.profiles.active=daily
# java -jar --spring.profiles.active=pre

cp powerjob-server/powerjob-server-starter/target/powerjob-server-starter-4.0.1.jar docker/pumpkinjob/powerjob-server.jar
cp powerjob-worker-agent/target/powerjob-worker-agent-4.0.1.jar docker/pumpkinjob-agent/powerjob-agent.jar

sudo rm -rf docker/pumpkinjob/powerjob-server.jar
sudo rm -rf docker/pumpkinjob-agent/powerjob-agent.jar

cd docker
cp -rf ../powerjob-server/powerjob-server-starter/target/*.jar pumpkinjob/powerjob-server.jar
cp -rf ../powerjob-worker-agent/target/*.jar pumpkinjob-agent/powerjob-agent.jar

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

sudo docker-compose build pumpkinjob-server
sudo docker-compose build pumpkinjob-agent
sudo docker-compose build pumpkinjob-agent1

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
root
CREATE DATABASE IF NOT EXISTS `powerjob-daily` DEFAULT CHARSET utf8mb4;
CREATE DATABASE IF NOT EXISTS `powerjob-pre` DEFAULT CHARSET utf8mb4;
mysql -h127.0.0.1 -P3307 -uroot -p powerjob-daily < ../others/powerjob-mysql.sql
mysql -h127.0.0.1 -P3307 -uroot -p powerjob-pre < ../others/powerjob-mysql.sql

select * from app_info;
select * from container_info;
select * from instance_info;
select * from job_info;
select * from oms_lock;
select * from server_info;
select * from user_info;
select * from workflow_info;
select * from workflow_instance_info;
select * from workflow_node_info;

truncate table app_info;
truncate table container_info;
truncate table instance_info;
truncate table job_info;
truncate table oms_lock;
truncate table server_info;
truncate table user_info;
truncate table workflow_info;
truncate table workflow_instance_info;
truncate table workflow_node_info;

sudo docker network create --subnet=172.22.0.0/16 pumpkinjob


docker run -d \
         --name powerjob-server \
         -p 7700:7700 -p 10086:10086 -p 5001:5005 -p 10001:10000 \
         -e JVMOPTIONS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=10000 -Dcom.sun.management.jmxremote.rmi.port=10000 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false" \
         -e PARAMS="--spring.profiles.active=pre" \
         -e TZ="Asia/Shanghai" \
         -v ~/git/PowerJob/docker/data/pumpkinjob/powerjob:/home/pumpkinjob/powerjob -v ~/.m2:/root/.m2 \
         docker_pumpkinjob-server

docker run -d \
         --name powerjob-server \
         -p 7700:7700 -p 10086:10086 -p 5001:5005 -p 10001:10000 \
         -e JVMOPTIONS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=10000 -Dcom.sun.management.jmxremote.rmi.port=10000 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false" \
         -e PARAMS="--spring.profiles.active=pre" \
         -e TZ="Asia/Shanghai" \
         -v ~/docker/powerjob-server:/root/powerjob-server -v ~/.m2:/root/.m2 \
         docker_pumpkinjob-server
         
docker rm powerjob-server

docker rmi `docker images|grep none |  awk '{print $3}'`
```

```
docker-compose mongo-express

mongo --username "root" --password
123456

tech.powerjob.official.processors.impl.script.ShellProcessor
```