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

public class ResetPasswdActivity extends AppCompatActivity implements View.OnClickListener {
    private Authorization authorization = new Authorization(this);

    private EditText edPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_passwd);
        setTitle(R.string.menu_reset_passwd);

        edPhone = findViewById(R.id.edPhone);

        PrefsHelper prefsHelper = new PrefsHelper(this);
        edPhone.setText(prefsHelper.getPref("phone"));

        Button btnResetPasswd = findViewById(R.id.btnResetPasswd);
        btnResetPasswd.setOnClickListener(this);
    }

    private boolean isPhoneCorrect(String phone) {
        return !phone.isEmpty();
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
        String phone = edPhone.getText().toString();

        if (isPhoneCorrect(phone))
            authorization.resetPasswdRequest(phone);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        authorization.dispose();
    }
}
