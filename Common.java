package com.aconst.spinareg;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Locale;

public class Common {
    public static final String BASE_DOMAIN = "http://tst.morevoprosov.net/";
    public static final String BASE_URL = BASE_DOMAIN + "spine_api/";

    // Используемые форматы даты и времени
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATE_FORMAT_VIEW = "dd MMMM yyyy";
    public static final String DATE_FORMAT_VIEW_SHORT = "dd MMM yyyy";
    public static final String DATE_FORMAT_SHORT = "dd.MM.yyyy";
    private static final String TIME_FORMAT = "HH:mm:ss";
    public static final String DATE_TIME_FORMAT = DATE_FORMAT + " " + TIME_FORMAT;

    // Константы видов запросов по клиентам
//    public final static int RC_GET_NAME = 1;
//    public final static int RC_GET_AVATAR = 2;
    public final static int RC_GET_PHOTO = 3;
//    public final static int RC_NEW_CLIENT = 4;
//    public final static int RC_SELECT_CLIENT = 5;
    public final static int RC_NEW_SESSION = 6;

    public final static int RC_NEW_SERVICE = 10;
    public final static int RC_EDIT_SERVICE = 11;
    public final static int RC_SELECT_DATE_TIME = 12;
    public final static int RC_SELECT_SERVICE = 13;

    // Опции профиля
    public final static int OPTION1 = 1;    // Выезд на дом
    public final static int OPTION2 = 3;    // Работаю с новорожденными
    public final static int OPTION3 = 5;    // Работаю с детьми
    public final static int OPTION4 = 6;    // Работаю с беременными

    public static String saveFile(Context context, Uri uri, String filename) {
        InputStream iStream = null;
        try {
            iStream = context.getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (iStream == null)
            return "";

        byte[] data = new byte[0];
        try {
            data = getBytes(iStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            FileOutputStream outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(data);
            outputStream.close();
            return context.getFilesDir() + "/" + filename;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String saveTempFile(Context context, Uri uri) {
        File outputFile = null;
        try {
            outputFile = File.createTempFile("tmp", ".jpg", context.getCacheDir());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (outputFile == null)
            return "";

        InputStream iStream = null;
        try {
            iStream = context.getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (iStream == null)
            return "";

        byte[] data = new byte[0];
        try {
            data = getBytes(iStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            FileOutputStream outputStream = new FileOutputStream(outputFile);
            outputStream.write(data);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outputFile.getName();
    }

    public static String saveTempFile(Context context, Bitmap bm) {
        File outputFile = null;
        try {
            outputFile = File.createTempFile("tmp", ".jpg", context.getCacheDir());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (outputFile == null)
            return "";

        int bytes = bm.getByteCount();
        ByteBuffer buffer = ByteBuffer.allocate(bytes);
        bm.copyPixelsToBuffer(buffer);

        String fullFilename = outputFile.getAbsolutePath();
        try {
            FileOutputStream outputStream = new FileOutputStream(outputFile);
            outputStream.write(buffer.array());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            fullFilename = "";
        }
        return fullFilename;
    }

    public static String saveTempFile(Context context, byte[] data, String filename) {
        String fullFilename = context.getCacheDir() + "/" + filename;
        try {
            FileOutputStream outputStream = new FileOutputStream(fullFilename);
            outputStream.write(data);
            outputStream.close();
        } catch (Exception e) {
            fullFilename = "";
            e.printStackTrace();
        }
        return fullFilename;
    }

    private static byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    public static byte[] readFile(String path) {
        File file = new File(path);
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            int readLength = buf.read(bytes, 0, bytes.length);
            buf.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    public static byte[] getData(Bitmap bmp) {
        int size = bmp.getRowBytes() * bmp.getHeight();
        ByteBuffer b = ByteBuffer.allocate(size);

        bmp.copyPixelsToBuffer(b);

        byte[] bytes = new byte[size];

        try {
            b.get(bytes, 0, bytes.length);
        } catch (BufferUnderflowException e) {
            // always happens
        }
        return bytes;
    }

    public static String numberInCase(int number, String[] cases) {
        if (cases.length != 4)
            return "Ошибка: количество вариантов должно быть 4";

        else {
            if (number == 0)
                return cases[0];
            else {
                String s;
                if (number == 1 || number > 20 && number % 10 == 1)
                    s = cases[1];
                else if (number < 5 || number % 10 < 5 && number % 10 > 0)
                    s = cases[2];
                else
                    s = cases[3];
                return String.format(Locale.getDefault(), "%d %s", number, s);
            }
        }
    }


    public static int getIntFromJson(String json, String value) {
        try {
            JSONObject object = new JSONObject(json);
            return object.getInt(value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static float getFloatFromJson(String json, String value) {
        try {
            JSONObject object = new JSONObject(json);
            return (float) object.getDouble(value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String getFromJson(String json, String value) {
        try {
            JSONObject object = new JSONObject(json);
            return object.getString(value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }
}
