package com.b919.guardsgun;

import java.util.Locale;

public enum AmmoMode {

    INFINITEAMMO,

    INFINITEMAGS,

    NORMAL;

    public static AmmoMode fromString(String value) {
        if (value == null) {
            return NORMAL;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        for (AmmoMode mode : values()) {
            if (mode.name().equals(normalized)) {
                return mode;
            }
        }
        return NORMAL;
    }
}
