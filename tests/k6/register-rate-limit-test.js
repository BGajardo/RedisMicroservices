import http from 'k6/http'
import {check} from 'k6'

export const options = {
    vus: 1,
    iterations: 6
}

export default function(){
    const response = http.post('http://localhost:8080/auth/register', JSON.stringify({
        username: `user${__ITER}`,
        password: 'pass123'
    }),
        {headers: {'Content-Type': 'application/json'}});
    console.log(`Request ${__ITER + 1} - Status: ${response.status}`);

    if(__ITER >= 3){
        check(response, {
            'rate limit -> 429': (r) => r.status === 429
        });
    }

}