import http from 'k6/http';
import {check, sleep} from 'k6';

let config = JSON.parse(open('../config/options.json'));
let baseUrl = config.baseURL;

export let options = config.options;

export default function() {

let response0 = http.get(baseUrl + '/all');
check(response0, {
	'GET status was 200': x => x.status && x.status == 200,
	'GET body size > 0': x => x.body.length > 0,
});

let response1 = http.get(baseUrl + '/1');
check(response1, {
	'GET status was 200': x => x.status && x.status == 200,
});

let response2 = http.get(baseUrl + '/3');
check(response2, {
	'GET status was 200': x => x.status && x.status == 200,
});
sleep(1);
}