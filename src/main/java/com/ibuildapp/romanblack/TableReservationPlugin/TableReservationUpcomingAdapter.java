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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.ibuildapp.romanblack.TableReservationPlugin.utils.Utils;
import com.seppius.i18n.plurals.PluralResources;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * This adapter uses for representation of upcoming reservations Source data -
 * array of upcomming reservations.
 */
public class TableReservationUpcomingAdapter extends BaseAdapter {

    private ArrayList<TableReservationOrderInfo> source;
    private LayoutInflater layoutInflater;
    private int fontColor;
    private Context context;

    /**
     * Constructs new TableReservationUpcomingAdapter with given parameters.
     * @param context activity that using this adapter
     * @param textColor text color
     * @param source list of order info
     */
    TableReservationUpcomingAdapter(Context context, int textColor,
            ArrayList<TableReservationOrderInfo> source) {
        this.source = source;
        this.fontColor = textColor;
        layoutInflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public int getCount() {
        return source.size();
    }

    @Override
    public Object getItem(int arg0) {
        return source.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int arg0, View arg1, ViewGroup arg2) {
        View row = arg1;

        if (arg1 == null) {
            row = layoutInflater.inflate(R.layout.sergeyb_tablereservation_list_item, null);
        }

        TextView datetext = (TextView) row.findViewById(R.id.sergeyb_tablereservation_date_text);
        TextView persontext = (TextView) row.findViewById(R.id.sergeyb_tablereservation_persons_text);
        TableReservationOrderInfo tempOrder = source.get(arg0);

        // proccessing datetime textview
        String resDateStamp = new SimpleDateFormat("E").format(tempOrder.orderDate) + ", "
                + new SimpleDateFormat("MMMMMMM").format(tempOrder.orderDate) + " "
                + new SimpleDateFormat("d").format(tempOrder.orderDate) + ", ";

        String resTimeStamp = null;
        if (Locale.getDefault().toString().equals("ru_RU")) {
            resTimeStamp = Utils.convertTimeToFormat(tempOrder.orderTime.houres, tempOrder.orderTime.minutes, true);
        } else {
            resTimeStamp = Utils.convertTimeToFormat(tempOrder.orderTime.houres, tempOrder.orderTime.minutes, false);
        }
        datetext.setText(resDateStamp + resTimeStamp);
        datetext.setTextColor(fontColor);

        // proccessing persons textview
        String personText = "";
        if (Locale.getDefault().toString().equals("ru_RU")) {
            try {
                personText = new PluralResources(context.getResources()).getQuantityString(R.plurals.orderTableForPerson, tempOrder.personsAmount, tempOrder.personsAmount);
            } catch (NoSuchMethodException e) {
                personText = "";
            }
        } else {
            personText = context.getResources().getString(R.string.tablereservation_party_of) + " " + tempOrder.personsAmount;
        }

        persontext.setText(personText);
        persontext.setTextColor(fontColor);

        return row;
    }
}
