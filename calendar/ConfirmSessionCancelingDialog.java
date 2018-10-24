package com.aconst.spinareg;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import com.aconst.spinareg.api.Session;
import com.aconst.spinareg.controllers.SessionController;

public class ConfirmSessionCancelingDialog {
    public Dialog onCreateDialog(final Context context, final int sessionId, final String token) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.prompt_cancel)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Session(context).delSession(token, sessionId);
                        new SessionController().deleteSession(sessionId).subscribe();
                        // ToDo - Обновление экрана
                    }
                });
        return builder.create();
    }
}
