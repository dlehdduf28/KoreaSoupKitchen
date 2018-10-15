package net.dongyeol.koreasoupkitchen;

import android.content.Context;
import android.os.Parcel;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;

/**
 * 클래스 : PlaceSpinnerManager
 * 지역별 상수와 전달된 스피너의 데이터와 리스너를 관리 함.
 */
public class PlaceSpinnerManager {
    private Spinner     spinner;
    private String[]    items;
    private Context     pContext;

    AdapterView.OnItemSelectedListener userListener;

    public PlaceSpinnerManager(Spinner sp, Context c)
    {
        spinner = sp;
        pContext = c;


        // 배열에 전국 지역 정보를 저장 함. //
        items = new String[] { c.getString(R.string.seoul_region), c.getString(R.string.busan_region),
                c.getString(R.string.incheon_region),
                c.getString(R.string.daejeon_region), c.getString(R.string.ulsan_region),
                c.getString(R.string.daegu_region), c.getString(R.string.gwangju_region),
                c.getString(R.string.sejong_region), c.getString(R.string.gyeonggiDo_region),
                c.getString(R.string.GangwonDo_region), c.getString(R.string.chungcheongbukDo_region),
                c.getString(R.string.chungcheongnamDo_region), c.getString(R.string.jeollabukDo_region),
                c.getString(R.string.jeollanamDo_region), c.getString(R.string.gyeongsangbukDo_region),
                c.getString(R.string.gyeongsangnamDo_region), c.getString(R.string.jejuDo_region)};
    }

    public void setPlaceSpinner()
    {
        if(userListener != null)
            spinner.setOnItemSelectedListener(userListener);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(pContext, R.layout.spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    public String[] getItems() {
        return items;
    }

    public void setUserListener(AdapterView.OnItemSelectedListener l)
    {
        userListener = l;
    }

    // 지역을 의미하는 상수 //
    /*
    public static final int SEOUL_REGION    = 10001;
    public static final int SEJONG_REGION   = 10002;
    public static final int BUSAN_REGION    = 10003;
    public static final int INCHEON_REGION  = 10004;
    public static final int DAEJEON_REGION  = 10005;
    public static final int ULSAN_REGION    = 10006;
    public static final int DAEGU_REGION    = 10007;
    public static final int GWANGJU_REGION  = 10008;

    public static final int GYEONGGIDO_REGION           = 20001;
    public static final int GANGWONDO_REGION            = 20002;
    public static final int CHUNGCHEONGBUKDO_REGION     = 20003;
    public static final int CHUNGCHEONGNAMDO_REGION     = 20004;
    public static final int JEOLLABUKDO_REGION          = 20005;
    public static final int JEOLLANAMDO_REGION          = 20006;
    public static final int GYEONGSANGBUKDO_REGION      = 20007;
    public static final int GYEONGSANGNAMDO_REGION      = 20008;
    public static final int JEJUDO_REGION               = 20009;
    */
}
