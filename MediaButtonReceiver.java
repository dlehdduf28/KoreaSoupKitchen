package net.dongyeol.koreasoupkitchen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

/**
 * 클래스 : MediaButtonReceiver
 * 미디어 버튼 인식을 위한 브로드캐스트 리시버
 */

public class MediaButtonReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
            KeyEvent keyEvent = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

            if(keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                MainActivity.onEventListenerForMediaBtn.onEvent(); // Poor code....//
                Log.d("MediaButtonReceiver", "미디어 버튼이 눌렸습니다.");
            }
        }
        // abortBroadcast(); 수신한 Broadcast를 지움으로써 다른 앱의 리시버에 이 Action의 인텐트가 가는 것을 방지.
    }

    /**
     * 이벤트 처리를 위한 Nested Interface
     */
    interface OnEventListener {
        void onEvent();
    }
}
