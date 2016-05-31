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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;
import com.appbuilder.sdk.android.AppBuilderModule;
import com.ibuildapp.romanblack.TableReservationPlugin.utils.TableReservationMapWebPageCreator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This activity represents restaurant map page.
 */
public class TableReservationMap extends AppBuilderModule implements LocationListener {

    private final int INITIALIZATION_FAILED = 0;
    private final int NEED_INTERNET_CONNECTION = 1;
    private final int SHOW_MAP = 2;
    private final int HIDE_PROGRESS_DIALOG = 3;
    private final int CLOSE_ACTIVITY = 4;
    private final int NO_GPS_SERVICE = 5;
    private final int SEARCH_LOCATION = 6;
    private final int DRAW_ROUTE = 7;
    private final int CHOSE_ROUTE_FINAL = 8;
    private final int SHOW_PROGRESS_DIALOG = 9;
    private final int GO_TO_URL = 10;
    private float srcLatitude = 0;
    private float srcLongitude = 0;
    private float dstLatitude = 0;
    private float dstLongitude = 0;
    private String title = "";
    private String htmlSource = "";
    private String urlToGo = "";
    private TableReservationMapLocation gpsLocation = null;
    private TableReservationMapLocation tempLocation = null;
    private TableReservationMapLocation userLocation = null;
    private TableReservationInfo orderInfo;
    private LocationManager locationManager = null;
    private ArrayList<TableReservationMapLocation> locations = new ArrayList<TableReservationMapLocation>();
    private ProgressDialog progressDialog = null;
    private WebView mapView = null;
    private Button btnDirection = null;
    private Button btnMyLocation = null;
    private Spinner locationSpinner = null;
    private TableReservationMapBottomPanel mapBottomPanel = null;
    private Timer mTimer;
    private TimerTask mTask;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case INITIALIZATION_FAILED: {
                    Toast.makeText(TableReservationMap.this,
                            R.string.alert_cannot_init,
                            Toast.LENGTH_LONG).show();
                    closeActivity();
                }
                break;
                case NEED_INTERNET_CONNECTION: {
                    Toast.makeText(TableReservationMap.this,
                            R.string.alert_no_internet,
                            Toast.LENGTH_LONG).show();
                    closeActivity();
                }
                break;
                case SHOW_MAP: {
                    showMap();
                }
                break;
                case HIDE_PROGRESS_DIALOG: {
                    hideProgressDialog();
                }
                break;
                case CLOSE_ACTIVITY: {
                    closeActivity();
                }
                break;
                case NO_GPS_SERVICE: {
                    Toast.makeText(TableReservationMap.this,
                            R.string.alert_gps_not_available,
                            Toast.LENGTH_LONG).show();
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                        }
                    }, 5000);
                }
                break;
                case SEARCH_LOCATION: {
                    Toast.makeText(TableReservationMap.this,
                            R.string.alert_gps_initialization,
                            Toast.LENGTH_LONG).show();
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                        }
                    }, 7000);
                }
                break;
                case DRAW_ROUTE: {
                    startRoute();
                }
                break;
                case CHOSE_ROUTE_FINAL: {
                    choseRouteFinal();
                }
                break;
                case SHOW_PROGRESS_DIALOG: {
                    showProgressDialog();
                }
                break;
                case GO_TO_URL: {
                    TableReservationMap.this.goToUrl(urlToGo, "");
                }
                break;
            }
        }
    };

    @Override
    public void destroy() {
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void create() {
        try {
            // disable activity header
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.sergeyb_tablereservation_mapweb_main);
            setTitle("Google Map");

            // getting info from parent activity
            Intent currentIntent = getIntent();
            orderInfo = (TableReservationInfo) currentIntent.getSerializableExtra("xml");

            // checking internet connection
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = cm.getActiveNetworkInfo();
            if (ni != null && ni.isConnectedOrConnecting()) {
                // for storing map and showing progress dialog
                mapView = (WebView) findViewById(R.id.sergeyb_tablereservation_mapweb_webview);
                mapView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
                mapView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);

                        handler.sendEmptyMessage(HIDE_PROGRESS_DIALOG);
                    }
                    @Override
                    public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(TableReservationMap.this);
                        builder.setMessage(R.string.notification_error_ssl_cert_invalid);
                        builder.setPositiveButton(TableReservationMap.this.getResources().getString(R.string.on_continue), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                handler.proceed();
                            }
                        });
                        builder.setNegativeButton(TableReservationMap.this.getResources().getString(R.string.on_cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                handler.cancel();
                            }
                        });
                        final AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon) {
                        super.onPageStarted(view, url, favicon);

                        handler.sendEmptyMessage(SHOW_PROGRESS_DIALOG);
                    }
                });

                mapView.addJavascriptInterface(new JavaScriptInterface(), "googleredirect");
                mapView.getSettings().setJavaScriptEnabled(true);
//                mapView.getSettings().setPluginsEnabled(true);
                mapView.getSettings().setGeolocationEnabled(true);

                // Mylocation Button handler
                btnMyLocation = (Button) findViewById(R.id.sergeyb_tablereservation_mapweb_back_to_my_location);
                btnMyLocation.setOnClickListener(new OnClickListener() {
                    public void onClick(View arg0) {

                        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            handler.sendEmptyMessage(NO_GPS_SERVICE);
                            return;
                        }

                        if (gpsLocation == null) {
                            if (tempLocation == null) {
                                handler.sendEmptyMessage(SEARCH_LOCATION);
                                return;
                            } else {
                                userLocation = tempLocation;
                            }
                        } else {
                            tempLocation = gpsLocation;
                            userLocation = tempLocation;
                        }

                        srcLatitude = userLocation.getLatitude();
                        srcLongitude = userLocation.getLongitude();
                        srcLatitude = srcLatitude / 1000000;
                        srcLongitude = srcLongitude / 1000000;
                        BigDecimal lat = new BigDecimal(srcLatitude);
                        lat = lat.setScale(6, BigDecimal.ROUND_HALF_UP);
                        BigDecimal lon = new BigDecimal(srcLongitude);
                        lon = lon.setScale(6, BigDecimal.ROUND_HALF_UP);
                        mapView.loadUrl("javascript:backToMyLocation(" + lat.toString() + "," + lon.toString() + ")");
                    }
                });

                // ShowDirection Button handler
                btnDirection = (Button) findViewById(R.id.sergeyb_tablereservation_mapweb_user_direction);
                btnDirection.setOnClickListener(new OnClickListener() {
                    public void onClick(View arg0) {
                        try {
                            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                handler.sendEmptyMessage(NO_GPS_SERVICE);
                                return;
                            }
                            if (gpsLocation == null) {
                                if (tempLocation == null) {
                                    handler.sendEmptyMessage(SEARCH_LOCATION);
                                    return;
                                } else {
                                    userLocation = tempLocation;
                                }
                            } else {
                                tempLocation = gpsLocation;
                                userLocation = tempLocation;
                            }

                            srcLatitude = userLocation.getLatitude();
                            srcLongitude = userLocation.getLongitude();

                            if (locations.isEmpty()) {
                            } else if (locations.size() == 1) {
                                dstLatitude = locations.get(0).getLatitude();
                                dstLongitude = locations.get(0).getLongitude();
                                handler.sendEmptyMessage(DRAW_ROUTE);
                            } else {
                                handler.sendEmptyMessage(CHOSE_ROUTE_FINAL);
                            }

                        } catch (Exception e) {
                        }
                    }
                });

                mapBottomPanel = (TableReservationMapBottomPanel) findViewById(R.id.sergeyb_tablereservation_mapweb_bottom_panel);

                // obtain locatonManager object
                locationManager = (LocationManager) TableReservationMap.this.
                        getSystemService(LOCATION_SERVICE);

                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    handler.sendEmptyMessage(NO_GPS_SERVICE);
                }

                // show progress dialog
                progressDialog = ProgressDialog.show(this, "", getResources().getString(R.string.common_loading_upper));
                progressDialog.setCancelable(true);
                progressDialog.setOnCancelListener(new OnCancelListener() {
                    public void onCancel(DialogInterface arg0) {
                        handler.sendEmptyMessage(CLOSE_ACTIVITY);
                    }
                });

                // update user location every 10sec
                mTimer = new Timer();
                mTask = new TimerTask() {
                    @Override
                    public void run() {
                        TimerMethod();
                    }
                };
                mTimer.scheduleAtFixedRate(mTask, 0, 12000);

                new Thread(new Runnable() {
                    public void run() {

                        try {
                            htmlSource = "";
                            try {
                                // get html source from resources 
                                InputStream is = getResources().openRawResource(R.raw.sergeyb_tablereservation_page_normal);
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                int flag = 0;
                                byte buf[] = new byte[512];
                                while ((flag = is.read(buf, 0, 512)) != -1) {
                                    baos.write(buf, 0, flag);
                                    Arrays.fill(buf, (byte) 0);
                                }
                                htmlSource = baos.toString();
                            } catch (IOException iOEx) {
                                handler.sendEmptyMessage(INITIALIZATION_FAILED);
                            }

                            htmlSource = TableReservationMapWebPageCreator.createMapPage(htmlSource, orderInfo.getLatitude(),
                                    orderInfo.getLongitude(), orderInfo.getRestaurantName(),
                                    orderInfo.getRestaurantadditional());

                            handler.sendEmptyMessage(SHOW_MAP);

                        } catch (Exception e) {
                        }

                    }
                }).start();

            } else {
                this.finish();
                Toast.makeText(getApplicationContext(), R.string.alert_no_internet,
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void resume() {
        super.resume();

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000,
                0, this);
        if (locationSpinner != null) {
            locationSpinner.setSelection(0);
        }
    }

    @Override
    public void pause() {
        super.pause();

        locationManager.removeUpdates(this);
    }

    @Override
    public void onBackPressed() {
        if (mapView.getUrl().equalsIgnoreCase("about:blank")) {
            boolean log = mTask.cancel();
            mTimer.cancel();
            super.onBackPressed();
        } else {
            showMap();
            mapBottomPanel.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Loads prepared map HTML code in WebView.
     */
    private void showMap() {
        try {

            if (title != null) {
                if (title.length() > 0) {
                    setTitle(title);
                }
            }

            mapView.loadDataWithBaseURL("", htmlSource, "text/html",
                    "utf-8", "");

        } catch (Exception e) {
        }
    }

    /**
     * Shows destination picker dialog.
     */
    private void choseRouteFinal() {
        try {
            if (locationSpinner == null) {
                locationSpinner = new Spinner(this);
                locationSpinner.setVisibility(View.INVISIBLE);

                ArrayList<String> strings = new ArrayList<String>();
                strings.add(getResources().getString(R.string.common_cancel_upper));
                for (int i = 0; i < locations.size(); i++) {
                    strings.add(locations.get(i).getTitle());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_spinner_item, strings);

                locationSpinner.setAdapter(adapter);
                locationSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> av, View view, int i, long l) {
                        if (i != 0) {
                            try {
                                dstLatitude = locations.get(i - 1).getLatitude();
                                dstLongitude = locations.get(i - 1).getLongitude();
                                handler.sendEmptyMessage(DRAW_ROUTE);
                            } catch (NullPointerException nPEx) {
                            }
                        }
                    }

                    public void onNothingSelected(AdapterView<?> av) {
                        Log.e("", "");
                    }
                });
                mapView.addView(locationSpinner);
            }

            locationSpinner.performClick();

        } catch (Exception e) {
        }
    }

    /**
     * Starts TableReservationMapRoute activity to show route to restaursnt.
     */
    private void startRoute() {
        try {

            String routeURL = "";

            StringBuilder sb = new StringBuilder();
            sb.append("http://maps.google.com/maps?saddr=");
            sb.append(srcLatitude / 1E6);
            sb.append(",");
            sb.append(srcLongitude / 1E6);
            sb.append("&daddr=");
            sb.append(dstLatitude / 1E6);
            sb.append(",");
            sb.append(dstLongitude / 1E6);
            sb.append("&ll=");
            sb.append((srcLatitude / 1E6 + dstLatitude / 1E6) / 2);
            sb.append(",");
            sb.append((srcLongitude / 1E6 + dstLongitude / 1E6) / 2);
            sb.append("&z=");

            int z = 0;
            int gr = 0;
            if (Math.abs(srcLatitude - dstLatitude)
                    > Math.abs(srcLongitude - dstLongitude)) {
                gr = Math.abs((int) (srcLatitude - dstLatitude));
            } else {
                gr = Math.abs((int) (srcLongitude - dstLongitude));
            }
            if (gr > (120 * 1E6)) {
                z = 1;
            } else if (gr > (60 * 1E6)) {
                z = 2;
            } else if (gr > (30 * 1E6)) {
                z = 3;
            } else if (gr > (15 * 1E6)) {
                z = 4;
            } else if (gr > (8 * 1E6)) {
                z = 5;
            } else if (gr > (4 * 1E6)) {
                z = 6;
            } else if (gr > (2 * 1E6)) {
                z = 7;
            } else if (gr > (1 * 1E6)) {
                z = 8;
            } else if (gr > (0.5 * 1E6)) {
                z = 9;
            } else {
                z = 10;
            }

            sb.append(z);

            routeURL = sb.toString();

            Intent intent = new Intent(this, TableReservationMapRoute.class);
            intent.putExtra("url", routeURL);
            startActivity(intent);

        } catch (Exception e) {
        }
    }

    private void showProgressDialog() {
        if (progressDialog != null) {
            if (!progressDialog.isShowing()) {
                progressDialog = ProgressDialog.show(this, "", getResources().getString(R.string.common_loading_upper));
            }
        }
    }

    private void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private void closeActivity() {
        hideProgressDialog();
        finish();
    }

    public void onLocationChanged(Location arg0) {
        if (arg0 != null) {
            gpsLocation = new TableReservationMapLocation(arg0.getLatitude(),
                    arg0.getLongitude());
        }
    }

    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
    }

    public void onProviderEnabled(String arg0) {
    }

    public void onProviderDisabled(String arg0) {
    }

    /**
     * Redirects to location details page when details link was clicked.
     */
    private final class JavaScriptInterface {

        public JavaScriptInterface() {
        }

        /**
         * Redirects to given URL.
         * @param urlToGo URL to redirect
         * @param pointName the map piont name
         */
        public void goToUrl(String urlToGo, String pointName) {
            TableReservationMap.this.urlToGo = urlToGo;
            handler.sendEmptyMessage(GO_TO_URL);
        }
    }

    /**
     * Redirects to given URL.
     * @param urlToGo URL to redirect
     * @param pointName the map piont name
     */
    private void goToUrl(String urlToGo, String pointName) {
        mapView.loadUrl(urlToGo);
        mapBottomPanel.setVisibility(View.GONE);
    }

    /**
     * Starts user location updatings.
     */
    private void TimerMethod() {
        this.runOnUiThread(Timer_Tick);
    }
    
    /**
     * This implementation updates user map location.
     */
    private Runnable Timer_Tick = new Runnable() {
        public void run() {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                return;
            }

            if (gpsLocation == null) {
                if (tempLocation == null) {
                    return;
                } else {
                    userLocation = tempLocation;
                }
            } else {
                tempLocation = gpsLocation;
                userLocation = tempLocation;
            }

            srcLatitude = userLocation.getLatitude();
            srcLongitude = userLocation.getLongitude();
            srcLatitude = srcLatitude / 1000000;
            srcLongitude = srcLongitude / 1000000;
            BigDecimal lat = new BigDecimal(srcLatitude);
            lat = lat.setScale(6, BigDecimal.ROUND_HALF_UP);
            BigDecimal lon = new BigDecimal(srcLongitude);
            lon = lon.setScale(6, BigDecimal.ROUND_HALF_UP);
            mapView.loadUrl("javascript:moveUserMarker(" + lat.toString() + "," + lon.toString() + ")");

        }
    };
}
