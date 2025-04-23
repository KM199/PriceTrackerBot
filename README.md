PriceTrackerBot

Sends a Telegram alert after every x% move in the price of a cryptocurrency.
Additionally it sends a message after x hours of no alerts for each crypto. 

Data Sources:
- Binance
- CoinMarketCap

# Install
Create Telegram Bot:
- https://core.telegram.org/bots/tutorial
- Get Api key

Create CoinMarketCap Api Key:
- https://coinmarketcap.com/academy/article/register-for-coinmarketcap-api
- Free, only makes one call every 5 minutes to stay within usage limits

Download the TelegramBot folder
- Edit settings.json to include the API Keys and bot name
- Edit users.json to include your chatId which you can get from @RawDataBot on telegram
  - Set custom thresholds if wanted
- Edit assets.json to include more assets if wanted
  - you also have to add these assets to user settings for each user that wants them

Run terminal command: chmod +x run.sh

Set up a service to run run.sh, I used systemctl.

# Troubleshooting:
Check log.log