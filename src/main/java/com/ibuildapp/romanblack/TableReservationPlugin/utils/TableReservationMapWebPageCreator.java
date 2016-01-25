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
 * This class using to prepare Google Map HTML to load in WebView.
 */
public class TableReservationMapWebPageCreator {

    /**
     * Creates map HTML page with given parameters.
     * @param sourcePage map HTML page template
     * @param latitude map location latitude
     * @param longitude map location longitu
     * @param restname the restaurant name
     * @param details the restaurant details
     * @return prepared map HTML page
     */
    public static String createMapPage(String sourcePage, Double latitude,
            Double longitude, String restname, String details) {
        String mapPage = "";

        mapPage = sourcePage.replace("__RePlAcE-Points__", createPoints(latitude,
                longitude, restname, details));
        mapPage = replaceCenterCoordinates(mapPage, latitude, longitude);

        return mapPage;
    }

    /**
     * Creates map points HTML fragment
     * @param latitude the locationlatitude
     * @param longitude the location longitude
     * @param restname the restaurant name
     * @param details the restaurant details
     * @return map points HTML fragment
     */
    private static String createPoints(Double latitude, Double longitude, String restname, String details) {
        String temp = "myMap.points.push({\n"
                + "point: \'" + restname.
                replaceAll("\n", " ").replaceAll("\'", "\\\\'") + "\',\n"
                + "latitude:" + Double.toString(latitude) + ",\n"
                + "longitude:" + Double.toString(longitude) + ",\n"
                + "details: \'" + details.
                replaceAll("\n", " ").replaceAll("\'", "\\\\'") + "\',\n"
                + "})\n\n";
        return temp;
    }

    /**
     * Replaces map center coordinates.
     * @param sourcePage map HTML page template
     * @param latitude the center latitude
     * @param longitude the center longitude
     * @return 
     */
    private static String replaceCenterCoordinates(String sourcePage,
            Double latitude, Double longitude) {
        String res = "";

        res = sourcePage.replace("__RePlAcE-Lat__", Double.toString(latitude));
        res = res.replace("__RePlAcE-Lng__", Double.toString(longitude));
        res = res.replace("__RePlAcE-Zoom__", "15");

        return res;
    }
}
