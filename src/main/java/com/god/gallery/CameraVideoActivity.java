package com.god.gallery;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.god.gallery.fagment.CameraVideoFragment;

public class CameraVideoActivity extends BaseFragmentActivity {

    public final static int FLAG_IMAGE = 0;
    public final static int FLAG_VIDEO = 1;
    public final static int FLAG_SELECT_IMAGE = 2;
    public static String DATA = "data";
    public static String FLAG = "flag";
    private static int sCheckMax = 1;

    private int KEY_select_image = 0x01;

    private View mView;

    public static void startForResult(Activity activity, int requestCode) {
        sCheckMax = 1;
        Intent intent = new Intent(activity, CameraVideoActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }
    public static void startForResult(Activity activity,int checkMax, int requestCode) {
        sCheckMax =  checkMax;
        Intent intent = new Intent(activity, CameraVideoActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_video);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); // 设置全屏

        ImageView iv_gallery = (ImageView) findViewById(R.id.iv_gallery);
        ImageView iv_close = (ImageView) findViewById(R.id.iv_close);
        mView =  findViewById(R.id.gb_fl_back_and_gallery);
        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        iv_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onViewClicked();
            }
        });

        CameraVideoFragment videoFragment = CameraVideoFragment.newInstance();
        videoFragment.setDefinition(CameraVideoFragment.Definition.SD);//没特殊要求,sd 就可以了,HD 的视频有点大,微信就相当于SD模式
        videoFragment.setVideoMaxDuration(60 * 1000);//最大录入时间,默认10s
        videoFragment.setVideoRatio(0.8f);//视频质量 ----微信视频 质量大概在 0.8f 左右, 要清晰一点,就调节大一些
        //videoFragment.setVideoMaxZie(50 * 1024 * 1024);//默认50MB
        videoFragment.setOnCameraVideoListener(new CameraVideoFragment.OnCameraVideoListener() {
            @Override
            public void onFragmentResult(String path, String type) {
                Intent intent = new Intent();
                intent.putExtra("flag", type.equals("jpg") ? FLAG_IMAGE : FLAG_VIDEO);
                intent.putExtra("data", path);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        videoFragment.setOnCameraVideoTouchListener(new CameraVideoFragment.OnCameraVideoTouchListener() {
            @Override
            public void onLongClick() {
                mView.setVisibility(View.GONE);
            }

            @Override
            public void onClick() {
                mView.setVisibility(View.GONE);
            }

            @Override
            public void onSuccess(String path) {

            }

            @Override
            public void onCancel() {
                mView.setVisibility(View.VISIBLE);
            }
        });

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.videoFragment, videoFragment);
        transaction.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == KEY_select_image) {
            Intent intent = new Intent();
            intent.putExtra("flag", FLAG_SELECT_IMAGE);
            intent.putStringArrayListExtra("data", data.getStringArrayListExtra(PhotoActivity.DATA));
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    public void onViewClicked() {
        Intent intent = new Intent(context, PhotoActivity.class);
        intent.putExtra(PhotoActivity.CHECK_MAX, sCheckMax);
        startActivityForResult(intent, KEY_select_image);
    }
}
