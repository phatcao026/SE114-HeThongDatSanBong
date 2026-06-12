package com.example.timsanbong.data.model;

import com.google.gson.annotations.SerializedName;

public class CheckoutSessionResponse {
    @SerializedName("checkoutUrl")
    private String checkoutUrl;

    public String getCheckoutUrl() { return checkoutUrl; }
}
