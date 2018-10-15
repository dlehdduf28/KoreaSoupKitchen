package net.dongyeol.koreasoupkitchen;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * 클래스 : FavoDataManager
 * 즐겨찾기로 저장하거나 저장 된 급식소를 관리한다.
 */

public class FavoDataManager {
    // 중복 생성 방지 //
    private static FavoDataManager inst = null;

    private static final String TAG = "FavoDataManager";
    public  static final int REQUESTNUM_PER = 1;

    Context context;

    // 저장소 쓰기/읽기 권한은 위험 권한이며 별도의 승인 요청이 필요하다. //
    // 저장소의 쓰기/읽기 권한이 승인 되있다면 permissionCheck는 true가 된다. //
    private static boolean permissionCheck = false;
    private static boolean prepareCheck = false;

    String  storagePath;
    File    dataPath;
    File[]  listFile;


    public static FavoDataManager getInstance(Context context) {
        if (inst == null)
            inst = new FavoDataManager(context);
        return inst;
    }
    // 반드시 MainActivity의 Activity 객체가 전달 되어야 함. //
    private FavoDataManager(Context c)
    {
        this.context = c;
        receivePermission();
    }

    public boolean prepareManager()
    {
        if(permissionCheck == true)
        {
            // 휴대폰 저장소의 절대 경로를 얻는다. //
            storagePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            // 휴대폰 저장소의 favo 파일을 저장하는 폴더를 File로 가르킨다. //
            dataPath = new File(storagePath + context.getString(R.string.favoData_Folder));

            // 휴대폰 저장소의 favo 파일을 저장하는 폴더가 생성 되지 않았다면 생성 한다. //
            if(dataPath.exists() == false)
                dataPath.mkdirs();
            prepareCheck = true;
            return true;
        }

        return false;
    }

    public ArrayList<SoupKitItem> readSoupItemList()
    {
        if(permissionCheck == false || prepareCheck == false ||
                storagePath == null || dataPath == null)
            return null;

        ArrayList<SoupKitItem> soupItemList = new ArrayList<SoupKitItem>();
        try
        {
            // 저장소 권한이 얻어진 직후에 이 메소드를 호출 하면 null이 반환 된다. //
            //사용자에게 권한을 얻고나서 어플을 다시 실행한 뒤에 즐겨찾기 기능을 사용할 것을 요구해야 한다. //

            listFile = dataPath.listFiles();

            if(listFile == null) {
                Log.d(TAG, "listFile is null!");
                return soupItemList;
            }

            for(int i = 0; i < listFile.length; i++)
            {
                // 파일 1개당 한 개의 SoupKitItem 객체를 저장 하고 있다. //
                ObjectInputStream inputStream = new ObjectInputStream(new BufferedInputStream(
                        new FileInputStream(listFile[i])));
                soupItemList.add((SoupKitItem) inputStream.readObject());
                inputStream.close();
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, e.toString());
        }

        return soupItemList;
    }

    public boolean writeSoupKitItem(SoupKitItem item)
    {
        if(permissionCheck == false || prepareCheck == false || storagePath == null
                || dataPath == null || item == null)
            return false;

        // 서울_성동종합사회복지관.soup //
        String fileName = item.getAddress().substring(0, 2) + "_" + item.getTitle();

        File file = new File(dataPath, fileName + ".soup");

        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(new BufferedOutputStream
                    (new FileOutputStream(file)));
            outputStream.writeObject(item);
            outputStream.close();
        } catch (Exception e)
        {
            Log.e(TAG, e.toString());
            return false;
        }

        Log.d(TAG, file.toString() + "저장 완료");

        return true;
    }

    public boolean deleteSoupKitItem(int idx)
    {
        if (listFile != null) {
            listFile[idx].delete();
            return true;
        }
        return false;
    }

    // 대화상자에 의한 권한 승인 결과는 MainActivity에 오버라이딩 된
    // onRequestPermissionResult 메소드에 의해 이 클래스의 permissionCheck 변수에 권한 승인 여부가 저장 된다.
    // receivePermission() 메소드를 호출 한 다음에 파일 입출력을 시도 해야 한다. //
    // 사용자 환경이 마시멜로 이상일 경우 권한 요청 대화상자를 띄운다. //
    public void receivePermission()
    {
        // 마시멜로 이상일 경우 권한을 자바 코드 상에서 요청하여야 한다. //
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 스토리지 접근 권한이 허가 되어 있는지 확인 한다. //
            int writePerCheck = ContextCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int readPerCheck = ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE);

            if (writePerCheck == PackageManager.PERMISSION_GRANTED &&
                    readPerCheck == PackageManager.PERMISSION_GRANTED)
            {
                Log.d(TAG, "SD카드 쓰기/읽기 권한 있음.");
                permissionCheck = true;
            } else {
                Log.d(TAG, "SD카드 쓰기/읽기 권한 없음.");
                permissionCheck = false;
                // 사용자에게 READ, WRITE 권한을 요청하는 대화상자를 띄운다. //
                ActivityCompat.requestPermissions((Activity) context, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUESTNUM_PER);
            }
        } else { // 마시멜로 이전의 환경의 경우엔 허용 절차를 취하지 않아도 되므로 허용한 것으로 처리 한다.
            permissionCheck = true;
        }
    }


    public static void setPermissionCheck(boolean permissionCheck) {
        FavoDataManager.permissionCheck = permissionCheck;

        // 권한이 승인 되기 전에 prepareManager가 호출 되었을 것이므로 prepareManager를 호출해본다. //
        if(FavoDataManager.permissionCheck == true && inst != null)
            inst.prepareManager();
    }

    public static boolean isPermissionCheck() {
        return permissionCheck;
    }

    public static boolean isPrepareCheck() {
        return prepareCheck;
    }
}
