import http from 'k6/http';
import { check } from 'k6';

export const options = {
    vus: 1,
    iterations: 1
};

export default function() {
    const response = http.post(
        'http://localhost:8080/auth/login',
        JSON.stringify({ username: 'Braulio', password: '123456' }),
        { headers: { 'Content-Type': 'application/json' } }
    );

    console.log(`Status: ${response.status}`);
    console.log(`Body: ${response.body}`);

    check(response, {
        'login exitoso -> 200': (r) => r.status === 200,
        'tiene accessToken': (r) => JSON.parse(r.body).accessToken !== undefined,
        'tiene refreshToken': (r) => JSON.parse(r.body).refreshToken !== undefined
    });
}