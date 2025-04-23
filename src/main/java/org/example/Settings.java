package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Settings {
    private static final Logger LOGGER
            = LoggerFactory.getLogger(Asset.class);
    private static final String settingsFile = "settings.json";

    public int TICKER_RESTART_TIME_SECONDS;
    public int DEFAULT_MAX_ALERT_TIME;
    public boolean COIN_MARKET_CAP_ENABLED;
    public String CMC_API_KEY = "";
    public boolean BINANCE_ENABLED;
    public String TELEGRAM_API_KEY = "";
    public String BOT_NAME = "";

    public Settings() {}

    public static Settings load() {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(settingsFile)) {
            return gson.fromJson(reader, Settings.class);
        } catch (IOException e) {
            LOGGER.error("Failed to load settings to file", e);
        }
        return null;
    }

    public void save() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(settingsFile)) {
            gson.toJson(this, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to save settings to file", e);
        }
    }
}