package com.example.elitemcservers.enums;

public enum ServerMode {
    SURVIVAL("Survival"),
    CREATIVE("Creative"),
    ADVENTURE("Adventure"),
    HARDCORE("Hardcore"),
    ANARCHY_SMP("Anarchy SMP"),
    FACTIONS("Factions"),
    SKYBLOCK("Skyblock"),
    PRISON("Prison"),
    KITPVP("KitPVP"),
    MINIGAMES("Minigames"),
    EARTH("Earth"),
    ROLEPLAY("Roleplay"),
    VANILLA("Vanilla"),
    MODDED("Modded"),
    PIXELMON("Pixelmon");

    private final String modeName;

    // Konstruktor
    ServerMode(String modeName) {
        this.modeName = modeName;
    }

    // Metoda zwracająca ładniejszą nazwę trybu
    public String getModeName() {
        return this.modeName;
    }
}
