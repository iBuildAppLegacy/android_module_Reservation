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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.http.SslError;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;
import com.appbuilder.sdk.android.AppBuilderModuleMain;
import com.appbuilder.sdk.android.authorization.entities.User;
import com.ibuildapp.romanblack.TableReservationPlugin.utils.TableReservationHTTP;
import com.ibuildapp.romanblack.TableReservationPlugin.utils.Utils;
import com.seppius.i18n.plurals.PluralResources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This activity represents reservation summary page.
 */
public class TableReservationSummary extends AppBuilderModuleMain {

    private final int SHOW_PROGRESS_DIALOG = 0;
    private final int HIDE_PROGRESS_DIALOG = 1;
    private final int DRAW_IMAGE_MAP = 2;
    private final int CANCEL_REQUEST_ERROR = 3;
    private final int DRAW_MARGINS = 4;
    private final int SHOW_MAP_ACTIVIRY = 10001;
    private LinearLayout tableTextLayout;
    private LinearLayout footerLayout;
    private LinearLayout infoLayout;
    private LinearLayout contentLayout;
    private int tableTextLayoutH = -1;
    private int footerLayoutH = -1;
    private int infoLayoutH = -1;
    private int contentLayoutH = -1;
    private boolean isPressed = false;
    private LinearLayout emailButton;
    private LinearLayout mailCalendarHolder;
    private LinearLayout restNameHolder;
    private LinearLayout calendarButton;
    private LinearLayout directioinButton;
    private LinearLayout okButton;
    private LinearLayout mapHolder;
    private TextView addressText;
    private TextView additionalText;
    private TextView orderDateText;
    private TextView personsText;
    private TextView restaurantText;
    private TextView addressPointer;
    private TextView kitchen;
    private TextView additionalPointer;
    private Resources res;
    private WebView map;
    private RelativeLayout mainLayout;
    private int fontColor;
    private int backColor;
    private User user;
    private String orderUUID;
    private String reservationTime;
    private String startPoint = null;
    private TableReservationInfo orderInfo;
    private String htmlSource;
    private ProgressDialog progressDialog = null;
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
                case DRAW_IMAGE_MAP: {
                    drawImageMap();
                    hideProgressDialog();
                }
                break;
                case CANCEL_REQUEST_ERROR: {
                    Toast toast = Toast.makeText(TableReservationSummary.this,
                            R.string.tablereservation_warning_timeout, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 95);
                    toast.show();
                    Log.d("", "");
                }
                break;
                case DRAW_MARGINS: {
                    setMargins();
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
        setContentView(R.layout.sergeyb_tablereservation_summary);
        res = getResources();

        setTopBarTitle(getResources().getString(R.string.tablereservation_summary));
        swipeBlock();
        setTopBarLeftButtonText(getResources().getString(R.string.common_back_upper), true, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (startPoint == null) {
                    closeActivity();
                } else if (startPoint.compareTo("summary") == 0) {
                    closeActivityOnCancel();
                }
            }
        });

        // always draw right button
        View v = LayoutInflater.from(this).inflate(R.layout.romanblack_tablereservation_listbutton, null);
        ImageView img = (ImageView) v.findViewById(R.id.image);
        BitmapDrawable b_png = (BitmapDrawable) img.getDrawable();
        b_png.setColorFilter(navBarDesign.itemDesign.textColor, PorterDuff.Mode.MULTIPLY);
        img.setImageDrawable(b_png);

        setTopBarRightButton(v, getString(R.string.common_modify_upper), new View.OnClickListener() {
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
                    Toast toast = Toast.makeText(TableReservationSummary.this,
                            R.string.alert_no_internet, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 95);
                    toast.show();
                    return;
                }

                // start confirmation activity
                Intent mainAndReservationBridge = new Intent(TableReservationSummary.this, TableReservatioinListOfReservations.class);
                mainAndReservationBridge.putExtra("userinfo", user);
                mainAndReservationBridge.putExtra("xml", orderInfo);
                mainAndReservationBridge.putExtra("fontColor", fontColor);
                mainAndReservationBridge.putExtra("backColor", backColor);
                startActivity(mainAndReservationBridge);
            }
        });

        // getting info from parent activity
        Intent currentIntent = getIntent();
        orderInfo = (TableReservationInfo) currentIntent.getSerializableExtra("xml");
        fontColor = currentIntent.getIntExtra("fontColor", Color.WHITE);
        backColor = currentIntent.getIntExtra("backColor", Color.parseColor("#37393b"));
        user = (User) currentIntent.getSerializableExtra("userinfo");
        orderUUID = currentIntent.getStringExtra("orderUUID");
        reservationTime = currentIntent.getStringExtra("reservationTIme");
        startPoint = currentIntent.getStringExtra("startPoint");

        // getting UI links
        emailButton = (LinearLayout) findViewById(R.id.sergeyb_tablereservation_mail);
        calendarButton = (LinearLayout) findViewById(R.id.sergeyb_tablereservation_calendar);
        directioinButton = (LinearLayout) findViewById(R.id.sergeyb_tablereservation_direction);
        okButton = (LinearLayout) findViewById(R.id.sergeyb_tablereservation_ok);
        mainLayout = (RelativeLayout) findViewById(R.id.sergeyb_tablereservation_main_layout);
        addressText = (TextView) findViewById(R.id.sergeyb_tablereservation_address);
        additionalText = (TextView) findViewById(R.id.sergeyb_tablereservation_additional);
        orderDateText = (TextView) findViewById(R.id.sergeyb_tablereservation_order_date);
        personsText = (TextView) findViewById(R.id.sergeyb_tablereservation_persons);
        restaurantText = (TextView) findViewById(R.id.sergeyb_tablereservation_restautant_name);
        addressPointer = (TextView) findViewById(R.id.sergeyb_tablereservation_address_pointer);
        additionalPointer = (TextView) findViewById(R.id.sergeyb_tablereservation_additional_pointer);
        infoLayout = (LinearLayout) findViewById(R.id.sergeyb_tablereservation_info_layout);
        mapHolder = (LinearLayout) findViewById(R.id.sergeyb_tablereservation_map_holder);
        restNameHolder = (LinearLayout) findViewById(R.id.sergeyb_tablereservation_restname_holder);
        kitchen = (TextView) findViewById(R.id.sergeyb_tablereservation_restautant_kitchen);
        mailCalendarHolder = (LinearLayout) findViewById(R.id.sergeyb_tablereservation_mail_calendar_holder);
        tableTextLayout = (LinearLayout) findViewById(R.id.sergeyb_tablereservation_table_text);
        footerLayout = (LinearLayout) findViewById(R.id.sergeyb_tablereservation_footer);
        contentLayout = (LinearLayout) findViewById(R.id.sergeyb_tablereservation_content_holder);

        // computing block's sizes
        infoLayout.post(new Runnable() {
            public void run() {
                infoLayoutH = infoLayout.getHeight();
            }
        });
        contentLayout.post(new Runnable() {
            public void run() {
                contentLayoutH = contentLayout.getHeight();
            }
        });
        tableTextLayout.post(new Runnable() {
            public void run() {
                tableTextLayoutH = tableTextLayout.getHeight();
            }
        });
        footerLayout.post(new Runnable() {
            public void run() {
                footerLayoutH = footerLayout.getHeight();
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (infoLayoutH != -1 && tableTextLayoutH != -1
                            && footerLayoutH != -1 && contentLayoutH != -1) {
                        handler.sendEmptyMessage(DRAW_MARGINS);
                        break;
                    }
                }
            }
        }).start();

        // hide unnesessary elements
        if (reservationTime != null && reservationTime.length() != 0) {
            mailCalendarHolder.setVisibility(View.GONE);
            okButton.setVisibility(View.GONE);
        }

        // set appropriate font color and back color
        mainLayout.setBackgroundColor(backColor);
        restaurantText.setTextColor(fontColor);
        addressText.setTextColor(fontColor);
        orderDateText.setTextColor(fontColor);
        personsText.setTextColor(fontColor);
        additionalText.setTextColor(fontColor);
        addressPointer.setTextColor(fontColor);
        additionalPointer.setTextColor(fontColor);
        kitchen.setTextColor(fontColor);

        // set up text views
        addressText.setText(orderInfo.getRestaurantadress());
        additionalText.setText(orderInfo.getRestaurantadditional());

        Date tempDate = orderInfo.getOrderDate();

        SimpleDateFormat simpleDateFormat;
        if (Locale.getDefault().toString().equals("ru_RU")) {
            simpleDateFormat = new SimpleDateFormat("EEEEE HH:mm, d MMMMM yyyy");
            orderDateText.setText(simpleDateFormat.format(tempDate));
        } else {
            String dayOfWeek = new SimpleDateFormat("E").format(orderInfo.getOrderDate());
            String Month = new SimpleDateFormat("MMMMMM").format(orderInfo.getOrderDate());
            String Day = new SimpleDateFormat("d").format(orderInfo.getOrderDate());
            String Year = new SimpleDateFormat("y").format(orderInfo.getOrderDate());
            String time = Utils.convertTimeToFormat(orderInfo.getOrderTime().houres, orderInfo.getOrderTime().minutes, false);
            orderDateText.setTypeface(null, Typeface.BOLD);
            orderDateText.setText(dayOfWeek + ", " + Month + " " + Day + ", " + Year + ", " + time);
        }

        if (Locale.getDefault().toString().equals("ru_RU")) {
            try {
                String text = new PluralResources(this.getResources()).getQuantityString(R.plurals.orderTableForPerson,
                        orderInfo.getPersonsAmount(), orderInfo.getPersonsAmount());
                personsText.setText(text);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        } else {

            if (orderInfo.getPersonsAmount() == 1) {
                personsText.setText("Table for " + Integer.toString(orderInfo.getPersonsAmount()) + " person");
            } else {
                personsText.setText("Table for " + Integer.toString(orderInfo.getPersonsAmount()) + " persons");
            }
        }
        restaurantText.setText(orderInfo.getRestaurantName());
        kitchen.setText(orderInfo.getKitchen());

        // adjusting map settings 
        map = (WebView) findViewById(R.id.sergeyb_tablereservation_map);
        map.setBackgroundColor(Color.BLACK);
        map.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        map.getSettings().setJavaScriptEnabled(true);
//        map.getSettings().setPluginsEnabled(true);
        map.getSettings().setGeolocationEnabled(true);
        map.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!isPressed) {
                    Intent Bridge = new Intent(TableReservationSummary.this, TableReservationMap.class);
                    Bridge.putExtra("xml", orderInfo);
                    startActivityForResult(Bridge, SHOW_MAP_ACTIVIRY);
                    isPressed = true;
                }

                return true;
            }
        });

        map.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                handler.sendEmptyMessage(SHOW_PROGRESS_DIALOG);
            }

            @Override
            public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(TableReservationSummary.this);
                builder.setMessage(R.string.notification_error_ssl_cert_invalid);
                builder.setPositiveButton(TableReservationSummary.this.getResources().getString(R.string.on_continue), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.proceed();
                    }
                });
                builder.setNegativeButton(TableReservationSummary.this.getResources().getString(R.string.on_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.cancel();
                    }
                });
                final AlertDialog dialog = builder.create();
                dialog.show();
            }
            @Override
            // callback which calls when page finish
            // @param url - url of web view page
            // @param view - link to webview
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                handler.sendEmptyMessage(HIDE_PROGRESS_DIALOG);
            }
        });

        // preparing html content for small webview
        htmlSource = "";
        try {
            // get html source from resources
            InputStream is = getResources().openRawResource(R.raw.sergeyb_tablereservation_page_mini);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int flag = 0;
            byte buf[] = new byte[512];
            while ((flag = is.read(buf, 0, 512)) != -1) {
                baos.write(buf, 0, flag);
                Arrays.fill(buf, (byte) 0);
            }
            htmlSource = baos.toString();
        } catch (IOException iOEx) {
            Log.e("", "");
        }

        String coordinates = createPoints(orderInfo.getLatitude(),
                orderInfo.getLongitude(), orderInfo.getRestaurantName(),
                orderInfo.getRestaurantadditional());
        htmlSource = htmlSource.replace("__RePlAcE-Points__", coordinates);
        htmlSource = htmlSource.replace("__RePlAcE-Lat__", Double.toString(orderInfo.getLatitude()));
        htmlSource = htmlSource.replace("__RePlAcE-Lng__", Double.toString(orderInfo.getLongitude()));
        htmlSource = htmlSource.replace("__RePlAcE-Zoom__", "15");

        // check internet connection
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni != null && ni.isConnectedOrConnecting()) {
            map.loadDataWithBaseURL("", htmlSource, "text/html", "utf-8", "");
        }

        // email button handler
        emailButton.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                // checking Internet connection
                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo ni = cm.getActiveNetworkInfo();
                if (!((ni != null) && (ni.isConnectedOrConnecting()))) {
                    Toast toast = Toast.makeText(TableReservationSummary.this, R.string.alert_no_internet, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 95);
                    toast.show();
                } else {
                    // get date string 
                    try {
                        String reservationDateString = null;
                        if (Locale.getDefault().toString().equals("en_US")) {
                            reservationDateString = new SimpleDateFormat("EEEEE, MMMMM dd").format(orderInfo.getOrderDate());
                        } else if (Locale.getDefault().toString().equals("ru_RU")) {
                            reservationDateString = new SimpleDateFormat("EEEEE, dd MMMMM").format(orderInfo.getOrderDate());
                        }

                        // get time string
                        String temp_time = "";
                        if (Locale.getDefault().toString().equals("ru_RU")) {
                            temp_time = Utils.convertTimeToFormat(orderInfo.getOrderTime().houres,
                                    orderInfo.getOrderTime().minutes, true);
                        } else {
                            temp_time = Utils.convertTimeToFormat(orderInfo.getOrderTime().houres,
                                    orderInfo.getOrderTime().minutes, false);
                        }

                        String persons = null;
                        if (Locale.getDefault().toString().equals("ru_RU")) {
                            try {
                                persons = new PluralResources(
                                        res).getQuantityString(R.plurals.orderTableForPerson,
                                        orderInfo.getPersonsAmount(), orderInfo.getPersonsAmount());
                            } catch (NoSuchMethodException e) {
                                e.printStackTrace();
                            }
                        } else {
                            persons = "Reserved for " + Integer.toString(orderInfo.getPersonsAmount()) + " people";
                        }

                        // creating google map link
                        String mapLink = "https://maps.google.com/maps?q="
                                + Double.toString(orderInfo.getLatitude()) + ","
                                + Double.toString(orderInfo.getLongitude()) + "&ll="
                                + Double.toString(orderInfo.getLatitude()) + ","
                                + Double.toString(orderInfo.getLongitude()) + "&z=15";
                        mapLink = "<a href=\"" + mapLink + "\" > Location</a>";

                        // result message
                        String mailText = orderInfo.getRestaurantName() + "<br/>"
                                + orderInfo.getRestaurantadress() + "<br/>"
                                + orderInfo.getRestaurantName() + "<br/><br/>"
                                + reservationDateString + "<br/>"
                                + temp_time + "<br/>"
                                + persons + "<br/><br/>"
                                + mapLink;

                        Intent intent = chooseEmailClient();
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setType("text/html");
                        intent.putExtra(Intent.EXTRA_SUBJECT, "reservation for " + orderInfo.getRestaurantName());
                        intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(mailText));
                        startActivity(intent);

                    } catch (Exception e) {
                        Log.d("", "");
                    }
                }
            }
        });

        // calendar button handler
        calendarButton.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {

                long startMillis = 0;
                long endMillis = 0;
                Calendar beginTime = Calendar.getInstance();
                int tempHoures;
                int tempDate;
                // set up notification one hour before 
                if ((orderInfo.getOrderTime().houres - 1) < 0) {
                    tempHoures = 23;
                    tempDate = orderInfo.getOrderDate().getDate() - 1;
                } else {
                    tempHoures = orderInfo.getOrderTime().houres - 1;
                    tempDate = orderInfo.getOrderDate().getDate();
                }

                beginTime.set(orderInfo.getOrderDate().getYear() + 1900,
                        orderInfo.getOrderDate().getMonth(),
                        tempDate,
                        tempHoures,
                        orderInfo.getOrderTime().minutes);
                startMillis = beginTime.getTimeInMillis();
                Calendar endTime = Calendar.getInstance();
                endTime.set(orderInfo.getOrderDate().getYear() + 1900,
                        orderInfo.getOrderDate().getMonth(),
                        orderInfo.getOrderDate().getDate(),
                        orderInfo.getOrderTime().houres, orderInfo.getOrderTime().minutes);
                endMillis = endTime.getTimeInMillis();

                Intent intent = new Intent(Intent.ACTION_EDIT);
                intent.setType("vnd.android.cursor.item/event");
                intent.putExtra("beginTime", startMillis);
                intent.putExtra("allDay", false);
                intent.putExtra("rrule", "FREQ=YEARLY");
                intent.putExtra("endTime", endMillis);
                intent.putExtra("title", "Reservation for " + orderInfo.getRestaurantName());
                startActivity(intent);
            }
        });

        // direction button handler
        directioinButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo ni = cm.getActiveNetworkInfo();
                if (!((ni != null) && (ni.isConnectedOrConnecting()))) {
                    Toast toast = Toast.makeText(TableReservationSummary.this, R.string.alert_no_internet, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 95);
                    toast.show();
                    return;
                }

                Intent Bridge = new Intent(TableReservationSummary.this, TableReservationMap.class);
                Bridge.putExtra("xml", orderInfo);
                startActivity(Bridge);
            }
        });

        // add OK button handler
        okButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                // show allert dialog! 
                AlertDialog.Builder builder = new AlertDialog.Builder(TableReservationSummary.this);
                builder.setMessage(getResources().getString(R.string.tablereservation_warning_cancel_reservation));
                builder.setPositiveButton(getResources().getString(R.string.common_yes_upper), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        // checking Internet connection
                        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo ni = cm.getActiveNetworkInfo();
                        if (!((ni != null) && (ni.isConnectedOrConnecting()))) {
                            Toast toast = Toast.makeText(TableReservationSummary.this, R.string.alert_no_internet, Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 95);
                            toast.show();
                            return;
                        }

                        // send confirmation email to customer
                        if (user.getUserEmail() != null) {
                            if (user.getUserEmail().length() != 0) {
                                TableReservationHTTP.sendMail(4, user, orderInfo,
                                        getResources().getString(R.string.app_name), handler);
                            }
                        }

                        // send cancellation request
                        TableReservationHTTP.sendCancelOrderRequest(orderUUID, user, handler, orderInfo);

                        // show cancellation message
                        Toast.makeText(
                                TableReservationSummary.this,
                                getResources().getString(R.string.tablereservation_warning_reservation_canceled),
                                Toast.LENGTH_SHORT).show();

                        // back to Make Reservation activity
                        closeActivityOnCancel();
                    }
                });
                builder.setNegativeButton(getResources().getString(R.string.common_no_upper), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                    }
                });
                builder.create().show();
            }
        });

    }

    /**
     * Creates map point.
     * @param latitude point latitude
     * @param longitude point longitude
     * @param restname restaurant name
     * @param details restaurant description
     * @return map point JavaScript string
     */
    private String createPoints(Double latitude, Double longitude, String restname, String details) {
        String temp = "myMap.points.push({\n"
                + "latitude:" + Double.toString(latitude) + ",\n"
                + "longitude:" + Double.toString(longitude) + "\n"
                + "})\n\n";
        return temp;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case SHOW_MAP_ACTIVIRY: {
                isPressed = false;
            }
            break;
        }
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = ProgressDialog.show(this, null, getResources().getString(R.string.common_loading_upper), true, true, new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface arg0) {
                    try {
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
     * Draws map image.
     */
    private void drawImageMap() {
    }

    /**
     * Closes activity with "Cancel" result.
     */
    private void closeActivityOnCancel() {
        Intent resIntent = new Intent();
        resIntent.putExtra("howtoclose", "root");
        setResult(RESULT_OK, resIntent);
        finish();
    }

    /**
     * Closes activity with "OK" result.
     */
    private void closeActivity() {
        Intent resIntent = new Intent();
        resIntent.putExtra("howtoclose", "");
        setResult(RESULT_OK, resIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (startPoint == null) {
            closeActivity();
        } else if (startPoint.compareTo("summary") == 0) {
            closeActivityOnCancel();
        }
    }

    /**
     * Sets the margins.
     */
    private void setMargins() {
        float density = getResources().getDisplayMetrics().density;
        int freeSpace = contentLayoutH - infoLayoutH
                - footerLayoutH - tableTextLayoutH;
        if (freeSpace > 20) {
            freeSpace -= 15;
        }
        freeSpace /= 2;

        LinearLayout.LayoutParams retake_param =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        retake_param.setMargins((int) (20 * density), freeSpace, (int) (20 * density), 0);
        tableTextLayout.setLayoutParams(retake_param);
        footerLayout.setLayoutParams(retake_param);
    }

    /**
     * Choosing email client. Gmail has hight priority.
     *
     * @return prepared intent
     */
    private Intent chooseEmailClient() {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        final PackageManager pm = getPackageManager();
        final List<ResolveInfo> matches = pm.queryIntentActivities(intent, 0);
        ResolveInfo best = null;

        // trying to find gmail client
        for (final ResolveInfo info : matches) {
            if (info.activityInfo.packageName.endsWith(".gm")
                    || info.activityInfo.name.toLowerCase().contains("gmail")) {
                best = info;
            }
        }

        if (best == null) {
            // if there is no gmail client trying to fing internal email client
            for (final ResolveInfo info : matches) {
                if (info.activityInfo.name.toLowerCase().contains("mail")) {
                    best = info;
                }
            }
        }
        if (best != null) {
            intent.setClassName(best.activityInfo.packageName, best.activityInfo.name);
        }

        return intent;
    }
}
