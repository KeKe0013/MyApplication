package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLite extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "UserData.db";
    private static final int DATABASE_VERSION = 14;

    // User table
    private static final String TABLE_USER = "user";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_USER_NAME = "user_name";
    private static final String COLUMN_PICTURE_ID = "picture_id";
    private static final String COLUMN_SIGN_DAY = "sign_day";
    private static final String COLUMN_DROP = "drop_num";
    private static final String COLUMN_SIGN_FLAG = "sign_flag";
    private static final String COLUMN_FIRST_LOGIN = "first_login";

    // Tree table
    private static final String TABLE_TREE = "tree";
    private static final String COLUMN_TREE_ID = "tree_id";
    private static final String COLUMN_TREE_BAR = "tree_bar";
    private static final String COLUMN_TREE_STAGE = "tree_stage";

    // Restaurant table
    private static final String TABLE_RESTAURANT = "restaurant";
    private static final String COLUMN_RESTAURANT_ID = "restaurant_id";
    private static final String COLUMN_RESTAURANT_NAME = "restaurant_name";
    private static final String COLUMN_CITY = "city";
    private static final String COLUMN_DISTRICT = "district";
    private static final String COLUMN_RESTAURANT_PHONE = "restaurant_phone";
    private static final String COLUMN_ADDRESS = "address";
    private static final String COLUMN_LAT = "lat";
    private static final String COLUMN_LNG = "lng";
    private static final String COLUMN_LOGO = "logo";
    private static final String COLUMN_MENU = "menu";

    // Donate table
    private static final String TABLE_DONATE = "donate";
    private static final String COLUMN_DONATE_ID = "donate_id";
    private static final String COLUMN_DONATE_NAME = "donate_name";
    private static final String COLUMN_DONATE_TIME = "donate_time";

    // Memo table
    private static final String TABLE_MEMO = "memo";
    private static final String COLUMN_MEMO_ID = "memo_id";
    private static final String COLUMN_MEMO_TIME = "memo_time";
    private static final String COLUMN_CONTENT = "content";

    // Check-in Record table
    private static final String TABLE_CHECK_IN_RECORD = "check_in_record";
    private static final String COLUMN_CHECKIN_ID = "checkIn_id";
    private static final String COLUMN_TIME = "time";
    private static final String COLUMN_COUNT = "count";
    private static final String COLUMN_FIRST_FLAG = "first_flag";

    // List table
    private static final String TABLE_LIST = "list";
    private static final String COLUMN_LIST_ID = "list_id";
    private static final String COLUMN_STATE = "state";

    // Picture table
    private static final String TABLE_PICTURE = "picture";
    private static final String COLUMN_PICTURE_NAME = "picture_name";

    // Business Hours table
    private static final String TABLE_BUSINESS_HOURS = "business_hours";
    private static final String COLUMN_OPENTIME_ID = "openTime_id";
    private static final String COLUMN_WEEK = "week";
    private static final String COLUMN_OPEN_TIME = "open_time";

    // Menu table
    private static final String TABLE_MENU = "menu";
    private static final String COLUMN_MENU_ID = "menu_id";
    private static final String COLUMN_MENU_NAME = "menu_name";

    // Tag table
    private static final String TABLE_TAG = "tag";
    private static final String COLUMN_TAG_ID = "tag_id";
    private static final String COLUMN_TAG_NAME = "tag_name";
    private static final String COLUMN_TAG_PICTURE = "tag_picture";
    private static final String COLUMN_CLICK_NUM = "click_num";

    // Restaurant Tag table
    private static final String TABLE_RESTAURANT_TAG = "restaurant_tag";
    private static final String COLUMN_RESTAURANT_TAG_ID = "restaurantTag_id";

    // Green Behavior table
    private static final String TABLE_GREEN_BEHAVIOR = "green_behavior";
    private static final String COLUMN_GREEN_BEHAVIOR_ID = "greenBehavior_id";
    private static final String COLUMN_GREEN_BEHAVIOR_NAME = "greenBehavior_name";

    // Restaurant Green Behavior table
    private static final String TABLE_RESTAURANT_GREEN_BEHAVIOR = "restaurant_greenBehavior";
    private static final String COLUMN_RESTAURANT_GREEN_BEHAVIOR_ID = "restaurantGreenBehavior_id";

    public SQLite(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 建立所有資料表
        String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_USER + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_USER_NAME + " TEXT,"
                + COLUMN_PICTURE_ID + " INTEGER,"
                + COLUMN_SIGN_DAY + " INTEGER,"
                + COLUMN_DROP + " INTEGER,"
                + COLUMN_SIGN_FLAG + " INTEGER,"
                + COLUMN_FIRST_LOGIN + " INTEGER DEFAULT 1" + ")";
        db.execSQL(CREATE_USER_TABLE);

        String CREATE_TREE_TABLE = "CREATE TABLE " + TABLE_TREE + "("
                + COLUMN_TREE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_ID + " INTEGER,"
                + COLUMN_TREE_BAR + " INTEGER,"
                + COLUMN_TREE_STAGE + " INTEGER" + ")";
        db.execSQL(CREATE_TREE_TABLE);

        String CREATE_RESTAURANT_TABLE = "CREATE TABLE " + TABLE_RESTAURANT + "("
                + COLUMN_RESTAURANT_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_RESTAURANT_NAME + " TEXT,"
                + COLUMN_CITY + " TEXT,"
                + COLUMN_DISTRICT + " TEXT,"
                + COLUMN_RESTAURANT_PHONE + " TEXT,"
                + COLUMN_ADDRESS + " TEXT,"
                + COLUMN_LAT + " REAL,"
                + COLUMN_LNG + " REAL,"
                + COLUMN_LOGO + " TEXT,"
                + COLUMN_MENU + " TEXT" + ")";
        db.execSQL(CREATE_RESTAURANT_TABLE);

        // 建立其他表
        String CREATE_DONATE_TABLE = "CREATE TABLE " + TABLE_DONATE + "("
                + COLUMN_DONATE_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_TREE_ID + " INTEGER,"
                + COLUMN_DONATE_NAME + " TEXT,"
                + COLUMN_DONATE_TIME + " TEXT" + ")";
        db.execSQL(CREATE_DONATE_TABLE);

        String CREATE_MEMO_TABLE = "CREATE TABLE " + TABLE_MEMO + "("
                + COLUMN_MEMO_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_USER_ID + " INTEGER,"
                + COLUMN_RESTAURANT_ID + " INTEGER,"
                + COLUMN_MEMO_TIME + " TEXT,"
                + COLUMN_CONTENT + " TEXT" + ")";
        db.execSQL(CREATE_MEMO_TABLE);

        String CREATE_CHECK_IN_RECORD_TABLE = "CREATE TABLE " + TABLE_CHECK_IN_RECORD + "("
                + COLUMN_CHECKIN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_USER_ID + " INTEGER,"
                + COLUMN_RESTAURANT_ID + " INTEGER,"
                + COLUMN_TIME + " TEXT,"
                + COLUMN_COUNT + " INTEGER,"
                + COLUMN_FIRST_FLAG + " INTEGER" + ")";
        db.execSQL(CREATE_CHECK_IN_RECORD_TABLE);

        String CREATE_LIST_TABLE = "CREATE TABLE " + TABLE_LIST + "("
                + COLUMN_LIST_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_USER_ID + " INTEGER,"
                + COLUMN_RESTAURANT_ID + " INTEGER,"
                + COLUMN_STATE + " INTEGER" + ")";
        db.execSQL(CREATE_LIST_TABLE);

        String CREATE_PICTURE_TABLE = "CREATE TABLE " + TABLE_PICTURE + "("
                + COLUMN_PICTURE_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_PICTURE_NAME + " TEXT" + ")";
        db.execSQL(CREATE_PICTURE_TABLE);

        String CREATE_BUSINESS_HOURS_TABLE = "CREATE TABLE " + TABLE_BUSINESS_HOURS + "("
                + COLUMN_OPENTIME_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_RESTAURANT_ID + " INTEGER,"
                + COLUMN_WEEK + " INTEGER,"
                + COLUMN_OPEN_TIME + " TEXT" + ")";
        db.execSQL(CREATE_BUSINESS_HOURS_TABLE);

        String CREATE_MENU_TABLE = "CREATE TABLE " + TABLE_MENU + "("
                + COLUMN_MENU_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_RESTAURANT_ID + " INTEGER,"
                + COLUMN_MENU_NAME + " TEXT" + ")";
        db.execSQL(CREATE_MENU_TABLE);

        String CREATE_TAG_TABLE = "CREATE TABLE " + TABLE_TAG + "("
                + COLUMN_TAG_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_TAG_NAME + " TEXT,"
                + COLUMN_TAG_PICTURE + " TEXT,"
                + COLUMN_CLICK_NUM + " INTEGER" + ")";
        db.execSQL(CREATE_TAG_TABLE);

        String CREATE_RESTAURANT_TAG_TABLE = "CREATE TABLE " + TABLE_RESTAURANT_TAG + "("
                + COLUMN_RESTAURANT_TAG_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_RESTAURANT_ID + " INTEGER,"
                + COLUMN_TAG_ID + " INTEGER" + ")";
        db.execSQL(CREATE_RESTAURANT_TAG_TABLE);

        String CREATE_GREEN_BEHAVIOR_TABLE = "CREATE TABLE " + TABLE_GREEN_BEHAVIOR + "("
                + COLUMN_GREEN_BEHAVIOR_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_GREEN_BEHAVIOR_NAME + " TEXT" + ")";
        db.execSQL(CREATE_GREEN_BEHAVIOR_TABLE);

        String CREATE_RESTAURANT_GREEN_BEHAVIOR_TABLE = "CREATE TABLE " + TABLE_RESTAURANT_GREEN_BEHAVIOR + "("
                + COLUMN_RESTAURANT_GREEN_BEHAVIOR_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_RESTAURANT_ID + " INTEGER,"
                + COLUMN_GREEN_BEHAVIOR_ID + " INTEGER" + ")";
        db.execSQL(CREATE_RESTAURANT_GREEN_BEHAVIOR_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 升級資料庫時，刪除舊的資料表並重新建立
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TREE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESTAURANT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DONATE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEMO);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHECK_IN_RECORD);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LIST);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PICTURE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUSINESS_HOURS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MENU);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TAG);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESTAURANT_TAG);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GREEN_BEHAVIOR);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESTAURANT_GREEN_BEHAVIOR);

        // 重新建立資料表
        onCreate(db);
    }

    // 以下是有關 User 表的相關方法

    /** 設置用戶的首次登入標記 **/
    public void setFirstLoginFlag(int userId, boolean isFirstLogin) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FIRST_LOGIN, isFirstLogin ? 1 : 0);
        String selection = COLUMN_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};
        db.update(TABLE_USER, values, selection, selectionArgs);
    }

    /** 檢查是否為首次登入 **/
    public boolean isFirstLogin(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_USER + " WHERE " + COLUMN_USER_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count == 0;
    }

    /** 新增使用者 **/
    public long addUser(int userId, String userName, int photoId, int signDay, int drop, int signFlag) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_USER_NAME, userName);
        values.put(COLUMN_PICTURE_ID, photoId);
        values.put(COLUMN_SIGN_DAY, signDay);
        values.put(COLUMN_DROP, drop);
        values.put(COLUMN_SIGN_FLAG, signFlag);
        values.put(COLUMN_FIRST_LOGIN, 1);  // 初次登入
        return db.insert(TABLE_USER, null, values);
    }

    /** 取得使用者資料 **/
    public int[] getUserData(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID, COLUMN_PICTURE_ID, COLUMN_SIGN_DAY, COLUMN_DROP, COLUMN_SIGN_FLAG};
        String selection = COLUMN_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};
        Cursor cursor = db.query(TABLE_USER, columns, selection, selectionArgs, null, null, null);
        int[] userData = new int[5];
        if (cursor.moveToFirst()) {
            for (int i = 0; i < 5; i++) {
                userData[i] = cursor.getInt(i);
            }
        }
        cursor.close();
        return userData;
    }

    /** 取得使用者名稱 **/
    public String getUserName(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_NAME};
        String selection = COLUMN_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};
        Cursor cursor = db.query(TABLE_USER, columns, selection, selectionArgs, null, null, null);
        String userName = "";
        if (cursor.moveToFirst()) {
            userName = cursor.getString(0);
        }
        cursor.close();
        return userName;
    }

    public void updateUserName(int userId, String newUserName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_name", newUserName);
        db.update("user", values, "user_id = ?", new String[]{String.valueOf(userId)});
    }

    @SuppressLint("Range")
    public void resetSignFlagAndDay(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // 首先，查詢當前的 sign_flag 和 sign_day
        Cursor cursor = db.rawQuery("SELECT sign_flag, sign_day FROM user WHERE user_id = ?", new String[]{String.valueOf(userId)});
        int currentSignFlag = 0;
        int currentSignDay = 0;

        if (cursor.moveToFirst()) {
            currentSignFlag = cursor.getInt(cursor.getColumnIndex("sign_flag"));
            currentSignDay = cursor.getInt(cursor.getColumnIndex("sign_day"));
        }
        cursor.close();

        // 根據條件更新 sign_flag 和 sign_day
        if (currentSignFlag == 1) {
            values.put("sign_flag", 0);

            if (currentSignDay == 7) {
                values.put("sign_day", 0);
            }

            // 更新資料庫
            db.update("user", values, "user_id = ?", new String[]{String.valueOf(userId)});
        }

        db.close();
    }

    public List<Integer> getAllPictureIds() {
        List<Integer> pictureIds = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // 查詢 picture 表中的所有 picture_id
        Cursor cursor = db.rawQuery("SELECT picture_id FROM picture", null);

        if (cursor.moveToFirst()) {
            do {
                int pictureId = cursor.getInt(0);
                pictureIds.add(pictureId);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return pictureIds;
    }


    public void updateUserPicture(int userId, int pictureId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("picture_id", pictureId);
        db.update("user", values, "user_id = ?", new String[]{String.valueOf(userId)});
    }


    /** 更新使用者資料 **/
    public int updateUserData(int userId, String userName, int photoId, int signDay, int drop, int signFlag) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (userName != null) values.put(COLUMN_USER_NAME, userName);
        values.put(COLUMN_PICTURE_ID, photoId);
        values.put(COLUMN_SIGN_DAY, signDay);
        values.put(COLUMN_DROP, drop);
        values.put(COLUMN_SIGN_FLAG, signFlag);
        String selection = COLUMN_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};
        return db.update(TABLE_USER, values, selection, selectionArgs);
    }

    /** 更新打卡成功後水滴數 **/
    public void insertCheckIn(int checkInId, int userId, int restaurantId, String time, int count, int firstFlag) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("checkIn_id", checkInId);
        values.put("user_id", userId);
        values.put("restaurant_id", restaurantId);
        values.put("time", time);
        values.put("count", count);
        values.put("first_flag", firstFlag);
        db.insert("check_in_record", null, values);
    }

    public void updateCheckIn(int userId, int restaurantId, String time, int count, int firstFlag) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("time", time);
        values.put("count", count);
        values.put("first_flag", firstFlag);
        db.update("check_in_record", values, "user_id = ? AND restaurant_id = ?", new String[]{String.valueOf(userId), String.valueOf(restaurantId)});
    }


    public int getMaxCheckInId() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT MAX(checkIn_id) FROM check_in_record";
        Cursor cursor = db.rawQuery(query, null);
        int maxCheckInId = 0;
        if (cursor.moveToFirst()) {
            maxCheckInId = cursor.getInt(0);
        }
        cursor.close();
        return maxCheckInId;
    }

    /** 更新簽到狀態 **/
    public void updateCheckState(int user_id, int sign_day) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SIGN_DAY, sign_day + 1);
        values.put(COLUMN_SIGN_FLAG, 1);
        db.update(TABLE_USER, values, COLUMN_USER_ID + " = ?", new String[]{String.valueOf(user_id)});
        Log.v("updateCheckState", "更新簽到狀態完成：user_id = " + user_id + ",sign_day = " + (sign_day + 1));
    }

    /** 更新簽到完成後水滴數 **/
    @SuppressLint("Range")
    public void updateSignDrop(int user_id, int multiple) {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT " + COLUMN_DROP + " FROM " + TABLE_USER + " WHERE " + COLUMN_USER_ID + " = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(user_id)});
        int drop = 0;
        if (cursor.moveToFirst()) {
            drop = cursor.getInt(cursor.getColumnIndex(COLUMN_DROP));
        }
        cursor.close();

        ContentValues values = new ContentValues();
        if (multiple == 1) {
            values.put(COLUMN_DROP, drop + 5);
        } else {
            values.put(COLUMN_DROP, drop + 5 * multiple);
        }
        db.update(TABLE_USER, values, COLUMN_USER_ID + " = ?", new String[]{String.valueOf(user_id)});
        Log.v("DB", "更新簽到完成後水滴數完成：" + (multiple == 1 ? "+5" : "+" + 5 * multiple));
    }

    // 以下是有關 Tree 表的相關方法

    /** 新增樹苗數據 **/
    public long addTree(int userId, int treeBar, int treeStage) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_TREE_BAR, treeBar);
        values.put(COLUMN_TREE_STAGE, treeStage);
        return db.insert(TABLE_TREE, null, values);
    }

    /** 取得樹苗數據 **/
    public int[] getTreeData(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_TREE_ID, COLUMN_TREE_BAR, COLUMN_TREE_STAGE};
        String selection = COLUMN_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};
        String orderBy = COLUMN_TREE_ID + " DESC LIMIT 1";
        Cursor cursor = db.query(TABLE_TREE, columns, selection, selectionArgs, null, null, orderBy);
        int[] treeData = new int[3];
        if (cursor.moveToFirst()) {
            for (int i = 0; i < 3; i++) {
                treeData[i] = cursor.getInt(i);
            }
        }
        cursor.close();
        return treeData;
    }

    /** 更新樹苗數據 **/
    public int updateTreeData(int treeId, int treeBar, int treeStage) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TREE_BAR, treeBar);
        values.put(COLUMN_TREE_STAGE, treeStage);
        String selection = COLUMN_TREE_ID + " = ?";
        String[] selectionArgs = {String.valueOf(treeId)};
        return db.update(TABLE_TREE, values, selection, selectionArgs);
    }

    public int getTreeCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM tree", null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }


    /** 更新使用者與樹苗資料 **/
    public void updateDataInTransaction(int userId, String userName, int photoId, int signDay, int drop, int signFlag, int treeId, int treeBar, int treeStage) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            // 更新用戶數據
            ContentValues userValues = new ContentValues();
            if (userName != null) userValues.put(COLUMN_USER_NAME, userName);
            userValues.put(COLUMN_PICTURE_ID, photoId);
            userValues.put(COLUMN_SIGN_DAY, signDay);
            userValues.put(COLUMN_DROP, drop);
            userValues.put(COLUMN_SIGN_FLAG, signFlag);
            db.update(TABLE_USER, userValues, COLUMN_USER_ID + " = ?", new String[]{String.valueOf(userId)});

            // 更新樹木數據
            ContentValues treeValues = new ContentValues();
            treeValues.put(COLUMN_TREE_BAR, treeBar);
            treeValues.put(COLUMN_TREE_STAGE, treeStage);
            db.update(TABLE_TREE, treeValues, COLUMN_TREE_ID + " = ?", new String[]{String.valueOf(treeId)});

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    // 以下是有關 Restaurant 表的相關方法

    /** 插入餐廳資料 **/
    public void insertRestaurants(List<Map<String, String>> data) {
        insertData(TABLE_RESTAURANT, data);
    }

    /** 插入營業時間資料 **/
    public void insertBusinessHours(List<Map<String, String>> data) {
        insertData(TABLE_BUSINESS_HOURS, data);
    }

    /** 插入餐廳標籤資料 **/
    public void insertRestaurantTags(List<Map<String, String>> data) {
        insertData(TABLE_RESTAURANT_TAG, data);
    }

    /** 插入餐廳綠行為資料 **/
    public void insertRestaurantGreenBehaviors(List<Map<String, String>> data) {
        insertData(TABLE_RESTAURANT_GREEN_BEHAVIOR, data);
    }

    /** 插入通用資料 **/
    private void insertData(String tableName, List<Map<String, String>> data) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            for (Map<String, String> row : data) {
                ContentValues values = new ContentValues();
                for (Map.Entry<String, String> entry : row.entrySet()) {
                    values.put(entry.getKey(), entry.getValue());
                }
                db.insert(tableName, null, values);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /** 取得餐廳經緯度 **/
    @SuppressLint("Range")
    public double[] getRLocation(int r_id) {
        double[] result = new double[2];
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT " + COLUMN_LAT + ", " + COLUMN_LNG + " FROM " + TABLE_RESTAURANT + " WHERE " + COLUMN_RESTAURANT_ID + " = ?";

        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(r_id)});
        if (cursor.moveToFirst()) {
            result[0] = cursor.getDouble(cursor.getColumnIndex(COLUMN_LAT));
            result[1] = cursor.getDouble(cursor.getColumnIndex(COLUMN_LNG));
        }
        cursor.close();
        return result;
    }

    /**取得第一次、上次打卡時間**/
    @SuppressLint("Range")
    public String[] getTime (int u_id, int r_id, int count){
        String result[] = new String[2];
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = count == 1 ? "SELECT * FROM " + TABLE_CHECK_IN_RECORD + " WHERE " + COLUMN_USER_ID + " = ? AND " + COLUMN_RESTAURANT_ID + " = ? AND " + COLUMN_FIRST_FLAG + " = 1"
                : "SELECT * FROM " + TABLE_CHECK_IN_RECORD + " WHERE " + COLUMN_USER_ID + " = ? AND " + COLUMN_RESTAURANT_ID + " = ? AND " + COLUMN_FIRST_FLAG + " = 0";

        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(u_id), String.valueOf(r_id)});
        if (cursor.moveToFirst()) {
            result[0] = cursor.getString(cursor.getColumnIndex(COLUMN_TIME));
            result[1] = cursor.getString(cursor.getColumnIndex(COLUMN_COUNT));
        }
        cursor.close();
        return result;
    }

    /**計算是否打卡過**/
    @SuppressLint("Range")
    public int checkInCount(int u_id, int r_id) {
        int count = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT " + COLUMN_COUNT + " FROM " + TABLE_CHECK_IN_RECORD + " WHERE " + COLUMN_USER_ID + " = ? AND " + COLUMN_RESTAURANT_ID + " = ?";

        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(u_id), String.valueOf(r_id)});
        if (cursor.moveToFirst()) {
            count = cursor.getInt(cursor.getColumnIndex(COLUMN_COUNT));
        }
        cursor.close();
        return count;
    }


    /** 取得餐廳打卡時間 **/
//
    public Map<Integer, String> getCheckInTimes(int userId, List<Integer> restaurantIds) {
        Map<Integer, String> checkInTimes = new HashMap<>();

        if (restaurantIds.isEmpty()) {
            return checkInTimes;
        }

        SQLiteDatabase db = this.getReadableDatabase();
        String placeholders = TextUtils.join(",", Collections.nCopies(restaurantIds.size(), "?"));
        String sql = "SELECT " + COLUMN_RESTAURANT_ID + ", SUBSTRING(" + COLUMN_TIME + ", 1, 10) AS time_substring " +
                "FROM " + TABLE_CHECK_IN_RECORD + " " +
                "WHERE " + COLUMN_USER_ID + " = ? AND " + COLUMN_RESTAURANT_ID + " IN (" + placeholders + ")";

        String[] selectionArgs = new String[restaurantIds.size() + 1];
        selectionArgs[0] = String.valueOf(userId);
        for (int i = 0; i < restaurantIds.size(); i++) {
            selectionArgs[i + 1] = String.valueOf(restaurantIds.get(i));
        }

        Cursor cursor = db.rawQuery(sql, selectionArgs);

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") int restaurantId = cursor.getInt(cursor.getColumnIndex(COLUMN_RESTAURANT_ID));
                @SuppressLint("Range") String checkInTime = cursor.getString(cursor.getColumnIndex("time_substring"));
                checkInTimes.put(restaurantId, checkInTime);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return checkInTimes;
    }


    /** 取得餐廳狀態 **/
    @SuppressLint("Range")
    public int getRestaurantState(int userId, int restaurantId) {
        int state = 0; // 預設值為 0，表示未喜歡或不喜歡

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT state FROM list WHERE user_id = ? AND restaurant_id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), String.valueOf(restaurantId)});

        if (cursor != null && cursor.moveToFirst()) {
            state = cursor.getInt(cursor.getColumnIndex("state"));
            cursor.close();
        }
        db.close();
        return state;
    }

    /** 更新餐廳狀態 **/
    public void updateRestaurantState ( int restaurantId, int state){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("state", state);
        db.update("list", values, "restaurant_id = ?", new String[]{String.valueOf(restaurantId)});
    }

    /** 取得特定日期和地理範圍內的餐廳資料 **/
    @SuppressLint("Range")
    public List<RestaurantData> getNearbyData(int today, double userLat, double userLng) {
        List<RestaurantData> result = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // 計算緯度、經度的大致範圍（約10公里）
        // 1度緯度約等於111公里，1度經度在台灣緯度約等於90公里
        double latDelta = 10.0 / 111.0;   // 緯度範圍約±0.09度
        double lngDelta = 10.0 / (111.0 * Math.cos(Math.toRadians(userLat)));  // 經度範圍會隨緯度變化

        String query = "SELECT r.*, bh.week, bh.open_time " +
                "FROM restaurant r " +
                "INNER JOIN business_hours bh ON r.restaurant_id = bh.restaurant_id " +
                "WHERE bh.week = ? AND " +
                "r.lat BETWEEN ? AND ? AND " +
                "r.lng BETWEEN ? AND ?";

        String[] selectionArgs = {
                String.valueOf(today),
                String.valueOf(userLat - latDelta),
                String.valueOf(userLat + latDelta),
                String.valueOf(userLng - lngDelta),
                String.valueOf(userLng + lngDelta)
        };
        Log.e("DatabaseHelper2", "selectionArgs: " + Arrays.toString(selectionArgs));

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, selectionArgs);

            if (cursor.moveToFirst()) {
                do {
                    RestaurantData data = createRestaurantDataFromCursor(cursor);
                    result.add(data);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error retrieving restaurant data", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return result;
    }

    /** 取得餐廳資料 **/
    @SuppressLint("Range")
    public List<RestaurantData> getData(int today) {
        List<RestaurantData> result = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT r.*, bh.week, bh.open_time " +
                "FROM restaurant r " +
                "INNER JOIN business_hours bh ON r.restaurant_id = bh.restaurant_id " +
                "WHERE bh.week = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(today)});

        if (cursor.moveToFirst()) {
            do {
                RestaurantData data = createRestaurantDataFromCursor(cursor);
                result.add(data);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }


    /** 取得收藏清單中的餐廳 **/
    @SuppressLint("Range")
    public ArrayList<RestaurantData> getListRestaurant(int userId, int state, int today) {
        ArrayList<RestaurantData> result = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT r.*, bh.week, bh.open_time " +
                "FROM restaurant r " +
                "INNER JOIN business_hours bh ON r.restaurant_id = bh.restaurant_id " +
                "INNER JOIN list l ON r.restaurant_id = l.restaurant_id " +
                "WHERE l.user_id = ? AND l.state = ? AND bh.week = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), String.valueOf(state), String.valueOf(today)});

        if (cursor.moveToFirst()) {
            do {
                RestaurantData data = createRestaurantDataFromCursor(cursor);
                result.add(data);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    /** 檢查記錄是否存在 **/
    public boolean checkRecordExists(int userId, int restaurantId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM list WHERE user_id = ? AND restaurant_id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), String.valueOf(restaurantId)});
        boolean exists = false;
        if (cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0;
        }
        cursor.close();
        db.close();
        return exists;
    }

    /** 取得記錄狀態 **/
    public int getRecordState(int userId, int restaurantId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT state FROM list WHERE user_id = ? AND restaurant_id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), String.valueOf(restaurantId)});
        int state = 0;
        if (cursor.moveToFirst()) {
            state = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return state;
    }

    /** 刪除收藏記錄 **/
    public void deleteRecord(int userId, int restaurantId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM list WHERE user_id = ? AND restaurant_id = ?";
        db.execSQL(query, new Object[]{userId, restaurantId});
        db.close();
    }


    /** 更新記錄狀態 **/
    public void updateRecordState(int userId, int restaurantId, int state) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE list SET state = ? WHERE user_id = ? AND restaurant_id = ?";
        db.execSQL(query, new Object[]{state, userId, restaurantId});
        db.close();
    }

    /** 插入新記錄 **/
    public void insertRecord(int userId, int restaurantId, int state) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "INSERT INTO list (user_id, restaurant_id, state) VALUES (?, ?, ?)";
        db.execSQL(query, new Object[]{userId, restaurantId, state});
        db.close();
    }

    /** 取得符合標籤的餐廳資料 **/
    @SuppressLint("Range")
    public ArrayList<RestaurantData> getTagRestaurantData(int userId, int tagId, int today) {
        ArrayList<RestaurantData> result = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT r.*, bh.week, bh.open_time, " +
                "(SELECT state FROM list WHERE restaurant_id = r.restaurant_id AND user_id = ?) AS user_state " +
                "FROM restaurant r " +
                "INNER JOIN business_hours bh ON r.restaurant_id = bh.restaurant_id " +
                "INNER JOIN restaurant_tag rt ON r.restaurant_id = rt.restaurant_id " +
                "WHERE rt.tag_id = ? AND bh.week = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), String.valueOf(tagId), String.valueOf(today)});

        if (cursor.moveToFirst()) {
            do {
                RestaurantData data = createRestaurantDataFromCursor(cursor);
                data.setState(cursor.getInt(cursor.getColumnIndex("user_state")));
                result.add(data);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    /** 取得打卡過的餐廳資料 **/
    @SuppressLint("Range")
    public ArrayList<RestaurantData> getCheckInRestaurant(int userId, int today) {
        ArrayList<RestaurantData> result = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT r.*, bh.week, bh.open_time, " +
                "(SELECT state FROM list WHERE restaurant_id = r.restaurant_id AND user_id = ?) AS user_state, " +
                "MAX(cir.time) AS latest_check_in " +
                "FROM restaurant r " +
                "INNER JOIN business_hours bh ON r.restaurant_id = bh.restaurant_id " +
                "INNER JOIN check_in_record cir ON r.restaurant_id = cir.restaurant_id " +
                "WHERE cir.user_id = ? AND bh.week = ? " +
                "GROUP BY r.restaurant_id " +
                "ORDER BY latest_check_in DESC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), String.valueOf(userId), String.valueOf(today)});

        if (cursor.moveToFirst()) {
            do {
                RestaurantData data = createRestaurantDataFromCursor(cursor);
                data.setState(cursor.getInt(cursor.getColumnIndex("user_state")));
                result.add(data);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    /** 取得與筆記相關的餐廳資料 **/
    @SuppressLint("Range")
    public ArrayList<RestaurantData> getMemoRestaurant(int userId, int today) {
        ArrayList<RestaurantData> result = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT DISTINCT r.*, bh.week, bh.open_time, m.memo_id, m.memo_time, m.content, " +
                "t.tag_name1, t.tag_name2, t.tag_name3 " +
                "FROM memo m " +
                "JOIN restaurant r ON m.restaurant_id = r.restaurant_id " +
                "LEFT JOIN business_hours bh ON r.restaurant_id = bh.restaurant_id AND bh.week = ? " +
                "LEFT JOIN (SELECT restaurant_id, " +
                "    MAX(CASE WHEN tag_rank = 1 THEN tag_name END) AS tag_name1, " +
                "    MAX(CASE WHEN tag_rank = 2 THEN tag_name END) AS tag_name2, " +
                "    MAX(CASE WHEN tag_rank = 3 THEN tag_name END) AS tag_name3 " +
                "    FROM (SELECT rt.restaurant_id, t.tag_name, " +
                "        (SELECT COUNT(*) FROM restaurant_tag rt2 " +
                "         WHERE rt2.restaurant_id = rt.restaurant_id AND rt2.tag_id <= rt.tag_id) AS tag_rank " +
                "        FROM restaurant_tag rt " +
                "        JOIN tag t ON rt.tag_id = t.tag_id) " +
                "    WHERE tag_rank <= 3 " +
                "    GROUP BY restaurant_id) t ON r.restaurant_id = t.restaurant_id " +
                "WHERE m.user_id = ? " +
                "ORDER BY m.memo_time DESC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(today), String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                RestaurantData data = createRestaurantDataFromCursor(cursor);
                data.setMemo_id(cursor.getInt(cursor.getColumnIndex("memo_id")));
                data.setMemo_time(cursor.getString(cursor.getColumnIndex("memo_time")));
                data.setMemo_content(cursor.getString(cursor.getColumnIndex("content")));

                List<String> tags = new ArrayList<>();
                addTagNameIfNotNull(tags, cursor, "tag_name1");
                addTagNameIfNotNull(tags, cursor, "tag_name2");
                addTagNameIfNotNull(tags, cursor, "tag_name3");
                data.setTags(tags);

                result.add(data);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    /** 取得餐廳資料 **/
    // 以下是有關 Donate 表的相關方法

    /** 匿名捐贈 **/
    public void anonymousDonate(int tree_id, String donateTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TREE_ID, tree_id);
        values.put(COLUMN_DONATE_TIME, donateTime);
        db.insert(TABLE_DONATE, null, values);
        Log.v("anonymousDonate", "匿名捐贈完成：tree_id = " + tree_id);
    }

    /** 實名捐贈 **/
    public void registeredDonate(int tree_id, String donateName, String donateTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TREE_ID, tree_id);
        values.put(COLUMN_DONATE_NAME, donateName);
        values.put(COLUMN_DONATE_TIME, donateTime);
        db.insert(TABLE_DONATE, null, values);
        Log.v("registeredDonate", "實名捐贈完成：" + donateName);
    }

    /** 取得總捐贈人數 **/
    @SuppressLint("Range")
    public int totalDonateNum() {
        int result = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT COUNT(*) as num FROM " + TABLE_DONATE;
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            result = cursor.getInt(cursor.getColumnIndex("num"));
        }
        cursor.close();
        Log.v("donatePeople", "total：" + result);
        return result;
    }

    // 以下是有關 Tag 表的相關方法

    public static class AllTag {
        private int tag_id;
        private String tag_name;
        private int clickNum;
        private String tag_photo;

        public AllTag(int tag_id, String tag_name, int clickNum, String tag_photo) {
            this.tag_id = tag_id;
            this.tag_name = tag_name;
            this.clickNum = clickNum;
            this.tag_photo = tag_photo;
        }

        public int getTag_id() {
            return tag_id;
        }

        public String getTag_name() {
            return tag_name;
        }

        public int getClickNum() {
            return clickNum;
        }

    }

    /** 插入標籤資料 **/
    public void insertTags(List<Map<String, String>> data) {
        insertData(TABLE_TAG, data);
    }

    /** 取得所有分類標籤 **/
    public ArrayList<AllTag> getAllTag() {
        ArrayList<AllTag> result = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT " + COLUMN_TAG_ID + ", " + COLUMN_TAG_NAME + ", " + COLUMN_CLICK_NUM + ", " + COLUMN_TAG_PICTURE + " FROM " + TABLE_TAG;
        Cursor cursor = db.rawQuery(sql, null);

        while (cursor.moveToNext()) {
            @SuppressLint("Range") int tag_id = cursor.getInt(cursor.getColumnIndex(COLUMN_TAG_ID));
            @SuppressLint("Range") String tag_name = cursor.getString(cursor.getColumnIndex(COLUMN_TAG_NAME));
            @SuppressLint("Range") int clickNum = cursor.getInt(cursor.getColumnIndex(COLUMN_CLICK_NUM));
            @SuppressLint("Range") String tag_photo = cursor.getString(cursor.getColumnIndex(COLUMN_TAG_PICTURE));
            AllTag data = new AllTag(tag_id, tag_name, clickNum, tag_photo);
            result.add(data);
        }
        cursor.close();
        return result;
    }

    /** 更新按鈕點擊數 **/
    public void updateClick(int tag_id, int clickNum) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CLICK_NUM, clickNum + 1);
        db.update(TABLE_TAG, values, COLUMN_TAG_ID + " = ?", new String[]{String.valueOf(tag_id)});
        Log.v("DB", "更新資料完成：" + tag_id);
    }

    /** 取得餐廳標籤 ID **/
    @SuppressLint("Range")
    public List<Integer> getRestaurantTagIds(int restaurantId) {
        List<Integer> tagIds = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT tag_id FROM restaurant_tag WHERE restaurant_id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(restaurantId)});
        while (cursor.moveToNext()) {
            tagIds.add(cursor.getInt(cursor.getColumnIndex("tag_id")));
        }
        cursor.close();
        db.close();
        return tagIds;
    }

    /** 透過 tag_id 取得 click_num **/
    @SuppressLint("Range")
    public int getClickNumByTagId(int tag_id) {
        int clickNum = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_CLICK_NUM + " FROM " + TABLE_TAG + " WHERE " + COLUMN_TAG_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(tag_id)});

        if (cursor.moveToFirst()) {
            clickNum = cursor.getInt(cursor.getColumnIndex(COLUMN_CLICK_NUM));
        }
        cursor.close();
        db.close();
        return clickNum;
    }


    /** 取得標籤列表 **/
    public List<AllTag> getTags(List<Integer> tagIds) {
        List<AllTag> tags = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String inClause = TextUtils.join(",", Collections.nCopies(tagIds.size(), "?"));
        String query = "SELECT tag_id, tag_name, click_num, tag_picture FROM tag WHERE tag_id IN (" + inClause + ")";
        String[] selectionArgs = new String[tagIds.size()];
        for (int i = 0; i < tagIds.size(); i++) {
            selectionArgs[i] = String.valueOf(tagIds.get(i));
        }
        Cursor cursor = db.rawQuery(query, selectionArgs);
        while (cursor.moveToNext()) {
            @SuppressLint("Range") int tagId = cursor.getInt(cursor.getColumnIndex("tag_id"));
            @SuppressLint("Range") String tagName = cursor.getString(cursor.getColumnIndex("tag_name"));
            @SuppressLint("Range") int clickNum = cursor.getInt(cursor.getColumnIndex("click_num"));
            @SuppressLint("Range") String tagPhoto = cursor.getString(cursor.getColumnIndex("tag_picture"));
            tags.add(new AllTag(tagId, tagName, clickNum, tagPhoto));
        }
        cursor.close();
        db.close();
        return tags;
    }

    // 以下是有關 Menu 表的相關方法

    public class Memo {
        private int memo_id;
        private String memo_time;
        private String content;

        public Memo(int memo_id, String memo_time, String content) {
            this.memo_id = memo_id;
            this.memo_time = memo_time;
            this.content = content;
        }

        public int getMemo_id() {
            return memo_id;
        }

        public String getMemo_time() {
            return memo_time;
        }

        public String getMemo_content() {
            return content;
        }
    }

    // 以下是有關 Memo 表的相關方法

    /** 取得筆記內容 **/
    public ArrayList<Memo> getMemo(int u_id, int r_id) {
        ArrayList<Memo> result = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT " + COLUMN_MEMO_ID + ", " + COLUMN_MEMO_TIME + ", " + COLUMN_CONTENT + " FROM " + TABLE_MEMO + " WHERE " + COLUMN_USER_ID + " = ? AND " + COLUMN_RESTAURANT_ID + " = ? ORDER BY " + COLUMN_MEMO_TIME;
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(u_id), String.valueOf(r_id)});

        while (cursor.moveToNext()) {
            @SuppressLint("Range") int memo_id = cursor.getInt(cursor.getColumnIndex(COLUMN_MEMO_ID));
            @SuppressLint("Range") String memo_time = cursor.getString(cursor.getColumnIndex(COLUMN_MEMO_TIME));
            @SuppressLint("Range") String memo_content = cursor.getString(cursor.getColumnIndex(COLUMN_CONTENT));
            Memo data = new Memo(memo_id, memo_time, memo_content);
            result.add(data);
        }
        cursor.close();
        return result;
    }

    /** 新增筆記 **/
    public void addMemo(int u_id, int r_id, String memoTime, String memoText) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, u_id);
        values.put(COLUMN_RESTAURANT_ID, r_id);
        values.put(COLUMN_MEMO_TIME, memoTime);
        values.put(COLUMN_CONTENT, memoText);
        db.insert(TABLE_MEMO, null, values);
        Log.v("addMemo", "新增資料完成：" + memoText);
    }

    /** 編輯筆記 **/
    public void editMemo(int M_id, String memoText, String editTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CONTENT, memoText);
        values.put(COLUMN_MEMO_TIME, editTime);
        db.update(TABLE_MEMO, values, COLUMN_MEMO_ID + " = ?", new String[]{String.valueOf(M_id)});
        Log.v("editMemo", "編輯資料完成：" + memoText);
    }

    /** 刪除筆記 **/
    public void deleteMemo(int M_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MEMO, COLUMN_MEMO_ID + " = ?", new String[]{String.valueOf(M_id)});
        Log.v("deleteMemo", "刪除資料完成");
    }

    // 以下是有關 Business Hours 表的相關方法

    public class BusinessHours {
        private String week;
        private String openTime;

        public BusinessHours(String week, String openTime) {
            this.week = week;
            this.openTime = openTime;
        }

        public String getWeek() {
            return week;
        }

        public String getOpenTime() {
            return openTime;
        }
    }

    /** 取得當天營業時間 **/
    @SuppressLint("Range")
    public String[][] getBusinessTime(int restaurant_id, int dayOfWeek) {
        String[][] result = new String[7][2];
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_BUSINESS_HOURS + " WHERE " + COLUMN_RESTAURANT_ID + " = ? " +
                "ORDER BY " +
                "  CASE " +
                "    WHEN " + COLUMN_WEEK + " > ? THEN " + COLUMN_WEEK + " - ? " +
                "    ELSE " + COLUMN_WEEK + " + (7-?) " +
                "  END";

        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(restaurant_id), String.valueOf(dayOfWeek), String.valueOf(dayOfWeek), String.valueOf(dayOfWeek)});
        int index = 0;
        while (cursor.moveToNext()) {
            result[index][0] = cursor.getString(cursor.getColumnIndex(COLUMN_WEEK));
            result[index][1] = cursor.getString(cursor.getColumnIndex(COLUMN_OPEN_TIME));
            index++;
        }
        cursor.close();
        return result;
    }

    /** 取得營業時間列表 **/
    public List<BusinessHours> getBusinessHours(int restaurantId) {
        List<BusinessHours> businessHoursList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT week, open_time FROM business_hours WHERE restaurant_id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(restaurantId)});
        while (cursor.moveToNext()) {
            @SuppressLint("Range") String week = cursor.getString(cursor.getColumnIndex("week"));
            @SuppressLint("Range") String openTime = cursor.getString(cursor.getColumnIndex("open_time"));
            businessHoursList.add(new BusinessHours(week, openTime));
        }
        cursor.close();
        db.close();
        return businessHoursList;
    }

    // 以下是有關 Green Behavior 表的相關方法

    public class GreenBehavior {
        private int id;
        private String name;

        public GreenBehavior(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    /** 插入綠行為資料 **/
    public void insertGreenBehaviors(List<Map<String, String>> data) {
        insertData(TABLE_GREEN_BEHAVIOR, data);
    }

    /** 取得餐廳綠行為 ID 列表 **/
    @SuppressLint("Range")
    public List<Integer> getRestaurantGreenBehaviorIds(int restaurantId) {
        List<Integer> greenBehaviorIds = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT greenBehavior_id FROM restaurant_greenBehavior WHERE restaurant_id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(restaurantId)});
        while (cursor.moveToNext()) {
            @SuppressLint("Range") int greenBehaviorId = cursor.getInt(cursor.getColumnIndex("greenBehavior_id"));
            greenBehaviorIds.add(greenBehaviorId);
        }
        cursor.close();
        db.close();
        return greenBehaviorIds;
    }

    /** 取得綠行為列表 **/
    public List<GreenBehavior> getGreenBehaviors(List<Integer> greenBehaviorIds) {
        List<GreenBehavior> greenBehaviors = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT greenBehavior_id, greenBehavior_name FROM green_behavior WHERE greenBehavior_id = ?";
        for (int greenBehaviorId : greenBehaviorIds) {
            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(greenBehaviorId)});
            if (cursor.moveToNext()) {
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex("greenBehavior_id"));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex("greenBehavior_name"));
                greenBehaviors.add(new GreenBehavior(id, name));
            }
            cursor.close();
        }
        db.close();
        return greenBehaviors;
    }

    // 其他輔助方法

    /** 創建 RestaurantData 物件 **/
    @SuppressLint("Range")
    private RestaurantData createRestaurantDataFromCursor(Cursor cursor) {
        return new RestaurantData(
                cursor.getInt(cursor.getColumnIndex(COLUMN_RESTAURANT_ID)),
                cursor.getString(cursor.getColumnIndex(COLUMN_RESTAURANT_NAME)),
                cursor.getString(cursor.getColumnIndex(COLUMN_CITY)),
                cursor.getString(cursor.getColumnIndex(COLUMN_DISTRICT)),
                cursor.getString(cursor.getColumnIndex(COLUMN_RESTAURANT_PHONE)),
                cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS)),
                cursor.getDouble(cursor.getColumnIndex(COLUMN_LAT)),
                cursor.getDouble(cursor.getColumnIndex(COLUMN_LNG)),
                cursor.getString(cursor.getColumnIndex(COLUMN_MENU)),
                cursor.getString(cursor.getColumnIndex(COLUMN_LOGO)),
                cursor.getString(cursor.getColumnIndex(COLUMN_OPEN_TIME))
        );
    }

    /** 添加標籤名稱（如果不為空） **/
    private void addTagNameIfNotNull(List<String> tags, Cursor cursor, String columnName) {
        @SuppressLint("Range") String tagName = cursor.getString(cursor.getColumnIndex(columnName));
        if (tagName != null && !tagName.isEmpty()) {
            tags.add(tagName);
        }
    }
}


