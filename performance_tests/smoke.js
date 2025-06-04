import http from 'k6/http';
import { check } from 'k6';

export let options = {
    vus: 20,
    duration: '20s',
};

export default function () {
    const res = http.get('http://backend:8080/api/consumers');
    check(res, {
        'status is 200': (r) => r.status === 200,
    });
    const res2 = http.get('http://backend:8080/charging-station');
    check(res2, {
        'status is 200': (r) => r.status === 200,
    });
}