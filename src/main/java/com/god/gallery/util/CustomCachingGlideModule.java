package com.god.gallery.util;

import android.content.Context;
import android.os.Environment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory;
import com.bumptech.glide.load.engine.cache.ExternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;
import com.bumptech.glide.module.GlideModule;
import com.god.gallery.PhotoActivity;

import java.io.File;

/**
 * Created by My on 2017/1/10.
 */

public class CustomCachingGlideModule implements GlideModule {
    private int diskSize = 1024 * 1024 * 50;
    private int memorySize = (int) (Runtime.getRuntime().maxMemory()) / 4;  // 取1/8最大内存作为最大缓存

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        // 定义缓存大小和位置
        //builder.setDiskCache(new InternalCacheDiskCacheFactory(context, diskSize));  //内存中
        // builder.setDiskCache(new ExternalCacheDiskCacheFactory(context, "cache", diskSize)); //sd卡中

        String dir = getDiskCacheDir(context, "glide");
        builder.setDiskCache(new DiskLruCacheFactory(dir, diskSize)); //sd卡中

//        // 默认内存和图片池大小
//        MemorySizeCalculator calculator = new MemorySizeCalculator(context);
//        int defaultMemoryCacheSize = calculator.getMemoryCacheSize(); // 默认内存大小
//        int defaultBitmapPoolSize = calculator.getBitmapPoolSize(); // 默认图片池大小
//        builder.setMemoryCache(new LruResourceCache(defaultMemoryCacheSize)); // 该两句无需设置，是默认的
//        builder.setBitmapPool(new LruBitmapPool(defaultBitmapPoolSize));

        // 自定义内存和图片池大小
        builder.setMemoryCache(new LruResourceCache(memorySize));
        builder.setBitmapPool(new LruBitmapPool(memorySize));

        // 定义图片格式
        // builder.setDecodeFormat(DecodeFormat.PREFER_ARGB_8888);
        // builder.setDecodeFormat(DecodeFormat.PREFER_RGB_565); // 默认
    }

    @Override
    public void registerComponents(Context context, Glide glide) {

    }

    /**
     * Get a usable cache directory (external if available, internal otherwise).
     *
     * @param context    The context to use
     * @param uniqueName A unique directory name to append to the cache dir
     * @return The cache dir
     */
    public static String getDiskDir(Context context, String uniqueName) {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        final String cachePath =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        Environment.isExternalStorageRemovable() ? Environment.getExternalStorageDirectory().getPath() :
                        context.getCacheDir().getPath();
        if (uniqueName == null)
            return cachePath;
        return cachePath + File.separator + uniqueName;
    }

    public static String getDiskCacheDir(Context context, String uniqueName) {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        final String cachePath =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        Environment.isExternalStorageRemovable() ? context.getExternalCacheDir().getPath() :
                        context.getCacheDir().getPath();
        if (uniqueName == null || uniqueName.length() == 0)
            return cachePath;
        return cachePath + File.separator + uniqueName;
    }

  //  public abstract String onDiskCacheDir(String uniqueName);
}
