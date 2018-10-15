package net.dongyeol.koreasoupkitchen;

import android.app.Activity;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import net.dongyeol.koreasoupkitchen.fttloperator.FTTLOperator;

import java.util.HashMap;


/**
 * 인터페이스 : ChatBot
 * ChatBot의 기준을 정의한다.
 */
interface ChatBot {
    String  call(final String questionStr);
    String  getName();
    DialogManager getDialogManager();
}

/**
 * 클래스 : VoiceChatBot
 * Android용 VoiceChatBot
 */
class VoiceChatBot implements ChatBot {
    private DialogManager   dialogManager   =   null;
    private VoiceManager    voiceManager    =   null;
    private Activity        activity        =   null;
    private BotEventListener eventListener  =   null;

    private static final String  botName     =   "VoiceChatBot";
    private static final String  myApiKey     =   "ea04c6c52e5b423cb6fefdffb0086eae";

    private String answerStr = null;

    public VoiceChatBot(final Activity activity) {
        // dialogManager 생성 //
        dialogManager   =   DialogManager.getInstance(myApiKey);
        voiceManager    =   new VoiceManager(activity);
        this.activity = activity;
    }

    @Override
    public String call(final String questionStr) {
        try {
            // 봇에 설정한 시작 이벤트가 있을시 호출한다. //
            if(eventListener != null) eventListener.onCall(this, questionStr);

            // 안드로이드에서는 네트워크를 요청하는 소스에 대해선 별도의 Thread에서 진행하여야 함. //
            Thread callThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    answerStr = dialogManager.speak(questionStr);

                    // 네트워크 문제로 인해 dialogManager에서 답변으로 null을 반환한 경우 //
                    if(answerStr == null) {
                        if(eventListener != null) eventListener.onFail();
                        return;
                    }

                    // 답변이 Not avaliable일 경우 기본 질문으로 재시도 해서 답변을 얻어낸다. //
                    while(answerStr.equals(""))
                        answerStr = dialogManager.speak(activity.getString(R.string.defaultQuestion_act_main));

                    // 답변이 앱 기능 명령어가 아닐 때만 tts 출력 함. //
                    if(!(answerStr.substring(0, 1).equals(FTTLOperator.SEPERATOR)))
                        voiceManager.speak(answerStr);
                }
            });

            callThread.start();
            callThread.join();
        } catch(Exception e) {

        }

        Log.d("VoiceChatBot", answerStr);
        return answerStr;
    }

    @Override
    public String getName() {
        return botName;
    }

    @Override
    public DialogManager getDialogManager() {
        return dialogManager;
    }

    public BotEventListener getEventListener() {
        return eventListener;
    }

    public void setEventListener(final BotEventListener eventListener) {
        this.eventListener = eventListener;

        // VoiceManager의 OnDoneListener에 This.BotEventListener의 onDone 이벤트 처리 내용을 설정해서
        // TTS의 speak가 종료될 때 onDone의 이벤트가 처리 되도록 한다.
        voiceManager.setOnDoneListener(new VoiceManager.OnDoneListener() {
            @Override
            public void onDone(VoiceManager voiceManager) {
                eventListener.onDone();
            }
        });
    }

    public void botStop() {
        // VoiceManager의 자원은 반환하여야 함. //
        voiceManager.ttsStop();
    }

    /**
     * 인터페이스 : BotEventListener
     * VoiceChatBot에 이벤트 설정 기능을 제공하기 위한 Nested Interface
     */
    public interface BotEventListener {
        /**
         * 봇이 call 되었을 때 호출되는 콜백 메소드
         */
        void onCall(VoiceChatBot bot, String questionStr);

        /**
         * tts의 출력이 끝났을 때 호출되는 콜백 메소드 VoiceManager(OnDoneListener)
         * -> UtteranceProgressListener -> TTS 쓰레드에서 콜백 메소드 호출.
         */
        void onDone();

        /**
         * 봇이 fail 상태일 때 호출되는 콜백 메소드
         */
        void onFail();
    }
}

/**
 * 클래스 : YamiChatBot
 */
public class YamiChatBot extends VoiceChatBot {
    private Activity        activity        =   null;
    private static final String botName     =   "Yami";

    public YamiChatBot(final Activity activity) {
        super(activity);
        this.activity = activity;
    }

    @Override
    public String call(final String questionStr) {
        String answerStr = super.call(questionStr);
        return answerStr;
    }

    @Override
    public String getName() {
        return this.botName;
    }
}
