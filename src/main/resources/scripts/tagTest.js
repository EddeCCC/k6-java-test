import http from "k6/http";
import {check, sleep} from "k6";

export const options = {
    stages: [
        { duration: "4s", target: 10 }, // Up to 10 Users in 4s
        { duration: "2s", target: 5 }
    ],
    // Thresholds help the software to understand, wether the test was sucessful
    thresholds: {
        http_req_duration: ["p(90)<450", "p(95)<500"],
        http_req_failed: ["rate<0.05"],
        "http_req_duration{group: home}": [{  // Tag with extra thresholds (pages with this tag are affected)
            threshold: "p(95)<650",
            abortOnFail: false,
            delayAbortEval: "3s"
        }],
        "http_req_duration{group: req}": [{
            threshold: "p(95)<500",
            abortOnFail: false
        }]
    }
}

export default function() {
    const pages = ["/blog/", "/kontakt/"]

    for(const page of pages) {
        const resHome = http.get(
            'https://www.novatec-gmbh.de', 
            {tags: {group: "home", test: "t"}}  //add tag, so extra threshold will work
        );
        check(resHome, {
            "status was 200": x => x.status == 200
        });
        // Checks help more the developers to understand, wether the test was sucessful

        const resPage = http.get('https://www.novatec-gmbh.de' + page, {
            //tags: {group: "req"}
        });
        check(resPage, {
            "status was 200": (x) => x.status == 200
        })
        sleep(1);
    }
}