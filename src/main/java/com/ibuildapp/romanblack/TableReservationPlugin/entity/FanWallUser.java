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

import com.appbuilder.sdk.android.authorization.entities.User;
import java.io.Serializable;

public class FanWallUser implements Serializable {

    private String userName = "";
    private String userFirstName = "";
    private String userLastName = "";
    private String avatarUrl = "";
    private User.ACCOUNT_TYPES accountType = User.ACCOUNT_TYPES.IBUILDAPP;
    private String accountId = "";

    public String getUserName() {
        if (userName.length() > 0) {
            return userName;
        } else if ((userFirstName.length() > 0) && (userLastName.length() > 0)) {
            return userFirstName + " " + userLastName;
        } else if (userFirstName.length() > 0) {
            return userFirstName;
        } else if (userLastName.length() > 0) {
            return userLastName;
        } else {
            return "";
        }
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserFirstName() {
        return userFirstName;
    }

    public void setUserFirstName(String userFirstName) {
        this.userFirstName = userFirstName;
    }

    public String getUserLastName() {
        return userLastName;
    }

    public void setUserLastName(String userLastName) {
        this.userLastName = userLastName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    /**
     * @return the accountType
     */
    public User.ACCOUNT_TYPES getAccountType() {
        return accountType;
    }

    /**
     * @param accountType the accountType to set
     */
    public void setAccountType(String accountType) {
        if (accountType.equalsIgnoreCase("facebook")) {
            this.accountType = User.ACCOUNT_TYPES.FACEBOOK;
        } else if (accountType.equalsIgnoreCase("twitter")) {
            this.accountType = User.ACCOUNT_TYPES.TWITTER;
        } else {
            this.accountType = User.ACCOUNT_TYPES.IBUILDAPP;
        }
    }

    /**
     * @return the accountId
     */
    public String getAccountId() {
        return accountId;
    }

    /**
     * @param accountId the accountId to set
     */
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
}
