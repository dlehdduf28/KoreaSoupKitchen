package net.dongyeol.koreasoupkitchen;


import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import net.dongyeol.koreasoupkitchen.fttloperator.FTTLOperator;

import java.util.ArrayList;
import java.util.Iterator;



/*
*   프로젝트 이 름 : 공짜 급식소 AI
*   제 작 자      : 이 동 열 (dlehdduf28@gmail.com)
*   수 정 사 항 : v1.0.1 (verC:2) 버그 수정   데이터 : #
*                v1.1   (verC:3) InfoActivity 추가.  데이터 : #
*
*                v1.1.1 (verC:4) CurLocationManager android.location.Address 객체의
*                       getAdminArea()의 null 반환에 따른 버그 수정. 주석 "버그 수정#1" 참고
*                       데이터 : # (v1.1과 동일)
*
*                v1.1.2 (verC:5) CurLocationManager android.location.Address 객체의
*                       getAdminArea()에서 null이 반환되지 않아 생기는 버그 수정. 주석 "버그 수정#1" 참고
*                       데이터 : #
*
*                v1.2   (verC:6) Yami AI 업데이트
*                       20180915 getAdminArea()에서 null이 반환되지 않아 생기는 버그 수정. 주석 "버그 수정#1" 참고
*                       20180925 Implement Yami AI using Google Dialogflow.
*                       20181009 시연용 Ball AI 구현
*                       데이터 : 20180901 (v1.1.2와 동일)
*/

/**
 * 액티비티 클래스 : MainActivity
 */
public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";

    private TabLayout   tabLayout;
    private ViewPager   viewPager;
    private Button      yamiButton;
    ImageView   yamiImageView;

    private PagerAdapter pagerAdapter;

    SoupFragment soupFragment;
    FavoFragment favoFragment;

    // Yami 객체 //
    YamiChatBot yamiChatBot = null;
    MediaButtonReceiver mediaButtonReceiver;
    // 리스너 데이터를 담을 수 없는 MediaButtonReceiver 대신 MainActivity가 static으로 담는다. //
    static MediaButtonReceiver.OnEventListener onEventListenerForMediaBtn; // Poor code....//

    private MainSegment mainSegment = new MainSegment(this);

    // 외부 쓰레드(VoiceManager)에서 MainActivity를 위한 이벤트를 처리할 때 핸들러가 사용 됨. //
    // 1. Yami AI 초기화 과정에서 종료 이벤트를 설정할 때 핸들러가 사용 됨.
    private Handler     handler     = new Handler();

    // 야미 AI의 활성화 여부 //
    boolean isYami  =   true;

    // 음성인식을 위한 권한 활성화 여부 //
    boolean perCheckOfAudioRecord = false;

    // Invariable for request/result //
    private static final int DIALOG_ID_INPUT_BOT = 11001;
    private static final int REQUESTNUM_PER_AUDIO_RECORD = 3; // FavoDataManager.REQUEST_PER가 1임. // CurLocationManager.REQUEST_PER가 2임.
    private static final int RESULTNUM_ACT_SPEECH = 20001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = (ViewPager) findViewById(R.id.viewPager_act_main);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout_act_main);
        yamiButton = (Button) findViewById(R.id.button_act_main);
        yamiImageView = (ImageView) findViewById(R.id.yamiImageView_act_main);

        // 기본적으로 Yami AI는 비활성화 된다. //
        yamiButton.setVisibility(View.INVISIBLE);
        // 야미 AI의 활성화 (isYami가 true일 경우 활성화 된다.)
        yamiInit();

        soupFragment = new SoupFragment();
        favoFragment = new FavoFragment();

        // 탭 레이아웃과 뷰페이저(ADSL)의 초기화 및 설정//
        pagerAdapter = new FixedTabspagerAdapter();
        viewPager.setAdapter(pagerAdapter);

        viewPager.addOnPageChangeListener(new OnViewChangeListener());

        tabLayout.setupWithViewPager(viewPager);

        // 미디어 버튼 인식 초기화 //
        initMediaButton();
    }


    /**
     * Yami AI 초기화 in 2018-09
     */
    private void yamiInit() {
        if(!isYami)
            return;

        yamiChatBot = new YamiChatBot(this);

        yamiButton.setVisibility(View.VISIBLE);

        // Yami 이미지뷰를 터치했을 때 사라질 수 있도록 이벤트 설정 //
        yamiImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                yamiImageView.setVisibility(View.INVISIBLE);
            }
        });

        // AI 호출 버튼을 눌렀을 때 Yami 출력 및 메시지 입력 대화상자 출력 //
        yamiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Yami 출력 //
                yamiImageView.setVisibility(View.VISIBLE);
                // 봇에게 전달할 메시지 입력 대화상자 출력 //
                showDialog(DIALOG_ID_INPUT_BOT);
            }
        });

        // YamiChatBot에게 시작 이벤트와 종료 이벤트 설정 //
        yamiChatBot.setEventListener(new YamiChatBot.BotEventListener() {
            @Override
            public void onCall(VoiceChatBot bot, String questionStr) {}

            @Override
            public void onDone() {
                // event가 끝나면 Yami 출력을 없앤다. //
                // VoiceChatBot(BotEventListener) -> VoiceManager(OnDoneListener) -> UtteranceProgressListener
                // -> TTS를 출력하는 외부 쓰레드에서 콜백 메소드를 호출해서 onDone() 내의 이벤트가 처리 됨.
                // 따라서 아래 이벤트 처리의 UI 처리는 메인 쓰레드의 핸들러를 이용하여서 처리해야 함.
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        hideYami();
                    }
                });
            }

            // 야미 초기화에 실패했을시 호출되는 콜백메소드 //
            @Override
            public void onFail() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar.make(getCurrentFocus(), getString(R.string.yamiError_act_main),
                                Snackbar.LENGTH_LONG).show();
                        hideYami();
                    }
                });
            }
        });
    }

    /**
     * Yami가 출력 되어 있을 경우 서서히 사라지는 애니메이션을 출력하며 Yami를 숨긴다.
     */
    public void hideYami() {
        if(!isYami || yamiImageView.getVisibility() == View.INVISIBLE)
            return;

        Animation yamiHideAnim = AnimationUtils.loadAnimation(this, R.anim.yami_hide);
        yamiHideAnim.setAnimationListener(new YamiHideAnimationListener());
        yamiImageView.startAnimation(yamiHideAnim);
    }

    // Yami AI를 위한 소스 끝 in 2018-09 //

    // 이어폰이나 블루투스 스피커의 제어를 인식받기 위한 메소드 //
    /**
     * MediaButton의 제어를 인식하기 위한 초기화 메소드
     */
    private void initMediaButton() {
        // 정의한 브로드캐스트 리시버를 시스템보다 우선순위가 높게 OS에 등록한다. //
        // 미디어 버튼 리시버에 이벤트가 발생됐을 때 음성인식을 실행한다. //

        mediaButtonReceiver = new MediaButtonReceiver();
        onEventListenerForMediaBtn = new MediaButtonReceiver.OnEventListener() { // Poor code....//
            @Override
            public void onEvent() {
                speechRecognize();

            }
        };

        IntentFilter mediaFilter = new IntentFilter(Intent.ACTION_MEDIA_BUTTON);
        mediaFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY + 1);
        registerReceiver(mediaButtonReceiver, mediaFilter);

        // AudioManager에 정의한 브로드캐스트 리시버를 MediaButtonEventReceiver로 등록한다.
        ComponentName componentName = new ComponentName(this, MediaButtonReceiver.class);
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.registerMediaButtonEventReceiver(componentName);


    }

    /**
     * Audio Record 위험 권한의 승인 여부를 확인하고 승인이 안 되어 있다면 승인 받는다.
     */
    private void receivePermissionForAR()
    {
        // 마시멜로 이상일 경우 권한을 자바 코드 상에서 요청하여야 한다. //
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 스토리지 접근 권한이 허가 되어 있는지 확인 한다. //
            int arPerCheck = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.RECORD_AUDIO);

            if (arPerCheck == PackageManager.PERMISSION_GRANTED)
            {
                Log.d(TAG, "음성 녹음 권한 있음.");
                perCheckOfAudioRecord = true;
            } else {
                Log.d(TAG, "음성 녹음 권한 없음.");
                perCheckOfAudioRecord = false;
                // 사용자에게 READ, WRITE 권한을 요청하는 대화상자를 띄운다. //
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.RECORD_AUDIO}, REQUESTNUM_PER_AUDIO_RECORD);
            }
        } else { // 마시멜로 이전의 환경의 경우엔 허용 절차를 취하지 않아도 되므로 허용한 것으로 처리 한다.
            perCheckOfAudioRecord = true;
        }
    }

    /**
     * 음성인식을 호출한다.
     */
    private void speechRecognize() {
        receivePermissionForAR();
        if(perCheckOfAudioRecord) {
            // 음성인식 객체 생성 및 리스너 등록 //
            final SpeechRecognizer stt = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
            stt.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle bundle) {
                }

                @Override
                public void onBeginningOfSpeech() {
                }

                @Override
                public void onRmsChanged(float v) {
                }

                @Override
                public void onBufferReceived(byte[] bytes) {
                }

                @Override
                public void onEndOfSpeech() {
                }

                @Override
                public void onError(int i) {
                }

                @Override
                public void onResults(Bundle bundle) {
                }

                @Override
                public void onPartialResults(Bundle bundle) {
                }

                @Override
                public void onEvent(int i, Bundle bundle) {
                }
            });

            // 음성인식 액티비티 출력을 위한 인텐트 생성 //
            final Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);

            Log.d("MainActivity", "스피치 시작");
            startActivityForResult(recognizerIntent, RESULTNUM_ACT_SPEECH);
        }
    }

    /**
     * 권한 요청 액티비티를 띄웠을 때 결과 값을 받는 콜백 메소드이다.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case FavoDataManager.REQUESTNUM_PER: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // FavoDataManager에게 권한이 승인 되었음을 알린다. //
                    FavoDataManager.setPermissionCheck(true);
                    Snackbar.make(getCurrentFocus(),
                            getString(R.string.savToast_perSuccess), Snackbar.LENGTH_LONG).show();
                }
                else {
                    FavoDataManager.setPermissionCheck(false);
                    Snackbar.make(getCurrentFocus(),
                            getString(R.string.savToast_perFail), Snackbar.LENGTH_LONG).show();
                }

                break;
            }
            case CurLocationManager.REQUESTNUM_PER: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // FavoDataManager에게 권한이 승인 되었음을 알린다. //
                    CurLocationManager.setPermissionCheck(true);

                    Snackbar.make(getCurrentFocus(),
                            getString(R.string.posToast_perSuccess), Snackbar.LENGTH_LONG).show();
                }
                else {
                    CurLocationManager.setPermissionCheck(false);

                    Snackbar.make(getCurrentFocus(),
                            getString(R.string.posToast_perFail), Snackbar.LENGTH_LONG).show();
                }

                break;
            }
            case REQUESTNUM_PER_AUDIO_RECORD: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    perCheckOfAudioRecord = true;
                else {
                    perCheckOfAudioRecord = false;
                    Snackbar.make(getCurrentFocus(),
                            getString(R.string.audioToast_perFail), Snackbar.LENGTH_LONG).show();
                }
            }
        }

        // 권한이 제대로 요청 되었는지 디버깅 해본다. //
        /*
        Log.d(TAG, permissions[0] + " " + grantResults[0] + "\n"
                + permissions[1] + " " + grantResults[1]);
        */
    }


    /**
     * 대화상자를 만드는 메소드이다. 이 메소드에 정의된 대화상자 ID는 showDialog(ID)로 띄울 수 있다.
     * @param id
     * @param args
     * @return
     */
    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        AlertDialog dialog = null;

        switch (id) {
            case DIALOG_ID_INPUT_BOT: // BOT 대화상자 호출 //
            {
                final LinearLayout dialogLayout = (LinearLayout) View.inflate(this, R.layout.dialog_input_bot, null);
                final EditText editText = (EditText) dialogLayout.findViewById(R.id.editText_dialog_input_bot);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                // 대화상자 설정 //
                builder.setTitle(getString(R.string.title_dialog_input_bot));
                builder.setMessage(getString(R.string.message_dialog_input_bot)); // 내용 설정
                builder.setIcon(android.R.drawable.ic_dialog_email);
                builder.setView(dialogLayout);

                // Text 입력 버튼 //
                builder.setPositiveButton(getString(R.string.positiveBtn_dialog_input_bot), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // 입력된 Text로 Yami AI의 답변을 출력한다. //
                        mainSegment.operateOfAI(editText.getText().toString().equals("")
                                ? getString(R.string.defaultQuestion_act_main)
                                : editText.getText().toString());
                        // 대화상자가 재활용 될 때 이전에 입력했던 질문이 남아있는 버그를 수정하기 위해 "" 입력. //
                        editText.setText("");
                    }
                });

                // 음성인식 호출 버튼 //
                builder.setNeutralButton(getString(R.string.neutralBtn_dialog_input_bot), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        speechRecognize();
                    }
                });

                dialog = builder.create();
                break;
            }
        }

        return dialog;
    }

    /**
     * 결과값을 받아야 하는 액티비티를 호출했을 때 결과값을 받는 콜백 메소드이다.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case RESULTNUM_ACT_SPEECH:
                // 음성인식이 성공적으로 되었을 경우 인식된 질문을 AI에게 전달한다. //
                if(resultCode == RESULT_OK && data != null) {
                    mainSegment.operateOfAI(data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0).equals("")
                            ? getString(R.string.defaultQuestion_act_main)
                            : data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0));
                }
                break;
            case CameraManager.REQUEST_IMAGE_CAPTURE:
                // 카메라 사진을 다 찍었을 때 처리 //
                Snackbar.make(getCurrentFocus(), getString(R.string.completeMakePhoto_act_main),
                        Snackbar.LENGTH_LONG).show();
                break;
        }
    }

    /**
     * R.menu.main에 정의된 메뉴를 만드는 메소드
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * 메뉴의 아이템이 선택 되었을 때 호출되는 메소드
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int curId = item.getItemId();

        switch(curId) {
            case R.id.menu_main:
                Intent intent = new Intent(this, InfoActivity.class);
                startActivity(intent);
                break;
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(isYami != false) yamiChatBot.botStop();
        unregisterReceiver(mediaButtonReceiver);
    }


    // 뷰페이저 및 구성을 위한 소스 //

    /**
     * 클래스 : 탭페이저 어댑터
     */
    class FixedTabspagerAdapter extends FragmentPagerAdapter {
        // PagerAdapter는 액티비티의 FragmentManager를 요구 한다.
        public FixedTabspagerAdapter() {
            super(getSupportFragmentManager());
        }

        // 프래그먼트의 갯수를 반환 합니다. //
        @Override
        public int getCount() {
            return 2;
        }

        // 각각의 페이지에 저장될 프래그먼트의 참조값을 idx 값을 기준으로 반환 한다. //
        // 각각의 위치에 연결된 프래그먼트가 반환 된다. //
        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0:
                    return soupFragment;
                case 1:
                    return favoFragment;
                default:
                    return null;
            }
        }

        // CharSequence는 String의 상위 클래스
        // 각각의 페이지의 타이틀을 문자열로 반환하는 메소드이다.
        // 탭 레이아웃의 페이지 이름의 명시에 활용 된다.
        @Override
        public CharSequence getPageTitle(int position) {
            switch(position) {
                case 0:
                    return getString(R.string.fragment_soup);
                case 1:
                    return getString(R.string.fragment_favo);
                default:
                    return null;
            }
        }
    }

    /**
     * 클래스 : OnViewChangeListener
     * 탭페이저 리스너
     */
    class OnViewChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            switch(position)
            {
                case 1:
                    favoFragment.setListView();
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    /**
     * final Inner 클래스 : YamiHideAnimationListener
     * 야미 AI의 애니메이션이 끝날 때 야미 이미지뷰를 숨기기 위해 생성한 리스너.
     */
    private final class YamiHideAnimationListener implements Animation.AnimationListener {
        @Override
        public void onAnimationStart(Animation animation) {}

        @Override
        public void onAnimationEnd(Animation animation) {
            yamiImageView.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {}
    }
}

/**
 * 세그먼트 클래스 : MainSegment
 * MainActivity에서 Yami AI의 실행 부분을 담당하는 세그먼트 클래스
 */
class MainSegment {

    MainActivity mainActivity;

    private static final int APP_INDEX = 0;
    private static final int SECTION_INDEX = 1;

    MainSegment(MainActivity activity) {
        this.mainActivity = activity;
    }

    /**
     * Yami 대화상자로 질문이 입력되었을 때 답변을 받아서 수행하는 메소드
     * FTTL 명령어(프로토콜)로 기능을 수행하거나 자연어 답변을 출력한다.
     * 앱의 제어를 수행하기 때문에 ChatBot의 역할이 아닌 MainSegment의 역할임.
     * @param question  질문
     */
    void operateOfAI(String question) {
        try {
            String answerStr = mainActivity.yamiChatBot.call(question);

            // 답변의 첫 시작이 앱 기능 호출일 경우 (답변이 SEPERATOR로 시작할 경우) //
            if (answerStr.substring(0, 1).equals(FTTLOperator.SEPERATOR)) {
                FTTLOperator fttlOperator = new FTTLOperator() {
                    @Override // FTTLOperator는 operate 메소드를 통해 전달된 FTTL 명령어를 토큰 단위로 제공한다. //
                    protected void execute(ArrayList<String> tokenList) {
                        switch (tokenList.get(SECTION_INDEX)) {
                            case "<FindByCurLocation>": {
                                mainActivity.soupFragment.setCurLocation();
                                break;
                            }
                            case "<FindByRegion>":
                            case "<FindByAddress>": {
                                // 토큰을 합쳐서 하나의 주소 문자열로 만든다. //
                                String region = "";

                                Iterator<String> itr = tokenList.iterator();
                                while (itr.hasNext()) {
                                    String token = itr.next();
                                        if (token.equals("<App>") || token.equals("<FindByRegion>") ||
                                            token.equals("<FindByAddress>") || token.equals("<Token>"))
                                        continue;
                                    region += token + " ";
                                }

                                // SoupFragment에게 주소 검색을 요청한다. //
                                mainActivity.soupFragment.searchSoupByAddress(region);
                                break;
                            }
                            case "<FindByName>": { // ▶<App>▶<FindByName>▶<Token>▶동촌1종합사회복지관

                                break;
                            }
                            case "<Function>": {            // ▶<App>▶<Function>
                                switch(tokenList.get(SECTION_INDEX+1)) {
                                    case "<Call>": {        // ▶<App>▶<Function>▶<Call>
                                        switch(tokenList.get(SECTION_INDEX+2)) {
                                            case "<FindByMap>": {     // ▶<App>▶<Function>▶<Call>▶<FindByMap>▶<Token>▶동촌1종합사회복지관
                                                if (tokenList.get(SECTION_INDEX + 3).equals("<Token>")) {
                                                    Intent intent = new Intent();
                                                    intent.setAction(Intent.ACTION_VIEW);
                                                    intent.setData(Uri.parse("geo:0,0?q=" + tokenList.get(SECTION_INDEX + 4)));
                                                    mainActivity.startActivity(intent);
                                                }
                                                break;
                                            }
                                            case "<Camera_Photo>": {
                                                CameraManager cameraManager = new CameraManager(mainActivity);
                                                cameraManager.requestCamera();
                                                break;
                                            }
                                            case "<Camera_Movie>": {
                                                Intent intent = new Intent();
                                                intent.setAction("android.provider.MediaStore.ACTION_VIDEO_CAPTURE");
                                                mainActivity.startActivity(intent);
                                                break;
                                            }
                                            case "<Contact>": {
                                                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                                                mainActivity.startActivity(intent);
                                                break;
                                            }
                                            case "<UpdateApp>": {
                                                Uri url = Uri.parse(InfoActivity.APP_LINK);
                                                Intent intent = new Intent(Intent.ACTION_VIEW, url);
                                                mainActivity.startActivity(intent);
                                                break;
                                            }
                                            case "<Internet>": { // ▶<App>▶<Function>▶<Call>▶<Internet>▶<Token>▶네이버
                                                if(tokenList.get(SECTION_INDEX + 3).equals("<Token>")) {
                                                    // 구글 검색 인텐트 //
                                                    Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                                                    intent.putExtra(SearchManager.QUERY, tokenList.get(SECTION_INDEX + 4));
                                                    mainActivity.startActivity(intent);
                                                }
                                                break;
                                            }
                                        }
                                        break;
                                    }
                                }
                                break;
                            }
                        }
                    }
                };

                fttlOperator.operate(answerStr);
                mainActivity.hideYami();
            } else { // 문장형 답변일 경우 //
                Toast toast = Toast.makeText(mainActivity, answerStr, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, -20, -30);
                toast.show();
            }
        } catch (ActivityNotFoundException e) {
            Snackbar.make(mainActivity.getCurrentFocus(),
                    mainActivity.getString(R.string.notFoundActivity_act_main), Snackbar.LENGTH_LONG).show();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}