package com.lewa.PIM.IM.util;

import java.util.Hashtable;

class StaticSingleton extends Singleton
{
  private static Hashtable _instances = new Hashtable();
  private Object _inst;
  private long _runtime_key;

  public void clear()
  {
    this._inst = null;
    String str = this._key;
    _instances.remove(str);
  }

  protected void init(String paramString, Class paramClass)
  {
    this._key = paramString;
    this._clstype = paramClass;
  }

  public Object instance()
  {
    int i = 0;
    if (this._inst != null)
    {
      return this._inst;
    }
    if(!this._clstype.isInstance(((Hashtable)_instances).get(this._key)))
    {
      try
      {
        this._inst = this._clstype.newInstance();;
        ((Hashtable)_instances).put(this._key, this._inst);
        
      }
      catch (IllegalAccessException localIllegalAccessException)
      {
      }
      catch (InstantiationException localInstantiationException)
      {
      }
      finally {
    	  return this._inst;
      }
    }
    else {
    	this._inst = ((Hashtable)_instances).get(this._key);
    	return this._inst;
    }
  }
}