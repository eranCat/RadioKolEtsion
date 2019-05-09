package com.erank.radiokoletsionv2.receivers.phone;

import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.erank.radiokoletsionv2.activities.MainActivity;

public class PhoneReceiver extends PhoneStateListener {

    private Context context;

    public PhoneReceiver(Context context) {
        this.context = context;
    }

    @Override
    public void onCallStateChanged(int state, String phoneNumber) {
        super.onCallStateChanged(state, phoneNumber);
        switch (state) {
            case TelephonyManager.CALL_STATE_IDLE:
//                toast("idle");
                break;
            case TelephonyManager.CALL_STATE_RINGING:
//                toast("ringing");
                context.startActivity(new Intent(context, MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));//needed to open app from receiver
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
//                toast("offhook- answered");
                break;
        }
    }

    private void toast(String s) {
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }
}
