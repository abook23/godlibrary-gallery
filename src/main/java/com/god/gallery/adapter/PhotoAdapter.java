package com.god.gallery.adapter;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.god.gallery.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by abook23 on 2015/10/20.
 */
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {
    public static final String KEY_PHOTO = "PHOTO";
    public static final String KEY_NAME = "NAME";
    public static final String KEY_COUNT = "COUNT";
    private Context context;
    private List<ArrayMap<String,String>> data;
    private HashMap<String, ArrayList<String>> checkImages;
    private AdapterView.OnItemClickListener mOnItemClickListener;

    public PhotoAdapter(Context context) {
        this.context = context;
    }

    public void setDate(List<ArrayMap<String,String>> data) {
        this.data = data;
    }

    public String getPhotoName(int position) {
        return data.get(position).get(KEY_NAME);
    }

    public void setCheckImages(HashMap<String, ArrayList<String>> checkImages) {
        this.checkImages = checkImages;
    }

    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_item_list_1, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PhotoViewHolder holder, int position) {

        ArrayMap<String,String> itemData = data.get(position);
        String name;
        switch (itemData.get(KEY_NAME)) {
            case "Camera":
                name = "相册";
                break;
            case "Screenshots":
                name = "截屏";

                break;
            default:
                name = itemData.get(KEY_NAME);
                break;
        }

        holder.tv1.setText(name);
        holder.tv2.setText(itemData.get(KEY_COUNT));
        Glide.with(context).load(itemData.get(KEY_PHOTO)).into(holder.iv1);

        holder.linearLayout.removeAllViews();
        if (checkImages != null && checkImages.size() > 0) {
            ArrayList<String> images = checkImages.get(itemData.get(KEY_NAME));
            if (images != null)
                for (String path : images) {
                    View view = getView(path, 25, 25);
                    holder.linearLayout.addView(view);
                }
        }
        //holder.itemView.setTag();
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    public View getView(String path, float width, float height) {
        View convertView = View.inflate(context, R.layout.gallery_item_image_check, null);
        CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);
        checkBox.setVisibility(View.GONE);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView1);

        FrameLayout frameLayout = (FrameLayout) convertView.findViewById(R.id.gb_fl_check_image);
        ViewGroup.LayoutParams layoutParams = frameLayout.getLayoutParams();
        layoutParams.width = dp2px(context, width);
        layoutParams.height = dp2px(context, height);
        frameLayout.setLayoutParams(layoutParams);
        //imageFetcher.loadImage(path, imageView);
        Glide.with(context).load(path).into(imageView);
        return convertView;
    }

    /**
     * dp和像素转换
     */
    private int dp2px(Context context, float dipValue) {
        float m = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * m + 0.5f);
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public class PhotoViewHolder extends RecyclerView.ViewHolder {
        public TextView tv1;
        public TextView tv2;
        public ImageView iv1;
        public LinearLayout linearLayout;

        public PhotoViewHolder(View itemView) {
            super(itemView);
            iv1 = (ImageView) itemView.findViewById(R.id.imageView1);
            tv1 = (TextView) itemView.findViewById(R.id.textView1);
            tv2 = (TextView) itemView.findViewById(R.id.textView2);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.gb_df_p_select);
            if (mOnItemClickListener != null)
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOnItemClickListener.onItemClick(null, v, getAdapterPosition(), getItemId());
                    }
                });
        }
    }

}
