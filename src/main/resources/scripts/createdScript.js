import http from 'k6/http';
import {check, sleep} from 'k6';
let config = JSON.parse(open('../config/config.json'));
export let options = config.options;
export default function() {
let baseUrl = config.baseURL;
let user1 = config.payload[0];
let user2 = config.payload[1];
let getResponse = http.get(baseUrl + '/all');
let postResponse = http.post(baseUrl + '/new', JSON.stringify(user1), {
headers: {'Content-Type': 'application/json'}
});
let putResponse = http.put(baseUrl + '/3', JSON.stringify(user2), {
 headers: {'Content-Type': 'application/json'}
});
let deleteResponse = http.del(baseUrl + '/1');
check(getResponse, {
'GET status was 200': x => x.status == 200,
'GET body size > 100': x => x.body.length > 100,
'GET content check': x => x.body.includes('id')
});
check(postResponse, {
'POST status was 201/409': x => x.status && (x.status == 201 || x.status == 409),
'POST Body not empty': x => x.body.length > 0
});
check(putResponse, {
'PUT status was 200/201': x => x.status == 200 || x.status == 201,
'PUT Body not empty': x => x.body.length > 0
});
check(deleteResponse, {
'DEL status was 204': x => x.status == 204
});
sleep(1);
}