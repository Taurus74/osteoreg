package com.aconst.spinareg.options;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;

import com.aconst.spinareg.PrefsHelper;
import com.aconst.spinareg.R;
import com.aconst.spinareg.api.Session;
import com.aconst.spinareg.controllers.SessionController;
import com.aconst.spinareg.model.ServiceItemRealm;
import com.aconst.spinareg.model.SessionItemRealm;
import com.aconst.spinareg.model.Vacation;

import java.util.List;

public class ConfirmDelVacationDialog extends DialogFragment {
    public Dialog onCreateDialog(final Context context, final Vacation vacation) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.option_delete_vacation)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Session session = new Session(context);
                        session.deleteVacation(vacation);
                    }
                });
        return builder.create();
    }
}
