package com.lewa.PIM.IM.util;

public abstract class Singleton
{
  private static Class _type = StaticSingleton.class;
  protected Class _clstype;
  protected String _key;

  public static Singleton make(String paramString, Class paramClass)
  {
    Singleton localSingleton = null;
    try
    {
      localSingleton = (Singleton)_type.newInstance();
      if (localSingleton != null)
        localSingleton.init(paramString, paramClass);     
    }
    catch (InstantiationException localInstantiationException)
    {
    }
    catch (IllegalAccessException localIllegalAccessException)
    {
    }
    finally {
    	return localSingleton;
    }
  }

  public static void setType(Class paramClass)
  {
    if (!Singleton.class.isAssignableFrom(paramClass))
      throw new IllegalArgumentException("Invalid type specified");
    _type = paramClass;
  }

  public abstract void clear();

  public String getKey()
  {
    return this._key;
  }

  public Class getType()
  {
    return this._clstype;
  }

  protected abstract void init(String paramString, Class paramClass);

  public abstract Object instance();

  public void setInstanceType(Class paramClass)
  {
    this._clstype = paramClass;
  }
}
