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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.appbuilder.sdk.android.AppBuilderModuleMain;
import com.appbuilder.sdk.android.authorization.entities.User;
import com.ibuildapp.romanblack.TableReservationPlugin.utils.TableReservationHTTP;

import java.util.ArrayList;
import java.util.Date;

/**
 * This activity represents list of reservations page.
 */
public class TableReservatioinListOfReservations extends AppBuilderModuleMain {


    private final int SHOW_PROGRESS_DIALOG = 0;
    private final int HIDE_PROGRESS_DIALOG = 1;
    private final int DRAW_UI = 2;
    private final int CONNECTION_TIMEOUT = 3;
    private final int SUMMARY_ACTIVITY = 4;
    private TableReservationInfo orderInfo;
    private int fontColor;
    private int backColor;
    private User user;
    private ArrayList<TableReservationOrderInfo> upcomingListData = new ArrayList<TableReservationOrderInfo>();
    private ArrayList<TableReservationOrderInfo> pastListData = new ArrayList<TableReservationOrderInfo>();
    private ListView upcomingList;
    private ListView pastList;
    private LinearLayout mainLayout;
    private orderParsedInfo orderList = new orderParsedInfo();
    private TextView noUpcomingText;
    private TextView noPastText;
    private LinearLayout noUpcominglayout;
    private LinearLayout noPastLayout;
    private Thread orderDownloader;
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
                case DRAW_UI: {
                    drawUI();
                }
                break;
                case CONNECTION_TIMEOUT: {
                    Toast toast = Toast.makeText(TableReservatioinListOfReservations.this,
                            R.string.tablereservation_warning_timeout, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 95);
                    toast.show();
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
        setContentView(R.layout.sergeyb_tablereservation_listofreservations);

        // set topbar title
        setTopBarTitle(getResources().getString(R.string.tablereservation_my_reservations));
        swipeBlock();
        setTopBarLeftButtonText(getResources().getString(R.string.common_back_upper), true, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // getting UI links
        upcomingList = (ListView) findViewById(R.id.sergeyb_tablereservation_upcoming);
        upcomingList.setCacheColorHint(Color.TRANSPARENT);
        upcomingList.setSelector(getResources().getDrawable(R.drawable.sergeyb_tablereservation_custom_background));
        pastList = (ListView) findViewById(R.id.sergeyb_tablereservation_past);
        pastList.setCacheColorHint(Color.TRANSPARENT);
        pastList.setSelector(getResources().getDrawable(R.drawable.sergeyb_tablereservation_custom_background));
        mainLayout = (LinearLayout) findViewById(R.id.sergeyb_tablereservation_main_layout);
        noUpcomingText = (TextView) findViewById(R.id.sergeyb_tablereservation_no_upcoming_text);
        noPastText = (TextView) findViewById(R.id.sergeyb_tablereservation_no_past_text);
        noUpcominglayout = (LinearLayout) findViewById(R.id.sergeyb_tablereservation_no_upcoming);
        noPastLayout = (LinearLayout) findViewById(R.id.sergeyb_tablereservation_no_past);

        // getting info from parent activity
        Intent currentIntent = getIntent();
        orderInfo = (TableReservationInfo) currentIntent.getSerializableExtra("xml");
        fontColor = currentIntent.getIntExtra("fontColor", Color.WHITE);
        backColor = currentIntent.getIntExtra("backColor", Color.parseColor("#37393b"));
        user = (User) currentIntent.getSerializableExtra("userinfo");

        // set appropriate colors
        mainLayout.setBackgroundColor(backColor);
        noUpcomingText.setTextColor(fontColor);
        noPastText.setTextColor(fontColor);

        // checking Internet connection

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (!((ni != null) && (ni.isConnectedOrConnecting()))) {
            Toast toast = Toast.makeText(TableReservatioinListOfReservations.this, R.string.alert_no_internet, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 95);
            toast.show();
            finish();
        }

        // making a POST request
        handler.sendEmptyMessage(SHOW_PROGRESS_DIALOG);
        orderDownloader = new Thread(new Runnable() {
            public void run() {
                try {
                    String requerRes = TableReservationHTTP.sendListOrdersRequest(user, orderInfo);

                    // processing request result 
                    if (requerRes.compareToIgnoreCase("error") != 0) {
                        orderList = JSONParser.parseOrderList(requerRes);
                        handler.sendEmptyMessage(DRAW_UI);
                    }
                    handler.sendEmptyMessage(HIDE_PROGRESS_DIALOG);
                } catch (Exception e) {
                    Log.d("", "");
                }
            }
        });
        orderDownloader.start();

        // upcoming list item click handler
        upcomingList.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                TableReservationOrderInfo tempOrderInfo = upcomingListData.get(arg2);

                Intent bridgeIntent = new Intent(TableReservatioinListOfReservations.this,
                        TableReservationSummary.class);
                bridgeIntent.putExtra("fontColor", fontColor);
                bridgeIntent.putExtra("backColor", backColor);
                // preparing orderInfo
                orderInfo.setOrderDate(tempOrderInfo.orderDate);
                orderInfo.setOrderTime(tempOrderInfo.orderTime.houres,
                        tempOrderInfo.orderTime.minutes, tempOrderInfo.orderTime.am_pm);
                orderInfo.setPersonsAmount(tempOrderInfo.personsAmount);
                orderInfo.setSpecialRequest(tempOrderInfo.specRequest);
                bridgeIntent.putExtra("xml", orderInfo);
                bridgeIntent.putExtra("userinfo", user);
                bridgeIntent.putExtra("orderUUID", tempOrderInfo.uuid);
                startActivityForResult(bridgeIntent, SUMMARY_ACTIVITY);
            }
        });

        // past list item click handler
        pastList.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                TableReservationOrderInfo tempOrderInfo = pastListData.get(arg2);

                Intent bridgeIntent = new Intent(TableReservatioinListOfReservations.this,
                        TableReservationSummary.class);
                bridgeIntent.putExtra("fontColor", fontColor);
                bridgeIntent.putExtra("backColor", backColor);
                bridgeIntent.putExtra("reservationTIme", "past");
                // preparing orderInfo
                orderInfo.setOrderDate(tempOrderInfo.orderDate);
                orderInfo.setOrderTime(tempOrderInfo.orderTime.houres,
                        tempOrderInfo.orderTime.minutes, tempOrderInfo.orderTime.am_pm);
                orderInfo.setPersonsAmount(tempOrderInfo.personsAmount);
                orderInfo.setSpecialRequest(tempOrderInfo.specRequest);
                bridgeIntent.putExtra("xml", orderInfo);
                bridgeIntent.putExtra("userinfo", user);
                bridgeIntent.putExtra("orderUUID", tempOrderInfo.uuid);
                startActivityForResult(bridgeIntent, SUMMARY_ACTIVITY);
            }
        });
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = ProgressDialog.show(this, null, getResources().getString(R.string.common_loading_upper), true, true, new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface arg0) {
                    try {
                        if (orderDownloader != null && orderDownloader.isAlive()) {
                            orderDownloader.interrupt();
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
     * Prepares and draws user interface.
     */
    private void drawUI() {

        if (orderList == null)
            return;

        Date currentDate = new Date();
        Long secTime = currentDate.getTime();

        for (int i = 0; i < orderList.itemsArray.size(); i++) {
            TableReservationOrderInfo tempOrder = orderList.itemsArray.get(i);
            Date tempDate = tempOrder.orderDate;
            tempDate.setHours(tempOrder.orderTime.houres);
            tempDate.setMinutes(tempOrder.orderTime.minutes);

            if (tempOrder.status == 1 || tempOrder.status == 2) {
                Long orderTime = tempDate.getTime();

                if (orderTime > secTime) {
                    upcomingListData.add(tempOrder);
                } else {
                    pastListData.add(tempOrder);
                }
            }
        }

        // processing Upcoming ListViews
        if (upcomingListData.size() == 0) {
            noUpcominglayout.setVisibility(View.VISIBLE);
        } else {
            TableReservationUpcomingAdapter upcomingAdapter =
                    new TableReservationUpcomingAdapter(this, fontColor, upcomingListData);
            upcomingList.setAdapter(upcomingAdapter);
        }

        // processing Past ListViews
        if (pastListData.size() == 0) {
            noPastLayout.setVisibility(View.VISIBLE);
        } else {
            TableReservationUpcomingAdapter pastAdapter =
                    new TableReservationUpcomingAdapter(this, fontColor, pastListData);
            pastList.setAdapter(pastAdapter);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case SUMMARY_ACTIVITY: {
                if (resultCode == RESULT_OK) {
                    String closeAction = data.getStringExtra("howtoclose");
                    if (closeAction.length() != 0 && closeAction != null) {
                        finish();
                    }
                }
            }
            break;
        }
    }
}
