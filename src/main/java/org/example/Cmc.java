package org.example;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.example.Secret.CMC_API_KEY;

public class Cmc implements Runnable {
    private ArrayList<Crypto> cryptos;
    private List<NameValuePair> parameters;

    public Cmc(ArrayList<Crypto> cryptos) {
        this.cryptos = cryptos;
        StringBuilder idValue = new StringBuilder();
        Iterator<Crypto> iterator = cryptos.iterator();
        while (iterator.hasNext()) {
            Crypto crypto = iterator.next();
            idValue.append(crypto.cmcId);
            if (iterator.hasNext()) {
                idValue.append(",");
            }
        }
        this.parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("id", idValue.toString()));
    }
    private static final Logger LOGGER
            = LoggerFactory.getLogger(Cmc.class);
    private static final String URL = "https://pro-api.coinmarketcap.com/v2/cryptocurrency/quotes/latest";

    public void getPrice() {
        try {
            String result = makeAPICall(URL, parameters);
            try {
                JsonObject jsonObject = JsonParser.parseString(result).getAsJsonObject();
                JsonObject status = jsonObject.getAsJsonObject("status");
                int errorCode = status.get("error_code").getAsInt();
                if (errorCode != 0) {
                    LOGGER.info("API Error");
                } else {
                    JsonObject data = jsonObject.getAsJsonObject("data");
                    if (data != null) {
                        for (Crypto crypto : cryptos) {
                            double price = getPriceFromJSON(data, crypto.cmcId);
                            if (price > 0) {
                                LOGGER.info(crypto.name + " Price: " + price);
                                crypto.update(price);
                            } else {
                                LOGGER.error(crypto.name + " Price Error");
                            }
                        }
                    }
                }
            } catch (Error e) {
                LOGGER.info(e.toString());
            }
        } catch (IOException e) {
            LOGGER.info("Error: cannont access content - " + e.toString());
        } catch (URISyntaxException e) {
            LOGGER.info("Error: Invalid URL " + e.toString());
        }
    }

    public static String makeAPICall(String uri, List<NameValuePair> parameters)
            throws URISyntaxException, IOException {
        String responseContent = "";

        URIBuilder query = new URIBuilder(uri);
        query.addParameters(parameters);

        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet(query.build());

        request.setHeader(HttpHeaders.ACCEPT, "application/json");
        request.addHeader("X-CMC_PRO_API_KEY", CMC_API_KEY);

        CloseableHttpResponse response = client.execute(request);

        try {
            LOGGER.debug(String.valueOf(response.getStatusLine()));
            HttpEntity entity = response.getEntity();
            responseContent = EntityUtils.toString(entity);
            EntityUtils.consume(entity);
        } catch (Error e) {
            LOGGER.error("API Call Error" + e);
        } finally {
            response.close();
        }

        return responseContent;
    }

    private double getPriceFromJSON(JsonObject data, String id) {
        if (data != null) {
            JsonObject tokenObj = data.getAsJsonObject(id);
            if (tokenObj != null) {
                JsonObject tokenQuote = tokenObj.getAsJsonObject("quote");
                if (tokenQuote != null) {
                    JsonObject tokenUSD = tokenQuote.getAsJsonObject("USD");
                    if (tokenUSD != null) {
                        try {
                            return tokenUSD.get("price").getAsDouble();
                        } catch (Error e) {
                            LOGGER.error(String.valueOf(e));
                        }
                    }
                }
            }
            LOGGER.error("Error decoding JSON: " + data.toString());
        }
        return -1;
    }

    @Override
    public void run() {
        try {
            // Your code to execute every 5 minutes
            this.getPrice();
        } catch (Exception e) {
            // Handle any exceptions here
            LOGGER.error("An error occurred: " + e.getMessage());
        }
    }
}
