docker run -d --hostname rabbitMq --name rabbitMq -p 5672:5672 -p 15672:15672 -e RABBITMQ_DEFAULT_USER=quest -e RABBITMQ_DEFAULT_PASS=quest rabbitmq:3-management