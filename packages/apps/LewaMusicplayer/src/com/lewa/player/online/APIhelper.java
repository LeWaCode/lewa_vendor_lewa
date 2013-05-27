package com.lewa.player.online;

import java.security.MessageDigest;
import java.util.Arrays;

public class APIhelper {

	private static final String APIUrl = "http://open.top100.cn/api/rest/";

	// / <summary>
	// / 巨鲸音乐分配的API Key
	// / </summary>
	private static final String APIKey = "jqihfco8if3ff5nazqucgcbssi6uaan8";

	// / <summary>
	// / 巨鲸音乐分配的Shard Secret
	// / </summary>
	private static final String ShardSecret = "857rrxa6u8vjpa6fkicqsc7hp007f48h";

	public static String GetUrl(String method, Object[] arguments) {
		StringBuilder url = new StringBuilder();
		StringBuilder sb = new StringBuilder();

		url.append(APIUrl);
		url.append("?method=" + method);
		url.append("&api_key=" + APIKey);

		sb.append(ShardSecret);

		if (arguments != null) {
			// 对参数进行排序
			Arrays.sort(arguments);

			// 合成参数

			for (int i = 0; i < arguments.length; i++) {
				Argument argument = (Argument) arguments[i];

				sb.append(argument.Name);
				sb.append(argument.Value);

				// 合成Url的参数的部分
				url.append("&");
				url.append(argument.Name);
				url.append("=");
				url.append(argument.Value);

			}
		}
		// 将字符串用MD5加密码，生成32位的签名（api_key）
		//String sig = GetMd5Sum(sb.toString());
		String sig = MD5Util.MD5Encode(sb.toString(), "UTF-8");

		// 合成Url的签名部分
		url.append("&api_sig=");
		url.append(sig);
		url.append("&rtype=json");
		return url.toString();
	}

	public static String GetMd5Sum(String text) {
		if (text == null)
			return "";

		StringBuffer hexString = new StringBuffer();

		try {

			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(text.getBytes());
			
			byte[] digest = md.digest();
			
			for(int i=0;i<digest.length;i++){
				text = Integer.toHexString(0xFF&digest[i]);
				if(text.length()<2){
					text ="0"+text;
				}
				hexString.append(text);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return hexString.toString();
	}
}
