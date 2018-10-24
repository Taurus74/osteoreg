package com.aconst.spinareg.clients;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.aconst.spinareg.CalendarHelper;
import com.aconst.spinareg.Common;
import com.aconst.spinareg.PrefsHelper;
import com.aconst.spinareg.R;
import com.aconst.spinareg.adapters.PhotosAdapter;
import com.aconst.spinareg.api.Clients;
import com.aconst.spinareg.profile.ProfileAddressActivity;
import com.aconst.spinareg.NameActivity;
import com.aconst.spinareg.sessions.ConfirmDelPhotoDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class NewClientActivity extends AppCompatActivity
        implements View.OnClickListener, ConfirmDelPhotoDialog.NoticeDialogListener {
    private final static int RC_GET_NAME = 1;
    private final static int RC_GET_AVATAR = 2;
    private final static int RC_GET_PHOTO = 3;

    private String clientName1;
    private String clientName2;
    private String clientName3;
    private Date date = new Date();

    private List<String> photos = new ArrayList<>();
    private int delPhoto = 0;
    RecyclerView rvClientPhotos;

    public void setDate(Date date) {
        this.date = date;
        ((TextView) findViewById(R.id.tvBirthday)).setText(
                CalendarHelper.dateToString(date, "dd MMMM yyyy"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_client);
        setTitle(R.string.title_client_new);

        findViewById(R.id.ivPhoto).setOnClickListener(this);
        findViewById(R.id.tvPhoto).setOnClickListener(this);
        findViewById(R.id.tvName).setOnClickListener(this);
        findViewById(R.id.tvBirthday).setOnClickListener(this);
        findViewById(R.id.btnClientSave).setOnClickListener(this);

        rvClientPhotos = findViewById(R.id.rvClientPhotos);
        photos.add("");
        PhotosAdapter photosAdapter = new PhotosAdapter(photos, getCacheDir() + "/", true);
        rvClientPhotos.setAdapter(photosAdapter);
        rvClientPhotos.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            private static final int MAX_CLICK_DURATION = 500;
            private long startClickTime;

            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startClickTime = Calendar.getInstance().getTimeInMillis();
                        break;
                    case MotionEvent.ACTION_UP:
                        long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                        if(clickDuration < MAX_CLICK_DURATION) {
                            int position = rv.getChildAdapterPosition(rv.findChildViewUnder(e.getX(), e.getY()));
                            if (position == 0) {
                                // Выбор фото
                                Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
                                photoPickerIntent.setType("image/*");
                                startActivityForResult(photoPickerIntent, RC_GET_PHOTO);

                            } else {
                                delPhoto = position;
                                ConfirmDelPhotoDialog dialog = new ConfirmDelPhotoDialog();
                                dialog.onCreateDialog(NewClientActivity.this).show();
                            }
                        }
                        break;
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) { }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) { }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivPhoto :
            case R.id.tvPhoto :
                Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, RC_GET_AVATAR);
                break;

            case R.id.tvName :
                Intent intentName = new Intent(this, NameActivity.class);
                intentName.putExtra("clientName1", clientName1);
                intentName.putExtra("clientName2", clientName2);
                intentName.putExtra("clientName3", clientName3);
                startActivityForResult(intentName, RC_GET_NAME);
                break;

            case R.id.tvBirthday :
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getSupportFragmentManager(), "tvBirthday");
                break;

            case R.id.btnClientSave :
                PrefsHelper prefsHelper = new PrefsHelper(this);
                String token = prefsHelper.getPref("token");

                int cardId = 1; // ToDo
                String complaints = ((TextView)findViewById(R.id.tvComplaints)).getText().toString();
                String anamnesis = ((TextView)findViewById(R.id.tvAnamnesis)).getText().toString();
                String comment = ((TextView)findViewById(R.id.tvComment)).getText().toString();

                new Clients(this).addClient(token, cardId, complaints, anamnesis, comment, 1);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_GET_AVATAR :
                if (resultCode == RESULT_OK) {
                    Uri photo = data.getData();
                    ImageView ivPhoto = findViewById(R.id.ivPhoto);
                    setPhoto(ivPhoto, photo);
                    findViewById(R.id.tvPhoto).setVisibility(View.GONE);
                }
                break;

            case RC_GET_NAME :
                if (resultCode == RESULT_OK) {
                    clientName1 = data.getStringExtra("clientName1");
                    clientName2 = data.getStringExtra("clientName2");
                    clientName3 = data.getStringExtra("clientName3");

                    TextView tvName = findViewById(R.id.tvName);
                    tvName.setText(getName());
                }
                break;

            case RC_GET_PHOTO :
                // Сохранить фото
                if (resultCode == RESULT_OK) {
                    // Получить выбранное фото
                    Uri photo = data.getData();
                    // Записать во временный файл
                    // ToDo
                    String file = Common.saveTempFile(this, photo);
                    // Загрузить в список фото
//                    byte[] photoData = Common.readFile(file);
//                    photos.add(BitmapFactory.decodeByteArray(photoData, 0, photoData.length));
                    photos.add(file);
                    // Отобразить изменения на экране
                    rvClientPhotos.getAdapter().notifyDataSetChanged();
                }
                break;
        }
    }

    private void setPhoto(ImageView view, Uri uri) {
        if (uri.toString().isEmpty()) {
            view.setImageDrawable(getResources().getDrawable(R.drawable.ic_photo));
            view.setScaleType(ImageView.ScaleType.CENTER);
            view.setScaleX(2);
            view.setScaleY(2);
        }
        else {
            view.setImageURI(uri);
            view.setScaleType(ImageView.ScaleType.FIT_CENTER);
            view.setScaleX(1);
            view.setScaleY(1);
        }
    }

    private void setPhoto(ImageView view, byte[] data) {
        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
        view.setImageBitmap(bmp);
    }

    private String getName() {
        String name = clientName3;
        name += (name.length() == 0? "": " ") + clientName1;
        name += (name.length() == 0? "": " ") + clientName2;
        return name;
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        if (delPhoto > 0) {
            photos.remove(delPhoto);
            rvClientPhotos.getAdapter().notifyDataSetChanged();
        }
        delPhoto = 0;
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        delPhoto = 0;
    }

}
