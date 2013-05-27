/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lewa.PIM.contacts.model;

import com.lewa.PIM.R;
import com.google.android.collect.Lists;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds.BaseTypes;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.DisplayPhoto;
import android.provider.ContactsContract.CommonDataKinds.SipAddress;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.Website;
import android.text.format.DateFormat;
import android.view.inputmethod.EditorInfo;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import android.util.Log;

public class FallbackSource extends ContactsSource {
    protected static final int FLAGS_PHONE = EditorInfo.TYPE_CLASS_PHONE;
    protected static final int FLAGS_EMAIL = EditorInfo.TYPE_CLASS_TEXT
            | EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
    protected static final int FLAGS_PERSON_NAME = EditorInfo.TYPE_CLASS_TEXT
            | EditorInfo.TYPE_TEXT_FLAG_CAP_WORDS | EditorInfo.TYPE_TEXT_VARIATION_PERSON_NAME;
    protected static final int FLAGS_PHONETIC = EditorInfo.TYPE_CLASS_TEXT
            | EditorInfo.TYPE_TEXT_VARIATION_PHONETIC;
    protected static final int FLAGS_GENERIC_NAME = EditorInfo.TYPE_CLASS_TEXT
            | EditorInfo.TYPE_TEXT_FLAG_CAP_WORDS;
    protected static final int FLAGS_NOTE = EditorInfo.TYPE_CLASS_TEXT
            | EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES | EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE;
    protected static final int FLAGS_WEBSITE = EditorInfo.TYPE_CLASS_TEXT
            | EditorInfo.TYPE_TEXT_VARIATION_URI;
    protected static final int FLAGS_POSTAL = EditorInfo.TYPE_CLASS_TEXT
            | EditorInfo.TYPE_TEXT_VARIATION_POSTAL_ADDRESS | EditorInfo.TYPE_TEXT_FLAG_CAP_WORDS
            | EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE;
    protected static final int FLAGS_SIP_ADDRESS = EditorInfo.TYPE_CLASS_TEXT
            | EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;  // since SIP addresses have the same
                                                             // basic format as email addresses
    protected static final int FLAGS_DATE = EditorInfo.TYPE_CLASS_DATETIME
            | EditorInfo.TYPE_DATETIME_VARIATION_DATE;

    public FallbackSource() {
        this.accountType = null;
        this.titleRes = R.string.account_phone;
        this.iconRes = R.drawable.ic_launcher_contacts;
    }

    @Override
    protected void inflate(Context context, int inflateLevel) {
        inflateStructuredName(context, inflateLevel);
        inflateNickname(context, inflateLevel);
        inflatePhone(context, inflateLevel);
        inflateEmail(context, inflateLevel);
        inflateStructuredPostal(context, inflateLevel);
        inflateIm(context, inflateLevel);
        inflateOrganization(context, inflateLevel);
        inflatePhoto(context, inflateLevel);
	 inflateDisplayPhoto(context,inflateLevel);	
        inflateNote(context, inflateLevel);
        inflateWebsite(context, inflateLevel);
        inflateEvent(context, inflateLevel);
        inflateSipAddress(context, inflateLevel);

        setInflatedLevel(inflateLevel);

    }

    protected EditType buildPhoneType(int type) {
        return new EditType(type, Phone.getTypeLabelResource(type));
    }

    protected EditType buildEmailType(int type) {
        return new EditType(type, Email.getTypeLabelResource(type));
    }

    protected EditType buildPostalType(int type) {
        return new EditType(type, StructuredPostal.getTypeLabelResource(type));
    }

    protected EditType buildImType(int type) {
        return new EditType(type, Im.getProtocolLabelResource(type));
    }

    protected EditType buildOrgType(int type) {
        return new EditType(type, Organization.getTypeLabelResource(type));
    }

    protected EditType buildEventType(int type) {
        return new EditType(type, Event.getTypeResource(type));
    }

    protected DataKind inflateStructuredName(Context context, int inflateLevel) {
        DataKind kind = getKindForMimetype(StructuredName.CONTENT_ITEM_TYPE);
        if (kind == null) {
            kind = addKind(new DataKind(StructuredName.CONTENT_ITEM_TYPE,
                    R.string.nameLabelsGroup, -1, -1, true));
        }

        if (inflateLevel >= ContactsSource.LEVEL_CONSTRAINTS) {
            boolean displayOrderPrimary =
                    context.getResources().getBoolean(R.bool.config_editor_field_order_primary);

            kind.fieldList = Lists.newArrayList();
            kind.fieldList.add(new EditField(StructuredName.PREFIX, R.string.name_prefix,
                    FLAGS_PERSON_NAME).setOptional(true));
            if (!displayOrderPrimary) {
                kind.fieldList.add(new EditField(StructuredName.FAMILY_NAME, R.string.name_family,
                        FLAGS_PERSON_NAME).setOptional(true));
                kind.fieldList.add(new EditField(StructuredName.MIDDLE_NAME, R.string.name_middle,
                        FLAGS_PERSON_NAME).setOptional(true));
                kind.fieldList.add(new EditField(StructuredName.GIVEN_NAME, R.string.name_given,
                        FLAGS_PERSON_NAME));
                kind.fieldList.add(new EditField(StructuredName.SUFFIX, R.string.name_suffix,
                        FLAGS_PERSON_NAME).setOptional(true));
                kind.fieldList.add(new EditField(StructuredName.PHONETIC_FAMILY_NAME,
                        R.string.name_phonetic_family, FLAGS_PHONETIC).setOptional(true));
                kind.fieldList.add(new EditField(StructuredName.PHONETIC_MIDDLE_NAME,
                        R.string.name_phonetic_middle, FLAGS_PHONETIC).setOptional(true));
                kind.fieldList.add(new EditField(StructuredName.PHONETIC_GIVEN_NAME,
                        R.string.name_phonetic_given, FLAGS_PHONETIC).setOptional(true));
            } else {
                kind.fieldList.add(new EditField(StructuredName.GIVEN_NAME, R.string.name_given,
                        FLAGS_PERSON_NAME));
                kind.fieldList.add(new EditField(StructuredName.MIDDLE_NAME, R.string.name_middle,
                        FLAGS_PERSON_NAME).setOptional(true));
                kind.fieldList.add(new EditField(StructuredName.FAMILY_NAME, R.string.name_family,
                        FLAGS_PERSON_NAME).setOptional(true));
                kind.fieldList.add(new EditField(StructuredName.SUFFIX, R.string.name_suffix,
                        FLAGS_PERSON_NAME).setOptional(true));
                kind.fieldList.add(new EditField(StructuredName.PHONETIC_GIVEN_NAME,
                        R.string.name_phonetic_given, FLAGS_PHONETIC).setOptional(true));
                kind.fieldList.add(new EditField(StructuredName.PHONETIC_MIDDLE_NAME,
                        R.string.name_phonetic_middle, FLAGS_PHONETIC).setOptional(true));
                kind.fieldList.add(new EditField(StructuredName.PHONETIC_FAMILY_NAME,
                        R.string.name_phonetic_family, FLAGS_PHONETIC).setOptional(true));
            }
        }

        return kind;
    }

    protected DataKind inflateNickname(Context context, int inflateLevel) {
        DataKind kind = getKindForMimetype(Nickname.CONTENT_ITEM_TYPE);
        if (kind == null) {
            kind = addKind(new DataKind(Nickname.CONTENT_ITEM_TYPE,
                    R.string.nicknameLabelsGroup, -1, 115, true));
            kind.secondary = true;
            kind.isList = false;
            kind.actionHeader = new SimpleInflater(R.string.nicknameLabelsGroup);
            kind.actionBody = new SimpleInflater(Nickname.NAME);
        }

        if (inflateLevel >= ContactsSource.LEVEL_CONSTRAINTS) {
            kind.defaultValues = new ContentValues();
            kind.defaultValues.put(Nickname.TYPE, Nickname.TYPE_DEFAULT);

            kind.fieldList = Lists.newArrayList();
            kind.fieldList.add(new EditField(Nickname.NAME, R.string.nicknameLabelsGroup,
                    FLAGS_PERSON_NAME));
        }

        return kind;
    }

    protected DataKind inflatePhone(Context context, int inflateLevel) {
        DataKind kind = getKindForMimetype(Phone.CONTENT_ITEM_TYPE);
        if (kind == null) {
            kind = addKind(new DataKind(Phone.CONTENT_ITEM_TYPE, R.string.phoneLabelsGroup,
                    android.R.drawable.sym_action_call, 10, true));
            kind.iconAltRes = R.drawable.sym_action_sms;
            kind.actionHeader = new PhoneActionInflater();
            kind.actionAltHeader = new PhoneActionAltInflater();
            kind.actionBody = new SimpleInflater(Phone.NUMBER);
        }

        if (inflateLevel >= ContactsSource.LEVEL_CONSTRAINTS) {
            kind.typeColumn = Phone.TYPE;
            
            kind.typeList = Lists.newArrayList();
            kind.typeList.add(buildPhoneType(Phone.TYPE_MOBILE));
            kind.typeList.add(buildPhoneType(Phone.TYPE_HOME));
            kind.typeList.add(buildPhoneType(Phone.TYPE_WORK));
            kind.typeList.add(buildPhoneType(Phone.TYPE_FAX_WORK).setSecondary(true));
            kind.typeList.add(buildPhoneType(Phone.TYPE_FAX_HOME).setSecondary(true));
            kind.typeList.add(buildPhoneType(Phone.TYPE_PAGER).setSecondary(true));
            kind.typeList.add(buildPhoneType(Phone.TYPE_OTHER));
            kind.typeList.add(buildPhoneType(Phone.TYPE_CUSTOM).setSecondary(true).setCustomColumn(
                    Phone.LABEL));
            kind.typeList.add(buildPhoneType(Phone.TYPE_CALLBACK).setSecondary(true));
            kind.typeList.add(buildPhoneType(Phone.TYPE_CAR).setSecondary(true));
            kind.typeList.add(buildPhoneType(Phone.TYPE_COMPANY_MAIN).setSecondary(true));
            kind.typeList.add(buildPhoneType(Phone.TYPE_ISDN).setSecondary(true));
            kind.typeList.add(buildPhoneType(Phone.TYPE_MAIN).setSecondary(true));
            kind.typeList.add(buildPhoneType(Phone.TYPE_OTHER_FAX).setSecondary(true));
            kind.typeList.add(buildPhoneType(Phone.TYPE_RADIO).setSecondary(true));
            kind.typeList.add(buildPhoneType(Phone.TYPE_TELEX).setSecondary(true));
            kind.typeList.add(buildPhoneType(Phone.TYPE_TTY_TDD).setSecondary(true));
            kind.typeList.add(buildPhoneType(Phone.TYPE_WORK_MOBILE).setSecondary(true));
            kind.typeList.add(buildPhoneType(Phone.TYPE_WORK_PAGER).setSecondary(true));
            kind.typeList.add(buildPhoneType(Phone.TYPE_ASSISTANT).setSecondary(true)
                    .setCustomColumn(Phone.LABEL));
            kind.typeList.add(buildPhoneType(Phone.TYPE_MMS).setSecondary(true));

            kind.fieldList = Lists.newArrayList();
            kind.fieldList.add(new EditField(Phone.NUMBER, R.string.phoneLabelsGroup, FLAGS_PHONE));
        }

        return kind;
    }

    protected DataKind inflateEmail(Context context, int inflateLevel) {
        DataKind kind = getKindForMimetype(Email.CONTENT_ITEM_TYPE);
        if (kind == null) {
            kind = addKind(new DataKind(Email.CONTENT_ITEM_TYPE,
                    R.string.emailLabelsGroup, android.R.drawable.sym_action_email, 20, true));
            kind.actionHeader = new EmailActionInflater();
            kind.actionBody = new SimpleInflater(Email.DATA);
        }

        if (inflateLevel >= ContactsSource.LEVEL_CONSTRAINTS) {
            kind.typeColumn = Email.TYPE;
            kind.typeList = Lists.newArrayList();
            kind.typeList.add(buildEmailType(Email.TYPE_HOME));
            kind.typeList.add(buildEmailType(Email.TYPE_WORK));
            kind.typeList.add(buildEmailType(Email.TYPE_OTHER));
            kind.typeList.add(buildEmailType(Email.TYPE_MOBILE));
            kind.typeList.add(buildEmailType(Email.TYPE_CUSTOM).setSecondary(true).setCustomColumn(
                    Email.LABEL));

            kind.fieldList = Lists.newArrayList();
            kind.fieldList.add(new EditField(Email.DATA, R.string.emailLabelsGroup, FLAGS_EMAIL));
        }

        return kind;
    }

    protected DataKind inflateStructuredPostal(Context context, int inflateLevel) {
        DataKind kind = getKindForMimetype(StructuredPostal.CONTENT_ITEM_TYPE);
        if (kind == null) {
            kind = addKind(new DataKind(StructuredPostal.CONTENT_ITEM_TYPE,
                    R.string.postalLabelsGroup, R.drawable.sym_action_map, 25, true));
                    
            //Wysie: Add navigation icon as alternate
            kind.iconAltRes = R.drawable.sym_action_navi;
            
            kind.actionHeader = new PostalActionInflater();
            kind.actionBody = new SimpleInflater(StructuredPostal.FORMATTED_ADDRESS);            
        }

        if (inflateLevel >= ContactsSource.LEVEL_CONSTRAINTS) {
            final boolean useJapaneseOrder =
                Locale.JAPANESE.getLanguage().equals(Locale.getDefault().getLanguage());
            kind.typeColumn = StructuredPostal.TYPE;
            kind.typeList = Lists.newArrayList();
            kind.typeList.add(buildPostalType(StructuredPostal.TYPE_HOME));
            kind.typeList.add(buildPostalType(StructuredPostal.TYPE_WORK));
            kind.typeList.add(buildPostalType(StructuredPostal.TYPE_OTHER));
            kind.typeList.add(buildPostalType(StructuredPostal.TYPE_CUSTOM).setSecondary(true)
                    .setCustomColumn(StructuredPostal.LABEL));

            kind.fieldList = Lists.newArrayList();

            if (useJapaneseOrder) {
                kind.fieldList.add(new EditField(StructuredPostal.COUNTRY,
                        R.string.postal_country, FLAGS_POSTAL).setOptional(true));
                kind.fieldList.add(new EditField(StructuredPostal.POSTCODE,
                        R.string.postal_postcode, FLAGS_POSTAL));
                kind.fieldList.add(new EditField(StructuredPostal.REGION,
                        R.string.postal_region, FLAGS_POSTAL));
                kind.fieldList.add(new EditField(StructuredPostal.CITY,
                        R.string.postal_city, FLAGS_POSTAL));
                kind.fieldList.add(new EditField(StructuredPostal.NEIGHBORHOOD,
                        R.string.postal_neighborhood, FLAGS_POSTAL).setOptional(true));
                kind.fieldList.add(new EditField(StructuredPostal.STREET,
                        R.string.postal_street, FLAGS_POSTAL));
                kind.fieldList.add(new EditField(StructuredPostal.POBOX,
                        R.string.postal_pobox, FLAGS_POSTAL).setOptional(true));
            } else {
                kind.fieldList.add(new EditField(StructuredPostal.STREET,
                        R.string.postal_street, FLAGS_POSTAL));
                kind.fieldList.add(new EditField(StructuredPostal.POBOX,
                        R.string.postal_pobox, FLAGS_POSTAL).setOptional(true));
                kind.fieldList.add(new EditField(StructuredPostal.NEIGHBORHOOD,
                        R.string.postal_neighborhood, FLAGS_POSTAL).setOptional(true));
                kind.fieldList.add(new EditField(StructuredPostal.CITY,
                        R.string.postal_city, FLAGS_POSTAL));
                kind.fieldList.add(new EditField(StructuredPostal.REGION,
                        R.string.postal_region, FLAGS_POSTAL));
                kind.fieldList.add(new EditField(StructuredPostal.POSTCODE,
                        R.string.postal_postcode, FLAGS_POSTAL));
                kind.fieldList.add(new EditField(StructuredPostal.COUNTRY,
                        R.string.postal_country, FLAGS_POSTAL).setOptional(true));
            }
        }

        return kind;
    }

    protected DataKind inflateIm(Context context, int inflateLevel) {
        DataKind kind = getKindForMimetype(Im.CONTENT_ITEM_TYPE);
        if (kind == null) {
            kind = addKind(new DataKind(Im.CONTENT_ITEM_TYPE, R.string.imLabelsGroup,
                    android.R.drawable.sym_action_chat, 15, true));
            
            kind.secondary = false;
            kind.actionHeader = new ImActionInflater();
            kind.actionBody = new SimpleInflater(Im.DATA);
        }

        if (inflateLevel >= ContactsSource.LEVEL_CONSTRAINTS) {
            // NOTE: even though a traditional "type" exists, for editing
            // purposes we're using the protocol to pick labels

            kind.defaultValues = new ContentValues();
            kind.defaultValues.put(Im.TYPE, Im.TYPE_OTHER);

            kind.typeColumn = Im.PROTOCOL;
            kind.typeList = Lists.newArrayList();
            kind.typeList.add(buildImType(Im.PROTOCOL_QQ));//modify by zenghuaying for bug 8903
            kind.typeList.add(buildImType(Im.PROTOCOL_AIM));
            kind.typeList.add(buildImType(Im.PROTOCOL_MSN));
            kind.typeList.add(buildImType(Im.PROTOCOL_YAHOO));
            kind.typeList.add(buildImType(Im.PROTOCOL_SKYPE));
            kind.typeList.add(buildImType(Im.PROTOCOL_GOOGLE_TALK));
            kind.typeList.add(buildImType(Im.PROTOCOL_ICQ));
            kind.typeList.add(buildImType(Im.PROTOCOL_JABBER));
            kind.typeList.add(buildImType(Im.PROTOCOL_CUSTOM).setSecondary(true).setCustomColumn(
            		Im.CUSTOM_PROTOCOL));
            kind.fieldList = Lists.newArrayList();
            kind.fieldList.add(new EditField(Im.DATA, R.string.imLabelsGroup, FLAGS_EMAIL));
        }

        return kind;
    }

    protected DataKind inflateOrganization(Context context, int inflateLevel) {
        DataKind kind = getKindForMimetype(Organization.CONTENT_ITEM_TYPE);
        if (kind == null) {
            kind = addKind(new DataKind(Organization.CONTENT_ITEM_TYPE,
                    R.string.organizationLabelsGroup, R.drawable.sym_action_organization, 30, true));
            kind.actionHeader = new SimpleInflater(Organization.COMPANY);
            kind.actionBody = new SimpleInflater(Organization.TITLE);
        }

        if (inflateLevel >= ContactsSource.LEVEL_CONSTRAINTS) {
            kind.typeColumn = Organization.TYPE;
            kind.typeList = Lists.newArrayList();
            kind.typeList.add(buildOrgType(Organization.TYPE_WORK));
            kind.typeList.add(buildOrgType(Organization.TYPE_OTHER));
            kind.typeList.add(buildOrgType(Organization.TYPE_CUSTOM).setSecondary(true)
                    .setCustomColumn(Organization.LABEL));

            kind.fieldList = Lists.newArrayList();
            kind.fieldList.add(new EditField(Organization.COMPANY, R.string.ghostData_company,
                    FLAGS_GENERIC_NAME));
            kind.fieldList.add(new EditField(Organization.TITLE, R.string.ghostData_title,
                    FLAGS_GENERIC_NAME));
        }

        return kind;
    }

    protected DataKind inflatePhoto(Context context, int inflateLevel) {
        DataKind kind = getKindForMimetype(Photo.CONTENT_ITEM_TYPE);
        if (kind == null) {
            kind = addKind(new DataKind(Photo.CONTENT_ITEM_TYPE, -1, -1, -1, true));
        }

        if (inflateLevel >= ContactsSource.LEVEL_CONSTRAINTS) {
            kind.fieldList = Lists.newArrayList();
            kind.fieldList.add(new EditField(Photo.PHOTO, -1, -1));
        }

        return kind;
    }

    protected DataKind inflateDisplayPhoto(Context context, int inflateLevel) {
        DataKind kind = getKindForMimetype(DisplayPhoto.CONTENT_ITEM_TYPE);
        if (kind == null) {
            kind = addKind(new DataKind(DisplayPhoto.CONTENT_ITEM_TYPE, -1, -1, -1, true));
        }

        if (inflateLevel >= ContactsSource.LEVEL_CONSTRAINTS) {
            kind.fieldList = Lists.newArrayList();
            kind.fieldList.add(new EditField(Photo.PHOTO, -1, -1));
        }

        return kind;
    }

    protected DataKind inflateNote(Context context, int inflateLevel) {
        DataKind kind = getKindForMimetype(Note.CONTENT_ITEM_TYPE);
        if (kind == null) {
            kind = addKind(new DataKind(Note.CONTENT_ITEM_TYPE,
                    R.string.label_notes, R.drawable.sym_note, 110, true));
            kind.isList = false;
            kind.secondary = true;
            kind.actionHeader = new SimpleInflater(R.string.label_notes);
            kind.actionBody = new SimpleInflater(Note.NOTE);
        }

        if (inflateLevel >= ContactsSource.LEVEL_CONSTRAINTS) {
            kind.fieldList = Lists.newArrayList();
            kind.fieldList.add(new EditField(Note.NOTE, R.string.label_notes, FLAGS_NOTE));
        }

        return kind;
    }

    protected DataKind inflateWebsite(Context context, int inflateLevel) {
        DataKind kind = getKindForMimetype(Website.CONTENT_ITEM_TYPE);
        if (kind == null) {
            kind = addKind(new DataKind(Website.CONTENT_ITEM_TYPE,
                    R.string.websiteLabelsGroup, -1, 120, true));
            kind.secondary = true;
            kind.actionHeader = new SimpleInflater(R.string.websiteLabelsGroup);
            kind.actionBody = new SimpleInflater(Website.URL);
        }

        if (inflateLevel >= ContactsSource.LEVEL_CONSTRAINTS) {
            kind.defaultValues = new ContentValues();
            kind.defaultValues.put(Website.TYPE, Website.TYPE_OTHER);

            kind.fieldList = Lists.newArrayList();
            kind.fieldList.add(new EditField(Website.URL, R.string.websiteLabelsGroup, FLAGS_WEBSITE));
        }

        return kind;
    }

    protected DataKind inflateEvent(final Context context, int inflateLevel) {
        DataKind kind = getKindForMimetype(Event.CONTENT_ITEM_TYPE);
        if (kind == null) {
            kind = addKind(new DataKind(Event.CONTENT_ITEM_TYPE,
                    R.string.eventLabelsGroup, R.drawable.sym_action_event, 150, true));
            kind.secondary = true;
            kind.actionHeader = new EventActionInflater();
            kind.actionBody = new EventDateInflater();
        }

        if (inflateLevel >= ContactsSource.LEVEL_CONSTRAINTS) {
            kind.typeColumn = Event.TYPE;
            kind.typeList = Lists.newArrayList();
            kind.typeList.add(buildEventType(Event.TYPE_BIRTHDAY));
            kind.typeList.add(buildEventType(Event.TYPE_ANNIVERSARY));
            kind.typeList.add(buildEventType(Event.TYPE_OTHER));

            kind.fieldList = Lists.newArrayList();
            kind.fieldList.add(new EventDateEditField(false));
        }

        return kind;
    }

    protected DataKind inflateSipAddress(Context context, int inflateLevel) {
        DataKind kind = getKindForMimetype(SipAddress.CONTENT_ITEM_TYPE);
        if (kind == null) {
            // The icon specified here is the one that gets displayed for
            // "Internet call" items, in the "view contact" UI within the
            // Contacts app.
            //
            // This is independent of the "SIP call" icon that gets
            // displayed in the Quick Contacts widget, which comes from
            // the android:icon attribute of the SIP-related
            // intent-filters in the Phone app's manifest.

            kind = addKind(new DataKind(SipAddress.CONTENT_ITEM_TYPE,
                    R.string.label_sip_address, android.R.drawable.sym_action_call, 130, true));

            kind.isList = false;
            kind.secondary = true;
            kind.actionHeader = new SimpleInflater(R.string.label_sip_address);
            kind.actionBody = new SimpleInflater(SipAddress.SIP_ADDRESS);
        }

        if (inflateLevel >= ContactsSource.LEVEL_CONSTRAINTS) {
            kind.fieldList = Lists.newArrayList();
            kind.fieldList.add(new EditField(SipAddress.SIP_ADDRESS,
                                             R.string.label_sip_address, FLAGS_SIP_ADDRESS));
        }

        return kind;
    }

    /**
     * Simple inflater that assumes a string resource has a "%s" that will be
     * filled from the given column.
     */
    public static class SimpleInflater implements StringInflater {
        private final int mStringRes;
        protected final String mColumnName;

        public SimpleInflater(int stringRes) {
            this(stringRes, null);
        }

        public SimpleInflater(String columnName) {
            this(-1, columnName);
        }

        public SimpleInflater(int stringRes, String columnName) {
            mStringRes = stringRes;
            mColumnName = columnName;
        }

        public CharSequence inflateUsing(Context context, Cursor cursor) {
            final int index = mColumnName != null ? cursor.getColumnIndex(mColumnName) : -1;
            final boolean validString = mStringRes > 0;
            final boolean validColumn = index != -1;

            final CharSequence stringValue = validString ? context.getText(mStringRes) : null;
            final CharSequence columnValue = validColumn ? cursor.getString(index) : null;

            if (validString && validColumn) {
                return String.format(stringValue.toString(), columnValue);
            } else if (validString) {
                return stringValue;
            } else if (validColumn) {
                return columnValue;
            } else {
                return null;
            }
        }

        public CharSequence inflateUsing(Context context, ContentValues values) {
            final boolean validColumn = values.containsKey(mColumnName);
            final boolean validString = mStringRes > 0;

            final CharSequence stringValue = validString ? context.getText(mStringRes) : null;
            final CharSequence columnValue = validColumn ? values.getAsString(mColumnName) : null;

            if (validString && validColumn) {
                return String.format(stringValue.toString(), columnValue);
            } else if (validString) {
                return stringValue;
            } else if (validColumn) {
                return columnValue;
            } else {
                return null;
            }
        }
    }

    public static abstract class CommonInflater implements StringInflater {
        protected abstract int getTypeLabelResource(Integer type);

        protected boolean isCustom(Integer type) {
            return type == BaseTypes.TYPE_CUSTOM;
        }

        protected String getTypeColumn() {
            return Phone.TYPE;
        }

        protected String getLabelColumn() {
            return Phone.LABEL;
        }

        protected CharSequence getTypeLabel(Resources res, Integer type, CharSequence label) {
            final int labelRes = getTypeLabelResource(type);
            if (type == null) {
                return res.getText(labelRes);
            } else if (isCustom(type)) {
                return res.getString(labelRes, label == null ? "" : label);
            } else {
                return res.getText(labelRes);
            }
        }

        public CharSequence inflateUsing(Context context, Cursor cursor) {
            final Integer type = cursor.getInt(cursor.getColumnIndex(getTypeColumn()));
            final String label = cursor.getString(cursor.getColumnIndex(getLabelColumn()));
            return getTypeLabel(context.getResources(), type, label);
        }

        public CharSequence inflateUsing(Context context, ContentValues values) {
            final Integer type = values.getAsInteger(getTypeColumn());
            final String label = values.getAsString(getLabelColumn());
            return getTypeLabel(context.getResources(), type, label);
        }
    }

    public static class PhoneActionInflater extends CommonInflater {
        @Override
        protected boolean isCustom(Integer type) {
            return type == Phone.TYPE_CUSTOM || type == Phone.TYPE_ASSISTANT;
        }

        @Override
        protected int getTypeLabelResource(Integer type) {
            if (type == null) return R.string.call_other;
            switch (type) {
                case Phone.TYPE_HOME: return R.string.call_home;
                case Phone.TYPE_MOBILE: return R.string.call_mobile;
                case Phone.TYPE_WORK: return R.string.call_work;
                case Phone.TYPE_FAX_WORK: return R.string.call_fax_work;
                case Phone.TYPE_FAX_HOME: return R.string.call_fax_home;
                case Phone.TYPE_PAGER: return R.string.call_pager;
                case Phone.TYPE_OTHER: return R.string.call_other;
                case Phone.TYPE_CALLBACK: return R.string.call_callback;
                case Phone.TYPE_CAR: return R.string.call_car;
                case Phone.TYPE_COMPANY_MAIN: return R.string.call_company_main;
                case Phone.TYPE_ISDN: return R.string.call_isdn;
                case Phone.TYPE_MAIN: return R.string.call_main;
                case Phone.TYPE_OTHER_FAX: return R.string.call_other_fax;
                case Phone.TYPE_RADIO: return R.string.call_radio;
                case Phone.TYPE_TELEX: return R.string.call_telex;
                case Phone.TYPE_TTY_TDD: return R.string.call_tty_tdd;
                case Phone.TYPE_WORK_MOBILE: return R.string.call_work_mobile;
                case Phone.TYPE_WORK_PAGER: return R.string.call_work_pager;
                case Phone.TYPE_ASSISTANT: return R.string.call_assistant;
                case Phone.TYPE_MMS: return R.string.call_mms;
                default: return R.string.call_custom;
            }
        }
    }

    public static class PhoneActionAltInflater extends CommonInflater {
        @Override
        protected boolean isCustom(Integer type) {
            return (type == Phone.TYPE_CUSTOM || type == Phone.TYPE_ASSISTANT);
        }

        @Override
        protected int getTypeLabelResource(Integer type) {
            if (type == null) return R.string.sms_other;
            switch (type) {
                case Phone.TYPE_HOME: return R.string.sms_home;
                case Phone.TYPE_MOBILE: return R.string.sms_mobile;
                case Phone.TYPE_WORK: return R.string.sms_work;
                case Phone.TYPE_FAX_WORK: return R.string.sms_fax_work;
                case Phone.TYPE_FAX_HOME: return R.string.sms_fax_home;
                case Phone.TYPE_PAGER: return R.string.sms_pager;
                case Phone.TYPE_OTHER: return R.string.sms_other;
                case Phone.TYPE_CALLBACK: return R.string.sms_callback;
                case Phone.TYPE_CAR: return R.string.sms_car;
                case Phone.TYPE_COMPANY_MAIN: return R.string.sms_company_main;
                case Phone.TYPE_ISDN: return R.string.sms_isdn;
                case Phone.TYPE_MAIN: return R.string.sms_main;
                case Phone.TYPE_OTHER_FAX: return R.string.sms_other_fax;
                case Phone.TYPE_RADIO: return R.string.sms_radio;
                case Phone.TYPE_TELEX: return R.string.sms_telex;
                case Phone.TYPE_TTY_TDD: return R.string.sms_tty_tdd;
                case Phone.TYPE_WORK_MOBILE: return R.string.sms_work_mobile;
                case Phone.TYPE_WORK_PAGER: return R.string.sms_work_pager;
                case Phone.TYPE_ASSISTANT: return R.string.sms_assistant;
                case Phone.TYPE_MMS: return R.string.sms_mms;
                default: return R.string.sms_custom;
            }
        }
    }

    public static class EmailActionInflater extends CommonInflater {
        @Override
        protected int getTypeLabelResource(Integer type) {
            if (type == null) return R.string.email;
            switch (type) {
                case Email.TYPE_HOME: return R.string.email_home;
                case Email.TYPE_WORK: return R.string.email_work;
                case Email.TYPE_OTHER: return R.string.email_other;
                case Email.TYPE_MOBILE: return R.string.email_mobile;
                default: return R.string.email_custom;
            }
        }
    }

    public static class EventActionInflater extends CommonInflater {
        @Override
        protected int getTypeLabelResource(Integer type) {
            return Event.getTypeResource(type);
        }
    }

    public static class PostalActionInflater extends CommonInflater {
        @Override
        protected int getTypeLabelResource(Integer type) {
            if (type == null) return R.string.map_other;
            switch (type) {
                case StructuredPostal.TYPE_HOME: return R.string.map_home;
                case StructuredPostal.TYPE_WORK: return R.string.map_work;
                case StructuredPostal.TYPE_OTHER: return R.string.map_other;
                default: return R.string.map_custom;
            }
        }
    }

    public static class ImActionInflater extends CommonInflater {
        @Override
        protected String getTypeColumn() {
            return Im.PROTOCOL;
        }

        @Override
        protected String getLabelColumn() {
            return Im.CUSTOM_PROTOCOL;
        }

        @Override
        protected int getTypeLabelResource(Integer type) {
            if (type == null) return R.string.chat;
            switch (type) {
                case Im.PROTOCOL_AIM: return R.string.chat_aim;
                case Im.PROTOCOL_MSN: return R.string.chat_msn;
                case Im.PROTOCOL_YAHOO: return R.string.chat_yahoo;
                case Im.PROTOCOL_SKYPE: return R.string.chat_skype;
                case Im.PROTOCOL_QQ: return R.string.chat_qq;
                case Im.PROTOCOL_GOOGLE_TALK: return R.string.chat_gtalk;
                case Im.PROTOCOL_ICQ: return R.string.chat_icq;
                case Im.PROTOCOL_JABBER: return R.string.chat_jabber;
                case Im.PROTOCOL_NETMEETING: return R.string.chat;
                default: return R.string.chat;
            }
        }
    }

    public static class EventDateInflater extends SimpleInflater {
        public EventDateInflater() {
            super(Event.START_DATE);
        }

        private CharSequence parseDate(Context context, CharSequence value) {
            Date date = EventDateConverter.parseDateFromDb(value);
            if (date != null) {
                return DateFormat.getLongDateFormat(context).format(date);
            }
            return value;
        }

        public CharSequence inflateUsing(Context context, Cursor cursor) {
            final int index = mColumnName != null ? cursor.getColumnIndex(mColumnName) : -1;
            final CharSequence columnValue = index != -1 ? cursor.getString(index) : null;

            return parseDate(context, columnValue);
        }

        public CharSequence inflateUsing(Context context, ContentValues values) {
            final boolean validColumn = values.containsKey(mColumnName);
            final CharSequence columnValue = validColumn ? values.getAsString(mColumnName) : null;

            return parseDate(context, columnValue);
        }
    }

    private static class EventDateConverter {
        private static SimpleDateFormat sDateFormat =
                new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        private static SimpleDateFormat sFullDateFormat =
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);

        public static Date parseDateFromDb(CharSequence value) {
            if (value == null) {
                return null;
            }

            String valueString = value.toString();

            /*
             * Try the most comprehensive format first.
             * Some servers (e.g. Exchange) use 'Z' as timezone, indicating
             * that the time is in UTC. The SimpleDateFormat routines don't
             * support that format, so replace 'Z' by 'GMT'.
             */
            Date date = parseDate(valueString.replace("Z", "GMT"), sFullDateFormat);
            if (date != null) {
                return date;
            }

            return parseDate(valueString, sDateFormat);
        }

        private static Date parseDate(String value, SimpleDateFormat format) {
            try {
                /* Reset format time zone in case it was changed by a previous run */
                format.setTimeZone(TimeZone.getDefault());
                return format.parse(value);
            } catch (ParseException e) {
            }
            return null;
        }

        public static CharSequence formatDateForDb(Date date) {
            return sDateFormat.format(date);
        }
    }

    protected static class EventDateEditField extends EditField {
        private View.OnClickListener mListener;
        private boolean mAllowClear;

        public EventDateEditField(boolean allowClear) {
            super(Event.START_DATE, R.string.label_date, FLAGS_DATE);

            mAllowClear = allowClear;
            mListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final EditText edit = (EditText) v;
                    openDatePicker(edit);
                }
            };
        }

        private void openDatePicker(final EditText edit) {
            final Context context = edit.getContext();
            String value = edit.getText().toString();
            final Calendar cal = Calendar.getInstance();

            try {
                Date date = DateFormat.getDateFormat(context).parse(value);
                cal.setTime(date);
            } catch (ParseException e) {
                /* use today in that case */
            }

            final DatePickerDialog.OnDateSetListener dateListener =
                    new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int day) {
                    final Context c = view.getContext();
                    cal.set(year, month, day);
                    edit.setText(DateFormat.getDateFormat(c).format(cal.getTime()));
                }
            };

            DatePickerDialog dp = new DatePickerDialog(context, dateListener,
                                                       cal.get(Calendar.YEAR),
                                                       cal.get(Calendar.MONTH),
                                                       cal.get(Calendar.DAY_OF_MONTH));

            if (mAllowClear) {
                dp.setButton3(context.getString(R.string.button_clear_date),
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        edit.setText(null);
                    }
                });
            }

            dp.show();
        }

        @Override
        public CharSequence fromValue(Context context, CharSequence value) {
            Date date = EventDateConverter.parseDateFromDb(value);
            if (date != null) {
                return DateFormat.getDateFormat(context).format(date);
            }
            return null;
        }

        @Override
        public CharSequence toValue(Context context, CharSequence value) {
            try {
                Date date = DateFormat.getDateFormat(context).parse(value.toString());
                return EventDateConverter.formatDateForDb(date);
            } catch (ParseException e) {
            }
            return null;
        }

        @Override
        public View.OnClickListener getOnClickListener() {
            return mListener;
        }
    }


    @Override
    public int getHeaderColor(Context context) {
        return 0xff7f93bc;
    }

    @Override
    public int getSideBarColor(Context context) {
        return 0xffbdc7b8;
    }
}
