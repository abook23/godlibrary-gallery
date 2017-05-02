package com.god.gallery.fagment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.god.gallery.OnItemViewListener;
import com.god.gallery.R;
import com.god.gallery.util.CameraUtil;
import com.god.gallery.util.PermissionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

public class FragmentGallery extends Fragment implements AdapterView.OnItemClickListener {

    private static final String TAG = FragmentGallery.class.toString();
    private RecyclerView mRecyclerView;
    private static final int RESULT_CAMERA = 100;

    private static final int REQUEST_CONTACTS = 1;
    private static String[] PERMISSIONS_CONTACT = {Manifest.permission.CAMERA};

    private OnItemViewListener mListener;
    private OnCameraListener cameraListener;
    private List<String> data = new ArrayList<>();
    private List<String> checkPath = new ArrayList<>();
    private int mWidth;
    private boolean mCheck;
    private int position = -1;
    private GalleryAdapter mGalleryAdapter;
    private boolean isCamera;

    public FragmentGallery() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static FragmentGallery newInstance(boolean showCheckBox) {
        FragmentGallery fragment = new FragmentGallery();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        fragment.mCheck = showCheckBox;
        return fragment;
    }

    public void setCamera(OnCameraListener listener) {
        this.cameraListener = listener;
        this.isCamera = listener != null;
    }

    public void setData(List<String> list) {
        setData(list, list);
    }

    public void addView(String url, boolean check) {
        addData(url, check);
    }

    public void addData(String url, boolean check) {
        addData(0, url, check);
    }

    public void addData(int index, String url, boolean check) {
        data.add(index, url);
        if (check)
            checkPath.add(url);
        else
            checkPath.remove(url);
    }

    public void setData(List<String> list, List<String> checkPath) {
        this.data.clear();
        this.checkPath.clear();

        this.data.addAll(list);
        this.checkPath.addAll(checkPath);

        if (mGalleryAdapter!=null) {
            mGalleryAdapter.setData(data);
            mGalleryAdapter.setCheckImage(this.checkPath);
            mGalleryAdapter.notifyDataSetChanged();
        }
    }


    public void setOnClickListener(OnItemViewListener listener) {
        this.mListener = listener;
    }

    public void setImageViewWithe(int width_dp) {
        this.mWidth = width_dp;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gallery_fragment_gallery, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.gallery_recyclerView);
        mGalleryAdapter = new GalleryAdapter(getContext());
        mGalleryAdapter.showCamera(isCamera);
        mGalleryAdapter.setData(data);
        mGalleryAdapter.setCheckImage(this.checkPath);
        mGalleryAdapter.setOnItemClickListener(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mRecyclerView.setAdapter(mGalleryAdapter);
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (cameraListener != null && requestCode == RESULT_CAMERA) {
                String path = CameraUtil.newInstance().path;
                MediaScannerConnection.scanFile(getContext(), new String[]{path}, null, null);
                addData(0, path, false);
                cameraListener.onCameraResult(path);
            }
        } else {
            CameraUtil.newInstance().delete();
        }
    }

    private void requestContactsPermissions() {
        if (PermissionUtil.requestPermission(this, PERMISSIONS_CONTACT, REQUEST_CONTACTS)) {
            //已经获取权限
            CameraUtil.newInstance().startCamera(this, RESULT_CAMERA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CONTACTS) {
            if (PermissionUtil.verifyPermissions(getContext(), PERMISSIONS_CONTACT, grantResults)) {
                Log.i(TAG, "已经全部授权");
                CameraUtil.newInstance().startCamera(this, RESULT_CAMERA);
            } else {
                Log.i(TAG, "缺少必要的权限");
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setPosition(int position) {
        this.position = position;
        if (position > -1 && mGalleryAdapter!=null)
           mGalleryAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (isCamera && position == 0) {
            requestContactsPermissions();
        } else {
            if (isCamera)
                position = position - 1;
            mListener.onItemViewClick(parent, view, position);
            mGalleryAdapter.notifyDataSetChanged();

        }

    }

    public interface OnCameraListener {
        void onCameraResult(String url);
    }

    /**
     * dp和像素转换
     */
    private int dp2px(Context context, float dipValue) {
        float m = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * m + 0.5f);
    }

    public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryHolder> {

        private List<Object> urls = new ArrayList<>();
        private Context mContext;
        private AdapterView.OnItemClickListener mItemClickListener;
        private boolean isCamera;
        private List<String> list_checkPath;


        public GalleryAdapter(Context context) {
            this.mContext = context;
        }

        public void setData(List<String> data) {
            urls.clear();
            if (isCamera)
                urls.add(0, R.mipmap.camera2);
            if (data != null)
                for (String s : data) {
                    urls.add(s);
                }
        }

        public void setCheckImage(List<String> list) {
            list_checkPath = list;
        }

        public void showCamera(boolean b) {
            isCamera = b;
        }

        public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
            this.mItemClickListener = listener;
        }

        @Override
        public GalleryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_item_image_check, parent, false);
            return new GalleryHolder(view);
        }

        @Override
        public void onBindViewHolder(GalleryHolder holder, int position) {
            if (isCamera && position == 0) {
                Glide.with(mContext).load(urls.get(position)).into(holder.imageView);
                holder.checkBox.setVisibility(View.GONE);
                holder.order.setText(null);
            } else {
                CheckBox checkBox = holder.checkBox;
                if (mCheck) {
                    checkBox.setVisibility(View.VISIBLE);
                } else {
                    checkBox.setVisibility(View.GONE);
                }
                if (mWidth > 0) {
                    FrameLayout frameLayout = holder.frameLayout;
                    ViewGroup.LayoutParams layoutParams = frameLayout.getLayoutParams();
                    layoutParams.width = dp2px(getContext(), mWidth);
                    layoutParams.height = dp2px(getContext(), mWidth);
                    frameLayout.setLayoutParams(layoutParams);
                    if (FragmentGallery.this.position == position) {
                        frameLayout.setPadding(2, 2, 2, 2);
                        frameLayout.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    }else {
                        frameLayout.setPadding(2, 2, 2, 2);
                        frameLayout.setBackgroundColor(getResources().getColor(R.color.colorCheckButton));
                    }
                }
                String path = (String) urls.get(position);
                if (mCheck && list_checkPath != null && list_checkPath.contains(path)) {
                    int on = list_checkPath.indexOf(path) + 1;
                    holder.order.setText(String.valueOf(on));
                } else {
                    holder.order.setText(null);
                }

                DrawableRequestBuilder<String> drawableResource = Glide.with(getContext()).load(path);
                if (mWidth > 0) {
                    drawableResource.override(dp2px(getContext(), mWidth), dp2px(getContext(), mWidth));
                }
                if (position != -1)
                    drawableResource.diskCacheStrategy(DiskCacheStrategy.ALL);
                drawableResource.into(holder.imageView);
                checkBox.setChecked(checkPath.contains(path));
            }
        }

        @Override
        public int getItemCount() {
            return urls == null ? 0 : urls.size();
        }

        class GalleryHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            public CheckBox checkBox;
            public ImageView imageView;
            public FrameLayout frameLayout;
            public TextView order;

            public GalleryHolder(View itemView) {
                super(itemView);
                checkBox = (CheckBox) itemView.findViewById(R.id.checkBox);
                imageView = (ImageView) itemView.findViewById(R.id.imageView1);
                order = (TextView) itemView.findViewById(R.id.tv_order);
                frameLayout = (FrameLayout) itemView.findViewById(R.id.gb_fl_check_image);
                if (mItemClickListener != null) {
                    imageView.setOnClickListener(this);
                    checkBox.setOnClickListener(this);
                }
            }

            @Override
            public void onClick(View v) {
                if (v == imageView) {
                    onImageViewClick(v, getAdapterPosition());
                } else if (v == checkBox) {
                    onCheckBoxClick((CheckBox) v, getAdapterPosition());
                }
            }

            private void onCheckBoxClick(CheckBox v, int position) {
                if (v.isChecked())
                    checkPath.add((String) urls.get(position));
                else
                    checkPath.remove(urls.get(position));
                mItemClickListener.onItemClick(null, v, position, 0);
            }

            private void onImageViewClick(View v, int position) {
                mItemClickListener.onItemClick(null, v, position, 0);
            }
        }
    }
}
