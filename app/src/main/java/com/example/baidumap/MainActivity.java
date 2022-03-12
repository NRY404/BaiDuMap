package com.example.baidumap;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.route.BikingRoutePlanOption;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNaviCommonParams;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBNRoutePlanManager;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import overlayutil.overlayutil.BikingRouteOverlay;
import overlayutil.overlayutil.DrivingRouteOverlay;
import overlayutil.overlayutil.WalkingRouteOverlay;

public class MainActivity extends AppCompatActivity implements OnGetGeoCoderResultListener{

    MapView mView;
    //定位
    LocationClient mLocationClient = null;
    MyLocationListener myListener = new MyLocationListener();
    BaiduMap mBaiDuMap = null;
    RadioButton normal,satellite,self;
    CheckBox traffic;
    String address;



    private static String[] PERMISSIONS_ALL = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE
    };

    private static int REQUEST_PERMISSION_CODE = 1;

    //兴趣点内容
    Button near = null;
    //经纬度坐标的对象
    LatLng ll;
    //创建标记对象
    Marker marker;
    //兴趣点
    PoiSearch mPoiSearch;
    //地图的某个点
    GeoCoder mGeoSearch;
    TextView geoAddress;
    LatLng from;
    //图层
    ScrollView sv;
    //路线规划相关
    Button change,walkLine,ridingLine,carLine,cancel_btn;
    private RoutePlanSearch mSearch = null;
    //导航
    Button nav;
    private static final String APP_FOLDER_NAME = "BNSDKSimpleDemo";
    private String mSDCardPath = null;
    private LocationManager mLocationManager;
    private double mCurrentLat;
    private double mCurrentLng;
    private LocationListener mLocationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            mCurrentLat = location.getLatitude();
            mCurrentLng = location.getLongitude();
            Toast.makeText(MainActivity.this, mCurrentLat
                    + "--" + mCurrentLng, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //动态申请权限
        if(Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP){
            if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=
                    PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,PERMISSIONS_ALL,REQUEST_PERMISSION_CODE);
            }
        }

        mView = findViewById(R.id.bmapView);
        normal = findViewById(R.id.radioButton);
        satellite = findViewById(R.id.radioButton2);
        self = findViewById(R.id.self);
        traffic = findViewById(R.id.cb_traffic);
        sv = findViewById(R.id.sv);
        self.getBackground().setAlpha(100);
        mBaiDuMap = mView.getMap();


        mBaiDuMap.setMyLocationEnabled(true);
        mLocationClient = new LocationClient(getApplicationContext());
        initLocation();
        //设置定位监听
        mLocationClient.registerLocationListener(myListener);
        mLocationClient.start();
        //点击地图，取消当前位置单选按钮选中状态
        mBaiDuMap.setOnMapTouchListener(new BaiduMap.OnMapTouchListener() {
            @Override
            public void onTouch(MotionEvent motionEvent) {
                self.setChecked(false);
            }
        });
        //地位到当前位置，并设置当前位置为地图显示的中心
        self.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,address,Toast.LENGTH_SHORT).show();
                self.setChecked(true);
                BDLocationListener nowLocation = new MyLocationListener();
                mLocationClient = new LocationClient(getApplicationContext());
                initLocation();
                mLocationClient.registerLocationListener(nowLocation);
                mLocationClient.start();
            }
        });
        //通过关键字搜索位置附近的地址
        near = (Button)findViewById(R.id.near);
        near.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText et = new EditText(MainActivity.this);
                AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this);
                ab.setTitle("搜索附近");
                ab.setView(et);
                ab.setPositiveButton("取消",null);
                ab.setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String input = et.getText().toString();
                        if(input.equals("")){
                            Toast.makeText(MainActivity.this,"搜索内容不能为空！",Toast.LENGTH_LONG).show();
                        }else{
                            PoiNearbySearchOption nearbySearchOption = new PoiNearbySearchOption();
                            if(sv.getVisibility()==View.VISIBLE) {
                                nearbySearchOption.location(from);
                            }else{
                                nearbySearchOption.location(ll);
                            }
                            nearbySearchOption.keyword(input);
                            nearbySearchOption.radius(10000);
                            nearbySearchOption.pageNum(1);
                            mPoiSearch.searchNearby(nearbySearchOption);
                            mLocationClient.start();
                            mLocationClient.requestLocation();
                        }
                    }
                });
                ab.setIcon(android.R.drawable.ic_dialog_info);
                ab.show();

            }
        });
        //兴趣点查找的监听
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(new OnGetPoiSearchResultListener() {
            @Override
            public void onGetPoiResult(PoiResult poiResult) {
                mBaiDuMap.clear();
                if(poiResult==null||poiResult.error== SearchResult.ERRORNO.RESULT_NOT_FOUND){
                    Toast.makeText(MainActivity.this,"未找到结果",Toast.LENGTH_LONG).show();
                    return;
                }else if(poiResult.error==SearchResult.ERRORNO.NO_ERROR){
                    MyOverLay myOverLay = new MyOverLay(mBaiDuMap);
                    myOverLay.setData(poiResult);
                    mBaiDuMap.setOnMarkerClickListener(myOverLay);
                    myOverLay.addToMap();
                    myOverLay.zoomToSpan();
                }
            }
            @Override
            public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

            }
            @Override
            public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {

            }
            @Override
            public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

            }
        });
        //普通地图视图显示
        normal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(normal.isChecked()==true){
                    //mBaiDuMap = mView.getMap();
                    mBaiDuMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                }else{
                    mBaiDuMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                }
            }
        });
        //交通热力图
        traffic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(traffic.isChecked()==true){
                    mBaiDuMap.setTrafficEnabled(true);
                }else{
                    mBaiDuMap.setTrafficEnabled(false);
                }
            }
        });
        //选取地点初始化
        initGEO();
        geoAddress = (TextView)findViewById(R.id.textView);
        //路线规划初始化
        initPoutePlan();
        cancel_btn = findViewById(R.id.cancel_btn);
        walkLine = (Button)findViewById(R.id.walk);
        ridingLine = findViewById(R.id.bike);
        nav= findViewById(R.id.nav);
        change = findViewById(R.id.change);
        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sv.setVisibility(View.VISIBLE);
                change.setVisibility(View.GONE);
                nav.setVisibility(View.GONE);
            }
        });
        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sv.setVisibility(View.GONE);
                near.setVisibility(View.VISIBLE);
        }
        });
        //步行路线按钮
        walkLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sv.setVisibility(View.GONE);
                near.setVisibility(View.VISIBLE);
                StarRoute("walk");
            }
        });
        //骑行路线
        ridingLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sv.setVisibility(View.GONE);
                near.setVisibility(View.VISIBLE);
                StarRoute("ride");
            }
        });
        //驾驶路线按钮
        carLine = (Button)findViewById(R.id.car);
        carLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sv.setVisibility(View.GONE);
                StarRoute("car");
                near.setVisibility(View.VISIBLE);
            }
        });
        //导航
//        if (initDirs()) {
//           initNavi();
//        }
        initLocationNav();
        nav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nav.setVisibility(View.INVISIBLE);
                if (BaiduNaviManagerFactory.getBaiduNaviManager().isInited()) {
                    if (mCurrentLat == 0 && mCurrentLng == 0) {
                        return;
                    }

                }
                BNRoutePlanNode sNode = new BNRoutePlanNode.Builder()
                        .latitude(mCurrentLat)
                        .longitude(mCurrentLng)
                        .coordinateType(BNRoutePlanNode.CoordinateType.WGS84)
                        .build();
                BNRoutePlanNode eNode = new BNRoutePlanNode.Builder()
                        .latitude(from.latitude)
                        .longitude(from.longitude)
                        .name("目的地")
                        .description("目的地")
                        .coordinateType(BNRoutePlanNode.CoordinateType.WGS84)
                        .build();
                routePlanToNavi(sNode, eNode);
            }
        });
    }

    //定位监听
    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            MyLocationData locData = new MyLocationData.Builder().accuracy(bdLocation.getRadius())
                    .latitude(bdLocation.getLatitude())   //纬度
                    .longitude(bdLocation.getLongitude())  //经度
                    .build();
            mBaiDuMap.setMyLocationData(locData);

            ll = new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude());
//            LatLng ll = new LatLng(32.242227,118.760091);
            MapStatus.Builder builder = new MapStatus.Builder();
            builder.target(ll).zoom(18.0f);

            mBaiDuMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            if(bdLocation.getCountry()!=null){
                address = bdLocation.getCountry()+" "
                        +bdLocation.getProvince()+" "
                        +bdLocation.getCity()+"  "
                        +bdLocation.getStreet()+" "
                        +bdLocation.getStreetNumber();
            }else{
                address = "无定位信息";
            }
        }
    }


    private void initGEO(){
        mGeoSearch = GeoCoder.newInstance();
        mGeoSearch.setOnGetGeoCodeResultListener(this);

        mBaiDuMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(marker!=null){
                    marker.remove();
                    sv.setVisibility(View.GONE);
                    marker=null;
                }else {
                    LatLng lat = latLng;
                    mGeoSearch.reverseGeoCode(new ReverseGeoCodeOption().location(lat));
                }
            }

            @Override
            public void onMapPoiClick(MapPoi mapPoi) {
                return;
            }
        });

    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

    }
    //选取地址信息
    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
        if(TextUtils.isEmpty(reverseGeoCodeResult.getAddress())){
            Toast.makeText(MainActivity.this,"地址信息解析失败，请重新选择",Toast.LENGTH_SHORT).show();
        }else{
            sv.setVisibility(View.VISIBLE);
            near.setVisibility(View.GONE);
            from = new LatLng(reverseGeoCodeResult.getLocation().latitude,reverseGeoCodeResult.getLocation().longitude);
            geoAddress.setText(reverseGeoCodeResult.getAddress()+"\n"+reverseGeoCodeResult.getBusinessCircle()+
                    "\n"+reverseGeoCodeResult.getSematicDescription()+"\n距离："+distance());
            geoAddress.setVisibility(View.VISIBLE);
            //设置半透明
            sv.getBackground().setAlpha(100);
            BitmapDescriptor bdB = BitmapDescriptorFactory.fromResource(R.drawable.poi);
            OverlayOptions ooP = new MarkerOptions().position(from).icon(bdB);
            marker = (Marker)mBaiDuMap.addOverlay(ooP);
            MapStatus.Builder builder = new MapStatus.Builder();
            builder.target(from).zoom(18.0f);

            mBaiDuMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        }
    }
    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
        option.setScanSpan(0);
        option.setIsNeedAddress(true);
        option.setOpenGps(true);
        option.setLocationNotify(true);
        option.setIsNeedLocationDescribe(true);
        option.setIsNeedLocationPoiList(true);
        option.setIgnoreKillProcess(false);
        option.setEnableSimulateGps(false);
        mLocationClient.setLocOption(option);
    }

    //路线规划初始化
    private void initPoutePlan(){
        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(new OnGetRoutePlanResultListener() {
            @Override
            public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {
                if(walkingRouteResult==null||walkingRouteResult.error!=SearchResult.ERRORNO.NO_ERROR){
                    Toast.makeText(MainActivity.this,"路线规划失败，请重新选取位置!",Toast.LENGTH_LONG).show();
                }
                if(walkingRouteResult.error==SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR){
                    walkingRouteResult.getSuggestAddrInfo();
                    return;
                }
                if(walkingRouteResult.error==SearchResult.ERRORNO.NO_ERROR){
                    mBaiDuMap.clear();
                    Toast.makeText(MainActivity.this,"步行路线规划",Toast.LENGTH_SHORT).show();
                    WalkingRouteOverlay overlay = new WalkingRouteOverlay(mBaiDuMap);
                    overlay.setData(walkingRouteResult.getRouteLines().get(0));
                    overlay.addToMap();
                    overlay.zoomToSpan();
                    change.setVisibility(View.VISIBLE);
                    nav.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {

            }

            @Override
            public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {

            }

            @Override
            public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {
                if(drivingRouteResult==null||drivingRouteResult.error!=SearchResult.ERRORNO.NO_ERROR){
                    Toast.makeText(MainActivity.this,"路线规划失败，请重新选取位置!",Toast.LENGTH_LONG).show();
                }
                if(drivingRouteResult.error==SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR){
                    drivingRouteResult.getSuggestAddrInfo();
                    return;
                }
                if(drivingRouteResult.error==SearchResult.ERRORNO.NO_ERROR){
                    mBaiDuMap.clear();
                    Toast.makeText(MainActivity.this,"驾车路线规划",Toast.LENGTH_SHORT).show();
                    DrivingRouteOverlay overlay = new DrivingRouteOverlay(mBaiDuMap);
                    overlay.setData(drivingRouteResult.getRouteLines().get(0));
                    overlay.addToMap();
                    overlay.zoomToSpan();
                    change.setVisibility(View.VISIBLE);
                    nav.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

            }

            @Override
            public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {
                if (bikingRouteResult==null||bikingRouteResult.error!=SearchResult.ERRORNO.NO_ERROR){
                    Toast.makeText(MainActivity.this,"路线规划失败，请重新选取位置!",Toast.LENGTH_LONG).show();
                }
                if (bikingRouteResult.error==SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR){
                    bikingRouteResult.getSuggestAddrInfo();
                    return;
                }
                if (bikingRouteResult.error==SearchResult.ERRORNO.NO_ERROR){
                    mBaiDuMap.clear();
                    Toast.makeText(MainActivity.this,"骑行路线规划",Toast.LENGTH_SHORT).show();
                    BikingRouteOverlay overlay = new BikingRouteOverlay(mBaiDuMap);
                    overlay.setData(bikingRouteResult.getRouteLines().get(0));
                    overlay.addToMap();
                    overlay.zoomToSpan();
                    change.setVisibility(View.VISIBLE);
                    nav.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void StarRoute(String line){
        SDKInitializer.initialize(getApplicationContext());

        /*PlanNode stNode = PlanNode.withCityNameAndPlaceName("北京", "西二旗地铁站");
        PlanNode enNode = PlanNode.withCityNameAndPlaceName("北京", "百度科技园");*/
        PlanNode stNode = PlanNode.withLocation(ll);
        PlanNode enNode = PlanNode.withLocation(from);
        if(line.equals("walk")) {
            mSearch.walkingSearch((new WalkingRoutePlanOption()).from(stNode).to(enNode));

        }else if(line.equals("ride")){
            mSearch.bikingSearch((new BikingRoutePlanOption()).from(stNode).to(enNode));

                    //ridingType 0 普通骑行，1 电动车骑行
        }else if(line.equals("car")){
            mSearch.drivingSearch((new DrivingRoutePlanOption()).from(stNode).to(enNode));

        }
    }

    public String distance(){
        int dis = (int)DistanceUtil.getDistance(ll,from);
        if(dis>1000){
            /*dis = (int)(dis/1000);
            return dis+"公里";*/
            double newDis = (double)dis/1000;
            //BigDecimal a = new BigDecimal(newDis).setScale(0,BigDecimal.ROUND_HALF_UP); 四舍五入
            BigDecimal a = new BigDecimal(newDis);
            newDis = a.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
            return newDis+"公里";
        }else{
            return dis+"米";
        }
    }
    private boolean initDirs() {
        mSDCardPath = getSdcardDir();
        if (mSDCardPath == null) {
            return false;
        }
        File f = new File(mSDCardPath, APP_FOLDER_NAME);
        if (!f.exists()) {
            try {
                f.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private void initLocationNav() {
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (mLocationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission
                    .ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat
                    .checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "没有权限", Toast.LENGTH_SHORT).show();
                return;
            }
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    1000, 1000, mLocationListener);
        }
    }

    private String getSdcardDir() {
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().toString();
        }
        return null;
    }

//    private void initNavi() {
//        if (BaiduNaviManagerFactory.getBaiduNaviManager().isInited()) {
//
//            return;
//        }
//
//        BaiduNaviManagerFactory.getBaiduNaviManager().init(this,
//                mSDCardPath, APP_FOLDER_NAME, new IBaiduNaviManager.INaviInitListener() {
//
//                    @Override
//                    public void onAuthResult(int status, String msg) {
//                        String result;
//                        if (0 == status) {
//                            result = "key校验成功!";
//                        } else {
//                            result = "key校验失败, " + msg;
//                        }
//                        Toast.makeText(MainActivity.this, result, Toast.LENGTH_LONG).show();
//                    }
//
//                    @Override
//                    public void initStart() {
//                        Toast.makeText(MainActivity.this.getApplicationContext(),
//                                "百度导航引擎初始化开始", Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void initSuccess() {
//                        Toast.makeText(MainActivity.this.getApplicationContext(),
//                                "百度导航引擎初始化成功", Toast.LENGTH_SHORT).show();
//                        // 初始化tts
//                        //initTTS();
//                    }
//
//                    @Override
//                    public void initFailed(int errCode) {
//                        Toast.makeText(MainActivity.this.getApplicationContext(),
//                                "百度导航引擎初始化失败 " + errCode, Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }

    private void routePlanToNavi(BNRoutePlanNode sNode, BNRoutePlanNode eNode) {
        List<BNRoutePlanNode> list = new ArrayList<>();
        list.add(sNode);
        list.add(eNode);

        BaiduNaviManagerFactory.getRoutePlanManager().routeplanToNavi(
                list,
                IBNRoutePlanManager.RoutePlanPreference.ROUTE_PLAN_PREFERENCE_DEFAULT,
                null,
                new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        switch (msg.what) {
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_START:
                                Toast.makeText(MainActivity.this.getApplicationContext(),
                                        "算路开始", Toast.LENGTH_SHORT).show();
                                break;
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_SUCCESS:
                                Toast.makeText(MainActivity.this.getApplicationContext(),
                                        "算路成功", Toast.LENGTH_SHORT).show();
                                // 躲避限行消息
                                Bundle infoBundle = (Bundle) msg.obj;
                                if (infoBundle != null) {
                                    String info = infoBundle.getString(
                                            BNaviCommonParams.BNRouteInfoKey.TRAFFIC_LIMIT_INFO
                                    );
                                    Log.d("OnSdkDemo", "info = " + info);
                                }
                                break;
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_FAILED:
                                Toast.makeText(MainActivity.this.getApplicationContext(),
                                        "算路失败", Toast.LENGTH_SHORT).show();
                                break;
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_TO_NAVI:
                                Toast.makeText(MainActivity.this.getApplicationContext(),
                                        "算路成功准备进入导航", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(MainActivity.this,
                                        DemoGuideActivity.class);

                                startActivity(intent);
                                break;
                            default:
                                // nothing
                                break;
                        }
                    }
                });
    }



}
