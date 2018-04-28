package com.recoded.taqadam.models;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by wisam on Apr 26 18.
 */

public class Transaction {
    private Date transactionTime;
    private float transactionAmount;
    private String transactionId;
    private String transactionHash;
    private String transactionDescription;
    private String walletId;

    public Transaction(Date time, float amount) {
        this.transactionTime = time;
        this.transactionAmount = amount;
    }

    public Date getTransactionTime() {
        return transactionTime;
    }

    public String getTransactionTimeString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/y");
        return sdf.format(transactionTime);
    }

    public void setTransactionTime(Date transactionTime) {
        this.transactionTime = transactionTime;
    }

    public float getTransactionAmount() {
        return transactionAmount;
    }

    public String getTransactionAmountFriendly() {
        return transactionAmount < 0 ? "- $" + Math.abs(transactionAmount) : "$" + transactionAmount;
    }


    public void setTransactionAmount(float transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public String getTransactionDescription() {
        return transactionDescription;
    }

    public void setTransactionDescription(String transactionDescription) {
        this.transactionDescription = transactionDescription;
    }

    public String getWalletId() {
        return walletId;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }
}
