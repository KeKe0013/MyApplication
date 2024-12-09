package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
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
import android.os.Handler;
import android.os.Looper;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AlignmentSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

public class list extends AppCompatActivity {

    private SwitchCompat toggleSwitch;
    private RestaurantListFragment bookmarkFragment;
    private RestaurantListFragment blacklistFragment;
    private ImageButton allTag, map, tree, user;

    private int userId;
    private double user_lat;
    private double user_lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        userId = getIntent().getIntExtra("userId", 0);
        user_lat = getIntent().getDoubleExtra("user_lat", 0);
        user_lng = getIntent().getDoubleExtra("user_lng", 0);

        toggleSwitch = findViewById(R.id.toggle_switch);

        bookmarkFragment = RestaurantListFragment.newInstance(userId, 1, user_lat, user_lng);
        blacklistFragment = RestaurantListFragment.newInstance(userId, -1, user_lat, user_lng);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, bookmarkFragment)
                .commit();

        toggleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                toggleFragments(isChecked);
            }
        });

        allTag = findViewById(R.id.allTag);
        map = findViewById(R.id.map);
        tree = findViewById(R.id.tree);
        user = findViewById(R.id.user);
        setupNavigationButtons();
    }

    private void toggleFragments(boolean isBookmark) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        TextView textView = findViewById(R.id.textView);

        if (isBookmark) {
            transaction.replace(R.id.fragment_container, blacklistFragment);
            textView.setText("黑名單");
        } else {
            transaction.replace(R.id.fragment_container, bookmarkFragment);
            textView.setText("收藏");
        }
        transaction.commit();
    }

    private void setupNavigationButtons() {
        allTag.setOnClickListener(view -> navigateTo(Tag.class));
        map.setOnClickListener(view -> navigateTo(MainActivity.class));
        tree.setOnClickListener(view -> navigateTo(Tree.class));
        user.setOnClickListener(view -> navigateTo(user.class));
    }

    private void navigateTo(Class<?> cls) {
        Intent intent = new Intent(list.this, cls);
        updateDatabase();
        intent.putExtra("userId", userId);
        intent.putExtra("user_lat", user_lat);
        intent.putExtra("user_lng", user_lng);
        startActivity(intent);
    }

    public void updateDatabase() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof RestaurantListFragment) {
            ((RestaurantListFragment) currentFragment).updateDatabase();
        }
    }


    public static class RestaurantListFragment extends Fragment {

        private static final String CITY_HINT = "請選擇縣市";
        private static final String DISTRICT_HINT = "請選擇區域";

        private int userId;
        private int state;
        private LinearLayout resultLayout;
        private ArrayList<RestaurantData> allRestaurants = new ArrayList<>();
        private Activity activity;
        private Spinner citySpinner;
        private Spinner districtSpinner;
        private ArrayAdapter<String> cityAdapter;
        private ArrayAdapter<String> districtAdapter;

        private double user_lat;
        private double user_lng;
        private ArrayList<RestaurantData> changedRestaurants = new ArrayList<>();
        private TextView emptyStateText;
        private Function function;
        private boolean isDataLoaded = false;

        public static RestaurantListFragment newInstance(int userId, int state, double user_lat, double user_lng) {
            RestaurantListFragment fragment = new RestaurantListFragment();
            Bundle args = new Bundle();
            args.putInt("userId", userId);
            args.putInt("state", state);
            args.putDouble("user_lat", user_lat);
            args.putDouble("user_lng", user_lng);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onAttach(@NonNull Context context) {
            super.onAttach(context);
            if (context instanceof Activity) {
                activity = (Activity) context;
            }
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                userId = getArguments().getInt("userId");
                state = getArguments().getInt("state");
                user_lat = getArguments().getDouble("user_lat");
                user_lng = getArguments().getDouble("user_lng");
            }
            function = new Function();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_restaurant_list, container, false);

            resultLayout = view.findViewById(R.id.result_layout);
            emptyStateText = view.findViewById(R.id.empty_state_text);
            citySpinner = view.findViewById(R.id.city_spinner);
            districtSpinner = view.findViewById(R.id.district_spinner);

            cityAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, new ArrayList<>());
            cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            citySpinner.setAdapter(cityAdapter);

            districtAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, new ArrayList<>());
            districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            districtSpinner.setAdapter(districtAdapter);

            loadRestaurantData();
            resetSpinners();
            updateRestaurantList(null,null);

            return view;
        }



        @Override
        public void onResume() {
            super.onResume();
            if (!isDataLoaded) {
                loadRestaurantData();
            }
        }

        private void resetSpinners() {
            cityAdapter.clear();
            cityAdapter.add(CITY_HINT);
            cityAdapter.notifyDataSetChanged();
            citySpinner.setSelection(0);

            districtAdapter.clear();
            districtAdapter.add(DISTRICT_HINT);
            districtAdapter.notifyDataSetChanged();
            districtSpinner.setSelection(0);
        }

        private void loadRestaurantData() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
                    if (today == Calendar.SUNDAY) {
                        today = 7;
                    } else {
                        today -= 1;
                    }

                    SQLite dbHelper = new SQLite(getContext());
                    allRestaurants = dbHelper.getListRestaurant(userId, state, today);

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                isDataLoaded = true;
                                updateUIAfterDataLoaded();
                            }
                        });
                    }
                }
            }).start();
        }

        private void updateUIAfterDataLoaded() {
            if (getContext() == null || allRestaurants == null) {
                return;
            }

            if (allRestaurants.isEmpty()) {
                String emptyMessage = (state == 1) ? "您尚未收藏任何餐廳" : "您尚未拉黑任何餐廳";
                showEmptyStateMessage(emptyMessage);
            } else {
                emptyStateText.setVisibility(View.GONE);
                resultLayout.setVisibility(View.VISIBLE);

                updateCitySpinner();
                resetDistrictSpinner();
                setupSpinners();
                updateRestaurantList(null, null);
            }
        }

        private void setupSpinners() {
            citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedCity = cityAdapter.getItem(position);
                    if (selectedCity.equals(CITY_HINT)) {
                        updateRestaurantList(null, null);
                        resetDistrictSpinner();
                    } else {
                        updateDistrictSpinner(selectedCity);
                        updateRestaurantList(selectedCity, null);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });

            districtSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedCity = citySpinner.getSelectedItem().toString();
                    String selectedDistrict = districtAdapter.getItem(position);
                    if (selectedDistrict.equals(DISTRICT_HINT)) {
                        updateRestaurantList(selectedCity, null);
                    } else {
                        updateRestaurantList(selectedCity, selectedDistrict);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }

        private void resetDistrictSpinner() {
            districtAdapter.clear();
            districtAdapter.add(DISTRICT_HINT);
            districtAdapter.notifyDataSetChanged();
            districtSpinner.setSelection(0);
        }

        private void updateCitySpinner() {
            cityAdapter.clear();
            cityAdapter.add(CITY_HINT);
            HashSet<String> uniqueCities = new HashSet<>();
            for (RestaurantData restaurant : allRestaurants) {
                String city = restaurant.getCity();
                if (!uniqueCities.contains(city)) {
                    uniqueCities.add(city);
                    cityAdapter.add(city);
                }
            }
            cityAdapter.notifyDataSetChanged();
        }

        private void updateDistrictSpinner(String selectedCity) {
            districtAdapter.clear();
            districtAdapter.add(DISTRICT_HINT);
            for (RestaurantData restaurant : allRestaurants) {
                if (restaurant.getCity().equals(selectedCity) && districtAdapter.getPosition(restaurant.getDistrict()) < 0) {
                    districtAdapter.add(restaurant.getDistrict());
                }
            }
            districtAdapter.notifyDataSetChanged();
            districtSpinner.setSelection(0);
        }

        private void updateRestaurantList(@Nullable String city, @Nullable String district) {
            resultLayout.removeAllViews();
            ArrayList<RestaurantData> filteredRestaurants = new ArrayList<>();

            for (RestaurantData restaurant : allRestaurants) {
                boolean shouldAdd = (city == null || city.equals(CITY_HINT)) ||
                        (restaurant.getCity().equals(city) &&
                                (district == null || district.equals(DISTRICT_HINT) ||
                                        restaurant.getDistrict().equals(district)));
                if (shouldAdd) {
                    filteredRestaurants.add(restaurant);
                }
            }

            emptyStateText.setVisibility(View.GONE);
            resultLayout.setVisibility(View.VISIBLE);
            addRestaurantButtonToLayout(filteredRestaurants, getContext().getPackageName());
        }

        private void showEmptyStateMessage(final String message) {
            resultLayout.setVisibility(View.GONE);
            emptyStateText.setText(message);
            emptyStateText.setVisibility(View.VISIBLE);
        }

        private void addRestaurantButtonToLayout(ArrayList<RestaurantData> restaurantDataList, String packageName) {
            // 根據距離排序餐廳列表
            Collections.sort(restaurantDataList, new Comparator<RestaurantData>() {
                @Override
                public int compare(RestaurantData o1, RestaurantData o2) {
                    double distance1 = function.CalculateDistance(o1.getLatitude(), o1.getLongitude(), user_lat, user_lng);
                    double distance2 = function.CalculateDistance(o2.getLatitude(), o2.getLongitude(), user_lat, user_lng);
                    return Double.compare(distance1, distance2);
                }
            });

            for (RestaurantData restaurantData : restaurantDataList) {
                // 建立 FrameLayout
                FrameLayout frameLayout = new FrameLayout(getContext());

                // 創建並設置距離 TextView
                TextView textView = new TextView(getContext());
                double restaurantLat = restaurantData.getLatitude();
                double restaurantLong = restaurantData.getLongitude();

                int distance = (int) Math.round(function.CalculateDistance(restaurantLat, restaurantLong, user_lat, user_lng));

                SpannableString ss;
                if (distance <= 1000) {
                    ss = new SpannableString("0" + distance + " m");
                } else {
                    double dis = Math.round(distance / 1000.0);
                    ss = new SpannableString("0" + dis + " km");
                }

                Drawable walkingDrawable = getResources().getDrawable(R.drawable.walking);
                walkingDrawable.setBounds(0, 0, 50, 50);
                ImageSpan imageSpan = new ImageSpan(walkingDrawable, ImageSpan.ALIGN_BOTTOM);
                ss.setSpan(imageSpan, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                AlignmentSpan.Standard alignmentSpan = new AlignmentSpan.Standard(Layout.Alignment.ALIGN_NORMAL);
                ss.setSpan(alignmentSpan, 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                textView.setText(ss);
                textView.setTypeface(null, Typeface.BOLD);

                // 設置 TextView 的布局參數
                FrameLayout.LayoutParams textViewParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                );
                textViewParams.gravity = Gravity.BOTTOM;
                textViewParams.setMargins(40, 20, 20, 5);
                textView.setLayoutParams(textViewParams);

                // 建立 RelativeLayout
                RelativeLayout relativeLayout = new RelativeLayout(getContext());

                // 建立 Button
                Button resultBtn = new Button(getContext());
                String R_name = restaurantData.getName();
                if (R_name.length() > 12) {
                    R_name = R_name.substring(0, 12) + "...";
                }
                // 合併店名和地址
                String buttonText = R_name + "\n\n" + restaurantData.getAddress();
                // 創建 SpannableString 來設定不同文字大小
                SpannableString spannableString = new SpannableString(buttonText);
                // 設定店名的文字大小
                spannableString.setSpan(new RelativeSizeSpan(1.5f), 0, R_name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                resultBtn.setText(spannableString);

                resultBtn.setTypeface(null, Typeface.BOLD);
                if (state == 1) {
                    resultBtn.setBackgroundResource(R.drawable.like_button);
                } else if (state == -1) {
                    resultBtn.setBackgroundResource(R.drawable.dislike_button);
                } else {
                    resultBtn.setBackgroundResource(R.drawable.result_button);
                }

                // 設置 Button 的布局參數
                RelativeLayout.LayoutParams btnParams = new RelativeLayout.LayoutParams(
                        1000,
                        330
                );
                btnParams.addRule(RelativeLayout.CENTER_IN_PARENT); // 將放在 RelativeLayout 的中心
                resultBtn.setLayoutParams(btnParams);

                String logo = "logo_" + restaurantData.getId();
                int logoResourceId = getResources().getIdentifier(logo, "drawable", getActivity().getPackageName());

                Drawable circleDrawable = loadResizeCircleLogo(logo, logoResourceId, 70, 20);
                resultBtn.setCompoundDrawables(circleDrawable, null, null, null);

                resultBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), res_info.class);
                        intent.putExtra("restaurant_data", restaurantData);
                        intent.putExtra("userId", userId);
                        intent.putExtra("user_lat", user_lat);
                        intent.putExtra("user_lng", user_lng);
                        intent.putExtra("state", state);
                        intent.putExtra("likeCount", restaurantData.getLikeCount());
                        intent.putExtra("unlikeCount", restaurantData.getUnlikeCount());
                        startActivity(intent);
                    }
                });

                // 新增：創建 ImageButton 來切換狀態
                ImageButton stateButton = new ImageButton(getContext());
                FrameLayout.LayoutParams stateButtonParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        Gravity.TOP | Gravity.RIGHT
                );
                stateButtonParams.setMargins(0, 10, 10, 0);
                stateButton.setLayoutParams(stateButtonParams);

                // 設置初始圖標
                int initialIconResource = (state == 1) ? R.drawable.heart : R.drawable.black_heart;
                stateButton.setImageResource(initialIconResource);
                stateButton.setBackgroundResource(android.R.color.transparent);

                // 設置點擊監聽器
                stateButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (state == 1) {
                            if (((ImageButton) v).getDrawable().getConstantState().equals(
                                    getResources().getDrawable(R.drawable.heart).getConstantState())) {
                                ((ImageButton) v).setImageResource(R.drawable.like);
                                changedRestaurants.add(restaurantData);
                            } else {
                                ((ImageButton) v).setImageResource(R.drawable.heart);
                                changedRestaurants.remove(restaurantData);
                            }
                        } else if (state == -1) {
                            if (((ImageButton) v).getDrawable().getConstantState().equals(
                                    getResources().getDrawable(R.drawable.black_heart).getConstantState())) {
                                ((ImageButton) v).setImageResource(R.drawable.unlike);
                                changedRestaurants.add(restaurantData);
                            } else {
                                ((ImageButton) v).setImageResource(R.drawable.black_heart);
                                changedRestaurants.remove(restaurantData);
                            }
                        }
                    }
                });

                relativeLayout.addView(resultBtn);
                frameLayout.addView(relativeLayout);
                frameLayout.addView(textView);
                frameLayout.addView(stateButton);

                // 設置 FrameLayout 的外邊距
                FrameLayout.LayoutParams frameLayoutParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                );
                frameLayoutParams.setMargins(40, 25, 40, 25);
                frameLayout.setLayoutParams(frameLayoutParams);

                // 將 FrameLayout 添加到 resultLayout
                resultLayout.addView(frameLayout);
            }
        }

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

            canvas.drawCircle(originalBitmap.getWidth() / 2, originalBitmap.getHeight() / 2, radius, paint);
            canvas.drawCircle(originalBitmap.getWidth() / 2, originalBitmap.getHeight() / 2, radius - borderWidth / 2, borderPaint);

            return new BitmapDrawable(getResources(), circleBitmap);
        }

        private Drawable loadResizeCircleLogo(String logoName, int resourceId, int sizeDp, int leftPaddingDp) {
            Resources resources = getResources();
            Drawable logoDrawable;

            // 加载logo
            if (resourceId != 0) {
                try {
                    logoDrawable = resources.getDrawable(resourceId);
                } catch (Resources.NotFoundException e) {
                    Log.w("RestaurantList", "Logo not found: " + logoName, e);
                    logoDrawable = resources.getDrawable(R.drawable.logonull);
                }
            } else {
                Log.w("RestaurantList", "Logo resource not found: " + logoName);
                logoDrawable = resources.getDrawable(R.drawable.logonull);
            }

            // 将dp转换为px
            float density = resources.getDisplayMetrics().density;
            int sizePx = Math.round(sizeDp * density);
            int leftPaddingPx = Math.round(leftPaddingDp * density);

            // 调整大小并保持宽高比
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

            // 计算缩放比例，保持宽高比
            float scale = (float) sizePx / Math.min(originalBitmap.getWidth(), originalBitmap.getHeight());
            int scaledWidth = Math.round(originalBitmap.getWidth() * scale);
            int scaledHeight = Math.round(originalBitmap.getHeight() * scale);

            Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, scaledWidth, scaledHeight, true);

            // 应用圆形效果
            Drawable circleDrawable = circle(new BitmapDrawable(resources, scaledBitmap));

            // 设置边界，保持左边距
            circleDrawable.setBounds(leftPaddingPx, 0, leftPaddingPx + sizePx, sizePx);

            return circleDrawable;
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            updateDatabase();
        }

        private void updateDatabase() {
            // 在主執行緒建立 SQLite 實例
            final SQLite dbHelper = new SQLite(activity.getApplicationContext());

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (RestaurantData restaurant : changedRestaurants) {
                            dbHelper.deleteRecord(userId, restaurant.getId());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public void onDetach() {
            super.onDetach();
            activity = null;
        }
    }
}
