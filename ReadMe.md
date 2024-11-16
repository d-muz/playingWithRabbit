
# RabbitMQ Consumer/Producer/Audit sample system

## Building:
The project can be built using Maven wrapper (without locally installed Maven):
- for Linux user: `./mvnw clean install`
- for Windows user: `mvnw.cmd clean install`

If there are maven binaries (version 3.6+) in the system path, simply run: `mvn clean install`

## Building and running docker containers:
The Project contains a docker compose definition. To build all images execute command:
`docker compose build`

To start all services in basic scenario execute:
`docker compose up`

## API and configuration
Each service supports the Swagger UI, which describes the API and assists in its use.
Below are the URLs for the basic scenario:
- Producer: [http://localhost:8180/swagger-ui/index.html](http://localhost:8180/swagger-ui/index.html)
- Worker: [http://localhost:8280/swagger-ui/index.html](http://localhost:8280/swagger-ui/index.html)
- Audit: [http://localhost:8380/swagger-ui/index.html](http://localhost:8380/swagger-ui/index.html)

## Test scenarios

The project provides scripts for 4 test scenarios. 
To run a scenario, you need to run the corresponding shell script: ./scenario_1|2|3|4.sh


## Starting services manually

To start RabbitMQ server, run script: `startRabbit.bat` or `startRabbit.sh`

New service instance can be started manually using Maven command, as below:

- Producer: `mvn spring-boot:run -f Producer/ -Dspring-boot.run.arguments=--server.port=8181`
- Worker: `mvn spring-boot:run -f Worker/ -Dspring-boot.run.arguments=--server.port=8281`
- Audit: `mvn spring-boot:run -f Audit/ -Dspring-boot.run.arguments=--server.port=8381`

If you do not have Maven binaries in the system PATH, use the wrapper ‘./mvnw’ or ‘mvnw.cmd’ instead of "mvn" and remember to increment the port number for each new instance.

The Swagger interface URL for the new instance is as follows: `http://localhost:PORT_NUMBER/swagger-ui/index.html `, where the correct port number should be used.

