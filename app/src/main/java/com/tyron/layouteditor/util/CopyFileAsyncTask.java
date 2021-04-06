package com.tyron.layouteditor.util;

import android.content.Context;
import android.os.AsyncTask;
import android.database.Cursor;
import android.provider.OpenableColumns;

import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.File;
import java.io.FileNotFoundException;

import java.lang.ref.WeakReference;

import android.net.Uri;

public class CopyFileAsyncTask extends AsyncTask<Uri, Integer, String> {

    public interface CallBackTask {
        void onCopyPreExecute();
        void onCopyProgressUpdate(int progress);
        void onCopyPostExecute(String path, boolean wasSuccessful, String reason);
    }

    private Uri mUri;
    private CallBackTask callback;
    private WeakReference<Context> mContext;
    private String pathPlusName;
    private String name;
    private File folder;
    private Cursor returnCursor;
    private InputStream is = null;
    private String errorReason = "";

    public CopyFileAsyncTask(Uri uri, Context context, CallBackTask callback) {
        this.mUri = uri;
        mContext = new WeakReference<>(context);
        this.callback = callback;
    }

    @Override
    protected void onPreExecute() {
        callback.onCopyPreExecute();
        Context context = mContext.get();
        if (context != null) {
            folder = context.getExternalFilesDir("images");
            returnCursor = context.getContentResolver().query(mUri, null, null, null, null);
            try {
                is = context.getContentResolver().openInputStream(mUri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        int post = values[0];
        callback.onCopyProgressUpdate(post);
    }

    @Override
    protected String doInBackground(Uri... params) {
        File file = null;
        int size = -1;

        try {
            try {
                if (returnCursor != null && returnCursor.moveToFirst()){
                    if (mUri.getScheme() != null)
                        if (mUri.getScheme().equals("content")) {
                            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                            size = (int) returnCursor.getLong(sizeIndex);
                        }else if (mUri.getScheme().equals("file")) {
                            File ff = new File(mUri.getPath());
                            size = (int) ff.length();
                        }
                }
            }
            finally {
                if (returnCursor != null)
                    returnCursor.close();
            }

            name = getFileName(mUri, mContext.get());
            pathPlusName = folder + "/" + name;
            file = new File(folder + "/" + getFileName(mUri, mContext.get()));

            BufferedInputStream bis = new BufferedInputStream(is);
            FileOutputStream fos = new FileOutputStream(file);


            byte[] data = new byte[1024];
            long total = 0;
            int count;
            while ((count = bis.read(data)) != -1) {
                if (!isCancelled()) {
                    total += count;
                    if (size != -1) {
                        publishProgress((int) ((total * 100) / size));
                    }
                    fos.write(data, 0, count);
                }
            }
            fos.flush();
            fos.close();

        } catch (IOException e) {
            errorReason = e.getMessage();
        }

        return file.getAbsolutePath();

    }

    private String getFileName(Uri uri, Context context) {
        String result = null;
        if (uri.getScheme() != null) {
            if (uri.getScheme().equals("content")) {
                Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            assert result != null;
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    protected void onPostExecute(String result) {
        if(result == null){
            callback.onCopyPostExecute(pathPlusName, false, errorReason);
        }else {
            callback.onCopyPostExecute(pathPlusName, true, name);
        }
    }
}
