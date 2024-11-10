docker stop rabbitMq
docker remove rabbitMq
docker run -d --hostname rabbitMq --name rabbitMq -p 5672:5672 -p 15672:15672 -e RABBITMQ_DEFAULT_USER=quest -e RABBITMQ_DEFAULT_PASS=quest rabbitmq:3-management

sleep 5

#run audit
mvn spring-boot:run -f Audit/ -Dspring-boot.run.arguments=--server.port=8380 &

#run worker
mvn spring-boot:run -f Worker/ -Dspring-boot.run.arguments=--server.port=8280 &
mvn spring-boot:run -f Worker/ -Dspring-boot.run.arguments=--server.port=8281 &
mvn spring-boot:run -f Worker/ -Dspring-boot.run.arguments=--server.port=8282 &
mvn spring-boot:run -f Worker/ -Dspring-boot.run.arguments=--server.port=8283 &

#start producers (by default production is stopped
mvn spring-boot:run -f Producer/ -Dspring-boot.run.arguments=--server.port=8180 &
mvn spring-boot:run -f Producer/ -Dspring-boot.run.arguments=--server.port=8181 &
mvn spring-boot:run -f Producer/ -Dspring-boot.run.arguments=--server.port=8182 &
mvn spring-boot:run -f Producer/ -Dspring-boot.run.arguments=--server.port=8183 &

#wait a while to start all services
sleep 20

curl -X 'POST' 'http://localhost:8280/timeDelayInSeconds' -H 'accept: */*' -H 'Content-Type: application/json' -d '8'
curl -X 'POST' 'http://localhost:8281/timeDelayInSeconds' -H 'accept: */*' -H 'Content-Type: application/json' -d '8'
curl -X 'POST' 'http://localhost:8282/timeDelayInSeconds' -H 'accept: */*' -H 'Content-Type: application/json' -d '8'
curl -X 'POST' 'http://localhost:8283/timeDelayInSeconds' -H 'accept: */*' -H 'Content-Type: application/json' -d '8'


#start producing
curl -X 'POST' 'http://localhost:8180/runningState' -H 'accept: */*' -H 'Content-Type: application/json' -d 'true'
curl -X 'POST' 'http://localhost:8181/runningState' -H 'accept: */*' -H 'Content-Type: application/json' -d 'true'
curl -X 'POST' 'http://localhost:8182/runningState' -H 'accept: */*' -H 'Content-Type: application/json' -d 'true'
curl -X 'POST' 'http://localhost:8183/runningState' -H 'accept: */*' -H 'Content-Type: application/json' -d 'true'


#Close current shell to close all services started in the background!