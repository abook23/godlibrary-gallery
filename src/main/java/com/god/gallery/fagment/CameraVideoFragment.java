
package com.god.gallery.fagment;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.god.gallery.R;
import com.god.gallery.util.PermissionUtil;
import com.god.gallery.widget.VideoProgress;
import com.sprylab.android.widget.TextureVideoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class CameraVideoFragment extends Fragment implements Callback, OnClickListener {
    private static String[] PERMISSIONS_CAMERA = new String[]{"android.permission.CAMERA", "android.permission.RECORD_AUDIO", "android.permission.WRITE_EXTERNAL_STORAGE"};
    private static final int REQUEST_CONTACTS = 1;
    private static String TAG = CameraVideoFragment.class.getSimpleName();
    private String PHOTO_PATH;
    private String VIDEO_PATH;
    ImageView cameraTransform;
    private Context context;
    ImageView iVvHd;
    private boolean isPlayVideo;
    private Camera mCamera;
    ImageView mCameraBack;
    private int mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    ImageView mCameraYes;
    private CountDownTimer mCountDownTimer;
    private CameraVideoFragment.Definition mDefinition;
    private CameraVideoFragment.DirectionOrientationListener mDirectionOrientationListener;
    private CameraVideoFragment.OnCameraVideoListener mListener;
    private CameraVideoFragment.OnCameraVideoTouchListener mTouchListener;
    private int mOrientation;
    private SurfaceHolder mSurfaceHolder;
    TextView mTvSecond;
    private int mVideoMaxDuration = 10000;
    private long mVideoMaxZie = 50 * 1024 * 1024;
    VideoProgress mVideoProgress;
    private float mVideoRatio;
    TextureVideoView mVideoView;
    private int pictureSizeHeight;
    private int pictureSizeWidth;
    private List<Size> pictureSizes;
    private List<Size> previewSizes;
    private MediaRecorder recorder;
    SurfaceView surfaceView;

    public static CameraVideoFragment newInstance() {
        CameraVideoFragment cameraVideoFragment = new CameraVideoFragment();
        cameraVideoFragment.setArguments(new Bundle());
        return cameraVideoFragment;
    }


    public void setOnCameraVideoListener(CameraVideoFragment.OnCameraVideoListener listener) {
        this.mListener = listener;
    }

    public void setOnCameraVideoTouchListener(CameraVideoFragment.OnCameraVideoTouchListener listener) {
        this.mTouchListener = listener;
    }

    public CameraVideoFragment() {
        this.mDefinition = CameraVideoFragment.Definition.SD;
        this.mCameraId = 0;
        this.mOrientation = 0;
        this.mVideoRatio = 1;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
        View view = inflater.inflate(R.layout.fragment_video, viewGroup, false);
        this.initView(view);
        this.context = this.getContext();
        this.mVideoProgress.setOnCameraVideoListener(new ClickListener());
        if (this.mDefinition == CameraVideoFragment.Definition.HD) {
            this.iVvHd.setBackgroundResource(R.mipmap.gb_gallery_hd);
        } else {
            this.iVvHd.setBackgroundResource(R.mipmap.gb_gallery_hd_off);
        }

        SurfaceHolder surfaceHolder = this.surfaceView.getHolder();
        surfaceHolder.setKeepScreenOn(true);
        surfaceHolder.addCallback(this);
        initCountDownTimer();
        this.mDirectionOrientationListener = new CameraVideoFragment.DirectionOrientationListener(this.context, 3);
        if (this.mDirectionOrientationListener.canDetectOrientation()) {
            this.mDirectionOrientationListener.enable();
            return view;
        } else {
            Log.d("chengcj1", "Can't Detect Orientation");
            return view;
        }
    }

    private void initView(View view) {
        this.surfaceView = (SurfaceView) view.findViewById(R.id.surfaceView_video);
        this.mVideoProgress = (VideoProgress) view.findViewById(R.id.videoProgress);
        this.mTvSecond = (TextView) view.findViewById(R.id.tv_second);
        this.iVvHd = (ImageView) view.findViewById(R.id.iv_hd);
        this.mCameraYes = (ImageView) view.findViewById(R.id.camera_yes);
        this.mCameraBack = (ImageView) view.findViewById(R.id.camera_back);
        this.mVideoView = (TextureVideoView) view.findViewById(R.id.videoView);
        this.cameraTransform = (ImageView) view.findViewById(R.id.iv_camera_transform);
        this.surfaceView.setOnClickListener(this);
        this.mTvSecond.setOnClickListener(this);
        this.iVvHd.setOnClickListener(this);
        this.mCameraYes.setOnClickListener(this);
        this.mCameraBack.setOnClickListener(this);
        this.cameraTransform.setOnClickListener(this);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        this.mSurfaceHolder = surfaceHolder;
        if (this.mCamera == null) {
            this.requestContactsPermissions();
        }
    }

    private void requestContactsPermissions() {
        if (PermissionUtil.requestPermission(this, PERMISSIONS_CAMERA, REQUEST_CONTACTS)) {
            this.openCamera(this.mCameraId);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == 1) {
            if (!PermissionUtil.verifyPermissions(this.context, PERMISSIONS_CAMERA, results)) {
                Log.i(TAG, "缺少必要的权限");
                Toast.makeText(this.context, "缺少 相应权限", Toast.LENGTH_SHORT).show();
                this.getActivity().finish();
                return;
            }
            Log.i(TAG, "已经全部授权");
            this.openCamera(this.mCameraId);
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        this.mSurfaceHolder = surfaceHolder;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        this.mSurfaceHolder = null;
        if (this.recorder != null) {
            this.recorder.release();
            this.recorder = null;
        }

        if (this.mCamera != null) {
            this.mCamera.stopPreview();
            this.mCamera.release();
            this.mCamera = null;
        }

    }

    /**
     * 打开相机
     *
     * @param cameraId 相机id
     */
    private void openCamera(int cameraId) {
        if (this.mCamera != null) {
            this.mCamera.stopPreview();
            this.mCamera.release();
            this.mCamera = null;
        }

        this.mCamera = Camera.open(cameraId);
        this.mCamera.setDisplayOrientation(90);
        if (this.pictureSizeHeight > 0) {
            this.setParameters(this.pictureSizeWidth, this.pictureSizeHeight);
        } else if (this.mDefinition == CameraVideoFragment.Definition.SD) {
            this.setParameters(1280, 960);
        } else if (this.mDefinition == CameraVideoFragment.Definition.HD) {
            this.setParameters(-1, -1);
        }

        try {
            this.mCamera.setPreviewDisplay(this.mSurfaceHolder);
            this.mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化录像机
     */
    private void initVideo() {
        if (this.recorder == null) {
            this.recorder = new MediaRecorder();
        }

        if (this.mCamera == null) {
            this.mCamera = Camera.open(0);
            this.mCamera.setDisplayOrientation(90);
        }

        this.mCamera.unlock();
        this.recorder.setCamera(this.mCamera);
        this.recorder.setAudioSource(1);
        this.recorder.setVideoSource(1);
        this.recorder.setOutputFormat(2);
        this.recorder.setVideoEncoder(2);
        this.recorder.setAudioEncoder(3);
        this.recorder.setVideoFrameRate(25);
        Size size;
        if (this.mDefinition == CameraVideoFragment.Definition.HD) {
            size = this.getDefaultSize(this.pictureSizes, 720);
            this.recorder.setVideoSize(size.width, size.height);
        } else {
            size = this.getDefaultSize(this.pictureSizes, 480);
            this.recorder.setVideoSize(size.width, size.height);
        }

        this.recorder.setMaxDuration(this.mVideoMaxDuration);
        this.recorder.setMaxFileSize(this.mVideoMaxZie);
        this.recorder.setVideoEncodingBitRate((int) (this.mVideoRatio * 1024.0F * 1024.0F));
        this.recorder.setAudioChannels(2);
        this.recorder.setAudioEncodingBitRate(128);
        this.mOrientation += 90;
        if (this.mOrientation > 270) {
            this.mOrientation = 0;
        }

        this.recorder.setOrientationHint(this.mOrientation);
        String var3 = System.currentTimeMillis() + ".mp4";
        this.VIDEO_PATH = this.getDiskDir(this.context, "");
        File file = new File(this.VIDEO_PATH);
        if (!file.exists()) {
            file.mkdirs();
        }

        file = new File(this.VIDEO_PATH, var3);
        this.recorder.setOutputFile(file.getAbsolutePath());
        this.recorder.setPreviewDisplay(this.mSurfaceHolder.getSurface());
        this.VIDEO_PATH = file.getAbsolutePath();
    }

    /**
     * 播放视频
     */
    private void startVideo() {
        try {
            this.recorder.prepare();
            this.recorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 聚焦
     *
     * @param camera
     */
    private void cameraAutoFocus(Camera camera) {
        camera.autoFocus(new AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean var1, Camera var2) {
                mCamera.cancelAutoFocus();
            }
        });
    }


    private void delFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }

    }

    private int dp2px(Context context, float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5F);
    }

    /**
     * 比例
     *
     * @param size
     * @param rate 16:9 1.777777
     * @return
     */
    private boolean equalRate(Size size, float rate) {
        return (double) Math.abs((float) size.width / (float) size.height - rate) <= 0.2D;
    }

    private Size getDefaultSize(List<Size> sizes, int height) {
        int i = 0;
        for (Iterator var4 = sizes.iterator(); var4.hasNext(); ++i) {
            Size size = (Size) var4.next();
            if (size.height >= height && this.equalRate(size, 1.77F)) {
                Log.i(TAG, "最终设置尺寸:w = " + size.width + "h = " + size.height);
                break;
            }
        }
        return i == sizes.size() ? sizes.get(i - 1) : sizes.get(i);
    }

    private String getDiskDir(Context context, String dir) {
        String path;
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) && !Environment.isExternalStorageRemovable()) {
            path = context.getCacheDir().getPath();
        } else {
            path = Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        return dir == null ? path : path + File.separator + dir;
    }

    private Size getPictureSize(List<Size> sizes, int height) {
        int i = 0;
        for (Iterator var4 = sizes.iterator(); var4.hasNext(); ++i) {
            Size var5 = (Size) var4.next();
            if (var5.height > height && this.equalRate(var5, 1.77F)) {
                Log.i(TAG, "最终设置图片尺寸:w = " + var5.width + "h = " + var5.height);
                break;
            }
        }
        return i == sizes.size() ? sizes.get(i - 1) : sizes.get(i);
    }

    private Size getPreviewSize(List<Size> sizes, int height) {
        int var3 = 0;

        for (Iterator var4 = sizes.iterator(); var4.hasNext(); ++var3) {
            Size var5 = (Size) var4.next();
            if (var5.height > height && this.equalRate(var5, 1.77F)) {
                Log.i(TAG, "最终设置预览尺寸:w = " + var5.width + "h = " + var5.height);
                break;
            }
        }

        return var3 == sizes.size() ? sizes.get(var3 - 1) : sizes.get(var3);
    }


    /**
     * 设置相机 图片大小
     *
     * @param width  宽
     * @param height 高
     */
    private void setParameters(int width, int height) {
        Parameters parameters = this.mCamera.getParameters();
        this.pictureSizes = parameters.getSupportedPictureSizes();
        this.previewSizes = parameters.getSupportedPreviewSizes();
        Collections.reverse(this.pictureSizes);
        Collections.reverse(this.previewSizes);
        Size size;
        if (height > 0) {
            size = this.getPictureSize(this.pictureSizes, height);
        } else {
            size = this.pictureSizes.get(this.pictureSizes.size() - 1);
        }

        parameters.setPictureSize(size.width, size.height);
        size = this.getPreviewSize(this.previewSizes, this.surfaceView.getWidth());
        parameters.setPreviewSize(size.width, size.height);
        parameters.setFocusMode("continuous-picture");
        this.mCamera.setParameters(parameters);
        this.mCamera.cancelAutoFocus();
    }

    /**
     * 定时器
     */
    private void initCountDownTimer() {
        this.mCountDownTimer = new CountDownTimer(mVideoMaxDuration, 100) {
            @Override
            public void onFinish() {
                mVideoProgress.setProgress(mVideoMaxDuration);
                mTvSecond.setText((mVideoMaxDuration / 1000) + "s");
            }

            @Override
            public void onTick(long timer) {
                mVideoProgress.setMax(mVideoMaxDuration);
                float var3 = mVideoMaxDuration - timer;
                mVideoProgress.setProgress(var3);
                mTvSecond.setText((int) var3 / 1000 + "s");
            }
        };
    }

    private void startAnimator1() {
        this.mTvSecond.setText("");
        this.mVideoProgress.setVisibility(View.GONE);
        this.mCameraYes.setVisibility(View.VISIBLE);
        this.mCameraBack.setVisibility(View.VISIBLE);
        float var1 = this.mCameraYes.getTranslationX();
        float var2 = this.mCameraYes.getTranslationX();
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(this.mCameraYes, "translationX", var1, (float) this.dp2px(this.context, 80.0F));
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(this.mCameraBack, "translationX", var2, (float) (-this.dp2px(this.context, 80.0F)));
        ObjectAnimator animator3 = ObjectAnimator.ofFloat(this.mVideoProgress, "alpha", 1.0F, 0.0F);
        ObjectAnimator animator4 = ObjectAnimator.ofFloat(this.mCameraYes, "alpha", 0.0F, 1.0F);
        ObjectAnimator animator5 = ObjectAnimator.ofFloat(this.mCameraBack, "alpha", 0.0F, 1.0F);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animator1, animator2, animator3, animator4, animator5);
        animatorSet.setDuration(300L);
        animatorSet.start();
    }

    private void startAnimator2() {
        this.mVideoProgress.setVisibility(View.VISIBLE);
        this.mCameraYes.setVisibility(View.GONE);
        this.mCameraBack.setVisibility(View.GONE);
        float var1 = this.mCameraYes.getTranslationX();
        float var2 = this.mCameraYes.getTranslationX();
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(this.mCameraYes, "translationX", var1, 0.0F);
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(this.mCameraBack, "translationX", -var2, 0.0F);
        ObjectAnimator animator3 = ObjectAnimator.ofFloat(this.mVideoProgress, "alpha", 0.0F, 1.0F);
        ObjectAnimator animator4 = ObjectAnimator.ofFloat(this.mCameraYes, "alpha", 1.0F, 0.0F);
        ObjectAnimator animator5 = ObjectAnimator.ofFloat(this.mCameraBack, "alpha", 1.0F, 0.0F);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animator1, animator2, animator3, animator4, animator5);
        animatorSet.setDuration(300L);
        animatorSet.start();
    }


    public void onAttach(Context context) {
        super.onAttach(context);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        this.mDirectionOrientationListener.disable();
    }

    public void setDefinition(CameraVideoFragment.Definition var1) {
        this.mDefinition = var1;
    }


    public void setPictureSize(int width, int height) {
        this.pictureSizeWidth = width;
        this.pictureSizeHeight = height;
    }

    public void setVideoMaxDuration(int maxDuration) {
        this.mVideoMaxDuration = maxDuration;
    }

    public void setVideoMaxZie(long maxZie) {
        this.mVideoMaxZie = maxZie;
    }

    public void setVideoRatio(float videoRatio) {
        if (videoRatio > 5.0F) {
            videoRatio = 5.0F;
        }
        this.mVideoRatio = videoRatio;
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.surfaceView_video) {
            if (this.mCamera != null) {
                this.cameraAutoFocus(this.mCamera);
            }
        } else if (viewId == R.id.iv_hd) {//高清
            if (this.mDefinition == CameraVideoFragment.Definition.SD) {
                this.mDefinition = CameraVideoFragment.Definition.HD;
                this.iVvHd.setBackgroundResource(R.mipmap.gb_gallery_hd);
            } else {
                this.mDefinition = CameraVideoFragment.Definition.SD;
                this.iVvHd.setBackgroundResource(R.mipmap.gb_gallery_hd_off);
            }
            this.openCamera(this.mCameraId);
        } else if (viewId == R.id.iv_camera_transform) {//前后摄像头切换
            if (this.mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                this.mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
            } else {
                this.mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
            }
            this.openCamera(this.mCameraId);
        } else if (viewId == R.id.camera_yes) {//选择当前拍摄
            if (this.mTouchListener != null) {
                mTouchListener.onSuccess(this.PHOTO_PATH);
            }
            if (this.mListener != null) {
                if (this.isPlayVideo) {
                    this.mListener.onFragmentResult(this.VIDEO_PATH, "mp4");
                    return;
                }
                this.mListener.onFragmentResult(this.PHOTO_PATH, "jpg");
            }
        } else if (viewId == R.id.camera_back) {//放弃拍摄
            this.startAnimator2();
            if (this.isPlayVideo) {
                this.stopVideo();
                this.delFile(this.VIDEO_PATH);
                return;
            }
            this.mCamera.startPreview();
            this.delFile(this.PHOTO_PATH);
            if (this.mTouchListener != null) {
                mTouchListener.onCancel();
            }
        }
    }

    private class ClickListener implements VideoProgress.OnClickListener {
        @Override
        public void onClick() {
            cameraTakePicture();
            if (mTouchListener != null)
                mTouchListener.onClick();
        }

        @Override
        public void onLongClick() {
            mCountDownTimer.start();
            initVideo();
            startVideo();
            if (mTouchListener != null)
                mTouchListener.onLongClick();
        }

        @Override
        public void onLongUpClick() {
            mTvSecond.setText("");
            mCountDownTimer.cancel();
            recorder.stop();
            recorder.reset();
            startAnimator1();
            playVideo();
        }
    }

    /**
     * 播放视频
     */
    private void playVideo() {
        this.isPlayVideo = true;
        this.surfaceView.setVisibility(View.GONE);
        this.mVideoView.setVisibility(View.VISIBLE);
        this.cameraTransform.setVisibility(View.GONE);
        this.iVvHd.setVisibility(View.GONE);
        this.mVideoView.setVideoURI(Uri.parse(this.VIDEO_PATH));
        this.mVideoView.setOnErrorListener(new OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return true;
            }
        });
        this.mVideoView.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.start();
                mp.setLooping(true);
            }
        });
        this.mVideoView.start();
        this.mVideoView.setOnInfoListener(new OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                return false;
            }
        });
    }

    /**
     * 停止视频
     */
    private void stopVideo() {
        this.isPlayVideo = false;
        this.iVvHd.setVisibility(View.VISIBLE);
        this.cameraTransform.setVisibility(View.VISIBLE);
        this.surfaceView.setVisibility(View.VISIBLE);
        this.mVideoView.setVisibility(View.GONE);
        this.mVideoView.pause();
        this.mVideoView.stopPlayback();
    }

    /**
     * 保存照片
     */
    private void cameraTakePicture() {
        mCamera.takePicture(null, null, new PictureCallback() {
            @Override
            public void onPictureTaken(final byte[] data, Camera camera) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        Matrix matrix = new Matrix();
                        CameraVideoFragment.this.mOrientation = CameraVideoFragment.this.mOrientation + 90;
                        if (CameraVideoFragment.this.mOrientation > 270) {
                            CameraVideoFragment.this.mOrientation = 0;
                        }

                        matrix.preRotate((float) CameraVideoFragment.this.mOrientation);
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                        String fileName = System.currentTimeMillis() + ".jpg";
                        CameraVideoFragment.this.PHOTO_PATH = CameraVideoFragment.this.getDiskDir(CameraVideoFragment.this.context, "/DCIM/Camera");
                        File file = new File(CameraVideoFragment.this.PHOTO_PATH);
                        if (!file.exists()) {
                            file.mkdirs();
                        }

                        file = new File(CameraVideoFragment.this.PHOTO_PATH, fileName);
                        CameraVideoFragment.this.PHOTO_PATH = file.getAbsolutePath();
                        try {
                            FileOutputStream os = new FileOutputStream(file);
                            bitmap.compress(CompressFormat.JPEG, 100, os);
                            bitmap.recycle();
                            os.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            System.gc();
                        }
                    }
                }).start();
                CameraVideoFragment.this.startAnimator1();
            }
        });
    }

    public enum Definition {
        HD, SD
    }

    /**
     * 重力感应
     */
    public class DirectionOrientationListener extends OrientationEventListener {

        public DirectionOrientationListener(Context context, int rate) {
            super(context, rate);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if (orientation != -1) {
                orientation = (orientation + 45) / 90 * 90 % 360;
                if (orientation != CameraVideoFragment.this.mOrientation) {
                    CameraVideoFragment.this.mOrientation = orientation;
                    return;
                }
            }
        }
    }


    public interface OnCameraVideoListener {
        void onFragmentResult(String path, String type);
    }

    public interface OnCameraVideoTouchListener {
        void onLongClick();

        void onClick();

        void onSuccess(String path);

        void onCancel();
    }
}
