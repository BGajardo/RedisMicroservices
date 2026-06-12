import http from 'k6/http';
import { check } from 'k6';

export const options = {
    vus: 1,
    iterations: 65
};

export function setup() {
    const loginResponse = http.post(
        'http://localhost:8080/auth/login',
        JSON.stringify({
            username: 'user1',
            password: 'pass123'
        }),
        {
            headers: {
                'Content-Type': 'application/json'
            }
        }
    );

    check(loginResponse, {
        'login exitoso': (r) => r.status === 200
    });

    const token = loginResponse.json('accessToken');

    console.log(`Token obtenido: ${token}`);

    return { token };
}

export default function(data) {
    const response = http.get(
        'http://localhost:8080/api/products/all',
        {
            headers: {
                'Authorization': `Bearer ${data.token}`
            }
        }
    );

    console.log(`Request ${__ITER + 1} - Status: ${response.status}`);

    if (__ITER >= 61) {
        check(response, {
            'rate limit -> 429': (r) => r.status === 429
        });
    }
}