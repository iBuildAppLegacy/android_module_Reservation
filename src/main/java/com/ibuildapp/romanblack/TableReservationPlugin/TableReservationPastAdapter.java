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
import android.graphics.drawable.Drawable;
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
 * array of upcomming reservation.
 *
 */
public class TableReservationPastAdapter extends BaseAdapter {

    private ArrayList<TableReservationOrderInfo> source;
    private Context context;
    private LayoutInflater layoutInflater;

    /**
     * Constructs new TableReservationPastAdapter instance with given parameters.
     * @param context activity that using this adapter
     * @param source list of reservation order info
     */
    TableReservationPastAdapter(Context context, ArrayList<TableReservationOrderInfo> source) {
        this.context = context;
        this.source = source;
        layoutInflater = LayoutInflater.from(context);
    }

    /**
     * Returns the list of reservation order info.
     * @return the list of order info
     */
    public ArrayList<TableReservationOrderInfo> getSource() {
        return this.source;
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
        View row;

        if (arg1 == null) {
            if (arg0 == 0) {
                row = layoutInflater.inflate(R.layout.sergeyb_tablereservation_list_text, null);
                TextView text = (TextView) row.findViewById(R.id.textView);
                text.setText(context.getResources().getString(R.string.tablereservation_past));
            } else {
                row = layoutInflater.inflate(R.layout.sergeyb_tablereservation_list_item, null);

                // set 15% alpha
                Drawable temp = row.getBackground();
                temp.setAlpha(38);
                row.setBackgroundDrawable(temp);

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

                // proccessing persons textview
                String personText = "";
                if (Locale.getDefault().toString().equals("en_EN")) {
                    personText = context.getResources().getString(R.string.tablereservation_party_of) + " " + tempOrder.personsAmount;
                } else {
                    try {
                        personText = new PluralResources(context.getResources()).getQuantityString(R.plurals.orderTableForPerson, tempOrder.personsAmount, tempOrder.personsAmount);
                    } catch (NoSuchMethodException e) {
                        personText = "";
                    }
                }
                persontext.setText(personText);
            }
        } else {
            row = arg1;
            if (arg0 == 0) {
                TextView text = (TextView) row.findViewById(R.id.textView);
                text.setText(context.getResources().getString(R.string.tablereservation_past));
            } else {
                // set 15% alpha
                Drawable temp = row.getBackground();
                temp.setAlpha(38);
                row.setBackgroundDrawable(temp);

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

                // proccessing persons textview
                String personText = "";
                if (Locale.getDefault().toString().equals("en_EN")) {
                    personText = context.getResources().getString(R.string.tablereservation_party_of) + " " + tempOrder.personsAmount;
                } else {
                    try {
                        personText = new PluralResources(context.getResources()).getQuantityString(R.plurals.orderTableForPerson, tempOrder.personsAmount, tempOrder.personsAmount);
                    } catch (NoSuchMethodException e) {
                        personText = "";
                    }
                }
                persontext.setText(personText);
            }
        }

        return row;
    }
}
