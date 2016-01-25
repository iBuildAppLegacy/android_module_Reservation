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
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.appbuilder.sdk.android.AppBuilderModuleMain;
import com.appbuilder.sdk.android.Statics;
import com.appbuilder.sdk.android.authorization.entities.User;
import com.ibuildapp.romanblack.TableReservationPlugin.entity.FanWallUser;
import com.ibuildapp.romanblack.TableReservationPlugin.utils.TableReservationHTTP;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;

import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This activity provides authorization via email functionality.
 */
public class TableReservationEMailSignUpActivity extends AppBuilderModuleMain implements OnClickListener,
        TextWatcher {

    private final int CLOSE_ACTIVITY_OK = 0;
    private final int CLOSE_ACTIVITY_BAD = 1;
    private final int SHOW_PROGRESS_DIALOG = 2;
    private final int HIDE_PROGRESS_DIALOG = 3;
    private final int CHECK_ACTIVE_SIGN_IN = 4;
    private final int EMEIL_IN_USE = 5;
    private boolean signUpActive = false;
    private boolean needCheckFields = false;
    private FanWallUser fwUser = null;
    private User resUser = new User();
    private EditText firstNameEditText = null;
    private EditText lastNameEditText = null;
    private EditText emailEditText = null;
    private EditText passwordEditText = null;
    private EditText rePasswordEditText = null;
    private CheckBox termsCheckBox = null;
    private ProgressDialog progressDialog = null;
    private LinearLayout termsLayout = null;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case CLOSE_ACTIVITY_OK: {
                    closeActivityOk();
                }
                break;
                case CLOSE_ACTIVITY_BAD: {
                    closeActivityBad();
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
                case CHECK_ACTIVE_SIGN_IN: {
                }
                break;

                case EMEIL_IN_USE: {
                    Toast.makeText(TableReservationEMailSignUpActivity.this,
                            R.string.alert_registration_email_inuse,
                            Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    };

    @Override
    public void create() {
        setContentView(R.layout.sergeyb_tablereservation_emailsignup);

        // set topbar title
        setTopBarTitle(getResources().getString(R.string.common_sign_up_upper));
        swipeBlock();
        setTopBarLeftButtonText(getResources().getString(R.string.common_back_upper), true, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_OK, null);
                finish();
            }
        });
        setTopBarRightButtonText(getResources().getString(R.string.common_sign_up_upper), false, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registration();
            }
        });

        firstNameEditText = (EditText) findViewById(R.id.sergeyb_tablereservation_emailsignup_fname);
        firstNameEditText.addTextChangedListener(this);
        firstNameEditText.clearFocus();

        lastNameEditText = (EditText) findViewById(R.id.sergeyb_tablereservation_emailsignup_lname);
        lastNameEditText.addTextChangedListener(this);
        firstNameEditText.clearFocus();

        emailEditText = (EditText) findViewById(R.id.sergeyb_tablereservation_emailsignup_email);
        emailEditText.addTextChangedListener(this);
        firstNameEditText.clearFocus();

        passwordEditText = (EditText) findViewById(R.id.sergeyb_tablereservation_emailsignup_pwd);
        passwordEditText.addTextChangedListener(this);
        firstNameEditText.clearFocus();

        rePasswordEditText = (EditText) findViewById(R.id.sergeyb_tablereservation_emailsignup_rpwd);
        rePasswordEditText.addTextChangedListener(this);

        termsCheckBox = (CheckBox) findViewById(R.id.sergeyb_tablereservation_emailsignup_chbterms);
        termsCheckBox.setOnClickListener(this);

        termsLayout = (LinearLayout) findViewById(R.id.sergeyb_tablereservation_emailsignup_layouttems);
        termsLayout.setVisibility(View.INVISIBLE);

        // hide keyboard on start
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        needCheckFields = true;
    }

    /**
     * Validates values of input fields.
     */
    private void checkFields() {
        if (!needCheckFields) {
            return;
        }

        if ((firstNameEditText.getText().toString().length() > 0)
                && (lastNameEditText.getText().toString().length() > 0)
                && (emailEditText.getText().toString().length() > 0)
                && (passwordEditText.getText().toString().length() > 0)
                && (rePasswordEditText.getText().toString().length() > 0)) {
            String regExpn =
                    "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                            + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                            + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                            + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                            + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                            + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";

            Pattern pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(emailEditText.getText().toString());

            if (matcher.matches()) {
            } else {
                signUpActive = false;
                return;
            }

            if (!firstNameEditText.getText().toString().equals(
                    lastNameEditText.getText().toString())) {
            } else {
                signUpActive = false;
                return;
            }

            if (passwordEditText.getText().toString().equals(rePasswordEditText.getText().toString())) {
            } else {
                signUpActive = false;
                return;
            }

            if (passwordEditText.getText().toString().length() >= 4) {
            } else {
                signUpActive = false;
                return;
            }

            signUpActive = true;

        } else {
            signUpActive = false;
        }
    }

    /**
     * Closes activity with "OK" result.
     */
    private void closeActivityOk() {
        hideProgressDialog();

        Intent resIntent = new Intent();
        resUser.setAccountId(fwUser.getAccountId());
        resUser.setUserEmail(emailEditText.getText().toString());
        resUser.setUserName(fwUser.getUserName());
        resUser.setAccountType("twitter");
        resUser.setAvatarUrl(fwUser.getAvatarUrl());
        resUser.setUserFirstName(fwUser.getUserFirstName());
        resUser.setUserLastName(fwUser.getUserLastName());
        resIntent.putExtra("user", resUser);
        setResult(RESULT_OK, resIntent);
        finish();
    }

    /**
     * Closes activity with "Cancel" result.
     */
    private void closeActivityBad() {
        hideProgressDialog();
        finish();
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = ProgressDialog.show(this, null, getResources().getString(R.string.common_registration_upper));
        } else {
            if (!progressDialog.isShowing()) {
                progressDialog = ProgressDialog.show(this, null, getResources().getString(R.string.common_registration_upper));
            }
        }
    }

    private void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    public void onClick(View arg0) {
        if (arg0 == termsCheckBox) {
            checkFields();

            handler.sendEmptyMessage(CHECK_ACTIVE_SIGN_IN);
        }
    }

    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
    }

    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
    }

    public void afterTextChanged(Editable arg0) {
        checkFields();

        handler.sendEmptyMessage(CHECK_ACTIVE_SIGN_IN);
    }

    /**
     * Validate input data and signs up new user on iBuildApp.
     */
    private void registration() {
        if (signUpActive) {
            handler.sendEmptyMessage(SHOW_PROGRESS_DIALOG);

            new Thread(new Runnable() {
                public void run() {

                    HttpParams params = new BasicHttpParams();
                    params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION,
                            HttpVersion.HTTP_1_1);
                    HttpClient httpClient = new DefaultHttpClient(params);

                    try {
                        HttpPost httpPost = new HttpPost(TableReservationHTTP.SIGNUP_URL);

                        String firstNameString = firstNameEditText.getText().toString();
                        String lastNameString = lastNameEditText.getText().toString();
                        String emailString = emailEditText.getText().toString();
                        String passwordString = passwordEditText.getText().toString();
                        String rePasswordString = rePasswordEditText.getText().toString();

                        MultipartEntity multipartEntity = new MultipartEntity();
                        multipartEntity.addPart("firstname", new StringBody(firstNameString, Charset.forName("UTF-8")));
                        multipartEntity.addPart("lastname", new StringBody(lastNameString, Charset.forName("UTF-8")));
                        multipartEntity.addPart("email", new StringBody(emailString, Charset.forName("UTF-8")));
                        multipartEntity.addPart("password", new StringBody(passwordString, Charset.forName("UTF-8")));
                        multipartEntity.addPart("password_confirm", new StringBody(rePasswordString, Charset.forName("UTF-8")));

                        // add security part
                        multipartEntity.addPart("app_id", new StringBody(Statics.appId, Charset.forName("UTF-8")));
                        multipartEntity.addPart("token", new StringBody(Statics.appToken, Charset.forName("UTF-8")));

                        httpPost.setEntity(multipartEntity);

                        String resp = httpClient.execute(httpPost, new BasicResponseHandler());

                        fwUser = JSONParser.parseLoginRequestString(resp);

                        if (fwUser == null) {
                            handler.sendEmptyMessage(EMEIL_IN_USE);
                            handler.sendEmptyMessage(HIDE_PROGRESS_DIALOG);
                            return;
                        }

                        handler.sendEmptyMessage(HIDE_PROGRESS_DIALOG);

                        handler.sendEmptyMessage(CLOSE_ACTIVITY_OK);

                    } catch (Exception e) {
                        handler.sendEmptyMessage(CLOSE_ACTIVITY_BAD);
                    }

                }
            }).start();
        } else {
            if (firstNameEditText.getText().toString().length() == 0
                    || lastNameEditText.getText().toString().length() == 0
                    || emailEditText.getText().toString().length() == 0
                    || passwordEditText.getText().toString().length() == 0
                    || rePasswordEditText.getText().toString().length() == 0) {
                Toast.makeText(this, R.string.alert_registration_fillin_fields, Toast.LENGTH_LONG).show();
                return;
            }

            if (firstNameEditText.getText().toString().equals(
                    lastNameEditText.getText().toString())) {
                Toast.makeText(this, R.string.alert_registration_spam, Toast.LENGTH_LONG).show();
                return;
            }

            if (firstNameEditText.getText().toString().length() <= 2
                    || lastNameEditText.getText().toString().length() <= 2) {
                Toast.makeText(this, R.string.alert_registration_two_symbols_name, Toast.LENGTH_LONG).show();
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
            Matcher matcher = pattern.matcher(emailEditText.getText().toString());

            if (matcher.matches()) {
            } else {
                Toast.makeText(this, R.string.alert_registration_correct_email, Toast.LENGTH_LONG).show();
                return;
            }

            if (passwordEditText.getText().toString().length() < 4) {
                Toast.makeText(this, R.string.alert_registration_two_symbols_password, Toast.LENGTH_LONG).show();
                return;
            }

            if (!passwordEditText.getText().toString().equals(
                    rePasswordEditText.getText().toString())) {
                Toast.makeText(this, "Passwords don't match.", Toast.LENGTH_LONG).show();
                return;
            }
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK, null);
        finish();

    }
}
