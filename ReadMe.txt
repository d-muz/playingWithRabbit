1. To simply run the first scenario just run: docker compose up

2. To run more instances:

    Producer
        Start: mvn spring-boot:run -f Producer/ -Dspring-boot.run.arguments=--server.port=8181
        Swagger: http://localhost:8180/swagger-ui/index.html
        To change production time rate, change the property: delayInMillis

    Worker
        Start: mvn spring-boot:run -f Worker/ -Dspring-boot.run.arguments=--server.port=8281
        Swagger: http://localhost:8280/swagger-ui/index.html

    Audit
        Start: mvn spring-boot:run -f Audit/ -Dspring-boot.run.arguments=--server.port=8381
        Swagger: http://localhost:8380/swagger-ui/index.html

    (increment port number for new instances)