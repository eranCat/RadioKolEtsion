package com.erank.koletsionpods.receivers.phone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class PhoneStateReceiver extends BroadcastReceiver {

    private boolean isAlreadyListening = false;

    @Override
    public void onReceive(Context context, Intent intent) {

//        don't add the listener more than once
        if (!isAlreadyListening) {
            PhoneReceiver phoneReceiver = new PhoneReceiver(context);
            TelephonyManager manager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);

            manager.listen(phoneReceiver, PhoneStateListener.LISTEN_CALL_STATE);
            isAlreadyListening = true;
        }
    }
}
