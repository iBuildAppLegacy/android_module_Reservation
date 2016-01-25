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

import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;

import com.ibuildapp.romanblack.TableReservationPlugin.entity.FanWallMessage;
import com.ibuildapp.romanblack.TableReservationPlugin.entity.FanWallUser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class provides static methods for JSON parsing.
 */
public class JSONParser {

    /**
     * Parses JSON messages data.
     * @param data JSON data to parse.
     * @return messages array
     */
    public static ArrayList<FanWallMessage> parseMessagesString(String data) {
        try {
            String resp = data;

            if (resp == null) {
                return null;
            }

            if (resp.length() == 0) {
                return null;
            }

            JSONObject mainObject = new JSONObject(resp);

            JSONArray messagesJSON = mainObject.getJSONArray("posts");

            ArrayList<FanWallMessage> parsedMessages = new ArrayList<FanWallMessage>();

            for (int i = 0; i < messagesJSON.length(); i++) {
                JSONObject messageJSON = messagesJSON.getJSONObject(i);

                FanWallMessage tmpMessage = new FanWallMessage();
                tmpMessage.setId(new Long(messageJSON.getString("post_id")).longValue());
                tmpMessage.setAuthor(messageJSON.getString("user_name"));
                tmpMessage.setDate(new Date(
                        new Long(messageJSON.getString("create")).longValue()));
                tmpMessage.setUserAvatarUrl(messageJSON.getString("user_avatar"));
                tmpMessage.setText(messageJSON.getString("text"));
                try {
                    tmpMessage.setPoint(
                            new Float(messageJSON.getString("latitude")).floatValue(),
                            new Float(messageJSON.getString("longitude")).floatValue());
                } catch (NumberFormatException nFEx) {
                    Log.e("", "");
                }

                try {
                    tmpMessage.setParentId(new Integer(messageJSON.getString("parent_id")).intValue());
                } catch (NumberFormatException nFEx) {
                    Log.e("", "");
                }

                try {
                    tmpMessage.setReplyId(new Integer(messageJSON.getString("reply_id")).intValue());
                } catch (NumberFormatException nFEx) {
                    Log.e("", "");
                }

                tmpMessage.setTotalComments(new Integer(messageJSON.getString("total_comments")).intValue());

                JSONArray imagesJSON = messageJSON.getJSONArray("images");
                if (imagesJSON.length() > 0) {
                    tmpMessage.setImageUrl(imagesJSON.getString(0));
                }

                tmpMessage.setAccountId(messageJSON.getString("account_id"));
                tmpMessage.setAccountType(messageJSON.getString("account_type"));

                parsedMessages.add(tmpMessage);
            }

            return parsedMessages;
        } catch (JSONException jSSONEx) {
            return null;
        }
    }

    /**
     * Downloads and parses JSON messages data.
     * @param url URL resource that contains JSON data
     * @return messages array
     */
    public static ArrayList<FanWallMessage> parseMessagesUrl(String url) {
        try {
            String resp = loadURLData(url);

            if (resp == null) {
                return null;
            }

            if (resp.length() == 0) {
                return null;
            }

            JSONObject mainObject = new JSONObject(resp);

            JSONArray messagesJSON = mainObject.getJSONArray("posts");

            ArrayList<FanWallMessage> parsedMessages = new ArrayList<FanWallMessage>();

            for (int i = 0; i < messagesJSON.length(); i++) {
                JSONObject messageJSON = messagesJSON.getJSONObject(i);

                FanWallMessage tmpMessage = new FanWallMessage();
                tmpMessage.setId(Long.valueOf(messageJSON.getString("post_id")).longValue());
                tmpMessage.setAuthor(messageJSON.getString("user_name"));
                tmpMessage.setDate(new Date(
                        Long.valueOf(messageJSON.getString("create")).longValue()));
                tmpMessage.setUserAvatarUrl(messageJSON.getString("user_avatar"));
                tmpMessage.setText(messageJSON.getString("text"));
                try {
                    tmpMessage.setPoint(
                            Float.valueOf(messageJSON.getString("latitude")).floatValue(),
                            Float.valueOf(messageJSON.getString("longitude")).floatValue());
                } catch (NumberFormatException nFEx) {
                }

                try {
                    tmpMessage.setParentId(Integer.valueOf(messageJSON.getString("parent_id")).intValue());
                } catch (NumberFormatException nFEx) {
                    Log.e("", "");
                }

                try {
                    tmpMessage.setReplyId(Integer.valueOf(messageJSON.getString("reply_id")).intValue());
                } catch (NumberFormatException nFEx) {
                    Log.e("", "");
                }

                tmpMessage.setTotalComments(Integer.valueOf(messageJSON.getString("total_comments")).intValue());

                JSONArray imagesJSON = messageJSON.getJSONArray("images");
                if (imagesJSON.length() > 0) {
                    tmpMessage.setImageUrl(imagesJSON.getString(0));
                }

                tmpMessage.setAccountId(messageJSON.getString("account_id"));
                tmpMessage.setAccountType(messageJSON.getString("account_type"));

                parsedMessages.add(tmpMessage);
            }

            return parsedMessages;
        } catch (JSONException jSSONEx) {
            return null;
        }
    }

    /**
     * Downloads and parses JSON messages that contains images data.
     * @param url URL resource that contains JSON data
     * @return messages array
     */
    public static ArrayList<FanWallMessage> parseGalleryUrl(String url) {
        try {
            String resp = loadURLData(url);

            if (resp == null) {
                return null;
            }

            if (resp.length() == 0) {
                return null;
            }

            JSONObject mainObject = new JSONObject(resp);

            JSONArray messagesJSON = mainObject.getJSONArray("gallery");

            ArrayList<FanWallMessage> parsedMessages = new ArrayList<FanWallMessage>();

            for (int i = 0; i < messagesJSON.length(); i++) {
                JSONObject messageJSON = messagesJSON.getJSONObject(i);

                FanWallMessage tmpMessage = new FanWallMessage();
                tmpMessage.setAuthor(messageJSON.getString("user_name"));
                tmpMessage.setText(messageJSON.getString("text"));

                JSONArray imagesJSON = messageJSON.getJSONArray("images");
                if (imagesJSON.length() > 0) {
                    tmpMessage.setImageUrl(imagesJSON.getString(0));
                }

                parsedMessages.add(tmpMessage);
            }

            return parsedMessages;
        } catch (JSONException jSSONEx) {
            return null;
        }
    }

    /**
     * Downloads and parses JSON profile data.
     * @param url URL resource that contains JSON data
     * @return the profile data strings
     */
    public static String[] parseProfileData(String url) {
        try {
            String[] res = new String[3];

            String resp = loadURLData(url);

            if (resp == null) {
                return null;
            }

            if (resp.length() == 0) {
                return null;
            }

            JSONObject mainObject = new JSONObject(resp);

            JSONObject dataObject = mainObject.getJSONObject("data");

            try {
                res[0] = dataObject.getString("total_posts");
            } catch (JSONException jSONEx) {
                Log.e("", "");
            }
            try {
                res[1] = dataObject.getString("total_comments");
            } catch (JSONException jSONEx) {
                Log.e("", "");
            }
            try {
                res[2] = dataObject.getString("last_message");
            } catch (JSONException jSONEx) {
                Log.e("", "");
            }

            Log.e("", "");

            return res;
        } catch (JSONException jSONEx) {
            return null;
        }
    }

    /**
     * Downloads and parses JSON login request data.
     * @param loginUrl URL resource that contains JSON data
     * @return the login request result
     */
    public static FanWallUser parseLoginRequestUrl(String loginUrl) {
        try {

            String resp = loadURLData(loginUrl);

            if (resp == null) {
                return null;
            }

            if (resp.length() == 0) {
                return null;
            }

            JSONObject mainObject = new JSONObject(resp);

            JSONObject dataObject = mainObject.getJSONObject("data");

            FanWallUser fwUser = new FanWallUser();
            fwUser.setAccountId(dataObject.getString("user_id"));
            fwUser.setUserName(dataObject.getString("user_name"));
            fwUser.setAvatarUrl(dataObject.getString("avatar"));
            fwUser.setAccountType("ibuildapp");

            return fwUser;

        } catch (JSONException jSONEx) {
            return null;
        }
    }

    /**
     * Parses JSON login request data.
     * @param loginUrl string that contains JSON data
     * @return the login request result
     */
    public static FanWallUser parseLoginRequestString(String loginUrl) {
        try {

            String resp = loginUrl;

            if (resp == null) {
                return null;
            }

            if (resp.length() == 0) {
                return null;
            }

            JSONObject mainObject = new JSONObject(resp);

            JSONObject dataObject = mainObject.getJSONObject("data");

            FanWallUser fwUser = new FanWallUser();
            fwUser.setAccountId(dataObject.getString("user_id"));
            fwUser.setUserName(dataObject.getString("username"));
            fwUser.setAvatarUrl(dataObject.getString("user_avatar"));
            fwUser.setAccountType("ibuildapp");

            return fwUser;

        } catch (JSONException jSONEx) {
            return null;
        }
    }

    /**
     * Download URL data to String.
     * @param msgsUrl URL to download
     * @return data string
     */
    private static String loadURLData(String msgsUrl) {
        try {
            URL url = new URL(msgsUrl);
            URLConnection conn = url.openConnection();
            InputStreamReader streamReader = new InputStreamReader(conn.getInputStream());

            BufferedReader br = new BufferedReader(streamReader);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            br.close();
            String resp = sb.toString();

            return resp;
        } catch (IOException iOEx) {
            return "";
        }
    }

    /**
     * Parses JSON order list data.
     * @param source string that contains JSON data
     * @return parsed order info
     */
    public static orderParsedInfo parseOrderList(String source) {
        JSONObject mainObject = null;
        try {
            orderParsedInfo orderList = new orderParsedInfo();

            mainObject = new JSONObject(source);
            orderList.errorNo = mainObject.getString("error");
            JSONArray dataObject = mainObject.getJSONArray("data");

            for (int i = 0; i < dataObject.length(); i++) {
                JSONObject jsonOrder = (JSONObject) dataObject.get(i);
                TableReservationOrderInfo tempOrder = new TableReservationOrderInfo();
                tempOrder.uuid = jsonOrder.getString("order_uid");
                tempOrder.personsAmount = jsonOrder.getInt("order_persons");
                tempOrder.status = jsonOrder.getInt("status");
                tempOrder.specRequest = jsonOrder.getString("order_spec_request");
                tempOrder.orderDate = new Date(Long.parseLong(jsonOrder.getString("order_date_time")) * 1000);
                tempOrder.orderTime.houres = tempOrder.orderDate.getHours();
                tempOrder.orderTime.minutes = tempOrder.orderDate.getMinutes();
                orderList.itemsArray.add(tempOrder);
            }

            return orderList;

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Parses error from jsom if some error was occured.
     * @param source string that contains JSON data
     * @return error string
     */
    public static String parseQueryError(String source) {
        JSONObject mainObject = null;
        try {
            mainObject = new JSONObject(source);
            return mainObject.getString("error");

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}

/**
 * This class describes unparsed JSON answer to OrderList request.
 */
class orderParsedInfo {

    public String errorNo;
    public ArrayList<TableReservationOrderInfo> itemsArray;
    
    /**
     * Construct new orderParsedInfo instance.
     */
    orderParsedInfo() {
        this.errorNo = null;
        this.itemsArray = new ArrayList<TableReservationOrderInfo>();
    }
}
