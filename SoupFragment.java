package net.dongyeol.koreasoupkitchen;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * 프래그먼트 클래스 : SoupFragment
 * SoupKitchen Fragment
 */

public class SoupFragment extends Fragment {
    private static final String TAG = "SoupFragment";

    // 대화상자를 의미 하는 상수 //
    private static final int DIALOG_ID_INPUT_REGION = 11001;
    private static final int DIALOG_ID_CHECK_BYINTERNET = 11002;

    Context context;

    private Spinner spinner;
    private TextView localText;

    private Button setBtn;
    private Button curPosBtn;

    private TextView resultText;

    private ListView listView;
    private SoupListAdapter listAdapter;

    private PlaceSpinnerManager spinnerManager;

    private String defRegion = "서울특별시";
    private String region = defRegion;


    // 스피너의 이벤트 처리를 방지할 플래그 //
    private boolean isSpinnerEvent = true;
    private static final int EVENTDELAY = 200;

    // 전체 데이터들을 담을 리스트 (PrepareActivity에 의해 준비 된다.) // 수정이 필요한 코드 (안정성 취약)//
    public static ArrayList<SoupKitItem> soupItemList;
    public static boolean isSoupItemList = false;

    // 특정 지역의 데이터들을 담을 리스트 //
    ArrayList<SoupKitItem> soupItemListCurPos;

    // 다른 스레드에서 메인 스레드에서 순차적으로 처리를 요청 하기 위한 핸들러 //
    Handler                handler;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_soup, container, false);

        soupItemListCurPos = new ArrayList<SoupKitItem>();

        // 핸들러 객체를 생성 한다. //
        handler = new Handler();
        // 액티비티 객체를 context 객체로 사용한다. //
        context = getActivity();

        // 인플레이션 된 리소스 참조 하기 //
        spinner = (Spinner) rootView.findViewById(R.id.spinner_frg_Soup);
        localText = (TextView) rootView.findViewById(R.id.localText_frg_Soup);
        setBtn = (Button) rootView.findViewById(R.id.setBtn_frg_Soup);
        curPosBtn = (Button) rootView.findViewById(R.id.curPosBtn_frg_Soup);
        resultText = (TextView) rootView.findViewById(R.id.resultText_frg_Soup);
        listView = (ListView) rootView.findViewById(R.id.listView_frg_Soup);

        // 리스트뷰를 위한 listAdapter 생성 //
        listAdapter = new SoupListAdapter(context, this);
        listView.setAdapter(listAdapter);

        // 스피너 설정 //
        spinnerManager = new PlaceSpinnerManager(spinner, context);
        spinnerManager.setUserListener(new SpinnerListener());
        spinnerManager.setPlaceSpinner();

        // 해당 버튼에 리스너 설정. //
        setBtn.setOnClickListener(new ItemButtonListener(ItemButtonListener.SET_BTN));
        curPosBtn.setOnClickListener(new ItemButtonListener(ItemButtonListener.CURPOS_BTN));

        return rootView;
    }

    // region 값을 스피너와 텍스트뷰에 설정하고 주소로 포함 된 데이터를 리스트뷰에 출력 하는 메소드 //
    // MainActivity에서 위치 제어를 위해 접근제어를 public으로 설정 함. //
    public void searchSoupByAddress(String keyword) {
        // 1. 스피너와 텍스트뷰 설정. //
        // 위치 갱신 뒤의 UI 설정 //
        // 서울특별시 중구 -> 서울특별시만 추출. //
        if (keyword.contains(" ")) {
            int gapIdx = keyword.indexOf(" ");
            String adminLoc = keyword.substring(0, gapIdx);
            // 서울특별시 중구 -> 중구만 추출. //
            String localLoc = keyword.substring(gapIdx + 1, keyword.length());

            // 1-1. 스피너가 선택 됨에 따라 검색 결과가 초기화 되는 것을 방지 함. //
            // 스피너의 이벤트 처리가 방지되는 영역 //
            isSpinnerEvent = false;

            // 스피너와 텍스트뷰 설정 //
            // adminLoc이 Spinner 목록에 있는 첫 번째 Local 값이 아닐 경우 //
            if(isFirstLocal(adminLoc) == false) {
                setCurLocation(); // 현재위치 조회
                localLoc = adminLoc;
            }

            spinner.setSelection(findSpinnerPosition(adminLoc, 0));
            localText.setText(localLoc);

            // 리스트뷰의 갱신이 다 끝난 뒤(200ms 뒤)에 스피너의 이벤트 활성화 //
            // 스피너에 선택 처리를 하지만 이벤트로 리스트뷰가 초기화 되는 것은
            // 방지하는 것이 목적이다. // 추후 현명한 최적화가 필요한 부분이다. //

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isSpinnerEvent = true;
                }
            }, EVENTDELAY);
        }

        // 2. 리스트뷰에 검색된 급식소 데이터 출력 //
        soupItemListCurPos.clear();

        if (soupItemList != null && soupItemList.size() > 0) {
            for (int i = 0; i < soupItemList.size(); i++) {
                SoupKitItem item = soupItemList.get(i);
                String address = item.getAddress();

                if (address != null)
                    if (address.contains(keyword))
                        soupItemListCurPos.add(item);
            }

            // 리스트뷰의 어댑터에 데이터를 반영 하고 리스트뷰에 대입 한다. //
            listAdapter.setListItems(soupItemListCurPos);
            listAdapter.notifyDataSetChanged();
            // 리스트뷰의 스크롤 값을 초기화 한다. //
            listView.setSelection(0);

            Log.d(TAG, "searchCurPosSoup: " + keyword);
            // resultText로 조회 결과를 출력 한다. //
            resultText.setText(soupItemListCurPos.size() + getString(R.string.resultText_frg_soup));

            // soupItemListCurPos.size() == 0 이라면 구글 검색 제안 대화상자를 클릭한다.
            if(soupItemListCurPos.size() == 0)
                showDialog(DIALOG_ID_CHECK_BYINTERNET, keyword + " " + getString(R.string.requestWordByInternet_frg_soup));
        }
    }

    private boolean isFirstLocal(String str) {
        boolean isFirstLocal = true;
        String[] firstLocArr = spinnerManager.getItems();

        for(int i = 0; i < firstLocArr.length; i++)
            if(firstLocArr[i].equals(str)) {
                isFirstLocal = true;
                return true;
            }
        return false;
    }

    // 전달된 keyword와 같은 Spinner 안에 들은 아이템의 인덱스를 반환 하는데,
    // 같은 값이 없을 경우 defValue로 반환 한다. //
    private int findSpinnerPosition(String keyword, int defValue) {
        String[] items = spinnerManager.getItems();

        int pos = 0;

        for (pos = 0; pos < items.length; pos++)
            if (items[pos].equals(keyword))
                return pos;

        return defValue;
    }


    // 이 프래그먼트에서 출력 되는 대화상자를 관리 함. //
    private void showDialog(int invar, final String ... vargs) {
        switch (invar) {
            // 지역 설정 할 때 출력 하는 대화상자. //
            case DIALOG_ID_INPUT_REGION:
                final LinearLayout linear = (LinearLayout) View.inflate(context, R.layout.dialog_input_region, null);
                final EditText editText = (EditText) linear.findViewById(R.id.editText_dialog_input_region);
                final TextView regText = (TextView) linear.findViewById(R.id.regionText_dialog_input_region);
                final TextView disText = (TextView) linear.findViewById(R.id.districtText_dialog_input_region);

                regText.setText(region);

                // 가장 큰 지역 범위에 따라 대화상자에 출력할 힌트 및 텍스트를 정해서 출력 한다. //
                if (region.charAt((region.length() - 1)) == '시') {
                    editText.setHint(getString(R.string.hint_si_dialog_input_region));
                    disText.setText(getString(R.string.region_si_dialog_input_region));
                } else {
                    editText.setHint(getString(R.string.hint_do_dialog_input_region));
                    disText.setText(getString(R.string.region_do_dialog_input_region));
                }

                // 데이터를 토대로 커스텀 대화상자를 만든다. //
                new AlertDialog.Builder(context).setTitle(getString(R.string.title_dialog_input_region)).setIcon(R.drawable.food_icon)
                        .setView(linear).setPositiveButton(getString(R.string.positiveBtn_dialog_input_region),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                region = regText.getText().toString() + " " + editText.getText().toString();
                                localText.setText(editText.getText().toString());
                                searchSoupByAddress(region);
                            }
                        }).show();
                break;
            case DIALOG_ID_CHECK_BYINTERNET:
                new AlertDialog.Builder(context).setTitle(getString(R.string.title_dialog_check_byinternet)).setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(getString(R.string.message_dialog_check_byinternet))
                        .setPositiveButton(getString(R.string.positiveBtn_dialog_check_byinternet),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                                intent.putExtra(SearchManager.QUERY, vargs[0]);
                                startActivity(intent);
                            }
                        }).setNegativeButton(getString(R.string.negativeBtn_dialog_check_byinternet),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {}
                        }).show();

                break;
        }
    }


    // 별도의 스레드를 생성 하여서 region 변수에 현재 위치 정보를 갱신 한다.
    // MainActivity에서 위치 제어를 위해 접근제어를 public으로 설정 함.
    public void setCurLocation() {
        LocPosStorage locStorage = new LocPosStorage();

        CurLocationManager locManager = CurLocationManager.getInstance(context, locStorage);
        Snackbar.make(getView(), getString(R.string.posSnackbar_content_frg_soup),
                Snackbar.LENGTH_LONG).show();

        // 만약 CurLocationManager에서 문제가 발생해서 준비에 실패 했다면
        // 사용자에게 알리고 위치 정보 갱신을 중단 한다. //
        if (!locManager.prepareManager()) {
            Snackbar.make(getView(), getString(R.string.posSnackbar_fail_frg_soup),
                    Snackbar.LENGTH_LONG).show();
            return;
        }

        // 좌표가 수신 되면 locStorage 안에서 잠든 스레드를 깨운다. //
        locManager.requestLocation();

        // 좌표가 수신 됐을 때 한글 주소 값을 얻을 수 있도록,
        // 별도의 스레드를 생성하여 locStorage 안에서 대기 했다가 한글 주소 값을 갱신할 수 있도록 한다. //
        CurLocThread thread = new CurLocThread(locStorage);
        thread.start();
    }

    /**
     * 클래스 : SpinnerListener
     * 스피너의 아이템이 선택 되었을 때 사용 되는 리스너
     */
    class SpinnerListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if ( isSpinnerEvent ) {
                // 현재 스피너에서 선택된 지역 //
                String curLocal = spinner.getSelectedItem().toString();
                region = curLocal;

                searchSoupByAddress(region);

                // localText에 설정된 텍스트가 있을 경우 초기화 한다. //
                localText.setText("");
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    /**
     * 클래스 : ItemButtonListener
     * SoupFragment에 정의된 버튼들을 위한 OnClickListener
     */
    private class ItemButtonListener implements View.OnClickListener {
        static final int SET_BTN = 10001;
        static final int CURPOS_BTN = 10002;

        final int curBtn;

        ItemButtonListener(int curBtn) {
            this.curBtn = curBtn;
        }

        public void onClick(View v) {
            switch (curBtn) {
                case SET_BTN:
                    region = spinner.getSelectedItem().toString();
                    showDialog(DIALOG_ID_INPUT_REGION);
                    break;
                case CURPOS_BTN:
                    setCurLocation();
                    break;
            }
        }
    }

    /**
     * 클래스 : CurLocThread
     * 현재 위치를 얻기 위한 쓰레드 클래스
     */
    private class CurLocThread extends Thread {
        LocPosStorage locStorage;

        CurLocThread(LocPosStorage locStorage) {
            this.locStorage = locStorage;
        }

        @Override
        public void run() {
            final String regionStr = locStorage.getCurLocation();

            // 위치 조회에 실패 했을 경우 리턴 한다. //
            if ( regionStr == null ) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar.make(getView(), getString(R.string.posSnackbar_fail_frg_soup),
                                Snackbar.LENGTH_LONG).show();
                    }
                });

                return;
            }

            handler.post(new Runnable() {
                @Override
                public void run() {
                    Snackbar.make(getView(),
                            getString(R.string.posSnackbar_success_frg_soup),
                            Snackbar.LENGTH_LONG).show();

                    // 검색 결과로 리스트뷰 갱신 //
                    searchSoupByAddress(regionStr);
                }
            });
        }
    }
}
