package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ResetSignStatusWorker extends Worker {
    private SQLite dbHelper;

    public ResetSignStatusWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.dbHelper = new SQLite(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            // 假設我們要重置所有用戶的簽到狀態
            // 如果只需要重置特定用戶，您需要傳入 userId 作為 Worker 的輸入參數
            Cursor cursor = db.rawQuery("SELECT user_id, sign_flag, sign_day FROM user", null);

            while (cursor.moveToNext()) {
                @SuppressLint("Range") int userId = cursor.getInt(cursor.getColumnIndex("user_id"));
                @SuppressLint("Range") int currentSignFlag = cursor.getInt(cursor.getColumnIndex("sign_flag"));
                @SuppressLint("Range") int currentSignDay = cursor.getInt(cursor.getColumnIndex("sign_day"));

                if (currentSignFlag == 1) {
                    ContentValues values = new ContentValues();
                    values.put("sign_flag", 0);

                    if (currentSignDay == 7) {
                        values.put("sign_day", 0);
                    }

                    // 更新資料庫
                    db.update("user", values, "user_id = ?", new String[]{String.valueOf(userId)});
                }
            }
            cursor.close();

            return Result.success();
        } catch (Exception e) {
            // 如果發生錯誤，返回失敗
            return Result.failure();
        } finally {
            // 關閉資料庫連接
            db.close();
        }
    }
}