package com.aconst.spinareg;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import static com.aconst.spinareg.profile.ProfileActivity.PREF_PROFILE_NAME1;
import static com.aconst.spinareg.profile.ProfileActivity.PREF_PROFILE_NAME2;
import static com.aconst.spinareg.profile.ProfileActivity.PREF_PROFILE_NAME3;

public class NameActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView tvName1;
    private TextView tvName2;
    private TextView tvName3;

    private String paramName1;
    private String paramName2;
    private String paramName3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_name);
        setTitle(R.string.title_name);

        Intent intent = getIntent();

        if (intent.hasExtra(PREF_PROFILE_NAME1)) {
            paramName1 = PREF_PROFILE_NAME1;
            paramName2 = PREF_PROFILE_NAME2;
            paramName3 = PREF_PROFILE_NAME3;

        } else {
            paramName1 = "clientName1";
            paramName2 = "clientName2";
            paramName3 = "clientName3";
        }

        tvName1 = findViewById(R.id.tvLastName);
        tvName1.setText(intent.getStringExtra(paramName1));

        tvName2 = findViewById(R.id.tvFirstName);
        tvName2.setText(intent.getStringExtra(paramName2));

        tvName3 = findViewById(R.id.tvSecondName);
        tvName3.setText(intent.getStringExtra(paramName3));

        findViewById(R.id.btnCancel).setOnClickListener(this);
        findViewById(R.id.btnOk).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        if (v.getId() == R.id.btnOk) {
            View emptyView = checkFields();
            if (emptyView == null) {
                intent.putExtra(paramName1, tvName1.getText().toString());
                intent.putExtra(paramName2, tvName2.getText().toString());
                intent.putExtra(paramName3, tvName3.getText().toString());
                setResult(RESULT_OK, intent);
                finish();

            } else {
                emptyView.requestFocus();
                Toast.makeText(this, "Необходимое поле не заполнено", Toast.LENGTH_SHORT).show();
            }

        } else {
            setResult(RESULT_CANCELED, intent);
            finish();
        }
    }

    private View checkFields() {
        if (tvName1.getText().toString().isEmpty())
            return findViewById(R.id.tvLastName);
        else if (tvName2.getText().toString().isEmpty())
            return findViewById(R.id.tvFirstName);
        else
            return null;
    }

}
