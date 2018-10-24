package com.aconst.spinareg.sessions;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;

import com.aconst.spinareg.PrefsHelper;
import com.aconst.spinareg.R;
import com.aconst.spinareg.api.Session;

public class ConfirmDelTimeoutDialog extends DialogFragment {
    public Dialog onCreateDialog(final Context context, final int sessionId) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.prompt_del_timeout)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Session session = new Session(context);
                        PrefsHelper prefsHelper = new PrefsHelper(context);
                        String token = prefsHelper.getPref("token");
                        session.delSession(token, sessionId);
                    }
                });
        return builder.create();
    }
}
