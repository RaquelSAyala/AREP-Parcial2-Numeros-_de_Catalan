package com.eci;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigInteger;

@RestController
public class MathController {

    @GetMapping("/catalan")
    public CatalanResponse catalan(@RequestParam(name = "value") int n) {
        if (n < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "value must be non-negative");
        }

        BigInteger[] c = new BigInteger[n + 1];
        c[0] = BigInteger.ONE;
        for (int i = 1; i <= n; i++) {
            BigInteger sum = BigInteger.ZERO;
            for (int j = 0; j < i; j++) {
                sum = sum.add(c[j].multiply(c[i - 1 - j]));
            }
            c[i] = sum;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= n; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(c[i].toString());
        }

        return new CatalanResponse("Secuencia de Catalan", n, sb.toString());
    }

    public record CatalanResponse(String operation, int input, String output) {
    }
}
