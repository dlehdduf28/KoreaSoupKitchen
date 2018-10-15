package net.dongyeol.koreasoupkitchen;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;

import java.io.File;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 클래스 : CameraManager
 * 인텐트로 카메라를 호출해 주고 사진 파일을 temp 폴더에 저장한 뒤 파일 경로와 비트맵 파일을 반환 한다.
 */

public final class CameraManager {
    public static final String  TAG = "CameraManager";
    public static final int     REQUEST_IMAGE_CAPTURE = 20002;

    // 1 / SAMPLE_SIZE (2의 배수) = 이미지 품질 //
    public static final int     DEFAULT_SAMPLE_SIZE   = 4;
    public static final int     LOW_SAMPLE_SIZE       = 32;

    private Activity            activity;
    private final String        dateFormatStr = "yyyy년 MM월 dd일 HH시 mm분 ss초";
    private SimpleDateFormat    dateFormat;

    private File                pictureDir;
    private File                file;
    private Intent              intent;

    /**
     * 생성자에 액티비티를 전달 하면 인스턴스의 초기화를 실행 함.
     * @param a
     */
    public CameraManager(Activity a) {
        activity    =   a;

        intent      =   new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // API 24 이상 부터 File 경로를 통한 입출력시 FileUriExposedException이 발생한다. //
        // 다음 메소드를 호출하면 예외가 발생하지 않는다. //
        resolveFileUriExposedException();

        pictureDir     =   new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                                    + a.getString(R.string.photoData_Folder));

        // /StorageHelper/temp/yyyy년 MM월 dd일 HH시 mm분 ss초.jpg  (dateFormat 적용했을 때) //
        dateFormat  =   new SimpleDateFormat(dateFormatStr);
        file     =   new File(pictureDir, dateFormat.format(new Date()) + ".jpg");

        // dateFormat 적용 안 했을 때 파일 이름 : 4792384723847 //
        // file        =   new File(tempDir, String.valueOf(new Date().getTime()));
    }

    /**
     * 메소드를 호출 하면 클래스 멤버인 REQUEST_IMAGE_CAPTURE 상수로 카메라 앱을 호출 한다.
     */
    public void requestCamera() {
        intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        activity.startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    /**
     * requestCamera()를 호출한 액티비티에서 카메라에서 RESULT_OK로 응답이 왔을 경우
     * 밑의 메소드를 실행하면 temp 폴더에 저장된 사진 파일의 압축된 비트맵과 파일 객체를 얻을 수 있다.
     * @return
     */

    public BitmapFile getBitmapFile() {
        try {
            Bitmap bitmap = getBitmap(file, DEFAULT_SAMPLE_SIZE);

            return new BitmapFile(file, bitmap);
        } catch (Exception e) {
            Snackbar.make(activity.getCurrentFocus(),
                    activity.getString(R.string.mag_camera_error), Snackbar.LENGTH_LONG).show();

        }

        return null;
    }

    /**
     * 전달 된 파일 객체가 가르키는 사진 파일을 Bitmap 객체로 반환 한다.
     * @param f             비트맵 객체화 할 사진 파일을 가리키는 파일 객체
     * @param sampleSize    이미지 품질 ( default = 4 )
     * @return
     */

    public static Bitmap getBitmap(File f, int sampleSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;

        return BitmapFactory.decodeFile(f.getAbsolutePath(), options);
    }

    /**
     * FileUriExposedException을 방지하는 소스를 실행한다.
     */
    private void resolveFileUriExposedException() {
        if(Build.VERSION.SDK_INT >= 24) {
            try {
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Nested Class //
    public static class BitmapFile {
        private File    file;
        private Bitmap  bitmap;

        public BitmapFile(File f, Bitmap b) {
            this.file   = f;
            this.bitmap = b;
        }

        public File getFile() {
            return file;
        }

        public void setFile(File file) {
            this.file = file;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

        public void setBitmap(Bitmap bitmap) {
            this.bitmap = bitmap;
        }
    }
}
