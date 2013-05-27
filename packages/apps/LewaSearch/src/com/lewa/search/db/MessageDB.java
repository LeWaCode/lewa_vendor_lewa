package com.lewa.search.db;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Telephony.Sms.Conversations;
import android.provider.Telephony.Threads;

import com.lewa.search.bean.MessageFileInfo;
import com.lewa.search.system.Constants;
import com.lewa.search.system.SoftCache;
import com.lewa.search.util.PhoneUtil;
import com.lewa.search.util.SearchUtil;
import com.lewa.search.util.StringUtil;

/**
 * This class defines a method for searching messages from database.
 * @author		wangfan
 * @version	2012.07.04
 */
public class MessageDB {

	//uri for searching messages in database
	private static Uri uri = Constants.URI_MESSAGE;
	
	//this thumbnail caches in softCache, load when system starts
	private static Drawable thumbnail;
	//this list contains results for return
	public static List<MessageFileInfo> list = new ArrayList<MessageFileInfo>();
	
	/**
	 * This method search matched messages
	 * @param key  key in searching messages
	 * @param sortMode  defines how to sort the list
	 */
	public static List<MessageFileInfo> searchMessages(String key, String sortMode)
	{
		//load thumbnail
		thumbnail = SoftCache.messageThumb;
		
		//clear list before each time of searching
		list.clear();
		//this projection tells which columns are to get from database
		//the "address" returns phone number of this message
		String[] projection = new String[] { "_id", "address", "body", "thread_id" };
		//this selectinIds records which columns have searching clauses
		int[] selectionIds;
		
		boolean isNumeric = StringUtil.isNumeric(key);
		if(isNumeric)	//if search key is numeric
		{
			//index "PROJECTION_ONE"("address") and index "PROJECTION_TWO"("body") have searching clauses
			selectionIds = new int[] { SearchUtil.PROJECTION_ONE, SearchUtil.PROJECTION_TWO };
		}
		else
		{
			//index "PROJECTION_TWO"("body") has searching clause
			selectionIds = new int[] { SearchUtil.PROJECTION_TWO };
		}
		
		//get selection
		String selection = SearchUtil.getSelection(projection, selectionIds, "OR", SearchUtil.SEARCH_MODE_BLURRED);
		
		//get selection arguments
		String[] selectionArgs = SearchUtil.getMultipleSelectionArgs(key, selectionIds.length, SearchUtil.SEARCH_MODE_BLURRED);
		
		//search matched results from database
        Cursor cur = DBUtil.searchByKey(uri, projection, selection, selectionArgs, sortMode);
        
        //create an empty item, it is a temp item
        MessageFileInfo messageItem;
        String id;
        String number;
        String threadId;
        String text;
        String title;
        
        //load thumbnail only at the first item
        boolean loadThumb = true;
        if(cur != null && cur.moveToFirst()) 
        {
        	int index_Id = cur.getColumnIndex("_id");
            int index_Address = cur.getColumnIndex("address");  
            int index_Body = cur.getColumnIndex("body"); 
            int index_ThreadId = cur.getColumnIndex("thread_id");
            
            do 
            {  
            	//get message information from search results
            	id = cur.getString(index_Id);  
                number = cur.getString(index_Address);
                threadId = cur.getString(index_ThreadId);
                
                if(number != null)
                {
                	//standardize phone number
                	String address = PhoneUtil.StandardizePhoneNum(number);
                	
                	if(SoftCache.contractMapNumber != null)
                	{
                		//get contact information by phone number
                		title = SoftCache.contractMapNumber.get(address);
                	}
                	else
                	{
                		title = address; 
                	}
                	
                	if(title == null || title.equals(""))
                	{
                		title = address; 
                	}
                	
                	text = cur.getString(index_Body);
                   
                    if(title.toLowerCase().contains(key.toLowerCase()) || (text != null &&text.toLowerCase().contains(key.toLowerCase())))
                    {
                    	if(loadThumb == true)	//create new item with thumbnail
                    	{
                    		messageItem = new MessageFileInfo(id, title, text, thumbnail, 
                    				threadId, number, Constants.CLASS_MESSAGE);
                    		//only the first item has thumbnail
                    		loadThumb = false;
                    	}
                    	else	//create new item without thumbnail
                    	{
                    		messageItem = new MessageFileInfo(id, title, text, null, 
                    				threadId, number, Constants.CLASS_MESSAGE);
                    	}
                    	
                    	list.add(messageItem);
                    }
                }
            	
                
            } while (cur.moveToNext());
        }
        if(cur != null) 
        {
        	cur.close(); 
        }  
       
        return list;
	}
	
	//   begin-----   refered from PIM   -----//
	private static final String[] ALL_THREADS_PROJECTION = {
        Threads._ID, Threads.DATE, Threads.MESSAGE_COUNT, Threads.RECIPIENT_IDS,
        Threads.SNIPPET, Threads.SNIPPET_CHARSET, Threads.READ, Threads.ERROR,
        Threads.HAS_ATTACHMENT
    };   
    
    private static final Uri sAllThreadsUri =
        Threads.CONTENT_URI.buildUpon().appendQueryParameter("simple", "true").build();
    

    public static boolean isGroupThread(int thread){
        boolean isGroup = false;

        Cursor cursor = DBUtil.searchByKey(
                sAllThreadsUri,
                ALL_THREADS_PROJECTION,
                "_id = '" + thread +"'",
                null,
                Conversations.DEFAULT_SORT_ORDER);
        
        if (cursor != null && cursor.moveToFirst()) {
            int recipientId = cursor.getColumnIndex(Threads.RECIPIENT_IDS);
            String recipientString = cursor.getString(recipientId);
            String[] list = recipientString.split(" ");
            isGroup = list.length > 1 ? true : false;
        }
        
        cursor.close();
        
        return isGroup;
    }
    //  end-----   refered from PIM   -----//
}
