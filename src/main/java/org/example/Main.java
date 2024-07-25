package org.example;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

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
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            HelloBot bot = new HelloBot();
            botsApi.registerBot(bot);
            Crypto solana = new Crypto("Solana", SOLANA_CMC_ID, ALERT_PERCENT_SOL);
            solana.load();
            Crypto trunk = new Crypto("Trunk", TRUNK_CMC_ID, ALERT_PERCENT_TRUNK);
            trunk.load();
            Crypto.setBot(bot, Secret.CHAT_ID);
            MiniTicker sol = new MiniTicker("solusdc", solana);
            Thread miniTickerThread = new Thread(sol);
            miniTickerThread.start();
            Cmc cmc = new Cmc(Crypto.cryptos);
            Thread cmcThread = new Thread(cmc);
            cmcThread.start();
            while (true) {
                TimeUnit.SECONDS.sleep(60);
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
