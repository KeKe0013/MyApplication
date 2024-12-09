package com.example.myapplication;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AlignmentSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

public class result extends AppCompatActivity {

    TextView resultText;
    Function function = new Function();
    SQLite dbHelper;
    double user_lat;
    double user_lng;
    private LinearLayout ResultLayout;
    Button morebtn;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        dbHelper = new SQLite(this); // 初始化 SQLite 帮助类

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            for (String key : extras.keySet()) {
                Object value = extras.get(key);
                Log.d("IntentDebug", "Key: " + key + ", Value: " + value);
            }
        } else {
            Log.d("IntentDebug", "Intent extras are null.");
        }
        String buttonText = getIntent().getStringExtra("button_text");
        int tag_id = getIntent().getIntExtra("tag_id", 0);
        int clickNum = getIntent().getIntExtra("clickNum", 0);
        userId = getIntent().getIntExtra("userId", 0);
        user_lat = getIntent().getDoubleExtra("user_lat", 0);
        user_lng = getIntent().getDoubleExtra("user_lng", 0);

        new Thread(new Runnable() {
            @Override
            public void run() {
                dbHelper.updateClick(tag_id, clickNum);
            }
        }).start();

        resultText = findViewById(R.id.resultText);
        resultText.setText(buttonText);

        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<RestaurantData> allResult = null; // 在try區塊外部聲明allResult
                try {
                    int today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
                    if (today == Calendar.SUNDAY) {
                        today = 7;
                    } else {
                        today -= 1;
                    }
                    allResult = dbHelper.getTagRestaurantData(userId, tag_id, today);
                    ResultLayout = findViewById(R.id.result_layout);

                    if (allResult != null) {
                        final ArrayList<RestaurantData> finalAllResult = allResult; // 宣告一個最終的allResult
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ArrayList<FrameLayout> distantFrameLayouts = addButtonsToLayout(finalAllResult);
                                morebtn = findViewById(R.id.morebtn);
                                morebtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        for (FrameLayout frameLayout : distantFrameLayouts) {
                                            ResultLayout.addView(frameLayout); //顯示20-50公里的店家
                                        }
                                        morebtn.setVisibility(View.GONE);
                                    }
                                });
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        ImageButton button_back = findViewById(R.id.back);
        button_back.setOnClickListener(view -> {
            finish();
        });
    }

    private ArrayList<FrameLayout> addButtonsToLayout(ArrayList<RestaurantData> allResult) {
        ResultLayout = findViewById(R.id.result_layout);
        // 搜尋結果按照距離排序
        Collections.sort(allResult, new Comparator<RestaurantData>() {
            @Override
            public int compare(RestaurantData o1, RestaurantData o2) {
                // 計算距離
                double distance1 = function.CalculateDistance(o1.getLatitude(), o1.getLongitude(), user_lat, user_lng);
                double distance2 = function.CalculateDistance(o2.getLatitude(), o2.getLongitude(), user_lat, user_lng);
                // 根據距離排序
                return Double.compare(distance1, distance2);
            }
        });

        ArrayList<FrameLayout> distantFrameLayouts = new ArrayList<>(); // 存放超過20公里的 FrameLayout

        for (RestaurantData RestaurantData : allResult) {
            //if(function.CalculateDistance(RestaurantData.getLat(),RestaurantData.getLng(),user_lat,user_lng) <= 100000){
            // 建立 FrameLayout
            FrameLayout frameLayout = new FrameLayout(this);
            // 建立TextView
            TextView textView = new TextView(this);
            int distance = (int) Math.round(function.CalculateDistance(RestaurantData.getLatitude(), RestaurantData.getLongitude(), user_lat, user_lng));
            SpannableString ss;
            if (distance <= 1000) {
                ss = new SpannableString("0" + distance + " m");
            } else {
                if (distance <= 10000) {
                    double dis = Math.round(distance / 1000.0);
                    ss = new SpannableString("0" + dis + " km");
                } else {
                    ss = new SpannableString("0" + distance / 1000 + " km");
                }
            }
            Drawable walkingDrawable = getResources().getDrawable(R.drawable.walking);
            walkingDrawable.setBounds(0, 0, 50, 50);

            ImageSpan imageSpan = new ImageSpan(walkingDrawable, ImageSpan.ALIGN_BOTTOM);

            ss.setSpan(imageSpan, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            AlignmentSpan.Standard alignmentSpan = new AlignmentSpan.Standard(Layout.Alignment.ALIGN_NORMAL);
            ss.setSpan(alignmentSpan, 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            textView.setText(ss);
            textView.setTypeface(null, Typeface.BOLD);

            ImageView imageView = new ImageView(this);
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.heart));

            // 建立 RelativeLayout
            RelativeLayout relativeLayout = new RelativeLayout(this);

            // 建立 Button
            Button resultBtn = new Button(this);
            String R_name = RestaurantData.getName();
            if (R_name.length() > 12) {
                R_name = R_name.substring(0, 12) + "...";
            }
            // 合併店名和地址
            String buttonText = R_name + "\n\n" + RestaurantData.getAddress();
            // 創建 SpannableString 來設定不同文字大小
            SpannableString spannableString = new SpannableString(buttonText);
            // 設定店名的文字大小
            spannableString.setSpan(new RelativeSizeSpan(1.5f), 0, R_name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            resultBtn.setText(spannableString);

            resultBtn.setTypeface(null, Typeface.BOLD);
            resultBtn.setBackgroundResource(R.drawable.result_button);
            //resultBtn.setAllCaps(false);

            // 設置 TextView 的布局参数
            FrameLayout.LayoutParams textViewParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );
            textViewParams.gravity = Gravity.BOTTOM;
            textViewParams.setMargins(40, 20, 20, 5);
            textView.setLayoutParams(textViewParams);

            // 設置 ImageView 的布局参数
            FrameLayout.LayoutParams imageViewParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );
            imageViewParams.gravity = Gravity.TOP;
            imageViewParams.setMargins(910, 20, 0, 0);
            imageView.setLayoutParams(imageViewParams);

            // 設置 Button 的布局参数
            RelativeLayout.LayoutParams btnParams = new RelativeLayout.LayoutParams(
                    1000,
                    330
            );
            btnParams.addRule(RelativeLayout.CENTER_IN_PARENT); // 將放在 RelativeLayout 的中心
            resultBtn.setLayoutParams(btnParams);

            setupRestaurantButton(RestaurantData.getId(), resultBtn);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    int state = dbHelper.getRestaurantState(userId, RestaurantData.getId());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (state == 1) {
                                frameLayout.addView(imageView);
                            }
                        }
                    });
                }
            }).start();

            relativeLayout.addView(resultBtn);
            frameLayout.addView(relativeLayout);
            frameLayout.addView(textView);

            // 設置 FrameLayout 的外邊距
            FrameLayout.LayoutParams frameLayoutParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );
            frameLayoutParams.setMargins(40, 25, 40, 25);
            frameLayout.setLayoutParams(frameLayoutParams);
            setButtonClickListener(resultBtn, RestaurantData); //按鈕點擊事件

            if (distance > 5000) {
                distantFrameLayouts.add(frameLayout);
                Log.v("more", RestaurantData.getName());
            } else {
                ResultLayout.addView(frameLayout);
            }
        }
        return distantFrameLayouts;
    }

    // 按鈕點擊事件
    private void setButtonClickListener(Button button, RestaurantData restaurantData) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultActivityIntent = new Intent(result.this, res_info.class);
                resultActivityIntent.putExtra("restaurant_data", restaurantData);
                resultActivityIntent.putExtra("userId", userId);
                resultActivityIntent.putExtra("state", restaurantData.getState());
                resultActivityIntent.putExtra("likeCount", restaurantData.getLikeCount());
                resultActivityIntent.putExtra("unlikeCount", restaurantData.getUnlikeCount());
                resultActivityIntent.putExtra("user_lat", user_lat);
                resultActivityIntent.putExtra("user_lng", user_lng);
                startActivity(resultActivityIntent);
                Log.v("click", restaurantData.getName() + Integer.toString(restaurantData.getId()));
            }
        });
    }

    private void setupRestaurantButton(int restaurantId, android.widget.Button resultBtn) {
        Resources resources = getResources();
        float density = resources.getDisplayMetrics().density;

        // Logo 設置
        String logoName = "logo_" + restaurantId;
        int logoResourceId = resources.getIdentifier(logoName, "drawable", getPackageName());
        Drawable logoDrawable;

        if (logoResourceId != 0) {
            try {
                logoDrawable = resources.getDrawable(logoResourceId);
            } catch (Resources.NotFoundException e) {
                Log.w("RestaurantList", "Logo not found: " + logoName, e);
                logoDrawable = resources.getDrawable(R.drawable.logonull);
            }
        } else {
            Log.w("RestaurantList", "Logo resource not found: " + logoName);
            logoDrawable = resources.getDrawable(R.drawable.logonull);
        }

        // 調整 logo 大小為 70dp x 70dp
        int logoSizePx = Math.round(70 * density);
        Bitmap originalBitmap;
        if (logoDrawable instanceof BitmapDrawable) {
            originalBitmap = ((BitmapDrawable) logoDrawable).getBitmap();
        } else {
            originalBitmap = Bitmap.createBitmap(logoDrawable.getIntrinsicWidth(),
                    logoDrawable.getIntrinsicHeight(),
                    Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(originalBitmap);
            logoDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            logoDrawable.draw(canvas);
        }

        float scale = (float) logoSizePx / Math.min(originalBitmap.getWidth(), originalBitmap.getHeight());
        int scaledWidth = Math.round(originalBitmap.getWidth() * scale);
        int scaledHeight = Math.round(originalBitmap.getHeight() * scale);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, scaledWidth, scaledHeight, true);
        Drawable circleDrawable = circle(new BitmapDrawable(resources, scaledBitmap));

        // Heart 圖標設置
        Drawable heartDrawable = resources.getDrawable(R.drawable.heart);

        // 設置 drawable 的邊界
        int leftPaddingPx = Math.round(15 * density);
        int topPaddingPx = Math.round(0 * density);
        circleDrawable.setBounds(leftPaddingPx, topPaddingPx, leftPaddingPx + logoSizePx, topPaddingPx + logoSizePx);

        int heartWidth = heartDrawable.getIntrinsicWidth();
        int heartHeight = heartDrawable.getIntrinsicHeight();
        int heartLeftPx = Math.round(-20 * density);
        int heartTopPx = Math.round(-100 * density);
        heartDrawable.setBounds(heartLeftPx, heartTopPx, heartLeftPx + heartWidth, heartTopPx + heartHeight);

        // 設置 button 的 drawable
        resultBtn.setCompoundDrawables(circleDrawable, null, null, null);
    }

    /**变成圆形图片**/
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
        borderPaint.setColor(Color.BLACK); // 设置外框颜色

        canvas.drawCircle(originalBitmap.getWidth() / 2, originalBitmap.getHeight() / 2, radius, paint);

        // 在图形外绘制外框
        canvas.drawCircle(originalBitmap.getWidth() / 2, originalBitmap.getHeight() / 2, radius - borderWidth / 2, borderPaint);

        Drawable circleDrawable = new BitmapDrawable(getResources(), circleBitmap);

        return circleDrawable;
    }
}
