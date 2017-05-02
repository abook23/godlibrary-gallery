package com.god.gallery.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by My on 2017/4/26.
 */

public class StorageDirectoryUtils {

    private List<File> list_image;
    private int SCAN_OK = 100;
    private static final String KEY_NEW_IMAGES = "NEW_IMAGES";
    private static final String KEY_FOLDER_IMAGES = "FOLDER_IMAGES";
    private OnSelectImageListener mListener;

    private static StorageDirectoryUtils newInstance() {
        return new StorageDirectoryUtils();
    }

    public static void getImages(Context context, OnSelectImageListener listener) {
        StorageDirectoryUtils storageDirectoryUtils = newInstance();
        storageDirectoryUtils.mListener = listener;
        storageDirectoryUtils.selectImage(context);
    }

    private void selectImage(Context context) {
        new Thread(new AllImageThread(context)).start();
    }

    private Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == SCAN_OK) {
                Map<String, Object> map = (Map<String, Object>) msg.obj;
                mListener.onSelectImage((List<String>) map.get(KEY_NEW_IMAGES), (HashMap<String, ArrayList<String>>) map.get(KEY_FOLDER_IMAGES));
            }
        }
    };

    // new Thread(new PathImageThread("/DCIM/Camera")).start();
    private class PathFileThread implements Runnable {
        private String path;

        public PathFileThread(String path) {
            this.path = path;
        }

        @Override
        public void run() {
            //获取存储卡路径
            final String mCardPath = Environment.getExternalStorageDirectory().getPath() + path;
            File file = new File(mCardPath);
            //文件过滤器
            MyFilenameFilter myFilenameFilter = new MyFilenameFilter(new String[]{".jpg"});
            File[] files = file.listFiles(myFilenameFilter);
            //File[] files = file.listFiles();
            if (files != null) {
                for (File mFile : files) {
                    list_image.add(mFile);
                }
                Collections.sort(list_image, new FileComparator());//排序
            }
            mHandler.sendEmptyMessage(SCAN_OK);
        }
    }

    /**
     * 查找图片
     */
    private class AllImageThread implements Runnable {
        private Context context;
        private Map<String, ArrayList<String>> imageMapFolder = new HashMap<>();
        private List<String> imageNew = new ArrayList<>();

        public AllImageThread(Context context) {
            this.context = context;
        }

        @Override
        public void run() {
            long t = System.currentTimeMillis();
            L.d(System.currentTimeMillis() - t + "ms");
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            ContentResolver mContentResolver = context.getContentResolver();
            String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.ORIENTATION};
            Cursor cursor = mContentResolver.query(uri, projection,
                    MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?",
                    new String[]{"image/jpeg", "image/png"},//截屏 属于 png
                    MediaStore.Images.Media.DATE_MODIFIED + " DESC");
            L.d(System.currentTimeMillis() - t + "ms");
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    //获取图片的路径
                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    imageNew.add(path);
                    //图片父路径
                    String parenName = new File(path).getParentFile().getName();
                    if ("cache".equals(parenName.toLowerCase()))
                        continue;
                    ArrayList<String> files = imageMapFolder.get(parenName);
                    if (files != null) {
                        files.add(path);
                    } else {
                        files = new ArrayList<>();
                        files.add(path);
                        imageMapFolder.put(parenName, files);
                    }
                }
                cursor.close();
            }
            L.d(System.currentTimeMillis() - t + "ms");
            Map<String, Object> objectMap = new HashMap<>();
            objectMap.put(KEY_NEW_IMAGES, imageNew);
            objectMap.put(KEY_FOLDER_IMAGES, imageMapFolder);
            mHandler.obtainMessage(SCAN_OK, objectMap).sendToTarget();
            L.d(System.currentTimeMillis() - t + "ms");
        }
    }

    /**
     * 文件过滤
     */
    public class MyFilenameFilter implements FilenameFilter {
        private String[] types;

        public MyFilenameFilter(String[] types) {
            this.types = types;
        }

        @Override
        public boolean accept(File dir, String filename) {
            for (String type : types) {
                return filename.endsWith(type);
            }
            return false;
        }
    }

    /**
     * 文件排序
     */
    public class FileComparator implements Comparator<File> {

        @Override
        public int compare(File lhs, File rhs) {
            if (lhs.lastModified() < rhs.lastModified()) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    public interface OnSelectImageListener {
        void onSelectImage(List<String> newImages, HashMap<String, ArrayList<String>> images);
    }
}
