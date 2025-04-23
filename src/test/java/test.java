import org.example.*;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class test {
    private static final Logger LOGGER
            = LoggerFactory.getLogger(Test.class);

    @Test
    public void loadSettings() {
        Settings settings = Settings.load();
        Assert.assertEquals(true, settings.COIN_MARKET_CAP_ENABLED);
    }
}