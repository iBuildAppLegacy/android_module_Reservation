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

import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.appbuilder.sdk.android.AppBuilderModuleMain;

/**
 * This activity represents entering special request page.
 */
public class TableReservationSpecialRequest extends AppBuilderModuleMain {

    private EditText specText;
    private LinearLayout doneButton;
    private TextView doneButtonTxt;
    private LinearLayout clearButton;
    private TextView clearButtonTxt;
    private LinearLayout mainLayout;
    private LinearLayout editTextLayout;
    private TextView specTextText;
    private int fontColor;
    private int backColor;
    private boolean colorSchema;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void create() {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.sergeyb_tablereservation_special_request);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // set topbar title
        setTopBarTitle(getResources().getString(R.string.tablereservation_spec_request));
        swipeBlock();
        setTopBarLeftButtonText(getResources().getString(R.string.common_back_upper), true, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        specText = (EditText) findViewById(R.id.sergeyb_tablereservation_special_request_edittext);
        doneButton = (LinearLayout) findViewById(R.id.sergeyb_tablereservation_done_button);
        mainLayout = (LinearLayout) findViewById(R.id.sergeyb_tablereservation_main_layout);
        specTextText = (TextView) findViewById(R.id.sergeyb_tablereservation_special_request_text);
        clearButton = (LinearLayout) findViewById(R.id.sergeyb_tablereservation_clear_button);
        editTextLayout = (LinearLayout) findViewById(R.id.sergeyb_tablereservation_special_request_edittext_layout);
        doneButtonTxt = (TextView) findViewById(R.id.sergeyb_tablereservation_done_button_text);
        clearButtonTxt = (TextView) findViewById(R.id.sergeyb_tablereservation_clear_button_text);

        // getting info from parent activity
        Intent currentIntent = getIntent();
        specText.setText(currentIntent.getStringExtra("specRequest"));
        fontColor = currentIntent.getIntExtra("fontColor", Color.WHITE);
        backColor = currentIntent.getIntExtra("backColor", Color.parseColor("#37393b"));
        colorSchema = currentIntent.getBooleanExtra("colorSchema", true);

        designButton(doneButtonTxt, bottomBarDesign.rightButtonDesign);
        doneButtonTxt.setTextColor(bottomBarDesign.leftButtonDesign.textColor);
        designButton(clearButtonTxt, bottomBarDesign.leftButtonDesign);

        if (colorSchema) // dark scheme
        {
            editTextLayout.setBackgroundResource(R.drawable.sergeyb_tablereservation_back_transperant_15alpha_dark);
        } else {
            editTextLayout.setBackgroundResource(R.drawable.sergeyb_tablereservation_back_transperant_15alpha);
        }

        // set appropriate colors to root layout and text views
        mainLayout.setBackgroundColor(backColor);
        specTextText.setTextColor(fontColor);
        specText.setTextColor(fontColor);

        // done button handler
        doneButton.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                closeActivityOk(specText.getText().toString());

            }
        });

        // clear button handler
        clearButton.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                specText.setText("");
            }
        });
    }

    /**
     * Closes activity with "OK" result.
     * @param spec result special request
     */
    private void closeActivityOk(String spec) {
        Intent resIntent = new Intent();
        resIntent.putExtra("special", spec);
        setResult(RESULT_OK, resIntent);
        finish();
    }

    /**
     * Closes activity with "Cancel" result.
     */
    private void closeActivityOkNoChange() {
        Intent resIntent = new Intent();
        setResult(RESULT_CANCELED, resIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        closeActivityOkNoChange();
    }
}