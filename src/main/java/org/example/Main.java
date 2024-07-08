package org.example;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.concurrent.TimeUnit;

public class Main {
    private static final Logger logger
            = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        boolean restart;
        do {
            logger.info("Booting ...");
            restart = MainProcess();
        } while (restart);
    }

    public static boolean MainProcess() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            HelloBot bot = new HelloBot();
            botsApi.registerBot(bot);
            MiniTicker sol = new MiniTicker("solusdc", 1, Settings.PERCENT_ALERT_SOL, bot, Secret.CHAT_ID);
            sol.run();
            while (true) {
                TimeUnit.SECONDS.sleep(60);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        logger.info("Restarting in 60 seconds");
        try {
            TimeUnit.SECONDS.sleep(60);
        } catch (InterruptedException e) {
        }
        return true;
    }
}