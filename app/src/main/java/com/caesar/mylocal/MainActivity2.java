package com.caesar.mylocal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.os.Bundle;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

public class MainActivity2 extends AppCompatActivity {


    LocationManager locationManager;
    String providerStr = "";
    String TAG = "caesar1";
    Context mContext = this;
    Boolean isOpenMockLoc = false;
    Boolean isAllowMockLoc = false;
    Boolean isSetMockLoc = false;
    Boolean isSupportGps = false;

    double lon = 121.63983917236328;
    double lat = 29.885562896728516;
    double invale = 0.0003;
    SpTool SpTool  = new SpTool();


    TextView tvLon = null;
    TextView tvLat = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        SpTool.init(this);
        lon = SpTool.getLon();
        lat = SpTool.getLat();
        initView();
        startLocat();
    }

    private void initView(){
        tvLon =  findViewById(R.id.tv_lon);
        tvLat =  findViewById(R.id.tv_lat);
        findViewById(R.id.btn_save).setOnClickListener(v -> {
            SpTool.setLon((float) lon);
            SpTool.setLat((float) lat);
        });
        findViewById(R.id.btn_lon_add).setOnClickListener(v -> {
            lon = lon+invale;
            refresh();
        });
        findViewById(R.id.btn_lon_red).setOnClickListener(v -> {
            lon = lon-invale;
            refresh();
        });
        findViewById(R.id.btn_lat_add).setOnClickListener(v -> {
            lat = lat + invale;
            refresh();
        });
        findViewById(R.id.btn_lat_red).setOnClickListener(v -> {
            lat = lat - invale;
            refresh();
        });
        refresh();
    }
    private void refresh(){
        tvLon.setText("经度:"+lon);
        tvLat.setText("纬度:"+lat);
    }

    private void  startLocat(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                        Log.d(TAG, "是否开启定位:" + startMockLocation(lon, lat));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();
    }


    /**
     * 开始模拟定位
     */
    public boolean startMockLocation(double lon, double lat) {

        if (lon <= 0.0 || lat <= 0.0) {
            Toast.makeText(mContext, "经纬度设置错误", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "经纬度设置错误。。。");
            return false;
        }
        if (!isAllowMockLoc) {
            isAllowMockLoc = isAllowMockLocation();
            if (!isAllowMockLoc) {
                Log.d(TAG, "模拟定位权限没有开启。。。");
                Toast.makeText(mContext, "模拟定位权限没有开启", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        if (!isSetMockLoc) {
            isSetMockLoc = checkPackageName(mContext.getPackageName());
            if (!isSetMockLoc) {
                Log.d(TAG, "不是模拟位置应用。。。");
                Toast.makeText(mContext, "不是模拟位置应用", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        if (!isSupportGps) {
            isSupportGps = isSupportGps();
            if (!isSupportGps) {
                Log.d(TAG, "设备中没有GPS模块。。。");
                Toast.makeText(mContext, "设备中没有GPS模块", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return openMockLocation(lon, lat);
    }

    /**
     * 判定模拟位置权限是否开启
     * true--允许
     * false--禁止
     *
     * @return
     */
    private boolean isAllowMockLocation() {
        boolean isAllow = false;
        //6.0以下
        if (Build.VERSION.SDK_INT <= 22) {
            isAllow = Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION, 0) != 0;
        } else {
            try {
                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return isAllow;
                }
                if (locationManager == null) {
                    locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
                }
                providerStr = LocationManager.GPS_PROVIDER;
                LocationProvider provider = locationManager.getProvider(providerStr);
                if (provider != null) {
                    Log.d(TAG, "provider不是空，addTestProvider");
                    locationManager.addTestProvider(
                            provider.getName()
                            , provider.requiresNetwork()
                            , provider.requiresSatellite()
                            , provider.requiresCell()
                            , provider.hasMonetaryCost()
                            , provider.supportsAltitude()
                            , provider.supportsSpeed()
                            , provider.supportsBearing()
                            , provider.getPowerRequirement()
                            , provider.getAccuracy());
                } else {
                    Log.d(TAG, "provider是空，addTestProvider");
                    locationManager.addTestProvider(
                            providerStr
                            , true, true, false, false, true, true, true
                            , Criteria.POWER_HIGH, Criteria.ACCURACY_FINE);
                }
                ///下面这个判断删除会导致Provider "gps" unknown//
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                String bestProvider = locationManager.getBestProvider(criteria, true);
                if (bestProvider == null) {
                    Log.d(TAG, "No location provider found!===ACCURACY_FINE");
                    return isAllow;
                } else {
                    criteria.setAccuracy(Criteria.ACCURACY_COARSE);
                    bestProvider = locationManager.getBestProvider(criteria, true);
                    if (bestProvider == null) {
                        Log.d(TAG, "No location provider found!===ACCURACY_COARSE");
                        return isAllow;
                    }
                }
                // 开启测试Provider
                locationManager.setTestProviderEnabled(providerStr, true);
                locationManager.setTestProviderStatus(providerStr, LocationProvider.AVAILABLE, null, System.currentTimeMillis());
                // 模拟位置可用
                isAllow = true;
                // 关闭测试Provider
                locationManager.setTestProviderEnabled(providerStr, false);
                locationManager.removeTestProvider(providerStr);
            } catch (SecurityException e) {
                e.printStackTrace();
                isAllow = false;
                Log.d(TAG, "出现异常");
            }
        }
        return isAllow;
    }

    /**
     * 该应用是否为模拟定位应用
     *
     * @param packageName 包名
     */
    private boolean checkPackageName(String packageName) {
        boolean isSet = false;
        if (packageName == null) {
            Log.d(TAG, "checkPackageName   invalid package name:");
        }
        int uid = Binder.getCallingUid();
        String[] packages = mContext.getPackageManager().getPackagesForUid(uid);
        if (packages == null) {
            Log.d(TAG, "checkPackageName  invalid UID");
        }
        for (String pkg : packages) {
            if (packageName.equals(pkg)) {
                isSet = true;
                break;
            }
        }
        if (!isSet) {
            Log.d(TAG, "checkPackageName  invalid package name: ");
        }
        return isSet;
    }

    /**
     * 检测设备是否有GPS模块
     */
    private boolean isSupportGps() {
        if (locationManager == null) {
            locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        }
        return locationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER);
    }

    /**
     * 模拟位置权限是否开启
     */
    @SuppressLint("ObsoleteSdkInt")
    private boolean openMockLocation(double longitude, double latitude) {
        locationManager.addTestProvider(LocationManager.GPS_PROVIDER,
                "requiresNetwork".equals(""), "requiresSatellite".equals(""),
                "requiresCell".equals(""), "hasMonetaryCost".equals(""),
                "supportsAltitude".equals(""), "supportsSpeed".equals(""),
                "supportsBearing".equals(""), Criteria.POWER_LOW,
                Criteria.ACCURACY_FINE);
        try {
            // 模拟位置（addTestProvider成功的前提下）
            Location mockLocation = new Location(providerStr);
            mockLocation.setLatitude(latitude);   // 维度（度）
            mockLocation.setLongitude(longitude);  // 经度（度）
            mockLocation.setAccuracy(0.1f);   // 精度（米）
            mockLocation.setTime(System.currentTimeMillis());   // 本地时间
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                mockLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
            }
            locationManager.setTestProviderLocation(providerStr, mockLocation);
            isOpenMockLoc = true;
        } catch (Exception e) {
            // 防止用户在软件运行过程中关闭模拟位置或选择其他应用
            e.printStackTrace();
            Log.d(TAG, "模拟定位出错了====" + e.getMessage());
            isOpenMockLoc = false;
            //            stopMockLocation();
        }
        return isOpenMockLoc;
    }


    private class RunnableMockLocation implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(1000);

                    try {
                        // 模拟位置（addTestProvider成功的前提下）
                        String providerStr = LocationManager.GPS_PROVIDER;
                        Location mockLocation = new Location(providerStr);
                        mockLocation.setLatitude(22);   // 维度（度）
                        mockLocation.setLongitude(113);  // 经度（度）
                        mockLocation.setAltitude(30);    // 高程（米）
                        mockLocation.setBearing(180);   // 方向（度）
                        mockLocation.setSpeed(10);    //速度（米/秒）
                        mockLocation.setAccuracy(0.1f);   // 精度（米）
                        mockLocation.setTime(new Date().getTime());   // 本地时间
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            mockLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
                        }
                        locationManager.setTestProviderLocation(providerStr, mockLocation);
                    } catch (Exception e) {
                        // 防止用户在软件运行过程中关闭模拟位置或选择其他应用
                        //                        stopMockLocation();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}