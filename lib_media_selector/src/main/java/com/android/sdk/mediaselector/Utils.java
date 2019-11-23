package com.android.sdk.mediaselector;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

/**
 * See:
 * <pre>
 *      https://stackoverflow.com/questions/20067508/get-real-path-from-uri-android-kitkat-new-storage-access-framework
 * </pre>
 *
 * @author Ztiany
 * Email: ztiany3@gmail.com
 * Date : 2017-08-09 10:54
 */
final class Utils {

    private static final String TAG = "Utils";

    private Utils() {
        throw new UnsupportedOperationException("Utils");
    }

    ///////////////////////////////////////////////////////////////////////////
    // Camera
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 判断系统中是否存在可以启动的相机应用
     *
     * @return 存在返回true，不存在返回false
     */
    static boolean hasCamera(Context context) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    /**
     * @param targetFile 源文件，裁剪之后新的图片覆盖此文件
     */
    static Intent makeCaptureIntent(Context context, File targetFile, String authority) {
        makeFilePath(targetFile);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (Build.VERSION.SDK_INT < 24) {
            Uri fileUri = Uri.fromFile(targetFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        } else {
            Uri fileUri = FileProvider.getUriForFile(context, authority, targetFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        return intent;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Files
    ///////////////////////////////////////////////////////////////////////////
    static Intent makeFilesIntent(String mimeType) {
        if (TextUtils.isEmpty(mimeType)) {
            mimeType = "*/*";
        }
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(mimeType);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        return intent;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Crop
    ///////////////////////////////////////////////////////////////////////////

    /**
     * @param targetFile 源文件，裁剪之后新的图片覆盖此文件
     */
    static Intent makeCropIntentCovering(Context context, File targetFile, String authority, CropOptions cropOptions, String title) {
        Log.d(TAG, "makeCropIntentCovering() called with: context = [" + context + "], targetFile = [" + targetFile + "], authority = [" + authority + "], cropOptions = [" + cropOptions + "], title = [" + title + "]");

        Intent intent = new Intent("com.android.camera.action.CROP");

        Uri fileUri;
        if (Build.VERSION.SDK_INT < 24) {
            fileUri = Uri.fromFile(targetFile);
        } else {
            fileUri = FileProvider.getUriForFile(context, authority, targetFile);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }

        intent.setDataAndType(fileUri, "image/*");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        intent.putExtra("aspectX", cropOptions.getAspectX());
        intent.putExtra("aspectY", cropOptions.getAspectY());
        intent.putExtra("outputX", cropOptions.getOutputX());
        intent.putExtra("outputY", cropOptions.getOutputY());
        intent.putExtra("scale", true);
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true);

        List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            context.grantUriPermission(packageName, fileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        intent = Intent.createChooser(intent, title);
        return intent;
    }

    /**
     * @param targetFile 目标文件，裁剪之后新的图片保存到此文件
     * @param srcFile    源文件
     */
    static Intent makeCropIntentNoCovering(Context context, File srcFile, File targetFile, String authority, CropOptions cropOptions, String title) {

        makeFilePath(targetFile);

        Intent intent = new Intent("com.android.camera.action.CROP");

        Uri outputUri;
        Uri srcUri;

        if (Build.VERSION.SDK_INT < 24) {
            outputUri = Uri.fromFile(targetFile);
            srcUri = Uri.fromFile(srcFile);
        } else {
            outputUri = FileProvider.getUriForFile(context, authority, targetFile);
            srcUri = FileProvider.getUriForFile(context, authority, srcFile);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }

        intent.setDataAndType(srcUri, "image/*");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);

        intent.putExtra("aspectX", cropOptions.getAspectX());
        intent.putExtra("aspectY", cropOptions.getAspectY());
        intent.putExtra("outputX", cropOptions.getOutputX());
        intent.putExtra("outputY", cropOptions.getOutputY());
        intent.putExtra("scale", true);
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true);

        List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            context.grantUriPermission(packageName, outputUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        intent = Intent.createChooser(intent, title);
        return intent;
    }

    ///////////////////////////////////////////////////////////////////////////
    // UCrop
    ///////////////////////////////////////////////////////////////////////////

    static void toUCrop(Context context, Fragment fragment, String srcPath, String targetPath, CropOptions cropConfig, int requestCode) {
        Uri srcUri = new Uri.Builder()
                .scheme("file")
                .appendPath(srcPath)
                .build();

        Uri targetUri = new Uri.Builder()
                .scheme("file")
                .appendPath(targetPath)
                .build();

        //参数
        UCrop.Options crop = new UCrop.Options();
        crop.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        crop.withMaxResultSize(cropConfig.getOutputX(), cropConfig.getAspectY());
        crop.withAspectRatio(cropConfig.getAspectX(), cropConfig.getAspectY());

        //颜色
        int color = MediaSelectorStyle.fetchPrimaryColor();
        crop.setToolbarColor(color);
        crop.setStatusBarColor(color);

        //开始裁减
        if (fragment != null) {
            UCrop.of(srcUri, targetUri)
                    .withOptions(crop)
                    .start(context, fragment, requestCode);
        } else {
            if (!(context instanceof AppCompatActivity)) {
                throw new IllegalArgumentException("the context must be instance of AppCompatActivity");
            }
            UCrop.of(srcUri, targetUri)
                    .withOptions(crop)
                    .start((AppCompatActivity) context, requestCode);
        }
    }

    public static Uri getUCropResult(Intent data) {
        if (data == null) {
            return null;
        }
        Throwable throwable = UCrop.getError(data);
        if (throwable != null) {
            return null;
        }
        return UCrop.getOutput(data);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Album
    ///////////////////////////////////////////////////////////////////////////
    static Intent makeAlbumIntent() {
        return new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    }

    ///////////////////////////////////////////////////////////////////////////
    // 从各种Uri中获取真实的路径
    ///////////////////////////////////////////////////////////////////////////

    /**
     * @see "https://stackoverflow.com/questions/20067508/get-real-path-from-uri-android-kitkat-new-storage-access-framework/20559175"
     */
    static String getAbsolutePath(final Context context, final Uri uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     * @see "https://stackoverflow.com/questions/20067508/get-real-path-from-uri-android-kitkat-new-storage-access-framework/20559175"
     */
    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = MediaStore.Images.Media.DATA;
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    ///////////////////////////////////////////////////////////////////////////
    // BitmapUtils：如果拍摄的图片存在角度问题，通过下面分发修正
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 读取图片的旋转的角度
     *
     * @param path 图片绝对路径
     * @return 图片的旋转角度
     */
    @SuppressWarnings("unused")
    public static int getBitmapDegree(String path) {
        int degree = 0;
        try {
            // 从指定路径下读取图片，并获取其EXIF信息
            ExifInterface exifInterface = new ExifInterface(path);
            // 获取图片的旋转信息
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 将图片按照某个角度进行旋转
     *
     * @param bm     需要旋转的图片
     * @param degree 旋转角度
     * @return 旋转后的图片
     */
    @SuppressWarnings("unused")
    public static Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
        Bitmap returnBm = null;
        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }

    ///////////////////////////////////////////////////////////////////////////
    //如果照片保存的文件目录是由 getExternalFilesDir() 所提供的，那么，媒体扫描器是不能访问这些文件的，因为照片对于你的APP来说是私有的。
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 显示图片到相册
     *
     * @param photoFile 要保存的图片文件
     */
    public static void displayToGallery(Context context, File photoFile) {
        if (photoFile == null || !photoFile.exists()) {
            return;
        }
        String photoPath = photoFile.getAbsolutePath();
        String photoName = photoFile.getName();
        // 其次把文件插入到系统图库
        try {
            ContentResolver contentResolver = context.getContentResolver();
            MediaStore.Images.Media.insertImage(contentResolver, photoPath, photoName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 最后通知图库更新
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + photoPath)));
    }

    ///////////////////////////////////////////////////////////////////////////
    // FileUtils
    ///////////////////////////////////////////////////////////////////////////

    private static boolean makeFilePath(File file) {
        if (file == null) {
            return false;
        }
        File parent = file.getParentFile();
        if (parent == null) {
            return false;
        }
        return parent.exists() || parent.mkdirs();
    }

    private static void makeNewFile(File file) {
        makeFilePath(file);
        try {
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean isSpace(final String s) {
        if (s == null) return true;
        for (int i = 0, len = s.length(); i < len; ++i) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static String getFileNameNoExtension(final String filePath) {
        if (isSpace(filePath)) return "";
        int lastPoi = filePath.lastIndexOf('.');
        int lastSep = filePath.lastIndexOf(File.separator);
        if (lastSep == -1) {
            return (lastPoi == -1 ? filePath : filePath.substring(0, lastPoi));
        }
        if (lastPoi == -1 || lastSep > lastPoi) {
            return filePath.substring(lastSep + 1);
        }
        return filePath.substring(lastSep + 1, lastPoi);
    }

    public static String getFileExtension(final String filePath) {
        if (isSpace(filePath)) return "";
        int lastPoi = filePath.lastIndexOf('.');
        int lastSep = filePath.lastIndexOf(File.separator);
        if (lastPoi == -1 || lastSep >= lastPoi) return "";
        return filePath.substring(lastPoi + 1);
    }

    public static String addFilePostfix(final String filePath, String postfix) {
        if (isSpace(filePath)) return "";
        File file = new File(filePath);
        return file.getParentFile().getAbsolutePath() + File.separator + getFileNameNoExtension(filePath) + postfix + "." + getFileExtension(filePath);
    }

}