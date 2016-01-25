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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.appbuilder.sdk.android.AppBuilderModuleMain;
import com.appbuilder.sdk.android.authorization.entities.User;
import com.ibuildapp.romanblack.TableReservationPlugin.entity.HouresMinutes;
import com.ibuildapp.romanblack.TableReservationPlugin.utils.TableReservationHTTP;
import com.ibuildapp.romanblack.TableReservationPlugin.utils.Utils;
import com.seppius.i18n.plurals.PluralResources;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * This activity represents Reservation details page.
 */
public class TableReservationReservation extends AppBuilderModuleMain {

    private final int IMAGE_HEIGHT = 100;
    private final int SPECIAL_REQUEST = 10001;
    private final int MODIFY = 10002;
    private final int CONFIRM = 10003;
    private final int SHOW_PROGRESS_DIALOG = 0;
    private final int HIDE_PROGRESS_DIALOG = 1;
    private final int START_SUMMARY = 2;
    private final int MAIL_SEND_ERROR = 3;
    private final int ADD_REQUEST_ERROR = 4;
    private String cachePath = "";
    private LayoutInflater layoutInflater;
    private User user;
    private TableReservationInfo orderInfo;
    private View headerView;
    private ImageView banner;
    private LinearLayout custom;
    private TextView emailText;
    private TextView restaurantName;
    private TextView restaurantGreetings;
    private LinearLayout specReqButton;
    private LinearLayout confirmButton;
    private ProgressDialog progressDialog = null;
    private LinearLayout mainLayout;
    private TextView reservationDate;
    private TextView reservationTime;
    private TextView userName;
    private TextView userPhone;
    private TextView specialRequestText;
    private int fontColor;
    private int backColor;
    private boolean colorSchema;
    private String orderUUID;
    private Thread workerThread;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case SHOW_PROGRESS_DIALOG: {
                    showProgressDialog();
                }
                break;
                case HIDE_PROGRESS_DIALOG: {
                    hideProgressDialog();
                }
                break;
                case START_SUMMARY: {
                    startSummaryActivity();
                }
                break;
                case MAIL_SEND_ERROR: {
                    Log.d("MAIL_SEND_ERROR", "");
                }
                break;
                case ADD_REQUEST_ERROR: {
                    Toast toast = Toast.makeText(TableReservationReservation.this,
                            R.string.tablereservation_warning_timeout, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 95);
                    toast.show();
                    Log.d("", "");
                }
                break;
            }
        }
    };

    /**
     * Called when the activity is first created.
     */
    @Override
    public void create() {

        setContentView(R.layout.sergeyb_tablereservation_reservation);
        layoutInflater = LayoutInflater.from(this);

        setTopBarTitle(getResources().getString(R.string.tablereservation_reservation));
        swipeBlock();
        setTopBarLeftButtonText(getResources().getString(R.string.common_back_upper), true, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        setTopBarRightButtonText(getResources().getString(R.string.common_modify_upper), false, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bridgeIntent = new Intent(TableReservationReservation.this,
                        TableReservationModify.class);
                bridgeIntent.putExtra("xml", orderInfo);
                bridgeIntent.putExtra("cachePath", cachePath);
                bridgeIntent.putExtra("specRequest", orderInfo.getSpecialRequest());
                bridgeIntent.putExtra("fontColor", fontColor);
                bridgeIntent.putExtra("backColor", backColor);
                bridgeIntent.putExtra("colorSchema", colorSchema);
                startActivityForResult(bridgeIntent, MODIFY);
            }
        });

        // get UI element links
        mainLayout = (LinearLayout) findViewById(R.id.sergeyb_tablereservation_main_layout);
        reservationDate = (TextView) findViewById(R.id.sergeyb_tablereservation_reservation_date);
        reservationTime = (TextView) findViewById(R.id.sergeyb_tablereservation_reservation_time_and_persons);
        userName = (TextView) findViewById(R.id.sergeyb_tablereservation_reservation_username);
        userPhone = (TextView) findViewById(R.id.sergeyb_tablereservation_reservation_userphone);
        specReqButton = (LinearLayout) findViewById(R.id.sergeyb_tablereservation_special_request);
        confirmButton = (LinearLayout) findViewById(R.id.sergeyb_tablereservation_confirm);
        specialRequestText = (TextView) findViewById(R.id.sergeyb_tablereservation_special_request_text);
        emailText = (TextView) findViewById(R.id.sergeyb_tablereservation_reservation_email);

        // getting info from parent activity
        Intent currentIntent = getIntent();
        cachePath = currentIntent.getStringExtra("cachePath");
        user = (User) currentIntent.getSerializableExtra("userinfo");
        orderInfo = (TableReservationInfo) currentIntent.getSerializableExtra("xml");
        fontColor = currentIntent.getIntExtra("fontColor", Color.WHITE);
        backColor = currentIntent.getIntExtra("backColor", Color.parseColor("#37393b"));
        colorSchema = currentIntent.getBooleanExtra("colorSchema", true);
        if (colorSchema) // dark scheme
        {
            specReqButton.setBackgroundResource(R.drawable.sergeyb_tablereservation_back_transperant_15alpha_dark);
        } else {
            specReqButton.setBackgroundResource(R.drawable.sergeyb_tablereservation_back_transperant_15alpha);
        }

        // choose layout to inflate
        if (orderInfo.getRestaurantimageurl() != null) {
            headerView = layoutInflater.inflate(R.layout.sergeyb_tablereservation_reservation_with_img, null);
            banner = (ImageView) headerView.findViewById(R.id.sergeyb_tablereservation_restaurant_image);
            Bitmap temp = decodeImageFile(orderInfo.getrestaurantimageFilePath());
            if (temp != null) {
                banner.setImageBitmap(temp);
            }

        } else {
            headerView = layoutInflater.inflate(R.layout.sergeyb_tablereservation_reservation_no_image, null);
            restaurantName = (TextView) headerView.findViewById(R.id.sergeyb_tablereservation_restaurant_name);
            restaurantName.setText(orderInfo.getRestaurantName());
            restaurantGreetings = (TextView) headerView.findViewById(R.id.sergeyb_tablereservation_restaurant_greetings);
            restaurantGreetings.setText(orderInfo.getRestaurantGreeting());
        }

        // set appropriate colors to back and textviews
        mainLayout.setBackgroundColor(backColor);
        reservationDate.setTextColor(fontColor);
        reservationTime.setTextColor(fontColor);
        emailText.setTextColor(fontColor);
        userName.setTextColor(fontColor);
        userPhone.setTextColor(fontColor);
        specialRequestText.setTextColor(fontColor);

        // assign layout to parent
        custom = (LinearLayout) findViewById(R.id.sergeyb_tablereservation_custom_layout);
        LinearLayout.LayoutParams custom_params =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                        LinearLayout.LayoutParams.FILL_PARENT);
        custom.addView(headerView, custom_params);

        String reservationDateString = "";

        if (Locale.getDefault().toString().equals("en_US")) {
            reservationDateString = new SimpleDateFormat("EEEEE, MMMMM dd").format(orderInfo.getOrderDate());
        } else if (Locale.getDefault().toString().equals("ru_RU")) {
            reservationDateString = new SimpleDateFormat("EEEEE, dd MMMMM").format(orderInfo.getOrderDate());
        }

        reservationDate.setText(reservationDateString);
        HouresMinutes orderTime = orderInfo.getOrderTime();

        String time = "";
        if (Locale.getDefault().toString().equals("ru_RU")) {
            try {
                String persons = new PluralResources(
                        this.getResources()).getQuantityString(R.plurals.orderTableForPerson,
                        orderInfo.getPersonsAmount(), orderInfo.getPersonsAmount());
                time = Utils.convertTimeToFormat(orderTime.houres, orderTime.minutes, true) + " " + persons;

            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        } else {
            time = Utils.convertTimeToFormat(orderTime.houres, orderTime.minutes, false)
                    + " for " + Integer.toString(orderInfo.getPersonsAmount()) + " people";
        }
        reservationTime.setText(time);

        // handling user name
        if (user.getAccountType() == User.ACCOUNT_TYPES.GUEST) {
            userName.setText(getResources().getString(R.string.tablereservation_guest));
        } else if (user.getAccountType() == User.ACCOUNT_TYPES.IBUILDAPP) {
            userName.setText(user.getUserName());
        } else if (user.getAccountType() == User.ACCOUNT_TYPES.TWITTER) {
            userName.setText(user.getUserName());
        } else {
            userName.setText(user.getUserFirstName() + " " + user.getUserLastName());
        }

        // handling user phone
        if (orderInfo.getPhoneNumber() != null) {
            if (orderInfo.getPhoneNumber().length() == 0) {
                userPhone.setVisibility(View.GONE);
            } else {
                userPhone.setVisibility(View.VISIBLE);
                userPhone.setText(orderInfo.getPhoneNumber());
            }
        } else {
            userPhone.setVisibility(View.GONE);
        }

        // handling user mail
        if (orderInfo.getCustomerEmail() != null) {
            if (orderInfo.getCustomerEmail().length() == 0) {
                emailText.setVisibility(View.GONE);
            } else {
                emailText.setVisibility(View.VISIBLE);
                emailText.setText(orderInfo.getCustomerEmail());
            }
        } else {
            emailText.setVisibility(View.GONE);
        }

        // add special request handler
        specReqButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Intent bridgeIntent = new Intent(TableReservationReservation.this,
                        TableReservationSpecialRequest.class);
                bridgeIntent.putExtra("specRequest", orderInfo.getSpecialRequest());
                bridgeIntent.putExtra("backColor", backColor);
                bridgeIntent.putExtra("fontColor", fontColor);
                bridgeIntent.putExtra("colorSchema", colorSchema);
                startActivityForResult(bridgeIntent, SPECIAL_REQUEST);
            }
        });

        // confirm button handler
        confirmButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                // checking Internet connection
                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo ni = cm.getActiveNetworkInfo();
                if (!((ni != null) && (ni.isConnectedOrConnecting()))) {
                    Toast toast = Toast.makeText(TableReservationReservation.this, R.string.alert_no_internet, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 95);
                    toast.show();
                    return;
                }

                handler.sendEmptyMessage(SHOW_PROGRESS_DIALOG);
                workerThread = new Thread(new Runnable() {
                    public void run() {
                        // send http reques
                        orderUUID = TableReservationHTTP.sendAddOrderRequest(user, handler, orderInfo);

                        // send email notification
                        // 1 - send confirmation mail to owner
                        // 2 - send cancellation mail to owner
                        // 3 - send confirmation mail to customer
                        // 4 - send cancellation mail to customer
                        if (orderUUID != null) {
                            // send confirmation email to customer
                            if (user.getUserEmail() != null) {
                                if (user.getUserEmail().length() != 0) {
                                    TableReservationHTTP.sendMail(3, user, orderInfo,
                                            getResources().getString(R.string.app_name), handler);
                                }
                            }
                            handler.sendEmptyMessage(START_SUMMARY);
                        }
                        handler.sendEmptyMessage(HIDE_PROGRESS_DIALOG);
                    }
                });
                workerThread.start();
            }
        });
    }

    /**
     * Calling when child activity finished it's work and trying to return
     * result.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case SPECIAL_REQUEST: {
                if (resultCode == RESULT_OK) {
                    orderInfo.setSpecialRequest(data.getStringExtra("special"));
                }
            }
            break;

            case MODIFY: {
                if (resultCode == RESULT_OK) {
                    // updte orderInfo
                    orderInfo.setOrderDate((Date) data.getSerializableExtra("date"));
                    HouresMinutes tempTime = (HouresMinutes) data.getSerializableExtra("time");
                    orderInfo.setOrderTime(tempTime.houres, tempTime.minutes, tempTime.am_pm);
                    orderInfo.setPhoneNumber(data.getStringExtra("phone"));
                    orderInfo.setCustomerEmail(data.getStringExtra("email"));
                    orderInfo.setSpecialRequest(data.getStringExtra("special"));
                    orderInfo.setPersonsAmount(Integer.parseInt(data.getStringExtra("persons")));

                    // update UI textviews
                    String reservationDateString = null;
                    if (Locale.getDefault().toString().equals("en_US")) {
                        reservationDateString = new SimpleDateFormat("EEEEE, MMMMM dd").format(orderInfo.getOrderDate());
                    } else if (Locale.getDefault().toString().equals("ru_RU")) {
                        reservationDateString = new SimpleDateFormat("EEEEE, dd MMMMM").format(orderInfo.getOrderDate());
                    }
                    reservationDate.setText(reservationDateString);

                    HouresMinutes orderTime = orderInfo.getOrderTime();
                    String time = "";
                    if (Locale.getDefault().toString().equals("ru_RU")) {
                        try {
                            String persons = new PluralResources(
                                    this.getResources()).getQuantityString(R.plurals.orderTableForPerson,
                                    orderInfo.getPersonsAmount(), orderInfo.getPersonsAmount());
                            //time = persons;
                            time = Utils.convertTimeToFormat(orderTime.houres, orderTime.minutes, true) + " " + persons;

                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        }
                    } else {
                        time = Utils.convertTimeToFormat(orderTime.houres, orderTime.minutes, false)
                                + " for " + Integer.toString(orderInfo.getPersonsAmount()) + " people";
                    }

                    reservationTime.setText(time);

                    // handling user phone
                    if (TextUtils.isEmpty(orderInfo.getPhoneNumber()))
                        userPhone.setVisibility(View.GONE);
                    else {
                        userPhone.setVisibility(View.VISIBLE);
                        userPhone.setText(orderInfo.getPhoneNumber());
                    }

                    // handling user mail
                    if (TextUtils.isEmpty(orderInfo.getCustomerEmail())) {
                        emailText.setVisibility(View.GONE);
                    } else {
                        emailText.setVisibility(View.VISIBLE);
                        emailText.setText(orderInfo.getCustomerEmail());
                    }
                }
            }
            break;

            case CONFIRM: {
                String closeAction = data.getStringExtra("howtoclose");
                if (closeAction.length() != 0 && closeAction != null) {
                    Intent fault = new Intent();
                    setResult(RESULT_OK, fault);
                    finish();
                }
            }
            break;
        }
    }

    /**
     * Decodes image file to bitmap from device external storage.
     *
     * @param imagePath image file path
     * @return decoded image bitmap
     */
    private Bitmap decodeImageFile(String imagePath) {
        try {
            File file = new File(imagePath);
            //Decode image size
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(file), null, opts);

            //Find the correct scale value. It should be the power of 2.
            int height = opts.outHeight;
            int scale = 1;
            while (true) {
                if (height / 2 < IMAGE_HEIGHT) {
                    break;
                }
                height /= 2;
                scale *= 2;
            }
            //Decode with inSampleSize
            opts = new BitmapFactory.Options();
            opts.inSampleSize = scale;

            return BitmapFactory.decodeStream(new FileInputStream(file), null, opts);
        } catch (Exception e) {
            Log.d("", "");
        }

        return null;
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = ProgressDialog.show(this, null, getResources().getString(R.string.common_loading_upper), true, true, new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface arg0) {
                    try {
                        if (workerThread != null && workerThread.isAlive()) {
                            workerThread.interrupt();
                        }
                        progressDialog.dismiss();
                        progressDialog = null;
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
            progressDialog = null;
        }
    }

    /**
     * Starts TableReservationSummary to view reservation summary.
     */
    private void startSummaryActivity() {
        // start Summary intent
        Intent bridgeIntent = new Intent(TableReservationReservation.this,
                TableReservationSummary.class);
        bridgeIntent.putExtra("orderUUID", orderUUID);
        bridgeIntent.putExtra("userinfo", user);
        bridgeIntent.putExtra("xml", orderInfo);
        bridgeIntent.putExtra("fontColor", fontColor);
        bridgeIntent.putExtra("backColor", backColor);
        bridgeIntent.putExtra("startPoint", "summary");
        startActivityForResult(bridgeIntent, CONFIRM);
    }
}
