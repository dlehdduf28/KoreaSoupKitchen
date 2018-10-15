package net.dongyeol.koreasoupkitchen;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

/**
 * 액티비티 클래스 : PrepareActivity
 * 앱을 실행했을 때 스플래시 화면을 띄우면서 데이터를 로드한다.
 */
public class PrepareActivity extends AppCompatActivity {
    private static final String TAG = "PrepareActivity";
    private SoupDataManager dataManager;
    // 전체 데이터들을 담을 리스트 //
    ArrayList<SoupKitItem> soupItemList;

    DataParsingThread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 스플래시 구현을 위해 액션바는 숨긴다. //
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prepare);

        // SoupDataManager 생성 //
        dataManager = new SoupDataManager(getApplicationContext());

        thread = new DataParsingThread();
        thread.start();
    }

    class DataParsingThread extends Thread {
        @Override
        public void run() {
            long startTime = System.currentTimeMillis();

            try {
                // 데이터 파싱을 시작한다. //
                dataManager.dataParsingStart();

                // 데이터를 담은 리스트를 받고 별도로 데이터를 담을 리스트를 생성 한다. //
                soupItemList = dataManager.getSoupItemList(); // 이 메소드 안에선 데이터가 전부 파싱될 때까지 대기 한다. //

                long endTime = System.currentTimeMillis();
                Log.d(TAG, "dataParsing Time : " + (endTime - startTime));

                // SoupFragment의 참조변수의 참조 값을 저장 한다.
                SoupFragment.soupItemList = soupItemList;
                SoupFragment.isSoupItemList = true;

                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        }
    }
}
