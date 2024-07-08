package org.example;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.concurrent.TimeUnit;

import static org.example.Settings.ALERT_PERCENT_SOL;
import static org.example.Settings.ALERT_PERCENT_TRUNK;

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
            Crypto solana = new Crypto("Solana", ALERT_PERCENT_SOL);
            Crypto trunk = new Crypto("Trunk", ALERT_PERCENT_TRUNK);
            solana.load();
            trunk.load();
            Crypto.setBot(bot, Secret.CHAT_ID);
            MiniTicker sol = new MiniTicker("solusdc", solana);
            Thread miniTickerThread = new Thread(sol);
            miniTickerThread.start();
            Cmc cmc = new Cmc(solana, trunk);
            Thread cmcThread = new Thread(cmc);
            cmcThread.start();
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