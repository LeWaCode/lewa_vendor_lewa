package com.lewa.providers.location;

/*
 * Usage:
 * String s = PhoneLocation.getLocationFromPhone("13501340000");
 * String[] loc = null;
 * if (s != null && !s.equals(" ")) {
 *     loc = s.split(",");
 * }
 * if (loc != null) {
 *     System.out.println("location code: " + loc[0]);
 *     System.out.println("location name: " + loc[1]);
 * }
 */

public final class PhoneLocation {
	public static int INFO_INDEX_LOCATION  = 0;
	public static int INFO_INDEX_AREACODE  = 1;
	public static int INFO_INDEX_CARD_TYPE = 3;
    static {
        System.loadLibrary("phoneloc_jni");
    }

    static native String getPhoneLocationJni(String phone);

    private static String _phone, _location;

    public synchronized static String getLocationFromPhone(String num) {
        if (num == null) return null;
        if (num.equals(_phone)) return _location;
        _phone = num;
        _location = getPhoneLocationJni(num);
        return _location;
    }

    private static String _getPosFromPhone(String num, int i) {
        String s = getLocationFromPhone(num);
        String[] loc = (s != null) ? s.split(",") : new String[0];
        if (loc.length == 3) {
            return loc[i];
        }
        return null;
    }
    
    private static String[] _getPosFromPhone(String num) {
        String s = getLocationFromPhone(num);
        String[] loc = (s != null) ? s.split(",") : new String[0];
        if (loc.length == 2) {
            return loc;
        }
        return null;
    }

    public static String[] getCityAndCardTypeFromPhone(String num) {
        return _getPosFromPhone(num);
    }
    
    public static String getCityFromPhone(String num) {
        return _getPosFromPhone(num, 1);
    }

    public static String getCodeFromPhone(String num) {
        return _getPosFromPhone(num, 0);
    }

}
