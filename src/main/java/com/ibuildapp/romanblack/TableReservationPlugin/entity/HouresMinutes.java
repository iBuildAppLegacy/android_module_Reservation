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
package com.ibuildapp.romanblack.TableReservationPlugin.entity;

import java.io.Serializable;

/**
 * Represents time.
 */
public class HouresMinutes implements Serializable {

    /**
     * Hours.
     */
    public int houres;
    /**
     * Minutes.
     */
    public int minutes;
    /**
     * After meridium / Post meridium.
     */
    public String am_pm;

    /**
     * Constructs new HouresMinutes instance with default parameters.
     */
    public HouresMinutes() {
        this.houres = -1;
        this.minutes = -1;
        this.am_pm = "";
    }
}
