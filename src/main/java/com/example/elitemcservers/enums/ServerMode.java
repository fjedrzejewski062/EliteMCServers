package com.example.elitemcservers.enums;

public enum ServerMode {
    ADVENTURE("Adventure"),
    ANARCHY_SMP("Anarchy SMP"),
    CREATIVE("Creative"),
    EARTH("Earth"),
    FACTIONS("Factions"),
    HARDCORE("Hardcore"),
    KITPVP("KitPVP"),
    MINIGAMES("Minigames"),
    MODDED("Modded"),
    PIXELMON("Pixelmon"),
    PRISON("Prison"),
    PVP("PVP"),
    ROLEPLAY("Roleplay"),
    SKYBLOCK("Skyblock"),
    SURVIVAL("Survival"),
    VANILLA("Vanilla");

    private final String modeName;

    ServerMode(String modeName) {
        this.modeName = modeName;
    }

    public String getModeName() {
        return this.modeName;
    }
}
