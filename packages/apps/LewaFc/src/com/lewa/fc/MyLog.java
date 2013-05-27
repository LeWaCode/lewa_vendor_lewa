package com.lewa.fc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.os.Environment;


public class MyLog{
	
	private static File logFile;
	private static String errorText = "";
	
	static
	{
		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
		String name = "log.txt";
		logFile = new File(path + name);
	}
	
	//获取 log 日志的线程,保存成 text
	public static void logWrite() 
	{
		FileOutputStream fos = null;
		InputStreamReader isr = null;
		
		ArrayList<String> cmdLine = new ArrayList<String>();   
		cmdLine.add("logcat");                
		cmdLine.add("-d");   
		//cmdLine.add("*:E");
		
		//ArrayList<String> clearLog = new ArrayList<String>();           
		//clearLog.add("logcat");                
		//clearLog.add("-c");         
		
		try
		{	
			Process process = Runtime.getRuntime().exec(cmdLine.toArray(new String[cmdLine.size()]));   //捕获日志                
			isr = new InputStreamReader(process.getInputStream());
			BufferedReader bufferedReader = new BufferedReader(isr, 1024);               
			Runtime.runFinalizersOnExit(true);                
			String str = null;   

			//fos = new FileOutputStream(logFile);
			boolean tag = false;
			
			while((str = bufferedReader.readLine()) != null)    //开始读取日志，每次读取一行                
			{                   
				//Runtime.getRuntime().exec(clearLog.toArray(new String[clearLog.size()]));  
				//清理日志....这里至关重要，不清理的话，任何操作都将产生新的日志，代码进入死循环，直到bufferreader满          

				if(str.contains("FATAL EXCEPTION"))
				{
					tag = true;
					errorText = "";
				}
				
				if(str.startsWith("E") && tag == true)
				{                                  
					errorText += (str + "\n");
				}
				else
				{
					if(!str.equals(""))
					{
						tag = false;
					}
				}
			}
	
			isr.close();
			bufferedReader.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public static String getErrorText() {
		return errorText;
	}

	public static void setErrorText(String errorText) {
		MyLog.errorText = errorText;
	}
	
	/*
	public static StringBuilder logRead()
	{
		StringBuilder text = new StringBuilder();
		FileInputStream fis;
		InputStreamReader isr;
		BufferedReader bufferedReader;
		try
		{
			fis = new FileInputStream(logFile);
			isr = new InputStreamReader(fis);
			bufferedReader = new BufferedReader(isr);
			
			boolean tag = false;
			
			Runtime.runFinalizersOnExit(true);                
			String str = null;   

			while((str = bufferedReader.readLine()) != null)        
			{      
				System.out.println(str);
				if(str.contains("FATAL EXCEPTION"))
				{
					tag = true;
					text = new StringBuilder();
				}
				
				if(str.startsWith("E") && tag == true)
				{                                  
					text.append(str + "\n");
				}
				else
				{
					if(!str.equals(""))
					{
						tag = false;
					}
				}
			} 	
			
			fis.close();
			isr.close();
			bufferedReader.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return text;
	}
	*/

	/*
	public static File getLogFile() {
		return logFile;
	}

	public static void setLogFile(File logFile) {
		MyLog.logFile = logFile;
	}
	*/
	
}
