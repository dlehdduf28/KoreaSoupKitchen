package net.dongyeol.koreasoupkitchen;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.List;

/**
 * 클래스 : CurLocationManager
 * GeoCoder를 이용해서, 현재 위치의 주소명을 인수로 전달된 LocPosStorage에 저장한다.
 */

public class CurLocationManager {
    // 중복 생성 방지 //
    private static CurLocationManager inst        = null;

    // 로깅 태그 //
    public  static final String  TAG              = "CurLocationManager";

    // Variable to request permission //
    public  static final int     REQUESTNUM_PER   = 2;
    private static boolean       permissionCheck  = false;
    private static boolean       prepareCheck     = false;

    // 각 제공자가 사용가능한 상태인지에 대한 플래그 //
    boolean isGPSEnabled        = false;
    boolean isNetworkEnabled    = false;

    // 현재 위치 정보가 준비 되었는지에 대한 플래그 //
    boolean isMakeCurLocation   = false;

    Context             context;

    // 주소를 담을 LocPosStorage //
    LocPosStorage     locStorage;

    private LocationManager     locationManager;
    private List<String>        locProviders;

    private double              latitudeVal;
    private double              longitudeVal;

    /**
     * 생성 메소드 : getInstance(Context, LocPosStorage)
     * @param context       Activity 객체 (권한 요청에 쓰이게 됨.)
     * @param locStorage    주소를 담을 LocPosStorage
     * @return
     */
    public static CurLocationManager getInstance(Context context, LocPosStorage locStorage) {
        if (inst == null)
            inst = new CurLocationManager(context);

        inst.locStorage = locStorage;
        return inst;
    }

    private CurLocationManager(Context context)
    {
        this.context = context;
        receivePermission();
    }

    public boolean prepareManager() {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if(locationManager != null) {
            isGPSEnabled        = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled    = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if(isGPSEnabled || isNetworkEnabled)
            {
                // 현재 사용 가능한 위치 제공자의 List<String>을 반환 받는다. //
                locProviders    = locationManager.getProviders(true);
                prepareCheck = true;
                return true;
            }

        }

        prepareCheck = false;
        return false;
    }

    public void requestLocation()
    {
        if(prepareCheck == false || permissionCheck == false)
            return;

        try {
            // 사용 가능한 위치 제공자로 1초 단위로 10m 이내의 위치를 조회 했을 때 Location 객체를 담아서 리턴 한다. //
            for(String name : locProviders)
                locationManager.requestLocationUpdates(name, 1000, 10, new LocationListener());
        } catch (SecurityException e) {
            e.toString();
        }
    }

    public String getLocationString()
    {
        if(isMakeCurLocation == false)
            return null;

        //    필요한 권 한
        //    ACCESS_FINE_LOCATION : 현재 나의 위치를 얻기 위해서 필요함
        //    INTERNET : 구글서버에 접근하기위해서 필요함

        String locationStr = null;

        try {
            final Geocoder geocoder = new Geocoder(context);

            // Geocoder에게 경도 위도 정보를 전달 하고 최대 10개의 Address객체를 제공 받는다. //
            List<Address> list = geocoder.getFromLocation(latitudeVal, longitudeVal, 10);

            if(list == null)
                return null;
            if(list.size() == 0)
                return null;

            // Address 객체에서 AdminArea = 서울특별시 Locality = 중구 급의 주소 정보를 얻는다. //
            Address address = list.get(0);

            // 버그 수정#1
            // AdminArea = 대구광역시, Locality = null, subLocality = 동구
            // AdminArea = 경상북도, Locality = 경산시

            String adminArea = address.getAdminArea();
            String baseArea = adminArea.substring(adminArea.length() - 1, adminArea.length()); // 이상, 미만

            if ( baseArea.equals("시") )
                // AdminArea가 시일 경우 //
                locationStr = address.getAdminArea() + " " + address.getSubLocality();
            else
                // AdminArea가 도일 경우 //
                locationStr = address.getAdminArea() + " " + address.getLocality();

            Log.d(TAG, locationStr);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return locationStr;
    }


    void receivePermission()
    {
        // 마시멜로 이상일 경우 권한을 자바 코드 상에서 요청하여야 한다. //
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 위치 조회 권한이 허가 되어 있는지 확인 한다. //
            int finePerCheck = ContextCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            int coarsePerCheck = ContextCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_COARSE_LOCATION);

            if (finePerCheck == PackageManager.PERMISSION_GRANTED &&
                    coarsePerCheck == PackageManager.PERMISSION_GRANTED)
            {
                Log.d(TAG, "위치 조회 권한 있음.");
                permissionCheck = true;
            } else {
                Log.d(TAG, "위치 조회 권한 없음.");
                permissionCheck = false;
                // 사용자에게 READ, WRITE 권한을 요청하는 대화상자를 띄운다. //
                ActivityCompat.requestPermissions((Activity) context, new String[] {Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION}, REQUESTNUM_PER);

                // 위치 조회 권한이 없을 때에 사용자에게 권한이 필요함을 스낵바를 통해 안내 한다. //

            }
        } else { // 마시멜로 전의 환경의 경우엔 허용 절차를 취하지 않아도 되므로 허용한 것으로 처리 한다.
            permissionCheck = true;
        }
    }

    public static void setPermissionCheck(boolean permissionCheck) {
        CurLocationManager.permissionCheck = permissionCheck;
    }

    public static boolean isPermissionCheck() {
        return permissionCheck;
    }

    public boolean isMakeCurLocation() {
        return isMakeCurLocation;
    }

    private class LocationListener implements android.location.LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            latitudeVal     =   location.getLatitude();
            longitudeVal    =   location.getLongitude();

            Log.d(TAG, "CurPos: " + latitudeVal + ", " + longitudeVal);

            // 현재 위치의 좌표가 수신 되었음을 LocPosStorage에 알려서 좌표를 받아가려는 스레드가 있다면
            // 깨운다. //
            locStorage.setCurLocPos(inst);

            isMakeCurLocation = true;
            try {
                // 위치 정보를 받았으므로 manager의 위치 조회 서비스를 종료 한다. //
                locationManager.removeUpdates(this);
            } catch (SecurityException e)
            {
                Log.e(TAG, e.toString());
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    }
}
