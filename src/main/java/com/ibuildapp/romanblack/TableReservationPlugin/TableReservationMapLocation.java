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

import com.google.android.maps.GeoPoint;

/**
 * Entity class that represents map location.
 */
public class TableReservationMapLocation {

    private GeoPoint point = null;
    private String title = "";
    private String subtitle = "";
    private String description = "";
    private int latitude = 0;
    private int longitude = 0;

    /**
     * Constructs new MapLocation instance.
     * @param latitude location latitude
     * @param longitude location longitude
     */
    public TableReservationMapLocation(double latitude, double longitude) {
        this.latitude = (int) (latitude * 1e6);
        this.longitude = (int) (longitude * 1e6);
    }

    /**
     * Returns the location GeoPoint.
     * @return the location GeoPoint
     */
    public GeoPoint getPoint() {
        return point;
    }

    /**
     * Sets the location title.
     * @param value the location title to set
     */
    public void setTitle(String value) {
        title = value;
    }

    /**
     * Returns the location title.
     * @return the location title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the location subtitle.
     * @param value the location subtitle to set
     */
    public void setSubtitle(String value) {
        subtitle = value;
    }

    /**
     * Returns the location subtitle.
     * @return the location subtitle
     */
    public String getSubtitle() {
        return subtitle;
    }

    /**
     * Sets the location description.
     * @param value the location description to set
     */
    public void setDescription(String value) {
        description = value;
    }

    /**
     * Returns the location description.
     * @return the location description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the location latitude.
     * @return the latitude latitude
     */
    public int getLatitude() {
        return latitude;
    }

    /**
     * Returns the location longitude.
     * @return the location longitude
     */
    public int getLongitude() {
        return longitude;
    }
}
