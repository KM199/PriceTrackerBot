package org.example;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Asset {
    private static final Logger LOGGER
            = LoggerFactory.getLogger(Asset.class);
    private static final String ASSET_FILE = "assets.json";
    public final String name;
    public final String binanceTicker;
    public final String cmcId;
    private static List<Asset> assets = new ArrayList<>();
    private transient double lastPrice;
    private transient List<AlertAsset> alertAssets = new ArrayList<>();

    public Asset(String name, String binanceTicker, String cmcId) {
        this.name = name;
        this.binanceTicker = binanceTicker;
        this.cmcId = cmcId;
        assets.add(this);
    }

    public double getLastPrice() {
        return lastPrice;
    }

    public void updatePrice(double newPrice) {
        this.lastPrice = newPrice;
        for (AlertAsset alertAsset : alertAssets) {
            alertAsset.checkPrice(lastPrice);
        }
    }

    public void addAlertAsset(AlertAsset alertAsset) {
        alertAssets.add(alertAsset);
    }

    public void removeAlertAsset(AlertAsset alertAsset) {
        alertAssets.remove(alertAsset);
    }

    public static void load() {
        Gson gson = new Gson();
        Type assetListType = new TypeToken<List<Asset>>() {
        }.getType();

        try (FileReader reader = new FileReader(ASSET_FILE)) {
            assets = gson.fromJson(reader, assetListType);
            for (Asset asset : assets) {
                asset.alertAssets = new ArrayList<>();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create(); // Pretty-printed JSON
        try (FileWriter writer = new FileWriter(ASSET_FILE)) {
            gson.toJson(assets, writer); // Serialize and write to file
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Asset getAssetByName(String name) {
        for (Asset asset : assets) {
            if (asset.name.equals(name)) {
                return asset;
            }
        }
        LOGGER.error("Asset not found: {}", name);
        return null;
    }

    public static List<Asset> getAssets() {
        return assets;
    }
}