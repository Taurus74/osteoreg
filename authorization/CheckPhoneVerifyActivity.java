package com.aconst.spinareg.authorization;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.aconst.spinareg.MenuHelper;
import com.aconst.spinareg.R;
import com.aconst.spinareg.api.Authorization;

public class CheckPhoneVerifyActivity extends AppCompatActivity implements View.OnClickListener {
    private Authorization authorization = new Authorization(this);

    private EditText edCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_phone_verify);
        setTitle(R.string.menu_check_phone_verify);

        edCode = findViewById(R.id.edCode);
    }

    private boolean isPhoneCorrect(String phone) {
        return !phone.isEmpty();
    }

    private boolean isCodeCorrect(String code) {
        return !code.isEmpty();
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
        String code = edCode.getText().toString();

        if (isCodeCorrect(code))
            authorization.checkPhoneVerifyCode(code);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        authorization.dispose();
    }
}
