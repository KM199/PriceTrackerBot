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
    private final Settings settings;
    public HelloBot(Settings settings) {
        this.settings = settings;
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
        String chatId = update.getMessage().getChatId().toString();
        User currentUser = null;
        for (User user : User.getUsers()) {
            if(user.CHAT_ID.equals(chatId)) {
                currentUser = user;
            }
        }
        //Allow User to update things, create a new account, etc
        if (currentUser != null) {
            currentUser.report();
        } else {
            this.send("Welcome", chatId);
        }
    }

    @Override
    public String getBotUsername() {
        return settings.BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return settings.TELEGRAM_API_KEY;
    }
}

