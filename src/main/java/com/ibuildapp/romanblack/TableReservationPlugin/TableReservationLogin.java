/****************************************************************************
*                                                                           *
*  Copyright (C) 2014-2015 iBuildApp, Inc. ( http://ibuildapp.com )         *
*                                                                           *
*  This file is part of iBuildApp.                                          *
*                                                                           *
*  This Source Code Form is subject to the terms of the iBuildApp License.  *
*  You can obtain one at http://ibuildapp.com/license/                      *
*                                                                           *
****************************************************************************/
package com.ibuildapp.romanblack.TableReservationPlugin;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.appbuilder.sdk.android.AppBuilderModuleMain;
import com.appbuilder.sdk.android.authorization.Authorization;
import com.appbuilder.sdk.android.authorization.entities.User;
import com.ibuildapp.romanblack.TableReservationPlugin.entity.FanWallUser;
import com.ibuildapp.romanblack.TableReservationPlugin.utils.TableReservationHTTP;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This activity provides a selection of ways to authorize using facebook, twitter or email.
 * Also user can register using his own email.
 */
public class TableReservationLogin extends AppBuilderModuleMain {

    private final int CLOSE_ACTIVITY_OK = 0;
    private final int NEED_INTERNET_CONNECTION = 2;
    private final int FAILED_TO_LOGIN = 3;
    private final int SHOW_PROGRESS_DIALOG = 4;
    private final int HIDE_PROGRESS_DIALOG = 5;
    private final int EMAIL_NOT_MATCHES = 6;
    private final int DRAW_MARGINS = 7;
    private final int SHOW_ERROR = 8;
    private final int TWITTER_AUTHORIZATION_ACTIVITY = 10000;
    private final int FACEBOOK_AUTHORIZATION_ACTIVITY = 10001;
    private final int IBUILD_AUTHORIZATION_ACTIVITY = 10002;
    private LinearLayout emailLayout;
    private LinearLayout passLayout;
    private TextView emailTextView;
    private TextView passTextView;
    private FrameLayout mainLayout;
    private EditText emailText;
    private EditText passText;
    private View facebook;
    private View twitter;
    private LinearLayout taskBar;
    private LinearLayout scrollHolder;
    private LinearLayout hideKeyboard;
    private LinearLayout twitterFbHolder;
    private LinearLayout ibuildappHolder;
    private LinearLayout guestHolder;
    private ScrollView scrollView;
    private int scrollHolderH = -1;
    private int twitterFbHolderH = -1;
    private int ibuildappHolderH = -1;
    private int guestHolderH = -1;
    private int scrollViewH = -1;
    private RelativeLayout ibuildappLogin;
    private RelativeLayout asAGuest;
    private RelativeLayout signIn;
    private TextView guestText;
    private FanWallUser tmpUser;
    private int fontColor;
    private int backColor;
    private boolean colorSchema;
    private String redirectTo;
    private User fwUser = new User();
    private ProgressDialog progressDialog = null;
    private Thread ibuildappAuthThread;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case CLOSE_ACTIVITY_OK: {
                    closeActivityLoginIbuildapp();
                }
                break;
                case SHOW_PROGRESS_DIALOG: {
                    showProgressDialog();
                }
                break;
                case HIDE_PROGRESS_DIALOG: {
                    hideProgressDialog();
                }
                break;
                case EMAIL_NOT_MATCHES: {
                    Toast.makeText(TableReservationLogin.this, R.string.alert_invalid_email, Toast.LENGTH_LONG).show();
                }
                break;
                case FAILED_TO_LOGIN: {
                    showFailedLogin();
                }
                break;
                case DRAW_MARGINS: {
                    setMargins();
                }
                break;
                case NEED_INTERNET_CONNECTION: {
                    Toast toast = Toast.makeText(TableReservationLogin.this,
                            R.string.alert_no_internet, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 95);
                    toast.show();
                }
                break;
                case SHOW_ERROR: {
                    Toast.makeText(TableReservationLogin.this, (String) msg.obj, Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    };

    @Override
    public void onBackPressed() {
        Intent resIntent = new Intent();
        fwUser.setUserName("noname");
        fwUser.setAccountId("noname");
        fwUser.setAccountType("noname");
        resIntent.putExtra("user", fwUser);
        setResult(RESULT_CANCELED, resIntent);
        finish();
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void create() {
        setContentView(R.layout.sergeyb_tablereservation_login);

        // set topbar title
        setTopBarTitle(getResources().getString(R.string.tablereservation_login));
        swipeBlock();
        setTopBarLeftButtonText(getResources().getString(R.string.common_back_upper), true, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mainLayout = (FrameLayout) findViewById(R.id.sergeyb_tablereservation_main_layout);
        scrollHolder = (LinearLayout) findViewById(R.id.sergeyb_tablereservation_scroll_holder);
        twitterFbHolder = (LinearLayout) findViewById(R.id.sergeyb_tablereservation_twitter_fb_holder);
        ibuildappHolder = (LinearLayout) findViewById(R.id.sergeyb_tablereservation_ibuildapp_holder);
        guestHolder = (LinearLayout) findViewById(R.id.sergeyb_tablereservation_guest_holder);
        scrollView = (ScrollView) findViewById(R.id.sergeyb_tablereservation_scrollView);
        hideKeyboard = (LinearLayout) findViewById(R.id.sergeyb_tablereservation_hide_keyboard);
        taskBar = (LinearLayout) findViewById(R.id.sergeyb_tablereservation_bottom_taskbar);
        emailText = (EditText) findViewById(R.id.sergeyb_tablereservation_ibuildapp_email_editText);
        passText = (EditText) findViewById(R.id.sergeyb_tablereservation_ibuildapp_pass_editText);
        asAGuest = (RelativeLayout) findViewById(R.id.sergeyb_tablereservation_guest);
        emailLayout = (LinearLayout) findViewById(R.id.sergeyb_tablereservation_ibuildapp_email);
        passLayout = (LinearLayout) findViewById(R.id.sergeyb_tablereservation_ibuildapp_password);

        twitterFbHolder.requestFocus();

        // hide keyboard on start
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mainLayout.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                //r will be populated with the coordinates of your view that area still visible.
                mainLayout.getWindowVisibleDisplayFrame(r);

                int heightDiff = mainLayout.getRootView().getHeight() - (r.bottom - r.top);
                /*if (heightDiff > (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? 300 : 100)) { // if more than 100 pixels, its probably a keyboard...
                    taskBar.setVisibility(View.VISIBLE);
                } else {*/
                    taskBar.setVisibility(View.INVISIBLE);
                //}
            }
        });

        hideKeyboard.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                InputMethodManager imm = (InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(emailText.getWindowToken(), 0);
            }
        });

        // waiting for UI drawing
        scrollHolder.post(new Runnable() {
            public void run() {
                scrollHolderH = scrollHolder.getHeight();
            }
        });
        twitterFbHolder.post(new Runnable() {
            public void run() {
                twitterFbHolderH = twitterFbHolder.getHeight();
            }
        });
        ibuildappHolder.post(new Runnable() {
            public void run() {
                ibuildappHolderH = ibuildappHolder.getHeight();
            }
        });
        guestHolder.post(new Runnable() {
            public void run() {
                guestHolderH = guestHolder.getHeight();
            }
        });
        scrollView.post(new Runnable() {
            public void run() {
                scrollViewH = scrollView.getHeight();
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (scrollHolderH != -1 && twitterFbHolderH != -1
                            && ibuildappHolderH != -1 && guestHolderH != -1 && scrollViewH != -1) {
                        handler.sendEmptyMessage(DRAW_MARGINS);
                        break;
                    }
                }
            }
        }).start();

        // getting info from parent activity
        Intent currentIntent = getIntent();
        redirectTo = currentIntent.getStringExtra("redirectTo");
        fontColor = currentIntent.getIntExtra("fontColor", Color.WHITE);
        backColor = currentIntent.getIntExtra("backColor", Color.parseColor("#37393b"));
        colorSchema = currentIntent.getBooleanExtra("colorSchema", true);

        if (colorSchema) // dark scheme
        {
            emailLayout.setBackgroundResource(R.drawable.sergeyb_tablereservation_row_first_dark);
            passLayout.setBackgroundResource(R.drawable.sergeyb_tablereservation_row_last_dark);
            asAGuest.setBackgroundResource(R.drawable.sergeyb_tablereservation_back_transperant_15alpha_dark);
        } else {
            emailLayout.setBackgroundResource(R.drawable.sergeyb_tablereservation_row_first);
            passLayout.setBackgroundResource(R.drawable.sergeyb_tablereservation_row_last);
            asAGuest.setBackgroundResource(R.drawable.sergeyb_tablereservation_back_transperant_15alpha);
        }

        // set appropriate color to text and background
        mainLayout = (FrameLayout) findViewById(R.id.sergeyb_tablereservation_main_layout);
        mainLayout.setBackgroundColor(backColor);

        emailTextView = (TextView) findViewById(R.id.sergeyb_tablereservation_email_textview);
        emailTextView.setTextColor(fontColor);
        passTextView = (TextView) findViewById(R.id.sergeyb_tablereservation_pass_textview);
        passTextView.setTextColor(fontColor);
        emailText.setTextColor(fontColor);
        emailText.setBackgroundColor(Color.TRANSPARENT);
        passText.setTextColor(fontColor);
        passText.setBackgroundColor(Color.TRANSPARENT);
        guestText = (TextView) findViewById(R.id.sergeyb_tablereservation_guest_text);
        guestText.setTextColor(fontColor);

        // twitterAuth handler
        twitter = findViewById(R.id.sergeyb_tablereservation_twitter_login);
        twitter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                // check internet connection
                boolean isOnline = false;
                ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo ni = cm.getActiveNetworkInfo();
                if (ni != null && ni.isConnectedOrConnecting()) {
                    isOnline = true;
                }

                if (!isOnline) {
                    handler.sendEmptyMessage(NEED_INTERNET_CONNECTION);
                    return;
                }

                if(Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_TWITTER) == null) {
                    Authorization.authorize(TableReservationLogin.this, TWITTER_AUTHORIZATION_ACTIVITY, Authorization.AUTHORIZATION_TYPE_TWITTER);
                } else {
                    Intent intent = new Intent();
                    intent.putExtra("user", Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_TWITTER));
                    onActivityResult(TWITTER_AUTHORIZATION_ACTIVITY, RESULT_OK, intent);
                }

            }
        });

        // facebook handler
        facebook = findViewById(R.id.sergeyb_tablereservation_facebook_login);
        facebook.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                // check internet connection
                boolean isOnline = false;
                ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo ni = cm.getActiveNetworkInfo();
                if (ni != null && ni.isConnectedOrConnecting()) {
                    isOnline = true;
                }

                if (!isOnline) {
                    handler.sendEmptyMessage(NEED_INTERNET_CONNECTION);
                    return;
                }

                if(Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_FACEBOOK) == null) {
                    Authorization.authorize(TableReservationLogin.this, FACEBOOK_AUTHORIZATION_ACTIVITY, Authorization.AUTHORIZATION_TYPE_FACEBOOK);
                } else {
                    Intent intent = new Intent();
                    intent.putExtra("user", Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_FACEBOOK));
                    onActivityResult(FACEBOOK_AUTHORIZATION_ACTIVITY, RESULT_OK, intent);
                }
            }
        });

        // ibuildappLogin handler
        ibuildappLogin = (RelativeLayout) findViewById(R.id.sergeyb_tablereservation_ibuildapp_login);
        ibuildappLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                // check internet connection
                boolean isOnline = false;
                ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo ni = cm.getActiveNetworkInfo();
                if (ni != null && ni.isConnectedOrConnecting()) {
                    isOnline = true;
                }

                if (!isOnline) {
                    handler.sendEmptyMessage(NEED_INTERNET_CONNECTION);
                    return;
                }

                String regExpn =
                        "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                                + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                                + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                                + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";

                Pattern pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(emailText.getText().toString());

                if (!matcher.matches()) {
                    handler.sendEmptyMessage(EMAIL_NOT_MATCHES);
                    return;
                }

                handler.sendEmptyMessage(SHOW_PROGRESS_DIALOG);

                ibuildappAuthThread = new Thread(new Runnable() {
                    public void run() {
                        loginIbuildapp();
                    }
                });
                ibuildappAuthThread.start();
            }
        });

        // sign up handler
        signIn = (RelativeLayout) findViewById(R.id.sergeyb_tablereservation_ibuildapp_signin);
        signIn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                // check internet connection
                boolean isOnline = false;
                ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo ni = cm.getActiveNetworkInfo();
                if (ni != null && ni.isConnectedOrConnecting()) {
                    isOnline = true;
                }

                if (!isOnline) {
                    handler.sendEmptyMessage(NEED_INTERNET_CONNECTION);
                    return;
                }

                Intent resIntent = new Intent(TableReservationLogin.this,
                        TableReservationEMailSignUpActivity.class);
                try {
                    startActivityForResult(resIntent, IBUILD_AUTHORIZATION_ACTIVITY);
                } catch (Exception e) {
                    Log.d("", "");
                }
            }
        });

        // guest reservation handler
        asAGuest.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Intent resIntent = new Intent();
                fwUser.setUserName("guest");
                fwUser.setAccountId(UUID.randomUUID().toString());
                fwUser.setAccountType("guest");
                fwUser.setUserEmail("");
                resIntent.putExtra("user", fwUser);
                resIntent.putExtra("redirectTo", redirectTo);
                setResult(RESULT_OK, resIntent);
                finish();
            }
        });
    }

    /**
     * Calling when child activity finished it's work and trying to return
     * result
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case FACEBOOK_AUTHORIZATION_ACTIVITY: {
                if (resultCode == RESULT_OK) {
                    data.putExtra("redirectTo", redirectTo);
                    data.putExtra("user", Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_FACEBOOK));
                    setResult(RESULT_OK, data);

                    finish();
                } else if (resultCode == RESULT_CANCELED) {
                    Toast toast = Toast.makeText(TableReservationLogin.this, R.string.alert_facebook_auth_error, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 95);
                    toast.show();
                }
            }
            break;
            case TWITTER_AUTHORIZATION_ACTIVITY: {
                if (resultCode == RESULT_OK) {
                    data.putExtra("redirectTo", redirectTo);
                    data.putExtra("user", Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_TWITTER));
                    setResult(RESULT_OK, data);
                    finish();

                } else if (resultCode == RESULT_CANCELED) {
                    Toast toast = Toast.makeText(TableReservationLogin.this, R.string.alert_twitter_auth_error, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 95);
                    toast.show();
                }
            }
            break;

            case IBUILD_AUTHORIZATION_ACTIVITY: {
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        data.putExtra("redirectTo", redirectTo);
                        setResult(RESULT_OK, data);
                        finish();
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    Toast toast = Toast.makeText(TableReservationLogin.this, R.string.alert_registration_error, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 95);
                    toast.show();
                }
            }
            break;


        }
    }

    /**
     * Logins user via ibuildapp.
     */
    private void loginIbuildapp() {
        try {
            String resp = TableReservationHTTP.loginPost(emailText.getText().toString(), passText.getText().toString());

            tmpUser = JSONParser.parseLoginRequestString(resp);

            if (tmpUser == null) {
                // show message
                handler.sendEmptyMessage(FAILED_TO_LOGIN);

                // hide dialog
                handler.sendEmptyMessage(HIDE_PROGRESS_DIALOG);
                return;
            }

            handler.sendEmptyMessage(HIDE_PROGRESS_DIALOG);
            handler.sendEmptyMessage(CLOSE_ACTIVITY_OK);

        } catch (Exception e) {
            Log.d("", "");
        }
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = ProgressDialog.show(this, null, getResources().getString(R.string.common_loading_upper), true, true, new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface arg0) {
                    try {
                        if (ibuildappAuthThread != null && ibuildappAuthThread.isAlive()) {
                            ibuildappAuthThread.interrupt();
                        }
                        progressDialog.dismiss();
                    } catch (Exception e) {
                        Log.d("", "");
                    }
                }
            });
        }
    }

    private void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    /**
     * Shows alert if login was failed.
     */
    private void showFailedLogin() {
        Toast toast = Toast.makeText(TableReservationLogin.this, R.string.alert_not_logged_in, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 95);
        toast.show();
    }

    /**
     * Closes activity with "OK" result.
     */
    private void closeActivityLoginIbuildapp() {
        Intent resIntent = new Intent();
        fwUser.setUserName(tmpUser.getUserName());
        fwUser.setUserEmail(emailText.getText().toString());
        fwUser.setAccountId(tmpUser.getAccountId());
        fwUser.setUserFirstName(tmpUser.getUserFirstName());
        fwUser.setUserLastName(tmpUser.getUserLastName());
        fwUser.setAccountType("ibuildapp");
        resIntent.putExtra("user", fwUser);
        resIntent.putExtra("redirectTo", redirectTo);
        setResult(RESULT_OK, resIntent);
        finish();
    }

    /**
     * Sets the margins.
     */
    private void setMargins() {
        int freeSpace = scrollViewH - twitterFbHolderH
                - ibuildappHolderH - guestHolderH;
        if (freeSpace > 15) {
            freeSpace -= 10;
        }
        freeSpace /= 2;

        LinearLayout.LayoutParams retake_param =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                        LinearLayout.LayoutParams.FILL_PARENT);
        retake_param.setMargins(0, freeSpace, 0, 0);
        ibuildappHolder.setLayoutParams(retake_param);
        guestHolder.setLayoutParams(retake_param);
    }
}
