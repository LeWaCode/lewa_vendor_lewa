package com.lewa.pond.utils;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;

import com.lewa.core.base.APIClient;
import com.lewa.core.base.APIRequest;
import com.lewa.core.base.APIResponse;

public class Login {
	
	public static final String KEY_EMAIL = "LoginForm[email]";
	public static final String KEY_PASSWORD = "LoginForm[password]";
	public static final String KEY_IMEI = "LoginForm[imei]";
	
	public static String VALUE_IMEI = "123123123123";	
	public static final String LOGIN_URL = "http://api.lewatek.com/account/login/?lang=en_US";	
	
	public APIClient client;
	public APIRequest request;
	public APIResponse response;	
	public Context mContext;
	
	public Login(Context c) {
		this.mContext=c;
		client=APIClient.getInstance(mContext);
		request=client.getNewRequest();
		response=client.getNewResponse();
	}
	
	public void setLoginParams(String email,String password,String imei){
		try {
			Bundle params = new Bundle();
			params.putString(KEY_EMAIL,email);
			params.putString(KEY_PASSWORD, password);
			params.putString(KEY_IMEI,imei);		
			request.setURL(LOGIN_URL);
			request.setParams(params);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String attemptLogin() {
		JSONObject jsonResponse = null;
		try {
			response = client.makeTypedRequest(request, APIClient.REQUEST_TYPE_POST);
		} catch (IOException e) {
			e.printStackTrace();
		}
		jsonResponse = response.getResponseJSON();
		return jsonResponse.toString();
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