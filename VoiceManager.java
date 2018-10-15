package net.dongyeol.koreasoupkitchen;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.HashMap;
import java.util.Locale;

/**
 * 클래스 : VoiceManager
 * TTS의 speak가 시작 또는 종료될 때 이벤트를 처리할 수 있는 기능을 제공 함.
 */

public class VoiceManager extends UtteranceProgressListener implements TextToSpeech.OnInitListener {
    TextToSpeech tts;
    OnDoneListener onDoneListener;

    public VoiceManager(Context context)
    {
        tts = new TextToSpeech(context, this);
    }

    @Override
    public void onInit(int status) {
        int result  =   tts.setLanguage(Locale.KOREA);

        if(status == TextToSpeech.SUCCESS)
        {
            if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
                Log.d("tts", "이 언어는 음성합성이 불가능하다.");
            else
                Log.d("tts", "이 언어는 음성합성이 가능하다.");
            tts.setOnUtteranceProgressListener(this);
        } else {
            Log.d("tts", "tts 초기화에 실패 하였다.");
        }
    }

    void speak(String str) {
        // UtteranceProgressListener 콜백 메소드가 호출 되도록 ID값을 지정하고 speak 한다.  //
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
        tts.speak(str, TextToSpeech.QUEUE_ADD, map);
    }

    void ttsStop()
    {
        tts.stop();
        tts.shutdown();
    }

    public OnDoneListener getOnDoneListener() {
        return onDoneListener;
    }

    /**
     * VoiceManager(this).OnDoneListener의 Setter 메소드
     * @param onDoneListener
     */
    public void setOnDoneListener(OnDoneListener onDoneListener) { this.onDoneListener = onDoneListener; }

    // UtteranceProgressListener의 Abstract 메소드 //
    // UtteranceProgressListener는 TTS의 실행주기 별 콜백 메소드를 제공하는 추상화 클래스이다.
    @Override
    public void onError(String s) {}

    @Override
    public void onStart(String s) {}

    @Override
    public void onDone(String s) {
        if(onDoneListener != null) onDoneListener.onDone(this);
    }

    /**
     * 인터페이스 : OnDoneListener
     * speak가 종료되었을 때 이벤트를 설정하기 위한 Nested Interface
     */
    interface OnDoneListener {
        void onDone(VoiceManager voiceManager);
    }
}

