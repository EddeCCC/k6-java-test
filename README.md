# Proof of Concept
Creating load tests for a Java API with the help of k6

The load test will start automatically when executed.

Run Docker with: `docker-compose up --build`

Use RabbitMQ locally with: `docker run -it --rm --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3.10-management`

See RabbitMQ Management at: `localhost:15672`