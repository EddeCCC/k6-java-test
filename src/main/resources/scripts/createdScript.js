import http from 'k6/http';
import {check, sleep} from 'k6';

let config = JSON.parse(open('../config/config.json'));
let baseUrl = config.baseURL;
export let options = config.options;

export default function() {

let response = http.get(baseUrl + '/all');
check(response, {
	'GET status was 200': x => x.status == 200,
});
sleep(1);
}