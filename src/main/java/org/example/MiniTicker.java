package org.example;

import com.binance.connector.client.WebSocketStreamClient;
import com.binance.connector.client.impl.WebSocketStreamClientImpl;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public final class MiniTicker implements Runnable{
    private static final Logger logger
            = LoggerFactory.getLogger(MiniTicker.class);
    private String ticker;
    private double lastAlert;
    private double percentAlert;
    private double price;
    public long lastTime;
    private HelloBot bot;
    private String chatID;
    private WebSocketStreamClient client;
    public MiniTicker(String ticker, double lastAlert, double percentAlert, HelloBot bot, String chatID) {
        this.ticker = ticker;
        this.lastAlert = lastAlert;
        this.percentAlert = percentAlert;
        this.bot = bot;
        this.chatID = chatID;
        this.lastTime = 0;
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
            price = Double.parseDouble(emap.get("c"));
            lastTime = System.currentTimeMillis() / 1000L;
            double diff = price - lastAlert;
            double percent = (diff/lastAlert)*100;
            if (Math.abs(percent) > percentAlert) {
                //Send Message
                StringBuilder msg = new StringBuilder();
                double oldLastAlert = lastAlert;
                msg.append(ticker.toUpperCase());
                if (diff > 0) {
                    msg.append(" up ");
                    lastAlert = lastAlert*(1+percentAlert/100);
                } else {
                    msg.append(" down ");
                    lastAlert = lastAlert*(1-percentAlert/100);
                }
                msg.append(percentAlert);
                msg.append("% from ");
                msg.append(RoundDouble.round(oldLastAlert, 2));
                msg.append(" to ");
                msg.append(RoundDouble.round(lastAlert, 2));
                TelegramApiException error = bot.send(msg.toString(), chatID);
                if (error != null) {
                    logger.error("Message Send Failure");
                    logger.error(error.getMessage());
                }
            }
            logger.debug(String.valueOf(price));
        }));
        while (true) {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                ;
            }
            if (lastTime != 0) {
                long currTime = System.currentTimeMillis() / 1000L;
                long stale = currTime - lastTime;
                logger.debug("Last update: " + stale);
                if (stale > Settings.TICKER_RESTART_TIME_SECONDS) {
                    lastTime = 0;
                    client.closeConnection(connection);
                    logger.info("Possibly stale connection, restarting ticker in 15 seconds");
                    //We can't reconnect immediately
                    try {
                        TimeUnit.SECONDS.sleep(15);
                    } catch (InterruptedException e) {
                        ;
                    }
                    run();
                }
            }
        }
    }
}