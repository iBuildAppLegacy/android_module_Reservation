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

import android.os.Handler;
import android.util.Log;
import com.appbuilder.sdk.android.Statics;
import com.appbuilder.sdk.android.authorization.entities.User;
import com.ibuildapp.romanblack.TableReservationPlugin.JSONParser;
import com.ibuildapp.romanblack.TableReservationPlugin.TableReservationInfo;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.*;

/**
 * This class provides static methods to make HTTP requests.
 */
public class TableReservationHTTP {

    private static final String TAG = "com.ibuildapp.romanblack.TableReservationPlugin.utils.TableReservationHTTP";

    private static final String ADD_ORDER_URL =  com.appbuilder.sdk.android.Statics.BASE_DOMEN + "/mdscr/tablereservation/addorder";
    private final static String CANCEL_ORDER_URL =  com.appbuilder.sdk.android.Statics.BASE_DOMEN + "/mdscr/tablereservation/cancelorder";
    private final static String LIST_ORDER_URL =  com.appbuilder.sdk.android.Statics.BASE_DOMEN + "/mdscr/tablereservation/getorders";
    private final static String MAIL_ORDER_URL = com.appbuilder.sdk.android.Statics.BASE_DOMEN + "/mdscr/tablereservation/sendmail";
    private final static String LOGIN_URL =    com.appbuilder.sdk.android.Statics.BASE_DOMEN + "/mdscr/user/login";
    public final static String SIGNUP_URL =    com.appbuilder.sdk.android.Statics.BASE_DOMEN + "/mdscr/user/signup";

    private final static int ADD_REQUEST_ERROR = 4;
    private final static int CANCEL_REQUEST_ERROR = 3;

    /**
     * This method sends HTTP request to add order.
     *
     * @param user
     * @param handler
     * @param orderInfo
     * @return
     */
    public static String sendAddOrderRequest(User user, Handler handler,
                                             TableReservationInfo orderInfo) {
        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, 15000);
        HttpConnectionParams.setSoTimeout(httpParameters, 15000);

        HttpClient httpclient = new DefaultHttpClient(httpParameters);
        HttpPost httppost = new HttpPost(ADD_ORDER_URL);
        Log.e(TAG, "ADD_ORDER_URL = " + ADD_ORDER_URL);

        MultipartEntity multipartEntity = new MultipartEntity();

        // user details
        String userType = null;
        String orderUUID = null;
        try {

            if (user.getAccountType() == User.ACCOUNT_TYPES.FACEBOOK) {
                userType = "facebook";
                multipartEntity.addPart("order_customer_name", new StringBody(user.getUserFirstName() + " " + user.getUserLastName(), Charset.forName("UTF-8")));
            } else if (user.getAccountType() == User.ACCOUNT_TYPES.TWITTER) {
                userType = "twitter";
                multipartEntity.addPart("order_customer_name", new StringBody(user.getUserName(), Charset.forName("UTF-8")));
            } else if (user.getAccountType() == User.ACCOUNT_TYPES.IBUILDAPP) {
                userType = "ibuildapp";
                multipartEntity.addPart("order_customer_name", new StringBody(user.getUserName(), Charset.forName("UTF-8")));

            } else if (user.getAccountType() == User.ACCOUNT_TYPES.GUEST) {
                userType = "guest";
                multipartEntity.addPart("order_customer_name", new StringBody("Guest", Charset.forName("UTF-8")));
            }

            multipartEntity.addPart("user_type", new StringBody(userType, Charset.forName("UTF-8")));
            multipartEntity.addPart("user_id", new StringBody(user.getAccountId(), Charset.forName("UTF-8")));
            multipartEntity.addPart("app_id", new StringBody(orderInfo.getAppid(), Charset.forName("UTF-8")));
            multipartEntity.addPart("module_id", new StringBody(orderInfo.getModuleid(), Charset.forName("UTF-8")));

            // order UUID
            orderUUID = UUID.randomUUID().toString();
            multipartEntity.addPart("order_uid", new StringBody(orderUUID, Charset.forName("UTF-8")));

            // order details
            Date tempDate = orderInfo.getOrderDate();
            tempDate.setHours(orderInfo.getOrderTime().houres);
            tempDate.setMinutes(orderInfo.getOrderTime().minutes);
            tempDate.getTimezoneOffset();
            String timeZone = timeZoneToString();

            multipartEntity.addPart("time_zone", new StringBody(timeZone, Charset.forName("UTF-8")));
            multipartEntity.addPart("order_date_time", new StringBody(Long.toString(tempDate.getTime() / 1000), Charset.forName("UTF-8")));
            multipartEntity.addPart("order_persons", new StringBody(Integer.toString(orderInfo.getPersonsAmount()), Charset.forName("UTF-8")));
            multipartEntity.addPart("order_spec_request", new StringBody(orderInfo.getSpecialRequest(), Charset.forName("UTF-8")));
            multipartEntity.addPart("order_customer_phone", new StringBody(orderInfo.getPhoneNumber(), Charset.forName("UTF-8")));
            multipartEntity.addPart("order_customer_email", new StringBody(orderInfo.getCustomerEmail(), Charset.forName("UTF-8")));

            // add security part
            multipartEntity.addPart("app_id", new StringBody(Statics.appId, Charset.forName("UTF-8")));
            multipartEntity.addPart("token", new StringBody(Statics.appToken, Charset.forName("UTF-8")));

        } catch (Exception e) {
            Log.d("", "");
        }

        httppost.setEntity(multipartEntity);

        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        try {
            String strResponseSaveGoal =
                    httpclient.execute(httppost, responseHandler);
            Log.d("sendAddOrderRequest", "");
            String res = JSONParser.parseQueryError(strResponseSaveGoal);

            if (res == null || res.length() == 0) {
                return orderUUID;
            } else {
                handler.sendEmptyMessage(ADD_REQUEST_ERROR);
                return null;
            }
        } catch (ConnectTimeoutException conEx) {
            handler.sendEmptyMessage(ADD_REQUEST_ERROR);
            return null;
        } catch (ClientProtocolException ex) {
            handler.sendEmptyMessage(ADD_REQUEST_ERROR);
            return null;
        } catch (IOException ex) {
            handler.sendEmptyMessage(ADD_REQUEST_ERROR);
            return null;
        }
    }

    /**
     * This method sends HTTP request to cancel order.
     *
     * @param orderUUID order UID
     * @param user      user
     * @param handler   result handler
     * @param orderInfo order info
     */
    public static void sendCancelOrderRequest(String orderUUID, User user,
                                              Handler handler, TableReservationInfo orderInfo) {
        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, 15000);
        HttpConnectionParams.setSoTimeout(httpParameters, 15000);

        HttpClient httpclient = new DefaultHttpClient(httpParameters);
        HttpPost httppost = new HttpPost(CANCEL_ORDER_URL);
        Log.e(TAG, "CANCEL_ORDER_URL = " + CANCEL_ORDER_URL);
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

        // order details
        nameValuePairs.add(new BasicNameValuePair("order_uid",
                orderUUID));

        // user details
        nameValuePairs.add(new BasicNameValuePair("user_id",
                user.getAccountId()));
        String userType = null;
        if (user.getAccountType() == User.ACCOUNT_TYPES.FACEBOOK) {
            userType = "facebook";
        } else if (user.getAccountType() == User.ACCOUNT_TYPES.TWITTER) {
            userType = "twitter";
        } else if (user.getAccountType() == User.ACCOUNT_TYPES.IBUILDAPP) {
            userType = "ibuildapp";
        } else if (user.getAccountType() == User.ACCOUNT_TYPES.GUEST) {
            userType = "guest";
        }
        nameValuePairs.add(new BasicNameValuePair("user_type",
                userType));

        // module info
        nameValuePairs.add(new BasicNameValuePair("app_id",
                orderInfo.getAppid()));
        nameValuePairs.add(new BasicNameValuePair("module_id",
                orderInfo.getModuleid()));

        // add security part
        nameValuePairs.add(new BasicNameValuePair("app_id",
                Statics.appId));
        nameValuePairs.add(new BasicNameValuePair("token",
                Statics.appToken));

        try {
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        } catch (UnsupportedEncodingException uEEx) {
            Log.e("", "");
        }

        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        try {
            String strResponseSaveGoal =
                    httpclient.execute(httppost, responseHandler);
            Log.d("sendCancelOrderRequest", strResponseSaveGoal);
        } catch (ConnectTimeoutException conEx) {
            handler.sendEmptyMessage(CANCEL_REQUEST_ERROR);
        } catch (ClientProtocolException ex) {
            Log.d("sendCancelOrderRequest", "");
            handler.sendEmptyMessage(CANCEL_REQUEST_ERROR);
        } catch (IOException ex) {
            Log.d("sendCancelOrderRequest", "");
            handler.sendEmptyMessage(CANCEL_REQUEST_ERROR);
        }
    }

    /**
     * This method sends HTTP request to get list of reservations.
     *
     * @param user      user
     * @param orderInfo order info
     * @return result
     */
    public static String sendListOrdersRequest(User user, TableReservationInfo orderInfo) {
        HttpParams httpParameters = new BasicHttpParams();
        httpParameters.setParameter(CoreProtocolPNames.PROTOCOL_VERSION,
                HttpVersion.HTTP_1_1);
        // Set the timeout in milliseconds until a connection is established.
        int timeoutConnection = 5000;
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        // Set the default socket timeout (SO_TIMEOUT) 
        // in milliseconds which is the timeout for waiting for data.
        int timeoutSocket = 5000;
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

        HttpClient httpclient = new DefaultHttpClient(httpParameters);
        HttpPost httppost = null;
        List<NameValuePair> nameValuePairs = null;

        httppost = new HttpPost(LIST_ORDER_URL);
        Log.e(TAG, "LIST_ORDER_URL = " + LIST_ORDER_URL);

        nameValuePairs = new ArrayList<NameValuePair>();

        // user details
        nameValuePairs.add(new BasicNameValuePair("app_id",
                orderInfo.getAppid()));
        nameValuePairs.add(new BasicNameValuePair("module_id",
                orderInfo.getModuleid()));
        nameValuePairs.add(new BasicNameValuePair("user_id",
                user.getAccountId()));

        // add security part
        nameValuePairs.add(new BasicNameValuePair("app_id",
                Statics.appId));
        nameValuePairs.add(new BasicNameValuePair("token",
                Statics.appToken));

        // user details
        String userType = null;
        if (user.getAccountType() == User.ACCOUNT_TYPES.FACEBOOK) {
            userType = "facebook";
        } else if (user.getAccountType() == User.ACCOUNT_TYPES.TWITTER) {
            userType = "twitter";
        } else if (user.getAccountType() == User.ACCOUNT_TYPES.IBUILDAPP) {
            userType = "ibuildapp";
        } else if (user.getAccountType() == User.ACCOUNT_TYPES.GUEST) {
            userType = "guest";
        }
        nameValuePairs.add(new BasicNameValuePair("user_type",
                userType));

        try {
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        } catch (UnsupportedEncodingException uEEx) {
            Log.e("", "");
            return "error";
        }

        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        try {
            String strResponseSaveGoal;
            strResponseSaveGoal = httpclient.execute(httppost, responseHandler);
            String error = JSONParser.parseQueryError(strResponseSaveGoal);

            if (error == null || error.length() == 0) {
                return strResponseSaveGoal;
            } else {
                return "error";
            }

        } catch (ConnectTimeoutException conEx) {
            Log.d("sendListOrdersRequest", "");
            return "error";
        } catch (ClientProtocolException ex) {
            Log.d("sendListOrdersRequest", "");
            return "error";
        } catch (IOException ex) {
            Log.d("sendListOrdersRequest", "");
            return "error";
        }
    }

    /**
     * This method call server to send email.
     *
     * @param template  1 - send confirmation mail to owner
     *                  2 - send cancellation mail to owner
     *                  3 - send confirmation mail to customer
     *                  4 - send cancellation mail to customer
     * @param user      user
     * @param orderInfo order info
     * @param appName   application name
     * @param handler   result handler
     * @return error code
     */
    public static String sendMail(int template, User user, TableReservationInfo orderInfo, String appName, Handler handler) {
        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, 15000);
        HttpConnectionParams.setSoTimeout(httpParameters, 15000);

        HttpClient httpclient = new DefaultHttpClient(httpParameters);
        HttpPost httppost = new HttpPost(MAIL_ORDER_URL);
        Log.e(TAG, "MAIL_ORDER_URL = " + MAIL_ORDER_URL);

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

        // restaurant details
        nameValuePairs.add(new BasicNameValuePair("app_name",
                appName));
        nameValuePairs.add(new BasicNameValuePair("restaurant_name",
                orderInfo.getRestaurantName()));
        nameValuePairs.add(new BasicNameValuePair("restaurant_address",
                orderInfo.getRestaurantadress()));
        nameValuePairs.add(new BasicNameValuePair("restaurant_phone",
                orderInfo.getRestaurantphone()));

        // user details
        String userType = null;
        if (user.getAccountType() == User.ACCOUNT_TYPES.FACEBOOK) {
            userType = "facebook";
        } else if (user.getAccountType() == User.ACCOUNT_TYPES.TWITTER) {
            userType = "twitter";
        } else if (user.getAccountType() == User.ACCOUNT_TYPES.IBUILDAPP) {
            userType = "ibuildapp";
        } else if (user.getAccountType() == User.ACCOUNT_TYPES.GUEST) {
            userType = "guest";
        }
        nameValuePairs.add(new BasicNameValuePair("user_type",
                userType));
        nameValuePairs.add(new BasicNameValuePair("user_id",
                user.getAccountId()));
        nameValuePairs.add(new BasicNameValuePair("user_name",
                user.getUserName()));
        nameValuePairs.add(new BasicNameValuePair("user_first_name",
                user.getUserFirstName()));
        nameValuePairs.add(new BasicNameValuePair("user_last_name",
                user.getUserLastName()));

        // order details
        Date tempDate = orderInfo.getOrderDate();
        tempDate.setHours(orderInfo.getOrderTime().houres);
        tempDate.setMinutes(orderInfo.getOrderTime().minutes);
        String timeZone = timeZoneToString();
        nameValuePairs.add(new BasicNameValuePair("time_zone", timeZone));
        nameValuePairs.add(new BasicNameValuePair("order_date_time",
                Long.toString(tempDate.getTime() / 1000)));
        nameValuePairs.add(new BasicNameValuePair("order_persons",
                Integer.toString(orderInfo.getPersonsAmount())));

        // contacts details
        nameValuePairs.add(new BasicNameValuePair("customer_email",
                user.getUserEmail()));
        nameValuePairs.add(new BasicNameValuePair("owner_email",
                orderInfo.getRestaurantmail()));
        nameValuePairs.add(new BasicNameValuePair("mail_type",
                Integer.toString(template)));

        // add security part
        nameValuePairs.add(new BasicNameValuePair("app_id",
                Statics.appId));
        nameValuePairs.add(new BasicNameValuePair("token",
                Statics.appToken));

        try {
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        } catch (UnsupportedEncodingException uEEx) {
            Log.e("", "");
            handler.sendEmptyMessage(3);
            return "error";
        }

        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        try {
            String strResponseSaveGoal =
                    httpclient.execute(httppost, responseHandler);
            Log.d("sendMail", "");
            return JSONParser.parseQueryError(strResponseSaveGoal);

        } catch (ConnectTimeoutException conEx) {
            handler.sendEmptyMessage(3);
            return "error";
        } catch (ClientProtocolException ex) {
            Log.d("sendMail", "");
            handler.sendEmptyMessage(3);
            return "error";
        } catch (IOException ex) {
            Log.d("sendMail", "");
            handler.sendEmptyMessage(3);
            return "error";
        }
    }

    /**
     * Logins user via email.
     *
     * @param login user login
     * @param pass  user password
     * @return result code
     */
    public static String loginPost(String login, String pass) {
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, 15000);
        HttpConnectionParams.setSoTimeout(params, 15000);
        HttpClient httpClient = new DefaultHttpClient(params);
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

        try {
            HttpPost httpPost = new HttpPost(LOGIN_URL);
            //HttpPost httpPost = new HttpPost("http://ibuildapp.com/modules/user/login");


            // order details
            nameValuePairs.add(new BasicNameValuePair("login",
                    login));
            nameValuePairs.add(new BasicNameValuePair("password",
                    pass));

            // add security part
            nameValuePairs.add(new BasicNameValuePair("app_id",
                    Statics.appId));
            nameValuePairs.add(new BasicNameValuePair("token",
                    Statics.appToken));

            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            String resp =
                    httpClient.execute(httpPost, new BasicResponseHandler());
            Log.d("", "");
            return resp;

        } catch (Exception e) {
            Log.e("REGISTRATION ERROR", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Converts time depending on timezone.
     *
     * @return formatted string
     */
    public static String timeZoneToString() {
        int offset = Calendar.getInstance().get(Calendar.ZONE_OFFSET) / 60000;
        String sign = null;
        if (offset >= 0) {
            sign = "+";
        } else {
            sign = "-";
        }

        String houres = Integer.toString(Math.abs(offset) / 60);
        String minutes = Integer.toString(Math.abs(offset) % 60);

        if (houres.length() == 1) {
            houres = "0" + houres;
        }

        if (minutes.length() == 1) {
            minutes = "0" + minutes;
        }

        return sign + houres + minutes;
    }
}
