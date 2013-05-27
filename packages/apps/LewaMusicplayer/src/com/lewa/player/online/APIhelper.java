package com.lewa.player.online;

import java.security.MessageDigest;
import java.util.Arrays;

public class APIhelper {

	private static final String APIUrl = "http://open.top100.cn/api/rest/";

	// / <summary>
	// / �޾����ַ����API Key
	// / </summary>
	private static final String APIKey = "jqihfco8if3ff5nazqucgcbssi6uaan8";

	// / <summary>
	// / �޾����ַ����Shard Secret
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
			// �Բ�����������
			Arrays.sort(arguments);

			// �ϳɲ���

			for (int i = 0; i < arguments.length; i++) {
				Argument argument = (Argument) arguments[i];

				sb.append(argument.Name);
				sb.append(argument.Value);

				// �ϳ�Url�Ĳ����Ĳ���
				url.append("&");
				url.append(argument.Name);
				url.append("=");
				url.append(argument.Value);

			}
		}
		// ���ַ�����MD5�����룬����32λ��ǩ����api_key��
		//String sig = GetMd5Sum(sb.toString());
		String sig = MD5Util.MD5Encode(sb.toString(), "UTF-8");

		// �ϳ�Url��ǩ������
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
