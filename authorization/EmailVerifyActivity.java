package com.aconst.spinareg.authorization;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.aconst.spinareg.MenuHelper;
import com.aconst.spinareg.PrefsHelper;
import com.aconst.spinareg.R;
import com.aconst.spinareg.api.Authorization;

import static com.aconst.spinareg.profile.ProfileActivity.PREF_PROFILE_EMAIL;

public class EmailVerifyActivity extends AppCompatActivity implements View.OnClickListener {
    private Authorization authorization = new Authorization(this);

    private EditText edEMail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verify);
        setTitle(R.string.menu_email_verify);

        edEMail = findViewById(R.id.edEMail);

        PrefsHelper prefsHelper = new PrefsHelper(this);
        edEMail.setText(prefsHelper.getPref(PREF_PROFILE_EMAIL));

        Button btnVerifyEMail = findViewById(R.id.btnVerifyEMail);

        btnVerifyEMail.setOnClickListener(this);

    }

    private boolean isEMailCorrect(String email) {
        return !email.isEmpty();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MenuHelper menuHelper = new MenuHelper(this);
        return menuHelper.processMenu(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        String email = edEMail.getText().toString();
        if (isEMailCorrect(email)) {
            PrefsHelper prefsHelper = new PrefsHelper(this);
            String phone = prefsHelper.getPref("phone");
            authorization.getEMailVerifyCode(phone);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        authorization.dispose();
    }
}
