package com.lewa.PIM.IM;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

import android.util.Log;
import android.webkit.MimeTypeMap;

public final class IMMessage {
	public static final long MAX_FILE_SIZE = 10000000L;
	public static final int MAX_SIZE = 10000;
	public static final int GeXinIM = 0;
	public static final int EcpIM = 1;
	public static final int MaxIMtype = EcpIM + 1;

	private HashMap<String, String> mStringHash = new HashMap();
	private HashMap<String, String> mExtraHash = new HashMap();
	private HashMap<String, byte[]> mImageHash = new HashMap();
	private String[] mFileUris;
	private String[] mFileUris_MimeType;
	private File mSendFile;
	private String mReceiveFileUri;
	private String mThreadId;
	private boolean mAllowForward = true;

	private final String mContentId;
	private final int mIMId;
	private BitmapFactory.Options mBitmapOptions = new BitmapFactory.Options();
	private int mTotalSize;
	private final String[][] MimeTypeTable = { { "3gp", "video/3gpp" },
			{ "amr", "audio/amr" }, { "avi", "video/x-msvideo" },
			{ "m3u", "audio/x-mpegurl" }, { "bmp", "image/bmp" },
			{ "jpeg", "image/jpeg" }, { "jpg", "image/jpg" },
			{ "m3u", "audio/x-mpegurl" }, { "m4a", "audio/mp4a-latm" },
			{ "m4b", "audio/mp4a-latm" }, { "m4u", "video/vnd.mpegurl" },
			{ "m4v", "video/x-m4v" }, { "mov", "video/quicktime" },
			{ "mp3", "audio/x-mpeg" }, { "mp4", "video/mp4" },
			{ "mpe", "video/mpeg" }, { "mpeg", "video/mpeg" },
			{ "mpg", "video/mpeg" }, { "mpg4", "audio/mp4" },
			{ "ogg", "audio/ogg" }, { "png", "image/png" },
			{ "rmvb", "audio/x-pn-realaudio" } };

	public IMMessage(int parmIMtype) {
		this.mIMId = parmIMtype;
		this.mContentId = UUID.randomUUID().toString();
	}

	public final String getContentId() {
		return this.mContentId;
	}

	private IMMessage(String paramString1, int paramString2,
			HashMap<String, String> paramHashMap1,
			HashMap<String, String> paramHashMap2,
			HashMap<String, byte[]> paramHashMap) {
		this.mIMId = paramString2;
		this.mContentId = paramString1;
		this.mStringHash = paramHashMap1;
		this.mExtraHash = paramHashMap2;
		this.mImageHash = paramHashMap;
	}

	public final String getExtra(String paramString) {
		return (String) this.mExtraHash.get(paramString);
	}

	public final HashMap<String, String> getExtras() {
		return (HashMap) this.mExtraHash.clone();
	}

	public final Drawable getPreviewImage() {
		byte[] localObject;
		if ((localObject = (byte[]) this.mImageHash.get("preview")) != null) {
			return new BitmapDrawable((Bitmap) BitmapFactory.decodeByteArray(
					localObject, 0, localObject.length));
		}
		return (Drawable) null;
	}

	public final String getText() {
		return (String) this.mStringHash.get("text");
	}

	public final String getTitle() {
		return (String) this.mStringHash.get("title");
	}

	public final String getSendAddress() {
		return (String) this.mStringHash.get("sendaddress");
	}

	public final String getThreadId() {
		return (String) this.mStringHash.get("threadid");
	}

	public final void putExtra(String paramString1, String paramString2) {
		int n = paramString1.length() + paramString2.length();
		this.mTotalSize += n;
		this.mExtraHash.put(paramString1, paramString2);
	}

	public final void setFileUri(String[] paramArrayOfString) {
		this.mFileUris = updateTextSizeArray(this.mFileUris, paramArrayOfString);
	}

	public final void setImage(BitmapDrawable paramBitmapDrawable) {
		if ((paramBitmapDrawable == null)
				|| (paramBitmapDrawable.getBitmap() == null))
			return;

		int height = paramBitmapDrawable.getBitmap().getHeight();
		int width = paramBitmapDrawable.getBitmap().getWidth();
		int i2 = Math.max(width, height);
		float f1 = 185.0F / i2;
		Matrix localMatrix;
		(localMatrix = new Matrix()).postScale(f1, f1);
		Bitmap workbitmap = Bitmap.createBitmap(
				paramBitmapDrawable.getBitmap(), 0, 0, width, height,
				localMatrix, true);
		ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
		int i1 = 70;
		do {
			localByteArrayOutputStream.reset();
			workbitmap.compress(Bitmap.CompressFormat.JPEG, i1,
					localByteArrayOutputStream);
			i1 -= 10;
		} while ((localByteArrayOutputStream.size() > 5120) && (i1 >= 10));
		savePreviewImage(localByteArrayOutputStream.toByteArray());
		try {
			localByteArrayOutputStream.close();
			return;
		} catch (IOException localIOException) {
		}
	}

	public final void setImage(Context paramContext, int resid) {
		BitmapFactory.Options localOptions;
		BitmapFactory.decodeResource(paramContext.getResources(), resid,
				this.mBitmapOptions);
		(localOptions = new BitmapFactory.Options()).inSampleSize = (this.mBitmapOptions.outWidth / 100);
		setImage(new BitmapDrawable(BitmapFactory.decodeResource(
				paramContext.getResources(), resid, localOptions)));
	}

	public final void setImage(File paramFile) throws IOException {
		Bitmap scaleBitmap = null;
		try {
			BitmapFactory.Options tempOptions;
			BitmapFactory.decodeStream(new FileInputStream(paramFile), null,
					this.mBitmapOptions);
			(tempOptions = new BitmapFactory.Options()).inSampleSize = (this.mBitmapOptions.outWidth / 100);
			scaleBitmap = BitmapFactory.decodeStream(
					(InputStream) new FileInputStream(paramFile), null,
					tempOptions);
		} catch (FileNotFoundException localFileNotFoundException) {
			throw localFileNotFoundException;
		}
		setImage(new BitmapDrawable((Bitmap) scaleBitmap));
	}

	public final void setText(String paramString) {
		updateTextSize((String) this.mStringHash.get("text"), paramString);
		if (paramString != null) {
			this.mStringHash.put("text", paramString);
			return;
		}
		this.mStringHash.remove("text");
	}

	public final void setSendAddress(String paramString) {
		updateTextSize((String) this.mStringHash.get("sendaddress"),
				paramString);
		if (paramString != null) {
			this.mStringHash.put("sendaddress", paramString);
			return;
		}
		this.mStringHash.remove("sendaddress");
	}

	public final void setThreadId(String paramString) {
		updateTextSize((String) this.mStringHash.get("threadid"), paramString);
		if (paramString != null) {
			this.mStringHash.put("threadid", paramString);
			return;
		}
		this.mStringHash.remove("threadid");
	}

	public final void setTitle(String paramString) {
		updateTextSize((String) this.mStringHash.get("title"), paramString);
		if (paramString != null) {
			this.mStringHash.put("title", paramString);
			return;
		}
		this.mStringHash.remove("title");
	}

	public final String getReceiverFileUrl() {
		return this.mReceiveFileUri;
	}

	public final String[] getSendFileUrls() {
		return this.mFileUris;
	}

	public final String[] getSendFileUrlsMimeType() {
		return this.mFileUris_MimeType;
	}

	public final void setFile(File paramFile) throws IOException {
		if (this.mSendFile != null) {
			if (!this.mSendFile.exists())
				throw new IOException(
						"Cannot attach file because it does not exist!");
			if (this.mSendFile.length() > 10000000L)
				throw new IOException(
						"File too large! Cannot exceed 10000000 bytes");
		}
		this.mSendFile = paramFile;
	}

	public final void setAllowForwarding(boolean paramBoolean) {
		this.mAllowForward = paramBoolean;
	}

	final void checkFile() throws IOException {
		if (this.mSendFile != null) {
			if (!this.mSendFile.exists())
				throw new IOException(
						"Cannot attach file because it does not exist!");
			if (this.mSendFile.length() > 10000000L)
				throw new IOException(
						"File too large! Cannot exceed 10000000 bytes");
			String fileName = this.mSendFile.getName();
			long fileSize = this.mSendFile.length();
			if (fileName != null) {
				if (fileName.replaceAll("[^a-zA-Z0-9 -_.,!@#$&()]", "")
						.length() > 256)
					fileName = ((String) fileName).substring(
							((String) fileName).length() - 256,
							((String) fileName).length());
				mStringHash.put("file-name", fileName);
				fileSize = 256;
			}
			mStringHash.put("file-size", Long.toString(fileSize));
		}
	}

	final void CheckMessageSize() throws IOException {
		if (this.mTotalSize > 10000)
			throw new IOException(
					"Message is invalid and cannot send because total data size exceeds limit of 10000 bytes.");
		if (this.mTotalSize < 0)
			throw new IOException(
					"Message is invalid and cannot send because data size has somehow become negative.");
	}

	final void checkIMType() throws IOException {
		if (this.mIMId > MaxIMtype)
			throw new IOException(
					"Message is invalid and cannot send because of invalid app Id.");
	}

	static public IMMessage getIMmessageFromIntent(Intent paramIntent) {
		Bundle getBundle;
		IMMessage valueIMMessage;
		if ((getBundle = paramIntent.getExtras()) == null)
			return null;

		String ContentId = getBundle
				.getString("com.lewa.PIM.IM.content.EXTRA_CONTENT_ID");
		int IMtype = getBundle.getInt("com.lewa.PIM.IM.content.EXTRA_IM_ID");
		getBundle.getString("com.lewa.PIM.IM.content.EXTRA_CONVO_ID");
		HashMap localHashMap1 = (HashMap) getBundle
				.get("com.lewa.PIM.IM.content.EXTRA_EXTRA_HASH");
		HashMap localHashMap2 = (HashMap) getBundle
				.get("com.lewa.PIM.IM.content.EXTRA_STRING_HASH");
		String str2 = null;
		boolean n = true;
		if (localHashMap2 != null) {
			n = getBundle.getBoolean("allow-forward");
			localHashMap2.remove("allow-forward");
			str2 = (String) localHashMap2.get("file-url");
			localHashMap2.remove("file-url");
		}
		HashMap<String, byte[]> imageHash = (HashMap) getBundle
				.get("com.lewa.PIM.IM.content.EXTRA_IMAGE_HASH");
		(valueIMMessage = new IMMessage(ContentId, IMtype, localHashMap2,
				localHashMap1, imageHash)).mAllowForward = n;
		valueIMMessage.mReceiveFileUri = str2;
		valueIMMessage.mFileUris = getBundle
				.getStringArray("com.lewa.PIM.IM.content.EXTRA_URIS");
		valueIMMessage.mFileUris_MimeType = getBundle
				.getStringArray("com.lewa.PIM.IM.content.EXTRA_URI_MIMETYPE");
		return (IMMessage) valueIMMessage;
	}

	final void CombineIntent(Intent paramIntent) {
		Vector localVector = new Vector();
		MimeTypeMap mimetypeMap = MimeTypeMap.getSingleton();
		int n;
		if (this.mFileUris != null) {
			for (n = 0; n < this.mFileUris.length; n++) {
				String minetype = "text/plain";
				String Extension = MimeTypeMap
						.getFileExtensionFromUrl(mFileUris[n]);
				for (int i = 0; i < MimeTypeTable.length; i++) {
					if (Extension.endsWith(MimeTypeTable[i][0])) {
						minetype = MimeTypeTable[i][1];
						break;
					}
				}
				localVector.add(new UriGroup(this.mFileUris[n], minetype, n + 1));
			}
		}
		String[] arrayOfString1;
		String[] arrayOfString2 = new String[(arrayOfString1 = new String[localVector
				.size()]).length];
		int[] arrayOfInt = new int[arrayOfString1.length];
		for (int i1 = 0; i1 < arrayOfString1.length; i1++) {
			arrayOfString1[i1] = ((UriGroup) localVector.elementAt(i1)).url;
			arrayOfString2[i1] = ((UriGroup) localVector.elementAt(i1)).MimeType;
			arrayOfInt[i1] = ((UriGroup) localVector.elementAt(i1)).id;
		}
		paramIntent.putExtra("com.lewa.PIM.IM.content.EXTRA_URIS",
				arrayOfString1);
		paramIntent.putExtra("com.lewa.PIM.IM.content.EXTRA_URI_MIMETYPE",
				arrayOfString2);
		paramIntent.putExtra("com.lewa.PIM.IM.content.EXTRA_URI_PRIORITIES",
				arrayOfInt);
		paramIntent.putExtra("com.lewa.PIM.IM.content.EXTRA_EXTRA_HASH",
				this.mExtraHash);
		paramIntent.putExtra("com.lewa.PIM.IM.content.EXTRA_STRING_HASH",
				this.mStringHash);
		paramIntent.putExtra("com.lewa.PIM.IM.content.EXTRA_IMAGE_HASH",
				this.mImageHash);

		paramIntent.putExtra("com.lewa.PIM.IM.content.EXTRA_IM_ID", this.mIMId);
		paramIntent.putExtra("com.lewa.PIM.IM.content.EXTRA_CONTENT_ID",
				this.mContentId);
	}

	final File getFile() {
		return this.mSendFile;
	}

	private String updateTextSize(String paramString1, String paramString2) {
		if (paramString1 != null)
			this.mTotalSize -= paramString1.length();
		if (paramString2 != null) {
			this.mTotalSize += paramString2.length();
		}
		return paramString2;
	}

	private String[] updateTextSizeArray(String[] paramArrayOfString1,
			String[] paramArrayOfString2) {
		int n;
		if (paramArrayOfString1 != null)
			for (n = 0; n < paramArrayOfString1.length; n++)
				this.mTotalSize -= paramArrayOfString1[n].length();
		if (paramArrayOfString2 != null)
			for (n = 0; n < paramArrayOfString2.length; n++)
				this.mTotalSize += paramArrayOfString2[n].length();
		return paramArrayOfString2;
	}

	private void savePreviewImage(byte[] paramArrayOfByte) {
		if (paramArrayOfByte != null) {
			this.mImageHash.put("preview", paramArrayOfByte);
			return;
		}
		this.mImageHash.remove("preview");
	}

	final void decodePrerviewImage() {
		byte[] previewImageByte;
		if ((previewImageByte = (byte[]) this.mImageHash.get("preview")) == null)
			return;
		Object localObject = BitmapFactory.decodeByteArray(previewImageByte, 0,
				previewImageByte.length);
		float f1 = 60.0F / ((Bitmap) localObject).getHeight();
		Matrix localMatrix;
		(localMatrix = new Matrix()).postScale(f1, f1);
		localObject = Bitmap.createBitmap((Bitmap) localObject, 0, 0,
				((Bitmap) localObject).getWidth(),
				((Bitmap) localObject).getHeight(), localMatrix, true);
		ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
		int n = 70;
		do {
			localByteArrayOutputStream.reset();
			((Bitmap) localObject).compress(Bitmap.CompressFormat.JPEG, n,
					localByteArrayOutputStream);
			n -= 10;
		} while ((localByteArrayOutputStream.size() > 5120) && (n >= 10));
		savePreviewImage(localByteArrayOutputStream.toByteArray());
	}

	public final String toString() {
		StringBuilder localStringBuilder;
		(localStringBuilder = new StringBuilder())
				.append("IMMessage:{App-Id: ");
		localStringBuilder.append(this.mIMId);
		localStringBuilder.append(" Content-id: ");
		localStringBuilder.append(this.mContentId);
		localStringBuilder.append(" Size: ");
		localStringBuilder.append(this.mTotalSize);
		localStringBuilder.append(" Allow-Forwarding: ");
		localStringBuilder.append(this.mAllowForward);
		localStringBuilder.append(" File:");
		localStringBuilder.append(this.mSendFile == null ? null
				: this.mSendFile.getAbsolutePath());
		localStringBuilder.append(" Android Uris: ");
		localStringBuilder.append(this.mFileUris == null ? "[]"
				: this.mFileUris.toString());
		localStringBuilder.append(" Images: [");
		Iterator localIterator = this.mImageHash.entrySet().iterator();
		Map.Entry localEntry;
		while (localIterator.hasNext()) {
			localEntry = (Map.Entry) localIterator.next();
			localStringBuilder.append("name:" + localEntry.getKey().toString());
			localStringBuilder.append("size:"
					+ ((byte[]) localEntry.getValue()).length + ",");
		}
		localStringBuilder.append("]");
		localStringBuilder.append(" Extras: [");
		localIterator = this.mExtraHash.entrySet().iterator();
		while (localIterator.hasNext()) {
			localEntry = (Map.Entry) localIterator.next();
			localStringBuilder.append("name:" + localEntry.getKey().toString());
			localStringBuilder.append("size:"
					+ localEntry.getValue().toString() + ",");
		}
		localStringBuilder.append("]}");
		return localStringBuilder.toString();
	}

	private static class UriGroup {
		public String url;
		public String MimeType = null;
		public int id = 1000;

		public UriGroup(String paramString1, String paramString2, int paramInt) {
			this.url = paramString1;
			this.MimeType = paramString2;
			this.id = paramInt;
		}
	}
}
