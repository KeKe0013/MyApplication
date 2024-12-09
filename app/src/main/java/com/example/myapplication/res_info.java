package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.text.Html;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class res_info extends AppCompatActivity {

    private int userId;
    private int state, likeCount, unlikeCount;
    private int restaurantId;
    private double user_lat;
    private double user_lng;
    private int[] userData;
    private static SQLite dbHelper;
    private String userName;
    private final String url = "jdbc:mysql://db4free.net:3306/sqlsql";
    private final String dbUser = "a1103353";
    private final String dbPassword = "l20200103";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_res_base);

        dbHelper = new SQLite(this);

        RestaurantData restaurantData = (RestaurantData) getIntent().getSerializableExtra("restaurant_data");
        userId = getIntent().getIntExtra("userId", 0);
        assert restaurantData != null;
        state = dbHelper.getRestaurantState(userId, restaurantData.getId());
        user_lat = getIntent().getDoubleExtra("user_lat", 0);
        user_lng = getIntent().getDoubleExtra("user_lng", 0);
        likeCount = getIntent().getIntExtra("likeCount", 0);
        unlikeCount = getIntent().getIntExtra("unlikeCount", 0);
        restaurantId = restaurantData.getId();

        // 從SQLite獲取userData
        userData = dbHelper.getUserData(userId);
        userName = dbHelper.getUserName(userId);

        updateRestaurantInfo();
        fetchRestaurantTags(restaurantData.getId());

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        ResInfoFragment resInfoFragment = new ResInfoFragment();
        fragmentTransaction.add(R.id.fragment_container, resInfoFragment);
        fragmentTransaction.commit();

        ImageButton buttonBack = findViewById(R.id.back);
        buttonBack.setOnClickListener(view -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("restaurantId", restaurantId);
            resultIntent.putExtra("state", state);  // 傳回當前狀態
            resultIntent.putExtra("latitude", restaurantData.getLatitude());
            resultIntent.putExtra("longitude", restaurantData.getLongitude());
            resultIntent.putExtra("likeCount", likeCount);
            resultIntent.putExtra("unlikeCount", unlikeCount);
            setResult(RESULT_OK, resultIntent);
            finish();
        });



        Button resInfo = findViewById(R.id.res_info);
        resInfo.setSelected(true);

        Button resMenu = findViewById(R.id.res_menu);
        resMenu.setSelected(false);

        resInfo.setOnClickListener(view -> {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragment_container, resInfoFragment);
            transaction.addToBackStack(null);
            transaction.commit();

            resInfo.setSelected(true);
            resMenu.setSelected(false);
        });

        resMenu.setOnClickListener(view -> {
            ResMenuFragment resMenuFragment = new ResMenuFragment();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragment_container, resMenuFragment);
            transaction.addToBackStack(null);
            transaction.commit();

            resInfo.setSelected(false);
            resMenu.setSelected(true);
        });
    }

    private void updateRestaurantInfo() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("restaurant_data")) {
            RestaurantData restaurantData = (RestaurantData) intent.getSerializableExtra("restaurant_data");

            // 將餐廳基本資訊顯示在相應的視圖中
            TextView resNameTextView = findViewById(R.id.res_name);
            assert restaurantData != null;
            resNameTextView.setText(restaurantData.getName());

            TextView like_num = findViewById(R.id.like_num);
            like_num.setText(String.valueOf(likeCount));
            TextView unlike_num = findViewById(R.id.unlike_num);
            unlike_num.setText(String.valueOf(unlikeCount));
            ImageButton like = findViewById(R.id.like);
            ImageButton unlike = findViewById(R.id.unlike);
            updateUI();

            // 當點擊「喜歡」按鈕時的邏輯
            like.setOnClickListener(v -> {
                int previousState = state; // 保存之前的狀態
                if (state == 1) {
                    // 如果目前狀態是「已喜歡」，再次點擊將狀態設置為未選擇
                    state = 0;
                    likeCount--; // 喜歡數量減一
                } else {
                    // 如果目前狀態是「未選擇」或「不喜歡」
                    if (state == -1) {
                        unlikeCount--; // 如果之前是不喜歡，則不喜歡數量減一
                    }
                    state = 1; // 將狀態設置為「喜歡」
                    likeCount++; // 喜歡數量加一
                }
                updateUI(); // 更新UI

                // 如果狀態發生變化，更新本地和遠端數據庫
                if (state != previousState) {
                    updateDatabase(userId, restaurantId, state);
                }
            });


            // 當點擊「不喜歡」按鈕時的邏輯
            unlike.setOnClickListener(v -> {
                int previousState = state; // 保存之前的狀態
                if (state == -1) {
                    // 如果目前狀態是「已不喜歡」，再次點擊將狀態設置為未選擇
                    state = 0;
                    unlikeCount--; // 不喜歡數量減一
                } else {
                    // 如果目前狀態是「未選擇」或「喜歡」
                    if (state == 1) {
                        likeCount--; // 如果之前是喜歡，則喜歡數量減一
                    }
                    state = -1; // 將狀態設置為「不喜歡」
                    unlikeCount++; // 不喜歡數量加一
                }
                updateUI(); // 更新UI

                // 如果狀態發生變化，更新本地和遠端數據庫
                if (state != previousState) {
                    updateDatabase(userId, restaurantId, state);
                }
            });

            String logo = "logo_"+restaurantData.getId();

            @SuppressLint("DiscouragedApi") int logoResourceId = getResources().getIdentifier(logo, "drawable", getPackageName());
            Drawable logoDrawable;
            if (logoResourceId != 0) {
                try {
                    logoDrawable = getResources().getDrawable(logoResourceId);
                } catch (Resources.NotFoundException e) {
                    // 使用默認logo
                    logoDrawable = getResources().getDrawable(R.drawable.logonull);
                }
            } else {
                Log.w("RestaurantList", "Logo resource not found: " + logo);
                // 使用默認logo
                logoDrawable = getResources().getDrawable(R.drawable.logonull);
            }
            Drawable circleDrawable = circle(logoDrawable);

            // 获取到 ImageView
            ImageView res_logo = findViewById(R.id.res_logo);
            if (res_logo != null) {
                // 设置获取到的 Drawable 到 ImageView
                res_logo.setImageDrawable(circleDrawable);
            } else {
                Log.e("res_info", "res_logo is null");
            }

            int r_id = restaurantData.getId();

            Function function = new Function();
            ImageButton checkin = findViewById(R.id.checkin);
            checkin.setOnClickListener(view -> {
                String currentTime = function.getCurrentTime();
                Log.e("Time1", currentTime);
                int dayOfWeek = function.getDayofWeek(currentTime);
                Log.e("Time2", String.valueOf(dayOfWeek));
                int userMayChange = userData[3]; // drop
                AtomicInteger userAtomic = new AtomicInteger(userMayChange);
                new Thread(() -> {
                    SQLite dbHelper = new SQLite(res_info.this);
                    String[][] businessTime = dbHelper.getBusinessTime(r_id, dayOfWeek);
                    String todayOpenTime = businessTime[6][1];
                    Log.e("Time3", todayOpenTime);
                    int isOpen = function.isOpen(todayOpenTime, currentTime);
                    Log.v("isOpen", String.valueOf(isOpen));
                    if (isOpen == 1) {
                        double[] location;
                        location = dbHelper.getRLocation(r_id);
                        double distance = function.CalculateDistance(location[0], location[1], user_lat, user_lng);
                        Log.e("location", String.valueOf(location[0]));
                        Log.e("location", String.valueOf(location[1]));
                        Log.e("location", String.valueOf(user_lat));
                        Log.e("location", String.valueOf(user_lng));
                        System.out.println(distance);
                        int countResult = dbHelper.checkInCount(userId, r_id);

                        if (distance <= 200000) {
                            final String lastTime = countResult >= 1 ? dbHelper.getTime(userId, r_id, countResult)[0] : null;
                            final int isTimeOK = countResult >= 1 ? function.checkCD(lastTime, currentTime) : 1;

                            runOnUiThread(() -> {
                                if (isTimeOK == 1) {
                                    // 如果冷卻結束，顯示對話框並調用 handleDialogResult
                                    function.showDialog(res_info.this, userId, userAtomic, countResult, isTimeOK, lastTime, businessTime, currentTime,
                                            (newDropNum, count) -> handleDialogResult(newDropNum, r_id, state, likeCount, unlikeCount));
                                } else {
                                    // 冷卻未結束，只顯示對話框，不調用 handleDialogResult
                                    function.showDialog(res_info.this, userId, userAtomic, countResult, 0, lastTime, businessTime, currentTime, null);
                                }
                            });
                        } else {
                            // 距離過遠，只顯示對話框，不調用 handleDialogResult
                            runOnUiThread(() -> function.showDialog(res_info.this, userId, userAtomic, 0, -2, null, businessTime, currentTime, null));
                        }
                    } else {
                        // 餐廳未營業，只顯示對話框，不調用 handleDialogResult
                        runOnUiThread(() -> function.showDialog(res_info.this, userId, userAtomic, 0, -1, null, businessTime, currentTime, null));
                    }

                }).start();
            });
        }
    }

    // 新增這個方法來統一處理對話框的結果
    private void handleDialogResult(int newDropNum, int r_id, int state, int likeCount, int unlikeCount) {
        // 更新 userData 中的 drop 數量
        if (userData != null && userData.length > 3) {
            userData[3] = newDropNum;
            // 更新SQLite中的數據
            dbHelper.updateUserData(userId, userName, userData[1], userData[2], newDropNum, userData[4]);
        }

        // 獲取當前時間
        String currentTime = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // 檢查是否已經打卡過該餐廳
        int checkInCount = dbHelper.checkInCount(userId, r_id);

        if (checkInCount == 0) { // 第一次打卡
            // 獲取最大 checkIn_id
            int maxCheckInId = dbHelper.getMaxCheckInId();
            int newCheckInId = maxCheckInId + 1;

            // 插入新的打卡記錄
            dbHelper.insertCheckIn(newCheckInId, userId, r_id, currentTime, 1, 1); // first_flag 設為 1
        } else { // 已經打卡過，更新記錄
            // 更新打卡時間和次數
            dbHelper.updateCheckIn(userId, r_id, currentTime, checkInCount + 1, 0); // first_flag 設為 0
        }

        // 創建返回到 MainActivity 的 Intent
        Intent resultIntent = new Intent();
        resultIntent.putExtra("newDropNum", newDropNum);
        resultIntent.putExtra("restaurantId", r_id);
        resultIntent.putExtra("state", state);
        resultIntent.putExtra("likeCount", likeCount);
        resultIntent.putExtra("unlikeCount", unlikeCount);
        setResult(RESULT_OK, resultIntent);
    }


    // 更新UI函數
    void updateUI() {
        ImageButton like = findViewById(R.id.like);
        ImageButton unlike = findViewById(R.id.unlike);
        TextView like_num = findViewById(R.id.like_num);
        TextView unlike_num = findViewById(R.id.unlike_num);

        if (state == 1) {
            // 如果狀態是「喜歡」，設置相應的按鈕圖標
            like.setImageResource(R.drawable.heart);
            unlike.setImageResource(R.drawable.unlike);
        } else if (state == -1) {
            // 如果狀態是「不喜歡」，設置相應的按鈕圖標
            unlike.setImageResource(R.drawable.black_heart);
            like.setImageResource(R.drawable.like);
        } else {
            // 如果狀態是未選擇，設置相應的按鈕圖標
            like.setImageResource(R.drawable.like);
            unlike.setImageResource(R.drawable.unlike);
        }
        // 更新喜歡和不喜歡的數量顯示
        like_num.setText(String.valueOf(likeCount));
        unlike_num.setText(String.valueOf(unlikeCount));
    }

    // 在活動結束時更新數據庫
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 更新數據庫
        updateDatabase(userId, restaurantId, state);
        // 關閉dbHelper
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    // 更新本地和遠端數據庫的函數
    void updateDatabase(int userId, int restaurantId, int state) {
        new Thread(() -> {
            try {
                // 建立遠端資料庫的連接
                Connection connection = DriverManager.getConnection(url, dbUser, dbPassword);

                // 首先檢查記錄是否存在
                String checkQuery = "SELECT state FROM list WHERE user_id = ? AND restaurant_id = ?";
                try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
                    checkStmt.setInt(1, userId);
                    checkStmt.setInt(2, restaurantId);
                    ResultSet rs = checkStmt.executeQuery();

                    if (rs.next()) {
                        // 如果記錄存在
                        int currentState = rs.getInt("state");
                        if (state == 0) {
                            // 如果新狀態為0，刪除記錄
                            String deleteQuery = "DELETE FROM list WHERE user_id = ? AND restaurant_id = ?";
                            try (PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery)) {
                                deleteStmt.setInt(1, userId);
                                deleteStmt.setInt(2, restaurantId);
                                deleteStmt.executeUpdate();
                                Log.d("updateDatabase", "Deleted record for user " + userId + " and restaurant " + restaurantId);
                            }
                        } else if (currentState != state) {
                            // 如果狀態不同，更新記錄
                            String updateQuery = "UPDATE list SET state = ? WHERE user_id = ? AND restaurant_id = ?";
                            try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                                updateStmt.setInt(1, state);
                                updateStmt.setInt(2, userId);
                                updateStmt.setInt(3, restaurantId);
                                updateStmt.executeUpdate();
                                Log.d("updateDatabase", "Updated state to " + state + " for user " + userId + " and restaurant " + restaurantId);
                            }
                        }
                    } else {
                        // 如果記錄不存在，且狀態不為0，插入新記錄
                        if (state != 0) {
                            String insertQuery = "INSERT INTO list (user_id, restaurant_id, state) VALUES (?, ?, ?)";
                            try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                                insertStmt.setInt(1, userId);
                                insertStmt.setInt(2, restaurantId);
                                insertStmt.setInt(3, state);
                                insertStmt.executeUpdate();
                                Log.d("updateDatabase", "Inserted new record with state " + state + " for user " + userId + " and restaurant " + restaurantId);
                            }
                        }
                    }
                }

                connection.close();
            } catch (SQLException e) {
                Log.e("updateDatabase", "Error updating database", e);
            }

            // 更新本地SQLite數據庫
            SQLite dbHelper = new SQLite(res_info.this);

            // 首先檢查記錄是否存在
            boolean recordExists = dbHelper.checkRecordExists(userId, restaurantId);

            if (recordExists) {
                // 如果記錄存在
                int currentState = dbHelper.getRecordState(userId, restaurantId);
                if (state == 0) {
                    // 如果新狀態為0，刪除記錄
                    dbHelper.deleteRecord(userId, restaurantId);
                    Log.d("updateDatabase", "Deleted record for user " + userId + " and restaurant " + restaurantId);
                } else if (currentState != state) {
                    // 如果狀態不同，更新記錄
                    dbHelper.updateRecordState(userId, restaurantId, state);
                    Log.d("updateDatabase", "Updated state to " + state + " for user " + userId + " and restaurant " + restaurantId);
                }
            } else {
                // 如果記錄不存在，且狀態不為0，插入新記錄
                if (state != 0) {
                    dbHelper.insertRecord(userId, restaurantId, state);
                    Log.d("updateDatabase", "Inserted new record with state " + state + " for user " + userId + " and restaurant " + restaurantId);
                }
            }

            dbHelper.close();
        }).start();
    }

    private void fetchRestaurantTags(int restaurantId) {
        // 使用新的線程從資料庫中獲取標籤數據
        new Thread(() -> {
            try {
                SQLite dbHelper = new SQLite(res_info.this);

                // 從 restaurant_tag 表中獲取 tag_id
                List<Integer> tagIds = dbHelper.getRestaurantTagIds(restaurantId);
                Log.d("fetchRestaurantTags", "Retrieved tag IDs: " + tagIds);

                // 從 Tag 表中獲取 T_name
                List<SQLite.AllTag> tags = dbHelper.getTags(tagIds);
                Log.d("fetchRestaurantTags", "Retrieved tags: " + tags);

                // 在主線程中更新視圖
                runOnUiThread(() -> updateTagViews(tags));
            } catch (Exception e) {
                Log.e("fetchRestaurantTags", "Error fetching tags", e);
            }
        }).start();
    }

    private void updateTagViews(List<SQLite.AllTag> tags) {
        ConstraintLayout tagContainer = findViewById(R.id.tag_container);
        tagContainer.removeAllViews();

        int previousButtonId = ConstraintSet.PARENT_ID;
        boolean isFirstTag = true;

        for (SQLite.AllTag tag : tags) {
            Pair<Button, TextView> views = createTagView(tagContainer, tag);
            Button button = views.first;
            TextView textView = views.second;

            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(tagContainer);

            // 設置按鈕的約束
            constraintSet.connect(button.getId(), ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID, ConstraintSet.TOP);
            constraintSet.connect(button.getId(), ConstraintSet.BOTTOM,
                    ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);

            if (isFirstTag) {
                // 第一個標籤靠左對齊，加上左邊距
                constraintSet.connect(button.getId(), ConstraintSet.START,
                        ConstraintSet.PARENT_ID, ConstraintSet.START, dpToPx(140));
                isFirstTag = false;
            } else {
                // 其他標籤與前一個標籤保持固定間距
                constraintSet.connect(button.getId(), ConstraintSet.START,
                        previousButtonId, ConstraintSet.END, dpToPx(10));
            }

            // 設置文本視圖的約束
            constraintSet.connect(textView.getId(), ConstraintSet.TOP, button.getId(), ConstraintSet.TOP);
            constraintSet.connect(textView.getId(), ConstraintSet.START, button.getId(), ConstraintSet.START);
            constraintSet.connect(textView.getId(), ConstraintSet.END, button.getId(), ConstraintSet.END);
            constraintSet.connect(textView.getId(), ConstraintSet.BOTTOM, button.getId(), ConstraintSet.BOTTOM);

            constraintSet.applyTo(tagContainer);
            previousButtonId = button.getId();
        }
    }

    private Pair<Button, TextView> createTagView(ConstraintLayout container, SQLite.AllTag tag) {
        int width = getWidthForTag(tag.getTag_name());

        Button button = new Button(this);
        ConstraintLayout.LayoutParams buttonParams = new ConstraintLayout.LayoutParams(
                width, dpToPx(20));
        button.setId(View.generateViewId());
        button.setLayoutParams(buttonParams);
        button.setBackgroundColor(Color.TRANSPARENT);
        button.setContentDescription(getString(R.string.jump_category1));

        TextView textView = new TextView(this);
        ConstraintLayout.LayoutParams textParams = new ConstraintLayout.LayoutParams(
                width, dpToPx(20));
        textView.setId(View.generateViewId());
        textView.setLayoutParams(textParams);
        textView.setBackground(ContextCompat.getDrawable(this, R.drawable.button_category));
        textView.setText(tag.getTag_name());
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView.setTextColor(ContextCompat.getColor(this, R.color.black));
        textView.setTextSize(13);
        textView.setTypeface(Typeface.DEFAULT_BOLD);

        button.setOnClickListener(view -> {
            Intent intent = new Intent(res_info.this, result.class);
            intent.putExtra("tag_id", tag.getTag_id());
            intent.putExtra("button_text", tag.getTag_name());
            intent.putExtra("clickNum", tag.getClickNum());
            intent.putExtra("restaurant_data", getIntent().getSerializableExtra("restaurant_data"));
            intent.putExtra("user_lat", getIntent().getSerializableExtra("user_lat"));
            intent.putExtra("user_lng", getIntent().getSerializableExtra("user_lng"));
            startActivity(intent);
        });

        container.addView(button);
        container.addView(textView);

        return new Pair<>(button, textView);
    }

    private int getWidthForTag(String tagName) {
        int length = tagName.length();
        if (length <= 2) {
            return dpToPx(50);
        } else if (length == 3) {
            return dpToPx(65);
        } else {
            return dpToPx(80);
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
//    private void updateTagViews(List<SQLite.AllTag> tags) {
//        ConstraintLayout tagContainer = findViewById(R.id.tag_container);
//        tagContainer.removeAllViews(); // 清除所有現有的視圖
//
//        float startBias = 0.4f;
//        for (SQLite.AllTag tag : tags) {
//            createTagView(tagContainer, tag, startBias);
//            startBias += 0.2f; // 增加水平偏移，您可以根據需要調整這個值
//        }
//    }
//
//    private void createTagView(ConstraintLayout container, SQLite.AllTag tag, float horizontalBias) {
//        int width = getWidthForTag(tag.getTag_name());
//
//        Button button = new Button(this);
//        ConstraintLayout.LayoutParams buttonParams = new ConstraintLayout.LayoutParams(
//                width, dpToPx(20));
//        button.setId(View.generateViewId());
//        button.setLayoutParams(buttonParams);
//        button.setBackgroundColor(Color.TRANSPARENT);
//        button.setContentDescription(getString(R.string.jump_category1));
//
//        TextView textView = new TextView(this);
//        ConstraintLayout.LayoutParams textParams = new ConstraintLayout.LayoutParams(
//                width, dpToPx(20));
//        textView.setId(View.generateViewId());
//        textView.setLayoutParams(textParams);
//        textView.setBackground(ContextCompat.getDrawable(this, R.drawable.button_category));
//        textView.setText(tag.getTag_name());
//        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
//        textView.setTextColor(ContextCompat.getColor(this, R.color.black));
//        textView.setTextSize(13);
//        textView.setTypeface(Typeface.DEFAULT_BOLD);
//
//        button.setOnClickListener(view -> {
//            Intent intent = new Intent(res_info.this, result.class);
//            intent.putExtra("tag_id", tag.getTag_id());
//            intent.putExtra("button_text", tag.getTag_name());
//            intent.putExtra("clickNum", tag.getClickNum());
//            intent.putExtra("restaurant_data", getIntent().getSerializableExtra("restaurant_data"));
//            intent.putExtra("user_lat", getIntent().getSerializableExtra("user_lat"));
//            intent.putExtra("user_lng", getIntent().getSerializableExtra("user_lng"));
//            startActivity(intent);
//        });
//
//        container.addView(button);
//        container.addView(textView);
//
//        ConstraintSet constraintSet = new ConstraintSet();
//        constraintSet.clone(container);
//
//        // 設置按鈕的約束
//        constraintSet.connect(button.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
//        constraintSet.connect(button.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
//        constraintSet.connect(button.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
//        constraintSet.setHorizontalBias(button.getId(), horizontalBias);
//
//        // 設置文本視圖的約束
//        constraintSet.connect(textView.getId(), ConstraintSet.TOP, button.getId(), ConstraintSet.TOP);
//        constraintSet.connect(textView.getId(), ConstraintSet.START, button.getId(), ConstraintSet.START);
//        constraintSet.connect(textView.getId(), ConstraintSet.END, button.getId(), ConstraintSet.END);
//        constraintSet.connect(textView.getId(), ConstraintSet.BOTTOM, button.getId(), ConstraintSet.BOTTOM);
//
//        constraintSet.applyTo(container);
//    }
//
//    private int getWidthForTag(String tagName) {
//        int length = tagName.length();
//        if (length <= 2) {
//            return dpToPx(50);
//        } else if (length == 3) {
//            return dpToPx(65);
//        } else {
//            return dpToPx(80);
//        }
//    }
//
//    private int dpToPx(int dp) {
//        return (int) (dp * getResources().getDisplayMetrics().density);
//    }

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
        borderPaint.setColor(Color.BLACK); // 設置外框顏色

        canvas.drawCircle((float) originalBitmap.getWidth() / 2, (float) originalBitmap.getHeight() / 2, radius, paint);

        // 在圖形外繪製外框
        canvas.drawCircle((float) originalBitmap.getWidth() / 2, (float) originalBitmap.getHeight() / 2, radius - (float) borderWidth / 2, borderPaint);

        return new BitmapDrawable(getResources(), circleBitmap);
    }

    public static class ResInfoFragment extends Fragment {
        private RestaurantData restaurantData;
        private Activity mActivity;
        Function function = new Function();
        private int userId;

        @Override
        public void onAttach(@NonNull Context context) {
            super.onAttach(context);
            if (context instanceof Activity) {
                mActivity = (Activity) context;
            }
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_res_info, container, false);

            // 獲取Intent並取出RestaurantData對象
            Intent intent = mActivity.getIntent();
            if (intent != null && intent.hasExtra("restaurant_data")) {
                restaurantData = (RestaurantData) intent.getSerializableExtra("restaurant_data");
                userId = intent.getIntExtra("userId", 0);
                Log.d("res_info", "userId: " + userId);

            }
            return rootView;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            // 初始化 TextView 對象
            TextView greenBehavior1TextView = view.findViewById(R.id.greenBehavior1);
            TextView greenBehavior2TextView = view.findViewById(R.id.greenBehavior2);
            TextView greenBehavior3TextView = view.findViewById(R.id.greenBehavior3);
            TextView greenBehavior4TextView = view.findViewById(R.id.greenBehavior4);
            TextView greenBehavior5TextView = view.findViewById(R.id.greenBehavior5);
            TextView greenBehavior6TextView = view.findViewById(R.id.greenBehavior6);
            TextView greenBehavior7TextView = view.findViewById(R.id.greenBehavior7);

            // 檢查 restaurantData 是否為 null
            if (restaurantData != null) {
                // 獲取子視圖並設置數據
                TextView resAddressTextView = view.findViewById(R.id.res_address);
                TextView resPhoneTextView = view.findViewById(R.id.res_phone);
                resPhoneTextView.setText(restaurantData.getPhone());

                String address = restaurantData.getAddress();

                // 獲取 TextView 的佈局參數
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) resAddressTextView.getLayoutParams();

                // 根據 address 的長度設置 margin
                if (address.length() <= 16) {
                    layoutParams.topMargin = dpToPx(18, mActivity);
                    layoutParams.bottomMargin = dpToPx(18, mActivity);
                } else {
                    layoutParams.topMargin = dpToPx(5, mActivity);
                    layoutParams.bottomMargin = dpToPx(5, mActivity);

                    // 如果地址超過 15 個字，則添加換行符
                    String firstLine = address.substring(0, 16);
                    String remaining = address.substring(16);
                    resAddressTextView.setText(firstLine + "\n" + remaining);
                }

                // 設定新的 layoutParams
                resAddressTextView.setLayoutParams(layoutParams);

                // 如果沒有超過 16 個字，則直接顯示地址
                if (address.length() <= 16) {
                    resAddressTextView.setText(address);
                }

                TextView resMondayTextView = view.findViewById(R.id.Monday);
                TextView resTuesdayTextView = view.findViewById(R.id.Tuesday);
                TextView resWednesdayTextView = view.findViewById(R.id.Wednesday);
                TextView resThursdayTextView = view.findViewById(R.id.Thursday);
                TextView resFridayTextView = view.findViewById(R.id.Friday);
                TextView resSaturdayTextView = view.findViewById(R.id.Saturday);
                TextView resSundayTextView = view.findViewById(R.id.Sunday);

                // 使用新的線程從資料庫中獲取營業時間數據
                new Thread(() -> {
                    SQLite dbHelper = new SQLite(mActivity);
                    List<SQLite.BusinessHours> businessHours = dbHelper.getBusinessHours(restaurantData.getId());

                    for (SQLite.BusinessHours hours : businessHours) {
                        String week = hours.getWeek();
                        String openTime = hours.getOpenTime();

                        // 在主線程中更新視圖
                        mActivity.runOnUiThread(() -> updateTextView(week, openTime, resMondayTextView, resTuesdayTextView, resWednesdayTextView, resThursdayTextView, resFridayTextView, resSaturdayTextView, resSundayTextView));
                    }
                }).start();

                // 調用 fetchRestaurantGreenBehavior
                fetchRestaurantGreenBehavior(restaurantData.getId(), greenBehavior1TextView, greenBehavior2TextView, greenBehavior3TextView, greenBehavior4TextView, greenBehavior5TextView, greenBehavior6TextView, greenBehavior7TextView);

                // 添加備忘錄功能
                ImageButton addMemo = view.findViewById(R.id.addMemo);
                addMemo.setOnClickListener(view1 -> {
                    Dialog dialog = new Dialog(mActivity);
                    dialog.setContentView(R.layout.input_dialog);
                    dialog.show();

                    EditText editText = dialog.findViewById(R.id.dialog_input);
                    final TextView counterTextView = dialog.findViewById(R.id.counter_text_view);
                    final int maxLength = 100;
                    function.totalWord(editText, counterTextView, maxLength);

                    Button saveButton = dialog.findViewById(R.id.saveBtn);
                    saveButton.setOnClickListener(v -> {
                        String memoText = editText.getText().toString().trim();

                        new Thread(() -> {
                            // 新增備忘錄到資料庫
                            dbHelper.addMemo(userId, restaurantData.getId(), function.getCurrentTime(), memoText);

                            // 從資料庫中獲取所有備忘錄
                            ArrayList<SQLite.Memo> allMemos = dbHelper.getMemo(userId, restaurantData.getId());

                            // 在主線程中更新 UI
                            mActivity.runOnUiThread(() -> {
                                if (allMemos != null && !allMemos.isEmpty()) {
                                    // 清空目前的備忘錄佈局，然後重新生成所有備忘錄
                                    LinearLayout memoLayout = mActivity.findViewById(R.id.memo_layout);
                                    memoLayout.removeAllViews();  // 清空佈局

                                    // 重新生成所有備忘錄
                                    addMemosToLayout(allMemos);
                                }
                                dialog.dismiss(); // 關閉對話框
                            });
                        }).start();
                    });

                    Button cancelBtn = dialog.findViewById(R.id.cancelBtn);
                    cancelBtn.setOnClickListener(view11 -> dialog.dismiss());
                });

                // 從資料庫中獲取並顯示現有的備忘錄
                new Thread(() -> {
                    ArrayList<SQLite.Memo> memoList = dbHelper.getMemo(userId, restaurantData.getId());
                    final TextView memoContent = view.findViewById(R.id.textView42);
                    if (memoList.isEmpty()) {
                        mActivity.runOnUiThread(() -> Log.v("memo", "沒有筆記"));
                    } else {
                        mActivity.runOnUiThread(() -> {
                            memoContent.setVisibility(View.GONE);
                            addMemosToLayout(memoList);
                        });
                    }
                }).start();

                // 添加 Google 地圖按鈕
                ImageButton mapButton = view.findViewById(R.id.googleMap);
                mapButton.setOnClickListener(v -> openGoogleMaps(restaurantData.getAddress()));

                // 添加電話按鈕
                ImageButton phoneButton = view.findViewById(R.id.phone);
                phoneButton.setOnClickListener(v -> callPhoneNumber(restaurantData.getPhone()));
            }
        }

        private void openGoogleMaps(String address) {
            // 創建一個 URI，用於 Google 地圖路線規劃
            Uri gmmIntentUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + Uri.encode(address));
            // 創建一個 Intent 來查看該 URI
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            // 指定使用 Google 地圖應用
            mapIntent.setPackage("com.google.android.apps.maps");

            // 檢查是否有應用可以處理此 Intent
            if (mapIntent.resolveActivity(mActivity.getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                // 如果沒有安裝 Google 地圖應用，則在瀏覽器中開啟
                startActivity(new Intent(Intent.ACTION_VIEW, gmmIntentUri));
            }
        }

        private void callPhoneNumber(String phoneNumber) {
            try {
                // 清理電話號碼，移除所有非數字字符
                String cleanedNumber = phoneNumber.replaceAll("[^0-9]", "");

                // 創建一個撥號 Intent
                Intent intent = new Intent(Intent.ACTION_DIAL);

                // 設置電話號碼，使用 Uri.encode 確保正確編碼
                intent.setData(Uri.parse("tel:" + Uri.encode(cleanedNumber)));

                // 添加 FLAG_ACTIVITY_NEW_TASK 標誌
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                // 直接開始活動，不檢查 resolveActivity
                startActivity(intent);
            } catch (Exception e) {
                // 如果發生任何錯誤，記錄錯誤並通知用戶
                Log.e("CallPhoneError", "Error initiating phone call", e);
                Toast.makeText(mActivity, "無法啟動電話應用", Toast.LENGTH_SHORT).show();
            }
        }

        // 根據星期幾更新相應的 TextView
        @SuppressLint("SetTextI18n")
        private void updateTextView(String dayFlag, String openTime, TextView resMondayTextView, TextView resTuesdayTextView, TextView resWednesdayTextView, TextView resThursdayTextView, TextView resFridayTextView, TextView resSaturdayTextView, TextView resSundayTextView) {
            switch (dayFlag) {
                case "1":
                    resMondayTextView.setText("週一          " + openTime);
                    break;
                case "2":
                    resTuesdayTextView.setText("週二          " + openTime);
                    break;
                case "3":
                    resWednesdayTextView.setText("週三          " + openTime);
                    break;
                case "4":
                    resThursdayTextView.setText("週四          " + openTime);
                    break;
                case "5":
                    resFridayTextView.setText("週五          " + openTime);
                    break;
                case "6":
                    resSaturdayTextView.setText("週六          " + openTime);
                    break;
                case "7":
                    resSundayTextView.setText("週日          " + openTime);
                    break;
            }
        }

        private void fetchRestaurantGreenBehavior(int restaurantId, TextView greenBehavior1TextView, TextView greenBehavior2TextView, TextView greenBehavior3TextView, TextView greenBehavior4TextView, TextView greenBehavior5TextView, TextView greenBehavior6TextView, TextView greenBehavior7TextView) {
            new Thread(() -> {
                SQLite dbHelper = new SQLite(mActivity);

                // 從 restaurant_greenBehavior 表中獲取 greenBehavior_id
                List<Integer> greenBehaviorIds = dbHelper.getRestaurantGreenBehaviorIds(restaurantId);

                Log.d("fetchRestaurantGreenBehavior", "Retrieved greenBehavior IDs: " + greenBehaviorIds);
                List<SQLite.GreenBehavior> greenBehaviors = dbHelper.getGreenBehaviors(greenBehaviorIds);

                Log.d("fetchRestaurantGreenBehavior", "Retrieved greenBehaviors: " + greenBehaviors);

                // 在主線程中更新視圖
                mActivity.runOnUiThread(() -> updateOtherViews(greenBehaviors, greenBehavior1TextView, greenBehavior2TextView, greenBehavior3TextView, greenBehavior4TextView, greenBehavior5TextView, greenBehavior6TextView, greenBehavior7TextView));
            }).start();
        }


        @SuppressLint("SetTextI18n")
        private void updateOtherViews(List<SQLite.GreenBehavior> others, TextView other1TextView, TextView other2TextView, TextView other3TextView, TextView other4TextView, TextView other5TextView, TextView other6TextView, TextView other7TextView) {
            for (SQLite.GreenBehavior greenBehavior : others) {
                int id = greenBehavior.getId();
                String name = greenBehavior.getName();

                // 根據 id 設置相應的文本視圖
                if (id == 1) {
                    other1TextView.setText(Html.fromHtml("☑&nbsp;&nbsp;" + "<b>" + name + "</b>"));
                } else if (id == 2) {
                    other2TextView.setText(Html.fromHtml("☑&nbsp;&nbsp;" + "<b>" + name + "</b>"));
                } else if (id == 3) {
                    other3TextView.setText(Html.fromHtml("☑&nbsp;&nbsp;" + "<b>" + name + "</b>"));
                } else if (id == 4) {
                    other4TextView.setText(Html.fromHtml("☑&nbsp;&nbsp;" + "<b>" + name + "</b>"));
                } else if (id == 5) {
                    other5TextView.setText(Html.fromHtml("☑&nbsp;&nbsp;" + "<b>" + name + "</b>"));
                } else if (id == 6) {
                    other6TextView.setText(Html.fromHtml("☑&nbsp;&nbsp;" + "<b>" + name + "</b>"));
                } else if (id == 7) {
                    other7TextView.setText(Html.fromHtml("☑&nbsp;&nbsp;" + "<b>" + name + "</b>"));
                }
            }
        }

        private void addMemo(SQLite.Memo memo) {
            LinearLayout memoLayout = mActivity.findViewById(R.id.memo_layout);

            // 建立 FrameLayout
            FrameLayout frameLayout = new FrameLayout(mActivity);

            // 建立 TextView
            TextView memoText = new TextView(mActivity);
            String formattedText = formatMemoText(memo.getMemo_content());
            String memoWord = memo.getMemo_time().split(" ")[0] + "\n" + formattedText;
            memoText.setText(memoWord);
            memoText.setTextColor(Color.BLACK);
            memoText.setTypeface(null, Typeface.BOLD);

            // 設置 TextView 的布局参数
            FrameLayout.LayoutParams textViewParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );
            textViewParams.gravity = Gravity.TOP;
            textViewParams.setMargins(60, 15, 20, 5);
            memoText.setLayoutParams(textViewParams);

            // 設置分割線
            View divider = new View(mActivity);
            LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                    790,
                    3
            );
            dividerParams.setMargins(30, 5, 0, 5);
            divider.setLayoutParams(dividerParams);
            divider.setBackgroundColor(Color.GRAY);

            // 建立 ImageButton
            ImageButton editBtn = new ImageButton(mActivity);
            editBtn.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.editmemo));
            editBtn.setBackgroundColor(Color.TRANSPARENT); // 去背

            // 設置 ImageButton 的布局参数
            FrameLayout.LayoutParams editBtnParams = new FrameLayout.LayoutParams(
                    80,
                    80
            );
            editBtnParams.gravity = Gravity.TOP;
            editBtnParams.setMargins(680, 0, 20, 20);
            editBtn.setLayoutParams(editBtnParams);

            // 建立 ImageButton
            ImageButton deleteBtn = new ImageButton(mActivity);
            deleteBtn.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.deletememo));
            deleteBtn.setBackgroundColor(Color.TRANSPARENT); // 去背

            // 設置 ImageButton 的布局参数
            FrameLayout.LayoutParams deleteBtnParams = new FrameLayout.LayoutParams(
                    80,
                    80
            );
            deleteBtnParams.gravity = Gravity.TOP;
            deleteBtnParams.setMargins(760, 0, 20, 20);
            deleteBtn.setLayoutParams(deleteBtnParams);

            // 將 TextView 和按鈕添加到 FrameLayout
            frameLayout.addView(memoText);
            frameLayout.addView(editBtn);
            frameLayout.addView(deleteBtn);

            // 將 FrameLayout 和分割線添加到 LinearLayout
            memoLayout.addView(frameLayout);
            memoLayout.addView(divider);

            // 設置按鈕點擊事件
            setEditBtnClickListener(editBtn, memo, memoText);
            setDeleteBtnClickListener(deleteBtn, frameLayout, divider, memo);
        }

        // 用於批量添加 Memo 並動態更新 UI
        private void addMemosToLayout(ArrayList<SQLite.Memo> memoList) {
            for (SQLite.Memo memo : memoList) {
                addMemo(memo);  // 使用上面的方法動態添加 Memo
            }
        }

        /**換行**/
        private String formatMemoText(String text) {
            StringBuilder formattedText = new StringBuilder();
            int length = text.length();
            for (int i = 0; i < length; i++) {
                formattedText.append(text.charAt(i));
                // 在每 lineLength 个字符后插入换行符
                if ((i + 1) % 19 == 0 && (i + 1) != length) {
                    formattedText.append("\n");
                }
            }
            return formattedText.toString();
        }

        /** 編輯按鈕點擊事件 **/
        private void setEditBtnClickListener(ImageButton imageButton, SQLite.Memo memo, TextView memoTextView) {
            imageButton.setOnClickListener(v -> {
                Dialog dialog = new Dialog(mActivity);
                dialog.setContentView(R.layout.input_dialog);
                dialog.show();
                EditText editText = dialog.findViewById(R.id.dialog_input);
                final TextView counterTextView = dialog.findViewById(R.id.counter_text_view);
                final int maxLength = 100;
                function.totalWord(editText, counterTextView, maxLength);
                editText.setText(memo.getMemo_content());

                Button saveButton = dialog.findViewById(R.id.saveBtn);
                saveButton.setOnClickListener(v1 -> {
                    String memoText = editText.getText().toString().trim();
                    new Thread(() -> {
                        // 使用 SQLite 進行更新
                        dbHelper.editMemo(memo.getMemo_id(), memoText, function.getCurrentTime());

                        // 更新 UI
                        mActivity.runOnUiThread(() -> {
                            // 更新 TextView 的內容
                            String formattedText = formatMemoText(memoText);
                            String memoWord = memo.getMemo_time().split(" ")[0] + "\n" + formattedText;
                            memoTextView.setText(memoWord);
                        });
                    }).start();
                    dialog.dismiss(); // 關閉對話框
                });

                Button cancelBtn = dialog.findViewById(R.id.cancelBtn);
                cancelBtn.setOnClickListener(view -> dialog.dismiss());
            });
        }

        /** 刪除按鈕點擊事件 **/
        private void setDeleteBtnClickListener(ImageButton imageButton, FrameLayout frameLayout, View divider, SQLite.Memo memo) {
            imageButton.setOnClickListener(v -> {
                Dialog dialog = new Dialog(mActivity);
                dialog.setContentView(R.layout.delete_input_dialog);
                dialog.show();

                Button deleteButton = dialog.findViewById(R.id.confirmBtn);
                deleteButton.setOnClickListener(v1 -> {
                    new Thread(() -> {
                        // 使用 SQLite 進行刪除
                        dbHelper.deleteMemo(memo.getMemo_id());

                        // 更新 UI
                        mActivity.runOnUiThread(() -> {
                            // 從佈局中移除該備忘錄和分割線
                            LinearLayout memoLayout = mActivity.findViewById(R.id.memo_layout);
                            memoLayout.removeView(frameLayout);
                            memoLayout.removeView(divider);
                        });
                    }).start();
                    dialog.dismiss(); // 關閉對話框
                });

                Button cancelBtn = dialog.findViewById(R.id.cancelBtn);
                cancelBtn.setOnClickListener(view -> dialog.dismiss());
            });
        }

        private int dpToPx(int dp, Context context) {
            return Math.round(dp * context.getResources().getDisplayMetrics().density);
        }
    }

//    public static class ResMenuFragment extends Fragment {
//
//        @Nullable
//        private RestaurantData restaurantData;
//        private ImageView menuImageView;
//        private TextView noMenuTextView;
//
//        @SuppressLint("MissingInflatedId")
//        @Nullable
//        @Override
//        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//            View rootView = inflater.inflate(R.layout.fragment_res_menu, container, false);
//
//            menuImageView = rootView.findViewById(R.id.menu_image_view);
//            noMenuTextView = rootView.findViewById(R.id.no_menu_text_view);
//
//            // 獲取Intent並取出RestaurantData對象
//            Intent intent = requireActivity().getIntent();
//            if (intent != null && intent.hasExtra("restaurant_data")) {
//                restaurantData = (RestaurantData) intent.getSerializableExtra("restaurant_data");
//            }
//
//            return rootView;
//        }
//
//        @Override
//        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//            super.onViewCreated(view, savedInstanceState);
//
//            if (restaurantData != null) {
//                loadMenuImage();
//            }
//        }
//
//        private void loadMenuImage() {
//            Resources resources = getResources();
//            String menuName = "menu_" + restaurantData.getId();
//            int menuResourceId = resources.getIdentifier(menuName, "drawable", requireActivity().getPackageName());
//
//            if (menuResourceId != 0) {
//                try {
//                    Drawable menuDrawable = resources.getDrawable(menuResourceId, null);
//                    menuImageView.setVisibility(View.VISIBLE);
//                    noMenuTextView.setVisibility(View.GONE);
//                    setupInitialImageViewMatrix(menuImageView, menuDrawable);
//                    menuImageView.setImageDrawable(menuDrawable);
//                    menuImageView.setOnClickListener(v -> showEnlargedMenuDialog(menuDrawable));
//                } catch (Resources.NotFoundException e) {
//                    Log.w("ResMenuFragment", "Menu not found: " + menuName, e);
//                    showNoMenuMessage();
//                }
//            } else {
//                Log.w("ResMenuFragment", "Menu resource not found: " + menuName);
//                showNoMenuMessage();
//            }
//        }
//
//        private void showNoMenuMessage() {
//            menuImageView.setVisibility(View.GONE);
//            noMenuTextView.setVisibility(View.VISIBLE);
//            noMenuTextView.setText("目前該餐廳未提供菜單");
//        }
//
//        private void setupInitialImageViewMatrix(ImageView imageView, Drawable drawable) {
//            if (drawable == null) return;
//
//            int drawableWidth = drawable.getIntrinsicWidth();
//            int drawableHeight = drawable.getIntrinsicHeight();
//
//            imageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//                @Override
//                public void onGlobalLayout() {
//                    imageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//
//                    int viewWidth = imageView.getWidth();
//                    int viewHeight = imageView.getHeight();
//
//                    float scale = Math.min(
//                            (float) viewWidth / drawableWidth,
//                            (float) viewHeight / drawableHeight
//                    );
//
//                    float translateX = (viewWidth - drawableWidth * scale) / 2f;
//                    float translateY = (viewHeight - drawableHeight * scale) / 2f;
//
//                    Matrix matrix = new Matrix();
//                    matrix.setScale(scale, scale);
//                    matrix.postTranslate(translateX, translateY);
//
//                    imageView.setImageMatrix(matrix);
//
//                    // 設置 ImageMatrixTouchHandler
//                    ImageMatrixTouchHandler touchHandler = new ImageMatrixTouchHandler(imageView, scale);
//                    imageView.setOnTouchListener(touchHandler);
//                }
//            });
//        }
//
//        private void showEnlargedMenuDialog(Drawable menuDrawable) {
//            Dialog dialog = new Dialog(requireActivity(), android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
//            dialog.setContentView(R.layout.layout_enlarged_menu);
//
//            ImageView blurredBackground = dialog.findViewById(R.id.blurred_background);
//            ImageView enlargedImageView = dialog.findViewById(R.id.enlarged_menu_image);
//            ImageButton closeButton = dialog.findViewById(R.id.close_button);
//
//            // 創建並設置模糊背景
//            Bitmap blurredBitmap = createBlurredBackground();
//            blurredBackground.setImageBitmap(blurredBitmap);
//
//            // 設置放大的菜單圖片
//            enlargedImageView.setImageDrawable(menuDrawable);
//
//            // 使用 ViewTreeObserver 確保我們可以獲得正確的視圖尺寸
//            enlargedImageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//                @Override
//                public void onGlobalLayout() {
//                    enlargedImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//
//                    int viewWidth = enlargedImageView.getWidth();
//                    int viewHeight = enlargedImageView.getHeight();
//                    int drawableWidth = menuDrawable.getIntrinsicWidth();
//                    int drawableHeight = menuDrawable.getIntrinsicHeight();
//
//                    float scale = Math.min((float) viewWidth / drawableWidth, (float) viewHeight / drawableHeight);
//
//                    Matrix matrix = new Matrix();
//                    matrix.setScale(scale, scale);
//                    float translateX = (viewWidth - drawableWidth * scale) / 2f;
//                    float translateY = (viewHeight - drawableHeight * scale) / 2f;
//                    matrix.postTranslate(translateX, translateY);
//
//                    enlargedImageView.setImageMatrix(matrix);
//
//                    ImageMatrixTouchHandler touchHandler = new ImageMatrixTouchHandler(enlargedImageView, scale);
//                    enlargedImageView.setOnTouchListener(touchHandler);
//                }
//            });
//
//            closeButton.setOnClickListener(v -> dialog.dismiss());
//
//            dialog.show();
//        }
//
//        private Bitmap createBlurredBackground() {
//            Bitmap screenshot = takeScreenshot();
//            return blurBitmap(screenshot, 25); // 25是模糊程度，可以根據需要調整
//        }
//
//        private Bitmap takeScreenshot() {
//            View rootView = requireActivity().getWindow().getDecorView().getRootView();
//            rootView.setDrawingCacheEnabled(true);
//            Bitmap bitmap = Bitmap.createBitmap(rootView.getDrawingCache());
//            rootView.setDrawingCacheEnabled(false);
//            return bitmap;
//        }
//
//        private Bitmap blurBitmap(Bitmap bitmap, float blurRadius) {
//            RenderScript rs = RenderScript.create(requireContext());
//            Allocation input = Allocation.createFromBitmap(rs, bitmap);
//            Allocation output = Allocation.createTyped(rs, input.getType());
//            ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
//            script.setRadius(blurRadius);
//            script.setInput(input);
//            script.forEach(output);
//            output.copyTo(bitmap);
//            rs.destroy();
//            return bitmap;
//        }
//
//        private static class ImageMatrixTouchHandler implements View.OnTouchListener {
//            private static final float MAX_SCALE = 3f;
//            private float minScale;
//
//            private final Matrix matrix = new Matrix();
//            private final Matrix savedMatrix = new Matrix();
//
//            private static final int NONE = 0;
//            private static final int DRAG = 1;
//            private static final int ZOOM = 2;
//            private int mode = NONE;
//
//            private PointF start = new PointF();
//            private PointF mid = new PointF();
//            private float oldDist = 1f;
//
//            private final ImageView imageView;
//            private final int viewWidth;
//            private final int viewHeight;
//            private final int drawableWidth;
//            private final int drawableHeight;
//
//            ImageMatrixTouchHandler(ImageView imageView, float initialScale) {
//                this.imageView = imageView;
//                this.viewWidth = imageView.getWidth();
//                this.viewHeight = imageView.getHeight();
//                Drawable drawable = imageView.getDrawable();
//                this.drawableWidth = drawable.getIntrinsicWidth();
//                this.drawableHeight = drawable.getIntrinsicHeight();
//                this.minScale = initialScale;
//                matrix.set(imageView.getImageMatrix());
//            }
//
//            @SuppressLint("ClickableViewAccessibility")
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                ImageView view = (ImageView) v;
//
//                switch (event.getAction() & MotionEvent.ACTION_MASK) {
//                    case MotionEvent.ACTION_DOWN:
//                        savedMatrix.set(matrix);
//                        start.set(event.getX(), event.getY());
//                        mode = DRAG;
//                        break;
//                    case MotionEvent.ACTION_POINTER_DOWN:
//                        oldDist = spacing(event);
//                        if (oldDist > 10f) {
//                            savedMatrix.set(matrix);
//                            midPoint(mid, event);
//                            mode = ZOOM;
//                        }
//                        break;
//                    case MotionEvent.ACTION_MOVE:
//                        if (mode == DRAG) {
//                            matrix.set(savedMatrix);
//                            float dx = event.getX() - start.x;
//                            float dy = event.getY() - start.y;
//                            matrix.postTranslate(dx, dy);
//                        } else if (mode == ZOOM) {
//                            float newDist = spacing(event);
//                            if (newDist > 10f) {
//                                matrix.set(savedMatrix);
//                                float scale = newDist / oldDist;
//                                matrix.postScale(scale, scale, mid.x, mid.y);
//                            }
//                        }
//                        break;
//                    case MotionEvent.ACTION_UP:
//                    case MotionEvent.ACTION_POINTER_UP:
//                        mode = NONE;
//                        break;
//                }
//
//                // 應用縮放和平移限制
//                applyLimits();
//
//                view.setImageMatrix(matrix);
//                return true;
//            }
//
//            private void applyLimits() {
//                float[] values = new float[9];
//                matrix.getValues(values);
//
//                // 限制縮放
//                float scale = values[Matrix.MSCALE_X];
//                if (scale < minScale) {
//                    matrix.setScale(minScale, minScale);
//                    matrix.getValues(values);
//                } else if (scale > MAX_SCALE) {
//                    float scaleFactor = MAX_SCALE / scale;
//                    matrix.postScale(scaleFactor, scaleFactor, viewWidth / 2f, viewHeight / 2f);
//                    matrix.getValues(values);
//                }
//
//                // 限制平移
//                float translateX = values[Matrix.MTRANS_X];
//                float translateY = values[Matrix.MTRANS_Y];
//
//                float currentWidth = drawableWidth * values[Matrix.MSCALE_X];
//                float currentHeight = drawableHeight * values[Matrix.MSCALE_Y];
//
//                if (currentWidth > viewWidth) {
//                    translateX = Math.min(0, Math.max(translateX, viewWidth - currentWidth));
//                } else {
//                    translateX = (viewWidth - currentWidth) / 2;
//                }
//
//                if (currentHeight > viewHeight) {
//                    translateY = Math.min(0, Math.max(translateY, viewHeight - currentHeight));
//                } else {
//                    translateY = (viewHeight - currentHeight) / 2;
//                }
//
//                matrix.setTranslate(translateX, translateY);
//                matrix.preScale(values[Matrix.MSCALE_X], values[Matrix.MSCALE_Y]);
//            }
//
//            private float spacing(MotionEvent event) {
//                float x = event.getX(0) - event.getX(1);
//                float y = event.getY(0) - event.getY(1);
//                return (float) Math.sqrt(x * x + y * y);
//            }
//
//            private void midPoint(PointF point, MotionEvent event) {
//                float x = event.getX(0) + event.getX(1);
//                float y = event.getY(0) + event.getY(1);
//                point.set(x / 2, y / 2);
//            }
//        }
//    }
    public static class ResMenuFragment extends Fragment {

        @Nullable
        private RestaurantData restaurantData;
        private LinearLayout menuContainer;
        private TextView noMenuTextView;

        @SuppressLint("MissingInflatedId")
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_res_menu, container, false);

            menuContainer = rootView.findViewById(R.id.menu_container);
            noMenuTextView = rootView.findViewById(R.id.no_menu_text_view);

            // 獲取Intent並取出RestaurantData對象
            Intent intent = requireActivity().getIntent();
            if (intent != null && intent.hasExtra("restaurant_data")) {
                restaurantData = (RestaurantData) intent.getSerializableExtra("restaurant_data");
            }

            return rootView;
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            if (restaurantData != null) {
                loadMenuImages();
            }
        }

        /**
         * 載入所有菜單圖片
         */
        private void loadMenuImages() {
            Resources resources = getResources();
            int menuIndex = 1;
            boolean hasMenu = false;

            while (true) {
                String menuName = "menu" + restaurantData.getId() + "_" + menuIndex;
                int menuResourceId = resources.getIdentifier(menuName, "drawable", requireActivity().getPackageName());

                if (menuResourceId != 0) {
                    try {
                        Drawable menuDrawable = resources.getDrawable(menuResourceId, null);
                        addMenuImageView(menuDrawable, menuIndex);
                        hasMenu = true;
                        Log.e("ResMenuFragment", "Menu added: " + menuIndex);
                    } catch (Resources.NotFoundException e) {
                        Log.e("ResMenuFragment", "Menu not found: " + menuName, e);
                        break;
                    }
                } else {
                    Log.e("ResMenuFragment", "Menu not found: " + menuName);
                    break;
                }

                menuIndex++;
            }

            if (!hasMenu) {
                showNoMenuMessage();
            }
        }

        /**
         * 添加菜單圖片到容器中
         * @param menuDrawable 菜單圖片
         * @param menuIndex 菜單索引
         */
        private void addMenuImageView(Drawable menuDrawable, int menuIndex) {
            ImageView menuImageView = new ImageView(requireContext());
            menuImageView.setAdjustViewBounds(true);
            menuImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, (int) (8 * getResources().getDisplayMetrics().density)); // 8dp的間距
            menuImageView.setLayoutParams(params);

            setupInitialImageViewMatrix(menuImageView, menuDrawable);
            menuImageView.setImageDrawable(menuDrawable);
            menuImageView.setOnClickListener(v -> showEnlargedMenuDialog(menuDrawable));

            menuContainer.addView(menuImageView);
        }

        /**
         * 顯示無菜單消息
         */
        private void showNoMenuMessage() {
            menuContainer.setVisibility(View.GONE);
            noMenuTextView.setVisibility(View.VISIBLE);
            noMenuTextView.setText("目前該餐廳未提供菜單");
        }

        /**
         * 設置初始圖片矩陣
         * @param imageView 圖片視圖
         * @param drawable 圖片drawable
         */
        private void setupInitialImageViewMatrix(ImageView imageView, Drawable drawable) {
            if (drawable == null) return;

            int drawableWidth = drawable.getIntrinsicWidth();
            int drawableHeight = drawable.getIntrinsicHeight();

            imageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    imageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    int viewWidth = imageView.getWidth();
                    int viewHeight = imageView.getHeight();

                    float scale = Math.min(
                            (float) viewWidth / drawableWidth,
                            (float) viewHeight / drawableHeight
                    );

                    float translateX = (viewWidth - drawableWidth * scale) / 2f;
                    float translateY = (viewHeight - drawableHeight * scale) / 2f;

                    Matrix matrix = new Matrix();
                    matrix.setScale(scale, scale);
                    matrix.postTranslate(translateX, translateY);

                    imageView.setImageMatrix(matrix);
                }
            });
        }

        /**
         * 顯示放大的菜單對話框
         * @param menuDrawable 菜單圖片drawable
         */
        private void showEnlargedMenuDialog(Drawable menuDrawable) {
            Dialog dialog = new Dialog(requireActivity(), android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
            dialog.setContentView(R.layout.layout_enlarged_menu);

            ImageView blurredBackground = dialog.findViewById(R.id.blurred_background);
            ImageView enlargedImageView = dialog.findViewById(R.id.enlarged_menu_image);
            ImageButton closeButton = dialog.findViewById(R.id.close_button);

            // 創建並設置模糊背景
            Bitmap blurredBitmap = createBlurredBackground();
            blurredBackground.setImageBitmap(blurredBitmap);

            // 設置放大的菜單圖片
            enlargedImageView.setImageDrawable(menuDrawable);

            // 使用 ViewTreeObserver 確保我們可以獲得正確的視圖尺寸
            enlargedImageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    enlargedImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    int viewWidth = enlargedImageView.getWidth();
                    int viewHeight = enlargedImageView.getHeight();
                    int drawableWidth = menuDrawable.getIntrinsicWidth();
                    int drawableHeight = menuDrawable.getIntrinsicHeight();

                    float scale = Math.min((float) viewWidth / drawableWidth, (float) viewHeight / drawableHeight);

                    Matrix matrix = new Matrix();
                    matrix.setScale(scale, scale);
                    float translateX = (viewWidth - drawableWidth * scale) / 2f;
                    float translateY = (viewHeight - drawableHeight * scale) / 2f;
                    matrix.postTranslate(translateX, translateY);

                    enlargedImageView.setImageMatrix(matrix);

                    ImageMatrixTouchHandler touchHandler = new ImageMatrixTouchHandler(enlargedImageView, scale);
                    enlargedImageView.setOnTouchListener(touchHandler);
                }
            });

            closeButton.setOnClickListener(v -> dialog.dismiss());

            dialog.show();
        }

        /**
         * 創建模糊背景
         * @return 模糊後的Bitmap
         */
        private Bitmap createBlurredBackground() {
            Bitmap screenshot = takeScreenshot();
            return blurBitmap(screenshot, 25); // 25是模糊程度，可以根據需要調整
        }

        /**
         * 截取屏幕截圖
         * @return 屏幕截圖Bitmap
         */
        private Bitmap takeScreenshot() {
            View rootView = requireActivity().getWindow().getDecorView().getRootView();
            rootView.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(rootView.getDrawingCache());
            rootView.setDrawingCacheEnabled(false);
            return bitmap;
        }

        /**
         * 對Bitmap進行模糊處理
         * @param bitmap 原始Bitmap
         * @param blurRadius 模糊半徑
         * @return 模糊後的Bitmap
         */
        private Bitmap blurBitmap(Bitmap bitmap, float blurRadius) {
            RenderScript rs = RenderScript.create(requireContext());
            Allocation input = Allocation.createFromBitmap(rs, bitmap);
            Allocation output = Allocation.createTyped(rs, input.getType());
            ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
            script.setRadius(blurRadius);
            script.setInput(input);
            script.forEach(output);
            output.copyTo(bitmap);
            rs.destroy();
            return bitmap;
        }

        /**
         * 處理圖片縮放和平移的內部類
         */
        private static class ImageMatrixTouchHandler implements View.OnTouchListener {
            private static final float MAX_SCALE = 3f;
            private float minScale;

            private final Matrix matrix = new Matrix();
            private final Matrix savedMatrix = new Matrix();

            private static final int NONE = 0;
            private static final int DRAG = 1;
            private static final int ZOOM = 2;
            private int mode = NONE;

            private PointF start = new PointF();
            private PointF mid = new PointF();
            private float oldDist = 1f;

            private final ImageView imageView;
            private final int viewWidth;
            private final int viewHeight;
            private final int drawableWidth;
            private final int drawableHeight;

            /**
             * 構造函數
             * @param imageView 要操作的ImageView
             * @param initialScale 初始縮放比例
             */
            ImageMatrixTouchHandler(ImageView imageView, float initialScale) {
                this.imageView = imageView;
                this.viewWidth = imageView.getWidth();
                this.viewHeight = imageView.getHeight();
                Drawable drawable = imageView.getDrawable();
                this.drawableWidth = drawable.getIntrinsicWidth();
                this.drawableHeight = drawable.getIntrinsicHeight();
                this.minScale = initialScale;
                matrix.set(imageView.getImageMatrix());
            }

            /**
             * 處理觸摸事件
             */
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ImageView view = (ImageView) v;

                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        savedMatrix.set(matrix);
                        start.set(event.getX(), event.getY());
                        mode = DRAG;
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        oldDist = spacing(event);
                        if (oldDist > 10f) {
                            savedMatrix.set(matrix);
                            midPoint(mid, event);
                            mode = ZOOM;
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (mode == DRAG) {
                            matrix.set(savedMatrix);
                            float dx = event.getX() - start.x;
                            float dy = event.getY() - start.y;
                            matrix.postTranslate(dx, dy);
                        } else if (mode == ZOOM) {
                            float newDist = spacing(event);
                            if (newDist > 10f) {
                                matrix.set(savedMatrix);
                                float scale = newDist / oldDist;
                                matrix.postScale(scale, scale, mid.x, mid.y);
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                        mode = NONE;
                        break;
                }

                // 應用縮放和平移限制
                applyLimits();

                view.setImageMatrix(matrix);
                return true;
            }

            /**
             * 應用縮放和平移限制
             */
            private void applyLimits() {
                float[] values = new float[9];
                matrix.getValues(values);

                // 限制縮放
                float scale = values[Matrix.MSCALE_X];
                if (scale < minScale) {
                    matrix.setScale(minScale, minScale);
                    matrix.getValues(values);
                } else if (scale > MAX_SCALE) {
                    float scaleFactor = MAX_SCALE / scale;
                    matrix.postScale(scaleFactor, scaleFactor, viewWidth / 2f, viewHeight / 2f);
                    matrix.getValues(values);
                }

                // 限制平移
                float translateX = values[Matrix.MTRANS_X];
                float translateY = values[Matrix.MTRANS_Y];

                float currentWidth = drawableWidth * values[Matrix.MSCALE_X];
                float currentHeight = drawableHeight * values[Matrix.MSCALE_Y];

                if (currentWidth > viewWidth) {
                    translateX = Math.min(0, Math.max(translateX, viewWidth - currentWidth));
                } else {
                    translateX = (viewWidth - currentWidth) / 2;
                }

                if (currentHeight > viewHeight) {
                    translateY = Math.min(0, Math.max(translateY, viewHeight - currentHeight));
                } else {
                    translateY = (viewHeight - currentHeight) / 2;
                }

                matrix.setTranslate(translateX, translateY);
                matrix.preScale(values[Matrix.MSCALE_X], values[Matrix.MSCALE_Y]);
            }

            /**
             * 計算兩點間的距離
             */
            private float spacing(MotionEvent event) {
                float x = event.getX(0) - event.getX(1);
                float y = event.getY(0) - event.getY(1);
                return (float) Math.sqrt(x * x + y * y);
            }

            /**
             * 計算兩點的中點
             */
            private void midPoint(PointF point, MotionEvent event) {
                float x = event.getX(0) + event.getX(1);
                float y = event.getY(0) + event.getY(1);
                point.set(x / 2, y / 2);
            }
        }
    }
}