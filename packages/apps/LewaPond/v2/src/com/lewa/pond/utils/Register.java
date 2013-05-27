package com.lewa.pond.utils;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;

import com.lewa.core.base.APIClient;
import com.lewa.core.base.APIRequest;
import com.lewa.core.base.APIResponse;

public class Register {

	public static final String KEY_EMAIL = "SignupForm[email]";
	public static final String KEY_PASSWORD = "SignupForm[password]";
	public static final String KEY_CAPTCHA = "SignupForm[captcha]";

	public static final String SIGNUP_URL = "http://api.lewatek.com/account/signup/?lang=en_US";

	public APIClient client;
	public APIRequest request;
	public APIResponse response;
	public Context mContext;

	public Register(Context c) {
		this.mContext = c;
		client = APIClient.getInstance(mContext);
		request = client.getNewRequest();
		response = client.getNewResponse();
	}

	public void setRegisterParams(String email, String password, String captcha) {
		try {
			Bundle params = new Bundle();
			params.putString(KEY_EMAIL, email);
			params.putString(KEY_PASSWORD, password);
			params.putString(KEY_CAPTCHA, captcha);
			request.setURL(SIGNUP_URL);
			request.setParams(params);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String attemptRegister() {
		JSONObject jsonResponse = null;
		try {
			response = client.makeTypedRequest(request,
					APIClient.REQUEST_TYPE_POST);
		} catch (IOException e) {
			e.printStackTrace();
		}
		jsonResponse = response.getResponseJSON();
		return (jsonResponse!=null)?jsonResponse.toString():"";
	}

	public JSONObject parseLoginData(String jsonString) {
		JSONObject json = null;
		try {
			json = new JSONObject(jsonString);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}
}