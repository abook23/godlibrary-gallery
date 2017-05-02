package com.god.gallery.fagment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.god.gallery.PinchImageView;
import com.god.gallery.R;

/**
 * Created by My on 2016/10/25.
 */

public class FragmentImage extends Fragment {
    private String path;
    private OnFragmentImageListener mListener;
    private boolean imageViewCheck;
    private boolean checkBoxState;
    private boolean isTouch;
    private CheckBox checkBox;
    private TextView mTvNo;
    private PinchImageView mImageView;

    public int height, width;
    private int no = 0;

    public FragmentImage() {
    }

    public static FragmentImage newInstance(boolean showCheckBox) {
        FragmentImage fragment = new FragmentImage();
        fragment.checkBoxState = showCheckBox;
        return fragment;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (mListener != null) {
            mListener.onUserVisibleHint(isVisibleToUser);
        }
    }

    public void setOnFragmentImageListener(OnFragmentImageListener listener) {
        this.mListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gallery_fragment_image, container, false);
        mImageView = (PinchImageView) view.findViewById(R.id.imageView1);
        mTvNo = (TextView) view.findViewById(R.id.gl_fl_no);
        mTvNo.setText(no == 0 ? null : no + "");
        if (height == 0) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            height = displayMetrics.heightPixels;
            width = displayMetrics.widthPixels;
        }

        Glide.with(this).load(path).diskCacheStrategy(DiskCacheStrategy.ALL).override(width / 2, height / 2).into(mImageView);
        checkBox = (CheckBox) view.findViewById(R.id.checkBox);
        checkBox.setChecked(imageViewCheck);
        if (checkBoxState) {
            checkBox.setVisibility(View.VISIBLE);
        } else {
            checkBox.setVisibility(View.GONE);
        }
        if (mListener != null) {
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isTouch) {
                        mListener.onCheckedChanged(buttonView, isChecked);
                        isTouch = false;
                    }
                }
            });
            checkBox.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    isTouch = true;
                    return false;
                }
            });
        }
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mListener = null;
//        if (mImageView != null) {
//            // Cancel any pending image work
//            mImageView.setImageDrawable(null);
//        }
    }

    public void bindData(String path) {
        this.path = path;
    }

    public void setCheck(boolean check) {
        this.imageViewCheck = check;
    }

    public void setNo(int no) {
        this.no = no;
        if (mTvNo != null)
            mTvNo.setText(no == 0 ? null : no + "");
    }

    public interface OnFragmentImageListener {
        void onUserVisibleHint(boolean isVisibleToUser);

        void onCheckedChanged(CompoundButton buttonView, boolean isChecked);
    }


}
