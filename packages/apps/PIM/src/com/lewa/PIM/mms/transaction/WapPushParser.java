package com.lewa.PIM.mms.transaction;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

public class WapPushParser {

	private byte[] data;
	private String SI;
	private String title;
	private String content;

	Stack<String> currentTag = new Stack<String>();

	public WapPushParser(byte[] d) throws Exception
	{
		data = d; 
		SI = "";
		title = "";
		content = "";

		currentTag.clear();
		
		Parser();
	}
	
	public String getSI()
	{
		return SI;
	}
	
	public String getTitle()
	{
		
		return title;
	}
	
	public String getContent()
	{
		
		return content;
	}
	
	private void Parser() throws Exception
	{
		byte[] txt = null;
		String buf = "";
		int p = 0;
		boolean isReadStringMode = false;
		boolean isUrl = false;
		boolean needQuotationmark = false;
		
		if (data.length == 0)
		{
			throw new Exception("data zero length");
		}
		
		SI = "<?xml version=\"1.0\" encoding=\"\"?>";

		//transactionID = data[0];
	
		for (int i = 1; i < data.length; ++i)
		{
			if (isReadStringMode)
			{
				switch(data[i])
				{
					case (byte)0x00:
						isReadStringMode = false;
						txt = new byte[i - p];
						System.arraycopy(data, p, txt, 0, i - p);
						if (isUrl)
						{
							SI += URLEncoder.encode(new String(txt, "UTF-8"), "UTF-8");
							isUrl = false;
						}
						else
						{
							SI += new String(txt, "UTF-8");
						}
						
						if (needQuotationmark)
						{
							SI += "\"";
							needQuotationmark = false;
						}

						break;
						
					default:
						break;				
				}
			}
			else
			{
				switch(data[i])
				{
					case (byte)0x05:
						SI += "<!DOCTYPE si PUBLIC \"-//WAPFORUM//DTD SI 1.0//EN\" \"http://www.wapforum.org/DTD/si.dtd\">";
						break;
					
					case (byte)0x6A:
						SI = SI.replaceAll("encoding=\"\"", "encoding=\"UTF-8\"");
						break;
					
					case (byte)0x00:
					case (byte)0x07:
					case (byte)0x08:
						break;
					
					case (byte)0x03:
						isReadStringMode = true;
						txt = null;
						p = i + 1;
						break;
										
					case (byte)0x45:
						SI += "<si>";
						currentTag.add("si");
						break;
					
					case (byte)0xC6:
						SI += "<indication ";
						currentTag.add("indication");
						currentTag.add(">");
						break;
					
					case (byte)0x0C:
						SI += "href=\"http://";
						isUrl = true;
						needQuotationmark = true;
						break;
					
					case (byte)0xC3:
						break;
					
					case (byte)0x01:
						buf = currentTag.pop();
						if (buf.equals(">"))
						{
							SI += ">";
						}
						else
						{
							SI += "</" + buf +">";
						}
						break;
					
					default:
						buf = Integer.toHexString(0xFF & data[i]);
						if (buf.length() == 1)
						{
							buf = "0" + buf;
						}
						
						throw new Exception("Unkonwn byte: 0x" + buf.toUpperCase() + " pos: " + i);
				}
			}
		}
		
		StringReader sr = new StringReader(SI); 
		InputSource is = new InputSource(sr); 
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(is);
		
		Element root = doc.getDocumentElement();
		NodeList nodeList = root.getElementsByTagName("indication");
		Element nd = (Element)nodeList.item(0);
		content = URLDecoder.decode(nd.getAttribute("href"), "UTF-8");
		title = nd.getFirstChild().getNodeValue();
	}
}
