package com.lewa.PIM.contacts;

import java.util.ArrayList;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.SipAddress;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.Website;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;

public class ContactHelper {
    public static final String[] PROJECTION_CONTACTS = { Contacts._ID };
    private static final String[] PROJECTION_PHONENUMBER = {Phone.TYPE, Phone.NUMBER };
    private static final String[] PROJECTION_DISPLAYNAME = { StructuredName.DISPLAY_NAME };
    private static final String[] PROJECTION_EMAIL = { Email.TYPE, Email.DATA1 };
    private static final String[] PROJECTION_IM = new String[] {Im.PROTOCOL, Im.DATA };
    private static final String[] PROJECTION_ORGANIZATION = new String[] {Organization.TYPE, Organization.COMPANY, Organization.TITLE };
    private static final String[] PROJECTION_NICKNAMES = new String[] {Nickname.NAME };
    private static final String[] PROJECTION_WEBSITES = new String[] {Website.URL};
    private static final String[] PROJECTION_SIP = new String[] {SipAddress.DATA1};
    private static final String[] PROJECTION_NOTES = new String[] { Note.NOTE };
    private static final String[] PROJECTION_ADDRESS = new String[] {
        StructuredPostal.TYPE, StructuredPostal.STREET, 
        StructuredPostal.CITY, StructuredPostal.REGION, 
        StructuredPostal.POSTCODE, StructuredPostal.COUNTRY, 
        StructuredPostal.POBOX, StructuredPostal.NEIGHBORHOOD 
    };

    public static ArrayList<ArrayList<String>> querySimiliarIds(Context context) {
        ContentResolver cr = context.getContentResolver();
        ArrayList<ArrayList<String>> totalIds = new ArrayList<ArrayList<String>>();      
        Cursor cursor = null;
        try {
            cursor = cr.query(RawContacts.CONTENT_URI, new String[] {"_id", "display_name"}, 
                    "deleted = 0) AND Upper(REPLACE(display_name,  ' ', '')) IN (select Upper(REPLACE(display_name,  ' ', '')) from raw_contacts where deleted = 0 GROUP BY Upper(REPLACE(display_name,  ' ', '')) HAVING Count(*) > 1",
                    null, "Upper(REPLACE(display_name,  ' ', '')) DESC");
            String pre = null;
            ArrayList<String> ids = null;
            while (cursor.moveToNext()) {
                String cur = cursor.getString(1).replace(" ", "");
                if (cur != null && !cur.equalsIgnoreCase(pre)) {
                    if (ids != null && ids.size() != 0 && !totalIds.contains(ids)) { 
                        totalIds.add(ids);
                    }
                    ids = new ArrayList<String>();
                    pre = cur;
                }
                ids.add(cursor.getString(0));
                if (cursor.isLast()) {
                    totalIds.add(ids);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return totalIds;
    }
    
    public static ContactInfo getSimiliarContactInfo(Context context, ArrayList<String> ids) {
        ContentResolver cr = context.getContentResolver();
        Cursor c = null; 
        String[] contentValue = null;
        ContactInfo info = new ContactInfo();

        try {
            c = cr.query(Data.CONTENT_URI, null, Data.RAW_CONTACT_ID +" in (" + combineIds(ids) + ")", null, null);
            while (c.moveToNext()) {
                String mimeType = c.getString(c.getColumnIndex(Data.MIMETYPE));
                if (StructuredName.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    contentValue = getStringInContactCursor(c, PROJECTION_DISPLAYNAME);
                    info.fieldNameMap.get("displayname").add(contentValue);
                } else if (Phone.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    contentValue = getStringInContactCursor(c, PROJECTION_PHONENUMBER);
                    info.fieldNameMap.get("phone").add(contentValue);
                } else if (Email.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    contentValue = getStringInContactCursor(c, PROJECTION_EMAIL);
                    info.fieldNameMap.get("email").add(contentValue);
                } else if (Im.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    contentValue = getStringInContactCursor(c, PROJECTION_IM);
                    info.fieldNameMap.get("im").add(contentValue);
                } else if (StructuredPostal.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    contentValue = getStringInContactCursor(c, PROJECTION_ADDRESS);
                    info.fieldNameMap.get("postal").add(contentValue);
                } else if (Organization.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    contentValue = getStringInContactCursor(c, PROJECTION_ORGANIZATION);
                    info.fieldNameMap.get("orgnization").add(contentValue);
                } else if (Nickname.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    contentValue = getStringInContactCursor(c, PROJECTION_NICKNAMES);
                    info.fieldNameMap.get("nickname").add(contentValue);
                } else if (Website.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    contentValue = getStringInContactCursor(c, PROJECTION_WEBSITES);
                    info.fieldNameMap.get("website").add(contentValue);
                } else if (SipAddress.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    contentValue = getStringInContactCursor(c, PROJECTION_SIP);
                    info.fieldNameMap.get("sip").add(contentValue);
                } else if (Note.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    contentValue = getStringInContactCursor(c, PROJECTION_NOTES);
                    info.fieldNameMap.get("note").add(contentValue);
                } 
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return info;
    }

    public static String insertContact(Context context, String accountName, String accountType, ContactInfo info) 
            throws RemoteException, OperationApplicationException {
        ContentResolver cr = context.getContentResolver();
        ArrayList<String[]> displayName = info.fieldNameMap.get(ContactInfo.DISPLAYNAME);
        ArrayList<String[]> phone = info.fieldNameMap.get(ContactInfo.PHONE);
        ArrayList<String[]> email = info.fieldNameMap.get(ContactInfo.EMAIL); 
        ArrayList<String[]> im = info.fieldNameMap.get(ContactInfo.IM);
        ArrayList<String[]> address = info.fieldNameMap.get(ContactInfo.POSTAL); 
        ArrayList<String[]> organization = info.fieldNameMap.get(ContactInfo.ORGNIZATION);
        ArrayList<String[]> nickname = info.fieldNameMap.get(ContactInfo.NICKNAME);
        ArrayList<String[]> website = info.fieldNameMap.get(ContactInfo.WEBSITE);
        ArrayList<String[]> sip = info.fieldNameMap.get(ContactInfo.SIP);
        ArrayList<String[]> notes = info.fieldNameMap.get(ContactInfo.NOTE);
        
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        long rawContactId = insertRawContact(cr, accountName, accountType);
        String rawId = Long.toString(rawContactId);

        if (displayName != null) {
            insertContactDisplayname(ops, StructuredName.CONTENT_ITEM_TYPE,
                    rawId, displayName);
        }
        if (phone != null) {
            for (int j = 0; j < phone.size(); j++) {
                String[] item = phone.get(j);
                insertItemToContact(ops, Phone.CONTENT_ITEM_TYPE, rawId,
                        PROJECTION_PHONENUMBER, item);
            }
        }
        if (email != null) {
            for (int j = 0; j < email.size(); j++) {
                String[] item = email.get(j);
                insertItemToContact(ops, Email.CONTENT_ITEM_TYPE, rawId,
                        PROJECTION_EMAIL, item);
            }
        }
        if (im != null) {
            for (int j = 0; j < im.size(); j++) {
                String[] item = im.get(j);
                insertItemToContact(ops, Im.CONTENT_ITEM_TYPE, rawId,
                        PROJECTION_IM, item);
            }
        }
        if (address != null) {
            for (int j = 0; j < address.size(); j++) {
                String[] item = address.get(j);
                insertItemToContact(ops, StructuredPostal.CONTENT_ITEM_TYPE,
                        rawId, PROJECTION_ADDRESS, item);
            }
        }
        if (organization != null) {
            for (int j = 0; j < organization.size(); j++) {
                String[] item = organization.get(j);
                insertItemToContact(ops, Organization.CONTENT_ITEM_TYPE, rawId,
                        PROJECTION_ORGANIZATION, item);
            }
        }
        if (nickname != null) {
            for (int j = 0; j < nickname.size(); j++) {
                String[] item = nickname.get(j);
                insertItemToContact(ops, Nickname.CONTENT_ITEM_TYPE, rawId,
                        PROJECTION_NICKNAMES, item);
            }
        }
        if (website != null) {
            for (int j = 0; j < website.size(); j++) {
                String[] item = website.get(j);
                insertItemToContact(ops, Website.CONTENT_ITEM_TYPE, rawId,
                        PROJECTION_WEBSITES, item);
            }
        }
        if (sip != null) {
            for (int j = 0; j < sip.size(); j++) {
                String[] item = sip.get(j);
                insertItemToContact(ops, SipAddress.CONTENT_ITEM_TYPE, rawId,
                        PROJECTION_SIP, item);
            }
        }
        if (notes != null) {
            for (int j = 0; j < notes.size(); j++) {
                String[] item = notes.get(j);
                insertItemToContact(ops, Note.CONTENT_ITEM_TYPE, rawId,
                        PROJECTION_NOTES, item);
            }
        }
        cr.applyBatch(ContactsContract.AUTHORITY, ops);
        return rawId;
    }

    /*
     * insert contact to ROWCONTACT database，in order to get rawId
     * @param cr
     * @param accountName : most NULL
     * @param accountType : most NULL
     * @return rawcontactid
     */

    private static long insertRawContact(ContentResolver cr, String accountName, String accountType) {
        ContentValues values = new ContentValues();
        values.put(RawContacts.ACCOUNT_NAME, accountName);
        values.put(RawContacts.ACCOUNT_TYPE, accountType);
        // values.put(Contacts.DISPLAY_NAME, displayName);
        Uri rawContactUri = cr.insert(RawContacts.CONTENT_URI, values);
        long rawContactId = ContentUris.parseId(rawContactUri);
        return rawContactId;
    }

    private static void insertContactDisplayname(
            ArrayList<ContentProviderOperation> ops, String mimeType,
			String rawContactId, ArrayList<String[]> displayName) throws RemoteException,
            OperationApplicationException {
        String[] name = displayName.get(0);
        if (name[0] != null) {
            ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI).withValue(
				Data.MIMETYPE, mimeType).withValue(Data.RAW_CONTACT_ID,
				rawContactId).withValue(StructuredName.DISPLAY_NAME,
				name[0]).build());
        }
    }

    private static void insertItemToContact(
            ArrayList<ContentProviderOperation> ops, String mimeType,
            String rawContactId, String[] PROJECTION_CONTACT, String[] item)
            throws RemoteException, OperationApplicationException {

        Builder builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
        builder.withYieldAllowed(true);
        builder.withValue(Data.RAW_CONTACT_ID, rawContactId);
        builder.withValue(Data.MIMETYPE, mimeType);
        for (int i = 0; i < PROJECTION_CONTACT.length; i++) {
            builder.withValue(PROJECTION_CONTACT[i], item[i]);
        }
        ops.add(builder.build());
    }
    
    public static String[] getStringInContactCursor(Cursor c, String[] projection) {
        String[] contentValue = new String[projection.length];
        for (int i = 0; i < contentValue.length; i++) {
            String value = c.getString(c.getColumnIndex(projection[i]));
            if (value == null) {
                contentValue[i] = "";
            } else {
                contentValue[i] = value;
            }
        }
        return contentValue;
    }
    
    public static String combineIds(ArrayList<String> contactIds) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < contactIds.size(); i++) {
            sb.append(contactIds.get(i));
            if (i != contactIds.size() - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }
}
