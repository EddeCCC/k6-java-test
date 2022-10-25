# k6 load testing  - Java example

The repository provides 
- API for load testing
- k6 mappers
- OTLP exporter
- Grafana visualization

The application will parse a JSON configuration into a javascript 
file, which will be executed by k6. The `k6 run` command will be executed
**automatically** after Spring is initialized and the API has started.
By default, the API runs at `localhost:8080/books`.

---
## SetUp

The application needs a JSON configuration to create a k6 script.
The configuration will be loaded from a fake server (by default: `localhost:8080/config`)

>Please take a look at the example configuration at `./src/main/resources/config/exampleConfig.json`
before writing your own configuration.


Furthermore, all environmental variables in `application.properties` have to be defined.

- `path.config`: Location of the test configuration (relative to `./src/main/resources`)
- `path.script`: Location where the javascript file will be created
- `path.output`: Location where the test results will be saved (as CSV)
- `path.logging`: Location where the console output of k6 will be logged
- `otel.host`: Host to run the OpenTelemetry collector on

All created files will be located relative to `./target/classes`

---
### Docker

You can run the whole application with: `docker-compose up --build`

You can run just the OpenTelemetry Collector with `docker-compose -f docker-compose-otel.yml up`

---
### OpenTelemetry

The result of the k6 test will be saved in a CSV file. Those metrics will be exported via OTLP
to an OpenTelemetry Collector after the test finished. 
The Collector further exports those metrics to Prometheus (`localhost:9090`)

---
### Grafana

TBD

---
## Implemented Features

Find more information about k6 here: https://k6.io/docs/

Find more information about k6 options here: https://k6.io/docs/using-k6/k6-options/reference/


| k6 Features                  | Implemented |
|------------------------------|-------------|
| Configuration (options)      | &#9745;     |
| Payload                      | &#9745;     |
| Http GET                     | &#9745;     |
| Http POST                    | &#9745;     |
| Http PUT                     | &#9745;     |
| Http DELETE                  | &#9745;     |
| Http PATCH                   | &#9744;     |
| Http HEAD                    | &#9744;     |
| Http OPTIONS                 | &#9744;     |
| Http BATCH                   | &#9744;     |
| ######                       | ######      |
| Params headers               | &#9745;     |
| Params tags                  | &#9745;     |
| Params cookies               | &#9745;     |
| Params timeout               | &#9745;     |
| Params authorization         | &#9744;     |
| Params jar                   | &#9744;     |
| Params redirects             | &#9744;     |
| Params compression           | &#9744;     |
| Params responseType          | &#9744;     |
| Params responseCallback      | &#9744;     |
| ######                       | ######      |
| Header content-type          | &#9745;     |
| Header content-length        | &#9744;     |
| Header user-agent            | &#9744;     |
| More common header responses | &#9744;     |
| ######                       | ######      |
| Check response status        | &#9745;     |
| Check alternative status     | &#9745;     |
| Check body min length        | &#9745;     |
| Check body includes          | &#9745;     |
| Check error code             | &#9745;     |
| Check error text             | &#9744;     |
| Check cookies                | &#9744;     |
| Check headers                | &#9744;     |
| Check status text            | &#9744;     |
| Check timings                | &#9744;     |
| Check tls_version            | &#9744;     |
| Check tls_cipher_suite       | &#9744;     |
| Check remote_ip              | &#9744;     |
| Check remote_port            | &#9744;     |
| Check ocsp                   | &#9744;     |
| ######                       | ######      |
| ...                          | &#9744;     |
