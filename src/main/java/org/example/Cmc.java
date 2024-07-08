package org.example;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
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

public class Cmc implements Runnable{
    private Crypto solana;
    private Crypto trunk;

    public Cmc(Crypto solana, Crypto trunk) {
        this.solana = solana;
        this.trunk = trunk;
    }
    private static final Logger logger
            = LoggerFactory.getLogger(Cmc.class);
    private static final String baseUrl = "https://pro-api.coinmarketcap.com";

    public void getPrice() {
        String uri = baseUrl + "/v2/cryptocurrency/quotes/latest";
        String solID = "5426";
        String trunkID = "30329";
        List<NameValuePair> paratmers = new ArrayList<NameValuePair>();
        paratmers.add(new BasicNameValuePair("id",solID + "," + trunkID));
        try {
            String result = makeAPICall(uri, paratmers);
            try {
                JsonObject jsonObject = JsonParser.parseString(result).getAsJsonObject();
                JsonObject status = jsonObject.getAsJsonObject("status");
                int errorCode = status.get("error_code").getAsInt();
                if (errorCode != 0) {
                    logger.info("API Error");
                } else {
                    logger.debug("API call successful");
                    JsonObject data = jsonObject.getAsJsonObject("data");
                    double solPrice = data.getAsJsonObject(solID).getAsJsonObject("quote").getAsJsonObject("USD").get("price").getAsDouble();
                    logger.info("Solana Price: " + solPrice);
                    solana.update(solPrice);
                    double trunkPrice = data.getAsJsonObject(trunkID).getAsJsonObject("quote").getAsJsonObject("USD").get("price").getAsDouble();
                    logger.info("Trunk Price: " + trunkPrice);
                    trunk.update(trunkPrice);
                }

            } catch (JsonSyntaxException e) {
                logger.info("Error: JsonSyntaxException - " + e.toString());
            }
        } catch (IOException e) {
            logger.info("Error: cannont access content - " + e.toString());
        } catch (URISyntaxException e) {
            logger.info("Error: Invalid URL " + e.toString());
        }
    }

    public static String makeAPICall(String uri, List<NameValuePair> parameters)
            throws URISyntaxException, IOException {
        String response_content = "";

        URIBuilder query = new URIBuilder(uri);
        query.addParameters(parameters);

        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet(query.build());

        request.setHeader(HttpHeaders.ACCEPT, "application/json");
        request.addHeader("X-CMC_PRO_API_KEY", CMC_API_KEY);

        CloseableHttpResponse response = client.execute(request);

        try {
            logger.debug(String.valueOf(response.getStatusLine()));
            HttpEntity entity = response.getEntity();
            response_content = EntityUtils.toString(entity);
            EntityUtils.consume(entity);
        } finally {
            response.close();
        }

        return response_content;
    }

    @Override
    public void run() {
        while (true) {
            this.getPrice();
            try {
                TimeUnit.SECONDS.sleep(300);
            } catch (InterruptedException e) {
                ;
            }
        }
    }
}
