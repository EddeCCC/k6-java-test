import http from "k6/http";
import {check, sleep} from "k6";

export const options = {
    stages: [
        { duration: "4s", target: 10 },
        { duration: "2s", target: 5 }
    ]
}

export default function() {
    const pages = ["/all", "/1", "/2", "/3"];

    for(const page of pages) {
        const res = http.get('http://127.0.0.1:8080/books' + page);
        check(res, {
            "status was 200": (x) => x.status == 200,
        });
        sleep(1);
    }
}