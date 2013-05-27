package com.lewa.base.highlight;


public abstract class Decorator {
	public static final int DECORATOR_HIGHLIGHT = 0;
	public static final int  DECORATOR_HIGHLIGHT_AND_SIMPLIFIED = 1;
	
	public abstract Object getDecorated(Object object);

}
