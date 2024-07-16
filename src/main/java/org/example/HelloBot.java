package org.example;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.*;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class HelloBot extends TelegramLongPollingBot {
    private static final Logger LOGGER
            = LoggerFactory.getLogger(HelloBot.class);
    public HelloBot() {

    }

    public TelegramApiException send(String msg, String chatID) {
        SendMessage message = new SendMessage(); // Create a SendMessage object with mandatory fields
        message.setChatId(chatID);
        message.setText(msg);
        LOGGER.debug("Sending message: " + msg + " to " + chatID);
        try {
            execute(message); // Call method to send the message
            LOGGER.debug("Sent!");
            return null;
        } catch (TelegramApiException e) {
            LOGGER.error(e.getMessage());
            return e;
        }
    }
    @Override
    public void onUpdateReceived(Update update) {
        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {
            //update.getMessage().getText()
            StringBuilder msg = new StringBuilder();
            for (Crypto c: Crypto.cryptos) {
                msg.append(c.name);
                msg.append(": ");
                msg.append(RoundDouble.round(c.getPrice(), 2));
                msg.append("\nLast update: ");
                msg.append(c.lastPriceUpdate());
                msg.append("\nLast alert: ");
                msg.append(c.lastAlertTime());
                msg.append("\n");
            }
            send(msg.toString(), update.getMessage().getChatId().toString());
        }
    }

    @Override
    public String getBotUsername() {
        return Secret.BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return Secret.TELEGRAM_API_KEY;
    }
}

