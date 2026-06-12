import http from "k6/http"
import {check, sleep } from "k6"

export const options = {
    vus: 1,
    iterations: 7
}

export default function (){
    const response = http.post("http://localhost:8080/auth/login", JSON.stringify({
        username: "test",
        password: "wrongPassword"
    }),{headers: {'Content-Type': 'application/json'}});

    console.log(`Intento ${__ITER + 1} - Status: ${response.status}`);

    if(__ITER >= 5){
        check(response, {
            'IP baneada -> 429': (r) => r.status === 429
        });
    }
    sleep(0.1);
}