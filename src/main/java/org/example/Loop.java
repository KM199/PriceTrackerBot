package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Loop implements Runnable {
    private static final Logger LOGGER
            = LoggerFactory.getLogger(Loop.class);
    private CoinMarketCap coinMarketCap;
    private final Settings settings;

    public Loop(CoinMarketCap coinMarketCap, Settings settings) {
        this.coinMarketCap = coinMarketCap;
        this.settings = settings;
    }

    @Override
    public void run() {
        try {
            if (settings.COIN_MARKET_CAP_ENABLED) {
                coinMarketCap.getPrice();
            }
            //User check In
            User.checkIn();
            //Save the user data to a JSON file
            User.save();
        } catch (Exception e) {
            // Handle any exceptions here
            LOGGER.error("An error occurred: " + e.getMessage());
        }
    }
}
