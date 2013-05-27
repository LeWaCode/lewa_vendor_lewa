package com.lewa.feedback;

import java.io.DataOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class Upload {
	 /**
     * ͨ��ƴ�ӵķ�ʽ�����������ݣ�ʵ�ֲ��������Լ��ļ�����
     * @param actionUrl
     * @param params
     * @param files
     * @return
     * @throws IOException
     */
	
	private static DataOutputStream outStream;
	private static HttpURLConnection conn;
	
	private static final String BOUNDARY = java.util.UUID.randomUUID().toString();
	private static final String PREFIX = "--" , LINEND = "\r\n";
	private static final String MULTIPART_FROM_DATA = "multipart/form-data"; 
	private static final String CHARSET = "UTF-8";
	
	public static final int STATE_CONNECT_SUCCESS = 0;
	public static final int STATE_SEND_MSG_SUCCESS = 1;
	public static final int STATE_SEND_FILE_SUCCESS = 2;
	public static final int STATE_CLOSE_SUCCESS = 3;
	public static final int STATE_CONNECT_FAIL = 4;
	public static final int STATE_SEND_MSG_FAIL = 5;
	public static final int STATE_SEND_FILE_FAIL = 6;
	public static final int STATE_CLOSE_FAIL = 7;
	
	public static final int PROCESS_ERROR = 1;
	public static final int SUCCESS  = 2;
	public static final int REDIRECT = 3;
	public static final int CLIENT_ERROR = 4;
	public static final int SERVER_ERROR = 5;
	
	
	public static int state = 0;
	
	public static boolean connectStart(String url)
	{
		try
		{
			URL uri = new URL(url); 
	   	 	conn = (HttpURLConnection) uri.openConnection(); 
	   	 	conn.setReadTimeout(50 * 1000); // ������ʱ�� 
	   	 	conn.setDoInput(true);// �������� 
	   	 	conn.setDoOutput(true);// ������� 
	   	 	conn.setUseCaches(false); // ������ʹ�û��� 
	   	 	conn.setRequestMethod("POST"); 
	   	 	conn.setRequestProperty("connection", "keep-alive"); 
	   	 	conn.setRequestProperty("Charsert", "UTF-8"); 
	   	 	conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA + ";boundary=" + BOUNDARY); 
	   	 	
	   	 	outStream = new DataOutputStream(conn.getOutputStream());
	   	 	return true;
		}
		catch(Exception e)
		{
			close();
			return false;
		}
	}
	
	public static boolean sendMessage(Map<String, String> params)
	{
		// ������ƴ�ı����͵Ĳ��� 
   	 	StringBuilder sb = new StringBuilder(); 
   	 	
   	 	for (Map.Entry<String, String> entry : params.entrySet()) 
   	 	{ 
   	 		sb.append(PREFIX); 
   	 		sb.append(BOUNDARY); 
   	 		sb.append(LINEND); 
   	 		sb.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + LINEND);
   	 		sb.append("Content-Type: text/plain; charset=" + CHARSET+LINEND);
   	 		sb.append("Content-Transfer-Encoding: 8bit" + LINEND);
   	 		sb.append(LINEND);
   	 		sb.append(entry.getValue()); 
   	 		sb.append(LINEND); 
   	 	} 
      
   	 	try
		{
   	 		outStream.write(sb.toString().getBytes()); 
   	 		return true;
		}
   	 	catch(Exception e)
		{
   	 		close();
			return false;
		}
	}
	
	public static boolean sendFiles(Map<String, File> files){ 

		// �����ļ����� 
   	 	if(files != null)
   	 	{
   	 		int i = 0;
        
   	 		for (Map.Entry<String, File> file: files.entrySet()) 
   	 		{ 
   	 			StringBuilder sb1 = new StringBuilder(); 
   	 			sb1.append(PREFIX); 
   	 			sb1.append(BOUNDARY); 
   	 			sb1.append(LINEND); 
           
   	 			sb1.append("Content-Disposition: form-data; name=\"file" + (i++) + "\"; filename=\"" + file.getKey() + "\"" + LINEND);
   	 			sb1.append("Content-Type: application/octet-stream; charset=" + CHARSET + LINEND);
   	 			sb1.append(LINEND);
   	 			
   	 			try
   	 			{
   	 				outStream.write(sb1.toString().getBytes()); 

   	 				InputStream is = new FileInputStream(file.getValue());
   	 				byte[] buffer = new byte[1024]; 
   	 				int len = 0; 
         
   	 				while ((len = is.read(buffer)) != -1) { 
   	 					outStream.write(buffer, 0, len); 
   	 				}
         
   	 				is.close(); 
   	 				outStream.write(LINEND.getBytes()); 
   	 				
   	 				return true;
   	 			}
   	 			catch(Exception e)
   	 			{
   	 				close();
   	 				return false;
   	 			}
   	 			
   	 		} 
   	 	}
   	 	return true;
	}
	
	public static int connectClose()
	{
	
		try	
		{
			//���������־
			byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes(); 
			outStream.write(end_data); 
			outStream.flush(); 
			
			//�õ���Ӧ�� 
			int res = conn.getResponseCode(); 

			close();
			return res / 100;
		}
		catch(Exception e)
		{
			return PROCESS_ERROR;
		}
	}
	
	private static void close()
	{
		try
		{
			outStream.close();
			conn.disconnect();
		}
		catch(Exception e)
		{
			
		}
	}
		 
}
