package com.god.gallery;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.god.gallery.adapter.PhotoAdapter;
import com.god.gallery.dialog.DialogLoading;
import com.god.gallery.fagment.FragmentGallery;
import com.god.gallery.fagment.TitleBarFragment;
import com.god.gallery.util.L;
import com.god.gallery.util.PermissionUtil;
import com.god.gallery.util.StorageDirectoryUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class PhotoActivity extends BaseFragmentActivity implements AdapterView.OnItemClickListener {

    private static final int RESULT_SELECT = 101;
    private static final int IMAGE_INFO_CODE = 102;
    public static String CHECK_MAX = "checkCount";//能选中多少张
    public static String CHECK_PATH = "check_path";//被选中的图片
    public static String DATA = "data";//返回值

    public static int CHECK_BUTTON_COLOR = R.color.colorCheckButton;
    public static int COLOR_BACK_BUTTON = R.color.colorTitleBack;

    private static final int REQUEST_CONTACTS = 1;
    private static String[] PERMISSIONS_CONTACT = {Manifest.permission.READ_EXTERNAL_STORAGE};

    public static HashMap<String, ArrayList<String>> checkMaps;//每个文件夹选择的图片
    public static ArrayList<String> checkList;//每个文件夹选择的图片
    public static int checkCount;//选择数量
    public static int checkMax;//总数
    private static boolean listViewAdapterUpdata;

    private HashMap<String, ArrayList<String>> map_folder;//所有文件图片
    private ArrayList<String> recentlyImage = new ArrayList<>();//最近的照片
    private DialogLoading loadingDialog;
    private FragmentGallery fragmentGallery;
    private PhotoAdapter photoAdapter;

    /**
     * @param ac         Activity
     * @param checkMax   最多选择多少张
     * @param checkPath  已选择的图片
     * @param resultCode 返回code
     */
    public static void startActivityForResult(Activity ac, int checkMax, ArrayList<String> checkPath, int resultCode) {
        Intent intent = new Intent(ac, PhotoActivity.class);
        intent.putExtra(CHECK_MAX, checkMax);
        intent.putExtra(CHECK_PATH, checkPath);
        ac.startActivityForResult(intent, resultCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_activity_photos);
        TitleBarFragment.COLOR_CHECK_BUTTON = CHECK_BUTTON_COLOR;
        TitleBarFragment.COLOR_BACK_BUTTON = COLOR_BACK_BUTTON;
        addTitleBarFragment();
        context = this;
        checkCount = 0;
        checkMaps = new HashMap<>();
        checkList = new ArrayList<>();

        //listViewAdapter = new ListViewAdapter(context);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        photoAdapter = new PhotoAdapter(context);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(photoAdapter);
        photoAdapter.setOnItemClickListener(this);

        Intent intent = getIntent();
        checkMax = intent.getIntExtra(CHECK_MAX, 9);//选择数量
        ArrayList<String> cPaths = intent.getStringArrayListExtra(CHECK_PATH);//选择的图片
        if (cPaths != null)
            for (String path : cPaths) {
                if (checkCount < checkMax)
                    setCheckImage(true, path);
            }
        initUI();
        requestContactsPermissions();//读取本地文件,需要权限
    }

    private void requestContactsPermissions() {
        if (PermissionUtil.requestPermission(this, PERMISSIONS_CONTACT, REQUEST_CONTACTS)) {
            //已经获取权限
            selectImage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CONTACTS) {
            if (PermissionUtil.verifyPermissions(context, PERMISSIONS_CONTACT, grantResults)) {
                // Log.i(TAG, "已经全部授权");
                selectImage();
            } else {
                // Log.i(TAG, "缺少必要的权限");
            }
        }
    }

    protected void initUI() {
        loadingDialog = new DialogLoading(context);
        loadingDialog.show("加载中....");

        fragmentGallery = FragmentGallery.newInstance(true);
        getSupportFragmentManager().beginTransaction().add(R.id.gb_fl_gallery, fragmentGallery).commit();
        fragmentGallery.setOnClickListener(new OnItemViewListener() {
            @Override
            public void onItemViewClick(View parentView, View childView, int position) {
                if (childView instanceof CheckBox) {
                    CheckBox checkBox = (CheckBox) childView;
                    if (checkBox.isChecked() && checkCount >= checkMax) {
                        checkBox.setChecked(false);
                        return;
                    }
                    setCheckImage(checkBox.isChecked(), recentlyImage.get(position));
                    titleBar.submit.setText("完成(" + checkCount + "/" + PhotoActivity.checkMax + ")");

                    photoAdapter.setCheckImages(checkMaps);
                    photoAdapter.notifyDataSetChanged();
                    if (checkMax == 1 && checkCount == 1) {
                        onResult();
                    }

                } else if (childView instanceof ImageView) {
                    Intent intent = new Intent();
                    intent.setClass(context, ImageInfoActivity.class);
                    intent.putExtra(ImageInfoActivity.PATHS, recentlyImage);
                    intent.putExtra(ImageInfoActivity.POSITION, position);
                    intent.putExtra(ImageInfoActivity.CHECK_MAX, PhotoActivity.checkMax);
                    intent.putExtra(ImageInfoActivity.CHECK_COUNT, PhotoActivity.checkCount);
                    intent.putExtra(ImageInfoActivity.SHOW_CHECKBOX, true);
                    intent.putStringArrayListExtra(ImageInfoActivity.CHECK_PATHS, getAllCheckImages());
                    startActivityForResult(intent, IMAGE_INFO_CODE);
                }
            }
        });
        fragmentGallery.setCamera(new FragmentGallery.OnCameraListener() {
            @Override
            public void onCameraResult(String path) {
                if (checkMax == 1) {//只选一张图片时
                    setCheckImage(true, path);
                    onResult();
                } else {
                    ArrayList<String> CameraList = map_folder.get("Camera");
                    if (CameraList != null)
                        CameraList.add(0, path);
                    else {
                        ArrayList<String> list = new ArrayList<>();
                        list.add(path);
                        map_folder.put("Camera", list);
                    }
                    setCheckImage(true, path);
                    setData();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (PhotoActivity.checkMax == 1 && PhotoActivity.checkCount == 1) {
            onResult();
        }
        titleBar.submit.setText("完成(" + PhotoActivity.checkCount + "/" + PhotoActivity.checkMax + ")");
        titleBar.submit.setOnClickListener(new OnSubmitClickListener());
        for (String path : getAllCheckImages()) {
            if (recentlyImage.contains(path)) {
                recentlyImage.remove(path);
            }
            recentlyImage.add(0, path);
        }
        fragmentGallery.setData(recentlyImage, getAllCheckImages());

        if (listViewAdapterUpdata) {
            photoAdapter.setCheckImages(checkMaps);
            photoAdapter.notifyDataSetChanged();
            listViewAdapterUpdata = false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == RESULT_SELECT && checkMax == 1) {
                onResult();
            } else if (requestCode == IMAGE_INFO_CODE) {
                ArrayList<String> list = data.getStringArrayListExtra(ImageInfoActivity.DATA);
                checkCount = 0;
                checkMaps.clear();
                checkList.clear();
                for (String s : list) {
                    setCheckImage(true, s);
                }
            }
        }
    }

    protected void selectImage() {
        map_folder = new HashMap<>();//所有图片
        final long t = System.currentTimeMillis();
        L.d(System.currentTimeMillis() - t + "ms");
        StorageDirectoryUtils.getImages(context, new StorageDirectoryUtils.OnSelectImageListener() {
            @Override
            public void onSelectImage(List<String> newImages, HashMap<String, ArrayList<String>> images) {
                map_folder = images;
                loadingDialog.dismiss();
                for (int i = 0; i < 10; i++) {
                    recentlyImage.add(newImages.get(i));
                }
                setData();
                // L.d(System.currentTimeMillis() - t + "ms");
            }
        });
    }

    private void setData() {
        //recentlyImage.clear();
        for (String path : getFolderCheck("Camera")) {
            if (recentlyImage.contains(path)) {
                recentlyImage.remove(path);
            }
            recentlyImage.add(1, path);
        }
        fragmentGallery.setData(recentlyImage, getFolderCheck("Camera"));
        List<ArrayMap<String, String>> list = new ArrayList<>();
        for (Map.Entry<String, ArrayList<String>> entry : map_folder.entrySet()) {
            ArrayMap<String, String> itemData = new ArrayMap<>();
            itemData.put(PhotoAdapter.KEY_PHOTO, entry.getValue().get(0));
            itemData.put(PhotoAdapter.KEY_NAME, entry.getKey());
            itemData.put(PhotoAdapter.KEY_COUNT, map_folder.get(entry.getKey()).size() + "张");
            switch (entry.getKey()) {
                case "Camera":
                    list.add(0, itemData);
                    break;
                case "Screenshots":
                    list.add(1, itemData);
                    break;
                default:
                    list.add(itemData);
                    break;
            }
        }
        photoAdapter.setDate(list);
        photoAdapter.notifyDataSetChanged();
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent();
        intent.setClass(context, SelectImagesActivity.class);
        String key = photoAdapter.getPhotoName(position);
        SelectImagesActivity.folderName = key;
        ArrayList<String> list = map_folder.get(key);
        intent.putStringArrayListExtra(SelectImagesActivity.PATHS, list);
        startActivityForResult(intent, RESULT_SELECT);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        checkMaps = null;
        checkList = null;
        System.gc();
    }


    /**
     * 图片裁剪
     *
     * @param uri
     */
    public static Intent startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽
        intent.putExtra("outputX", 200);
        intent.putExtra("outputY", 200);
        intent.putExtra("return-data", true);
        return intent;
    }

    private class OnSubmitClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            onResult();
        }
    }

    private void onResult() {
        Intent intent = new Intent();
        intent.putStringArrayListExtra(DATA, getAllCheckImages());
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    public static ArrayList<String> getFolderCheck(String folderName) {
        if (checkMaps == null) {
            checkMaps = new HashMap<>();
        }
        ArrayList<String> list = checkMaps.get(folderName);
        if (list == null)
            list = new ArrayList<>();
        return list;
    }

    public static void setFolderCheck(String folderName, ArrayList<String> images) {
        if (checkMaps == null) {
            checkMaps = new HashMap<>();
        }
        checkMaps.put(folderName, images);
        getAllCheckImages();
    }

    public static ArrayList<String> getAllCheckImages() {
//        ArrayList<String> arrayList = new ArrayList<>();
//        checkCount = 0;
//        if (checkMaps == null)
//            return arrayList;
//        for (Map.Entry<String, ArrayList<String>> entry : checkMaps.entrySet()) {
//            if (entry.getValue() != null)
//                for (String path : entry.getValue()) {
//                    arrayList.add(path);
//                    checkCount++;
//                }
//        }
        checkCount = checkList == null ? 0 : checkList.size();
        return checkList;
    }

    public static void setCheckImage(boolean state, String path) {
        if (checkMaps == null) {
            checkMaps = new HashMap<>();
        }

        String parentName = new File(path).getParentFile().getName();
        ArrayList<String> images = checkMaps.get(parentName);
        if (images == null)
            images = new ArrayList<>();
        if (state) {
            images.add(path);
            checkList.add(path);
            checkCount++;
        } else {
            checkList.remove(path);
            images.remove(path);
            checkCount--;
        }
        checkMaps.remove(parentName);
        checkMaps.put(parentName, images);
        listViewAdapterUpdata = true;
    }
}
