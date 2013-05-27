package com.lewa.store.extras;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.lewa.store.model.SpecAppInfo;

public class XmlContentHandler extends DefaultHandler {
	
	private String TAG=XmlContentHandler.class.getSimpleName();

	private String ELEMENT_NAME="package";
	private String ELEMENT_PROPERTY_ID="id";
	private String ELEMENT_FILENAME="filename";
	private String ELEMENT_CHINESENAME="chinesename";
	private String id,filename, chinesename;
	private String tagName;
	
	private List<SpecAppInfo> specApps =null;  
	private SpecAppInfo info;

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		String chars = new String(ch, start, length).trim(); 
		if(chars!=null){
			if (tagName.equals(ELEMENT_FILENAME)) {
				info.setFilename(chars.trim());
				filename=chars;
			} else if (tagName.equals(ELEMENT_CHINESENAME)) {
				info.setChinesename(chars.trim());
				chinesename=chars;
			} 	
		}		
	}	

	@Override
	public void endDocument() throws SAXException {
		// TODO Auto-generated method stub
		super.endDocument();
	}

	@Override
	public void endElement(String uri, String localName, String name)
			throws SAXException {
		tagName = "";
		if (localName.equals(this.ELEMENT_NAME)) {
			specApps.add(info);
		}
	}

	@Override
	public void startDocument() throws SAXException {
		// TODO Auto-generated method stub
		super.startDocument();
		specApps= new ArrayList<SpecAppInfo>();
	}

	@Override
	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {
		
		tagName = localName;
		if (localName.equals(this.ELEMENT_NAME)) {
//			Log.e(TAG,"length="+attributes.getLength());
//			Log.e(TAG,"value=="+attributes.getValue("id"));
			info=new SpecAppInfo();
			for (int i = 0; i < attributes.getLength(); i++) {
//				System.out.println(attributes.getLocalName(i) + "=" + attributes.getValue(i));
			    id=attributes.getValue(i).trim();	
			    info.setId(id);	
			}
		}
	}
	
	public ArrayList<SpecAppInfo> getSpecApps(){
		return (ArrayList<SpecAppInfo>) this.specApps;
	}

	public void printout() {
		System.out.print("id:");
		System.out.println(id);
		System.out.print("filename:");
		System.out.println(filename);
		System.out.print("chinesename:");
		System.out.println(chinesename);
	}
}