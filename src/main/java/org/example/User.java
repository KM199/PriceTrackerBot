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

public class User {
    private static final Logger LOGGER
            = LoggerFactory.getLogger(User.class);
    private static final String userFile = "users.json";
    private static List<User> users = new ArrayList<>();
    private static HelloBot bot;
    private static Settings settings;

    public final String CHAT_ID;
    private int maxAlertTime = settings.DEFAULT_MAX_ALERT_TIME;
    private long lastAlertTime;

    private List<AlertAsset> alertAssets = new ArrayList<>();

    public User(String chatId) {
        CHAT_ID = chatId;
        users.add(this);
    }

    public static void setHelloBot(HelloBot helloBot) {
        bot = helloBot;
    }

    public void setMaxAlertTime(int maxAlertTime) {
        if (maxAlertTime <= 0) {
            throw new IllegalArgumentException("Alert time cannot be negative or zero");
        } else {
            this.maxAlertTime = maxAlertTime;
        }
    }

    private void updateLastAlertTime() {
        lastAlertTime = System.currentTimeMillis() / 1000L;
    }

    private boolean timeForAlert() {
        return (System.currentTimeMillis() / 1000L - lastAlertTime) >= maxAlertTime;
    }

    public static void checkIn() {
        for (User user : users) {
            if (user.timeForAlert()) {
                user.report();
            }
        }
    }

    public void report() {
        LOGGER.info("Checking in with " + this.CHAT_ID);
        for (AlertAsset alertAsset : this.alertAssets) {
            alertAsset.checkIn();
        }
    }


    public void addAlertAsset(AlertAsset alertAsset) {
        alertAssets.add(alertAsset);
    }

    public void removeAlertAsset(AlertAsset alertAsset) {
        //Remove the alertAsset from our user
        alertAssets.remove(alertAsset);
        //Remove the alertAsset from the Asset
        alertAsset.asset.removeAlertAsset(alertAsset);
    }
    
    public static List<User> getUsers() {
        return users;
    }

    public static void save() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create(); // Pretty-printed JSON
        try (FileWriter writer = new FileWriter(userFile)) {
            gson.toJson(users, writer); // Serialize and write to file
        } catch (IOException e) {
            LOGGER.error("Failed to save users to file", e);
        }
    }

    public static void load() {
        //Ensure Assets are loaded before loading users
        Asset.load();
        List<Asset> assets = Asset.getAssets();

        Gson gson = new Gson();
        Type userListType = new TypeToken<List<User>>() {
        }.getType();

        try (FileReader reader = new FileReader(userFile)) {
            users = gson.fromJson(reader, userListType);
            for (User user : users) {
                for (AlertAsset alertAsset : user.alertAssets) {
                    //Link alertAsset to User
                    alertAsset.setUser(user);
                    //Link alertAsset to Asset
                    alertAsset.setAsset(Asset.getAssetByName(alertAsset.assetName));
                }
            }
            LOGGER.info("Loaded " + users.size() + " users");
        } catch (IOException e) {
            LOGGER.error("Failed to load users from file", e);
        }
    }

    public void sendMessage(String message) {
        bot.send(message, this.CHAT_ID);
        this.updateLastAlertTime();
    }

    public static void setSettings(Settings settings) {
        User.settings = settings;
    }

}
