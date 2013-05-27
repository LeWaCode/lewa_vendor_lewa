package com.lewa.search.system;

/**
 * This class defines a model class for setting infomation in system.
 * It would be swapped to a view class.
 * @author		wangfan
 * @version	2012.07.04
 */

public class SettingInfo {
	//id of setting information
	private int _id;
	//set nameInCurrentMode to chineseName in chinese language mode,
	//	to englishName in english language mode
	private String nameInCurrentMode;
	private String chineseName;
	private String englishName;
	//actionName and packageName used for opening this setting item
	private String actionName;
	private String packageName;
	
	//system language mode
	private int languageMode;

	public SettingInfo(int _id, String chineseName, String englishName,
			String actionName, String packageName) {
		super();
		this._id = _id;
		this.chineseName = chineseName;
		this.englishName = englishName;
		this.actionName = actionName;
		this.packageName = packageName;
	}

	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	public String getNameInCurrentMode() {
		return nameInCurrentMode;
	}

	public void setNameInCurrentMode(String nameInCurrentMode) {
		this.nameInCurrentMode = nameInCurrentMode;
	}

	public String getActionName() {
		return actionName;
	}

	public void setActionName(String actionName) {
		this.actionName = actionName;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public int getLanguageMode() {
		return languageMode;
	}
	
	/**
	 * This method sets language mode, it also sets current name of this item(in different language modes, there are different names).
	 * @param languageMode  language mode in system
	 */
	public void setLanguageMode(int languageMode) {
		this.languageMode = languageMode;
		
		if(languageMode == Constants.LANGUAGEMODE_CHINESE)
		{
			//set current name to chinese name
			this.nameInCurrentMode = this.chineseName;
		}
		else 
		{
			//set current name to english name
			this.nameInCurrentMode = this.englishName;
		}
	}
}
