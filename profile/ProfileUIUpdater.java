package com.aconst.spinareg.calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SessionUIUpdater extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.sendBroadcast(new Intent("Update_session_UI"));
    }
}
