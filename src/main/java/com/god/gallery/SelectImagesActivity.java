package com.god.gallery;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Toast;

import com.god.gallery.fagment.FragmentGrid;

import java.util.ArrayList;

/**
 * 图片选择
 * Created by abook23 on 2015/10/19.
 */
public class SelectImagesActivity extends BaseFragmentActivity implements FragmentGrid.Callback {

    public static String folderName;
    /**
     * 文件夹路径
     */
    public static String PATHS = "paths";
    private ArrayList<String> paths;
    private String TAG = SelectImagesActivity.class.toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_activity_select_photo);
        addTitleBarFragment();

        Intent intent = getIntent();
        paths = intent.getStringArrayListExtra(PATHS);

        FragmentGrid fragmentGrid = FragmentGrid.newInstance(paths);
        fragmentGrid.setCallback(this);

        if (getSupportFragmentManager().findFragmentByTag(TAG) == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.content, fragmentGrid, TAG);
            ft.commit();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        titleBar.submit.setText("完成(" + PhotoActivity.checkCount + "/" + PhotoActivity.checkMax + ")");
        titleBar.submit.setOnClickListener(new OnSubmitClickListener());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 3 && PhotoActivity.checkMax == 1) {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                this.finish();
            } else {
                ArrayList<String> list = data.getStringArrayListExtra(ImageInfoActivity.DATA);
                PhotoActivity.setFolderCheck(folderName, list);
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onImageSelected(String path) {
        if (PhotoActivity.checkCount >= PhotoActivity.checkMax) {
            Toast.makeText(context, "最多只能选择" + PhotoActivity.checkMax, Toast.LENGTH_SHORT).show();
            return false;
        }
        PhotoActivity.setCheckImage(true, path);
        titleBar.submit.setText("完成(" + PhotoActivity.checkCount + "/" + PhotoActivity.checkMax + ")");
        if (PhotoActivity.checkMax == 1) {
            this.finish();
        }
        return true;
    }

    @Override
    public void onImageUnselected(String path) {
        PhotoActivity.setCheckImage(false, path);
        titleBar.submit.setText("完成(" + PhotoActivity.checkCount + "/" + PhotoActivity.checkMax + ")");
    }

    private class OnSubmitClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            SelectImagesActivity.this.finish();
        }
    }

}
