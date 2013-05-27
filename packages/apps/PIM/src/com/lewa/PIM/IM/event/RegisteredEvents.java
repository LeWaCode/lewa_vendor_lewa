package com.lewa.PIM.IM.event;

import java.util.Hashtable;

public class RegisteredEvents
{
  private int eventId;
  public Hashtable events;
  public Hashtable listenerBindings;
  private Object regLock;

  public RegisteredEvents()
  {
    this.regLock = new Object();
    this.events = new Hashtable();
    this.listenerBindings = new Hashtable();
    this.eventId = 0;
  }

  public int nextEventId()
  {
    synchronized (this.regLock)
    {
      int i = this.eventId;
      i++;
      this.eventId = i;
      return i;
    }
  }
}
