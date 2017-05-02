package com.god.gallery.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.god.gallery.R;

import java.util.List;

/**
 * Created by My on 2017/4/27.
 */

public class CheckImageAdapter extends RecyclerView.Adapter<CheckImageAdapter.CheckViewHolder> {

    private List<String> files;
    private List<String> list_checkPath;
    private OnCheckBoxOnListener onCheckBoxOnListener;
    private AdapterView.OnItemClickListener mItemClickListener;
    private int on;
    public int numColumns;
    public Context mContext;

    public void setData(List<String> files) {
        this.files = files;
    }


    public void setCheckImage(List<String> list) {
        list_checkPath = list;
    }

    public void setNumColumns(int numColumns) {
        this.numColumns = numColumns;
    }

    @Override
    public CheckViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_item_gridview_1, parent,false);
        return new CheckViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CheckViewHolder holder, final int position) {
        String filePath = files.get(position);
        /**
         * 给imageView 赋 一个标签
         */
        //holder.iv.setTag(filePath);
        holder.cb.setTag("cb" + position);
        //if (numColumns > 0)
        Glide.with(mContext).load(filePath).into(holder.iv);
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
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mItemClickListener.onItemClick(null, v, holder.getAdapterPosition(), 0);
            }
        });
    }

    @Override
    public int getItemCount() {
        return files == null ? 0 : files.size();
    }

    public void setOnCheckBoxOnListener(OnCheckBoxOnListener onCheckBoxOnListener) {
        this.onCheckBoxOnListener = onCheckBoxOnListener;
    }

    public void setItemClickListener(AdapterView.OnItemClickListener listener) {
        this.mItemClickListener = listener;
    }

    public interface OnCheckBoxOnListener {
        void onCheckBox(CheckBox checkBox, int position);
    }

    class CheckViewHolder extends RecyclerView.ViewHolder {
        private ImageView iv;
        private CheckBox cb;
        private TextView tv;
        private FrameLayout fl;

        public CheckViewHolder(View itemView) {
            super(itemView);
            iv = (ImageView) itemView.findViewById(R.id.gridView_iv);
            cb = (CheckBox) itemView.findViewById(R.id.gridView_cb);
            tv = (TextView) itemView.findViewById(R.id.select_count_text);
            fl = (FrameLayout) itemView.findViewById(R.id.gridView_fl_cb);
        }
    }
}
