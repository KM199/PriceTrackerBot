package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.FileUtils;

public class Crypto {
    private static final Logger logger
            = LoggerFactory.getLogger(Crypto.class);
    private final String name;
    private Double price;
    private long lastPriceUpdate;
    private Double alertHigh;
    private Double alertLow;
    private long lastAlertTime;

    public Crypto(String name) {
        this.name = name;
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
        logger.debug("Loaded: " + name);
        logger.debug("Price: " + price);
        logger.debug("Last Price Update: " + lastPriceUpdate() + " seconds ago");
    }

    //Update Prices
    public void update(double price) {
        if (this.price == price) {
            logger.info("Potentially Stale Price");
        } else {
            this.price = price;
            this.lastPriceUpdate = System.currentTimeMillis() / 1000L;
        }
        save();
    }

    public long lastPriceUpdate() {
        return (System.currentTimeMillis() / 1000L) - this.lastPriceUpdate;
    }

    public long lastAlertTime() {
        return (System.currentTimeMillis() / 1000L) - this.lastAlertTime;
    }
}
