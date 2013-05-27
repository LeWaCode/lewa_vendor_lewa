package com.lewa.player.online;

public class Argument implements Comparable<Object> {

	public String Name;
	public String Value;
	
	public Argument(String name,String value){
		this.Name = name;
		this.Value = value;
	}

	public int compareTo(Object o) {
		
		return this.Name.compareTo(((Argument)o).Name);
	}

}
