package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.sql.Connection;
import java.sql.DriverManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LatLng userLocation;
    private Marker userMarker;
    private boolean isUserMarkerCreated = false;
    private final List<Marker> restaurantMarkers = new ArrayList<>();
    private boolean isMapFollowingUser = true;
    private static final float DEFAULT_ZOOM = 15f;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private SQLite dbHelper;

    private double user_lat;
    private double user_lng;
    private int userId;
    private int r_id;
    private int likeCount;
    private int unlikeCount;

    private Map<Integer, Integer> likeCounts = new HashMap<>();
    private Map<Integer, Integer> unlikeCounts = new HashMap<>();
    private int[] userData;
    private String userName;
    private AtomicInteger userAtomic;
    String mysql_ip = "db4free.net";
    int mysql_port = 3306; // Port 預設為 3306
    String db_name = "sqlsql";
    String url = "jdbc:mysql://" + mysql_ip + ":" + mysql_port + "/" + db_name;
    private final String dbUser = "a1103353";
    private final String dbPassword = "l20200103";

    private List<RestaurantData> restaurantList = new ArrayList<>();

    private boolean isShowingRestaurantInfo = false;
    private View customInfoWindowView = null;
    private LayoutInflater inflater;
    private Function function;

    private boolean dataLoaded = false;
    private final Object dataLock = new Object();

    public Dialog currentDialog;
    private static final int REQUEST_CODE_RES_INFO = 1001;

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private final CountDownLatch dataLatch = new CountDownLatch(1);
    private LatLng selectedRestaurantLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.googleMap);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        preloadData();
        dbHelper = new SQLite(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        userId = prefs.getInt("userId", -1);
        Log.e("userId", String.valueOf(userId));

        if (userId == -1) {
            createNewUser();
        } else {
            loadUserData();
        }

//        loadUserDataFromLocalDb();

        inflater = LayoutInflater.from(this);

        ImageButton back_user = findViewById(R.id.back_user);
        back_user.setOnClickListener(view -> {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
            isMapFollowingUser = true;
            isShowingRestaurantInfo = false;
        });

        setupNavigationButtons();

        // 找到 buttonContainer
        LinearLayout buttonContainer = findViewById(R.id.buttonContainer);

        // 直接在 buttonContainer 中生成標籤按鈕
        generateTagButtons(buttonContainer);

        scheduleSignStatusReset();
    }

    private void scheduleSignStatusReset() {
        WorkManager workManager = WorkManager.getInstance(this);

        // 計算距離下一個午夜的延遲時間
        Calendar midnight = Calendar.getInstance();
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 0);
        midnight.add(Calendar.DAY_OF_YEAR, 1);

        long delayInMillis = midnight.getTimeInMillis() - System.currentTimeMillis();

        // 創建一個在指定時間執行的一次性工作請求
        OneTimeWorkRequest resetWorkRequest = new OneTimeWorkRequest.Builder(ResetSignStatusWorker.class)
                .setInitialDelay(delayInMillis, TimeUnit.MILLISECONDS)
                .addTag("resetSignStatus")
                .build();

        // 設置工作請求
        workManager.enqueueUniqueWork(
                "resetSignStatus",
                ExistingWorkPolicy.REPLACE,
                resetWorkRequest);

        // 設置每日重複
        workManager.getWorkInfosByTagLiveData("resetSignStatus")
                .observeForever(workInfos -> {
                    if (workInfos != null && !workInfos.isEmpty()) {
                        WorkInfo workInfo = workInfos.get(0);
                        if (workInfo.getState().isFinished()) {
                            scheduleSignStatusReset(); // 重新調度下一天的任務
                        }
                    }
                });
    }

    private void generateTagButtons(LinearLayout buttonContainer) {
        List<SQLite.AllTag> tagList = getTopTags();

        for (SQLite.AllTag tag : tagList) {
            Button button = new Button(this);
            button.setText(tag.getTag_name());
            button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

            // 动态获取图标资源
            String iconResourceName = "category_" + tag.getTag_id();
            int iconResourceId = getResources().getIdentifier(iconResourceName, "drawable", getPackageName());

            // 如果找到对应的图标，则设置图标
            if (iconResourceId != 0) {
                Drawable icon = ContextCompat.getDrawable(this, iconResourceId);
                // 调整图标大小（可选）
                if (icon != null) {
                    icon.setBounds(0, 0, dpToPx(24), dpToPx(24)); // 设置图标大小为24dp
                    button.setCompoundDrawables(icon, null, null, null); // 设置左侧图标
                }
            }

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, dpToPx(4), 0); // 设置右边距为 4dp
            button.setLayoutParams(params);

            // 设置图标和文字的间距（可选）
            button.setCompoundDrawablePadding(dpToPx(8));

            button.setOnClickListener(view -> {
                Intent intent = new Intent(MainActivity.this, result.class);
                intent.putExtra("tag_id", tag.getTag_id());
                intent.putExtra("button_text", tag.getTag_name());
                intent.putExtra("clickNum", tag.getClickNum());
                intent.putExtra("user_lat", user_lat);
                intent.putExtra("user_lng", user_lng);
                startActivity(intent);
            });

            buttonContainer.addView(button);
        }
    }

    private List<SQLite.AllTag> getTopTags() {
        List<SQLite.AllTag> tagList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT tag_id, tag_name, click_num, tag_picture FROM tag ORDER BY click_num DESC LIMIT 10";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") int tag_id = cursor.getInt(cursor.getColumnIndex("tag_id"));
                @SuppressLint("Range") String tag_name = cursor.getString(cursor.getColumnIndex("tag_name"));
                @SuppressLint("Range") int clickNum = cursor.getInt(cursor.getColumnIndex("click_num"));
                @SuppressLint("Range") String tag_photo = cursor.getString(cursor.getColumnIndex("tag_picture"));
                tagList.add(new SQLite.AllTag(tag_id, tag_name, clickNum, tag_photo));
            } while (cursor.moveToNext());
        }
        cursor.close();

        // 按照 click_num 降序排序
        Collections.sort(tagList, new Comparator<SQLite.AllTag>() {
            @Override
            public int compare(SQLite.AllTag t1, SQLite.AllTag t2) {
                return Integer.compare(t2.getClickNum(), t1.getClickNum());
            }
        });

        return tagList;
    }

    // Method: 創建新用戶
    private void createNewUser() {
        MysqlCon con = new MysqlCon();
        executorService.submit(() -> {
            int newUserId = con.getMaxUserId() + 1;
            String userName;
            if(newUserId < 10){
                userName = "User0000" + newUserId;
            }else if(newUserId < 100){
                userName = "User000" + newUserId;
            }else if(newUserId < 1000){
                userName = "User00" + newUserId;
            }else if(newUserId < 10000){
                userName = "User0" + newUserId;
            }else{
                userName = "User" + newUserId;
            }

            long result = dbHelper.addUser(newUserId, userName, 1, 0, 0, 0);

            if (result != -1) {
                userId = newUserId;
                SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("userId", userId);
                editor.apply();

                dbHelper.addTree(userId, 0, 0);

                runOnUiThread(this::loadUserData);
            } else {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "創建新用戶失敗", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // Method: 加載用戶數據
    private void loadUserData() {
        if (dbHelper.isFirstLogin(userId)) {
            fetchDataFromRemoteAndSaveToLocal();
            Log.e("userId1", String.valueOf(userId));
        } else {
//            reloadDatabaseData();
            loadDataFromLocal();
//            fetchDataFromRemoteAndSaveToLocal();
            Log.e("userId", String.valueOf(userId));
        }
    }

    // 添加一個新的方法來重新載入資料
    private void reloadDatabaseData() {
        executorService.submit(() -> {
            try {
                // 建立資料庫連接
                Connection connection = DriverManager.getConnection(url, dbUser, dbPassword);
                res_SQL resSql = new res_SQL(connection);
                SQLite sqlite = new SQLite(MainActivity.this);

                // 重新載入資料
                sqlite.insertBusinessHours(resSql.getBusinessHours());
                sqlite.insertRestaurants(resSql.getRestaurants());
                sqlite.insertRestaurantTags(resSql.getRestaurantTags());
                sqlite.insertRestaurantGreenBehaviors(resSql.getRestaurantGreenBehaviors());
                sqlite.insertTags(resSql.getTags());


                // 關閉資料庫連接
                connection.close();

                // 在主線程更新UI
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "資料已重新載入", Toast.LENGTH_SHORT).show();
                    // 重新載入餐廳資料並更新地圖
                    loadRestaurantData();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Log.e("Main", "重新載入資料失敗: " + e.getMessage());
                });
            }
        });
    }

    // Method: 從資料庫獲取數據並保存到本地SQLite
    private void fetchDataFromRemoteAndSaveToLocal() {
        executorService.submit(() -> {
            try {
                Connection connection = DriverManager.getConnection(url, dbUser, dbPassword);
                res_SQL resSql = new res_SQL(connection);
                SQLite sqlite = new SQLite(MainActivity.this);

                sqlite.insertBusinessHours(resSql.getBusinessHours());
                sqlite.insertRestaurants(resSql.getRestaurants());
                sqlite.insertRestaurantTags(resSql.getRestaurantTags());
                sqlite.insertRestaurantGreenBehaviors(resSql.getRestaurantGreenBehaviors());
                sqlite.insertGreenBehaviors(resSql.getGreenBehaviors());
                sqlite.insertTags(resSql.getTags());

                dbHelper.setFirstLoginFlag(userId, false);

                MysqlCon mysqlCon = new MysqlCon();
                mysqlCon.run();
                MysqlCon.User[] users = mysqlCon.getUser(userId);
                if (users != null && users.length > 0) {
                    MysqlCon.User user = users[0];
                    dbHelper.updateUserData(userId, user.getUser_name(), user.getPhoto_id(), user.getSignday(), user.getDrop(), user.getSign_flag());
                }

                int[] treeData = mysqlCon.tree(userId);
                if (treeData != null && treeData.length >= 3) {
                    dbHelper.updateTreeData(treeData[0], treeData[1], treeData[2]);
                }

                runOnUiThread(this::updateUI);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // Method: 從本地SQLite加載數據
    private void loadDataFromLocal() {
        int today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        if (today == Calendar.SUNDAY) {
            today = 7;
        } else {
            today -= 1;
        }
        userData = dbHelper.getUserData(userId);
        userName = dbHelper.getUserName(userId);
        restaurantList = dbHelper.getData(today);
        updateUI();
    }

    // Method: 更新UI，主要是刷新地圖上的標記
    private void updateUI() {
        if (mMap != null) {
            updateMapWithRestaurants();
        }
        dataLatch.countDown();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        }
        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startLocationUpdates();
            setInitialMapPosition();
        }
//
//        loadRestaurantData();

        mMap.setOnMapClickListener(latLng -> {
            isMapFollowingUser = false;
        });

        mMap.setOnMapClickListener(latLng -> closeCustomInfoWindow());

        mMap.setOnMarkerClickListener(marker -> {
            if (marker.getTag() instanceof RestaurantData) {
                RestaurantData restaurantData = (RestaurantData) marker.getTag();
                selectedRestaurantLocation = marker.getPosition();
                showCustomInfoWindow(restaurantData);
                mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                isMapFollowingUser = false;
            }
            return true;
        });

        mMap.setOnCameraMoveStartedListener(reason -> {
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                isMapFollowingUser = false;
            }
        });

        updateMapWithRestaurants();
    }

    // Method: 設定初始的地圖位置
    private void setInitialMapPosition() {
        if (userLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, DEFAULT_ZOOM));
        }
    }

//    @SuppressLint("MissingPermission")
//    private void startLocationUpdates() {
//        LocationRequest locationRequest = LocationRequest.create();
//        locationRequest.setInterval(1);
//        locationRequest.setFastestInterval(1);
//        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//
//        locationCallback = new LocationCallback() {
//            @Override
//            public void onLocationResult(@NonNull LocationResult locationResult) {
//                for (Location location : locationResult.getLocations()) {
//                    updateUserLocation(location);
//                }
//            }
//        };
//
//        fusedLocationClient.requestLocationUpdates(locationRequest,
//                locationCallback,
//                Looper.getMainLooper());
//
//        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
//            if (location != null) {
//                updateUserLocation(location);
//                setInitialMapPosition();
//                if (!isUserMarkerCreated) {
//                    createUserMarker();
//                }
//            }
//        });
//    }
    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(1);
        locationRequest.setFastestInterval(1);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    updateUserLocation(location);

                    // 首次定位後載入餐廳資料
                    if (restaurantList == null || restaurantList.isEmpty()) {
                        loadRestaurantData();
                    }
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                updateUserLocation(location);
                setInitialMapPosition();

                // 確保在取得位置後立即載入餐廳資料
                loadRestaurantData();

                if (!isUserMarkerCreated) {
                    createUserMarker();
                }
            }
        });
    }


    private void updateUserLocation(Location location) {
        user_lat = location.getLatitude();
        user_lng = location.getLongitude();
        Log.e("LocationUpdate", "位置已更新：lat=" + user_lat + ", lng=" + user_lng);
        Log.e("DatabaseHelper", String.valueOf(user_lat) + " " + String.valueOf(user_lng));
//        //高雄
//        user_lat = 22.66971137122702;
//        user_lng = 120.30257585903212;
        //台北
//        user_lat = 25.033713512244482;
//        user_lng = 121.56476420109684;
        userLocation = new LatLng(user_lat, user_lng);

        if (userMarker != null) {
            userMarker.setPosition(userLocation);
        } else {
            createUserMarker();
        }

        if (isMapFollowingUser && !isShowingRestaurantInfo) {
            mMap.animateCamera(CameraUpdateFactory.newLatLng(userLocation));
        }
    }

    // Method: 創建用戶位置標記
    private void createUserMarker() {
        if (mMap != null && !isUserMarkerCreated) {
            BitmapDescriptor userIcon = resizeBitmapDescriptor(R.drawable.user_location);
            userMarker = mMap.addMarker(new MarkerOptions()
                    .position(userLocation)
                    .title("我的定位")
                    .icon(userIcon));

            if (userMarker != null) {
                userMarker.setTag("user_location");
                isUserMarkerCreated = true;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    // Method: 停止位置更新
    private void stopLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMap != null) {
            startLocationUpdates();
        }
    }

    // Method: 預載數據，從遠端獲取餐廳信息
    private void preloadData() {
        executorService.execute(() -> {
            MysqlCon con = new MysqlCon();
            con.run();

            if (!restaurantList.isEmpty()) {
                List<Integer> restaurantIds = new ArrayList<>();
                for (RestaurantData restaurant : restaurantList) {
                    restaurantIds.add(restaurant.getId());
                }

                Map<Integer, Integer> likes = con.getLikeCounts(restaurantIds);
                Map<Integer, Integer> unlikes = con.getUnlikeCounts(restaurantIds);

                for (RestaurantData restaurant : restaurantList) {
                    r_id = restaurant.getId();
                    likeCounts.put(r_id, likes.getOrDefault(r_id, 0));
                    unlikeCounts.put(r_id, unlikes.getOrDefault(r_id, 0));
                }
            } else {
                Log.e("Main", "restaurantList is empty");
            }

            synchronized (dataLock) {
                dataLoaded = true;
                dataLock.notifyAll();
            }
        });
    }



//    private void loadRestaurantData() {
//        Log.e("LocationUpdate", "準備載入餐廳資料：lat=" + user_lat + ", lng=" + user_lng);
//        executorService.submit(() -> {
//            int today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
//            if (today == Calendar.SUNDAY) {
//                today = 7;
//            } else {
//                today -= 1;
//            }
//
//            // 直接在 SQLite 查詢附近的餐廳
//            restaurantList = dbHelper.getNearbyData(today, user_lat, user_lng);
//            Log.e("DatabaseHelper1", String.valueOf(user_lat) + " " + String.valueOf(user_lng));
//
//            // 更新地圖上的餐廳標記
//            runOnUiThread(this::updateMapWithRestaurants);
//        });
//    }
    //    // Method: 加載餐廳數據並更新地圖標記
//    private void loadRestaurantData() {
//        executorService.submit(() -> {
//            int today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
//            if (today == Calendar.SUNDAY) {
//                today = 7;
//            } else {
//                today -= 1;
//            }
//            restaurantList = dbHelper.getData(today);
//            runOnUiThread(this::updateMapWithRestaurants);
//        });
//    }
    // 載入餐廳資料的方法
    private void loadRestaurantData() {
        executorService.submit(() -> {
            // 取得今日星期幾（調整星期日為7）
            int today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
            if (today == Calendar.SUNDAY) {
                today = 7;
            } else {
                today -= 1;
            }

            // 初始化附近餐廳清單
            List<RestaurantData> nearbyRestaurants = dbHelper.getNearbyData(today, user_lat, user_lng);
            // 取得今日所有餐廳資料
            List<RestaurantData> allRestaurants = dbHelper.getData(today);

            // 立即更新地圖上的附近餐廳
            restaurantList = nearbyRestaurants;
            runOnUiThread(this::updateMapWithRestaurants);

            // 若附近餐廳數量少於全部餐廳數量，則在背景載入完整清單
            if (nearbyRestaurants.size() < allRestaurants.size()) {
                executorService.submit(() -> {
                    // 模擬背景處理時間
                    try {
                        Thread.sleep(2000); // 等待2秒，模擬背景處理
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // 使用完整餐廳清單更新地圖
                    restaurantList = allRestaurants;
                    runOnUiThread(this::updateMapWithRestaurants);
                });
            }
        });
    }

    // Method: 更新地圖上顯示的餐廳標記
    private void updateMapWithRestaurants() {
        for (Marker marker : restaurantMarkers) {
            marker.remove();
        }
        restaurantMarkers.clear();

        BitmapDescriptor res = resizeBitmapDescriptor(R.drawable.res);
        BitmapDescriptor likeRes = resizeBitmapDescriptor(R.drawable.like_res);
        BitmapDescriptor unlikeRes = resizeBitmapDescriptor(R.drawable.unlike_res);

        for (RestaurantData restaurant : restaurantList) {
            LatLng location = new LatLng(restaurant.getLatitude(), restaurant.getLongitude());
            int state = dbHelper.getRestaurantState(userId, restaurant.getId());
            BitmapDescriptor icon = (state == 1) ? likeRes : (state == -1) ? unlikeRes : res;

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(location)
                    .title(restaurant.getName())
                    .icon(icon);
            Marker marker = mMap.addMarker(markerOptions);
            if (marker != null) {
                marker.setTag(restaurant);
                restaurantMarkers.add(marker);
            }
        }
    }

    // Method: 更新餐廳標記狀態並重新設置圖標
    private void updateRestaurantMarker(int restaurantId, int state) {
        for (Marker marker : restaurantMarkers) {
            if (marker.getTag() instanceof RestaurantData) {
                RestaurantData restaurantData = (RestaurantData) marker.getTag();
                if (restaurantData.getId() == restaurantId) {
                    BitmapDescriptor newIcon;
                    if (state == 1) {
                        newIcon = resizeBitmapDescriptor(R.drawable.like_res);
                    } else if (state == -1) {
                        newIcon = resizeBitmapDescriptor(R.drawable.unlike_res);
                    } else {
                        newIcon = resizeBitmapDescriptor(R.drawable.res);
                    }
                    marker.setIcon(newIcon);

                    restaurantData.setState(state);
                    dbHelper.updateRestaurantState(restaurantData.getId(), state);

                    break;
                }
            }
        }
    }

    // Method: 關閉自訂信息窗口
    private void closeCustomInfoWindow() {
        Log.d("MainActivity", "customInfoWindowView is null: " + (customInfoWindowView == null));
        if (customInfoWindowView != null) {
            ((ViewGroup) customInfoWindowView.getParent()).removeView(customInfoWindowView);
            customInfoWindowView = null;
        }
    }

    // Method: 顯示自訂信息窗口
    @SuppressLint("InflateParams")
    private void showCustomInfoWindow(RestaurantData restaurantData) {
        if (customInfoWindowView == null) {
            customInfoWindowView = inflater.inflate(R.layout.custom_info_window, null);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );
            params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            params.bottomMargin = dpToPx(70);
            addContentView(customInfoWindowView, params);
        }

        Function function = new Function();

        selectedRestaurantLocation = new LatLng(restaurantData.getLatitude(), restaurantData.getLongitude());
        isShowingRestaurantInfo = true;

        String logo = "logo_" + restaurantData.getId();
        int logoResourceId = getResources().getIdentifier(logo, "drawable", getPackageName());

        ImageView res_logo = customInfoWindowView.findViewById(R.id.res_logo);
        if (res_logo != null) {
            if (logoResourceId != 0) {
                Drawable logoDrawable = getResources().getDrawable(logoResourceId);
                Drawable circleDrawable = circle(logoDrawable);
                res_logo.setImageDrawable(circleDrawable);
            } else {
                // 如果找不到對應的資源，可以設置一個默認的圖片或者處理錯誤
                Log.e("MainActivity", "Logo resource not found: " + logo);
                res_logo.setImageResource(R.drawable.logonull); // 設置默認logo
            }
        } else {
            Log.e("MainActivity", "res_logo is null");
        }

        TextView title = customInfoWindowView.findViewById(R.id.name);
        TextView openingHours = customInfoWindowView.findViewById(R.id.opening_hours);
        TextView distance = customInfoWindowView.findViewById(R.id.distance);
        Button more = customInfoWindowView.findViewById(R.id.more);
        title.setText(restaurantData.getName());
        openingHours.setText(restaurantData.getOpenTime().replace(",", ",\n"));
        int distanceCal = (int) Math.round(function.CalculateDistance(restaurantData.getLatitude(), restaurantData.getLongitude(), user_lat, user_lng));
        Log.e("1234", String.valueOf(user_lat) + " " + String.valueOf(user_lng));
        SpannableString ss;
        if (distanceCal <= 1000) {
            ss = new SpannableString(distanceCal + " m");
        } else {
            double dis = Math.round(distanceCal / 100.0) / 10.0;
            ss = new SpannableString(dis + " km");
        }
        distance.setText(ss);

        r_id = restaurantData.getId();
        likeCount = likeCounts.getOrDefault(r_id, 0);
        unlikeCount = unlikeCounts.getOrDefault(r_id, 0);

        Log.e("1234", String.format("Restaurant: %s (ID: %d), Likes: %d, Unlikes: %d",
                restaurantData.getName(), r_id, likeCount, unlikeCount));

        more.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, res_info.class);
            intent.putExtra("restaurant_data", restaurantData);
            intent.putExtra("userId", userId);
            intent.putExtra("likeCount", likeCount);
            intent.putExtra("unlikeCount", unlikeCount);
            intent.putExtra("user_lat", user_lat);
            intent.putExtra("user_lng", user_lng);
            startActivityForResult(intent, REQUEST_CODE_RES_INFO);
        });

        ImageButton checkIn = customInfoWindowView.findViewById(R.id.checkin);
        checkIn.setOnClickListener(view -> {
            String currentTime = function.getCurrentTime();
            int dayOfWeek = function.getDayofWeek(currentTime);
            int userMayChange = userData[3];
            userAtomic = new AtomicInteger(userMayChange);
            Log.v("currentTime", currentTime);

            new Thread(() -> {
                SQLite sqlite = new SQLite(MainActivity.this);
                String[][] businessTime = sqlite.getBusinessTime(r_id, dayOfWeek);
                String todayOpenTime = businessTime[6][1];
                int isOpen = function.isOpen(todayOpenTime, currentTime);
                Log.v("isOpen", String.valueOf(isOpen));
                if (isOpen == 1) {
                    double[] location;
                    location = dbHelper.getRLocation(r_id);
                    Log.e("location", "" + location[0]);
                    Log.e("location", "" + location[1]);
                    Log.e("location", "" + user_lat);
                    Log.e("location", "" + user_lng);
                    double distance1 = function.CalculateDistance(location[0], location[1], user_lat, user_lng);
                    System.out.println(distance1);
                    int countResult;

                    if (distance1 <= 200000) {
                        countResult = dbHelper.checkInCount(userId, r_id);
                        final String lastTime = countResult >= 1 ? dbHelper.getTime(userId, r_id, countResult)[0] : null;
                        final int isTimeOK = countResult >= 1 ? function.checkCD(lastTime, currentTime) : 1;

                        runOnUiThread(() -> {
                            if (isTimeOK == 1) {
                                function.showDialog(MainActivity.this, userId, userAtomic, countResult, isTimeOK, lastTime, businessTime, currentTime,
                                        (newDropNum, count) -> handleDialogResult(newDropNum, r_id, likeCount, unlikeCount));
                            } else {
                                function.showDialog(MainActivity.this, userId, userAtomic, -1, 0, lastTime, businessTime, currentTime, null);
                            }
                        });
                    } else {
                        runOnUiThread(() -> function.showDialog(MainActivity.this, userId, userAtomic, -1, -2, null, businessTime, currentTime, null));
                    }
                } else {
                    runOnUiThread(() -> function.showDialog(MainActivity.this, userId, userAtomic, -1, -1, null, businessTime, currentTime, null));
                }
            }).start();
        });
    }

    // Method: 處理對話框結果
    private void handleDialogResult(int newDropNum, int r_id, int likeCount, int unlikeCount) {
        if (userData != null && userData.length > 3) {
            userData[3] = newDropNum;
            dbHelper.updateUserData(userId, userName, userData[1], userData[2], newDropNum, userData[4]);
        }

        String currentTime = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault()).format(new Date());

        int checkInCount = dbHelper.checkInCount(userId, r_id);

        if (checkInCount == 0) {
            int maxCheckInId = dbHelper.getMaxCheckInId();
            int newCheckInId = maxCheckInId + 1;

            dbHelper.insertCheckIn(newCheckInId, userId, r_id, currentTime, 1, 1);
        } else {
            dbHelper.updateCheckIn(userId, r_id, currentTime, checkInCount + 1, 0);
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra("newDropNum", newDropNum);
        resultIntent.putExtra("restaurantId", r_id);
        resultIntent.putExtra("likeCount", likeCount);
        resultIntent.putExtra("unlikeCount", unlikeCount);
        setResult(RESULT_OK, resultIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_RES_INFO && resultCode == RESULT_OK && data != null) {
            int updatedRestaurantId = data.getIntExtra("restaurantId", -1);
            int updatedState = data.getIntExtra("state", 0);

            // 更新餐廳標記的狀態
            updateRestaurantMarker(updatedRestaurantId, updatedState);

            // 檢查返回的經緯度數據，並將地圖聚焦到餐廳標記位置
            if (data.hasExtra("latitude") && data.hasExtra("longitude")) {
                double latitude = data.getDoubleExtra("latitude", user_lat);
                double longitude = data.getDoubleExtra("longitude", user_lng);

                // 打印 latitude 和 longitude 的值
                Log.d("1234", "Latitude: " + latitude + ", Longitude: " + longitude);

                LatLng restaurantLocation = new LatLng(latitude, longitude);
                new Handler(Looper.getMainLooper()).postDelayed(() -> mMap.animateCamera(CameraUpdateFactory.newLatLng(restaurantLocation)), 60);  // 延遲聚焦到餐廳標記位置
                isMapFollowingUser = false;
                isShowingRestaurantInfo = true;
            } else {
                Log.e("12", "onActivityResult: Latitude and Longitude not found");
            }
        }
    }



    // Method: 設置導航按鈕的點擊事件
    private void setupNavigationButtons() {
        ImageButton[] buttons = {
                findViewById(R.id.allTag),
                findViewById(R.id.list),
                findViewById(R.id.tree),
                findViewById(R.id.user)
        };
        Class<?>[] activities = {Tag.class, list.class, Tree.class, user.class};

        for (int i = 0; i < buttons.length; i++) {
            final Class<?> activityClass = activities[i];
            buttons[i].setOnClickListener(view -> navigateToActivity(activityClass));
        }
    }

    // Method: 導航到指定的 Activity
    private void navigateToActivity(Class<?> activityClass) {
        executorService.submit(() -> {
            try {
                dataLatch.await();
                runOnUiThread(() -> {
                    Intent intent = new Intent(MainActivity.this, activityClass);
                    intent.putExtra("userId", userId);
                    intent.putExtra("user_lat", user_lat);
                    intent.putExtra("user_lng", user_lng);
                    startActivity(intent);
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    // Method: 將 dp 值轉換為 px 值
    public int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    // Method: 調整 Bitmap 大小並返回 BitmapDescriptor
    private BitmapDescriptor resizeBitmapDescriptor(int resourceId) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), resourceId);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, (int) (imageBitmap.getWidth() * (float) 0.5), (int) (imageBitmap.getHeight() * (float) 0.5), false);
        return BitmapDescriptorFactory.fromBitmap(resizedBitmap);
    }

    // Method: 將 Drawable 圖片裁剪為圓形
    public Drawable circle(Drawable drawable) {
        int borderWidth = 2;
        Bitmap originalBitmap = ((BitmapDrawable) drawable).getBitmap();
        Bitmap circleBitmap = Bitmap.createBitmap(originalBitmap.getWidth(), originalBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(circleBitmap);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(new BitmapShader(originalBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

        int radius = Math.min(originalBitmap.getWidth(), originalBitmap.getHeight()) / 2;

        Paint borderPaint = new Paint();
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(borderWidth);
        borderPaint.setColor(Color.BLACK);

        canvas.drawCircle((float) originalBitmap.getWidth() / 2, (float) originalBitmap.getHeight() / 2, radius, paint);

        canvas.drawCircle((float) originalBitmap.getWidth() / 2, (float) originalBitmap.getHeight() / 2, radius - (float) borderWidth / 2, borderPaint);

        return new BitmapDrawable(getResources(), circleBitmap);
    }
}
