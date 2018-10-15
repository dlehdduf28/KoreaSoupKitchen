package net.dongyeol.koreasoupkitchen;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * 액티비티 클래스 : InfoActivity
 * 앱의 정보를 출력한다.
 */

public class InfoActivity extends AppCompatActivity {
    public static final String APP_LINK = "market://details?id=net.dongyeol.koreasoupkitchen";

    Button      updateBtn;
    TextView    helper_DataGov_TextView;
    TextView    helper_PSJ_TextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        // 리소스 찾기 //
        updateBtn       =       (Button) findViewById(R.id.info_button_update);

        helper_DataGov_TextView = (TextView) findViewById(R.id.info_textView_info_helper_DataGov);
        helper_PSJ_TextView     = (TextView) findViewById(R.id.info_textView_info_helper_ParkSooJeong);

        // 업데이트 버튼을 클릭 하면 플레이 스토어의 공짜 급식소 정보를 출력 한다. //
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri url = Uri.parse(APP_LINK);
                Intent intent = new Intent(Intent.ACTION_VIEW, url);
                startActivity(intent);
            }
        });


        // 텍스트뷰를 클릭 하면 활용된 데이터를 출력 한다. //
        helper_DataGov_TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view,
                        getString(R.string.info_helper_DataGov_c), Snackbar.LENGTH_LONG).show();
            }
        });

    }
}
