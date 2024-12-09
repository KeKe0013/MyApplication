package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.ImageViewScrolling.IEventEnd;
import com.example.myapplication.ImageViewScrolling.ImageViewScrolling;
import com.example.myapplication.ImageViewScrolling.Util;

import java.util.ArrayList;
import java.util.Random;

public class Tag extends AppCompatActivity implements IEventEnd {
    private LinearLayout buttonLayout;
    private ImageButton list, map, tree, user, foodWheel;;
    private SQLite dbHelper;
    private int userId;
    double user_lat;
    double user_lng;

    private ImageView btn_down, btn_up, slotMachine;
    private int width, height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag);

        userId = getIntent().getIntExtra("userId", 0);
        user_lat = getIntent().getDoubleExtra("user_lat", 0);
        user_lng = getIntent().getDoubleExtra("user_lng", 0);


        list = findViewById(R.id.list);
        map = findViewById(R.id.map);
        tree = findViewById(R.id.tree);
        user = findViewById(R.id.user);

        foodWheel = findViewById(R.id.food_slotmachine);
        foodWheel.setOnClickListener(view -> show_slotMachineDialog(Tag.this));

        setupNavigationButtons();

        dbHelper = new SQLite(this); // 初始化 SQLite 帮助类

        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<SQLite.AllTag> allTag = dbHelper.getAllTag();
                buttonLayout = findViewById(R.id.button_layout);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        addButtonsToLayout(allTag);
                    }
                });
            }
        }).start();

        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        width = display.getWidth();
        height = display.getHeight();
    }

    private void setupNavigationButtons() {
        list.setOnClickListener(view -> navigateTo(list.class));
        map.setOnClickListener(view -> navigateTo(MainActivity.class));
        tree.setOnClickListener(view -> navigateTo(Tree.class));
        user.setOnClickListener(view -> navigateTo(user.class));
    }

    private void navigateTo(Class<?> cls) {
        Intent intent = new Intent(Tag.this, cls);
        intent.putExtra("userId", userId);
        intent.putExtra("user_lat", user_lat);
        intent.putExtra("user_lng", user_lng);

        startActivity(intent);
    }

    private void addButtonsToLayout(ArrayList<SQLite.AllTag> allTag) {
        int columnCount = 4; // 每行4個按鈕
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int buttonWidth = (screenWidth - 32 * (columnCount + 1)) / columnCount; // 考慮邊距
        int buttonHeight = buttonWidth; // 設置按鈕為正方形

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(buttonWidth, buttonHeight);
        params.setMargins(16, 16, 16, 16); // 設置按鈕之間的間距

        LinearLayout rowLayout = new LinearLayout(this);
        rowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        int btnCount = 0;
        for (SQLite.AllTag tag : allTag) {
            Button tagBtn = new Button(this);
            tagBtn.setLayoutParams(params);

            // 使用新的 setupTagButton 方法
            setupTagButton(tag, tagBtn);

            tagBtn.setText(tag.getTag_name());
            tagBtn.setTypeface(null, Typeface.BOLD);
            tagBtn.setBackgroundResource(R.drawable.tag_button);

            // 設置文字大小和行數
            tagBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12); // 調整文字大小
            tagBtn.setLines(2);
            tagBtn.setEllipsize(TextUtils.TruncateAt.END);

            tagBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent resultActivityIntent = new Intent(Tag.this, result.class);
                    resultActivityIntent.putExtra("button_text", tag.getTag_name());
                    resultActivityIntent.putExtra("tag_id", tag.getTag_id());
                    resultActivityIntent.putExtra("clickNum", tag.getClickNum());
                    resultActivityIntent.putExtra("userId", userId);
                    resultActivityIntent.putExtra("user_lat", user_lat);
                    resultActivityIntent.putExtra("user_lng", user_lng);
                    startActivity(resultActivityIntent);
                    Log.v("click", tag.getTag_name() + Integer.toString(tag.getTag_id()));
                }
            });

            rowLayout.addView(tagBtn);
            btnCount++;

            if (btnCount == columnCount) {
                buttonLayout.addView(rowLayout);
                rowLayout = new LinearLayout(this);
                rowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                btnCount = 0;
            }
        }

        // 添加最後一行，並填充空白按鈕
        if (btnCount > 0) {
            for (int i = btnCount; i < columnCount; i++) {
                View spacer = new View(this);
                spacer.setLayoutParams(params);
                rowLayout.addView(spacer);
            }
            buttonLayout.addView(rowLayout);
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void setupTagButton(SQLite.AllTag tag, Button tagBtn) {
        Resources resources = getResources();
        float density = resources.getDisplayMetrics().density;

        // Tag photo 設置
        String tagPhotoName = "category_" + tag.getTag_id();
        @SuppressLint("DiscouragedApi") int tagPhotoResourceId = resources.getIdentifier(tagPhotoName, "drawable", getPackageName());
        Drawable tagDrawable;

        if (tagPhotoResourceId != 0) {
            try {
                tagDrawable = resources.getDrawable(tagPhotoResourceId);
            } catch (Resources.NotFoundException e) {
                Log.w("TagList", "Tag photo not found: " + tagPhotoName, e);
                tagDrawable = resources.getDrawable(R.drawable.logonull);
            }
        } else {
            Log.w("TagList", "Tag photo resource not found: " + tagPhotoName);
            tagDrawable = resources.getDrawable(R.drawable.logonull);
        }

        // 調整 tag photo 大小
        int buttonWidth = tagBtn.getLayoutParams().width;
        int iconSize = buttonWidth / 2; // 圖標大小為按鈕寬度的一半

        Bitmap originalBitmap;
        if (tagDrawable instanceof BitmapDrawable) {
            originalBitmap = ((BitmapDrawable) tagDrawable).getBitmap();
        } else {
            originalBitmap = Bitmap.createBitmap(tagDrawable.getIntrinsicWidth(),
                    tagDrawable.getIntrinsicHeight(),
                    Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(originalBitmap);
            tagDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            tagDrawable.draw(canvas);
        }

        float scale = (float) iconSize / Math.min(originalBitmap.getWidth(), originalBitmap.getHeight());
        int scaledWidth = Math.round(originalBitmap.getWidth() * scale);
        int scaledHeight = Math.round(originalBitmap.getHeight() * scale);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, scaledWidth, scaledHeight, true);
        Drawable scaledDrawable = new BitmapDrawable(resources, scaledBitmap);

        // 設置 drawable 的邊界
        scaledDrawable.setBounds(0, 0, iconSize, iconSize);

        // 設置 button 的 drawable
        tagBtn.setCompoundDrawables(null, scaledDrawable, null, null);
    }

    /**顯示食物分類拉霸**/
    private void show_slotMachineDialog(Activity activity) {
        Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.food_slotmachine);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        // 設置 WindowManager.LayoutParams
        WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
        layoutParams.width = (int) width;
        layoutParams.height = (int) height;
        dialog.getWindow().setAttributes(layoutParams);

        dialog.show();

        // 配置 Dialog 中的視圖
        btn_down = dialog.findViewById(R.id.btn_down);
        btn_up = dialog.findViewById(R.id.btn_up);
        slotMachine = dialog.findViewById(R.id.slot_machine);

        ImageViewScrolling image = dialog.findViewById(R.id.image);
        image.setEventEnd(this);

        btn_up.setOnClickListener(view -> {
            btn_up.setVisibility(View.GONE);
            btn_down.setVisibility(View.VISIBLE);
            image.setValueRandom(new Random().nextInt(42), // 幾張圖片
                    new Random().nextInt((15 - 5) + 1) + 5);  // 5-15 旋轉次數
        });

        ImageButton close_btn = dialog.findViewById(R.id.close_btn);
        close_btn.setOnClickListener(view -> {
            dialog.dismiss();
        });

    }

    /**顯示拉霸結果**/
    @Override
    public void eventEnd(int result, int count) {
        runOnUiThread(() -> {
            if (btn_down != null) {
                btn_down.setVisibility(View.GONE);
            }
            if (btn_up != null) {
                btn_up.setVisibility(View.VISIBLE);
            }

            show_foodResultDialog(result);

        });
    }


    /**顯示食物拉霸結果視窗**/
    private void show_foodResultDialog(int result) {
        Dialog dialog = new Dialog(Tag.this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        switch (result) {
            case Util.CHINESE:
                dialog.setContentView(R.layout.category_1);
                dialog.show();
                Button confirm_btn1 = dialog.findViewById(R.id.button);
                confirm_btn1.setOnClickListener(v -> {
                    Intent resultActivityIntent = new Intent(Tag.this, result.class);
                    resultActivityIntent.putExtra("button_text", "中式");
                    resultActivityIntent.putExtra("tag_id", 1);
                    resultActivityIntent.putExtra("clickNum", dbHelper.getClickNumByTagId(1));
                    resultActivityIntent.putExtra("userId", userId);
                    resultActivityIntent.putExtra("user_lat", user_lat);
                    resultActivityIntent.putExtra("user_lng", user_lng);
                    startActivity(resultActivityIntent);
                    Log.v("click", "中式");
                });
                Button again_btn1 = dialog.findViewById(R.id.button2);
                again_btn1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                break;
            case Util.JAPANESE:
                dialog.setContentView(R.layout.category_2);
                dialog.show();
                Button confirm_btn2 = dialog.findViewById(R.id.button);
                confirm_btn2.setOnClickListener(v -> {
                    Intent resultActivityIntent = new Intent(Tag.this, result.class);
                    resultActivityIntent.putExtra("button_text", "日式");
                    resultActivityIntent.putExtra("tag_id", 2);
                    resultActivityIntent.putExtra("clickNum", dbHelper.getClickNumByTagId(2));
                    resultActivityIntent.putExtra("userId", userId);
                    resultActivityIntent.putExtra("user_lat", user_lat);
                    resultActivityIntent.putExtra("user_lng", user_lng);
                    startActivity(resultActivityIntent);
                    Log.v("click", "日式");
                });
                Button again_btn2 = dialog.findViewById(R.id.button2);
                again_btn2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                break;

            case Util.KOREAN:
                dialog.setContentView(R.layout.category_3);
                dialog.show();
                Button confirm_btn3 = dialog.findViewById(R.id.button);
                confirm_btn3.setOnClickListener(v -> {
                    Intent resultActivityIntent = new Intent(Tag.this, result.class);
                    resultActivityIntent.putExtra("button_text", "韓式");
                    resultActivityIntent.putExtra("tag_id", 3);
                    resultActivityIntent.putExtra("clickNum", dbHelper.getClickNumByTagId(3));
                    resultActivityIntent.putExtra("userId", userId);
                    resultActivityIntent.putExtra("user_lat", user_lat);
                    resultActivityIntent.putExtra("user_lng", user_lng);
                    startActivity(resultActivityIntent);
                    Log.v("click", "韓式");
                });
                Button again_btn3 = dialog.findViewById(R.id.button2);
                again_btn3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                break;
            case Util.HONGKONG:
                dialog.setContentView(R.layout.category_4);
                dialog.show();
                Button confirm_btn4 = dialog.findViewById(R.id.button);
                confirm_btn4.setOnClickListener(v -> {
                    Intent resultActivityIntent = new Intent(Tag.this, result.class);
                    resultActivityIntent.putExtra("button_text", "港式");
                    resultActivityIntent.putExtra("tag_id", 4);
                    resultActivityIntent.putExtra("clickNum", dbHelper.getClickNumByTagId(4));
                    resultActivityIntent.putExtra("userId", userId);
                    resultActivityIntent.putExtra("user_lat", user_lat);
                    resultActivityIntent.putExtra("user_lng", user_lng);
                    startActivity(resultActivityIntent);
                    Log.v("click", "港式");
                });
                Button again_btn4 = dialog.findViewById(R.id.button2);
                again_btn4.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                break;
            case Util.THAI:
                dialog.setContentView(R.layout.category_5);
                dialog.show();
                Button confirm_btn5 = dialog.findViewById(R.id.button);
                confirm_btn5.setOnClickListener(v -> {
                    Intent resultActivityIntent = new Intent(Tag.this, result.class);
                    resultActivityIntent.putExtra("button_text", "泰式");
                    resultActivityIntent.putExtra("tag_id", 5);
                    resultActivityIntent.putExtra("clickNum", dbHelper.getClickNumByTagId(5));
                    resultActivityIntent.putExtra("userId", userId);
                    resultActivityIntent.putExtra("user_lat", user_lat);
                    resultActivityIntent.putExtra("user_lng", user_lng);
                    startActivity(resultActivityIntent);
                    Log.v("click", "泰式");
                });
                Button again_btn5 = dialog.findViewById(R.id.button2);
                again_btn5.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                break;
            case Util.VIETNAMESE:
                dialog.setContentView(R.layout.category_6);
                dialog.show();
                Button confirm_btn6 = dialog.findViewById(R.id.button);
                confirm_btn6.setOnClickListener(v -> {
                    Intent resultActivityIntent = new Intent(Tag.this, result.class);
                    resultActivityIntent.putExtra("button_text", "越式");
                    resultActivityIntent.putExtra("tag_id", 6);
                    resultActivityIntent.putExtra("clickNum", dbHelper.getClickNumByTagId(6));
                    resultActivityIntent.putExtra("userId", userId);
                    resultActivityIntent.putExtra("user_lat", user_lat);
                    resultActivityIntent.putExtra("user_lng", user_lng);
                    startActivity(resultActivityIntent);
                    Log.v("click", "越式");
                });
                Button again_btn6 = dialog.findViewById(R.id.button2);
                again_btn6.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                break;
            case Util.NANYANG:
                dialog.setContentView(R.layout.category_7);
                dialog.show();
                Button confirm_btn7 = dialog.findViewById(R.id.button);
                confirm_btn7.setOnClickListener(v -> {
                    Intent resultActivityIntent = new Intent(Tag.this, result.class);
                    resultActivityIntent.putExtra("button_text", "南洋");
                    resultActivityIntent.putExtra("tag_id", 7);
                    resultActivityIntent.putExtra("clickNum", dbHelper.getClickNumByTagId(7));
                    resultActivityIntent.putExtra("userId", userId);
                    resultActivityIntent.putExtra("user_lat", user_lat);
                    resultActivityIntent.putExtra("user_lng", user_lng);
                    startActivity(resultActivityIntent);
                    Log.v("click", "南洋");
                });
                Button again_btn7 = dialog.findViewById(R.id.button2);
                again_btn7.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                break;
            case Util.AMERICAN:
                dialog.setContentView(R.layout.category_8);
                dialog.show();
                Button confirm_btn8 = dialog.findViewById(R.id.button);
                confirm_btn8.setOnClickListener(v -> {
                    Intent resultActivityIntent = new Intent(Tag.this, result.class);
                    resultActivityIntent.putExtra("button_text", "美式");
                    resultActivityIntent.putExtra("tag_id", 8);
                    resultActivityIntent.putExtra("clickNum", dbHelper.getClickNumByTagId(8));
                    resultActivityIntent.putExtra("userId", userId);
                    resultActivityIntent.putExtra("user_lat", user_lat);
                    resultActivityIntent.putExtra("user_lng", user_lng);
                    startActivity(resultActivityIntent);
                    Log.v("click", "美式");
                });
                Button again_btn8 = dialog.findViewById(R.id.button2);
                again_btn8.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                break;
            case Util.ITALIAN:
                dialog.setContentView(R.layout.category_9);
                dialog.show();
                Button confirm_btn9 = dialog.findViewById(R.id.button);
                confirm_btn9.setOnClickListener(v -> {
                    Intent resultActivityIntent = new Intent(Tag.this, result.class);
                    resultActivityIntent.putExtra("button_text", "義式");
                    resultActivityIntent.putExtra("tag_id", 9);
                    resultActivityIntent.putExtra("clickNum", dbHelper.getClickNumByTagId(9));
                    resultActivityIntent.putExtra("userId", userId);
                    resultActivityIntent.putExtra("user_lat", user_lat);
                    resultActivityIntent.putExtra("user_lng", user_lng);
                    startActivity(resultActivityIntent);
                    Log.v("click", "義式");
                });
                Button again_btn9 = dialog.findViewById(R.id.button2);
                again_btn9.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                break;
            case Util.FEATURE_MEAL:
                dialog.setContentView(R.layout.category_10);
                dialog.show();
                Button confirm_btn10 = dialog.findViewById(R.id.button);
                confirm_btn10.setOnClickListener(v -> {
                    Intent resultActivityIntent = new Intent(Tag.this, result.class);
                    resultActivityIntent.putExtra("button_text", "特色菜");
                    resultActivityIntent.putExtra("tag_id", 10);
                    resultActivityIntent.putExtra("clickNum", dbHelper.getClickNumByTagId(10));
                    resultActivityIntent.putExtra("userId", userId);
                    resultActivityIntent.putExtra("user_lat", user_lat);
                    resultActivityIntent.putExtra("user_lng", user_lng);
                    startActivity(resultActivityIntent);
                    Log.v("click", "特色菜");
                });
                Button again_btn10 = dialog.findViewById(R.id.button2);
                again_btn10.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                break;
            case Util.BRUNCH:
                dialog.setContentView(R.layout.category_11);
                dialog.show();
                Button confirm_btn11 = dialog.findViewById(R.id.button);
                confirm_btn11.setOnClickListener(v -> {
                    Intent resultActivityIntent = new Intent(Tag.this, result.class);
                    resultActivityIntent.putExtra("button_text", "早午餐");
                    resultActivityIntent.putExtra("tag_id", 11);
                    resultActivityIntent.putExtra("clickNum", dbHelper.getClickNumByTagId(11));
                    resultActivityIntent.putExtra("userId", userId);
                    resultActivityIntent.putExtra("user_lat", user_lat);
                    resultActivityIntent.putExtra("user_lng", user_lng);
                    startActivity(resultActivityIntent);
                    Log.v("click", "早午餐");
                });
                Button again_btn11 = dialog.findViewById(R.id.button2);
                again_btn11.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                break;
            case Util.AFTERNOON_TEA:
                dialog.setContentView(R.layout.category_12);
                dialog.show();
                Button confirm_btn12 = dialog.findViewById(R.id.button);
                confirm_btn12.setOnClickListener(v -> {
                    Intent resultActivityIntent = new Intent(Tag.this, result.class);
                    resultActivityIntent.putExtra("button_text", "下午茶");
                    resultActivityIntent.putExtra("tag_id", 12);
                    resultActivityIntent.putExtra("clickNum", dbHelper.getClickNumByTagId(12));
                    resultActivityIntent.putExtra("userId", userId);
                    resultActivityIntent.putExtra("user_lat", user_lat);
                    resultActivityIntent.putExtra("user_lng", user_lng);
                    startActivity(resultActivityIntent);
                    Log.v("click", "下午茶");
                });
                Button again_btn12 = dialog.findViewById(R.id.button2);
                again_btn12.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                break;
            case Util.LIGHTFOOD:
                dialog.setContentView(R.layout.category_13);
                dialog.show();
                Button confirm_btn13 = dialog.findViewById(R.id.button);
                confirm_btn13.setOnClickListener(v -> {
                    Intent resultActivityIntent = new Intent(Tag.this, result.class);
                    resultActivityIntent.putExtra("button_text", "輕食");
                    resultActivityIntent.putExtra("tag_id", 13);
                    resultActivityIntent.putExtra("clickNum", dbHelper.getClickNumByTagId(13));
                    resultActivityIntent.putExtra("userId", userId);
                    resultActivityIntent.putExtra("user_lat", user_lat);
                    resultActivityIntent.putExtra("user_lng", user_lng);
                    startActivity(resultActivityIntent);
                    Log.v("click", "輕食");
                });
                Button again_btn13 = dialog.findViewById(R.id.button2);
                again_btn13.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                break;
            case Util.DESSERT:
                dialog.setContentView(R.layout.category_14);
                dialog.show();
                Button confirm_btn14 = dialog.findViewById(R.id.button);
                confirm_btn14.setOnClickListener(v -> {
                    Intent resultActivityIntent = new Intent(Tag.this, result.class);
                    resultActivityIntent.putExtra("button_text", "甜點");
                    resultActivityIntent.putExtra("tag_id", 14);
                    resultActivityIntent.putExtra("clickNum", dbHelper.getClickNumByTagId(14));
                    resultActivityIntent.putExtra("userId", userId);
                    resultActivityIntent.putExtra("user_lat", user_lat);
                    resultActivityIntent.putExtra("user_lng", user_lng);
                    startActivity(resultActivityIntent);
                    Log.v("click", "甜點");
                });
                Button again_btn14 = dialog.findViewById(R.id.button2);
                again_btn14.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                break;
            case Util.ICECREAM:
                dialog.setContentView(R.layout.category_15);
                dialog.show();
                Button confirm_btn15 = dialog.findViewById(R.id.button);
                confirm_btn15.setOnClickListener(v -> {
                    Intent resultActivityIntent = new Intent(Tag.this, result.class);
                    resultActivityIntent.putExtra("button_text", "冰品");
                    resultActivityIntent.putExtra("tag_id", 15);
                    resultActivityIntent.putExtra("clickNum", dbHelper.getClickNumByTagId(15));
                    resultActivityIntent.putExtra("userId", userId);
                    resultActivityIntent.putExtra("user_lat", user_lat);
                    resultActivityIntent.putExtra("user_lng", user_lng);
                    startActivity(resultActivityIntent);
                    Log.v("click", "冰品");
                });
                Button again_btn15 = dialog.findViewById(R.id.button2);
                again_btn15.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                break;
            case Util.DRINK:
                dialog.setContentView(R.layout.category_16);
                dialog.show();
                Button confirm_btn16 = dialog.findViewById(R.id.button);
                confirm_btn16.setOnClickListener(v -> {
                    Intent resultActivityIntent = new Intent(Tag.this, result.class);
                    resultActivityIntent.putExtra("button_text", "飲品");
                    resultActivityIntent.putExtra("tag_id", 16);
                    resultActivityIntent.putExtra("clickNum", dbHelper.getClickNumByTagId(16));
                    resultActivityIntent.putExtra("userId", userId);
                    resultActivityIntent.putExtra("user_lat", user_lat);
                    resultActivityIntent.putExtra("user_lng", user_lng);
                    startActivity(resultActivityIntent);
                    Log.v("click", "飲品");
                });
                Button again_btn16 = dialog.findViewById(R.id.button2);
                again_btn16.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                break;
            case Util.COFFEE:
                dialog.setContentView(R.layout.category_17);
                dialog.show();
                Button confirm_btn17 = dialog.findViewById(R.id.button);
                confirm_btn17.setOnClickListener(v -> {
                    Intent resultActivityIntent = new Intent(Tag.this, result.class);
                    resultActivityIntent.putExtra("button_text", "咖啡");
                    resultActivityIntent.putExtra("tag_id", 17);
                    resultActivityIntent.putExtra("clickNum", dbHelper.getClickNumByTagId(17));
                    resultActivityIntent.putExtra("userId", userId);
                    resultActivityIntent.putExtra("user_lat", user_lat);
                    resultActivityIntent.putExtra("user_lng", user_lng);
                    startActivity(resultActivityIntent);
                    Log.v("click", "咖啡");
                });
                Button again_btn17 = dialog.findViewById(R.id.button2);
                again_btn17.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                break;
            case Util.TEA:
                dialog.setContentView(R.layout.category_18);
                dialog.show();
                Button confirm_btn18 = dialog.findViewById(R.id.button);
                confirm_btn18.setOnClickListener(v -> {
                    Intent resultActivityIntent = new Intent(Tag.this, result.class);
                    resultActivityIntent.putExtra("button_text", "茶店");
                    resultActivityIntent.putExtra("tag_id", 18);
                    resultActivityIntent.putExtra("clickNum", dbHelper.getClickNumByTagId(18));
                    resultActivityIntent.putExtra("userId", userId);
                    resultActivityIntent.putExtra("user_lat", user_lat);
                    resultActivityIntent.putExtra("user_lng", user_lng);
                    startActivity(resultActivityIntent);
                    Log.v("click", "茶店");
                });
                Button again_btn18 = dialog.findViewById(R.id.button2);
                again_btn18.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                break;
            case Util.BOX_LUNCH:
                dialog.setContentView(R.layout.category_19);
                dialog.show();
                Button confirm_btn19 = dialog.findViewById(R.id.button);
                confirm_btn19.setOnClickListener(v -> {
                    Intent resultActivityIntent = new Intent(Tag.this, result.class);
                    resultActivityIntent.putExtra("button_text", "便當");
                    resultActivityIntent.putExtra("tag_id", 19);
                    resultActivityIntent.putExtra("clickNum", dbHelper.getClickNumByTagId(19));
                    resultActivityIntent.putExtra("userId", userId);
                    resultActivityIntent.putExtra("user_lat", user_lat);
                    resultActivityIntent.putExtra("user_lng", user_lng);
                    startActivity(resultActivityIntent);
                    Log.v("click", "便當");
                });
                Button again_btn19 = dialog.findViewById(R.id.button2);
                again_btn19.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                break;
            case Util.HEALTHY_MEAL:
                dialog.setContentView(R.layout.category_20);
                dialog.show();
                Button confirm_btn20 = dialog.findViewById(R.id.button);
                confirm_btn20.setOnClickListener(v -> {
                    Intent resultActivityIntent = new Intent(Tag.this, result.class);
                    resultActivityIntent.putExtra("button_text", "健康餐");
                    resultActivityIntent.putExtra("tag_id", 20);
                    resultActivityIntent.putExtra("clickNum", dbHelper.getClickNumByTagId(20));
                    resultActivityIntent.putExtra("userId", userId);
                    resultActivityIntent.putExtra("user_lat", user_lat);
                    resultActivityIntent.putExtra("user_lng", user_lng);
                    startActivity(resultActivityIntent);
                    Log.v("click", "健康餐");
                });
                Button again_btn20 = dialog.findViewById(R.id.button2);
                again_btn20.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                break;
            case Util.SUSHI:
                dialog.setContentView(R.layout.category_21);
                dialog.show();
                Button confirm_btn21 = dialog.findViewById(R.id.button);
                confirm_btn21.setOnClickListener(v -> {
                    Intent resultActivityIntent = new Intent(Tag.this, result.class);
                    resultActivityIntent.putExtra("button_text", "壽司");
                    resultActivityIntent.putExtra("tag_id", 21);
                    resultActivityIntent.putExtra("clickNum", dbHelper.getClickNumByTagId(21));
                    resultActivityIntent.putExtra("userId", userId);
                    resultActivityIntent.putExtra("user_lat", user_lat);
                    resultActivityIntent.putExtra("user_lng", user_lng);
                    startActivity(resultActivityIntent);
                    Log.v("click", "壽司");
                });
                Button again_btn21 = dialog.findViewById(R.id.button2);
                again_btn21.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                break;

            case Util.NOODLES:
                dialog.setContentView(R.layout.category_22);
                dialog.show();
                Button confirm_btn22 = dialog.findViewById(R.id.button);
                confirm_btn22.setOnClickListener(v -> {
                    Intent resultActivityIntent = new Intent(Tag.this, result.class);
                    resultActivityIntent.putExtra("button_text", "麵食");
                    resultActivityIntent.putExtra("tag_id", 22);
                    resultActivityIntent.putExtra("clickNum", dbHelper.getClickNumByTagId(22));
                    resultActivityIntent.putExtra("userId", userId);
                    resultActivityIntent.putExtra("user_lat", user_lat);
                    resultActivityIntent.putExtra("user_lng", user_lng);
                    startActivity(resultActivityIntent);
                    Log.v("click", "麵食");
                });
                Button again_btn22 = dialog.findViewById(R.id.button2);
                again_btn22.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                break;
            case Util.BEEF_NOODLES:
                dialog.setContentView(R.layout.category_23);
                dialog.show();
                Button confirm_btn23 = dialog.findViewById(R.id.button);
                confirm_btn23.setOnClickListener(v -> {
                    Intent resultActivityIntent = new Intent(Tag.this, result.class);
                    resultActivityIntent.putExtra("button_text", "牛肉麵");
                    resultActivityIntent.putExtra("tag_id", 23);
                    resultActivityIntent.putExtra("clickNum", dbHelper.getClickNumByTagId(23));
                    resultActivityIntent.putExtra("userId", userId);
                    resultActivityIntent.putExtra("user_lat", user_lat);
                    resultActivityIntent.putExtra("user_lng", user_lng);
                    startActivity(resultActivityIntent);
                    Log.v("click", "牛肉麵");
                });
                Button again_btn23 = dialog.findViewById(R.id.button2);
                again_btn23.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                break;
            case Util.RAMEN:
                dialog.setContentView(R.layout.category_24);
                dialog.show();
                Button confirm_btn24 = dialog.findViewById(R.id.button);
                confirm_btn24.setOnClickListener(v -> {
                    Intent resultActivityIntent = new Intent(Tag.this, result.class);
                    resultActivityIntent.putExtra("button_text", "拉麵");
                    resultActivityIntent.putExtra("tag_id", 24);
                    resultActivityIntent.putExtra("clickNum", dbHelper.getClickNumByTagId(24));
                    resultActivityIntent.putExtra("userId", userId);
                    resultActivityIntent.putExtra("user_lat", user_lat);
                    resultActivityIntent.putExtra("user_lng", user_lng);
                    startActivity(resultActivityIntent);
                    Log.v("click", "拉麵");
                });
                Button again_btn24 = dialog.findViewById(R.id.button2);
                again_btn24.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                break;
            case Util.FOODSTALL:
                dialog.setContentView(R.layout.category_25);
                dialog.show();
                Button confirm_btn25 = dialog.findViewById(R.id.button);
                confirm_btn25.setOnClickListener(v -> {
                    Intent resultActivityIntent = new Intent(Tag.this, result.class);
                    resultActivityIntent.putExtra("button_text", "小吃");
                    resultActivityIntent.putExtra("tag_id", 25);
                    resultActivityIntent.putExtra("clickNum", dbHelper.getClickNumByTagId(25));
                    resultActivityIntent.putExtra("userId", userId);
                    resultActivityIntent.putExtra("user_lat", user_lat);
                    resultActivityIntent.putExtra("user_lng", user_lng);
                    startActivity(resultActivityIntent);
                    Log.v("click", "小吃");
                });
                Button again_btn25 = dialog.findViewById(R.id.button2);
                again_btn25.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                break;
            case Util.BUFFET:
                dialog.setContentView(R.layout.category_26);
                dialog.show();
                Button confirm_btn26 = dialog.findViewById(R.id.button);
                confirm_btn26.setOnClickListener(v -> {
                    Intent resultActivityIntent = new Intent(Tag.this, result.class);
                    resultActivityIntent.putExtra("button_text", "自助餐");
                    resultActivityIntent.putExtra("tag_id", 26);
                    resultActivityIntent.putExtra("clickNum", dbHelper.getClickNumByTagId(26));
                    resultActivityIntent.putExtra("userId", userId);
                    resultActivityIntent.putExtra("user_lat", user_lat);
                    resultActivityIntent.putExtra("user_lng", user_lng);
                    startActivity(resultActivityIntent);
                    Log.v("click", "自助餐");
                });
                Button again_btn26 = dialog.findViewById(R.id.button2);
                again_btn26.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                break;
            case Util.PRIX_FIXE:
                dialog.setContentView(R.layout.category_27);
                dialog.show();
                Button confirm_btn27 = dialog.findViewById(R.id.button);
                confirm_btn27.setOnClickListener(v -> {
                    Intent resultActivityIntent = new Intent(Tag.this, result.class);
                    resultActivityIntent.putExtra("button_text", "合菜");
                    resultActivityIntent.putExtra("tag_id", 27);
                    resultActivityIntent.putExtra("clickNum", dbHelper.getClickNumByTagId(27));
                    resultActivityIntent.putExtra("userId", userId);
                    resultActivityIntent.putExtra("user_lat", user_lat);
                    resultActivityIntent.putExtra("user_lng", user_lng);
                    startActivity(resultActivityIntent);
                    Log.v("click", "合菜");
                });
                Button again_btn27 = dialog.findViewById(R.id.button2);
                again_btn27.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                break;
            case Util.STIR_FRIED:
                dialog.setContentView(R.layout.category_28);
                dialog.show();
                Button confirm_btn28 = dialog.findViewById(R.id.button);
                confirm_btn28.setOnClickListener(v -> {
                    Intent resultActivityIntent = new Intent(Tag.this, result.class);
                    resultActivityIntent.putExtra("button_text", "熱炒");
                    resultActivityIntent.putExtra("tag_id", 28);
                    resultActivityIntent.putExtra("clickNum", dbHelper.getClickNumByTagId(28));
                    resultActivityIntent.putExtra("userId", userId);
                    resultActivityIntent.putExtra("user_lat", user_lat);
                    resultActivityIntent.putExtra("user_lng", user_lng);
                    startActivity(resultActivityIntent);
                    Log.v("click", "熱炒");
                });
                Button again_btn28 = dialog.findViewById(R.id.button2);
                again_btn28.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                break;
            case Util.TEPPANYAKI:
                dialog.setContentView(R.layout.category_29);
                dialog.show();
                Button confirm_btn29 = dialog.findViewById(R.id.button);
                confirm_btn29.setOnClickListener(v -> {
                    Intent resultActivityIntent = new Intent(Tag.this, result.class);
                    resultActivityIntent.putExtra("button_text", "鐵板燒");
                    resultActivityIntent.putExtra("tag_id", 29);
                    resultActivityIntent.putExtra("clickNum", dbHelper.getClickNumByTagId(29));
                    resultActivityIntent.putExtra("userId", userId);
                    resultActivityIntent.putExtra("user_lat", user_lat);
                    resultActivityIntent.putExtra("user_lng", user_lng);
                    startActivity(resultActivityIntent);
                    Log.v("click", "鐵板燒");
                });
                Button again_btn29 = dialog.findViewById(R.id.button2);
                again_btn29.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                break;
            case Util.HOTPOT:
                dialog.setContentView(R.layout.category_30);
                dialog.show();
                Button confirm_btn30 = dialog.findViewById(R.id.button);
                confirm_btn30.setOnClickListener(v -> {
                    Intent resultActivityIntent = new Intent(Tag.this, result.class);
                    resultActivityIntent.putExtra("button_text", "火鍋");
                    resultActivityIntent.putExtra("tag_id", 30);
                    resultActivityIntent.putExtra("clickNum", dbHelper.getClickNumByTagId(30));
                    resultActivityIntent.putExtra("userId", userId);
                    resultActivityIntent.putExtra("user_lat", user_lat);
                    resultActivityIntent.putExtra("user_lng", user_lng);
                    startActivity(resultActivityIntent);
                    Log.v("click", "火鍋");
                });
                Button again_btn30 = dialog.findViewById(R.id.button2);
                again_btn30.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                break;
            case Util.STEAK:
                dialog.setContentView(R.layout.category_31);
                dialog.show();
                Button confirm_btn31 = dialog.findViewById(R.id.button);
                confirm_btn31.setOnClickListener(v -> {
                    Intent resultActivityIntent = new Intent(Tag.this, result.class);
                    resultActivityIntent.putExtra("button_text", "牛排");
                    resultActivityIntent.putExtra("tag_id", 31);
                    resultActivityIntent.putExtra("clickNum", dbHelper.getClickNumByTagId(31));
                    resultActivityIntent.putExtra("userId", userId);
                    resultActivityIntent.putExtra("user_lat", user_lat);
                    resultActivityIntent.putExtra("user_lng", user_lng);
                    startActivity(resultActivityIntent);
                    Log.v("click", "牛排");
                });
                Button again_btn31 = dialog.findViewById(R.id.button2);
                again_btn31.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                break;
            case Util.HAMBURGER:
                dialog.setContentView(R.layout.category_32);
                dialog.show();
                Button confirm_btn32 = dialog.findViewById(R.id.button);
                confirm_btn32.setOnClickListener(v -> {
                    Intent resultActivityIntent = new Intent(Tag.this, result.class);
                    resultActivityIntent.putExtra("button_text", "漢堡");
                    resultActivityIntent.putExtra("tag_id", 32);
                    resultActivityIntent.putExtra("clickNum", dbHelper.getClickNumByTagId(32));
                    resultActivityIntent.putExtra("userId", userId);
                    resultActivityIntent.putExtra("user_lat", user_lat);
                    resultActivityIntent.putExtra("user_lng", user_lng);
                    startActivity(resultActivityIntent);
                    Log.v("click", "漢堡");
                });
                Button again_btn32 = dialog.findViewById(R.id.button2);
                again_btn32.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                break;
            case Util.BBQ:
                dialog.setContentView(R.layout.category_33);
                dialog.show();
                Button confirm_btn33 = dialog.findViewById(R.id.button);
                confirm_btn33.setOnClickListener(v -> {
                    Intent resultActivityIntent = new Intent(Tag.this, result.class);
                    resultActivityIntent.putExtra("button_text", "烤肉");
                    resultActivityIntent.putExtra("tag_id", 33);
                    resultActivityIntent.putExtra("clickNum", dbHelper.getClickNumByTagId(33));
                    resultActivityIntent.putExtra("userId", userId);
                    resultActivityIntent.putExtra("user_lat", user_lat);
                    resultActivityIntent.putExtra("user_lng", user_lng);
                    startActivity(resultActivityIntent);
                    Log.v("click", "烤肉");
                });
                Button again_btn33 = dialog.findViewById(R.id.button2);
                again_btn33.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                break;
            case Util.CURRY:
                dialog.setContentView(R.layout.category_34);
                dialog.show();
                Button confirm_btn34 = dialog.findViewById(R.id.button);
                confirm_btn34.setOnClickListener(v -> {
                    Intent resultActivityIntent = new Intent(Tag.this, result.class);
                    resultActivityIntent.putExtra("button_text", "咖哩");
                    resultActivityIntent.putExtra("tag_id", 34);
                    resultActivityIntent.putExtra("clickNum", dbHelper.getClickNumByTagId(34));
                    resultActivityIntent.putExtra("userId", userId);
                    resultActivityIntent.putExtra("user_lat", user_lat);
                    resultActivityIntent.putExtra("user_lng", user_lng);
                    startActivity(resultActivityIntent);
                    Log.v("click", "咖哩");
                });
                Button again_btn34 = dialog.findViewById(R.id.button2);
                again_btn34.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                break;
            case Util.SEAFOOD:
                dialog.setContentView(R.layout.category_35);
                dialog.show();
                Button confirm_btn35 = dialog.findViewById(R.id.button);
                confirm_btn35.setOnClickListener(v -> {
                    Intent resultActivityIntent = new Intent(Tag.this, result.class);
                    resultActivityIntent.putExtra("button_text", "海鮮");
                    resultActivityIntent.putExtra("tag_id", 35);
                    resultActivityIntent.putExtra("clickNum", dbHelper.getClickNumByTagId(35));
                    resultActivityIntent.putExtra("userId", userId);
                    resultActivityIntent.putExtra("user_lat", user_lat);
                    resultActivityIntent.putExtra("user_lng", user_lng);
                    startActivity(resultActivityIntent);
                    Log.v("click", "海鮮");
                });
                Button again_btn35 = dialog.findViewById(R.id.button2);
                again_btn35.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                break;
            case Util.VEGAN:
                dialog.setContentView(R.layout.category_36);
                dialog.show();
                Button confirm_btn36 = dialog.findViewById(R.id.button);
                confirm_btn36.setOnClickListener(v -> {
                    Intent resultActivityIntent = new Intent(Tag.this, result.class);
                    resultActivityIntent.putExtra("button_text", "素食");
                    resultActivityIntent.putExtra("tag_id", 36);
                    resultActivityIntent.putExtra("clickNum", dbHelper.getClickNumByTagId(36));
                    resultActivityIntent.putExtra("userId", userId);
                    resultActivityIntent.putExtra("user_lat", user_lat);
                    resultActivityIntent.putExtra("user_lng", user_lng);
                    startActivity(resultActivityIntent);
                    Log.v("click", "素食");
                });
                Button again_btn36 = dialog.findViewById(R.id.button2);
                again_btn36.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                break;
            case Util.IZAKAYA:
                dialog.setContentView(R.layout.category_37);
                dialog.show();
                Button confirm_btn37 = dialog.findViewById(R.id.button);
                confirm_btn37.setOnClickListener(v -> {
                    Intent resultActivityIntent = new Intent(Tag.this, result.class);
                    resultActivityIntent.putExtra("button_text", "居酒屋");
                    resultActivityIntent.putExtra("tag_id", 37);
                    resultActivityIntent.putExtra("clickNum", dbHelper.getClickNumByTagId(37));
                    resultActivityIntent.putExtra("userId", userId);
                    resultActivityIntent.putExtra("user_lat", user_lat);
                    resultActivityIntent.putExtra("user_lng", user_lng);
                    startActivity(resultActivityIntent);
                    Log.v("click", "居酒屋");
                });
                Button again_btn37 = dialog.findViewById(R.id.button2);
                again_btn37.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                break;
            case Util.BISTRO:
                dialog.setContentView(R.layout.category_38);
                dialog.show();
                Button confirm_btn38 = dialog.findViewById(R.id.button);
                confirm_btn38.setOnClickListener(v -> {
                    Intent resultActivityIntent = new Intent(Tag.this, result.class);
                    resultActivityIntent.putExtra("button_text", "餐酒館");
                    resultActivityIntent.putExtra("tag_id", 38);
                    resultActivityIntent.putExtra("clickNum", dbHelper.getClickNumByTagId(38));
                    resultActivityIntent.putExtra("userId", userId);
                    resultActivityIntent.putExtra("user_lat", user_lat);
                    resultActivityIntent.putExtra("user_lng", user_lng);
                    startActivity(resultActivityIntent);
                    Log.v("click", "餐酒館");
                });
                Button again_btn38 = dialog.findViewById(R.id.button2);
                again_btn38.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                break;
            case Util.BAR:
                dialog.setContentView(R.layout.category_39);
                dialog.show();
                Button confirm_btn39 = dialog.findViewById(R.id.button);
                confirm_btn39.setOnClickListener(v -> {
                    Intent resultActivityIntent = new Intent(Tag.this, result.class);
                    resultActivityIntent.putExtra("button_text", "酒吧");
                    resultActivityIntent.putExtra("tag_id", 39);
                    resultActivityIntent.putExtra("clickNum", dbHelper.getClickNumByTagId(39));
                    resultActivityIntent.putExtra("userId", userId);
                    resultActivityIntent.putExtra("user_lat", user_lat);
                    resultActivityIntent.putExtra("user_lng", user_lng);
                    startActivity(resultActivityIntent);
                    Log.v("click", "酒吧");
                });
                Button again_btn39 = dialog.findViewById(R.id.button2);
                again_btn39.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                break;
            case Util.POSH_RESTAURANT:
                dialog.setContentView(R.layout.category_40);
                dialog.show();
                Button confirm_btn40 = dialog.findViewById(R.id.button);
                confirm_btn40.setOnClickListener(v -> {
                    Intent resultActivityIntent = new Intent(Tag.this, result.class);
                    resultActivityIntent.putExtra("button_text", "精緻高級");
                    resultActivityIntent.putExtra("tag_id", 40);
                    resultActivityIntent.putExtra("clickNum", dbHelper.getClickNumByTagId(40));
                    resultActivityIntent.putExtra("userId", userId);
                    resultActivityIntent.putExtra("user_lat", user_lat);
                    resultActivityIntent.putExtra("user_lng", user_lng);
                    startActivity(resultActivityIntent);
                    Log.v("click", "精緻高級");
                });
                Button again_btn40 = dialog.findViewById(R.id.button2);
                again_btn40.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                break;
            case Util.FAMILY_RESTAURANT:
                dialog.setContentView(R.layout.category_41);
                dialog.show();
                Button confirm_btn41 = dialog.findViewById(R.id.button);
                confirm_btn41.setOnClickListener(v -> {
                    Intent resultActivityIntent = new Intent(Tag.this, result.class);
                    resultActivityIntent.putExtra("button_text", "親子餐廳");
                    resultActivityIntent.putExtra("tag_id", 41);
                    resultActivityIntent.putExtra("clickNum", dbHelper.getClickNumByTagId(41));
                    resultActivityIntent.putExtra("userId", userId);
                    resultActivityIntent.putExtra("user_lat", user_lat);
                    resultActivityIntent.putExtra("user_lng", user_lng);
                    startActivity(resultActivityIntent);
                    Log.v("click", "親子餐廳");
                });
                Button again_btn41 = dialog.findViewById(R.id.button2);
                again_btn41.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                break;
            case Util.VIEW_RESTAURANT:
                dialog.setContentView(R.layout.category_42);
                dialog.show();
                Button confirm_btn42 = dialog.findViewById(R.id.button);
                confirm_btn42.setOnClickListener(v -> {
                    Intent resultActivityIntent = new Intent(Tag.this, result.class);
                    resultActivityIntent.putExtra("button_text", "景觀餐廳");
                    resultActivityIntent.putExtra("tag_id", 42);
                    resultActivityIntent.putExtra("clickNum", dbHelper.getClickNumByTagId(42));
                    resultActivityIntent.putExtra("userId", userId);
                    resultActivityIntent.putExtra("user_lat", user_lat);
                    resultActivityIntent.putExtra("user_lng", user_lng);
                    startActivity(resultActivityIntent);
                    Log.v("click", "景觀餐廳");
                });
                Button again_btn42 = dialog.findViewById(R.id.button2);
                again_btn42.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                break;
            default:
                break;
        }
    }
}
