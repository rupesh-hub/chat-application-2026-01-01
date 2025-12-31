package com.alfarays.util;

import lombok.NonNull;

public final class Util {

    private Util() {
    }

    public static String capitalize(@NonNull String value) {
        if(value.isBlank()) return "";

        value = value.trim().toLowerCase();
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

}
