package com.example.myapplication;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class Function {

    /**
     * 取得現在時間
     **/
    public String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        return sdf.format(new Date());
    }

    /**
     * 取得星期幾
     **/
    public int getDayofWeek(String currentTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        Date date;
        try {
            date = sdf.parse(currentTime);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (dayOfWeek == 0) {
            dayOfWeek = 7;
        }
        Log.v("day", String.valueOf(dayOfWeek));
        return dayOfWeek;
    }

    /**
     * 計算冷卻時間
     **/
    public int checkCD(String lastTime, String currentTime) {
        int isTimeOK;

        if (lastTime == null || lastTime.isEmpty()) {
            // 如果 lastTime 為 null 或空字符串，認為冷卻時間已過，可以打卡
            isTimeOK = 1;
        } else {
            Calendar dt1 = Calendar.getInstance();
            try {
                dt1.setTime(new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault()).parse(lastTime));
            } catch (ParseException e) {
                e.printStackTrace();
                // 解析失敗時，設置為允許打卡
                return 1;
            }
            long last = dt1.getTimeInMillis();

            Calendar dt2 = Calendar.getInstance();
            try {
                dt2.setTime(new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault()).parse(currentTime));
            } catch (ParseException e) {
                e.printStackTrace();
                // 解析失敗時，設置為允許打卡
                return 1;
            }
            long now = dt2.getTimeInMillis();

            // 檢查冷卻時間是否已過
            if (now - last >= 10800000) { // 3小時 (10800000 毫秒)
                isTimeOK = 1;
            } else {
                isTimeOK = 0;
            }
        }
        return isTimeOK;
    }


    /**
     * 計算距離
     **/
    public double CalculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double lat1Rad = Math.toRadians(lat1);
        double lng1Rad = Math.toRadians(lng1);
        double lat2Rad = Math.toRadians(lat2);
        double lng2Rad = Math.toRadians(lng2);
        double D_lat = lat2Rad - lat1Rad;
        double D_lng = lng2Rad - lng1Rad;
        double a = Math.pow(Math.sin(D_lat / 2), 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.pow(Math.sin(D_lng / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 6371 * c * 1000; // 公尺
    }

    public interface DialogCallback {
        void onDialogCompleted(int newDropNum, int count);
    }

    /**
     * 顯示打卡對話框
     **/
//    @SuppressLint({"SimpleDateFormat", "SetTextI18n"})
//    public void showDialog(Activity activity, int u_id, AtomicInteger dropNumAtomic, int count, int isTimeOK, String lastTime, String[][] businessTime, String currentTime, DialogCallback callback, int userId) {
//        int dropNum = dropNumAtomic.get();
//        Dialog dialog = new Dialog(activity);
//
//        if (isTimeOK == 1) { // 可以打卡
//            if (count == 1 || count == 2) {
//                dialog.setContentView(R.layout.repeat_checkin);
//
//                Button toWater = dialog.findViewById(R.id.button5);
//                toWater.setOnClickListener(view -> {
//                    // 更新 dropNum 並執行回調
//                    updateDropAndNotifyCallback(activity, u_id, dropNum, 10, callback, count);
//                    // 跳轉到 Tree Activity
//                    Intent main2ActivityIntent = new Intent(activity, Tree.class);
//                    main2ActivityIntent.putExtra("userId", userId);
//                    dialog.dismiss();
//                    activity.startActivity(main2ActivityIntent);
//                });
//
//                Button toMap = dialog.findViewById(R.id.button6);
//                toMap.setOnClickListener(view -> {
//                    // 更新 dropNum 並執行回調
//                    updateDropAndNotifyCallback(activity, u_id, dropNum, 10, callback, count);
//                    // 返回 MainActivity
//                    dialog.dismiss();
//                    activity.setResult(RESULT_OK);
//                    activity.finish(); // 結束當前 Activity，返回到 MainActivity
//                });
//
//            } else {
//                dialog.setContentView(R.layout.first_checkin);
//
//                Button toWater = dialog.findViewById(R.id.button5);
//                toWater.setOnClickListener(view -> {
//                    // 更新 dropNum 並執行回調
//                    updateDropAndNotifyCallback(activity, u_id, dropNum, 20, callback, count);
//                    // 跳轉到 Tree Activity
//                    Intent main2ActivityIntent = new Intent(activity, Tree.class);
//                    main2ActivityIntent.putExtra("userId", userId);
//                    dialog.dismiss();
//                    activity.startActivity(main2ActivityIntent);
//                });
//
//                Button toMap = dialog.findViewById(R.id.button6);
//                toMap.setOnClickListener(view -> {
//                    // 更新 dropNum 並執行回調
//                    updateDropAndNotifyCallback(activity, u_id, dropNum, 20, callback, count);
//                    // 返回 MainActivity
//                    dialog.dismiss();
//                    activity.setResult(RESULT_OK);
//                    activity.finish(); // 結束當前 Activity，返回到 MainActivity
//                });
//            }
//        } else if (isTimeOK == 0) { // 冷卻時間未到
//            dialog.setContentView(R.layout.tooquick);
//            TextView textView = dialog.findViewById(R.id.textView7);
//            Calendar dt = Calendar.getInstance();
//            try {
//                dt.setTime(Objects.requireNonNull(new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").parse(lastTime)));
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//            long next = dt.getTimeInMillis() + 10800000;
//            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
//            String nextTime = sdf.format(new Date(next));
//
//            textView.setText(nextTime.split(" ")[1]);
//
//            Button toMap = dialog.findViewById(R.id.button6);
//            toMap.setOnClickListener(view -> {
//                dialog.dismiss();
//                if (callback != null) {
//                    callback.onDialogCompleted(dropNum, count);
//                }
//            });
//        } else if (isTimeOK == -1) { // 非營業時間
//            dialog.setContentView(R.layout.not_time);
//            TextView textView = dialog.findViewById(R.id.textView7);
//            String[] str = nextOpenTime(businessTime, currentTime);
//            textView.setText(str[0] + " " + str[1]);
//            Button toMap = dialog.findViewById(R.id.button6);
//            toMap.setOnClickListener(view -> {
//                dialog.dismiss();
//                if (callback != null) {
//                    callback.onDialogCompleted(dropNum, count);
//                }
//            });
//        } else { // 距離太遠
//            dialog.setContentView(R.layout.toofar);
//
//            Button toMap = dialog.findViewById(R.id.button6);
//            toMap.setOnClickListener(view -> {
//                dialog.dismiss();
//                if (callback != null) {
//                    callback.onDialogCompleted(dropNum, count);
//                }
//            });
//        }
//
//        dialog.show();
//    }
    @SuppressLint({"SimpleDateFormat", "SetTextI18n"})
    public void showDialog(Activity activity, int userId, AtomicInteger dropNumAtomic, int count, int isTimeOK, String lastTime, String[][] businessTime, String currentTime, DialogCallback callback) {
        int dropNum = dropNumAtomic.get();
        Dialog dialog = new Dialog(activity);

        if (isTimeOK == 1) { // 可以打卡
            if (count >= 1) {
                dialog.setContentView(R.layout.repeat_checkin);

                Button toWater = dialog.findViewById(R.id.button5);
                toWater.setOnClickListener(view -> {
                    // 更新 dropNum 並執行回調
                    updateDropAndNotifyCallback(activity, userId, dropNum, 10, callback, count);
                    // 跳轉到 Tree Activity
                    Intent main2ActivityIntent = new Intent(activity, Tree.class);
                    main2ActivityIntent.putExtra("userId", userId);
                    dialog.dismiss();
                    activity.startActivity(main2ActivityIntent);
                });

                Button toMap = dialog.findViewById(R.id.button6);
                toMap.setOnClickListener(view -> {
                    // 更新 dropNum 並執行回調
                    updateDropAndNotifyCallback(activity, userId, dropNum, 10, callback, count);
                    // 跳轉到 MainActivity
                    Intent mainActivityIntent = new Intent(activity, MainActivity.class);
                    mainActivityIntent.putExtra("userId", userId);
                    dialog.dismiss();
                    activity.startActivity(mainActivityIntent);
                });

            } else {
                dialog.setContentView(R.layout.first_checkin);

                Button toWater = dialog.findViewById(R.id.button5);
                toWater.setOnClickListener(view -> {
                    // 更新 dropNum 並執行回調
                    updateDropAndNotifyCallback(activity, userId, dropNum, 20, callback, count);
                    // 跳轉到 Tree Activity
                    Intent main2ActivityIntent = new Intent(activity, Tree.class);
                    main2ActivityIntent.putExtra("userId", userId);
                    dialog.dismiss();
                    activity.startActivity(main2ActivityIntent);
                });

                Button toMap = dialog.findViewById(R.id.button6);
                toMap.setOnClickListener(view -> {
                    // 更新 dropNum 並執行回調
                    updateDropAndNotifyCallback(activity, userId, dropNum, 20, callback, count);
                    // 跳轉到 MainActivity
                    Intent mainActivityIntent = new Intent(activity, MainActivity.class);
                    mainActivityIntent.putExtra("userId", userId);
                    dialog.dismiss();
                    activity.startActivity(mainActivityIntent);
                });
            }
        } else if (isTimeOK == 0) { // 冷卻時間未到
            dialog.setContentView(R.layout.tooquick);
            TextView textView = dialog.findViewById(R.id.textView7);
            Calendar dt = Calendar.getInstance();
            try {
                dt.setTime(Objects.requireNonNull(new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").parse(lastTime)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            long next = dt.getTimeInMillis() + 10800000;
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
            String nextTime = sdf.format(new Date(next));

            textView.setText(nextTime.split(" ")[1]);

            Button toMap = dialog.findViewById(R.id.button6);
            toMap.setOnClickListener(view -> {
                dialog.dismiss();
                if (callback != null) {
                    callback.onDialogCompleted(dropNum, count);
                }
                // 跳轉到 MainActivity
                Intent mainActivityIntent = new Intent(activity, MainActivity.class);
                mainActivityIntent.putExtra("userId", userId);
                activity.startActivity(mainActivityIntent);
            });
        } else if (isTimeOK == -1) { // 非營業時間
            dialog.setContentView(R.layout.not_time);
            TextView textView = dialog.findViewById(R.id.textView7);
            String[] str = nextOpenTime(businessTime, currentTime);
            textView.setText(str[0] + " " + str[1]);
            Button toMap = dialog.findViewById(R.id.button6);
            toMap.setOnClickListener(view -> {
                dialog.dismiss();
                if (callback != null) {
                    callback.onDialogCompleted(dropNum, count);
                }
                // 跳轉到 MainActivity
                Intent mainActivityIntent = new Intent(activity, MainActivity.class);
                mainActivityIntent.putExtra("userId", userId);
                activity.startActivity(mainActivityIntent);
            });
        } else { // 距離太遠
            dialog.setContentView(R.layout.toofar);

            Button toMap = dialog.findViewById(R.id.button6);
            toMap.setOnClickListener(view -> {
                dialog.dismiss();
                if (callback != null) {
                    callback.onDialogCompleted(dropNum, count);
                }
                // 跳轉到 MainActivity
                Intent mainActivityIntent = new Intent(activity, MainActivity.class);
                mainActivityIntent.putExtra("userId", userId);
                activity.startActivity(mainActivityIntent);
            });
        }

        dialog.show();
    }


    private void updateDropAndNotifyCallback(Activity activity, int u_id, int dropNum, int increment, DialogCallback callback, int count) {
        SQLite dbHelper = new SQLite(activity);
        int[] userData = dbHelper.getUserData(u_id);
        int newDropNum = userData[3] + increment; // userData[3] 是 drop_num
        dbHelper.updateUserData(u_id, null, userData[1], userData[2], newDropNum, userData[4]);
        callback.onDialogCompleted(newDropNum, count);
    }

    /**
     * 判斷是否營業
     **/
    public int isOpen(String businessTime, String currentTime) {
        int isopen = 0;
        if (!businessTime.equals("closed")) {
            String[] open = businessTime.split(", ");
            for (String open_time : open) {
                int i = 0;
                String[] eachtime = open_time.split("-");
                String startTime = currentTime.split(" ")[0] + " " + eachtime[0];
                String closedTime = currentTime.split(" ")[0] + " " + eachtime[1];
                Log.e("Time4", eachtime[i]);
                Log.e("Time5", startTime);
                Log.e("Time6", closedTime);

                Calendar dt1 = Calendar.getInstance();
                try {
                    dt1.setTime(new SimpleDateFormat("yyyy.MM.dd HH:mm").parse(startTime));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                long start = dt1.getTimeInMillis();
                Log.e("Time7", String.valueOf(start));

                Calendar dt2 = Calendar.getInstance();
                try {
                    dt2.setTime(new SimpleDateFormat("yyyy.MM.dd HH:mm").parse(closedTime));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                long closed = dt2.getTimeInMillis();
                Log.e("Time8", String.valueOf(closed));

                Calendar dt3 = Calendar.getInstance();
                try {
                    dt3.setTime(new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").parse(currentTime));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                long now = dt3.getTimeInMillis();
                Log.e("Time9", String.valueOf(now));

                if (start <= now && now <= closed) {
                    isopen = 1;
                    return isopen;
                }
                i++;
            }
        }

        return isopen;
    }

    /**
     * 取得下次營業時間
     **/
    public String[] nextOpenTime(String[][] businessTime, String currentTime) {
        String[] result = new String[2];
        if (businessTime[6][1].equals("closed")) {
            int index = 0;
            while (businessTime[index][1].equals("closed")) {
                index++;
            }
            result[0] = businessTime[index][0];
            result[1] = businessTime[index][1];
        } else {
            String[] endTimeList = businessTime[6][1].split("-");
            String endTime = currentTime.split(" ")[0] + " " + endTimeList[endTimeList.length - 1];
            Calendar dt1 = Calendar.getInstance();
            try {
                dt1.setTime(new SimpleDateFormat("yyyy.MM.dd HH:mm").parse(endTime));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            long end = dt1.getTimeInMillis();

            Calendar dt2 = Calendar.getInstance();
            try {
                dt2.setTime(new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").parse(currentTime));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            long now = dt2.getTimeInMillis();

            if (now > end) {
                int index = 0;
                while (businessTime[index][1].equals("closed")) {
                    index++;
                }
                result[0] = businessTime[index][0];
                result[1] = businessTime[index][1];
            } else {
                result = businessTime[6];
            }
        }
        String[] days = {"週一", "週二", "週三", "週四", "週五", "週六", "週日"};
        result[0] = days[Integer.parseInt(result[0]) - 1];
        return result;
    }

    /**
     * 計算已輸入字數
     **/
    public void totalWord(EditText editText, TextView counterTextView, int maxLength) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 在文字變化之前執行
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int inputLength = s.toString().length();
                counterTextView.setText(inputLength + " / " + maxLength);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 在文字變化之後執行
            }
        });
    }

    /**
     * 取得樹苗各階段最大值
     **/
    public int getBarMax(int treeStage, int treeBar) {
        int maxBar = 0;
        switch (treeStage) {
            case 0:
                maxBar = 200;
                break;
            case 1:
                if (treeBar == 200) {
                    maxBar = 500;
                } else {
                    maxBar = 200;
                }
                break;
            case 2:
                if (treeBar == 500) {
                    maxBar = 1000;
                } else {
                    maxBar = 500;
                }
                break;
            case 3:
                if (treeBar == 1000) {
                    maxBar = 2000;
                } else {
                    maxBar = 1000;
                }
                break;
            case 4:
                maxBar = 2000;
                break;
            default:
                throw new IllegalArgumentException("Invalid tree stage: " + treeStage);
        }
        return maxBar;
    }

    /**
     * 顯示任務列表
     **/
    public int[] show_taskDialog(Activity activity, int user_id, AtomicIntegerArray userAtomic, TextView dropNum, ImageButton taskBtn) {
        new Thread(() -> {
            activity.runOnUiThread(() -> {
                Dialog dialog = new Dialog(activity);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 不顯示標題
                dialog.setContentView(R.layout.checkin_days);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                ImageButton[] buttons = new ImageButton[7];
                buttons[0] = dialog.findViewById(R.id.day1);
                buttons[1] = dialog.findViewById(R.id.day2);
                buttons[2] = dialog.findViewById(R.id.day3);
                buttons[3] = dialog.findViewById(R.id.day4);
                buttons[4] = dialog.findViewById(R.id.day5);
                buttons[5] = dialog.findViewById(R.id.day6);
                buttons[6] = dialog.findViewById(R.id.day7);

                int[] multiple = {10, 8, 6, 4, 2, 10, 8, 6, 4, 2, 10, 8, 6, 4, 2, 10, 8, 6, 4, 2};

                for (int i = 0; i < buttons.length; i++) {
                    final int index = i; // 在內部類中使用這個變量
                    String checkinDay = "day" + (i + 1) + "_finish";
                    int checkinDayResourceId = activity.getResources().getIdentifier(checkinDay, "drawable", activity.getPackageName());

                    if (i < userAtomic.get(1)) { // 禁用已經簽到過的天數按鈕
                        if (checkinDayResourceId != 0) {
                            buttons[i].setImageResource(checkinDayResourceId);
                            buttons[i].setClickable(false);
                            buttons[i].setEnabled(false);
                        } else {
                            Log.e("ResourceError", "Resource not found for " + checkinDay);
                        }
                    } else if (i == userAtomic.get(1)) { // 今天的簽到按鈕
                        if (userAtomic.get(2) != 1) { // 今天還沒簽到
                            buttons[i].setOnClickListener(view -> {
                                if (index == 6) {
                                    show_wheelDialog(activity, multiple, user_id, userAtomic.get(0), userAtomic.get(1), dropNum, taskBtn, buttons, (newDropNum, new_signFlag, new_signDay, isSpin) -> {
                                        new Thread(() -> {
                                            SQLite dbHelper = new SQLite(activity);
                                            dbHelper.updateCheckState(user_id, userAtomic.get(1));
                                            userAtomic.set(0, newDropNum);
                                            userAtomic.set(1, new_signDay);
                                            userAtomic.set(2, new_signFlag);

                                            activity.runOnUiThread(() -> {
                                                dropNum.setText(String.valueOf(newDropNum));
                                                if (isSpin == 1) {
                                                    buttons[index].setImageResource(checkinDayResourceId);
                                                    buttons[index].setClickable(false); // 簽到後禁用按鈕
                                                }
                                            });
                                        }).start();
                                    });
                                } else {
                                    new Thread(() -> {
                                        SQLite dbHelper = new SQLite(activity);
                                        dbHelper.updateCheckState(user_id, userAtomic.get(1));
                                        dbHelper.updateSignDrop(user_id, 1);

                                        userAtomic.set(2, 1); // 把簽到狀態改成已簽到
                                        userAtomic.set(1, userAtomic.get(1) + 1); // 把簽到天數 + 1

                                        activity.runOnUiThread(() -> {
                                            userAtomic.set(0, userAtomic.get(0) + 5);
                                            dropNum.setText(String.valueOf(userAtomic.get(0)));
                                            taskBtn.setImageResource(R.drawable.task_finish);
                                        });

                                    }).start();
                                    show_SignCompletedDialog(activity);
                                    buttons[index].setImageResource(checkinDayResourceId);
                                    buttons[index].setClickable(false); // 簽到後禁用按鈕
                                }

                            });
                        } else { // 今天已經簽到過
                            buttons[i].setOnClickListener(view -> {
                                Log.d("tomorrowCheckin", "今天已經簽到過了");
                                show_SignCompletedDialog(activity);
                            });
                        }
                    } else { // 禁用其他未來的按鈕
                        buttons[i].setOnClickListener(view -> {
                            Log.d("tomorrowCheckin", "今天已經簽到過了");
                            show_SignCompletedDialog(activity);
                        });
                    }
                }

                Button newCheckin = dialog.findViewById(R.id.newCheckin);
                newCheckin.setOnClickListener(view -> {
                    Intent main2ActivityIntent = new Intent(activity, MainActivity.class); // 利用 Intent 切換頁面
                    activity.startActivity(main2ActivityIntent);
                });

                Button repeatCheckin = dialog.findViewById(R.id.repeatCheckin);
                repeatCheckin.setOnClickListener(view -> {
                    Intent main2ActivityIntent = new Intent(activity, user.class); // 利用 Intent 切換頁面
                    activity.startActivity(main2ActivityIntent);
                });

                ImageButton closeBtn = dialog.findViewById(R.id.closeBtn);
                closeBtn.setOnClickListener(view -> dialog.dismiss());

                // 設置對話框的位置和佈局參數
                Window window = dialog.getWindow();
                if (window != null) {
                    WindowManager.LayoutParams params = window.getAttributes();
                    params.width = WindowManager.LayoutParams.MATCH_PARENT;
                    params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                    params.gravity = Gravity.BOTTOM; // 可根據需要設置位置
                    window.setAttributes(params);
                }

                dialog.show();
            });
        }).start();
        int[] needUpdateThing = {userAtomic.get(0), userAtomic.get(1), userAtomic.get(2)};

        return needUpdateThing;
    }

    /**
     * 顯示完成簽到視窗
     **/
    public void show_SignCompletedDialog(Activity activity) {
        Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.today_checkin_complete);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        Button backBtn = dialog.findViewById(R.id.backBtn);
        backBtn.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
    }

    public interface WheelDialogCallback {
        void onDialogCompleted(int newDropNum, int new_signFlag, int new_signDay, int isSpin);
    }

    /**
     * 顯示轉盤
     **/
    public void show_wheelDialog(Activity activity, int[] multiple, int user_id, int last_dropNum, int signDay, TextView dropNum, ImageButton taskBtn, ImageButton[] buttons, WheelDialogCallback callback) {
        Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.wheel_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        ImageView wheelImageView = dialog.findViewById(R.id.wheelImageView);
        ImageButton spinButton = dialog.findViewById(R.id.spinButton);
        boolean[] isSpinning = {false}; // 使用 array 包裝 boolean 以便在內部類中修改

        spinButton.setOnClickListener(view -> {
            buttons[6].setImageResource(R.drawable.day7_finish);
            buttons[6].setClickable(false); // 簽到後禁用按鈕
            if (!isSpinning[0]) {
                isSpinning[0] = true;
                int isSpin = 1;
                Random random = new Random();
                List<Integer> excludedAngles = new ArrayList<>();

                int angle = 0;
                while (angle <= 360) { // 排除角度，避免在兩個區域的正中間
                    excludedAngles.add(angle);
                    angle = angle + (360 / multiple.length);
                }

                int degree = generateRandomDegree(random, excludedAngles) + 3600; // 隨機旋轉 360-3960 度
                Log.v("degree", "degree：" + degree);

                ObjectAnimator rotateWheel = ObjectAnimator.ofFloat(wheelImageView, "rotation", 0f, degree);
                rotateWheel.setDuration(3000); //轉三秒
                rotateWheel.setInterpolator(new AccelerateDecelerateInterpolator());
                rotateWheel.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        spinButton.setEnabled(false);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        isSpinning[0] = false;
                        spinButton.setEnabled(true);

                        int selectedItemIndex = calculateSelectedItemIndex(degree, multiple);
                        Log.v("selectedItemIndex", "selectedItemIndex：" + selectedItemIndex);
                        int selectedMultiple = multiple[selectedItemIndex];
                        int new_dropNum = show_wheelWaterDialog(activity, selectedMultiple, dialog, user_id, last_dropNum, dropNum, taskBtn);
                        int new_signDay = signDay + 1;
                        int new_signFlag = 1;
                        callback.onDialogCompleted(new_dropNum, new_signFlag, new_signDay, isSpin);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                });

                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playSequentially(rotateWheel);

                animatorSet.start();
            }
        });

        dialog.show();

        ImageButton closeBtn = dialog.findViewById(R.id.closeBtn);
        closeBtn.setOnClickListener(view -> dialog.dismiss());
    }

    /**
     * 計算排除的轉盤角度
     **/
    public int generateRandomDegree(Random random, List<Integer> excludedAngles) {
        int degree;
        do {
            degree = random.nextInt(360); // 生成0到359之間的隨機數
        } while (excludedAngles.contains(degree)); // 如果生成的數字在排除列表中，則重新生成

        return degree;
    }

    /**
     * 取得角度對應的 index
     **/
    public int calculateSelectedItemIndex(int degree, int[] multiple) {
        int itemAngle = 360 / multiple.length;
        int index = degree % 360 / itemAngle;
        return index;
    }

    /**
     * 顯示領取轉盤水滴視窗
     **/
    public int show_wheelWaterDialog(Activity activity, int multiple, Dialog wheelDialog, int user_id, int last_dropNum, TextView dropNum, ImageButton taskBtn) {
        Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.reward_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView rewardDrop = dialog.findViewById(R.id.rewardDrop);
        rewardDrop.setText(multiple * 5 + "滴水滴");

        Button receiveBtn = dialog.findViewById(R.id.receiveBtn);
        receiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dropNum.setText(String.valueOf((last_dropNum + multiple * 5)));
                taskBtn.setImageResource(R.drawable.task_finish);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SQLite dbHelper = new SQLite(activity);
                        dbHelper.updateSignDrop(user_id, multiple);
                    }
                }).start();
                dialog.dismiss();
                wheelDialog.dismiss();
            }
        });
        dialog.show();
        return last_dropNum + multiple * 5;
    }

    public interface DonateDialogCallback {
        void onDialogCompleted(int newTreeId, int newTreeBar, int newTreeStage);
    }

    /**
     * 顯示捐贈視窗
     **/
    public void show_donateDialog(Activity activity, int tree_id, int user_id, Button harvestBtn, ImageButton waterBtn, ImageButton taskBtn, int sign_flag, DonateDialogCallback callback) {
        Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.complete_tree_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        //匿名捐贈
        Button anonymousBtn = dialog.findViewById(R.id.anonymous);
        anonymousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                harvestBtn.setVisibility(View.GONE);
                waterBtn.setEnabled(true);
                waterBtn.setImageResource(R.drawable.sprinkler);
                taskBtn.setEnabled(true);
                if (sign_flag == 1) { //判斷簽到狀態確認是否顯示任務紅點
                    taskBtn.setImageResource(R.drawable.task_finish);
                } else {
                    taskBtn.setImageResource(R.drawable.task);
                }

                SQLite dbHelper = new SQLite(activity);
                dbHelper.addTree(user_id, 0, 0); // 新增一棵樹
                int[] newTreeData = dbHelper.getTreeData(user_id);
                callback.onDialogCompleted(newTreeData[0], 0, 0);

                dialog.setContentView(R.layout.complete_finally_dialog);

                Button toWater = dialog.findViewById(R.id.toWater);
                toWater.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                Button toMap = dialog.findViewById(R.id.toMap);
                toMap.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent main2ActivityIntent = new Intent(activity, MainActivity.class); //利用Intent 切換頁面(現在的class檔案,要切換的class檔案)
                        activity.startActivity(main2ActivityIntent);
                    }
                });
            }
        });

        //實名捐贈
        Button registeredBtn = dialog.findViewById(R.id.registered);
        registeredBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.setContentView(R.layout.complete_writename_dialog);
                EditText editText = dialog.findViewById(R.id.dialog_input);
                final TextView counterTextView = dialog.findViewById(R.id.counter_text_view);
                final int maxLength = 13;
                totalWord(editText, counterTextView, maxLength);

                Button saveBtn = dialog.findViewById(R.id.saveBtn);
                saveBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String donateName = editText.getText().toString().trim();
                        boolean isValidInput = isValidInput(donateName);
                        Log.v("isValidInput", String.valueOf(isValidInput));
                        if (!donateName.isEmpty()) {
                            if (isValidInput == true) {
                                harvestBtn.setVisibility(View.GONE);
                                waterBtn.setEnabled(true);
                                waterBtn.setImageResource(R.drawable.sprinkler);
                                taskBtn.setEnabled(true);
                                if (sign_flag == 1) { //判斷簽到狀態確認是否顯示任務紅點
                                    taskBtn.setImageResource(R.drawable.task_finish);
                                } else {
                                    taskBtn.setImageResource(R.drawable.task);
                                }
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        SQLite dbHelper = new SQLite(activity);
                                        dbHelper.registeredDonate(tree_id, donateName, getCurrentTime());
                                        dbHelper.addTree(user_id, 0, 0);
                                        callback.onDialogCompleted(tree_id + 1, 0, 0);
                                    }
                                }).start();

                                dialog.setContentView(R.layout.complete_finally_dialog);
                                Button toWater = dialog.findViewById(R.id.toWater);
                                toWater.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dialog.dismiss();
                                    }
                                });

                                Button toMap = dialog.findViewById(R.id.toMap);
                                toMap.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent main2ActivityIntent = new Intent(activity, MainActivity.class); //利用Intent 切換頁面(現在的class檔案,要切換的class檔案)
                                        activity.startActivity(main2ActivityIntent);
                                    }
                                });
                            } else {
                                show_inputNameError(activity);
                            }

                        } else {
                            show_inputNameEmpty(activity);
                        }

                    }
                });

                Button cancelBtn = dialog.findViewById(R.id.cancelBtn);
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
            }
        });
        dialog.show();
    }

    /**
     * 判斷樹苗圖片大小和位置
     **/
    public int[] treeViewSize(int treeStage) {
        int size[] = new int[5];
        int width = 0;
        int height = 0;
        int marginStart = 0;
        int marginTop = 0;
        int marginBottom = 0;

        switch (treeStage) {
            case 0:
                break;
            case 1: // tree1
                width = 112;
                height = 125;
                marginStart = 12;
                marginTop = 40;
                marginBottom = 12;
                break;
            case 2: // tree2
                width = 225;
                height = 225;
                marginStart = 12;
                marginTop = 0;
                marginBottom = 50;
                break;
            case 3: // tree3
                width = 325;
                height = 350;
                marginStart = 12;
                marginTop = 0;
                marginBottom = 175;
                break;
            case 4: // tree4
                width = 500;
                height = 550;
                marginStart = 12;
                marginTop = 0;
                marginBottom = 300;
                break;
            case 5: // tree5
                width = 625;
                height = 625;
                marginStart = 12;
                marginTop = 0;
                marginBottom = 375;
                break;
            default:
                throw new IllegalArgumentException("Invalid tree stage: " + treeStage);
        }

        size[0] = width;
        size[1] = height;
        size[2] = marginStart;
        size[3] = marginTop;
        size[4] = marginBottom;

        return size;
    }

    /**
     * 判斷澆水後的樹苗階段
     **/
    public int getNewTreeStage(int treeBar) {
        int newStage = 0;

        if (treeBar == 0) {
            newStage = 0;
        } else if (treeBar > 0 && treeBar <= 200) {
            newStage = 1;
        } else if (treeBar > 200 && treeBar <= 500) {
            newStage = 2;
        } else if (treeBar > 500 && treeBar <= 1000) {
            newStage = 3;
        } else if (treeBar > 1000 && treeBar <= 1999) {
            newStage = 4;
        } else if (treeBar == 2000) {
            newStage = 5;
        }

        return newStage;
    }

    /**
     * 顯示感謝狀
     **/
    public void show_appreciateDialog(Activity activity, int donateNum) {
        Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.appreciate_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        TextView Amount = dialog.findViewById(R.id.Amount);
        int amount = donateNum * 100;

        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
        String formattedAmount = numberFormat.format(amount);

        // 設置格式化後的文本
        Amount.setText(formattedAmount);

        ImageButton closeBtn = dialog.findViewById(R.id.closeBtn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    /**
     * 顯示沒有輸入捐贈名稱視窗
     **/
    public void show_inputNameEmpty(Activity activity) {
        Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.input_name_empty);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        Button backBtn = dialog.findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * 顯示不能輸入特殊文字視窗
     **/
    public void show_inputNameError(Activity activity) {
        Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.input_name_error);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        Button backBtn = dialog.findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * 判斷輸入是否只為中英數字
     **/
    public boolean isValidInput(String input) {
        // 正則表達式：^[\u4e00-\u9fa5a-zA-Z0-9]+$，^ 和 $ 表示匹配整個字串，[\u4e00-\u9fa5] 代表所有中文字，[a-zA-Z0-9] 所有英数字
        String regex = "^[\u4e00-\u9fa5a-zA-Z0-9]+$";
        return input.matches(regex);
    }
}

