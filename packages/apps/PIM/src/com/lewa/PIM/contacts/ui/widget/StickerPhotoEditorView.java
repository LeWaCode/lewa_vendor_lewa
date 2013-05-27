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

package com.lewa.PIM.contacts.ui.widget;

import com.lewa.PIM.R;
import com.lewa.PIM.contacts.model.Editor;
import com.lewa.PIM.contacts.model.EntityDelta;
import com.lewa.PIM.contacts.model.ContactsSource.DataKind;
import com.lewa.PIM.contacts.model.EntityDelta.ValuesDelta;
import com.lewa.PIM.contacts.ui.ViewIdGenerator;


import android.content.Context;
import android.content.ContentValues;
import android.content.ContentProviderOperation;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.DisplayPhoto;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import android.os.RemoteException;

/**
 * Simple editor for {@link Photo}.
 */
public class StickerPhotoEditorView extends Button implements Editor, OnClickListener {
    private static final String TAG = "PhotoEditorView";

    private ValuesDelta mEntry;
    private EditorListener mListener;

    private boolean mHasSetPhoto = false;
    private boolean mReadOnly;

    public StickerPhotoEditorView(Context context) {
        super(context);
    }

    public StickerPhotoEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /** {@inheritDoc} */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.setOnClickListener(this);
    }

    /** {@inheritDoc} */
    public void onClick(View v) {
        if (mListener != null) {
            mListener.onRequest(EditorListener.REQUEST_PICK_STICKER_PHOTO);
        }
    }

    /** {@inheritDoc} */
    public void onFieldChanged(String column, String value) {
        throw new UnsupportedOperationException("Photos don't support direct field changes");
    }

    /** {@inheritDoc} */
    public void setValues(DataKind kind, ValuesDelta values, EntityDelta state, boolean readOnly,
            ViewIdGenerator vig) {
        mEntry = values;
        mReadOnly = readOnly;

        setId(vig.getId(state, kind, values, 0));

        if (values != null) {
	     Log.d("11111111","values = " + values);
            // Try decoding photo if actual entry
            if (values.getAsInteger(DisplayPhoto.PHOTO_FILE_ID) != null) {
                mHasSetPhoto = true;
                mEntry.setFromTemplate(false);
            } else {
                resetDefault();
            }
        } else {
            resetDefault();
        }
    }

    /**
     * Return true if a valid {@link Photo} has been set.
     */
    public boolean hasSetPhoto() {
        return mHasSetPhoto;
    }

    /**
     * Assign the given {@link Bitmap} as the new value, updating UI and
     * readying for persisting through {@link ValuesDelta}.
     */
    public void setPhotoBitmap(Bitmap photo) {
        if (photo == null) {
            // Clear any existing photo and return
            if (mEntry != null) {
                mEntry.put(Photo.PHOTO, (byte[])null);
            }
            resetDefault();
            return;
        }
	if (photo != null) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		photo.compress(Bitmap.CompressFormat.JPEG, 75, stream);

		if (mEntry != null) {
			mEntry.put(Photo.PHOTO, stream.toByteArray());
			mEntry.setFromTemplate(false);

			// When the user chooses a new photo mark it as super primary
			mEntry.put(Photo.IS_SUPER_PRIMARY, 1);
		}	
		
	}        
    }

    /**
     * Set the super primary bit on the photo.
     */
    public void setSuperPrimary(boolean superPrimary) {
        mEntry.put(Photo.IS_SUPER_PRIMARY, superPrimary ? 1 : 0);
    }

    protected void resetDefault() {
        mHasSetPhoto = false;
        if (mEntry != null) {
            mEntry.setFromTemplate(true);
        }
    }

    /** {@inheritDoc} */
    public void setEditorListener(EditorListener listener) {
        mListener = listener;
    }
     /**
     * Inserts a photo on the raw contact.
     * @param values the photo values
     * @param assertAccount if true, will check to verify that no photos exist for Google,
     *     Exchange and unsynced phone account types. These account types only take one picture,
     *     so if one exists, the account will be updated with the new photo.
     */
   /* private void insertDisplayPhoto(ContentValues values, Uri rawContactDataUri,
            boolean assertAccount) {

        ArrayList<ContentProviderOperation> operations =
            new ArrayList<ContentProviderOperation>();


	 if (assertAccount) {
            // Make sure no pictures exist for Google, Exchange and unsynced phone accounts.
            operations.add(ContentProviderOperation.newAssertQuery(rawContactDataUri)
                    .withSelection(Photo.MIMETYPE + "=? AND "
                            + RawContacts.ACCOUNT_TYPE + " IS NULL",
                            new String[] {DisplayPhoto.CONTENT_ITEM_TYPE})
                            .withExpectedCount(0).build());
        }

        // insert the photo
        values.put(Photo.MIMETYPE, DisplayPhoto.CONTENT_ITEM_TYPE);
        operations.add(ContentProviderOperation.newInsert(rawContactDataUri)
                .withValues(values).build());

        try {
            mContentResolver.applyBatch(ContactsContract.AUTHORITY, operations);
        } catch (RemoteException e) {
            throw new IllegalStateException("Problem querying raw_contacts/data", e);
        } catch (OperationApplicationException e) {
            // the account doesn't allow multiple photos, so update
            if (assertAccount) {
                updateDisplayPhoto(values, rawContactDataUri, true);
            } else {
                throw new IllegalStateException("Problem inserting photo into raw_contacts/data", e);
            }
        }
    }*/

     /**
     * Tries to update the photo on the raw_contact.  If no photo exists, and allowInsert == true,
     * then will try to {@link #updatePhoto(ContentValues, boolean)}
     */
   /* private void updateDisplayPhoto(ContentValues values, Uri rawContactDataUri,
            boolean allowInsert) {
        ArrayList<ContentProviderOperation> operations =
            new ArrayList<ContentProviderOperation>();

        values.remove(Photo.MIMETYPE);

        // check that a photo exists
        operations.add(ContentProviderOperation.newAssertQuery(rawContactDataUri)
                .withSelection(Photo.MIMETYPE + "=?", new String[] {
                    DisplayPhoto.CONTENT_ITEM_TYPE
                }).withExpectedCount(1).build());

        // update that photo
        operations.add(ContentProviderOperation.newUpdate(rawContactDataUri)
                .withSelection(Photo.MIMETYPE + "=?", new String[] {DisplayPhoto.CONTENT_ITEM_TYPE})
                .withValues(values).build());

        try {
            mContentResolver.applyBatch(ContactsContract.AUTHORITY, operations);
        } catch (RemoteException e) {
            throw new IllegalStateException("Problem querying raw_contacts/data", e);
        } catch (OperationApplicationException e) {
            if (allowInsert) {
                // they deleted the photo between insert and update, so insert one
                insertDisplayPhoto(values, rawContactDataUri, false);
            } else {
                throw new IllegalStateException("Problem inserting photo raw_contacts/data", e);
            }
        }
    }*/
}
