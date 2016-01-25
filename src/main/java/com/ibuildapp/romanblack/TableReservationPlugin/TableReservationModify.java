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
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.Log;
import android.view.*;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.appbuilder.sdk.android.AppBuilderModuleMain;
import com.ibuildapp.romanblack.TableReservationPlugin.entity.HouresMinutes;
import com.ibuildapp.romanblack.TableReservationPlugin.utils.TableReservationPhoneNumberFormattingTextWatcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Modify activity for user reservation details.
 */
public class TableReservationModify extends AppBuilderModuleMain implements View.OnFocusChangeListener {

    private final String DEFAULT_PHONE_TEXT = "--- --- ----";
    private LayoutInflater layoutInflater;
    private LinearLayout timeDateModify;
    private View dateTimePickerView;
    private LinearLayout personModify;
    private Dialog mAlertDialog;
    private AlertDialog.Builder builder;
    private TableReservationInfo orderInfo;
    private LinearLayout taskBar;
    private LinearLayout specTextLayout;
    private TextView personText;
    private View personPickerView;
    private FrameLayout mainLayout;
    private String cachePath;
    private EditText phoneEdit;
    private EditText mailEdit;
    private EditText specText;
    private TextView orderDateText;
    private TextView orderTimeText;
    private ImageView dateImg;
    private ImageView personImg;
    private ImageView phoneImg;
    private ImageView mailImg;
    private LinearLayout doneButton;
    private LinearLayout hideKeyboard;
    private TextView reservDetailsText;
    private TextView phoneContactText;
    private TextView specReqText;
    private TextView mailText;
    private LinearLayout phoneEditLayout;
    private LinearLayout mailEditLayout;
    private int fontColor;
    private int backColor;
    private boolean colorSchema;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void create() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.sergeyb_tablereservation_modify);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // set topbar title
        setTopBarTitle(getResources().getString(R.string.common_modify_upper));
        swipeBlock();
        setTopBarLeftButtonText(getResources().getString(R.string.common_back_upper), true, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeActivityBad();
            }
        });

        builder = new AlertDialog.Builder(this);
        personPickerView = personPickerViewinflate();

        // getting info from parent activity
        Intent currentIntent = getIntent();
        cachePath = currentIntent.getStringExtra("cachePath");
        orderInfo = (TableReservationInfo) currentIntent.getSerializableExtra("xml");
        fontColor = currentIntent.getIntExtra("fontColor", Color.WHITE);
        backColor = currentIntent.getIntExtra("backColor", Color.parseColor("#37393b"));
        colorSchema = currentIntent.getBooleanExtra("colorSchema", true);

        Date orderDate = orderInfo.getOrderDate();

        mainLayout = (FrameLayout) findViewById(R.id.sergeyb_tablereservation_main_layout);
        personText = (TextView) findViewById(R.id.sergeyb_tablereservation_person_text);
        specText = (EditText) findViewById(R.id.sergeyb_tablereservation_special_request_edittext);
        specText.setText(currentIntent.getStringExtra("specRequest"));
        personText = (TextView) findViewById(R.id.sergeyb_tablereservation_person_text);
        personText.setText(Integer.toString(orderInfo.getPersonsAmount()));
        orderDateText = (TextView) findViewById(R.id.sergeyb_tablereservation_date_text);
        orderTimeText = (TextView) findViewById(R.id.sergeyb_tablereservation_time_text);
        timeDateModify = (LinearLayout) findViewById(R.id.sergeyb_tablereservation_time_date);
        personModify = (LinearLayout) findViewById(R.id.sergeyb_tablereservation_person);
        phoneEdit = (EditText) findViewById(R.id.sergeyb_tablereservation_phone_edittext);
        reservDetailsText = (TextView) findViewById(R.id.sergeyb_tablereservation_reserv_details_text);
        phoneEditLayout = (LinearLayout) findViewById(R.id.sergeyb_tablereservation_phone_layout);
        phoneContactText = (TextView) findViewById(R.id.sergeyb_tablereservation_phone_text);
        specReqText = (TextView) findViewById(R.id.sergeyb_tablereservation_special_request_text);
        mailText = (TextView) findViewById(R.id.sergeyb_tablereservation_mail_text);
        mailEdit = (EditText) findViewById(R.id.sergeyb_tablereservation_mail_edittext);
        taskBar = (LinearLayout) findViewById(R.id.sergeyb_tablereservation_bottom_taskbar);
        hideKeyboard = (LinearLayout) findViewById(R.id.sergeyb_tablereservation_hide_keyboard);
        dateImg = (ImageView) findViewById(R.id.sergeyb_tablereservation_dateimg);
        personImg = (ImageView) findViewById(R.id.sergeyb_tablereservation_personimg);
        phoneImg = (ImageView) findViewById(R.id.sergeyb_tablereservation_phoneimg);
        mailImg = (ImageView) findViewById(R.id.sergeyb_tablereservation_mailimg);
        mailEditLayout = (LinearLayout) findViewById(R.id.sergeyb_tablereservation_mail_layout);
        specTextLayout = (LinearLayout) findViewById(R.id.sergeyb_tablereservation_special_request_edittext_layout);

        timeDateModify.requestFocus();

        // hide keyboard on start
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        if (colorSchema) // dark scheme
        {

            dateImg.setImageResource(R.drawable.sergeyb_tablereservation_time_img);
            personImg.setImageResource(R.drawable.sergeyb_tablereservation_persons_img);
            phoneImg.setImageResource(R.drawable.sergeyb_tablereservation_phone);
            mailImg.setImageResource(R.drawable.sergeyb_tablereservation_email);

            timeDateModify.setBackgroundResource(R.drawable.sergeyb_tablereservation_back_transperant_15alpha_dark);
            personModify.setBackgroundResource(R.drawable.sergeyb_tablereservation_back_transperant_15alpha_dark);
            phoneEditLayout.setBackgroundResource(R.drawable.sergeyb_tablereservation_back_transperant_15alpha_dark);
            mailEditLayout.setBackgroundResource(R.drawable.sergeyb_tablereservation_back_transperant_15alpha_dark);
            specTextLayout.setBackgroundResource(R.drawable.sergeyb_tablereservation_back_transperant_15alpha_dark);
        } else {
            dateImg.setImageResource(R.drawable.sergeyb_tablereservation_time_img_dark);
            personImg.setImageResource(R.drawable.sergeyb_tablereservation_persons_img_dark);
            phoneImg.setImageResource(R.drawable.sergeyb_tablereservation_phone_dark);
            mailImg.setImageResource(R.drawable.sergeyb_tablereservation_email_dark);

            timeDateModify.setBackgroundResource(R.drawable.sergeyb_tablereservation_back_transperant_15alpha);
            personModify.setBackgroundResource(R.drawable.sergeyb_tablereservation_back_transperant_15alpha);
            phoneEditLayout.setBackgroundResource(R.drawable.sergeyb_tablereservation_back_transperant_15alpha);
            mailEditLayout.setBackgroundResource(R.drawable.sergeyb_tablereservation_back_transperant_15alpha);
            specTextLayout.setBackgroundResource(R.drawable.sergeyb_tablereservation_back_transperant_15alpha);
        }

        mainLayout.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                //r will be populated with the coordinates of your view that area still visible.
                mainLayout.getWindowVisibleDisplayFrame(r);

                int heightDiff = mainLayout.getRootView().getHeight() - (r.bottom - r.top);
                if (heightDiff > 300) { // if more than 100 pixels, its probably a keyboard...
                    taskBar.setVisibility(View.VISIBLE);
                } else {
                    taskBar.setVisibility(View.INVISIBLE);
                }
            }
        });

        hideKeyboard.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                InputMethodManager imm = (InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mailEdit.getWindowToken(), 0);
            }
        });

        // set appropriate colors to root layout and text views
        mainLayout.setBackgroundColor(backColor);
        reservDetailsText.setTextColor(fontColor);
        phoneContactText.setTextColor(fontColor);
        specReqText.setTextColor(fontColor);
        orderDateText.setTextColor(fontColor);
        orderTimeText.setTextColor(fontColor);
        personText.setTextColor(fontColor);
        phoneEdit.setTextColor(fontColor);
        mailEdit.setTextColor(fontColor);
        specText.setTextColor(fontColor);
        mailText.setTextColor(fontColor);
        phoneEdit.setBackgroundColor(Color.TRANSPARENT);
        mailEdit.setBackgroundColor(Color.TRANSPARENT);

        // set special request to appropriate edittext
        specText.setText(currentIntent.getStringExtra("specRequest"));

        // get modify view and set appropriate numbers to date and time picker
        dateTimePickerView = dateTimeModifierViewinflate();
        TimePicker mtimePicker = (TimePicker) dateTimePickerView.findViewById(R.id.timePicker);
        mtimePicker.setCurrentHour(orderInfo.getOrderTime().houres);
        mtimePicker.setCurrentMinute(orderInfo.getOrderTime().minutes);
        String formTime = convertTimeToFormat(orderInfo.getOrderTime().houres,
                orderInfo.getOrderTime().minutes, false);
        String am_pm = null;
        if (formTime.contains("AM")) {
            am_pm = "AM";
        } else {
            am_pm = "PM";
        }

        Button amBtn = null;
        try {
            amBtn = ((Button) ((ViewGroup) mtimePicker.getChildAt(0)).getChildAt(2));
        } catch (Exception e) {
            amBtn = null;
        }
        if (amBtn != null)
            amBtn.setText(am_pm);

        DatePicker mdatePicker = (DatePicker) dateTimePickerView.findViewById(R.id.datePicker);
        mdatePicker.init(1900 + orderDate.getYear(), orderDate.getMonth(), orderDate.getDate(), null);

        // set time and date to appropriate textviews
        String resDateStamp = new SimpleDateFormat("E").format(orderInfo.getOrderDate()) + ", "
                + new SimpleDateFormat("MMMMMMM").format(orderInfo.getOrderDate()) + " "
                + new SimpleDateFormat("d").format(orderInfo.getOrderDate()) + ", ";
        orderDateText.setText(resDateStamp);

        HouresMinutes tempTime = orderInfo.getOrderTime();
        String temp_time = convertTimeToFormat(tempTime.houres, tempTime.minutes, false);
        orderTimeText.setText(temp_time);

        // time date modify
        timeDateModify.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                // creating alert dialog
                ViewGroup mParent = (ViewGroup) dateTimePickerView.getParent();
                if (mParent != null) {
                    mParent.removeAllViews();
                }

                // set appropriate fields to date and time picker
                TimePicker mtimePicker = (TimePicker) dateTimePickerView.findViewById(R.id.timePicker);
                mtimePicker.setCurrentHour(orderInfo.getOrderTime().houres);
                mtimePicker.setCurrentMinute(orderInfo.getOrderTime().minutes);
                String formTime = convertTimeToFormat(orderInfo.getOrderTime().houres,
                        orderInfo.getOrderTime().minutes, false);
                String am_pm = null;
                if (formTime.contains("AM")) {
                    am_pm = "AM";
                } else {
                    am_pm = "PM";
                }

                Button amBtn = null;
                try {
                    amBtn = ((Button) ((ViewGroup) mtimePicker.getChildAt(0)).getChildAt(2));
                } catch (Exception e) {
                    amBtn = null;
                }
                if (amBtn != null)
                    amBtn.setText(am_pm);

                DatePicker mdatePicker = (DatePicker) dateTimePickerView.findViewById(R.id.datePicker);
                mdatePicker.init(1900 + orderInfo.getOrderDate().getYear(),
                        orderInfo.getOrderDate().getMonth(),
                        orderInfo.getOrderDate().getDate(), new DatePicker.OnDateChangedListener() {
                    public void onDateChanged(DatePicker arg0, int arg1, int arg2, int arg3) {
                    }
                });

                // create dialog
                builder.setView(dateTimePickerView);
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

        // person amount modify
        personModify.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                // creating alert dialog
                ViewGroup mParent = (ViewGroup) personPickerView.getParent();
                if (mParent != null) {
                    mParent.removeAllViews();
                }
                builder.setView(personPickerView);
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

        // phone edit text handler
        if (orderInfo.getPhoneNumber() != null) {
            if (orderInfo.getPhoneNumber() != "") {
                phoneEdit.setText(orderInfo.getPhoneNumber());
            } else {
                phoneEdit.setText(DEFAULT_PHONE_TEXT);
            }
        } else {
            phoneEdit.setText(DEFAULT_PHONE_TEXT);
        }
        phoneEdit.setOnFocusChangeListener(this);
        phoneEdit.addTextChangedListener(new TableReservationPhoneNumberFormattingTextWatcher());

        // email edit text handler
        if (orderInfo.getCustomerEmail() != null) {
            if (orderInfo.getCustomerEmail().length() != 0) {
                mailEdit.setText(orderInfo.getCustomerEmail());
            }
        }
        mailEdit.setOnFocusChangeListener(this);

        // phone edit text handler
        doneButton = (LinearLayout) findViewById(R.id.sergeyb_tablereservation_done_button);
        doneButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                // check time availability for pointed time
                Date tempDate = orderInfo.getOrderDate();
                tempDate.setHours(0);
                tempDate.setMinutes(0);
                tempDate.setSeconds(0);
                tempDate.setHours(orderInfo.getOrderTime().houres);
                tempDate.setMinutes(orderInfo.getOrderTime().minutes);
                long orderTimeSec = tempDate.getTime();

                Date current = new Date();
                long curTime = current.getTime();
                if (orderTimeSec < curTime) {
                    Toast toast = Toast.makeText(TableReservationModify.this,
                            R.string.tablereservation_warning_too_late, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 95);
                    toast.show();
                    return;
                }

                tempDate.setHours(0);
                tempDate.setMinutes(0);
                tempDate.setSeconds(0);
                tempDate.setHours(orderInfo.getStartTime().houres);
                tempDate.setMinutes(orderInfo.getStartTime().minutes);
                long startTimeSec = tempDate.getTime();

                tempDate.setHours(0);
                tempDate.setMinutes(0);
                tempDate.setSeconds(0);
                tempDate.setHours(orderInfo.getEndTime().houres);
                tempDate.setMinutes(orderInfo.getEndTime().minutes);
                long endTimeSec = tempDate.getTime();
                if (startTimeSec > endTimeSec) {
                    endTimeSec += 86400 * 1000;
                }

                if ((orderTimeSec >= startTimeSec) && (orderTimeSec <= endTimeSec)) {
                    closeActivityOk();
                } else {
                    Toast toast = Toast.makeText(TableReservationModify.this,
                            R.string.tablereservation_warning_wrong_time, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 95);
                    toast.show();
                }
            }
        });

    }

    /**
     * This method inflates view for date/time picker dialog.
     */
    private View dateTimeModifierViewinflate() {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(this);
        }

        View custom = layoutInflater.inflate(R.layout.sergeyb_tablereservation_datetimepicker_alert, null);

        // ok button handler
        Button ok = (Button) custom.findViewById(R.id.okButton);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // date handling 
                View parent = (View) view.getParent().getParent();
                DatePicker mdatePicker = (DatePicker) parent.findViewById(R.id.datePicker);
                mdatePicker.clearChildFocus(getCurrentFocus());

                Date tempDate = orderInfo.getOrderDate();
                tempDate.setYear(mdatePicker.getYear() - 1900);
                tempDate.setMonth(mdatePicker.getMonth());
                tempDate.setDate(mdatePicker.getDayOfMonth());
                orderInfo.setOrderDate(tempDate);

                String resDateStamp = new SimpleDateFormat("E").format(orderInfo.getOrderDate()) + ", "
                        + new SimpleDateFormat("MMMMMMM").format(orderInfo.getOrderDate()) + " "
                        + new SimpleDateFormat("d").format(orderInfo.getOrderDate()) + ", ";
                orderDateText.setText(resDateStamp);

                // time handling 
                TimePicker mtimePicker = (TimePicker) parent.findViewById(R.id.timePicker);
                mtimePicker.clearChildFocus(getCurrentFocus());
                EditText minutesEdit = (EditText) ((LinearLayout) (((ViewGroup) mtimePicker.getChildAt(0)).getChildAt(1))).getChildAt(1);
                String temp = ((Button) ((ViewGroup) mtimePicker.getChildAt(0)).getChildAt(2)).getText().toString();
                orderInfo.setOrderTime(mtimePicker.getCurrentHour(),
                        Integer.parseInt(minutesEdit.getText().toString()), temp);

                HouresMinutes tempTime = orderInfo.getOrderTime();
                String temp_time = convertTimeToFormat(tempTime.houres, tempTime.minutes, false);
                orderTimeText.setText(temp_time);

                if (mAlertDialog != null) {
                    mAlertDialog.dismiss();
                }
            }
        });

        // cancel button handler
        Button cancel = (Button) custom.findViewById(R.id.cancelButton);
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
     * This method inflates view for person picker dialog.
     */
    private View personPickerViewinflate() {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(this);
        }

        View custom = layoutInflater.inflate(R.layout.sergeyb_tablereservation_person_picker, null);
        ListView list = (ListView) custom.findViewById(R.id.sergeyb_tablereservation_person_layout_listView);
        TableReservationPersonPickerAdapter adapter = new TableReservationPersonPickerAdapter(this, 15);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                orderInfo.setPersonsAmount(arg2 + 1);
                if ((arg2 + 1) == 1) {
                    personText.setText(Integer.toString(arg2 + 1));
                } else {
                    personText.setText(Integer.toString(arg2 + 1));
                }

                if (mAlertDialog != null) {
                    mAlertDialog.dismiss();
                }
            }
        });

        return custom;
    }

    /**
     * Closes activiyt with "Cancel" result.
     */
    private void closeActivityBad() {
        Intent resIntent = new Intent();
        setResult(RESULT_CANCELED, resIntent);
        finish();
    }

    /**
     * Closes activity with "OK" result.
     */
    private void closeActivityOk() {
        // save user phone to cache 
        File phoneCache = new File(cachePath + "/userphone.data");
        if (!phoneCache.exists()) {
            try {
                phoneCache.createNewFile();
            } catch (IOException ex) {
                Log.d("CACHE APP CONF", "success");
            }
        }

        try {
            FileOutputStream wStream = new FileOutputStream(phoneCache);
            wStream.write(phoneEdit.getText().toString().getBytes());
            wStream.flush();
            wStream.close();
        } catch (Exception e) {
            phoneCache.delete();
        }

        // transmit data back to parent activity
        Intent resIntent = new Intent();
        resIntent.putExtra("special", specText.getText().toString());
        if (phoneEdit.getText().toString().compareTo(DEFAULT_PHONE_TEXT) == 0) {
            phoneEdit.setText("");
        }
        resIntent.putExtra("phone", phoneEdit.getText().toString());
        resIntent.putExtra("email", mailEdit.getText().toString());
        resIntent.putExtra("persons", personText.getText());
        resIntent.putExtra("date", orderInfo.getOrderDate());
        resIntent.putExtra("time", orderInfo.getOrderTime());
        setResult(RESULT_OK, resIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        closeActivityBad();
    }

    public void onFocusChange(View arg0, boolean arg1) {
        if (arg0.getId() == R.id.sergeyb_tablereservation_phone_edittext) {
            if (arg1) {
                // set appropriate text
                if (((EditText) arg0).getText().toString().equals(DEFAULT_PHONE_TEXT)) {
                    ((EditText) arg0).setText("");
                }
            } else if (arg1 == false) {
                if (((EditText) arg0).getText().toString().trim().equals("")) {
                    ((EditText) arg0).setText(DEFAULT_PHONE_TEXT);
                }
            }
        }
    }

    // this function converts data and time represented in minutes in necessary format

    /**
     * Converts data and time represented in minutes in necessary format.
     *
     * @param hh     hours
     * @param mm     minutes
     * @param format result format
     * @return formatted date string
     */
    private String convertTimeToFormat(int hh, int mm, boolean format) {
        String temphhstr = null;
        String tempminstr = null;
        if (format) // use 24 format
        {
            // proccessing houres
            if (Integer.toString(hh).length() < 2) {
                temphhstr = Integer.toString(hh);
                temphhstr = "0" + temphhstr;
            } else {
                temphhstr = Integer.toString(hh);
            }

            // proccessing minutes
            if (Integer.toString(mm).length() < 2) {
                tempminstr = Integer.toString(mm);
                tempminstr = "0" + tempminstr;
            } else {
                tempminstr = Integer.toString(mm);
            }

            return temphhstr + ":" + tempminstr;
        } else // use am/pm format
        {
            String am_pm = "";
            int temp_sum = hh * 100 + mm;
            if (temp_sum >= 1200) {
                am_pm = "PM";
            } else {
                am_pm = "AM";
            }

            // processing minutes
            if (Integer.toString(mm).length() < 2) {
                tempminstr = Integer.toString(mm);
                tempminstr = "0" + tempminstr;
            } else {
                tempminstr = Integer.toString(mm);
            }

            // processing houres
            if (hh > 12) {
                int tempHH = hh;
                tempHH = tempHH - 12;

                if (Integer.toString(tempHH).length() < 2) {
                    temphhstr = Integer.toString(tempHH);
                    temphhstr = "0" + temphhstr;
                } else {
                    temphhstr = Integer.toString(tempHH);
                }
            } else {
                if (Integer.toString(hh).length() < 2) {
                    temphhstr = Integer.toString(hh).toString();
                    temphhstr = "0" + temphhstr;
                } else {
                    temphhstr = Integer.toString(hh).toString();
                }
            }

            return temphhstr + ":" + tempminstr + " " + am_pm;

        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks whether a hardware keyboard is available
        if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {
            Toast.makeText(TableReservationModify.this, "keyboard visible", Toast.LENGTH_LONG).show();
        } else if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
            Toast.makeText(TableReservationModify.this, "keyboard hidden", Toast.LENGTH_LONG).show();
        }
    }
}
