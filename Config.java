package com.ultramenu;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    public static Config I = new Config();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("ultramenu.json");

    // Вид
    public float aspect = 0f;          // 0 = авто
    public boolean fullbright = false;
    public boolean noBob = false;
    public boolean zoomEnabled = true;
    public float zoomLevel = 4f;

    // Руки
    public boolean handsEnabled = true;
    public float handX = 0f, handY = 0f, handZ = 0f;
    public float handRotX = 0f, handRotY = 0f, handRotZ = 0f;
    public float handScale = 1f;

    // Миникарта
    public boolean minimap = true;
    public boolean mmEntities = true;
    public boolean mmCoords = true;
    public int mmSize = 110;
    public int mmRange = 40;           // радиус в блоках
    public int mmCorner = 1;           // 0 ЛВ, 1 ПВ, 2 ЛН, 3 ПН

    // Облегчалки
    public boolean autoSprint = false;
    public boolean infoHud = true;
    public boolean showFps = true, showCoords = true, showPing = true, showBiome = false, showDir = true;

    // PvP
    public boolean pvpCps = true;
    public boolean pvpCombo = true;
    public boolean armorHud = true;
    public boolean supplyHud = true;
    public boolean targetHud = true;

    public static void load() {
        try {
            if (Files.exists(PATH)) {
                Config c = GSON.fromJson(Files.readString(PATH), Config.class);
                if (c != null) I = c;
            }
        } catch (Exception e) {
            I = new Config();
        }
    }

    public static void save() {
        try {
            Files.writeString(PATH, GSON.toJson(I));
        } catch (IOException ignored) {
        }
    }
}
