# k6 Load Testing - Java Example

The repository provides 
- Local API for load testing
- k6 Mappers
- OTLP Exporter
- InfluxDB
- Grafana

The application will parse a JSON configuration into a javascript 
file, which will be executed by k6. The `k6 run` command will be executed
**automatically** after Spring is initialized and the API has started.
By default, the API runs at `localhost:8080/books`.

[The repository is also available in **Kotlin**.](https://github.com/dqualizer/dqualizer-automated-load-testing-spike)

### Breakpoint Test

If the property `test.breakpoint` is set true, the load test will rerun with increased load after it´s finished. 
This will continue until a threshold is not met or the maximum amount of loops are reached.

**Be aware** that the test configuration needs to follow a specific pattern to make breakpoint testing possible.
[See here for more information.](docu/BreakpointConfiguration.md)

---
## SetUp

You can run the application in Docker or install k6 manually.
You can find the installation instructions here: https://k6.io/docs/get-started/installation/

The application needs a JSON configuration to create a k6 script.
The configuration will be loaded from a fake server (by default: `localhost:8080/config`).

> Please take a look at the [documentation](docu/TestConfiguration.md) for the configuration

Furthermore, all values in [application.properties](src/main/resources/application.properties) have to be defined. 
You can also use the default values.

- `otel.host`: Host to run the OpenTelemetry collector on _(default: localhost)_
- `test.output`: What file format should be used for the results (json or csv) _(default: json)_
- `test.breakpoint`: Should Breakpoint Testing be enabled _(default: false)_
- `test.loops`: How often should the test be repeated (The loops will stop, if a [threshold](https://k6.io/docs/using-k6/thresholds/) is not met) _(default: 1)_
- `path.config`: Location of the test configuration (relative to [resources](src/main/resources)) _(default: config/exampleConfig.json)_

All **created** files will be located relative to `./target/classes`.

---
### Docker

You can run the whole application with: `docker-compose up --build`

You can run all containers except the API with: `docker-compose -f docker-compose-no-api.yml up`

Generated **output** will be stored in `./docker-output`. 
You can configure all the docker containers in [docker-config](docker-config) and [env](env).

---
### OpenTelemetry

The result of the test will be saved in a local file. Those metrics will be exported via OTLP
to an OpenTelemetry Collector after the test has finished. 
The Collector further exports those metrics to InfluxDB.

---
### InfluxDB (v2)

URL: `localhost:8086`
Default login: username > k6,  password > telegraf

You can change it in the [.env](env/.env) file.
The organization, bucket and token are configured here, too.

---
### Grafana

URL: `localhost:3030`
Default login: username > admin, password > admin

You can view the test results in the dashboard: [Load_Test_Results](docker-config/grafana/my-dashboards/home.json)

If you use JSON for the test results, there will also be a visualized threshold.

---
### More Information about k6

- k6 in general: https://k6.io/docs/
- k6 options: https://k6.io/docs/using-k6/k6-options/reference/
- k6 metrics: https://k6.io/docs/using-k6/metrics/
- k6 params: https://k6.io/docs/javascript-api/k6-http/params/
- k6 responses: https://k6.io/docs/javascript-api/k6-http/response/

---
## Implemented Features

| k6 Features                | Implemented |
|----------------------------|-------------|
| Configuration (options)    | &#9745;     |
| Payload                    | &#9745;     |
| Params                     | &#9745;     |
| CSV Output                 | &#9745;     |
| JSON Output                | &#9745;     |
| Custom metrics             | &#9744;     |
| Groups                     | &#9744;     |
| k6 Cloud                   | &#9744;     |
| ######                     | ######      |
| Http GET                   | &#9745;     |
| Http POST                  | &#9745;     |
| Http PUT                   | &#9745;     |
| Http DELETE                | &#9745;     |
| Http PATCH                 | &#9744;     |
| Http HEAD                  | &#9744;     |
| Http OPTIONS               | &#9744;     |
| Http BATCH                 | &#9744;     |
| ######                     | ######      |
| GraphQL                    | &#9744;     |
| WebSocket                  | &#9744;     |
| gRPC                       | &#9744;     |
| ######                     | ######      |
| Check response status      | &#9745;     |
| Check alternative status   | &#9745;     |
| Check body min length      | &#9745;     |
| Check body includes        | &#9745;     |
| Check error_code           | &#9745;     |
| Check more body attributes | &#9744;     |
| Check error_text           | &#9744;     |
| Check cookies              | &#9744;     |
| Check headers              | &#9744;     |
| Check status_text          | &#9744;     |
| Check timings              | &#9744;     |
| Check tls_version          | &#9744;     |
| Check tls_cipher_suite     | &#9744;     |
| Check remote_ip            | &#9744;     |
| Check remote_port          | &#9744;     |
| Check ocsp                 | &#9744;     |
| ######                     | ######      |
| ... more?                  | &#9744;     |
