package com.lewa.PIM.IM.event;

import java.lang.ref.WeakReference;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;
import com.lewa.PIM.IM.util.Singleton;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class EventManager {
	private static EventManager _inst;
	private static Singleton _registered = Singleton.make(
			"Lewa/EventManager/RegisteredEvents", RegisteredEvents.class);
	private Object _queueLock;
	private Object _setupLock;

	static {
		_inst = new EventManager();
	}

	protected EventManager() {
		this._queueLock = new Object();
		this._setupLock = new Object();
	}

	public static void clear() {
		_registered.clear();
	}

	public static EventManager inst() {
		return _inst;
	}

	public void cancelAllEvents(PendingIntent paramPendingIntent) {
		Enumeration localEnumeration = registered().events.keys();
		while (localEnumeration.hasMoreElements()) {
			int i = ((Integer) localEnumeration.nextElement()).intValue();
			cancelEvent(paramPendingIntent, i);
		}
	}

	public void cancelEvent(int paramInt) {
		RegisteredEvents localRegisteredEvents = registered();
		boolean EventExist = ((Hashtable) localRegisteredEvents.events)
				.containsKey(new Integer(paramInt));
		if (EventExist) {
			synchronized (localRegisteredEvents.listenerBindings) {
				Vector localVector = (Vector) ((Hashtable) localRegisteredEvents.listenerBindings)
						.get(new Integer(paramInt));
				localVector.removeAllElements();
			}
		}
	}
	
	public void cancelEvent(PendingIntent paramPendingIntent, int paramInt) {
		RegisteredEvents localRegisteredEvents = registered();
		boolean EventExist = ((Hashtable) localRegisteredEvents.events)
				.containsKey(new Integer(paramInt));
		if (EventExist) {
			synchronized (localRegisteredEvents.listenerBindings) {

				Vector localVector = (Vector) ((Hashtable) localRegisteredEvents.listenerBindings)
						.get(new Integer(paramInt));

				if (localVector.size() > 0) {
					for (int i = localVector.size() - 1; i >= 0; i--) {

						requsetlistenPair localListenerPair = (requsetlistenPair) localVector
								.elementAt(i);
						if (localListenerPair == null) {
							localVector.removeElementAt(i);
						} else {
							if (localListenerPair.mPendingIntent == paramPendingIntent) {
								localVector.removeElementAt(i);
							}
						}
					}
				}
			}
		}
	}

	public void cancelEvent(PendingIntent paramPendingIntent,
			int[] paramArrayOfInt) {
		int j = 0;
		for (int i = 0; i < paramArrayOfInt.length; i++) {
			j = paramArrayOfInt[i];
			cancelEvent(paramPendingIntent, j);
		}
	}

	public void dumpListeners() {
		String str1 = "EventManager";
		RegisteredEvents localRegisteredEvents = registered();
		Log.d(str1, "-- Event Types --");
		Enumeration localEnumeration = localRegisteredEvents.events.keys();
		while (localEnumeration.hasMoreElements()) {
			Integer localInteger1 = (Integer) localEnumeration.nextElement();
			StringBuilder localStringBuilder1 = new StringBuilder();
			Object localObject = localRegisteredEvents.events
					.get(localInteger1);
			String str2 = localObject + " -> " + localInteger1;
			Log.d(str1, str2);
		}
		Log.d(str1, "-- Listeners --");
		localEnumeration = localRegisteredEvents.listenerBindings.keys();
		while (localEnumeration.hasMoreElements()) {
			Integer localInteger2 = (Integer) localEnumeration.nextElement();
			String str3 = (String) localRegisteredEvents.events
					.get(localInteger2);
			Vector localVector = (Vector) localRegisteredEvents.listenerBindings
					.get(localInteger2);
			if (localVector.isEmpty())
				continue;
			String str4 = "[" + str3 + "]";
			Log.d(str1, str4);
			int i = 0;
			int j = localVector.size();
			PendingIntent localPendingIntent;
			if (i < j) {
				requsetlistenPair localListenerPair = (requsetlistenPair) localVector
						.elementAt(i);
				localPendingIntent = localListenerPair.mPendingIntent;
				if (localPendingIntent != null)
					continue;
				Log.d(str1, "(WEAK REF CLEARED)");
				localVector.removeElementAt(i);
				i--;
			}
		}
	}

	public void fireEvent(Context context, int paramInt, Intent resultIntent) {
		RegisteredEvents localRegisteredEvents = registered();
		boolean EventExist = ((Hashtable) localRegisteredEvents.events)
				.containsKey(new Integer(paramInt));
		if (EventExist) {
			synchronized (localRegisteredEvents.listenerBindings) {

				Vector localVector = (Vector) ((Hashtable) localRegisteredEvents.listenerBindings)
						.get(new Integer(paramInt));
				if (localVector.size() > 0) {
					for (int i = localVector.size() - 1; i >= 0; i--) {
						requsetlistenPair localListenerPair = (requsetlistenPair) localVector
								.elementAt(i);
						if (localListenerPair != null
								&& localListenerPair.mPendingIntent != null) {
							try {
								localListenerPair.mPendingIntent.send(context,
										0, resultIntent, null, null);
							} catch (PendingIntent.CanceledException error) {
								Log.e("EventManager", "catch CanceledException");
							}
						}
					}
				}
			}
		}
	}

	public int getEventIDForType(String paramString) {
		RegisteredEvents localRegisteredEvents = registered();
		synchronized (localRegisteredEvents.events) {
			boolean bool = localRegisteredEvents.events
					.containsKey(paramString);
			if (bool) {
				Object localObject1 = localRegisteredEvents.events;
				Integer localInteger = (Integer) ((Hashtable) localObject1)
						.get(paramString);
				if (localInteger != null) {
					return localInteger.intValue();
				}
			}
			return -1;
		}
	}

	public void listenForEvent(PendingIntent paramPendingIntent, int paramInt) {
		RegisteredEvents localRegisteredEvents = registered();
		synchronized (localRegisteredEvents.listenerBindings) {
			Object localObject1 = localRegisteredEvents.listenerBindings;
			Integer localInteger1 = new Integer(paramInt);
			Vector localVector = (Vector) ((Hashtable) localObject1)
					.get(localInteger1);
			if (localVector == null) {
				localVector = new Vector();
				((Hashtable) localRegisteredEvents.listenerBindings).put(
						new Integer(paramInt), localVector);
			}

			localObject1 = new requsetlistenPair(paramPendingIntent,
					System.currentTimeMillis());
			localVector.addElement(localObject1);
			return;
		}
	}

	public void listenForEvent(PendingIntent paramPendingIntent,
			Vector paramVector) {
		for (int i = 0;; i++) {
			int j = paramVector.size();
			if (i >= j)
				break;
			int k = ((Integer) paramVector.elementAt(i)).intValue();
			listenForEvent(paramPendingIntent, k);
		}
	}

	public void listenForEvent(PendingIntent paramPendingIntent,
			int[] paramArrayOfInt) {
		int localObject = 0;
		while (true) {
			int i = paramArrayOfInt.length;
			if (localObject >= i)
				break;
			int j = paramArrayOfInt[localObject];
			listenForEvent(paramPendingIntent, j);
			localObject++;
		}
	}

	public int getListenerPendingIntentCount(int paramInt) {
		Integer tempInteger = new Integer(paramInt);
		RegisteredEvents localRegisteredEvents = registered();
		if (localRegisteredEvents.events.containsKey(tempInteger)) {
			if (((Hashtable) localRegisteredEvents.listenerBindings)
					.containsKey(tempInteger)) {
				Vector localVector = (Vector) ((Hashtable) localRegisteredEvents.listenerBindings)
						.get(tempInteger);
				return localVector.size();
			} else {
				return 0;
			}
		} else {
			return 0;
		}
	}

	public String lookupType(int paramInt) {
		Hashtable localHashtable = registered().events;
		Integer localInteger = new Integer(paramInt);
		return (String) localHashtable.get(localInteger);
	}

	public int registerEventType(String paramString) {
		RegisteredEvents localRegisteredEvents = registered();
		synchronized (localRegisteredEvents.events) {
			int i = paramString.hashCode();
			if (!localRegisteredEvents.events.containsKey(new Integer(i))) {
				String str1 = "Registering event " + paramString;
				Log.i("Event Queue", str1);
				localRegisteredEvents.events.put(new Integer(i), paramString);
				if (!((Hashtable) localRegisteredEvents.listenerBindings)
						.containsKey(new Integer(i))) {
					((Hashtable) localRegisteredEvents.listenerBindings).put(
							new Integer(i), new Vector());
				}
			} else {
				String str2 = "Re-registering event " + paramString;
				Log.i("Event Queue", str2);
			}
			return i;
		}
	}

	protected RegisteredEvents registered() {
		return (RegisteredEvents) _registered.instance();
	}

	final static class requsetlistenPair {
		public PendingIntent mPendingIntent;
		public long mRequestStartTime;

		public requsetlistenPair(PendingIntent paramPendingIntent,
				long paramRequestTime) {
			mPendingIntent = paramPendingIntent;
			mRequestStartTime = paramRequestTime;
		}
	}

}
