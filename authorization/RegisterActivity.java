package com.aconst.spinareg.authorization;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.aconst.spinareg.PrefsHelper;
import com.aconst.spinareg.R;
import com.aconst.spinareg.api.Authorization;
import com.aconst.spinareg.profile.ProfileActivity;

import static com.aconst.spinareg.profile.ProfileActivity.PREF_PROFILE_EMAIL;
import static com.aconst.spinareg.profile.ProfileActivity.PREF_PROFILE_PHONE;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private Authorization authorization = new Authorization(this);

    private PrefsHelper prefsHelper;
    private EditText edPhone;
    private EditText edEMail;
    private EditText edPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setTitle(R.string.title_login);

        edPhone = findViewById(R.id.edPhone);
        edEMail = findViewById(R.id.edEMail);
        edPassword = findViewById(R.id.edPassword);

        prefsHelper = new PrefsHelper(this);
        edPhone.setText(prefsHelper.getPref(PREF_PROFILE_PHONE));
        edEMail.setText(prefsHelper.getPref(PREF_PROFILE_EMAIL));

        final Button btnSignIn = findViewById(R.id.btnSignIn);
        TextView tvPassForget = findViewById(R.id.tvPassForget);
        final Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvContinue = findViewById(R.id.tvContinue);

        btnSignIn.setOnClickListener(this);
        tvPassForget.setOnClickListener(this);
        btnRegister.setOnClickListener(this);
        tvContinue.setOnClickListener(this);

        edPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                String s1 = s.toString();
                if (s1.startsWith("8"))
                    s1 = "+7" + s1.substring(1);
                if (!s1.equals(edPhone.getText().toString()))
                    edPhone.setText(s1);
            }
        });

//        btnSignIn.setEnabled(false);
//        btnRegister.setEnabled(false);
//
//        Observable<String> phoneObservable = RxEditText.getTextWatcherObservable(edPhone);
////        Observable<String> emailObservable = RxEditText.getTextWatcherObservable(edEMail);
//        Observable<String> passwordObservable = RxEditText.getTextWatcherObservable(edPassword);
//
//        Observable.combineLatest(phoneObservable, passwordObservable,
//                new BiFunction<String, String, Boolean>() {
//                    @Override
//                    public Boolean apply(String s, String s2) {
//                        return isPhoneCorrect(s) && isPasswordCorrect(s2);
//                    }
//                })
//                .subscribe(new Consumer<Object>() {
//                    @Override
//                    public void accept(Object o) {
//                        btnSignIn.setEnabled((Boolean) o);
//                        btnRegister.setEnabled((Boolean) o);
//                    }
//                });
    }

    private boolean isPhoneCorrect(String phone) {
        return !phone.isEmpty();
    }

    private boolean isEMailCorrect(String email) {
        return !email.isEmpty();
    }

    private boolean isPasswordCorrect(String password) {
        return (!password.isEmpty()) && (password.length() >= 10);
    }

    @Override
    public void onClick(View v) {
        String phone = edPhone.getText().toString();
//        String email = edEMail.getText().toString();
        String password = edPassword.getText().toString();

        switch (v.getId()) {
            case R.id.btnSignIn :
/*                if (phone.isEmpty() && isEMailCorrect(email) && isPasswordCorrect(password)) {
                    edPhone.setVisibility(View.VISIBLE);
                    edPhone.requestFocus();
                    Toast.makeText(this, "Для входа по ранее созданной "
                            + "учетной записи укажите номер телефона", Toast.LENGTH_SHORT).show();
                }

                else */if (isPhoneCorrect(phone) && isPasswordCorrect(password)) {
                    prefsHelper.setPref(PREF_PROFILE_PHONE, phone);
//                    prefsHelper.setPref(PREF_PROFILE_EMAIL, email);

                    String pushToken = prefsHelper.getPref("pushToken");
                    authorization.authentication(phone, password, pushToken);
                    finish();
                }

                else
                    Toast.makeText(this, "Проверьте введенные данные",
                            Toast.LENGTH_SHORT).show();
                break;

            case R.id.tvPassForget :
                Intent intent = new Intent(this, ResetPasswdActivity.class);
                startActivity(intent);
                break;

            case R.id.btnRegister :
                if (/*isEMailCorrect(email) && */isPasswordCorrect(password)) {
//                    prefsHelper.setPref(PREF_PROFILE_EMAIL, email);
                    prefsHelper.setPref("password", password);

                    Intent intentProfile = new Intent(this, ProfileActivity.class);
                    startActivity(intentProfile);

                } else {
                    Toast.makeText(this, "E-mail и пароль должны быть заполнены, "
                            + "пароль не менее 10 символов",
                            Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.tvContinue :
                Toast.makeText(this, "Функция в разработке...", Toast.LENGTH_SHORT).show();
                break;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        authorization.dispose();
    }

}
