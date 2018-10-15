package net.dongyeol.koreasoupkitchen;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 레이아웃 클래스 : SoupKitItemView
 * 리스트뷰에서 하나의 무료급식소 뷰로 사용하기 위한 레이아웃 클래스
 */
public class SoupKitItemView extends LinearLayout {
    private String TAG = "SoupKitItemView";


    private TextView  regionText; // 대표 지역
    private TextView  sText01; // 시설명
    private TextView  sText02; // 급식소 주소
    private TextView  sText03; // 급식소 장소
    private TextView  sText04; // 급식 날짜
    private TextView  sText05; // 급식 시간
    private TextView  sText06; // 연락처
    private TextView  sText07; // 확인일자

    private Button favoBtn;
    private Button naviBtn;
    private Button callBtn;

    SoupKitItem soupItem;
    Context     context;

    // 생성자 //
    public SoupKitItemView (Context context, SoupKitItem item)
    {
        super(context);

        this.context = context;
        soupItem = item;
        // listitem.xml의 레이아웃 요소를 이 객체에 인플레이션 한다. //
        LayoutInflater inflater = (LayoutInflater) context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.listitem, this, true);


        regionText = (TextView) findViewById(R.id.regionText_listitem);
        sText01 = (TextView)  findViewById(R.id.dataItem01);
        sText02 = (TextView)  findViewById(R.id.dataItem02);
        sText03 = (TextView)  findViewById(R.id.dataItem03);
        sText04 = (TextView)  findViewById(R.id.dataItem04);
        sText05 = (TextView)  findViewById(R.id.dataItem05);
        sText06 = (TextView)  findViewById(R.id.dataItem06);
        sText07 = (TextView)  findViewById(R.id.dataItem07);

        favoBtn = (Button) findViewById(R.id.favoBtn_listitem);
        naviBtn = (Button) findViewById(R.id.naviBtn_listitem);
        callBtn = (Button) findViewById(R.id.callBtn_listitem);

        // 뷰에 해당 인덱스 아이템의 데이터 설정 //
        setSoupKitItem(item);

        // 해당 버튼에 리스너 설정. //
        favoBtn.setOnClickListener(new ItemButtonListener(ItemButtonListener.FAVO_BTN));
        naviBtn.setOnClickListener(new ItemButtonListener(ItemButtonListener.NAVI_BTN));
        callBtn.setOnClickListener(new ItemButtonListener(ItemButtonListener.CALL_BTN));
    }

    // 인덱스를 전달 받아 해당 텍스트 뷰에 텍스트를 설정하는 메소드 //
    public void setText(int index, String data)
    {
        if (index == 0) {
            sText01.setText(data);
        } else if (index == 1) {
            sText02.setText(data);
        } else if (index == 2) {
            sText03.setText(data);
        } else if (index == 3) {
            sText04.setText(data);
        } else if (index == 4) {
            sText05.setText(data);
        } else if (index == 5) {
            sText06.setText(data);
        } else if (index == 6) {
            sText07.setText(data);
        }
    }

    // 전달되는 SoupKitItem의 데이터로 인스턴스의 데이터를 변경 하는 메소드 //
    public void setSoupKitItem(SoupKitItem item)
    {
        soupItem = item;

        // 뷰에 해당 인덱스 아이템의 데이터 설정 //
        // setText 메소드는 인자에 null이 전달 되더라도 예외가 발생하진 않음. //
        sText01.setText(item.getTitle());
        sText02.setText(item.getAddress());
        sText03.setText(item.getLocation());
        sText04.setText(item.getDate());
        sText05.setText(item.getTime());
        sText06.setText(item.getContact());
        sText07.setText(item.getDataDate());

        // 해당 데이터가 없는 경우 버튼을 활성화 하지 않는다. //
        if(item.getAddress() == null)
            naviBtn.setEnabled(false);
        else
            naviBtn.setEnabled(true);

        if(item.getContact() == null)
            callBtn.setEnabled(false);
        else
            callBtn.setEnabled(true);
    }

    // sText02에 출력 되고 있는 주소 데이터에서 대표 지역명을 regionText에 출력 한다. //
    public void setRegionText()
    {
        CharSequence chars = sText02.getText();
        String str = chars.toString();

        // 앞의 두 글자만 빼서 regionText에 설정 한다. //
        // subSequence(a, b)  a에서 b-1까지의 문자열을 반환 한다. //
        regionText.setText(str.subSequence(0, 2));
    }

    // Favo 버튼의 기능을 바꾸는 메소드이다. text와 리스너를 교체할 수 있다. //
    public void favoButtonSet(String text, View.OnClickListener l)
    {
        favoBtn.setText(text);
        favoBtn.setOnClickListener(l);
    }


    /**
     * 클래스 : ItemButtonListener
     * SoupKitiTemView에 정의된 버튼들을 위한 기본 정의 OnClickListener
     */
    private class ItemButtonListener implements View.OnClickListener {
        static final int FAVO_BTN = 10001;
        static final int NAVI_BTN = 10002;
        static final int CALL_BTN = 10003;

        final int curBtn;

        ItemButtonListener(int curBtn)
        {
            this.curBtn = curBtn;
        }

        public void onClick(View v)
        {
            Intent intent;
            switch(curBtn)
            {
                case FAVO_BTN:
                    FavoDataManager dataManager = FavoDataManager.getInstance(context);
                    // FavoDataManager가 성공적으로 준비 되었다면 파일을 쓴다. //
                    if(dataManager.prepareManager()) {
                        dataManager.writeSoupKitItem(soupItem);
                        Snackbar.make(getRootView(), context.getString(R.string.saveSuccess_itemView),
                                Snackbar.LENGTH_LONG).show();
                    }
                    else
                        Snackbar.make(getRootView(), context.getString(R.string.failSuccess_itemView),
                                Snackbar.LENGTH_LONG).show();
                    break;
                case NAVI_BTN:
                    intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("geo:0,0?q="+soupItem.getAddress()));

                    try {
                        context.startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Snackbar.make(getRootView(), context.getString(R.string.errorNavi_itemView)
                        , Snackbar.LENGTH_LONG).show();
                    }

                    break;
                case CALL_BTN:
                    String tel_tag = "tel:";
                    String data = soupItem.getContact();

                    // 010-1234-5678 -> tel:01012345678
                    // String 수정 메소드는 String을 반환 한다. String 객체는 수정할 수 없기 때문이다. //
                    data = data.replace("-", "");
                    tel_tag = tel_tag.concat(data);
                    intent = new Intent(Intent.ACTION_DIAL, Uri.parse(tel_tag));

                    try {
                        context.startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Snackbar.make(getRootView(), context.getString(R.string.errorCall_itemView)
                                , Snackbar.LENGTH_LONG).show();
                    }

                    break;
            }
        }
    }
}
