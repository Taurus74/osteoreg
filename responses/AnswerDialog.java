package com.aconst.spinareg.responses;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.aconst.spinareg.R;
import com.aconst.spinareg.api.Response;

public class AnswerActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView tvAnswer;
    private int sessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer);
        setTitle(R.string.response_answer);

        Intent intent = getIntent();
        sessionId = intent.getIntExtra("sessionId", 0);

        tvAnswer = findViewById(R.id.tvAnswer);
        findViewById(R.id.btnCancel).setOnClickListener(this);
        findViewById(R.id.btnOk).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnOk) {
            if (tvAnswer.getText().toString().isEmpty())
                Toast.makeText(this, R.string.prompt_empty_answer, Toast.LENGTH_SHORT).show();
            else {
                Intent intent = new Intent();
                intent.putExtra("answer", tvAnswer.getText().toString());
                intent.putExtra("sessionId", sessionId);
                setResult(RESULT_OK);
//                finish();
            }
        }
//        else {
//            setResult(RESULT_CANCELED);
//        }
        finish();
    }
}
