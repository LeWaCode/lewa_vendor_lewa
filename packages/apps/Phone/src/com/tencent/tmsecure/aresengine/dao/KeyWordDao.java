package com.tencent.tmsecure.aresengine.dao;

import java.util.ArrayList;
import java.util.List;

import com.tencent.tmsecure.module.aresengine.IKeyWordDao;


public class KeyWordDao implements IKeyWordDao {
	private static List<String> mWords = new ArrayList<String>();
	private static KeyWordDao mKeyWordDao;
	private static final String STATIC_KEYWORDS[] =	{
		"", "", "", "hot girl"
	};
	
	private KeyWordDao() {
		reset();
	}
	
	public void reset() {
		mWords.clear();
		for (final String word:STATIC_KEYWORDS)	{
			mWords.add(word);
		}
	}

	public static KeyWordDao getInstance() {
		if (null == mKeyWordDao) {
			synchronized (KeyWordDao.class) {
				mKeyWordDao = new KeyWordDao();
			}
		}
		return mKeyWordDao;
	}
	
	@Override
	public boolean contains(String msg) {
		for (String word : mWords) {
			if (msg.contains(word)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public ArrayList<String> getAll() {
		return (ArrayList<String>) mWords;
	}

	@Override
	public void setAll(List<String> words) {
		mWords = words;
	}

}
