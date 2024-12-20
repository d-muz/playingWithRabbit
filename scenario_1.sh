docker stop rabbitMq
docker remove rabbitMq
docker run -d --hostname rabbitMq --name rabbitMq -p 5672:5672 -p 15672:15672 -e RABBITMQ_DEFAULT_USER=quest -e RABBITMQ_DEFAULT_PASS=quest rabbitmq:3-management

sleep 5
#run audit
mvn spring-boot:run -f Audit/ -Dspring-boot.run.arguments=--server.port=8380 &

#run worker
mvn spring-boot:run -f Worker/ -Dspring-boot.run.arguments=--server.port=8280 &

#start producers (by default production is stopped
mvn spring-boot:run -f Producer/ -Dspring-boot.run.arguments=--server.port=8180 &

#wait a while to start all services
sleep 20

#start producing
curl -X 'POST' 'http://localhost:8180/runningState' -H 'accept: */*' -H 'Content-Type: application/json' -d 'true'

sleep 30

#stop producing
curl -X 'POST' 'http://localhost:8180/runningState' -H 'accept: */*' -H 'Content-Type: application/json' -d 'false'

#close current shell to close all services started in the background!
