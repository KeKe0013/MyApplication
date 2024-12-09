package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
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
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class user extends AppCompatActivity {
    private ImageButton allTag, list, map, tree;
    private Button btnCheckedIn, btnMemo;
    public int userId;
    double user_lat;
    double user_lng;

    private int[] userData;
    private String userName;
    private SQLite dbHelper;

    private ImageButton userPicture, editName;
    Function function = new Function();
    private int selectedPictureId = -1; // 用來記錄選中的圖片ID
    private TextView userNameView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        // 初始化 SQLite 數據庫助手
        dbHelper = new SQLite(this);

        // 從 Intent 中獲取使用者 ID
        userId = getIntent().getIntExtra("userId", 0);
        // 從 Intent 中獲取用戶座標
        user_lat = getIntent().getDoubleExtra("user_lat", 0);
        user_lng = getIntent().getDoubleExtra("user_lng", 0);


        // 從 SQLite 獲取用戶數據
        userData = dbHelper.getUserData(userId);
        userName = dbHelper.getUserName(userId);

        if (userData != null && userName != null) {
            int userPhotoId = userData[1];
            int userSignday = userData[2];
            int userDrop = userData[3];
            int userSignFlag = userData[4];

            // 使用獲取的數據更新UI
            updateUserInfo(userName, userPhotoId);
        }

        allTag = findViewById(R.id.allTag);
        list = findViewById(R.id.list);
        map = findViewById(R.id.map);
        tree = findViewById(R.id.tree);

        setupNavigationButtons();

        // 新增的按鈕和 Fragment 切換邏輯
        btnCheckedIn = findViewById(R.id.btnCheckedIn);
        btnCheckedIn.setSelected(true);
        btnMemo = findViewById(R.id.btnMemo);
        btnMemo.setSelected(false);

        btnCheckedIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(CheckedInFragment.newInstance(userId, user_lat, user_lng));
                btnCheckedIn.setSelected(true);
                btnMemo.setSelected(false);
            }
        });

        btnMemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(MemoFragment.newInstance(userId));
                btnCheckedIn.setSelected(false);
                btnMemo.setSelected(true);
            }
        });

        // 自動加載 CheckedInFragment
        loadFragment(CheckedInFragment.newInstance(userId, user_lat, user_lng));

        // 顯示 treeNum
        TextView treeNum = findViewById(R.id.tree_num);
        int treeCount = dbHelper.getTreeCount();
        treeNum.setText(String.valueOf(treeCount));

        // 設置編輯名稱按鈕
        editName = findViewById(R.id.edit_name);
        editName.setOnClickListener(v -> editUserName());
    }

    private void updateUserInfo(String userName, int photoId) {
        userNameView = findViewById(R.id.user_name);
        userNameView.setText(userName);

        String picture = "picture" + photoId;
        int pictureResourceId = getResources().getIdentifier(picture, "drawable", getPackageName());
        Drawable pictureDrawable = getResources().getDrawable(pictureResourceId);
        Drawable circleDrawable = circle(pictureDrawable);

        userPicture = findViewById(R.id.picture);
        if (userPicture != null) {
            userPicture.setImageDrawable(circleDrawable);

            // 點擊 userPicture 顯示圖片選擇對話框
            userPicture.setOnClickListener(v -> openPictureSelectionDialog());
        } else {
            Log.e("user", "user_picture is null");
        }

        // 可以在這裡添加其他用戶信息的更新
    }

    private void openPictureSelectionDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.picture_selection_dialog);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();

        RecyclerView recyclerView = dialog.findViewById(R.id.pictureRecyclerView);
        List<Integer> pictureIds = dbHelper.getAllPictureIds();

        ProfilePictureAdapter adapter = new ProfilePictureAdapter(this, pictureIds);
        CenterSnapLayoutManager layoutManager = new CenterSnapLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(pictureId -> {
            selectedPictureId = pictureId;
        });

        Button confirmBtn = dialog.findViewById(R.id.confirmBtn);
        confirmBtn.setOnClickListener(v -> confirmPictureSelection(dialog));

        Button cancelBtn = dialog.findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        // 初始滾動到"無限"列表的中間
        recyclerView.scrollToPosition(Integer.MAX_VALUE / 2);
    }

    private void confirmPictureSelection(Dialog dialog) {
        if (selectedPictureId != -1) {
            // 更新 userPicture
            int pictureResourceId = getResources().getIdentifier("picture" + selectedPictureId, "drawable", getPackageName());
            Drawable selectedDrawable = circle(getResources().getDrawable(pictureResourceId));
            userPicture.setImageDrawable(selectedDrawable);

            // 更新資料庫
            dbHelper.updateUserPicture(userId, selectedPictureId);

            // 關閉對話框
            dialog.dismiss();
        }
    }

    private void editUserName() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_edit_name); // 使用包含输入字段和按钮的布局
        dialog.show();

        EditText editText = dialog.findViewById(R.id.edit_name_input);
        final TextView counterTextView = dialog.findViewById(R.id.counter_text_view);
        final int maxLength = 10; // 名字的最大長度
        function.totalWord(editText, counterTextView, maxLength);
        editText.setText(userNameView.getText().toString()); // 設置當前用戶名到輸入框

        Button saveButton = dialog.findViewById(R.id.saveBtn);
        saveButton.setOnClickListener(v -> {
            String newUserName = editText.getText().toString().trim();
            new Thread(() -> {
                // 使用 SQLite 更新用戶名
                dbHelper.updateUserName(userId, newUserName);

                // 更新 UI
                runOnUiThread(() -> {
                    userNameView.setText(newUserName); // 更新 UI 上的用戶名
                });
            }).start();
            dialog.dismiss(); // 關閉對話框
        });

        Button cancelBtn = dialog.findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(view -> dialog.dismiss());
    }


    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
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
        borderPaint.setColor(Color.BLACK); // 設置外框顏色

        canvas.drawCircle(originalBitmap.getWidth() / 2, originalBitmap.getHeight() / 2, radius, paint);

        // 在圖形外繪製外框
        canvas.drawCircle(originalBitmap.getWidth() / 2, originalBitmap.getHeight() / 2, radius - borderWidth / 2, borderPaint);

        return new BitmapDrawable(getResources(), circleBitmap);
    }

    private void setupNavigationButtons() {
        allTag.setOnClickListener(view -> navigateTo(Tag.class));
        list.setOnClickListener(view -> navigateTo(list.class));
        map.setOnClickListener(view -> navigateTo(MainActivity.class));
        tree.setOnClickListener(view -> navigateTo(Tree.class));
    }

    private void navigateTo(Class<?> cls) {
        Intent intent = new Intent(user.this, cls);
        intent.putExtra("userId", userId);
        intent.putExtra("user_lat", user_lat);
        intent.putExtra("user_lng", user_lng);
        startActivity(intent);
    }


    public static class CheckedInFragment extends Fragment {
        private static final String CITY_HINT = "請選擇縣市";
        private static final String DISTRICT_HINT = "請選擇區域";

        private int userId;
        private double user_lat;
        private double user_lng;
        private ArrayList<RestaurantData> allRestaurants = new ArrayList<>();
        private LinearLayout containerLayout;

        private Spinner citySpinner;
        private Spinner districtSpinner;
        private ArrayAdapter<String> cityAdapter;
        private ArrayAdapter<String> districtAdapter;

        private Map<Integer, String> checkInTimes;
        private SQLite dbHelper;

        public static CheckedInFragment newInstance(int userId, double user_lat, double user_lng) {
            CheckedInFragment fragment = new CheckedInFragment();
            Bundle args = new Bundle();
            args.putInt("userId", userId);
            args.putDouble("user_lat", user_lat);
            args.putDouble("user_lng", user_lng);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                userId = getArguments().getInt("userId", -1);
                user_lat = getArguments().getDouble("user_lat", -1);
                user_lng = getArguments().getDouble("user_lng", -1);
                Log.e("location", String.valueOf(user_lat));
                Log.e("location", String.valueOf(user_lng));
                dbHelper = new SQLite(getActivity());
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_checked_in, container, false);
            containerLayout = view.findViewById(R.id.containerLayout);

            // 初始化Spinner
            citySpinner = view.findViewById(R.id.city_spinner);
            districtSpinner = view.findViewById(R.id.district_spinner);

            cityAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, new ArrayList<>());
            cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            citySpinner.setAdapter(cityAdapter);

            districtAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, new ArrayList<>());
            districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            districtSpinner.setAdapter(districtAdapter);

            cityAdapter.add(CITY_HINT);
            districtAdapter.add(DISTRICT_HINT);

            loadRestaurantData(userId);

            return view;
        }

        private void loadRestaurantData(int userId) {
            new Thread(() -> {
                try {
                    int today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
                    if (today == Calendar.SUNDAY) {
                        today = 7;
                    } else {
                        today -= 1;
                    }

                    allRestaurants = dbHelper.getCheckInRestaurant(userId, today);

                    List<Integer> restaurantIds = new ArrayList<>();
                    for (RestaurantData restaurantData : allRestaurants) {
                        restaurantIds.add(restaurantData.getId());
                    }

                    checkInTimes = dbHelper.getCheckInTimes(userId, restaurantIds);

                    getActivity().runOnUiThread(() -> updateUIAfterDataLoaded());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }

        private void updateUIAfterDataLoaded() {
            if (getContext() == null || allRestaurants == null) {
                return;
            }

            if (allRestaurants.isEmpty()) {
                // 顯示空狀態消息
                TextView emptyStateText = new TextView(getContext());
                emptyStateText.setText("您尚未簽到任何餐廳");
                emptyStateText.setGravity(Gravity.CENTER);
                containerLayout.addView(emptyStateText);
            } else {
                // 更新城市列表
                updateCitySpinner();

                // 重置區域選擇器
                resetDistrictSpinner();

                // 設置 Spinner 監聽器
                setupSpinners();

                // 更新餐廳列表
                updateRestaurantList(null, null);
            }
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
                public void onNothingSelected(AdapterView<?> parent) {
                }
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
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }

        private void resetDistrictSpinner() {
            districtAdapter.clear();
            districtAdapter.add(DISTRICT_HINT);
            districtAdapter.notifyDataSetChanged();
            districtSpinner.setSelection(0);
        }

        private void updateDistrictSpinner(String selectedCity) {
            districtAdapter.clear();
            districtAdapter.add(DISTRICT_HINT);

            // 使用 Set 來存儲唯一的區域
            Set<String> uniqueDistricts = new HashSet<>();

            for (RestaurantData restaurant : allRestaurants) {
                if (restaurant.getCity().equals(selectedCity)) {
                    uniqueDistricts.add(restaurant.getDistrict());
                }
            }

            // 將唯一的區域添加到適配器中
            for (String district : uniqueDistricts) {
                districtAdapter.add(district);
            }

            districtAdapter.notifyDataSetChanged();
            districtSpinner.setSelection(0);
        }

        private void updateRestaurantList(@Nullable String city, @Nullable String district) {
            containerLayout.removeAllViews();
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

            if (filteredRestaurants.isEmpty()) {
                TextView emptyStateText = new TextView(getContext());
                emptyStateText.setText("沒有符合條件的餐廳");
                emptyStateText.setGravity(Gravity.CENTER);
                containerLayout.addView(emptyStateText);
            } else {
                updateContainerLayout(filteredRestaurants, checkInTimes);
            }
        }

        private void updateContainerLayout(ArrayList<RestaurantData> restaurantDataList, Map<Integer, String> checkInTimes) {
            containerLayout.removeAllViews();
            int columnCount = 4; // 每行4個項目
            int buttonWidth = getResources().getDisplayMetrics().widthPixels / columnCount;

            LinearLayout currentRow = null;

            for (int i = 0; i < restaurantDataList.size(); i++) {
                RestaurantData restaurantData = restaurantDataList.get(i);

                if (i % columnCount == 0) {
                    currentRow = new LinearLayout(getContext());
                    currentRow.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));
                    currentRow.setOrientation(LinearLayout.HORIZONTAL);
                    containerLayout.addView(currentRow);
                }

                Button restaurantButton = new Button(getContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(buttonWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
                restaurantButton.setLayoutParams(params);
                restaurantButton.setPadding(16, 16, 16, 16);

                String logo = "logo_" + restaurantData.getId();

                @SuppressLint("DiscouragedApi")
                int logoResourceId = getResources().getIdentifier(logo, "drawable", getActivity().getPackageName());
                Drawable logoDrawable;

                if (logoResourceId != 0) {
                    try {
                        logoDrawable = getResources().getDrawable(logoResourceId, null);
                    } catch (Resources.NotFoundException e) {
                        // 如果找不到資源，使用預設圖片
                        logoDrawable = getResources().getDrawable(R.drawable.logonull, null);
                        Log.w("RestaurantList", "Logo resource not found: " + logo);
                    }
                } else {
                    // 如果資源ID為0，使用預設圖片
                    logoDrawable = getResources().getDrawable(R.drawable.logonull, null);
                    Log.w("RestaurantList", "Logo resource ID is 0 for: " + logo);
                }

                Drawable circleDrawable = circle(logoDrawable);
                circleDrawable.setBounds(0, 16, 200, 200);

                String name = restaurantData.getName();
                if (name.length() > 5) {
                    name = name.substring(0, 4) + "...";
                }
                String time = checkInTimes.getOrDefault(restaurantData.getId(), "未簽到");

                SpannableStringBuilder builder = new SpannableStringBuilder();

                // 添加店名
                SpannableString nameSpan = new SpannableString(name + "\n");
                nameSpan.setSpan(new RelativeSizeSpan(1.4f), 0, name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.append(nameSpan);

                // 添加日期
                SpannableString timeSpan = new SpannableString(time);
                timeSpan.setSpan(new RelativeSizeSpan(1.2f), 0, time.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.append(timeSpan);

                restaurantButton.setText(builder);
                restaurantButton.setTextSize(12);
                restaurantButton.setLines(3);
                restaurantButton.setGravity(Gravity.CENTER);
                restaurantButton.setCompoundDrawables(null, circleDrawable, null, null);
                restaurantButton.setCompoundDrawablePadding(16);

                restaurantButton.setOnClickListener(v -> {
                    Intent intent = new Intent(getContext(), res_info.class);
                    intent.putExtra("restaurant_data", restaurantData);
                    intent.putExtra("userId", userId);
                    intent.putExtra("user_lat", user_lat);
                    intent.putExtra("user_lng", user_lng);
                    intent.putExtra("state", restaurantData.getState());
                    intent.putExtra("likeCount", restaurantData.getLikeCount());
                    intent.putExtra("unlikeCount", restaurantData.getUnlikeCount());
                    startActivity(intent);
                });

                currentRow.addView(restaurantButton);
            }
            // 填充最後一行的空白
            int remainingButtons = columnCount - (restaurantDataList.size() % columnCount);
            if (remainingButtons < columnCount) {
                for (int i = 0; i < remainingButtons; i++) {
                    View spacer = new View(getContext());
                    spacer.setLayoutParams(new LinearLayout.LayoutParams(buttonWidth, 1));
                    currentRow.addView(spacer);
                }
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
    }

    public static class MemoFragment extends Fragment {
        private int userId;
        private int lineCount;
        private LinearLayout memoLayout;
        private Activity mActivity;
        private ArrayList<RestaurantData> allRestaurants = new ArrayList<>();
        private Set<String> uniqueTags = new HashSet<>();
        private Map<Integer, List<String>> restaurantTags = new HashMap<>(); // 存儲餐廳ID和對應的標籤
        private Spinner spinner;
        Function function = new Function();
        private SQLite dbHelper;

        public static MemoFragment newInstance(int userId) {
            MemoFragment fragment = new MemoFragment();
            Bundle args = new Bundle();
            args.putInt("userId", userId);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                userId = getArguments().getInt("userId", -1);
                dbHelper = new SQLite(getActivity());
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_memo, container, false);
            memoLayout = view.findViewById(R.id.containerLayout);
            spinner = view.findViewById(R.id.memo_spinner);
            mActivity = getActivity();

            return view;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            loadMemos();
        }

        private void updateSpinner() {
            List<String> tagList = new ArrayList<>(uniqueTags);
            tagList.add(0, "請選擇分類");
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, tagList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedTag = (String) parent.getItemAtPosition(position);
                    filterMemosByTag(selectedTag);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // 不做任何事
                }
            });
        }

        private void filterMemosByTag(String selectedTag) {
            memoLayout.removeAllViews();
            if (selectedTag.equals("請選擇分類")) {
                displayAllMemos();
            } else {
                for (RestaurantData restaurantData : allRestaurants) {
                    List<String> tags = restaurantData.getTags();
                    if (tags != null && tags.contains(selectedTag)) {
                        addMemosToLayout(restaurantData);
                    }
                }
            }
        }

        private void loadMemos() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        int today = getTodayDayOfWeek();

                        allRestaurants = dbHelper.getMemoRestaurant(userId, today);

                        // 處理所有餐廳的標籤
                        uniqueTags = new HashSet<>();
                        for (RestaurantData restaurant : allRestaurants) {
                            List<String> tags = restaurant.getTags();
                            restaurantTags.put(restaurant.getId(), tags);
                            uniqueTags.addAll(tags);
                        }

                        // 在主線程上更新UI
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateSpinner();
                                    displayAllMemos();
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // 顯示錯誤消息
                                    Toast.makeText(getActivity(), "加載失敗，請稍後重試", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }
            }).start();
        }

        private int getTodayDayOfWeek() {
            int today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
            return (today == Calendar.SUNDAY) ? 7 : today - 1;
        }

        private void displayAllMemos() {
            memoLayout.removeAllViews();

            for (RestaurantData restaurantData : allRestaurants) {
                addMemosToLayout(restaurantData);
            }
        }

        private void addMemosToLayout(RestaurantData restaurantData) {
            // 創建主要的 FrameLayout
            FrameLayout memoItem = new FrameLayout(mActivity);
            FrameLayout.LayoutParams memoParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );
            memoParams.setMargins(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8));
            memoItem.setLayoutParams(memoParams);

            // 創建 logo ImageView
            ImageView logoView = new ImageView(mActivity);
            int logoSize = dpToPx(50);
            FrameLayout.LayoutParams logoParams = new FrameLayout.LayoutParams(logoSize, logoSize);
            logoParams.gravity = Gravity.START | Gravity.TOP;
            logoView.setLayoutParams(logoParams);

            // 設置 logo
            String logo = "logo_" + restaurantData.getId();
            int logoResourceId = getResources().getIdentifier(logo, "drawable", mActivity.getPackageName());
            Drawable logoDrawable = ContextCompat.getDrawable(mActivity, logoResourceId);
            Drawable circleDrawable = circle(logoDrawable);
            circleDrawable.setBounds(0, 0, 200, 200);
            logoView.setImageDrawable(circleDrawable);

            // 創建店名 TextView
            TextView nameView = new TextView(mActivity);
            nameView.setText(restaurantData.getName());
            nameView.setTextSize(18);
            nameView.setTypeface(null, Typeface.BOLD);
            FrameLayout.LayoutParams nameParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );
            nameParams.setMargins(dpToPx(66), 0, 0, 0);
            nameParams.gravity = Gravity.START | Gravity.TOP;
            nameView.setLayoutParams(nameParams);

            // 創建包含內容和日期的LinearLayout
            LinearLayout contentDateLayout = new LinearLayout(mActivity);
            contentDateLayout.setOrientation(LinearLayout.HORIZONTAL);
            FrameLayout.LayoutParams contentDateParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );
            contentDateParams.setMargins(dpToPx(66), dpToPx(30), dpToPx(16), 0);
            contentDateLayout.setLayoutParams(contentDateParams);

            // 創建內容 Button
            Button contentButton = new Button(mActivity);
            contentButton.setText(restaurantData.getMemo_content());
            contentButton.setTextSize(16);
            contentButton.setAllCaps(false);

            // 設置自動換行
            contentButton.setSingleLine(false);
            contentButton.setMaxLines(9); // 限制最大行數

            // 創建編輯和刪除按鈕的LinearLayout
            LinearLayout actionLayout = new LinearLayout(mActivity);
            actionLayout.setOrientation(LinearLayout.HORIZONTAL);

            // 動態計算高度
            contentButton.post(new Runnable() {
                @Override
                public void run() {
                    // 計算文字高度
                    lineCount = contentButton.getLineCount();
                    Log.e("lineCount1", String.valueOf(lineCount));
                    int baseHeight = dpToPx(50); // 基礎高度
                    int additionalHeightPerLine = dpToPx(25); // 每行增加的高度

                    LinearLayout.LayoutParams buttonParams = (LinearLayout.LayoutParams) contentButton.getLayoutParams();
                    buttonParams.height = baseHeight + (lineCount - 1) * additionalHeightPerLine;
                    contentButton.setLayoutParams(buttonParams);

                    FrameLayout.LayoutParams actionParams = new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT
                    );
                    actionParams.gravity = Gravity.END | Gravity.BOTTOM;
                    actionParams.setMargins(0, dpToPx(80+(25*(lineCount-1))), dpToPx(90), 0);
                    actionLayout.setLayoutParams(actionParams);
                }
            });

            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
            );
            contentButton.setLayoutParams(buttonParams);

            // 設置 Button 的圓角背景
            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.RECTANGLE);
            shape.setColor(Color.parseColor("#ADD8E6"));
            shape.setCornerRadius(dpToPx(15));
            contentButton.setBackground(shape);

            // 創建日期 LinearLayout
            LinearLayout dateLayout = new LinearLayout(mActivity);
            dateLayout.setOrientation(LinearLayout.VERTICAL);
            dateLayout.setGravity(Gravity.BOTTOM);
            LinearLayout.LayoutParams dateLayoutParams = new LinearLayout.LayoutParams(
                    dpToPx(80),
                    LinearLayout.LayoutParams.MATCH_PARENT
            );
            dateLayout.setLayoutParams(dateLayoutParams);

            // 創建時間 TextView
            TextView timeView = new TextView(mActivity);
            String fullTime = restaurantData.getMemo_time();
            String truncatedTime = fullTime.length() > 10 ? fullTime.substring(0, 10) : fullTime;
            timeView.setText(truncatedTime);
            timeView.setTextSize(14);
            timeView.setTextColor(Color.GRAY);
            timeView.setGravity(Gravity.BOTTOM | Gravity.END);
            dateLayout.addView(timeView);

            contentDateLayout.addView(contentButton);
            contentDateLayout.addView(dateLayout);

//            // 創建編輯和刪除按鈕的LinearLayout
//            LinearLayout actionLayout = new LinearLayout(mActivity);
//            actionLayout.setOrientation(LinearLayout.HORIZONTAL);
//            FrameLayout.LayoutParams actionParams = new FrameLayout.LayoutParams(
//                    FrameLayout.LayoutParams.WRAP_CONTENT,
//                    FrameLayout.LayoutParams.WRAP_CONTENT
//            );
//            actionParams.gravity = Gravity.END | Gravity.BOTTOM;
//            Log.e("LINE", String.valueOf(lineCount));
//            actionParams.setMargins(0, dpToPx(75 * lineCount), dpToPx(100), 0);
//            actionLayout.setLayoutParams(actionParams);

            Button editButton = createActionButton("編輯");
            actionLayout.addView(editButton);

            Button deleteButton = createActionButton("刪除");
            actionLayout.addView(deleteButton);

            // 設置點擊區域
            setEditBtnClickListener(editButton, restaurantData);
            setDeleteBtnClickListener(deleteButton, restaurantData);

            // 將所有視圖添加到主 FrameLayout
            memoItem.addView(logoView);
            memoItem.addView(nameView);
            memoItem.addView(contentDateLayout);
            memoItem.addView(actionLayout);

            // 將 memoItem 添加到主佈局
            memoLayout.addView(memoItem);
        }
        //        private void addMemosToLayout(RestaurantData restaurantData) {
//            // 創建主要的 FrameLayout
//            FrameLayout memoItem = new FrameLayout(mActivity);
//            FrameLayout.LayoutParams memoParams = new FrameLayout.LayoutParams(
//                    FrameLayout.LayoutParams.MATCH_PARENT,
//                    FrameLayout.LayoutParams.WRAP_CONTENT
//            );
//            memoParams.setMargins(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8));
//            memoItem.setLayoutParams(memoParams);
//
//            // 創建 logo ImageView
//            ImageView logoView = new ImageView(mActivity);
//            int logoSize = dpToPx(50);
//            FrameLayout.LayoutParams logoParams = new FrameLayout.LayoutParams(logoSize, logoSize);
//            logoParams.gravity = Gravity.START | Gravity.TOP;
//            logoView.setLayoutParams(logoParams);
//
//            // 設置 logo
//            String logo = "logo_" + restaurantData.getId();
//            int logoResourceId = getResources().getIdentifier(logo, "drawable", mActivity.getPackageName());
//            Drawable logoDrawable = ContextCompat.getDrawable(mActivity, logoResourceId);
//            logoView.setImageDrawable(logoDrawable);
//
//            // 創建店名 TextView
//            TextView nameView = new TextView(mActivity);
//            nameView.setText(restaurantData.getName());
//            nameView.setTextSize(18);
//            nameView.setTypeface(null, Typeface.BOLD);
//            FrameLayout.LayoutParams nameParams = new FrameLayout.LayoutParams(
//                    FrameLayout.LayoutParams.WRAP_CONTENT,
//                    FrameLayout.LayoutParams.WRAP_CONTENT
//            );
//            nameParams.setMargins(dpToPx(66), 0, 0, 0);
//            nameParams.gravity = Gravity.START | Gravity.TOP;
//            nameView.setLayoutParams(nameParams);
//
//            // 創建包含內容和日期的LinearLayout
//            LinearLayout contentDateLayout = new LinearLayout(mActivity);
//            contentDateLayout.setOrientation(LinearLayout.HORIZONTAL);
//            FrameLayout.LayoutParams contentDateParams = new FrameLayout.LayoutParams(
//                    FrameLayout.LayoutParams.MATCH_PARENT,
//                    FrameLayout.LayoutParams.WRAP_CONTENT
//            );
//            contentDateParams.setMargins(dpToPx(66), dpToPx(30), dpToPx(16), 0);
//            contentDateLayout.setLayoutParams(contentDateParams);
//
//            // 創建內容 Button
//            Button contentButton = new Button(mActivity);
//            contentButton.setText(restaurantData.getMemo_content());
//            contentButton.setTextSize(16);
//            contentButton.setAllCaps(false);
//
//            // 設置自動換行
//            contentButton.setSingleLine(false);
//            contentButton.setMaxLines(9); // 限制最大行數
//
//            // 動態計算高度
//            contentButton.post(new Runnable() {
//                @Override
//                public void run() {
//                    // 計算文字高度
//                    int lineCount = contentButton.getLineCount();
//                    int baseHeight = dpToPx(50); // 基礎高度
//                    int additionalHeightPerLine = dpToPx(25); // 每行增加的高度
//
//                    LinearLayout.LayoutParams buttonParams = (LinearLayout.LayoutParams) contentButton.getLayoutParams();
//                    buttonParams.height = baseHeight + (lineCount - 1) * additionalHeightPerLine;
//                    contentButton.setLayoutParams(buttonParams);
//                }
//            });
//
//            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
//                    0,
//                    LinearLayout.LayoutParams.WRAP_CONTENT,
//                    1f
//            );
//            contentButton.setLayoutParams(buttonParams);
//
//            // 設置 Button 的圓角背景
//            GradientDrawable shape = new GradientDrawable();
//            shape.setShape(GradientDrawable.RECTANGLE);
//            shape.setColor(Color.parseColor("#ADD8E6"));
//            shape.setCornerRadius(dpToPx(15));
//            contentButton.setBackground(shape);
//
//            // 創建日期 LinearLayout
//            LinearLayout dateLayout = new LinearLayout(mActivity);
//            dateLayout.setOrientation(LinearLayout.VERTICAL);
//            dateLayout.setGravity(Gravity.BOTTOM);
//            LinearLayout.LayoutParams dateLayoutParams = new LinearLayout.LayoutParams(
//                    dpToPx(80),
//                    LinearLayout.LayoutParams.MATCH_PARENT
//            );
//            dateLayout.setLayoutParams(dateLayoutParams);
//
//            // 創建時間 TextView
//            TextView timeView = new TextView(mActivity);
//            String fullTime = restaurantData.getMemo_time();
//            String truncatedTime = fullTime.length() > 10 ? fullTime.substring(0, 10) : fullTime;
//            timeView.setText(truncatedTime);
//            timeView.setTextSize(14);
//            timeView.setTextColor(Color.GRAY);
//            timeView.setGravity(Gravity.BOTTOM | Gravity.END);
//            dateLayout.addView(timeView);
//
//            contentDateLayout.addView(contentButton);
//            contentDateLayout.addView(dateLayout);
//
//            // 創建編輯和刪除按鈕的LinearLayout
//            LinearLayout actionLayout = new LinearLayout(mActivity);
//            actionLayout.setOrientation(LinearLayout.HORIZONTAL);
//            FrameLayout.LayoutParams actionParams = new FrameLayout.LayoutParams(
//                    FrameLayout.LayoutParams.WRAP_CONTENT,
//                    FrameLayout.LayoutParams.WRAP_CONTENT
//            );
//            actionParams.gravity = Gravity.END | Gravity.BOTTOM;
//            actionParams.setMargins(0, dpToPx(75), dpToPx(100), 0);
//            actionLayout.setLayoutParams(actionParams);
//
//            Button editButton = createActionButton("編輯");
//            actionLayout.addView(editButton);
//
//            Button deleteButton = createActionButton("刪除");
//            actionLayout.addView(deleteButton);
//
//            // 設置點擊區域
//            setEditBtnClickListener(editButton, restaurantData);
//            setDeleteBtnClickListener(deleteButton, restaurantData);
//
//            // 將所有視圖添加到主 FrameLayout
//            memoItem.addView(logoView);
//            memoItem.addView(nameView);
//            memoItem.addView(contentDateLayout);
//            memoItem.addView(actionLayout);
//
//            // 將 memoItem 添加到主佈局
//            memoLayout.addView(memoItem);
//        }
        private void setEditBtnClickListener(Button editButton, RestaurantData restaurantData) {
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Dialog dialog = new Dialog(mActivity);
                    dialog.setContentView(R.layout.edit_note_dialog);
                    dialog.show();

                    EditText editText = dialog.findViewById(R.id.dialog_input);
                    final TextView counterTextView = dialog.findViewById(R.id.counter_text_view);
                    final int maxLength = 100;
                    function.totalWord(editText, counterTextView, maxLength);
                    editText.setText(restaurantData.getMemo_content());

                    Button saveButton = dialog.findViewById(R.id.saveBtn);
                    saveButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String memoText = editText.getText().toString().trim();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    dbHelper.editMemo(restaurantData.getMemo_id(), memoText, function.getCurrentTime());

                                    // 在主線程中重新加載所有備忘錄
                                    mActivity.runOnUiThread(() -> {
                                        loadMemos(); // 重新載入所有備忘錄
                                        dialog.dismiss(); // 關閉對話框
                                    });
                                }
                            }).start();
                        }
                    });

                    Button cancelBtn = dialog.findViewById(R.id.cancelBtn);
                    cancelBtn.setOnClickListener(view -> dialog.dismiss());
                }
            });
        }


        private void setDeleteBtnClickListener(Button deleteButton, RestaurantData restaurantData) {
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Dialog dialog = new Dialog(mActivity);
                    dialog.setContentView(R.layout.delete_input_dialog);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.show();

                    Button confirmButton = dialog.findViewById(R.id.confirmBtn);
                    confirmButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    dbHelper.deleteMemo(restaurantData.getMemo_id());

                                    // 在主線程中重新加載所有備忘錄
                                    mActivity.runOnUiThread(() -> {
                                        loadMemos(); // 重新載入所有備忘錄
                                        dialog.dismiss(); // 關閉對話框
                                    });
                                }
                            }).start();
                        }
                    });

                    Button cancelBtn = dialog.findViewById(R.id.cancelBtn);
                    cancelBtn.setOnClickListener(view -> dialog.dismiss());
                }
            });
        }

        private Button createActionButton(String text) {
            Button button = new Button(mActivity);
            button.setText(text);
            button.setTextSize(14);
            button.setAllCaps(false);
            button.setBackgroundColor(Color.TRANSPARENT); // 移除背景
            button.setTextColor(Color.GRAY); // 或其他您想要的顏色

            int width = dpToPx(50); // 設定寬度為50dp
            int height = dpToPx(25); // 設定高度為25dp

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
            // 設定按鈕的內邊距
            int padding = dpToPx(2); // 設定內邊距為2dp
            button.setPadding(padding, padding, padding, padding);
            button.setLayoutParams(params);
            return button;
        }

        private int dpToPx(int dp) {
            float density = mActivity.getResources().getDisplayMetrics().density;
            return Math.round((float) dp * density);
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
            borderPaint.setColor(Color.BLACK); // 設置外框顏色

            canvas.drawCircle(originalBitmap.getWidth() / 2, originalBitmap.getHeight() / 2, radius, paint);

            // 在圖形外繪製外框
            canvas.drawCircle(originalBitmap.getWidth() / 2, originalBitmap.getHeight() / 2, radius - borderWidth / 2, borderPaint);

            return new BitmapDrawable(getResources(), circleBitmap);
        }
    }
}
