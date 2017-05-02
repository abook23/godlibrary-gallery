godlibrary-gallery
======================
[![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

# 使用方法
###Stop1
```java
compile 'com.abook23:godlibrary-gallery:1.1'
```
###Stop2
打开相册
```java
PhotoActivity.startActivityForResult(Activity ac, int checkMax, ArrayList<String> checkPath, int resultCode)
```

图片墙
```java
ImageInfoActivity.start(Context context, int position, ArrayList<String> urls)
```
##自定义相机
```java
		CameraVideoFragment videoFragment = CameraVideoFragment.newInstance();
		//没特殊要求,sd 就可以了,HD 的视频有点大,微信就相当于SD模式
        videoFragment.setDefinition(CameraVideoFragment.Definition.SD);
        videoFragment.setVideoMaxDuration(60 * 1000);//最大录入时间,默认10s
        videoFragment.setVideoRatio(0.8f);//视频质量 ----微信视频 质量大概在 0.8f 左右, 要清晰一点,就调节大一些
        //videoFragment.setVideoMaxZie(50 * 1024 * 1024);//默认50MB
        videoFragment.setOnCameraVideoListener(new CameraVideoFragment.OnCameraVideoListener() {
            @Override
            public void onFragmentResult(String path, String type) {
                L.d(path);
            }
        });

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.videoFragment, videoFragment);
        transaction.commit();
```