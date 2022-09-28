import http from "k6/http";
import {check, sleep} from "k6";


export const options = {
   vus: 2,
   duration: "3s",
   iterations: 2,
   ext: {
    loadimpact: {
        projectID: 3602786,
        name: "Sample API Test"
    }
   }
}

var payload = {
    "name": "eddy",
    "job": "trainee"
}

var putPayload = {
    "name": "Jim",
    "job": "Manager"
}

export default function() {
    let getResponse = http.get("https://reqres.in/api/users/2");
    let postResponse = http.post("https://reqres.in/api/users", JSON.stringify(payload), { 
        // Kann auch ohne headers ausgeführt werden
        headers: {
            "Content-Type": "application/json"
        }
    });
    let putResponse = http.put("https://reqres.in/api/users/2", JSON.stringify(putPayload), { 
        headers: {
            "Content-Type": "application/json"
        }
    })
    let deleteResponse = http.del("https://reqres.in/api/users/2");

    check(getResponse, {
        "GET status was 200": x => x.status == 200,
        "GET has data": x => (JSON.parse(x.body)).data.id != undefined,
        "GET body size > 100": x => x.body.length > 100,
        "GET content check": x => x.body.includes("id")
    });

    check(postResponse, {
        "POST status was 201": x => x.status == 201,
        "POST Body not empty": x => x.body.length > 0
    });
    console.log("POST BODY: " + postResponse.body);

    check(putResponse, {
        "PUT status was 200/201": x => x.status == 200 || x.status == 201,
        "PUT Body not empty": x => x.body.length > 0
    });

    check(deleteResponse, {
        "DEL status was 204": x => x.status == 204
    })

    sleep(1);
}