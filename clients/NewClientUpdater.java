package com.aconst.spinareg.clients;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ClientsUIUpdater extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.sendBroadcast(new Intent("Update_clients_UI"));
    }
}
