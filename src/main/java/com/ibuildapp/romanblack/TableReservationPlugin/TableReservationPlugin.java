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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.appbuilder.sdk.android.AppBuilderModuleMain;
import com.appbuilder.sdk.android.StartUpActivity;
import com.appbuilder.sdk.android.Widget;
import com.appbuilder.sdk.android.authorization.Authorization;
import com.appbuilder.sdk.android.authorization.entities.User;
import com.ibuildapp.romanblack.TableReservationPlugin.utils.EntityParser;
import com.ibuildapp.romanblack.TableReservationPlugin.utils.TableReservationHTTP;
import com.ibuildapp.romanblack.TableReservationPlugin.utils.Utils;
import com.seppius.i18n.plurals.PluralResources;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

/**
 * Main module class. Module entry point. Represents table reservation widget.
 */
@StartUpActivity(moduleName = "Reservation")
public class TableReservationPlugin extends AppBuilderModuleMain {

    private final int LOGIN_ACTIVITY = 10005;
    private final int PERSON_PICKER_ACTIVITY = 10006;
    private final int SHOW_PROGRESS_DIALOG = 4;
    private final int HIDE_PROGRESS_DIALOG = 5;
    private final int DRAW_NOTIFICATION_VIEW = 6;
    private final int RESERVATION_ACTIVITY = 7;
    private final int NEED_INTERNET_CONNECTION = 8;
    // alert dialog layouts
    private View timePickerView;
    private View datePickerView;
    private Dialog mAlertDialog;
    private AlertDialog.Builder builder;
    private LinearLayout submit;
    private LinearLayout datePicker;
    private LinearLayout timePicker;
    private LinearLayout personPicker;
    private LinearLayout mainLayout;
    private LinearLayout notificationLayout;
    private Widget widget;
    private LayoutInflater layoutInflater;
    private User user = new User();
    private Thread imgDownloader;
    private int fontColor;
    private int backColor;
    private boolean isLoggedIn = false;
    private orderParsedInfo orderList;
    private TableReservationOrderInfo upcoming;
    private TextView dateTextView;
    private TextView timeTextView;
    private TextView personTextView;
    private TextView titleTextView;
    private TextView submitTextView;
    private ImageView datePickerImg;
    private ImageView timePickerImg;
    private ImageView personPickerImg;
    private TableReservationInfo parsedXML;
    private String cachePath;
    private ProgressDialog progressDialog = null;
    private boolean colorSchemeDark = false;
    // state of timepicker
    private Integer timePickerMinute;
    private Integer timePickerHour;
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
                case DRAW_NOTIFICATION_VIEW: {
                    drawNotificationView();
                }
                break;
                case NEED_INTERNET_CONNECTION: {
                    Toast toast = Toast.makeText(TableReservationPlugin.this,
                            R.string.alert_no_internet, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 95);
                    toast.show();
                }
                break;

            }
        }
    };

    @Override
    public void create() {
        upcoming = null;
        setContentView(R.layout.sergeyb_tablereservation_main);

        // topbar initialization
        setTopBarLeftButtonText(getResources().getString(R.string.common_home_upper), true, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // always draw right button
        View v = LayoutInflater.from(this).inflate(R.layout.romanblack_tablereservation_listbutton, null);
        ImageView img = (ImageView) v.findViewById(R.id.image);
        BitmapDrawable b_png = (BitmapDrawable) img.getDrawable();
        b_png.setColorFilter(navBarDesign.itemDesign.textColor, PorterDuff.Mode.MULTIPLY);
        img.setImageDrawable(b_png);

        setTopBarRightButton(v, getResources().getString(R.string.top_bar_right_button_text), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

                if (!isLoggedIn) {
                    Intent brigdeIntent = new Intent(TableReservationPlugin.this, TableReservationLogin.class);
                    brigdeIntent.putExtra("redirectTo", "redirectToOrderList");
                    brigdeIntent.putExtra("orderInfo", parsedXML);
                    brigdeIntent.putExtra("fontColor", fontColor);
                    brigdeIntent.putExtra("backColor", backColor);
                    brigdeIntent.putExtra("colorSchema", colorSchemeDark);
                    brigdeIntent.putExtra("appName", widget.getAppName());
                    startActivityForResult(brigdeIntent, LOGIN_ACTIVITY);
                } else {
                    // start confirmation activity
                    Intent mainAndReservationBridge = new Intent(TableReservationPlugin.this, TableReservatioinListOfReservations.class);
                    mainAndReservationBridge.putExtra("userinfo", user);
                    mainAndReservationBridge.putExtra("xml", parsedXML);
                    mainAndReservationBridge.putExtra("fontColor", fontColor);
                    mainAndReservationBridge.putExtra("backColor", backColor);
                    startActivity(mainAndReservationBridge);
                }
            }
        });

        // getting input data
        Intent currentIntent = getIntent();
        Bundle store = currentIntent.getExtras();
        widget = (Widget) store.getSerializable("Widget");

        if (widget == null) {
            Toast.makeText(TableReservationPlugin.this, R.string.alert_cannot_init, Toast.LENGTH_LONG).show();
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    finish();
                }
            }, 5000);
            return;
        }

        if (widget.getTitle() != null && widget.getTitle().length() != 0) {
            setTopBarTitle(widget.getTitle());
        } else {
            setTopBarTitle(getResources().getString(R.string.restaurant));
        }

        // set appropriate color to text and background
        mainLayout = (LinearLayout) findViewById(R.id.sergeyb_tablereservation_main_layout);
        titleTextView = (TextView) findViewById(R.id.sergeyb_tablereservation_title_text);
        dateTextView = (TextView) findViewById(R.id.sergeyb_tablereservation_date_textview);
        timeTextView = (TextView) findViewById(R.id.sergeyb_tablereservation_time_textview);
        personTextView = (TextView) findViewById(R.id.sergeyb_tablereservation_person_textview);
        submitTextView = (TextView) findViewById(R.id.sergeyb_tablereservation_submit_text);
        datePicker = (LinearLayout) findViewById(R.id.sergeyb_tablereservation_layoutDatePicker);
        timePicker = (LinearLayout) findViewById(R.id.sergeyb_tablereservation_layoutTimePicker);
        personPicker = (LinearLayout) findViewById(R.id.sergeyb_tablereservation_layoutPersonPicker);
        datePickerImg = (ImageView) findViewById(R.id.sergeyb_tablereservation_imageViewDate);
        timePickerImg = (ImageView) findViewById(R.id.sergeyb_tablereservation_imageViewTime);
        personPickerImg = (ImageView) findViewById(R.id.sergeyb_tablereservation_imageViewPerson);

        // parse XML
        EntityParser parser = new EntityParser(com.appbuilder.sdk.android.Utils.readXmlFromFile(widget.getPathToXmlFile()));
        parser.parse();
        parsedXML = parser.getTableReservationInfo();

        // set appropriate color
        backColor = parsedXML.colorskin.color1;
        fontColor = parsedXML.colorskin.color2;
        if (BackColorToFontColor(backColor) == Color.BLACK) // light background
        {
            colorSchemeDark = false;
            datePicker.setBackgroundResource(R.drawable.sergeyb_tablereservation_row_first);
            timePicker.setBackgroundResource(R.drawable.sergeyb_tablereservation_row_middle);
            personPicker.setBackgroundResource(R.drawable.sergeyb_tablereservation_row_last);

            datePickerImg.setImageResource(R.drawable.sergeyb_tablereservation_calendar_img_dark);
            timePickerImg.setImageResource(R.drawable.sergeyb_tablereservation_time_img_dark);
            personPickerImg.setImageResource(R.drawable.sergeyb_tablereservation_persons_img_dark);
        } else // dark background
        {
            datePicker.setBackgroundResource(R.drawable.sergeyb_tablereservation_row_first_dark);
            timePicker.setBackgroundResource(R.drawable.sergeyb_tablereservation_row_middle_dark);
            personPicker.setBackgroundResource(R.drawable.sergeyb_tablereservation_row_last_dark);

            datePickerImg.setImageResource(R.drawable.sergeyb_tablereservation_calendar_img);
            timePickerImg.setImageResource(R.drawable.sergeyb_tablereservation_time_img);
            personPickerImg.setImageResource(R.drawable.sergeyb_tablereservation_persons_img);
            colorSchemeDark = true;
        }

        mainLayout.setBackgroundColor(backColor);
        titleTextView.setTextColor(fontColor);
        dateTextView.setTextColor(fontColor);
        timeTextView.setTextColor(fontColor);
        personTextView.setTextColor(fontColor);

        // inflate layouts
        builder = new AlertDialog.Builder(this);
        timePickerView = timePickerViewinflate();
        datePickerView = datePickerViewinflate();

        // creating cache path
        cachePath = getExternalCacheDir().getAbsolutePath() + File.separator + "tablereservation-" + parsedXML.getModuleid();
        File cache = new File(cachePath);
        if (!cache.exists()) {
            cache.mkdirs();
        }

        // get user login info
        File userCache = new File(cachePath + "/usercache.data");
        if (userCache.exists()) {
            try {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(userCache));
                user = (User) ois.readObject();
                ois.close();
                isLoggedIn = true;
                parsedXML.setCustomerEmail(user.getUserEmail());
            } catch (Exception e) {
                Log.w("LOAD CONFIG", e);
            }
        } else if(Authorization.isAuthorized()) {
            try {
                user = Authorization.getAuthorizedUser();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(userCache, false));
                objectOutputStream.writeObject(user);
                objectOutputStream.flush();
                objectOutputStream.close();
                isLoggedIn = true;
                parsedXML.setCustomerEmail(user.getUserEmail());
            } catch(Exception exception) {
                exception.printStackTrace();
            }
        }

        // get user phone 
        File phoneCache = new File(cachePath + "/userphone.data");
        if (phoneCache.exists()) {
            try {
                FileInputStream rStream = new FileInputStream(phoneCache);
                byte[] buf = new byte[512];
                Arrays.fill(buf, (byte) 0);
                int count = rStream.read(buf, 0, 512);
                if (count != -1) {
                    String str = new String(buf, 0, count, "UTF8");
                    parsedXML.setPhoneNumber(str);
                }
            } catch (IOException ex) {
                Log.d("", "");
            }
        }

        // set default orderInfo params 
        parsedXML.setOrderDate(new Date());
        parsedXML.setOrderTime(parsedXML.getStartTime().houres,
                parsedXML.getStartTime().minutes, "");
        parsedXML.setPersonsAmount(2);

        // date time personts fields initialization
        dateTextView.setText(getResources().getString(R.string.common_date_upper));
        dateTextView.setTextColor(fontColor & 0x3CFFFFFF);
        timeTextView.setTextColor(fontColor & 0x3CFFFFFF);

        // set work houres
        String openTime, closeTime;
        if ((widget.getDateFormat() == 1) || Locale.getDefault().toString().equals("ru_RU")) // 24 format
        {
            openTime = Utils.convertTimeToFormat(parsedXML.getStartTime().houres, parsedXML.getStartTime().minutes, true);
            closeTime = Utils.convertTimeToFormat(parsedXML.getEndTime().houres, parsedXML.getEndTime().minutes, true);
        } else {
            openTime = Utils.convertTimeToFormat(parsedXML.getStartTime().houres, parsedXML.getStartTime().minutes, false);
            closeTime = Utils.convertTimeToFormat(parsedXML.getEndTime().houres, parsedXML.getEndTime().minutes, false);
        }
        timeTextView.setText(getResources().getString(R.string.common_time_upper) + " (" + openTime + " - " + closeTime + ")");

        personTextView.setText(getResources().getString(R.string.tablereservation_party_size));
        personTextView.setTextColor(fontColor & 0x3CFFFFFF);

        // set appropriate time to timePicker
        TimePicker mtimePicker = (TimePicker) timePickerView.findViewById(R.id.sergeyb_tablereservation_time_layout_timePicker);
        mtimePicker.setCurrentHour(parsedXML.getStartTime().houres);
        mtimePicker.setCurrentMinute(parsedXML.getStartTime().minutes);

        if (Locale.getDefault().toString().equals("ru_RU")) {
            mtimePicker.setIs24HourView(true);
        }

        DatePicker mdatePicker = (DatePicker) datePickerView.findViewById(R.id.sergeyb_tablereservation_date_layout_datePicker);
        mdatePicker.init(1900 + parsedXML.getOrderDate().getYear(),
                parsedXML.getOrderDate().getMonth(), parsedXML.getOrderDate().getDate(), null);

        // submit button handler
        submit = (LinearLayout) findViewById(R.id.sergeyb_tablereservation_submit_button);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // check fields for empyness
                if (dateTextView.getText().toString().contains(getResources().getString(R.string.common_date_upper))
                        || timeTextView.getText().toString().contains(getResources().getString(R.string.common_time_upper))
                        || personTextView.getText().toString().contains(getResources().getString(R.string.tablereservation_party_size))) {
                    String tempError = "";

                    if (dateTextView.getText().toString().equals(getResources().getString(R.string.common_date_upper))) {
                        if (tempError.length() > 0) {
                            tempError += getString(R.string.tablereservation_ampersand);
                        }
                        tempError += getResources().getString(R.string.common_date_upper);
                    }

                    if (timeTextView.getText().toString().contains(getResources().getString(R.string.common_time_upper))) {
                        if (tempError.length() > 0) {
                            tempError += getString(R.string.tablereservation_ampersand);
                        }
                        tempError += getResources().getString(R.string.common_time_upper);
                    }

                    if (personTextView.getText().toString().equals(getResources().getString(R.string.tablereservation_party_size))) {
                        if (tempError.length() > 0) {
                            tempError += getString(R.string.tablereservation_ampersand);
                        }
                        tempError += getResources().getString(R.string.tablereservation_party_size);
                    }

                    String errorStr = getResources().getString(R.string.tablereservation_common_specify_upper) + " " + tempError;

                    Toast toast = Toast.makeText(TableReservationPlugin.this, errorStr, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 95);
                    toast.show();
                    return;
                }

                // check time availability for pointed time
                Date tempDate = new Date(parsedXML.getOrderDate().getTime());
                tempDate.setHours(parsedXML.getOrderTime().houres);
                tempDate.setMinutes(parsedXML.getOrderTime().minutes);
                long orderTimeSec = tempDate.getTime();

                // check if order made earlier than now
                if (orderTimeSec <= (new Date().getTime() + parsedXML.getOffsetTime() * 1000)) {
                    Toast.makeText(TableReservationPlugin.this,
                            R.string.tablereservation_warning_too_late, Toast.LENGTH_LONG).show();
                    return;
                }

                // calculate restaurant wrok time
                long startTimeSec = -1;
                long endTimeSec = -1;
                if (parsedXML.getEndTime().houres > 24) {
                    int currentMinutes = parsedXML.getOrderTime().houres * 60 + parsedXML.getOrderTime().minutes;
                    int endTimeMinutes = (parsedXML.getEndTime().houres - 24) * 60 + parsedXML.getEndTime().minutes;
                    if (currentMinutes < endTimeMinutes) {
                        tempDate = parsedXML.getOrderDate();
                        tempDate.setHours(parsedXML.getStartTime().houres);
                        tempDate.setMinutes(parsedXML.getStartTime().minutes);
                        startTimeSec = (long) (tempDate.getTime() - (86400 * 1000));

                        tempDate = parsedXML.getOrderDate();
                        if (parsedXML.getEndTime().houres > 24) {
                            tempDate.setHours(parsedXML.getEndTime().houres - 24);
                        } else {
                            tempDate.setHours(parsedXML.getEndTime().houres);
                        }
                        tempDate.setMinutes(parsedXML.getEndTime().minutes);
                        endTimeSec = tempDate.getTime();
                    } else {
                        tempDate = parsedXML.getOrderDate();
                        tempDate.setHours(parsedXML.getStartTime().houres);
                        tempDate.setMinutes(parsedXML.getStartTime().minutes);
                        startTimeSec = tempDate.getTime();

                        tempDate = parsedXML.getOrderDate();
                        if (parsedXML.getEndTime().houres > 24) {
                            tempDate.setHours(parsedXML.getEndTime().houres - 24);
                        } else {
                            tempDate.setHours(parsedXML.getEndTime().houres);
                        }
                        tempDate.setMinutes(parsedXML.getEndTime().minutes);
                        if (parsedXML.getEndTime().houres > 24) {
                            endTimeSec = tempDate.getTime() + (86400 * 1000);
                        } else {
                            endTimeSec = tempDate.getTime();
                        }
                    }
                } else {
                    tempDate = parsedXML.getOrderDate();
                    tempDate.setHours(parsedXML.getStartTime().houres);
                    tempDate.setMinutes(parsedXML.getStartTime().minutes);
                    startTimeSec = tempDate.getTime();

                    tempDate = parsedXML.getOrderDate();
                    if (parsedXML.getEndTime().houres > 24) {
                        tempDate.setHours(parsedXML.getEndTime().houres - 24);
                    } else {
                        tempDate.setHours(parsedXML.getEndTime().houres);
                    }
                    tempDate.setMinutes(parsedXML.getEndTime().minutes);
                    if (parsedXML.getEndTime().houres > 24) {
                        endTimeSec = tempDate.getTime() + (86400 * 1000);
                    } else {
                        endTimeSec = tempDate.getTime();
                    }
                }

                if ((orderTimeSec >= startTimeSec) && (orderTimeSec <= (endTimeSec - parsedXML.getOffsetTime() * 1000))) {
                    if (!isLoggedIn) {
                        Intent brigdeIntent = new Intent(TableReservationPlugin.this, TableReservationLogin.class);
                        brigdeIntent.putExtra("redirectTo", "redirectToReservationDetails");
                        brigdeIntent.putExtra("orderInfo", parsedXML);
                        brigdeIntent.putExtra("fontColor", fontColor);
                        brigdeIntent.putExtra("backColor", backColor);
                        brigdeIntent.putExtra("colorSchema", colorSchemeDark);
                        brigdeIntent.putExtra("appName", widget.getAppName());

                        startActivityForResult(brigdeIntent, LOGIN_ACTIVITY);
                    } else {
                        // start confirmation activity
                        Intent mainAndReservationBridge = new Intent(TableReservationPlugin.this, TableReservationReservation.class);
                        mainAndReservationBridge.putExtra("userinfo", user);
                        mainAndReservationBridge.putExtra("cachePath", cachePath);
                        mainAndReservationBridge.putExtra("xml", parsedXML);
                        mainAndReservationBridge.putExtra("fontColor", fontColor);
                        mainAndReservationBridge.putExtra("backColor", backColor);
                        mainAndReservationBridge.putExtra("colorSchema", colorSchemeDark);
                        startActivityForResult(mainAndReservationBridge, RESERVATION_ACTIVITY);
                    }
                } else {
                    Toast toast = Toast.makeText(TableReservationPlugin.this,
                            R.string.tablereservation_warning_wrong_time, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 95);
                    toast.show();
                }
            }
        });

        // datePicker button handler
        datePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            protected Object clone() throws CloneNotSupportedException {
                return super.clone();
            }

            @Override
            public void onClick(View view) {
                // creating alert dialog
                ViewGroup mParent = (ViewGroup) datePickerView.getParent();
                if (mParent != null) {
                    mParent.removeAllViews();
                }

                // datepicker fields initialization 
                DatePicker mdatePicker = (DatePicker) datePickerView.findViewById(R.id.sergeyb_tablereservation_date_layout_datePicker);
                mdatePicker.init(1900 + parsedXML.getOrderDate().getYear(),
                        parsedXML.getOrderDate().getMonth(), parsedXML.getOrderDate().getDate(), null);

                builder.setView(datePickerView);
                mAlertDialog = builder.create();
                mAlertDialog.setOnCancelListener(new OnCancelListener() {
                    public void onCancel(DialogInterface arg0) {
                        if (mAlertDialog != null) {
                            mAlertDialog.dismiss();
                        }
                    }
                });

                mAlertDialog.show();
            }
        });

        // timePicker button handler
        timePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // creating alert dialog
                ViewGroup mParent = (ViewGroup) timePickerView.getParent();
                if (mParent != null) {
                    mParent.removeAllViews();
                }

                // set appropriate time to timePicker
                TimePicker mtimePicker = (TimePicker) timePickerView.findViewById(R.id.sergeyb_tablereservation_time_layout_timePicker);
                mtimePicker.setCurrentHour(parsedXML.getOrderTime().houres);
                mtimePicker.setCurrentMinute(parsedXML.getOrderTime().minutes);

                // creating alert dialog
                builder.setView(timePickerView);
                mAlertDialog = builder.create();

                mAlertDialog.setOnCancelListener(new OnCancelListener() {
                    public void onCancel(DialogInterface arg0) {
                        if (mAlertDialog != null) {
                            mAlertDialog.dismiss();
                        }
                    }
                });

                mAlertDialog.show();
            }
        });

        // personPicker button handler
        personPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bridge = new Intent(TableReservationPlugin.this,
                        TableReservationPersonPicker.class);
                bridge.putExtra("maxperson", parsedXML.getMaxpersons());
                startActivityForResult(bridge, PERSON_PICKER_ACTIVITY);

            }
        });

        // downloading restaurant banner
        handler.sendEmptyMessage(SHOW_PROGRESS_DIALOG);
        imgDownloader = new Thread(new Runnable() {
            public void run() {
                // download screen 
                try {
                    File mfile = new File(cachePath + "/restaurantbanner.jpg");
                    if (mfile.exists()) {
                        parsedXML.setrestaurantimageFilePath(cachePath + "/restaurantbanner.jpg");
                    } else {
                        if (parsedXML.getRestaurantimageurl() != null) {
                            splashScreenDownload(parsedXML.getRestaurantimageurl(), cachePath);
                            parsedXML.setrestaurantimageFilePath(cachePath + "/restaurantbanner.jpg");
                        }
                    }
                } catch (Exception e) {
                    Log.d("", "");
                }

                // check for upcoming reservation
                if (isLoggedIn) {
                    String requerRes = TableReservationHTTP.sendListOrdersRequest(user, parsedXML);
                    if (requerRes.compareToIgnoreCase("error") != 0) {
                        orderList = JSONParser.parseOrderList(requerRes);
                        upcoming = checkForUpcoming(orderList);
                        if (upcoming != null) {
                            // show ontop notification
                            handler.sendEmptyMessage(DRAW_NOTIFICATION_VIEW);
                        }
                    }
                }
                handler.sendEmptyMessage(HIDE_PROGRESS_DIALOG);
            }
        });
        imgDownloader.start();
    }

    /**
     * This method inflates view for time picker dialog.
     */
    private View timePickerViewinflate() {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(this);
        }

        View custom = layoutInflater.inflate(R.layout.sergeyb_tablereservation_time_picker, null);
        Button save = (Button) custom.findViewById(R.id.sergeyb_tablereservation_time_layout_buttonSave);
        Button cancel = (Button) custom.findViewById(R.id.sergeyb_tablereservation_time_layout_buttonCancel);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // getting root relative layout
                View parent = (View) view.getParent().getParent();
                TimePicker mpicker = (TimePicker) parent.findViewById(R.id.sergeyb_tablereservation_time_layout_timePicker);
                mpicker.clearChildFocus(getCurrentFocus());
                String dateTimeText;
                dateTimeText = Utils.convertTimeToFormat(mpicker.getCurrentHour(), mpicker.getCurrentMinute(), mpicker.is24HourView());
                timeTextView.setText(dateTimeText);
                timeTextView.setTextColor(fontColor);
                timePickerHour = mpicker.getCurrentHour();
                timePickerMinute = mpicker.getCurrentMinute();
                parsedXML.setOrderTime(timePickerHour, timePickerMinute, "AM");
                // close dialog if parametr are correct
                if (mAlertDialog != null) {
                    mAlertDialog.dismiss();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mAlertDialog != null) {
                    mAlertDialog.dismiss();
                }
            }
        });

        return custom;
    }

    /**
     * This method inflates view for date picker dialog.
     */
    private View datePickerViewinflate() {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(this);
        }

        View custom = layoutInflater.inflate(R.layout.sergeyb_tablereservation_date_picker, null);
        Button save = (Button) custom.findViewById(R.id.sergeyb_tablereservation_date_layout_buttonSave);
        Button cancel = (Button) custom.findViewById(R.id.sergeyb_tablereservation_date_layout_buttonCancel);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // getting root relative layout
                View parent = (View) view.getParent().getParent();
                DatePicker mpicker = (DatePicker) parent.findViewById(R.id.sergeyb_tablereservation_date_layout_datePicker);
                mpicker.clearChildFocus(getCurrentFocus());

                Date orderDate = new Date(mpicker.getYear() - 1900,
                        mpicker.getMonth(),
                        mpicker.getDayOfMonth());

                // check DD and MM and YYYY values and save theme
                parsedXML.setOrderDate(orderDate);

                String dataString = null;
                if (Locale.getDefault().toString().equals("ru_RU")) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    dataString = sdf.format(orderDate);
                } else {
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                    dataString = sdf.format(orderDate);
                }

                dateTextView.setText(dataString);
                dateTextView.setTextColor(fontColor);

                // close dialog if parametr are correct
                if (mAlertDialog != null) {
                    mAlertDialog.dismiss();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mAlertDialog != null) {
                    mAlertDialog.dismiss();
                }
            }
        });

        return custom;
    }

    /**
     * Calling when child activity finished it's work and trying to return
     * result
     *
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case LOGIN_ACTIVITY: {
                if (resultCode == RESULT_OK) {
                    String lastId = user.getAccountId();
                    user = (User) data.getSerializableExtra("user");

                    // let GUEST interract with application at one session with module
                    if (lastId != null) {
                        if (lastId.length() != 0) {
                            if (lastId != user.getAccountId()) {
                                user.setAccountId(lastId);
                            }
                        }
                    }

                    if (user.getUserEmail() != null && user.getUserEmail().length() != 0) {
                        parsedXML.setCustomerEmail(user.getUserEmail());
                    }
                    String redirectTo = data.getStringExtra("redirectTo");

                    // save user login info
                    if (user.getAccountType() != User.ACCOUNT_TYPES.GUEST) {
                        File cache = new File(cachePath + "/usercache.data");
                        if (!cache.exists()) {
                            try {
                                cache.createNewFile();
                                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(cache));
                                oos.writeObject(user);
                                oos.close();
                                Log.i("CACHE APP CONF", "success");
                            } catch (Exception e) {
                                Log.w("CACHE APP CONF", e);
                                cache.delete();
                            }
                        }
                        // let user interract with app at once
                        isLoggedIn = true;
                    }

                    // choose what activity to start
                    if (redirectTo.compareTo("redirectToOrderList") == 0) {
                        // start list of orders activity
                        Intent mainAndReservationBridge = new Intent(TableReservationPlugin.this, TableReservatioinListOfReservations.class);
                        mainAndReservationBridge.putExtra("userinfo", user);
                        mainAndReservationBridge.putExtra("xml", parsedXML);
                        mainAndReservationBridge.putExtra("fontColor", fontColor);
                        mainAndReservationBridge.putExtra("backColor", backColor);
                        startActivity(mainAndReservationBridge);

                    } else if (redirectTo.compareTo("redirectToReservationDetails") == 0) {
                        // start confirmation activity
                        Intent mainAndReservationBridge = new Intent(TableReservationPlugin.this, TableReservationReservation.class);
                        mainAndReservationBridge.putExtra("userinfo", user);
                        mainAndReservationBridge.putExtra("cachePath", cachePath);
                        mainAndReservationBridge.putExtra("xml", parsedXML);
                        mainAndReservationBridge.putExtra("fontColor", fontColor);
                        mainAndReservationBridge.putExtra("backColor", backColor);
                        mainAndReservationBridge.putExtra("colorSchema", colorSchemeDark);
                        startActivityForResult(mainAndReservationBridge, RESERVATION_ACTIVITY);
                    }

                } else {
                    Toast.makeText(TableReservationPlugin.this, R.string.alert_not_logged_in, Toast.LENGTH_LONG).show();
                }

            }
            break;

            case (RESERVATION_ACTIVITY): {
                if (resultCode == RESULT_OK) {
                    // clear UI fields
                    dateTextView.setText(getResources().getString(R.string.common_date_upper));
                    dateTextView.setTextColor(fontColor & 0x3CFFFFFF);

                    if (widget.getDateFormat() == 1) // 24 format
                    {
                        String openTime = Utils.convertTimeToFormat(parsedXML.getStartTime().houres,
                                parsedXML.getStartTime().minutes, true);
                        String closeTime = Utils.convertTimeToFormat(parsedXML.getEndTime().houres,
                                parsedXML.getEndTime().minutes, true);
                        timeTextView.setText(getResources().getString(R.string.common_time_upper) + " (" + openTime + " - " + closeTime + ")");
                    } else {
                        String openTime = Utils.convertTimeToFormat(parsedXML.getStartTime().houres,
                                parsedXML.getStartTime().minutes, false);
                        String closeTime = Utils.convertTimeToFormat(parsedXML.getEndTime().houres,
                                parsedXML.getEndTime().minutes, false);
                        timeTextView.setText(getResources().getString(R.string.common_date_upper) + " (" + openTime + " - " + closeTime + ")");
                    }
                    timeTextView.setTextColor(fontColor & 0x3CFFFFFF);

                    personTextView.setText(getResources().getString(R.string.tablereservation_party_size));
                    personTextView.setTextColor(fontColor & 0x3CFFFFFF);

                    // clear order details
                    parsedXML.setOrderDate(new Date());
                    parsedXML.setOrderTime(parsedXML.getStartTime().houres,
                            parsedXML.getStartTime().minutes, "");
                }
            }
            break;

            case (PERSON_PICKER_ACTIVITY): {
                if (resultCode == RESULT_OK) {
                    int amount = data.getIntExtra("persons", 2);
                    parsedXML.setPersonsAmount(amount);

                    String persons = "";
                    try {
                        persons = new PluralResources(getResources()).getQuantityString(R.plurals.tablereservation_persons_list, amount, amount);
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                    personTextView.setText(persons);
                    personTextView.setTextColor(fontColor);
                }
            }
            break;

        }
    }

    /**
     * Downloads the restaurant splash screen.
     * @param splashUrl the splash screen image URL
     * @param cachePath external storage path
     */
    private void splashScreenDownload(String splashUrl, String cachePath) {
        try {
            URL url = new URL(splashUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(10000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            // sending request ...
            conn.connect();
            if (conn.getResponseCode() == 200) {
                BufferedInputStream mstream = new BufferedInputStream(conn.getInputStream());
                File mfile = new File(cachePath + "/restaurantbanner.jpg");
                if (!mfile.exists()) {
                    mfile.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(mfile);
                byte buf[] = new byte[1024];
                int count = 0;
                while ((count = mstream.read(buf, 0, 1024)) != -1) {
                    fos.write(buf, 0, count);
                    Arrays.fill(buf, (byte) 0);
                }
                fos.flush();
                fos.close();
                mstream.close();
            }
        } catch (ProtocolException e) {
            Log.d("splashScreenDownload", e.getMessage());
        } catch (IOException e) {
            Log.d("splashScreenDownload", e.getMessage());
        } catch (NullPointerException e) {
            Log.d("splashScreenDownload", e.getMessage());
        }
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = ProgressDialog.show(this, null, getResources().getString(R.string.common_loading_upper), true, true, new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface arg0) {
                    try {
                        if (imgDownloader != null && imgDownloader.isAlive()) {
                            imgDownloader.interrupt();
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
     * Converts background color to font color.
     * @param backColor background color
     * @return font color
     */
    private int BackColorToFontColor(int backColor) {
        int r = (backColor >> 16) & 0xFF;
        int g = (backColor >> 8) & 0xFF;
        int b = (backColor >> 0) & 0xFF;

        double Y = (0.299 * r + 0.587 * g + 0.114 * b);
        if (Y > 127) {
            return Color.BLACK;
        } else {
            return Color.WHITE;
        }
    }

    /**
     * Checks for upcoming reservations.
     * @param orderList order list
     * @return upcoming order info
     */
    private TableReservationOrderInfo checkForUpcoming(orderParsedInfo orderList) {
        long curTime = new Date().getTime() + 1800000; // before 30 min
        Date maxDate = new Date();
        maxDate.setTime(10000);
        int orderNum = -1;

        Date curT = new Date();
        curT.setTime(curTime);

        for (int i = 0; i < orderList.itemsArray.size(); i++) {
            TableReservationOrderInfo tempOrder = orderList.itemsArray.get(i);
            Date orderDate = tempOrder.orderDate;
            orderDate.setHours(tempOrder.orderTime.houres);
            orderDate.setMinutes(tempOrder.orderTime.minutes);

            long orderTime = orderDate.getTime();
            if (new Date().getTime() < orderTime && orderTime < curTime) {
                if (orderTime > maxDate.getTime()) {
                    maxDate.setTime(orderTime);
                    orderNum = i;
                }
            }
        }

        if (orderNum != -1) {
            return orderList.itemsArray.get(orderNum);
        } else {
            return null;
        }
    }

    /**
     * Prepares and draws notification view.
     */
    private void drawNotificationView() {
        // show ontop notification
        notificationLayout = (LinearLayout) findViewById(R.id.sergeyb_tablereservation_notification);
        notificationLayout.setVisibility(View.VISIBLE);
        LinearLayout notificationButton = (LinearLayout) notificationLayout.findViewById(R.id.sergeyb_tablereservation_notification_button);
        notificationButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Intent bridgeIntent = new Intent(TableReservationPlugin.this,
                        TableReservationSummary.class);
                bridgeIntent.putExtra("fontColor", fontColor);
                bridgeIntent.putExtra("backColor", backColor);
                // preparing orderInfo
                parsedXML.setOrderDate(upcoming.orderDate);
                parsedXML.setOrderTime(upcoming.orderTime.houres,
                        upcoming.orderTime.minutes, upcoming.orderTime.am_pm);
                parsedXML.setPersonsAmount(upcoming.personsAmount);
                parsedXML.setSpecialRequest(upcoming.specRequest);
                bridgeIntent.putExtra("xml", parsedXML);
                bridgeIntent.putExtra("userinfo", user);
                bridgeIntent.putExtra("orderUUID", upcoming.uuid);
                startActivity(bridgeIntent);
                notificationLayout.setVisibility(View.GONE);
            }
        });
    }
}