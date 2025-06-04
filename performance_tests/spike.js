import http from 'k6/http';
import { check } from 'k6';

export let options = {
    stages: [
        { duration: '10s', target: 200 },
        { duration: '30s', target: 200 },
        { duration: '10s', target: 1 },
        { duration: '30s', target: 1 },
    ],
};

export default function () {
    const res = http.get('http://backend:8080/charging-station');
    check(res, {
        'status is 200': (r) => r.status === 200,
    });
}