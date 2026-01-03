package com.alfarays.token.util;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public final class TokenGenerator {

    private TokenGenerator() { }

    private static final Random random = new Random();
    private static final Set<Integer> generatedTokens = new HashSet<>();

    public static String generate() {
        int token;
        do {
            token = 100000 + random.nextInt(900000);
        } while (!generatedTokens.add(token));

        return String.valueOf(token);
    }

}

