package com.lewa.store.model;

public class SpecAppInfo {

	private String id, filename, chinesename;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getChinesename() {
		return chinesename;
	}

	public void setChinesename(String chinesename) {
		this.chinesename = chinesename;
	}

	@Override
	public String toString() {
		return "SpecAppInfo [id=" + id + ", filename=" + filename
				+ ", chinesename=" + chinesename + "]";
	}

}
