package com.lewa.search.decorator;


/**
 * This class defines a abstract decorator for objects in the view.
 * This class has to be extended.
 * Each objects can register several decorators.
 * @author		wangfan
 * @version	2012.07.04
 */

public abstract class Decorator {
	//these three integers are decorators' types, other kinds of decorator can be expanded
	public static final int DECORATOR_HIGHLIGHT = 0;
	public static final int  DECORATOR_HIGHLIGHT_AND_SIMPLIFIED_NO_SUFFIX = 1;
	public static final int  DECORATOR_HIGHLIGHT_AND_SIMPLIFIED = 2;
	
	/**
	 * This method returns decorated object.
	 * @param object  decorated object
	 */
	public abstract Object getDecorated(Object object);

}
