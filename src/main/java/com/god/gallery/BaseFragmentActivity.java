package com.god.gallery;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.WindowManager;

import com.god.gallery.fagment.TitleBarFragment;


/**
 * Created by abook23 on 2015/10/21.
 */
public class BaseFragmentActivity extends FragmentActivity {

    protected TitleBarFragment titleBar;
    protected Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//锁定竖屏
        WindowManager.LayoutParams params = getWindow().getAttributes();
        if (params.softInputMode == WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE) {
            // 隐藏软键盘
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            params.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN;
        }
    }

    /**
     * 添加标题
     * resId = R.id.titleBar
     */
    protected void addTitleBarFragment() {
        titleBar = new TitleBarFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.titleBar, titleBar);
        transaction.commit();
    }
}
