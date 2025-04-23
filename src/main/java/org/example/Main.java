package org.example;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.example.Settings.*;

public class Main {
    private static final Logger LOGGER
            = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        boolean restart;
        do {
            LOGGER.info("Booting ...");
            restart = mainProcess();
        } while (restart);
    }

    public static boolean mainProcess() {
        try {
            Settings settings = Settings.load();
            assert settings != null;
            User.setSettings(settings);
            User.load();
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            HelloBot bot = new HelloBot(settings);
            botsApi.registerBot(bot);
            User.setHelloBot(bot);
            if (settings.BINANCE_ENABLED) {
                for (Asset asset : Asset.getAssets() ) {
                    if (asset.binanceTicker != null) {
                        MiniTicker miniTicker = new MiniTicker(asset, settings);
                        Thread miniTickerThread = new Thread(miniTicker);
                        miniTickerThread.start();
                    }
                }
            }
            CoinMarketCap coinMarketCap = new CoinMarketCap(Asset.getAssets(), settings);
            Loop loop = new Loop(coinMarketCap, settings);
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(loop, 0, 300, TimeUnit.SECONDS);
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {
                LOGGER.error(e.toString());
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        LOGGER.info("Restarting in 60 seconds");
        try {
            TimeUnit.SECONDS.sleep(60);
        } catch (InterruptedException e) {
            LOGGER.debug(e.toString());
        }
        return true;
    }
}
