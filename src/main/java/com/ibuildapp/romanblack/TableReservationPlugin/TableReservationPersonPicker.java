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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

/**
 * This activity provides pick of persons count functionality.
 */
public class TableReservationPersonPicker extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.sergeyb_tablereservation_person_picker);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // get parent info
        Intent parent = getIntent();
        int personAmount = parent.getIntExtra("maxperson", 50);

        // render listview
        ListView list = (ListView) findViewById(R.id.sergeyb_tablereservation_person_layout_listView);
        TableReservationPersonPickerAdapter adapter = new TableReservationPersonPickerAdapter(this, personAmount);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                closeActivityGood(arg2 + 1);
            }
        });
    }

    @Override
    public void onBackPressed() {
        closeActivityBad();
    }

    /**
     * Closes activity with "OK" result.
     * @param amount persons count
     */
    private void closeActivityGood(int amount) {
        Intent resIntent = new Intent();
        resIntent.putExtra("persons", amount);
        setResult(RESULT_OK, resIntent);
        finish();
    }

    /**
     * Closes activiyt with "Cancel" result.
     */
    private void closeActivityBad() {
        setResult(RESULT_CANCELED);
        finish();
    }
}
