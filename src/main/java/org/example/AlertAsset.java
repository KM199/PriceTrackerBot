package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlertAsset {
    private static final Logger LOGGER
            = LoggerFactory.getLogger(AlertAsset.class);
    private transient User user;
    public transient Asset asset;
    public final String assetName;
    private double alertPercent;

    private double alertLow;
    private double alertHigh;



    public AlertAsset(User user, Asset asset, double alertPercent, double alertLow, double alertHigh) {
        this.user = user;
        this.asset = asset;
        asset.addAlertAsset(this);
        this.assetName = asset.name;
        this.alertPercent = alertPercent;
        this.alertLow = alertLow;
        this.alertHigh = alertHigh;
        //Ensure we add the Alert Asset to the User's Alert Assets list'
        this.user.addAlertAsset(this);
    }

    public AlertAsset(User user, Asset asset, double alertPercent, double price) {
        this.user = user;
        this.asset = asset;
        asset.addAlertAsset(this);
        this.assetName = asset.name;
        this.alertPercent = alertPercent;
        setBounds(price);
        //Ensure we add the Alert Asset to the User's Alert Assets list'
        this.user.addAlertAsset(this);
    }

    public void checkPrice(double price) {
        if (price > alertHigh || price < alertLow) {
            //Send Message
            StringBuilder msg = new StringBuilder();
            msg.append(this.assetName);
            if (price > alertHigh) {
                msg.append(" is up ");
                this.setBounds(alertHigh);
            } else {
                msg.append(" is down ");
                this.setBounds(alertLow);
            }
            msg.append(RoundDouble.round(alertPercent, 2));
            msg.append("%\nThe current price is ");
            msg.append(RoundDouble.round(price, 2));
            msg.append("\nNext Alert High: ");
            msg.append(RoundDouble.round(alertHigh, 2));
            msg.append("\nNext Alert Low: ");
            msg.append(RoundDouble.round(alertLow, 2));
            user.sendMessage(msg.toString());
        }
    }

    public double getAlertPercent() {
        return alertPercent;
    }

    public void setAlertPercent(double alertPercent) {
        this.alertPercent = alertPercent;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
        //Ensure that the Alert Asset is added to the Asset's Alert Assets list
        asset.addAlertAsset(this);
    }

    private void setBounds(double price) {
        alertLow = price * (1 - (alertPercent / 100));
        alertHigh = price * (1 + (alertPercent / 100));
    }

    public void checkIn() {
        double currPrice = asset.getLastPrice();
        setBounds(currPrice);
        StringBuilder msg = new StringBuilder();
        msg.append(this.assetName).append(":");
        msg.append(RoundDouble.round(currPrice, 2));
        msg.append("\nNext Alert High: ");
        msg.append(RoundDouble.round(alertHigh, 2));
        msg.append("\nNext Alert Low: ");
        msg.append(RoundDouble.round(alertLow, 2));
        user.sendMessage(msg.toString());
    }

}
