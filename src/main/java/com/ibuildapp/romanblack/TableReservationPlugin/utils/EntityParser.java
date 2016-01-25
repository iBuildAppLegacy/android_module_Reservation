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

import android.graphics.Color;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.util.Log;
import android.util.Xml;
import com.appbuilder.sdk.android.Utils;
import com.ibuildapp.romanblack.TableReservationPlugin.TableReservationInfo;

import java.io.ByteArrayInputStream;

/**
 * This class using for module xml data parsing.
 */
public class EntityParser {

    private TableReservationInfo parsedXML;
    private String xml = "";

    /**
     * Constructs new EntityParser instance.
     * @param xml - module xml data to parse
     */
    public EntityParser(String xml) {
        this.xml = xml;
        this.parsedXML = new TableReservationInfo();
    }

    /**
     * Returns the parsing result.
     * @return the parsing result
     */
    public TableReservationInfo getTableReservationInfo() {
        return parsedXML;
    }

    /**
     * Parses module data that was set in constructor.
     */
    public void parse() {

        RootElement root = new RootElement("data");

        root.setEndElementListener(new EndElementListener() {
            @Override
            public void end() {
            }
        });

        root.getChild("color1").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                if (body != null) {
                    parsedXML.colorskin.color1 = Color.parseColor(body);
                }
            }
        });

        root.getChild("color2").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                if (body != null) {
                    parsedXML.colorskin.color2 = Color.parseColor(body);
                }
            }
        });

        root.getChild("starttime").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                if (body != null) {
                    int sec = Integer.parseInt(new String(body));
                    int startHH = (int) (sec / 3600);
                    int startMM = (sec / 60) - (startHH * 60);
                    parsedXML.setStartTime(startHH, startMM);
                }
            }
        });

        root.getChild("endtime").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                if (body != null) {
                    int sec = Integer.parseInt(new String(body));
                    int startHH = (int) (sec / 3600);
                    int startMM = (sec / 60) - (startHH * 60);
                    parsedXML.setEndTime(startHH, startMM);
                }
            }
        });

        root.getChild("timeoffset").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                if (body != null) {
                    parsedXML.setOffsetTime(Integer.parseInt(new String(body)));
                }
            }
        });

        root.getChild("maxpersons").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                if (body != null) {
                    parsedXML.setMaxpersons(Integer.parseInt(new String(body)));
                }
            }
        });


        root.getChild("longitude").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                if (body != null) {
                    parsedXML.setLongitude(Double.parseDouble(body));
                }
            }
        });

        root.getChild("latitude").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                if (body != null) {
                    parsedXML.setLatitude(Double.parseDouble(body));
                }
            }
        });

        root.getChild("restaurantname").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                if (body != null) {
                    parsedXML.setRestaurantName(Utils.removeSpec(body));
                }
            }
        });

        root.getChild("restaurantimageurl").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                if (body != null) {
                    parsedXML.setRestaurantimageurl(body);
                }
            }
        });

        root.getChild("restaurantaddress").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                if (body != null) {
                    parsedXML.setRestaurantadress(Utils.removeSpec(body));
                }
            }
        });

        root.getChild("restaurantgreeting").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                if (body != null) {
                    parsedXML.setRestaurantGreeting(Utils.removeSpec(body));
                }
            }
        });

        root.getChild("restaurantmail").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                if (body != null) {
                    parsedXML.setRestaurantmail(Utils.removeSpec(body));
                }
            }
        });

        root.getChild("restaurantphone").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                if (body != null) {
                    parsedXML.setRestaurantphone(Utils.removeSpec(body));
                }
            }
        });

        root.getChild("emailconfirmation").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                if (body != null) {
                    if (body.equals("1")) {
                        parsedXML.setEmailConfirmation(true);
                    } else {
                        parsedXML.setEmailConfirmation(false);
                    }
                }
            }
        });

        root.getChild("smsconfirmation").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                if (body != null) {
                    if (body.equals("1")) {
                        parsedXML.setSmsConfirmation(true);
                    } else {
                        parsedXML.setSmsConfirmation(false);
                    }
                }
            }
        });

        root.getChild("parking").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                if (body != null) {
                    parsedXML.setRestaurantadditional(Utils.removeSpec(body));
                }
            }
        });

        root.getChild("app_id").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                if (body != null) {
                    parsedXML.setAppid(Utils.removeSpec(body));
                }
            }
        });

        root.getChild("module_id").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                if (body != null) {
                    parsedXML.setModuleid(Utils.removeSpec(body));
                }
            }
        });

        root.getChild("restaurantkitchen").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                if (body != null) {
                    parsedXML.setKitchen(Utils.removeSpec(body));
                }
            }
        });

        try {
            Xml.parse(new ByteArrayInputStream(xml.getBytes()), Xml.Encoding.UTF_8, root.getContentHandler());
        } catch (Exception e) {
            Log.d("parse", e.getMessage());
        }
    }
}