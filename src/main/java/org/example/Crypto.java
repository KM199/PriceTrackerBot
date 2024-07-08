package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import utils.FileUtils;

import static org.example.Settings.MAX_ALERT_TIME;

public class Crypto {
    private static HelloBot bot;
    private static String chatID;
    private static final Logger LOGGER
            = LoggerFactory.getLogger(Crypto.class);
    private final String name;
    private Double price;
    private long lastPriceUpdate;
    private final Double alertPercent;
    private Double alertHigh;
    private Double alertLow;
    private long lastAlertTime;

    public Crypto(String name, Double alertPercent) {
        this.name = name;
        this.alertPercent = alertPercent;
    }

    public static void setBot(HelloBot bot, String chatID) {
        Crypto.bot = bot;
        Crypto.chatID = chatID;
    }

    //Save Prices
    public void save() {
        StringBuilder sb = new StringBuilder(String.valueOf(price));
        sb.append("\n");
        sb.append(lastPriceUpdate);
        sb.append("\n");
        sb.append(alertHigh);
        sb.append("\n");
        sb.append(alertLow);
        sb.append("\n");
        sb.append(lastAlertTime);
        sb.append("\n");
        FileUtils.writeFile(name + ".txt", sb.toString());
    }

    //Load Prices
    public void load() {
        String loaded = FileUtils.readFile(name + ".txt");
        String[] s = loaded.split("\n");
        this.price = Double.valueOf(s[0]);
        this.lastPriceUpdate = Long.valueOf(s[1]);
        this.alertHigh = Double.valueOf(s[2]);
        this.alertLow = Double.valueOf(s[3]);
        this.lastAlertTime = Long.valueOf(s[4]);
        LOGGER.debug("Loaded: " + name);
        LOGGER.debug("Price: " + price);
        LOGGER.debug("Last Price Update: " + lastPriceUpdate() + " seconds ago");
    }

    //Update Prices
    public void update(double price) {
        if (this.price == price) {
            LOGGER.info("Potentially Stale Price");
        } else {
            this.price = price;
            this.lastPriceUpdate = System.currentTimeMillis() / 1000L;
        }
        checkAlert();
        save();
    }

    public long lastPriceUpdate() {
        return (System.currentTimeMillis() / 1000L) - this.lastPriceUpdate;
    }

    public long lastAlertTime() {
        return (System.currentTimeMillis() / 1000L) - this.lastAlertTime;
    }

    public void checkAlert() {
        if (lastAlertTime() > MAX_ALERT_TIME) {
            StringBuilder msg = new StringBuilder();
            msg.append(this.name);
            msg.append(": ");
            msg.append(price);
            msg.append("\nUpdated: ");
            msg.append(lastPriceUpdate());
            TelegramApiException error = bot.send(msg.toString(), chatID);
            if (error != null) {
                LOGGER.error("Message Send Failure");
                LOGGER.error(error.getMessage());
            } else {
                this.lastAlertTime = System.currentTimeMillis() / 1000L;
            }
        } else if (price > alertHigh || price < alertLow) {
            //Send Message
            StringBuilder msg = new StringBuilder();
            msg.append(this.name);
            if (price > alertHigh) {
                msg.append(" is up ");
                alertLow = alertHigh * (1 - alertPercent / 100);
                alertHigh = alertHigh * (1 + alertPercent / 100);
            } else {
                msg.append(" is down ");
                alertHigh = alertLow * (1 + alertPercent / 100);
                alertLow = alertLow * (1 - alertPercent / 100);
            }
            msg.append(alertPercent);
            msg.append("%\nThe current price is ");
            msg.append(RoundDouble.round(price, 2));
            msg.append("\nNext Alert High: ");
            msg.append(RoundDouble.round(alertHigh, 2));
            msg.append("\nNext Alert Low: ");
            msg.append(RoundDouble.round(alertLow, 2));
            TelegramApiException error = bot.send(msg.toString(), chatID);
            if (error != null) {
                LOGGER.error("Message Send Failure");
                LOGGER.error(error.getMessage());
            } else {
                this.lastAlertTime = System.currentTimeMillis() / 1000L;
            }
        }
    }
}
