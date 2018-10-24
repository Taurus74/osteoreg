package com.aconst.spinareg.responces;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.aconst.spinareg.R;
import com.aconst.spinareg.adapters.ResponseAdapter;
import com.aconst.spinareg.api.Response;
import com.aconst.spinareg.model.SessionResponse;

import java.util.LinkedList;
import java.util.List;

public class ResponsesActivity extends AppCompatActivity implements Response.SetResponses {
    private List<SessionResponse> responseList = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_responses);
        setTitle(R.string.title_responses);

        Response response = new Response(this);
        ResponseAdapter adapter = new ResponseAdapter(responseList);
        RecyclerView rvResponses = findViewById(R.id.rvResponses);
        rvResponses.setAdapter(adapter);
        rvResponses.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void sessionResponse(List<SessionResponse> sessionResponses) {
        responseList = sessionResponses;
    }

    @Override
    public void responce(String responseStatus) {

    }
}
