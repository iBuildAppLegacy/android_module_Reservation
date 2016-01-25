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
package com.ibuildapp.romanblack.TableReservationPlugin.utils;

/**
 * This class provides static utils methods.
 */
public class Utils {

    private static final int SECONDS_IN_DAY = 86400;

    /**
     * This function converts data and time represented in minutes in necessary
     * format.
     *
     * @param hh - hours
     * @param mm - minutes
     * @param format - true - 24 hous format, am/pm otherwise
     * @return formattd string
     */
    public static String convertTimeToFormat(int hh, int mm, boolean format) {
        String temphhstr = null;
        String tempminstr = null;

        // if time more than 24 hours
        int total_sec = hh * 3600 + mm * 60;
        if (total_sec > SECONDS_IN_DAY) {
            total_sec = total_sec - SECONDS_IN_DAY;
            hh = (int) (total_sec / 3600);
            mm = (total_sec / 60) - (hh * 60);
        }

        if (format) // use 24 format
        {
            // proccessing hours
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

            // processing hours
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
}
