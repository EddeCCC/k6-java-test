import http from 'k6/http';
import {check, sleep} from 'k6';

let config = JSON.parse(open('../config/options.json'));
let baseUrl = config.baseURL;

export let options = config.options;

export default function() {

let response0 = http.get(baseUrl + '/all');
check(response0, {
	'GET status was 200': x => x.status && x.status == 200,
	'GET body size > 0': x => x.body && x.body.length > 0,
});
sleep(1);

let response1 = http.get(baseUrl + '/1');
check(response1, {
	'GET status was 200': x => x.status && x.status == 200,
});
sleep(1);

var payload2 = {"releaseDate":"2005-10-10","author":"Julia","name":"Smart Stories"}

let response2 = http.post(baseURL + '/new', JSON.stringify(payload2), {
headers: {
'Content-Type': 'application/json',
}
});
check(response2, {
	'POST status was 201/409': x => x.status && (x.status == 201 || x.status == 409),
});
sleep(2);

var payload3 = {"releaseDate":"2015-11-10","author":"Nils","name":"Mediocre Stories"}

let response3 = http.put(baseURL + '/1', JSON.stringify(payload3), {
headers: {
'Content-Type': 'application/json',
}
});
check(response3, {
	'PUT status was 200/201': x => x.status && (x.status == 200 || x.status == 201),
});
sleep(2);

let response4 = http.del(baseUrl + '/2');
check(response4, {
	'DELETE status was 204': x => x.status && x.status == 204,
});
sleep(1);
}