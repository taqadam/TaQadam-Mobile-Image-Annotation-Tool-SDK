package com.recoded.taqadam.models;

import java.io.Serializable;

/**
 * Created by wisam on Dec 10 17.
 */

class Wallet implements Serializable {
    //TODO-wisam: Payment and wallet increase should always take place in the backend. this is just a wrapper.
    //Ask Jon to do it
    private boolean mIsActive = false;
    private String mUid;
    private double mAmount;
    private Currency mCurrency;

    public enum Currency {
        USD(0),
        LBP(1),
        EUR(2);
        private final int currencyId;
        private final double USD_TO_LBP = 1511.01d; //1 USD = 1511.01 LBP      as of Dec 14 2017
        private final double EUR_TO_LBP = 1787.951d; //1 EUR = 1787.951 LBP    as of Dec 14 2017
        private final double USD_TO_EUR = USD_TO_LBP / EUR_TO_LBP; // 0.8451

        Currency(int cId) {
            this.currencyId = cId;
        }

        public int getCurrencyId() {
            return currencyId;
        }

        public double getConversionRate(Currency to) {
            switch (currencyId) {
                case 0: //USD
                    if (to.getCurrencyId() == 1) return USD_TO_LBP; //TO LBP
                    if (to.getCurrencyId() == 2) return USD_TO_EUR; //TO EUR
                    break;
                case 1: //LBP
                    if (to.getCurrencyId() == 0) return 1d / USD_TO_LBP; //TO USD
                    if (to.getCurrencyId() == 2) return 1d / EUR_TO_LBP; //TO EUR
                    break;
                case 2: //EUR
                    if (to.getCurrencyId() == 0) return 1d / USD_TO_EUR; //TO USD
                    if (to.getCurrencyId() == 1) return EUR_TO_LBP; //TO LBP
            }
            return 1d;
        }
    }

    public Wallet(String uid, double amount, int currencyId) {
        this.mUid = uid;
        this.mIsActive = false;
        this.mAmount = amount;
        switch (currencyId) {
            case 0:
                mCurrency = Currency.USD;
                break;
            case 1:
                mCurrency = Currency.LBP;
                break;
            case 2:
                mCurrency = Currency.EUR;
                break;
        }
    }

    public Wallet(String uid, double amount) {
        this(uid, amount, 0);
    }

    public Wallet(String uid) {
        this(uid, 0d);
    }

    public void setActive(boolean active) {
        this.mIsActive = active;
    }

    public boolean isActive() {
        return this.mIsActive;
    }

    public String getUid() {
        return mUid;
    }

    public void setUid(String mUid) {
        this.mUid = mUid;
    }

    public double getAmount() {
        return mAmount;
    }

    public void setAmount(double mAmount) {
        this.mAmount = mAmount;
    }

    public Currency getCurrency() {
        return mCurrency;
    }

    public void setCurrency(Currency newCurrency) {
        if (this.mCurrency.getCurrencyId() != newCurrency.currencyId) {
            //TODO-wisam: make sure to implement updated currency conversion from old currency to new one
            this.mAmount *= this.mCurrency.getConversionRate(newCurrency);
            this.mCurrency = newCurrency;
        }
    }

    public double pay(double amount) {
        this.mAmount += amount;
        return this.mAmount;
    }

}
