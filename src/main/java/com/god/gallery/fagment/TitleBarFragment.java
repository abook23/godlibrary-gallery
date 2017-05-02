package com.god.gallery.fagment;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.god.gallery.R;

import java.lang.reflect.Field;


/**
 * Created by abook23 on 2015/7/8.
 */
public class TitleBarFragment extends Fragment {
    public Button submit;
    public ImageView more, back;
    public TextView title;
    private View positionView;
    public static int COLOR_CHECK_BUTTON = R.color.transparent;
    public static int COLOR_BACK_BUTTON;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.gallery_item_title_bar_lib, container, false);
        back = (ImageView) v.findViewById(R.id.iv_back);
        submit = (Button) v.findViewById(R.id.but_submit);
        more = (ImageView) v.findViewById(R.id.iv_more);
        title = (TextView) v.findViewById(R.id.tv_title);
        back.setOnClickListener(new onBackListener());
        more.setVisibility(View.GONE);
        submit.setBackgroundResource(COLOR_CHECK_BUTTON);
        v.setBackgroundResource(COLOR_BACK_BUTTON);

        positionView = v.findViewById(R.id.statusBar);
        dealStatusBar(positionView); // 调整状态栏高度

        return v;
    }

    private class onBackListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            getActivity().finish();
        }
    }

    /**
     * 调整沉浸式菜单的title
     */
    private void dealStatusBar(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 1. 沉浸式状态栏
            Window window = getActivity().getWindow();
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            int statusBarHeight = getStatusBarHeight();
            ViewGroup.LayoutParams lp = positionView.getLayoutParams();
            lp.height = statusBarHeight;
            view.setLayoutParams(lp);
        }
    }

    private int getStatusBarHeight() {
        int statusBarHeight = 0;
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return statusBarHeight;
    }

}
