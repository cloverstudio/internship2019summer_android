package com.example.summerschoolapp.model;

import com.google.gson.annotations.SerializedName;

public class RequestRegister {

    @SerializedName("oib")
    public String oib;

    @SerializedName("email")
    public String email;

    @SerializedName("password")
    public String password;
}