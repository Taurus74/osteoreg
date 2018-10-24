package com.aconst.spinareg.authorization;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.aconst.spinareg.MenuHelper;
import com.aconst.spinareg.PrefsHelper;
import com.aconst.spinareg.R;
import com.aconst.spinareg.api.Authorization;

public class ChangePasswdActivity extends AppCompatActivity implements View.OnClickListener {
    private Authorization authorization = new Authorization(this);

    private EditText edPhone;
    private EditText edPassword;
    private EditText edNewPasswd;
    private EditText edNewPasswd2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_passwd);
        setTitle(R.string.menu_change_passwd);

        edPhone = findViewById(R.id.edPhone);
        edPassword = findViewById(R.id.edPassword);
        edNewPasswd = findViewById(R.id.edNewPasswd);
        edNewPasswd2 = findViewById(R.id.edNewPasswd2);

        PrefsHelper prefsHelper = new PrefsHelper(this);
        edPhone.setText(prefsHelper.getPref("phone"));

        Button btnChangePasswd = findViewById(R.id.btnChangePasswd);
        btnChangePasswd.setOnClickListener(this);
    }

    private boolean isPhoneCorrect(String phone) {
        return !phone.isEmpty();
    }

    private boolean isPasswordCorrect(String password) {
        return (!password.isEmpty()) && (password.length() >= 10);
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
        String password = edPassword.getText().toString();
        String newPasswd = edNewPasswd.getText().toString();
        String newPasswd2 = edNewPasswd2.getText().toString();

        if (isPhoneCorrect(phone) && isPasswordCorrect(password)
                && isPasswordCorrect(newPasswd) && isPasswordCorrect(newPasswd2)) {
            if (newPasswd.equals(newPasswd2))
                authorization.changePasswd(phone, password, newPasswd, newPasswd2);
            else
                Toast.makeText(this, "Новый пароль не совпадает с подтверждением пароля",
                        Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Проверьте введенные данные", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        authorization.dispose();
    }
}
