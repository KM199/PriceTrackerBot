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
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.example.Secret.CMC_API_KEY;

public class Cmc implements Runnable {
    private static Crypto solana;
    private static Crypto trunk;

    public Cmc(Crypto solana, Crypto trunk) {
        Cmc.solana = solana;
        Cmc.trunk = trunk;
    }
    private static final Logger LOGGER
            = LoggerFactory.getLogger(Cmc.class);
    private static final String BASE_URL = "https://pro-api.coinmarketcap.com";

    public void getPrice() {
        String uri = BASE_URL + "/v2/cryptocurrency/quotes/latest";
        String solID = "5426";
        String trunkID = "30329";
        List<NameValuePair> paratmers = new ArrayList<NameValuePair>();
        paratmers.add(new BasicNameValuePair("id", solID + "," + trunkID));
        try {
            String result = makeAPICall(uri, paratmers);
            try {
                JsonObject jsonObject = JsonParser.parseString(result).getAsJsonObject();
                JsonObject status = jsonObject.getAsJsonObject("status");
                int errorCode = status.get("error_code").getAsInt();
                if (errorCode != 0) {
                    LOGGER.info("API Error");
                } else {
                    LOGGER.debug("API call successful");
                    JsonObject data = jsonObject.getAsJsonObject("data");
                    double solPrice = data.getAsJsonObject(solID).getAsJsonObject("quote").getAsJsonObject("USD").get("price").getAsDouble();
                    LOGGER.info("Solana Price: " + solPrice);
                    solana.update(solPrice);
                    double trunkPrice = data.getAsJsonObject(trunkID).getAsJsonObject("quote").getAsJsonObject("USD").get("price").getAsDouble();
                    LOGGER.info("Trunk Price: " + trunkPrice);
                    trunk.update(trunkPrice);
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
        } catch (Error e){
            LOGGER.error("API Call Error" + e);
        } finally {
            response.close();
        }

        return responseContent;
    }

    @Override
    public void run() {
        this.getPrice();
        while (true) {
            try {
                TimeUnit.SECONDS.sleep(300);
                this.getPrice();
            } catch (Error | InterruptedException e) {
                LOGGER.debug(e.toString());
            }
        }
    }
}
