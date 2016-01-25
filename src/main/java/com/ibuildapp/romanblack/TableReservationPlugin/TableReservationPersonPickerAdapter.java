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

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.seppius.i18n.plurals.PluralResources;

/**
 * This class is using for person picker alert dialog.
 */
public class TableReservationPersonPickerAdapter extends BaseAdapter {

    private int maxPersons = -1;
    private Integer[] personsList;
    private LayoutInflater layoutInflater;
    private Resources res;

    /**
     * Constructs new TableReservationPersonPickerAdapter with given parameters.
     * @param context activity tha using this adapter
     * @param maxPersons max persons count
     */
    TableReservationPersonPickerAdapter(Context context, int maxPersons) {
        this.maxPersons = maxPersons;
        layoutInflater = LayoutInflater.from(context);
        this.res = context.getResources();

        personsList = new Integer[maxPersons];
        for (int i = 1; i < maxPersons + 1; i++) {
            personsList[i - 1] = i;
        }
    }

    @Override
    public int getCount() {
        return maxPersons;
    }

    @Override
    public Object getItem(int arg0) {
        return personsList[arg0];
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int i, View arg1, ViewGroup arg2) {
        View row = arg1;
        if (row == null) {
            row = layoutInflater.inflate(R.layout.sergeyb_tablereservation_person_picker_item, null);
        }

        TextView text = (TextView) row.findViewById(R.id.sergeyb_tablereservation_person_layout_text);
        String persons = "";
        try {
            persons = new PluralResources(res).getQuantityString(R.plurals.tablereservation_persons_list, i + 1, i + 1);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        text.setText(persons);

        return row;
    }
}
