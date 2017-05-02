package com.god.gallery;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.god.gallery.fagment.FragmentGallery;
import com.god.gallery.fagment.FragmentImage;
import com.god.gallery.util.CustPagerTransformer;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by abook23 on 2015/10/21.
 * <p>
 * 2016年10月28日 10:35:57  v2.0
 */
public class ImageInfoActivity extends BaseFragmentActivity {

    public static String PATHS = "paths";
    public static String POSITION = "position";
    public static String CHECK_MAX = "check_max";
    public static String CHECK_COUNT = "check_count";
    public static String CHECK_PATHS = "check_paths";
    public static String SHOW_CHECKBOX = "showCheckBox";
    public static String PREVIEW = "preview";

    public static String DATA = "data";//返回的数据

    private ArrayList<String> paths;
    private ArrayList<String> paths_check;
    private ViewPager viewPager;
    private FragmentGallery fragmentGallery;
    private List<FragmentImage> fragments = new ArrayList<>();
    private int check_max;
    private int check_count;
    private TextView tvCheckCount;

    public static void start(Context context, String url) {
        ArrayList<String> paths = new ArrayList<>();
        paths.add(url);
        start(context, 0, paths);
    }

    /**
     * 图片预览 查看
     *
     * @param context
     * @param urls
     */
    public static void start(Context context, int position, ArrayList<String> urls) {
        Intent intent = new Intent(context, ImageInfoActivity.class);
        intent.putExtra(ImageInfoActivity.POSITION, position);
        intent.putStringArrayListExtra(ImageInfoActivity.PATHS, urls);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_activity_image_info);
        //  addTitleBarFragment();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); // 设置全屏

        tvCheckCount = (TextView) findViewById(R.id.gb_tv_check_count);
        TextView tvCheckMax = (TextView) findViewById(R.id.gb_tv_check_max);

        Intent intent = getIntent();
        check_max = intent.getIntExtra(CHECK_MAX, 5);
        check_count = intent.getIntExtra(CHECK_COUNT, 0);
        boolean showCheckBox = intent.getBooleanExtra(SHOW_CHECKBOX, false);
        boolean preview = intent.getBooleanExtra(PREVIEW, true);
        paths_check = intent.getStringArrayListExtra(CHECK_PATHS);
        paths = intent.getStringArrayListExtra(PATHS);
        int position = getIntent().getIntExtra(POSITION, 0);//显示页
        if (preview)
            if (paths_check == null) {
                paths_check = new ArrayList<>();
                paths_check.addAll(paths);
                tvCheckMax.setVisibility(View.GONE);
                tvCheckCount.setVisibility(View.GONE);
            }
        if (paths == null) {
            paths = new ArrayList<>();
        }
        if (paths_check == null)
            paths_check = new ArrayList<>();

        if (!showCheckBox) {
            tvCheckMax.setVisibility(View.GONE);
            tvCheckCount.setVisibility(View.GONE);
        }
        tvCheckCount.setText(check_count + "");
        tvCheckMax.setText("/" + check_max);

        viewPager = (ViewPager) findViewById(R.id.gb_photo_viewpager);
        viewPager.setPageTransformer(false, new CustPagerTransformer(this));
        // 2. viewPager添加adapter
        for (int i = 0; i < paths.size(); i++) {
            // 预先准备10个fragment
            fragments.add(FragmentImage.newInstance(showCheckBox));
        }
        viewPager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(final int position) {
                String path = paths.get(position);
                final FragmentImage fragment = fragments.get(position);
                fragment.bindData(path);
                int no = paths_check.indexOf(path)+1;
                fragment.setCheck(no > 0);
                fragment.setNo(no);
                fragment.setOnFragmentImageListener(new FragmentImage.OnFragmentImageListener() {
                    @Override
                    public void onUserVisibleHint(boolean isVisibleToUser) {
                        if (isVisibleToUser) {
                            String path = paths.get(position);
                            fragment.setNo(paths_check.indexOf(path)+1);
                            fragmentGallery.setPosition(paths_check.indexOf(path));
                        }
                    }

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        String path = paths.get(position);
                        if (isChecked && check_count >= check_max) {
                            buttonView.setChecked(false);
                            return;
                        }
                        if (isChecked) {
                            paths_check.add(path);
                            check_count++;
                        } else {
                            paths_check.remove(path);
                            check_count--;
                        }
                        fragment.setNo(paths_check.indexOf(path)+1);
                        fragmentGallery.setData(paths_check);
                        tvCheckCount.setText(check_count + "");
                    }
                });
                return fragment;
            }

            @Override
            public int getCount() {
                return paths.size();
            }
        });
        viewPager.setCurrentItem(position);


        fragmentGallery = FragmentGallery.newInstance(false);
        getSupportFragmentManager().beginTransaction().add(R.id.gb_fl_gallery, fragmentGallery).commit();
        fragmentGallery.setImageViewWithe(35);
        fragmentGallery.setPosition(position);
        fragmentGallery.setOnClickListener(new OnItemViewListener() {
            @Override
            public void onItemViewClick(View parentView, View childView, int position) {
                if (childView instanceof ImageView) {
                    int index = paths.indexOf(paths_check.get(position));
                    viewPager.setCurrentItem(index);
                    fragmentGallery.setPosition(position);
                }
            }
        });
        fragmentGallery.setData(paths_check);

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent();
            intent.putStringArrayListExtra(DATA, paths_check);
            setResult(RESULT_OK, intent);
        }
        return super.onKeyDown(keyCode, event);
    }
}
