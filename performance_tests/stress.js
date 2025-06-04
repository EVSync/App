import http from 'k6/http';
import { check } from 'k6';

export let options = {
    stages: [
        { duration: '1m', target: 50 },
        { duration: '1m', target: 150 },
        { duration: '1m', target: 300 },
        { duration: '1m', target: 0 },
    ],
};

export default function () {
    const res = http.get('http://backend:8080/charging-station');
    check(res, {
        'status is 200': (r) => r.status === 200,
    });
}