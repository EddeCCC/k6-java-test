import http from "k6/http";
import {check, sleep} from "k6";

export const options = {
   vus: 2,
   duration: "3s",
   iterations: 2,
}

var postPayload = {
    "name": "Smart Stories",
    "author": "Julia",
    "releaseDate": "2005-10-10"
}

var putPayload = {
    "name": "Mediocore Stories",
    "author": "Nils",
    "releaseDate": "2015-11-10"
}

const baseURL = "http://localhost:8080/books"

export default function() {
    let getResponse = http.get(baseURL + "/all");
    let postResponse = http.post(baseURL + "/new", JSON.stringify(postPayload), { 
        headers: {
            "Content-Type": "application/json"
        }
    });
    let putResponse = http.put(baseURL + "/3", JSON.stringify(putPayload), { 
        headers: {
            "Content-Type": "application/json"
        }
    });
    let deleteResponse = http.del(baseURL + "/1");

    check(getResponse, {
        "GET status was 200": x => x.status == 200,
        "GET body size > 100": x => x.body.length > 100,
        "GET content check": x => x.body.includes("id")
    });

    check(postResponse, {
        "POST status was 201": x => x.status == 201,
        "POST Body not empty": x => x.body.length > 0
    });

    check(putResponse, {
        "PUT status was 200/201": x => x.status == 200 || x.status == 201,
        "PUT Body not empty": x => x.body.length > 0
    });

    check(deleteResponse, {
        "DEL status was 204": x => x.status == 204
    })

    sleep(1);
}