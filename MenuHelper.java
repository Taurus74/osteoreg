package com.aconst.spinareg;

import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;

import com.aconst.spinareg.api.Authorization;
import com.aconst.spinareg.authorization.ChangePasswdActivity;
import com.aconst.spinareg.authorization.CheckPhoneVerifyActivity;
import com.aconst.spinareg.authorization.EmailVerifyActivity;
import com.aconst.spinareg.authorization.PhoneVerifyActivity;
import com.aconst.spinareg.authorization.RegisterActivity;
import com.aconst.spinareg.calendar.CalendarActivity;
import com.aconst.spinareg.clients.ClientsActivity;
import com.aconst.spinareg.more.HelpActivity;
import com.aconst.spinareg.messages.MessagesActivity;
import com.aconst.spinareg.more.SubscribeActivity;
import com.aconst.spinareg.options.OptionsActivity;
import com.aconst.spinareg.profile.ProfileActivity;
import com.aconst.spinareg.reports.ReportsActivity;
import com.aconst.spinareg.services.NewEditServiceActivity;

public class MenuHelper {
    private Context context;

    public MenuHelper(Context context) {
        this.context = context;
    }

    public boolean processMenu(MenuItem item) {
        Intent intent = null;
        switch (item.getItemId()) {
            case R.id.menu_change_passwd :
                intent = new Intent(context, ChangePasswdActivity.class);
                break;
            case R.id.menu_check_phone_verify :
                intent = new Intent(context, CheckPhoneVerifyActivity.class);
                break;
            case R.id.menu_email_verify :
                intent = new Intent(context, EmailVerifyActivity.class);
                break;
            case R.id.menu_logout :
                PrefsHelper prefsHelper = new PrefsHelper(context);
                String token = prefsHelper.getPref("token");
                new Authorization(context).logout(token);
                break;
            case R.id.menu_phone_verify :
                intent = new Intent(context, PhoneVerifyActivity.class);
                break;
            case R.id.menu_registration :
                intent = new Intent(context, RegisterActivity.class);
                break;
            case R.id.menu_calendar :
                intent = new Intent(context, CalendarActivity.class);
                break;
            case R.id.menu_clients :
                intent = new Intent(context, ClientsActivity.class);
                break;
            case R.id.menu_add_service :
                intent = new Intent(context, NewEditServiceActivity.class);
                break;
            case R.id.menu_profile :
                intent = new Intent(context, ProfileActivity.class);
                break;
            case R.id.menu_messages :
                intent = new Intent(context, MessagesActivity.class);
                break;
            case R.id.menu_options :
                intent = new Intent(context, OptionsActivity.class);
                break;
            case R.id.menu_reports :
                intent = new Intent(context, ReportsActivity.class);
                break;
            case R.id.menu_help :
                intent = new Intent(context, HelpActivity.class);
                break;
            case R.id.menu_subscribe :
                intent = new Intent(context, SubscribeActivity.class);
                break;
        }
        if (intent == null)
            return false;
        else {
            context.startActivity(intent);
            return true;
        }
    }
}
