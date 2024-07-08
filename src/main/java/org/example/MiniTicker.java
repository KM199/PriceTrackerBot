package org.example;

import com.binance.connector.client.WebSocketStreamClient;
import com.binance.connector.client.impl.WebSocketStreamClientImpl;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public final class MiniTicker implements Runnable {
    private static final Logger LOGGER
            = LoggerFactory.getLogger(MiniTicker.class);
    private String ticker;
    private WebSocketStreamClient client;
    private Crypto token;
    private long lastTime;
    public MiniTicker(String ticker, Crypto token) {
        this.ticker = ticker;
        this.token = token;
        client = new WebSocketStreamClientImpl();
    }

    public static HashMap<String, String> read(String str) {
        Gson gson = new Gson();
        HashMap<String, String> hmap = new Gson().fromJson(str, HashMap.class);
        return hmap;
    }

    @Override
    public void run() {
        int connection = client.miniTickerStream(ticker, ((event) -> {
            HashMap<String, String> emap = read(event);
            double price = Double.parseDouble(emap.get("c"));
            LOGGER.debug(String.valueOf(price));
            token.update(price);
            lastTime = System.currentTimeMillis() / 1000L;
        }));
        while (true) {
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                LOGGER.debug(e.toString());
            }
            if (lastTime != 0) {
                long currTime = System.currentTimeMillis() / 1000L;
                long stale = currTime - lastTime;
                LOGGER.debug("Last update: " + stale);
                if (stale > Settings.TICKER_RESTART_TIME_SECONDS) {
                    lastTime = 0;
                    client.closeConnection(connection);
                    LOGGER.info("Possibly stale connection, restarting ticker in 30 seconds");
                    //We can't reconnect immediately
                    try {
                        TimeUnit.SECONDS.sleep(30);
                    } catch (InterruptedException e) {
                        LOGGER.debug(e.toString());
                    }
                    run();
                }
            }
        }
    }
}
