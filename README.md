# Proof of Concept
Creating load tests for a Java API with the help of k6

The load test will start automatically when executed.

Run only the OpenTelemetry-Collector with: `docker-compose -f docker-compose-otel.yml up --build`

Run both the API and the Collector in Docker with: `docker-compose up --build`



### Current Issues

- The application does not stop sending data to the collector. You have to shut down the whole application
- Some lines of the CSV output are not unique, they cannot be exported singly