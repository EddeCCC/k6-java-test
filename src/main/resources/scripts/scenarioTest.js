import http from "k6/http";
import {check, sleep} from "k6";


export const options = {
    // See more about scenarios here: https://k6.io/docs/using-k6/scenarios/
    scenarios: {
        scenario_1: {
            executor: "shared-iterations", // neccessary
            startTime: "1s",
            gracefulStop: "4s",
            vus: 4,
            iterations: 4,
        },
        scenario_2: {
            executor: "constant-vus",
            vus: 2,
            duration: "5s"
        }
    }
}

export default function() {

    let res = http.get("https://k6.io");

    check(res, {
        "Status": x => x.status == 200
    });
   
    sleep(1);
}