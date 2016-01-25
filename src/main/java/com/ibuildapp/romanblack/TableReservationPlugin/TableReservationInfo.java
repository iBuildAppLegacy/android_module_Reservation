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

import com.ibuildapp.romanblack.TableReservationPlugin.entity.HouresMinutes;

import java.io.Serializable;
import java.util.Date;

/**
 * This class represents module data.
 */
public class TableReservationInfo implements Serializable {

    private HouresMinutes startTime;
    private HouresMinutes endTime;
    private Integer offsetTime;
    private int maxpersons;
    private Double latitude;
    private Double longitude;
    private String restaurantName = "";
    private String restaurantGreeting = "";
    private String restaurantimageurl = "";
    private String restaurantimageFilePath = "";
    private String restaurantadress = "";
    private String restaurantadditional = "";
    private String restaurantphone = "";
    private String restaurantmail = "";
    private boolean smsConfirmation;
    private boolean emailConfirmation;
    private String restaurantkitchen = "";
    private String login = "";
    private String phoneNumber = "";
    private String specialRequest = "";
    private String customerEmail = "";
    private String appid = "";
    private String moduleid = "";
    private TableReservationOrderInfo orderInfo;
    public ColorSkin colorskin;

    /**
     * Constructs new TableReservationInfo instance with default parameters.
     */
    public TableReservationInfo() {
        this.maxpersons = -1;
        this.startTime = new HouresMinutes();
        this.endTime = new HouresMinutes();
        this.orderInfo = new TableReservationOrderInfo();
        this.colorskin = new ColorSkin();
    }

    /**
     * Returns the application ID.
     * @return the application ID
     */
    public String getAppid() {
        return appid;
    }

    /**
     * Sets the application ID.
     * @param appid the ID to set
     */
    public void setAppid(String appid) {
        this.appid = appid;
    }

    /**
     * Returns the rstaurant kitchen.
     * @return the kitchen
     */
    public String getKitchen() {
        return this.restaurantkitchen;
    }

    /**
     * Sets the restaurant kitchen.
     * @param kitchen the kitchen to set
     */
    public void setKitchen(String kitchen) {
        this.restaurantkitchen = kitchen;
    }

    /**
     * Returns the module ID.
     * @return the module ID
     */
    public String getModuleid() {
        return moduleid;
    }

    /**
     * Sets the module ID.
     * @param moduleid the ID to set
     */
    public void setModuleid(String moduleid) {
        this.moduleid = moduleid;
    }

    /**
     * Returns the customer email.
     * @return the email
     */
    public String getCustomerEmail() {
        return customerEmail;
    }

    /**
     * Sets the customer email.
     * @param email the email to set
     */
    public void setCustomerEmail(String email) {
        this.customerEmail = email;
    }

    /**
     * Returns the restaurant greeting.
     * @return the greeting
     */
    public String getRestaurantGreeting() {
        return restaurantGreeting;
    }

    /**
     * Returns the needing of SMS confirmation.
     * @return true if need SMS confirmation, false otherwise
     */
    public boolean getSmsConfirmation() {
        return smsConfirmation;
    }

    /**
     * Sets the needing of SMS confirmation.
     * @param var true if need confirmation, false otherwise
     */
    public void setSmsConfirmation(boolean var) {
        this.smsConfirmation = var;
    }

    /**
     * Returns the needing of email confirmation.
     * @return true if need SMS confirmation, false otherwise
     */
    public boolean getEmailConfirmation() {
        return emailConfirmation;
    }

    /**
     * Sets the needing of email confirmation.
     * @return true if need email confirmation, false otherwise
     */
    public void setEmailConfirmation(boolean var) {
        this.emailConfirmation = var;
    }

    /**
     * Sets the restaurant greeting.
     * @param restaurantGreeting the greeting to se
     */
    public void setRestaurantGreeting(String restaurantGreeting) {
        this.restaurantGreeting = restaurantGreeting;
    }

    /**
     * Returns the login.
     * @return the login
     */
    public String getLogin() {
        return login;
    }

    /**
     * Returns the restaurant image cache file path.
     * @return the file path
     */
    public String getrestaurantimageFilePath() {
        return restaurantimageFilePath;
    }

    /**
     * Sets the restaurant image cache file path.
     * @param src the file path to set
     */
    public void setrestaurantimageFilePath(String src) {
        this.restaurantimageFilePath = src;
    }

    /**
     * Returns the restaurant phone number.
     * @return the phone number
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Returns the special request.
     * @return the special request
     */
    public String getSpecialRequest() {
        return specialRequest;
    }

    /**
     * Sets the special request.
     * @param specialRequest the special request to set
     */
    public void setSpecialRequest(String specialRequest) {
        this.specialRequest = specialRequest;
    }

    /**
     * Sets the restaurant phone number.
     * @param phoneNumber the phone number to set
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * Sets the login.
     * @param login the login to set
     */
    public void setLogin(String login) {
        this.login = login;
    }

    /**
     * Returns the restaurant open time.
     * @return the open time
     */
    public HouresMinutes getStartTime() {
        return startTime;
    }

    /**
     * Returns the restaurant close time.
     * @return the close time
     */
    public HouresMinutes getEndTime() {
        return endTime;
    }

    /**
     * Returns the max table persons.
     * @return the max persons
     */
    public int getMaxpersons() {
        return maxpersons;
    }

    /**
     * Returns the restaurant name.
     * @return the name
     */
    public String getRestaurantName() {
        return restaurantName;
    }

    /**
     * Returns the restaurant image URL.
     * @return the image URL
     */
    public String getRestaurantimageurl() {
        return restaurantimageurl;
    }

    /**
     * Returns the restaurant address.
     * @return the address string
     */
    public String getRestaurantadress() {
        return restaurantadress;
    }

    /**
     * Sets the restaurant open time.
     * @param hh hours
     * @param mm minutes
     */
    public void setStartTime(int hh, int mm) {
        this.startTime.houres = hh;
        this.startTime.minutes = mm;
    }

    /**
     * Sets the restaurant close time.
     * @param hh hours 
     * @param mm minutes
     */
    public void setEndTime(int hh, int mm) {
        this.endTime.houres = hh;
        this.endTime.minutes = mm;
    }

    /**
     * Sets the max table persons.
     * @param maxpersons the max persons to set
     */
    public void setMaxpersons(int maxpersons) {
        if (maxpersons != 0) {
            this.maxpersons = maxpersons;
        }
    }

    /**
     * Sets the restaurant name.
     * @param restaurantName the name to set
     */
    public void setRestaurantName(String restaurantName) {
        if ((restaurantName.length() != 0) && (restaurantName != "")) {
            this.restaurantName = restaurantName;
        }
    }

    /**
     * Sets the restaurant image URL.
     * @param restaurantimageurl the image URL to set
     */
    public void setRestaurantimageurl(String restaurantimageurl) {
        if ((restaurantimageurl.length() != 0) && (restaurantimageurl != "")) {
            this.restaurantimageurl = restaurantimageurl;
        }
    }

    /**
     * Sets the restaurant address.
     * @param restaurantadress the address string
     */
    public void setRestaurantadress(String restaurantadress) {
        if ((restaurantadress.length() != 0) && (restaurantadress != "")) {
            this.restaurantadress = restaurantadress;
        }
    }

    /**
     * Returns the restaurant location latitude.
     * @return the latitude
     */
    public Double getLatitude() {
        return latitude;
    }

    /**
     * Returns the restaurant location longitude.
     * @return the longitude
     */
    public Double getLongitude() {
        return longitude;
    }

    /**
     * Sets the restaurant location latitude
     * @param latitude the latitude to set
     */
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    /**
     * Sets the restaurant location longitude
     * @param longitude the longitude to set
     */
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    /**
     * Sets the restaurant email address.
     * @param restaurantmail the email address to set
     */
    public void setRestaurantmail(String restaurantmail) {
        if ((restaurantmail.length() != 0) && (restaurantmail != "")) {
            this.restaurantmail = restaurantmail;
        }
    }

    /**
     * Sets the restaurant phone number.
     * @param restaurantphone the number string to set
     */
    public void setRestaurantphone(String restaurantphone) {
        if ((restaurantphone.length() != 0) && (restaurantphone != "")) {
            this.restaurantphone = restaurantphone;
        }
    }

    /**
     * Sets the restaurant additional.
     * @param restaurantadditional the additional to set
     */
    public void setRestaurantadditional(String restaurantadditional) {
        if ((restaurantadditional.length() != 0) && (restaurantadditional != "")) {
            this.restaurantadditional = restaurantadditional;
        }
    }

    /**
     * Returns the restaurant email address.
     * @return the email address
     */
    public String getRestaurantmail() {
        return restaurantmail;
    }

    /**
     * Returns the restaurant phone number.
     * @return the phone number string
     */
    public String getRestaurantphone() {
        return restaurantphone;
    }

    /**
     * Returns the restaurant additional.
     * @return the restaurant additional
     */
    public String getRestaurantadditional() {
        return restaurantadditional;
    }

    /**
     * Returns the reservation offset time.
     * @return the offset time
     */
    public Integer getOffsetTime() {
        return offsetTime;
    }

    /**
     * Sets the restaurant offset time.
     * @param offsetTime the time to set
     */
    public void setOffsetTime(int offsetTime) {
        this.offsetTime = offsetTime;
    }

    // setters
    /**
     * Sets the ordet timen
     * @param hh hours 
     * @param mm minutes
     * @param am_pm day part
     */
    public void setOrderTime(int hh, int mm, String am_pm) {
        this.orderInfo.orderTime.houres = hh;
        this.orderInfo.orderTime.minutes = mm;
        this.orderInfo.orderTime.am_pm = am_pm;
    }

    /**
     * Sets the order date.
     * @param orderDate the date to set
     */
    public void setOrderDate(Date orderDate) {
        this.orderInfo.orderDate = orderDate;
    }

    /**
     * Sets the persons amount.
     * @param personsAmount the amount to set
     */
    public void setPersonsAmount(int personsAmount) {
        this.orderInfo.personsAmount = personsAmount;
    }

    // getters
    /**
     * Returns the order time.
     * @return the order time
     */
    public HouresMinutes getOrderTime() {
        return orderInfo.orderTime;
    }

    /**
     * Returns the order date.
     * @return the order date
     */
    public Date getOrderDate() {
        return orderInfo.orderDate;
    }

    /**
     * Returns the persons amount.
     * @return the persons amount
     */
    public int getPersonsAmount() {
        return orderInfo.personsAmount;
    }

    /**
     * This class represens module color scheme.
     */
    public static class ColorSkin implements Serializable {

        public int color1 = 0x0;    // background
        public int color2 = 0x0;    // item name
        public int color3 = 0x0;    // item description
        public int color4 = 0x0;
        public int color5 = 0x0;
    }
}

/**
 * This class describes order details.
 */
class TableReservationOrderInfo implements Serializable {

    public HouresMinutes orderTime;
    public Date orderDate;
    public int personsAmount;
    public String specRequest;
    public String uuid;
    public int status; // new = 1; approved = 2; rejected = 3; cancaled = 4

    /**
     * Constructs new TableReservationOrderInfo instance.
     */
    TableReservationOrderInfo() {
        this.orderTime = new HouresMinutes();
        this.orderDate = new Date();
        this.personsAmount = -1;
        this.specRequest = "";
        this.uuid = "";
    }
}