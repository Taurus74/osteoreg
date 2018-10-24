package com.aconst.spinareg;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.aconst.spinareg.api.Authorization;
import com.aconst.spinareg.api.References;
import com.aconst.spinareg.authorization.RegisterActivity;
import com.aconst.spinareg.controllers.ClientsController;
import com.aconst.spinareg.controllers.ServiceController;
import com.aconst.spinareg.controllers.SessionController;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class StartActivity extends AppCompatActivity {
    Authorization authorization = new Authorization(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Realm.init(this);

        Realm.setDefaultConfiguration(new RealmConfiguration.Builder()
                .name("osteoreg.realm")
                .deleteRealmIfMigrationNeeded()
                .build()
        );

        PrefsHelper prefsHelper = new PrefsHelper(this);
        String token = prefsHelper.getPref("token");

        if (!token.isEmpty()) {
            new SessionController().deleteSessions().subscribe();
            new ServiceController().deleteServices().subscribe();
            new ClientsController().deleteClients().subscribe();
        }

        Point size = new Point();
        try {
            this.getWindowManager().getDefaultDisplay().getRealSize(size);
            prefsHelper.setPref("screenWidth", size.x);
            prefsHelper.setPref("screenHeight", size.y);
        } catch (NoSuchMethodError e) {
            e.printStackTrace();
        }

        prefsHelper.setPrefLong("dayTime", 0);
        prefsHelper.setPrefLong("weekTime", 0);
        prefsHelper.setPrefLong("monthTime", 0);

        if (token.isEmpty()) {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);

        } else {
            authorization.getCard(token);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        authorization.dispose();
    }
}
