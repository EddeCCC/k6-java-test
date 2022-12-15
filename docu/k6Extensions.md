# k6 Extensions

Here you can find additional information to all used k6 extensions.
[You can also read the official documentation here](https://k6.io/docs/extensions/).

## influxDB-v2 exporter  

**k6-core** only allows to export results to influxDB-v1. If you want to export metrics to influxDB-v2,
you need to install the extension. [You can find the installation guide here](https://github.com/grafana/xk6-output-influxdb).

Be aware that only the `k6.exe` in your current directory contains the  installed extension.

---
Before you execute the `k6 run` command, you need to set the following environmental variables:

- K6_INFLUXDB_ORGANIZATION
- K6_INFLUXDB_BUCKET  
- K6_INFLUXDB_TOKEN

Furthermore, you need to pass the influxDB address as an argument to the command line.

An example command could look like this:
`.\k6.exe run -o xk6-influxdb=http://localhost:8086 script.js`

---
There is also the **Load_Test_Results_k6x** Grafana dashboard to visualize metrics that 
were directly exported via the influxDB-v2 extension.


