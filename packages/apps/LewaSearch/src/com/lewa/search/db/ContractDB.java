package com.lewa.search.db;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.text.TextUtils;

import com.lewa.search.bean.ContactFileInfo;
import com.lewa.search.system.Constants;
import com.lewa.search.system.SoftCache;
import com.lewa.search.util.SearchUtil;
import com.lewa.search.util.StringUtil;

/**
 * This class defines some methods for searching contact infomation.
 * @author		wangfan
 * @version	2012.07.04
 */

public class ContractDB {
	
	//uri for searching contacts in database
	private static Uri uri = Constants.URI_CONTACT;
	//this thumbnail caches in softCache, load when system starts
	private static Drawable thumbnail;
	//this list contains results for return
	public static List<ContactFileInfo> list = new ArrayList<ContactFileInfo>();
	
	/**
	 * This method search matched contact information
	 * @param key  key in searching contact
	 * @param sortMode  defines how to sort the list
	 */
	public static List<ContactFileInfo> searchContracts(String key, String sortMode)
	{	
		//this method call different methods according to different inputs
		boolean isNumeric = StringUtil.isNumeric(key);
		
		if(isNumeric)
		{
			//call searchContractsNormal if user input is numeric
			return searchContractsNormal(key, sortMode);
		}
		else
		{
			//call searchContractsWithAppendedUri if user input is not numeric 
			return searchContractsWithAppendedUri(key, sortMode);
		}
	}
	
	/**
	 * This method uses customized method to search matched contact information
	 * This method will match contact name and contact phone number.
	 * @param key  key in searching contact
	 * @param sortMode  defines how to sort the list
	 */
	public static List<ContactFileInfo> searchContractsNormal(String key, String sortMode)
	{
		//load thumbnail
		thumbnail = SoftCache.contractThumb;
		
		//clear list before each time of searching
		list.clear();
		//this projection tells which column are to get from database
		String[] projection = new String[] { "contact_id", "display_name", "data1", "sort_key" };  
		
		//this selectinIds records which columns have searching clauses
		//in this case, index "PROJECTION_ONE"("_display_name") and  index "PROJECTION_TWO"("data1") have searching clauses
		int[] selectionIds;
		selectionIds = new int[] { SearchUtil.PROJECTION_ONE, SearchUtil.PROJECTION_TWO };
		//get selection
		String selection = SearchUtil.getSelection(projection, selectionIds, "OR", SearchUtil.SEARCH_MODE_BLURRED);
		//get selection arguments
		String[] selectionArgs = SearchUtil.getMultipleSelectionArgs(key, selectionIds.length, SearchUtil.SEARCH_MODE_BLURRED);
	
		//search matched results from database
        Cursor cur = DBUtil.searchByKey(uri, projection, selection, selectionArgs, sortMode);
        
        //create an empty item, it is a temp item
        ContactFileInfo contactItem;
        String id;
        String text;
        String title;
        
        //load thumbnail only at the first item
        boolean loadThumb = true;
        if(cur != null && cur.moveToFirst()) 
        {
        	int index_Id = cur.getColumnIndex("contact_id");
            int index_Name = cur.getColumnIndex("display_name");  
            int index_Number = cur.getColumnIndex("data1");    
            
            do 
            {  
            	//get file information from search results
            	id = cur.getString(index_Id);  
            	title = cur.getString(index_Name); 	
            	text = cur.getString(index_Number);
           
            	if(loadThumb == true)	//create new item with thumbnail
            	{	
            		contactItem = new ContactFileInfo(id, title, text, thumbnail, Constants.CLASS_CONTRACT);    		
            		
            		//only the first item has thumbnail
            		loadThumb = false;
            	}
            	else	//create new item without thumbnail
            	{
            		
            		contactItem = new ContactFileInfo(id, title, text, null, Constants.CLASS_CONTRACT);
            	}
                
                list.add(contactItem);
            } while (cur.moveToNext());  
        }
        if(cur != null) 
        {
        	cur.close(); 
        }  
        
        return list;
	}
	
	/**
	 * This method uses method which android provides for contact searching to search matched contact information
	 * @param key  key in searching contact
	 * @param sortMode  defines how to sort the list
	 */
	public static List<ContactFileInfo> searchContractsWithAppendedUri(String key, String sortMode)
	{	
		//load thumbnail
		thumbnail = SoftCache.contractThumb;
		
		//clear list before each time of searching
		list.clear();
		//this projection tells which column are to get from database
		String[] projection = new String[] { "_id", "display_name" };
		//searching clauses are included in this uri
		Uri appendedUri = getContactFilterUri(key); 
		
		//search matched results from database
		Cursor cur = DBUtil.searchByKey(appendedUri, projection, null, null, sortMode);	 
		
		//create an empty item, it is a temp item
		ContactFileInfo contactItem;
        String id;
        String title;
        
        //load thumbnail only at the first item
        boolean loadThumb = true;
        
//        if(cur == null)
//        {
//        	return null;
//        }
        
        if(cur != null && cur.moveToFirst()) 
        {
        	int index_Id = cur.getColumnIndex("_id");
            int index_Name = cur.getColumnIndex("display_name");  
            
            do 
            {  
            	//get contact information from search results
            	id = cur.getString(index_Id);  
            	title = cur.getString(index_Name);
            	
            	List<String> numbers = null;
            	if(SoftCache.numberMapContract != null)
            	{
            		//get numbers by contact name
            		//this numbers-contact map has been cached in SoftCache
            		numbers = SoftCache.numberMapContract.get(title);
            	}
            	
            	if(numbers != null && numbers.size() != 0)	//if contact has phone number content
            	{
            		for(int i = 0; i < numbers.size(); i ++)
                	{
                		if(loadThumb == true)	//create new item with thumbnail
                    	{	
                    		contactItem = new ContactFileInfo(id, title, numbers.get(i), thumbnail, Constants.CLASS_CONTRACT);    		
                    		//only the first item has thumbnail
                    		loadThumb = false;
                    	}
                		else	//create new item without thumbnail
                    	{
                    		
                    		contactItem = new ContactFileInfo(id, title, numbers.get(i), null, Constants.CLASS_CONTRACT);
                    	}
                		
                		list.add(contactItem);
                	}
            	}
            	else	//if contact has no phone number content
            	{
            		if(loadThumb == true)	//create new item with thumbnail
                	{	
                		contactItem = new ContactFileInfo(id, title, "", thumbnail, Constants.CLASS_CONTRACT);    		
                		//only the first item has thumbnail
                		loadThumb = false;
                	}
            		else	//create new item without thumbnail
                	{
                		
                		contactItem = new ContactFileInfo(id, title, "", null, Constants.CLASS_CONTRACT);
                	}
            		
            		list.add(contactItem);
            	}
            	
            } while (cur.moveToNext());  
        }
        if(cur != null) 
        {
        	cur.close(); 
        }  
        
        return list;
	}
	
	/*
	public static String getConstractByNum(String number)
	{
		String phoneNum = PhoneUtil.StandardizePhoneNum(number);
		
		String[] projection = { "display_name", "data1" };
		int[] selectionIds = new int[] { SearchUtil.PROJECTION_ONE };
		
		String selection = SearchUtil.getSelection(projection, selectionIds, "OR", SearchUtil.SEARCH_MODE_CLEAR);
		String[] selectionArgs = SearchUtil.getMultipleSelectionArgs(phoneNum, selectionIds.length, SearchUtil.SEARCH_MODE_CLEAR);
		
		Cursor cur = DBUtil.searchByKey(uri, projection, selection, selectionArgs, null);	  
		
		String contactName = "";
		if (cur.moveToFirst()) 
        {
			int index_Name = cur.getColumnIndex("display_name");
			contactName = cur.getString(index_Name);
        }
		
		return !contactName.equals("") ? contactName : number;
	}
	*/
	
	/**
	 * This method gets number-contact map from database, number is key.
	 */
	public static Map<String, String> getPhoneMapContract()
	{
		//this projection tells which column are to get from database
		String[] projection = { "display_name", "data1" };
		//search all contact results from database
		Cursor cur = DBUtil.searchByKey(uri, projection, "", null, null);
		//create a new number-contact map
		Map<String,String> phoneContractMap = null;
		
		if (cur != null && cur.moveToFirst()) 
        {
			phoneContractMap = new HashMap<String, String>();
        	
            int index_Name = cur.getColumnIndex("display_name");  
            int index_Number = cur.getColumnIndex("data1");  
            
            String name;
            String number;
            
            do 
            {  
            	//get contact information from search results
            	name = cur.getString(index_Name); 	
            	number = cur.getString(index_Number);
               
            	//cache number-contact map
            	phoneContractMap.put(number, name);
            } while (cur.moveToNext());  
        }
		
		return phoneContractMap;
	}
	
	/**
	 * This method gets contact-phone map from database, contact is key.
	 */
	public static Map<String, List<String>> getContractMapPhone()
	{
		//this projection tells which column are to get from database
		String[] projection = { "display_name", "data1" };
		//search all contact results from database
		Cursor cur = DBUtil.searchByKey(uri, projection, "", null, null);
		//create a new number-contact map
		Map<String,List<String>> contractPhoneMap = null;
		
		if (cur != null && cur.moveToFirst()) 
        {
			contractPhoneMap = new HashMap<String, List<String>>();
        	
            int index_Name = cur.getColumnIndex("display_name");  
            int index_Number = cur.getColumnIndex("data1");  
            
            String name;
            String number;
            
            do 
            {  
            	//get contact information from search results
            	name = cur.getString(index_Name); 	
            	number = cur.getString(index_Number);
               
            	//get numberList by name(a contact item can get several numbers)
            	List<String> numberList = contractPhoneMap.get(name);
            	
            	//add number to numberList
            	if(numberList == null)
            	{
            		numberList = new ArrayList<String>();
            		numberList.add(number);
            		contractPhoneMap.put(name, numberList);
            	}
            	else
            	{
            		numberList.add(number);
            	}
            	
            } while (cur.moveToNext());  
        }
		
		return contractPhoneMap;
	}
	
	//  begin-----   refered from PIM   -----//
	public static Uri getContactFilterUri(String filter) 
	{
        Uri baseUri;
        if(!TextUtils.isEmpty(filter))
        {
            baseUri = Uri.withAppendedPath(Contacts.CONTENT_FILTER_URI, Uri.encode(filter));
        } 
        else 
        {
            baseUri = Contacts.CONTENT_URI;
        }
       
        return baseUri;
    }
	//  end-----   refered from PIM   -----//
}	
