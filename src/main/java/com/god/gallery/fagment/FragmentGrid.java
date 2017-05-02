package com.god.gallery.fagment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.CheckBox;

import com.god.gallery.ImageInfoActivity;
import com.god.gallery.PhotoActivity;
import com.god.gallery.R;
import com.god.gallery.adapter.CheckImageAdapter;
import com.god.gallery.util.Utils;

import java.util.ArrayList;

import static com.god.gallery.SelectImagesActivity.folderName;

public class FragmentGrid extends Fragment {
    private static final String PATHS = "PATHS";

    private ArrayList<String> paths;
    private RecyclerView mRecyclerView;
    private Callback callback;
    private Context context;
    private int mWidth;
    private int mSpacing;
    private long onClickTime;
    private int oldPosition;
    private CheckImageAdapter checkImageAdapter;
    private GridLayoutManager mGridLayoutManager;

    public FragmentGrid() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static FragmentGrid newInstance(ArrayList<String> paths) {
        FragmentGrid fragment = new FragmentGrid();
        Bundle args = new Bundle();
        args.putStringArrayList(PATHS, paths);
        fragment.setArguments(args);
        return fragment;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            paths = getArguments().getStringArrayList(PATHS);
        }
        mWidth = getResources().getDimensionPixelSize(R.dimen.image_width_size);
        mSpacing = getResources().getDimensionPixelSize(R.dimen.image_spacing_size);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.gallery_fragment_grid, container, false);
        context = getContext();
        mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);

        checkImageAdapter = new CheckImageAdapter();
        checkImageAdapter.setData(paths);
        mGridLayoutManager = new GridLayoutManager(context, 3);
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mRecyclerView.setAdapter(checkImageAdapter);

        setListener();

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        onClickTime = 0;
        //checkImageAdapter.setCheckImage(PhotoActivity.getFolderCheck(folderName));
        checkImageAdapter.setCheckImage(PhotoActivity.getAllCheckImages());
        checkImageAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private long delay = 350;

    private void setListener() {
        mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (checkImageAdapter.numColumns == 0) {
                    int numColumns = (int) Math.floor(mRecyclerView.getWidth() / (mWidth + mSpacing));
                    if (numColumns > 0) {
                        //int width = (gridView.getWidth() / numColumns) - mSpacing;
                        mGridLayoutManager.setSpanCount(numColumns);
                        checkImageAdapter.setNumColumns(numColumns);
                        if (Utils.hasJellyBean())
                            mRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        else
                            mRecyclerView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                }
            }
        });
        checkImageAdapter.setItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // if (position == oldPosition && System.currentTimeMillis() - onClickTime < delay) {//双击
                Intent intent = new Intent();
                intent.setClass(context, ImageInfoActivity.class);
                intent.putStringArrayListExtra(ImageInfoActivity.PATHS, paths);
                intent.putStringArrayListExtra(ImageInfoActivity.CHECK_PATHS, PhotoActivity.getFolderCheck(folderName));
                //intent.putStringArrayListExtra(ImageInfoActivity.CHECK_PATHS, PhotoActivity.getAllCheckImages());
                intent.putExtra(ImageInfoActivity.CHECK_MAX, PhotoActivity.checkMax);
                intent.putExtra(ImageInfoActivity.CHECK_COUNT, PhotoActivity.checkCount);
                intent.putExtra(ImageInfoActivity.SHOW_CHECKBOX, true);
                intent.putExtra(ImageInfoActivity.POSITION, position);
                startActivityForResult(intent, 3);
            }
        });
        checkImageAdapter.setOnCheckBoxOnListener(new CheckImageAdapter.OnCheckBoxOnListener() {
            @Override
            public void onCheckBox(CheckBox checkBox, int position) {
                if (callback != null) {
                    if (checkBox.isChecked()) {
                        boolean b = callback.onImageSelected(paths.get(position));
                        checkBox.setChecked(b);
                    } else {
                        callback.onImageUnselected(paths.get(position));
                    }
                    checkImageAdapter.setCheckImage(PhotoActivity.getAllCheckImages());
                    //checkImageAdapter.notifyItemChanged(position);
                    checkImageAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }

    /**
     * 回调接口
     */
    public interface Callback {

        boolean onImageSelected(String path);

        void onImageUnselected(String path);
    }
}
