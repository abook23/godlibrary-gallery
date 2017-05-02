package com.god.gallery.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.god.gallery.PhotoActivity;
import com.god.gallery.R;

import java.util.List;

/**
 * Created by abook23 on 2015/10/19.
 */
public class GridViewAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private List<String> files;
    private List<String> list_checkPath;
    private OnCheckBoxOnListener onCheckBoxOnListener;
    private int on;
    public int numColumns;
    public Context context;

    public GridViewAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    public void setData(List<String> files) {
        this.files = files;
    }


    public void setCheckImage(List<String> list) {
        list_checkPath = list;
    }

    @Override
    public int getCount() {
        return files == null ? 0 : files.size();
    }

    @Override
    public Object getItem(int position) {
        return files.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View v, ViewGroup parent) {
        final ViewHolder holder;
        if (v == null) {
            holder = new ViewHolder();
            v = inflater.inflate(R.layout.gallery_item_gridview_1, parent, false);
            holder.iv = (ImageView) v.findViewById(R.id.gridView_iv);
            holder.cb = (CheckBox) v.findViewById(R.id.gridView_cb);
            holder.tv = (TextView) v.findViewById(R.id.select_count_text);
            holder.fl = (FrameLayout) v.findViewById(R.id.gridView_fl_cb);
            if (PhotoActivity.checkMax == 1) {
                holder.cb.setVisibility(View.GONE);
            }
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        String filePath = files.get(position);
        /**
         * 给imageView 赋 一个标签
         */
        //holder.iv.setTag(filePath);
        holder.cb.setTag("cb" + position);

        //ImageLoader2.bindWithTag(parent, holder.iv, filePath, options);
        if (numColumns > 0)
            Glide.with(context).load(filePath).into(holder.iv);

//        holder.cb.setOnClickListener(new View.OnClickListener() {//点击范围太小了
//            @Override
//            public void onClick(View v) {
//                if (onCheckBoxOnListener != null)
//                    onCheckBoxOnListener.onCheckBox(holder.cb, position);
//            }
//        });
        holder.fl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.cb.setChecked(!holder.cb.isChecked());
                if (onCheckBoxOnListener != null)
                    onCheckBoxOnListener.onCheckBox(holder.cb, position);
            }
        });
        if (list_checkPath != null && list_checkPath.contains(files.get(position))) {
            on = list_checkPath.indexOf(files.get(position)) + 1;
            holder.tv.setText(String.valueOf(on));
            holder.cb.setChecked(true);
        } else {
            holder.tv.setText(null);
            holder.cb.setChecked(false);
        }
        return v;
    }

    public void setNumColumns(int numColumns) {
        this.numColumns = numColumns;
    }

    public class ViewHolder {
        public ImageView iv;
        public CheckBox cb;
        public TextView tv;
        public FrameLayout fl;
    }

    public void setOnCheckBoxOnListener(OnCheckBoxOnListener onCheckBoxOnListener) {
        this.onCheckBoxOnListener = onCheckBoxOnListener;
    }

    public interface OnCheckBoxOnListener {
        void onCheckBox(CheckBox checkBox, int position);
    }
}
