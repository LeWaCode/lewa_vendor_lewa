package com.lewa.store.extras;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.lewa.store.activity.AppListActivity;
import com.lewa.store.model.SpecAppInfo;

public class GooglePackages {

	private String TAG = GooglePackages.class.getSimpleName();

	private String packageName;
	private String fileName;
	private Map<String, String> fileMap;// <包名，文件名>
	private Map<String, String> fileNameMap;// <包名,中文名>
	public List<String> googlePackageList;
	
	private ArrayList<SpecAppInfo> list;

	public GooglePackages() {
		fileNameMap = new HashMap<String, String>();
		fileMap = new HashMap<String, String>();
		googlePackageList = new ArrayList<String>();
		new Thread(new XmlRunnable()).start();
	}

	class XmlRunnable implements Runnable {

		@Override
		public void run() {
			XmlContentHandler xmlHandler=null;
			try {
				SAXParserFactory factory = SAXParserFactory.newInstance();
				XMLReader reader = factory.newSAXParser().getXMLReader();
				xmlHandler = new XmlContentHandler();
				reader.setContentHandler(xmlHandler);
				reader.parse(new InputSource(new StringReader(AppListActivity.xmlContent)));
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				if(null!=xmlHandler){
					list= xmlHandler.getSpecApps();	
					for(SpecAppInfo info:list){
						fileMap.put(info.getId().trim(),info.getFilename().trim());
					}
					setGooglePackageNameList();
					setFileNameMap();
				}
			}
		}
	}
	
	public void setFileNameMap(){
		for(SpecAppInfo info : list){
			if(!fileNameMap.containsKey(info.getId())){
				fileNameMap.put(info.getId(),info.getChinesename());
			}
		}
	}

	public void setGooglePackageNameList() {
		for(SpecAppInfo info : list){
			if (!googlePackageList.contains(info.getId())) {
				googlePackageList.add(info.getId());
			}
		}
	}

	public Map<String, String> getGoogleSpecPackages() {
		return this.fileMap;
	}

	public Map<String, String> getGoogleAppNames() {
		return this.fileNameMap;
	}

	public List<String> getGooglePackageNameList() {
		return this.googlePackageList;
	}

	@Override
	public String toString() {
		return "GooglePackages [packageName=" + packageName + ", fileName="
				+ fileName + ", fileMap=" + fileMap + "]";
	}
}
