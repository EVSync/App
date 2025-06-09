import http from 'k6/http';
import { check } from 'k6';

export let options = {
    stages: [
        { duration: '2m', target: 70 },
        { duration: '5m', target: 70 },
        { duration: '2m', target: 0 },
    ],
};

export default function () {
    const res = http.get('http://backend:8080/charging-station');
    check(res, {
        'status is 200': (r) => r.status === 200,
    });
}