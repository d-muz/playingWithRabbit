1. Rabbit:
    Start: docker run -d --hostname rabbitMq --name rabbitMq -p 5672:5672 -p 15672:15672 -e RABBITMQ_DEFAULT_USER=quest -e RABBITMQ_DEFAULT_PASS=quest rabbitmq:3-management
    Management: http://localhost:15672/  - log in using guest/quest

2. Producer
    Start: mvn spring-boot:run -f Producer/ -Dspring-boot.run.arguments=--server.port=8180
    Swagger: http://localhost:8180/swagger-ui/index.html
    To change production time rate, change the property: delayInMillis

3. Worker
    Start: mvn spring-boot:run -f Worker/ -Dspring-boot.run.arguments=--server.port=8280
    Swagger: http://localhost:8280/swagger-ui/index.html

4. Audit
    Start: mvn spring-boot:run -f Audit/ -Dspring-boot.run.arguments=--server.port=8380
    Swagger: http://localhost:8380/swagger-ui/index.html

