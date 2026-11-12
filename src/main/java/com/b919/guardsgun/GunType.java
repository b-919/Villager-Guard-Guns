package com.b919.guardsgun;

import java.util.Locale;

public enum GunType {
    RIFLE("rifle"),
    SMG("smg"),
    PISTOL("pistol"),
    SNIPER("sniper"),
    SHOTGUN("shotgun"),
    MG("mg"),
    RPG("rpg"),
    OTHER("other");

    private final String taCzName;

    GunType(String taCzName) {
        this.taCzName = taCzName;
    }

    public String getTaCzName() {
        return taCzName;
    }

    public static GunType fromTaCzType(String type) {
        if (type == null) {
            return OTHER;
        }
        String normalized = type.trim().toLowerCase(Locale.ROOT);
        for (GunType value : values()) {
            if (value.taCzName.equals(normalized)) {
                return value;
            }
        }
        return OTHER;
    }
}
