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

package com.lewa.player.ui.view;

import com.lewa.player.R;
import com.lewa.player.model.Editor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Simple editor for {@link Photo}.
 */
public class PhotoEditorView extends ImageView implements Editor, OnClickListener {
    private static final String TAG = "PhotoEditorView";

    private EditorListener mListener;

    private boolean mHasSetPhoto = false;
    private boolean mReadOnly;

    public PhotoEditorView(Context context) {
        super(context);
    }

    public PhotoEditorView(Context context, AttributeSet attrs) {
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
            mListener.onRequest(EditorListener.REQUEST_PICK_PHOTO);
        }
    }

    /** {@inheritDoc} */
    public void onFieldChanged(String column, String value) {
        throw new UnsupportedOperationException("Photos don't support direct field changes");
    }

//    /** {@inheritDoc} */
//    public void setValues(DataKind kind, ValuesDelta values, EntityDelta state, boolean readOnly,
//            ViewIdGenerator vig) {
//        mEntry = values;
//        mReadOnly = readOnly;
//
//        setId(vig.getId(state, kind, values, 0));
//
//        if (values != null) {
//            // Try decoding photo if actual entry
//            final byte[] photoBytes = values.getAsByteArray(Photo.PHOTO);
//            if (photoBytes != null) {
//                final Bitmap photo = BitmapFactory.decodeByteArray(photoBytes, 0,
//                        photoBytes.length);
//
//                setScaleType(ImageView.ScaleType.CENTER_CROP);
//                setImageBitmap(photo);
//                setEnabled(true);
//                mHasSetPhoto = true;
//                mEntry.setFromTemplate(false);
//            } else {
//                resetDefault();
//            }
//        } else {
//            resetDefault();
//        }
//    }

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

            resetDefault();
            return;
        }

        final int size = photo.getWidth() * photo.getHeight() * 4;
        final ByteArrayOutputStream out = new ByteArrayOutputStream(size);

        try {
            photo.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

            setImageBitmap(photo);
            setEnabled(true);
            mHasSetPhoto = true;

        } catch (IOException e) {
            Log.w(TAG, "Unable to serialize photo: " + e.toString());
        }
    }



    protected void resetDefault() {
        // Invalid photo, show default "add photo" place-holder
        setScaleType(ImageView.ScaleType.CENTER);
        if (mReadOnly) {
            setImageResource(R.drawable.ic_playlist_cover);
            setEnabled(false);
        } else {
            setImageResource(R.drawable.ic_playlist_add_cover);
            setEnabled(true);
        }
        mHasSetPhoto = false;
    }

    /** {@inheritDoc} */
    public void setEditorListener(EditorListener listener) {
        mListener = listener;
    }
}
