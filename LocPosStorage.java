package net.dongyeol.koreasoupkitchen;

import android.util.Log;

/**
 * 클래스 : LocPosStorage
 *
 * 동기화 처리를 해서 주소를 받아가는 스레드를 Blocked 시켰다가 주소가 등록 되면 주소가 반환 되는 메소드가 있음.
 * 주소를 받아가는 스레드는 현재 위치의 좌표 값이 얻어질 때까지 대기 했다가, 좌표 값이 얻어지면 GeoCoder를
 * 통해 문자열로 된 주소 값을 얻는다.
 */

public class LocPosStorage {
    public static String TAG = "LocPosStorage";
    private CurLocationManager locManager;
    private boolean isCurLocPos = false;

    void setCurLocPos(CurLocationManager manager) {
        locManager  = manager;
        isCurLocPos = true;

        synchronized (this) {
            notifyAll();
        }
    }

    String getCurLocation() {
        if (isCurLocPos == false) {
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Log.d(TAG, e.getMessage());
                }
            }
        }

        return locManager.getLocationString();
    }
}
