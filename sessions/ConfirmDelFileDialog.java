package com.aconst.spinareg.sessions;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;

import com.aconst.spinareg.R;

public class ConfirmDelPhotoDialog extends DialogFragment {
    public interface NoticeDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);
        void onDialogNegativeClick(DialogFragment dialog);
    }

    public Dialog onCreateDialog(Context context) {
        final NoticeDialogListener mListener = (NoticeDialogListener) context;
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.prompt_del_photo)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onDialogNegativeClick(ConfirmDelPhotoDialog.this);
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onDialogPositiveClick(ConfirmDelPhotoDialog.this);
                    }
                });
        return builder.create();
    }
}
