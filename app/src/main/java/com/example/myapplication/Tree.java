package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.atomic.AtomicIntegerArray;

public class Tree extends AppCompatActivity {
    private ImageButton allTag, list, map, user, waterBtn, taskBtn, appreciateBtn;
    private ImageView treeView;
    private View layout;
    private Button harvestBtn;
    private ProgressBar progressBar;

    Function function = new Function();
    public int userId;
    private double user_lat;
    private double user_lng;
    private int[] treeData;
    private int[] userData;
    private String userName;

    private AtomicIntegerArray userAtomic;
    private AtomicIntegerArray treeAtomic;

    private SQLite dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tree);

        // 初始化 SQLite
        dbHelper = new SQLite(this);

        // 從 Intent 中獲取使用者 ID
        userId = getIntent().getIntExtra("userId", 0);
        // 從 Intent 中獲取用戶座標
        user_lat = getIntent().getDoubleExtra("user_lat", 0);
        user_lng = getIntent().getDoubleExtra("user_lng", 0);

        // 初始化 UI 元素
        initializeUIElements();


        loadDataFromSQLite();


        setupUI();


        setupNavigationButtons();

        checkDataConsistency();
    }

    private void initializeUIElements() {
        layout = findViewById(R.id.background);
        treeView = findViewById(R.id.treeView);
        progressBar = findViewById(R.id.progressBar);
        taskBtn = findViewById(R.id.taskBtn);
        appreciateBtn = findViewById(R.id.thank_certificate);
        harvestBtn = findViewById(R.id.harvestBtn);
        waterBtn = findViewById(R.id.waterBtn);
        allTag = findViewById(R.id.allTag);
        list = findViewById(R.id.list);
        map = findViewById(R.id.map);
        user = findViewById(R.id.user);
    }

    private void loadDataFromSQLite() {
        userData = dbHelper.getUserData(userId);
        userName = dbHelper.getUserName(userId);

        treeData = dbHelper.getTreeData(userId);

        if (userData == null || treeData == null) {
            finish();
            return;
        }

        int[] userMayChange = new int[3];
        userMayChange[0] = userData[3]; // drop
        userMayChange[1] = userData[2]; // signday
        userMayChange[2] = userData[4]; // sign_flag
        userAtomic = new AtomicIntegerArray(userMayChange);

        treeAtomic = new AtomicIntegerArray(treeData);
    }

    private void setupUI() {
        TextView dropNum = findViewById(R.id.dropNum);
        TextView treeBar = findViewById(R.id.treeBar);

        dropNum.setText(String.valueOf(userAtomic.get(0)));
        changeTreePhoto(treeAtomic.get(2), treeView, layout);

        setupWaterButton(dropNum, treeBar);
        setupTaskButton(dropNum);
        setupHarvestButton(treeBar);
        setupAppreciateButton();

        updateUIBasedOnTreeStage(treeBar);
    }

    private void setupWaterButton(TextView dropNum, TextView treeBar) {
        waterBtn.setOnClickListener(view -> {
            if ((userAtomic.get(0) + treeAtomic.get(1)) >= 2000) {
                handleFullWatering(dropNum);
            } else {
                handlePartialWatering(dropNum, treeBar);
            }
        });
    }

    private void handleFullWatering(TextView dropNum) {
        int newDropNum = userAtomic.get(0) + treeAtomic.get(1) - 2000;
        new Thread(() -> {
            dbHelper.updateDataInTransaction(userId, userName, userData[1], userData[2], newDropNum, userData[4], treeAtomic.get(0), 2000, treeAtomic.get(2));
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                findViewById(R.id.treeBar).setVisibility(View.GONE);
                waterBtn.setEnabled(false);
                taskBtn.setEnabled(false);
                harvestBtn.setVisibility(View.VISIBLE);
                userAtomic.set(0, newDropNum);
                treeAtomic.set(1, 2000);
                dropNum.setText(String.valueOf(userAtomic.get(0)));
            });
        }).start();
    }

    private void handlePartialWatering(TextView dropNum, TextView treeBar) {
        new Thread(() -> {
            if (userAtomic.get(0) != 0) {
                int newTreeWater = treeAtomic.get(1) + userAtomic.get(0);
                dbHelper.updateDataInTransaction(userId, userName, userData[1], userData[2], 0, userData[4], treeAtomic.get(0), newTreeWater, treeAtomic.get(2));

                runOnUiThread(() -> {
                    dropNum.setText("0");
                    int barMax = function.getBarMax(treeAtomic.get(2), newTreeWater);
                    treeAtomic.set(1, newTreeWater);
                    updateTreeStageIfNeeded(barMax);
                    updateTreeUI(treeBar, barMax);
                    userAtomic.set(0, 0);
                });
            }
        }).start();
    }

    private void updateTreeStageIfNeeded(int barMax) {
        if (treeAtomic.get(1) >= barMax && treeAtomic.get(2) < 4) {
            int newStage = function.getNewTreeStage(treeAtomic.get(1));
            treeAtomic.set(2, newStage);
            dbHelper.updateDataInTransaction(userId, userName, userData[1], userData[2], userAtomic.get(0), userData[4], treeAtomic.get(0), treeAtomic.get(1), newStage);
            changeTreePhoto(newStage, treeView, layout);
        } else if (treeAtomic.get(2) == 0) {
            int newStage = function.getNewTreeStage(treeAtomic.get(1));
            treeAtomic.set(2, newStage);
            dbHelper.updateDataInTransaction(userId, userName, userData[1], userData[2], userAtomic.get(0), userData[4], treeAtomic.get(0), treeAtomic.get(1), newStage);
            changeTreePhoto(newStage, treeView, layout);
        }
    }

    private void updateTreeUI(TextView treeBar, int barMax) {
        treeBar.setText(treeAtomic.get(1) + " / " + barMax);
        progressBar.setMax(function.getBarMax(treeAtomic.get(2), treeAtomic.get(1)));
        progressBar.setProgress(treeAtomic.get(1));
    }

    private void setupTaskButton(TextView dropNum) {
        taskBtn.setOnClickListener(view -> {
            int[] results = function.show_taskDialog(Tree.this, userId, userAtomic, dropNum, taskBtn);
            userAtomic.set(0, results[0]);
            userAtomic.set(1, results[1]);
            userAtomic.set(2, results[2]);
            dbHelper.updateDataInTransaction(userId, userName, userData[1], results[1], results[0], results[2], treeAtomic.get(0), treeAtomic.get(1), treeAtomic.get(2));
        });
    }

    private void setupHarvestButton(TextView treeBar) {
        harvestBtn.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            treeBar.setVisibility(View.VISIBLE);
            function.show_donateDialog(Tree.this, treeAtomic.get(0), userId, harvestBtn, waterBtn, taskBtn, userAtomic.get(2), (newTreeId, newTreeBar, newTreeStage) -> {
                treeAtomic.set(0, newTreeId);
                treeAtomic.set(1, newTreeBar);
                treeAtomic.set(2, newTreeStage);
                dbHelper.updateDataInTransaction(userId, userName, userData[1], userData[2], userAtomic.get(0), userAtomic.get(2), newTreeId, newTreeBar, newTreeStage);
                runOnUiThread(() -> {
                    changeTreePhoto(newTreeStage, treeView, layout);
                    int barMax = function.getBarMax(newTreeStage, newTreeBar);
                    treeBar.setText(newTreeBar + " / " + barMax);
                    progressBar.setMax(barMax);
                    progressBar.setProgress(newTreeBar);
                });
            });
        });
    }

    private void setupAppreciateButton() {
        appreciateBtn.setOnClickListener(view -> {
            new Thread(() -> {
                int donateNum = dbHelper.totalDonateNum();
                new Handler(Looper.getMainLooper()).post(() ->
                        function.show_appreciateDialog(Tree.this, donateNum)
                );
            }).start();
        });
    }

    private void updateUIBasedOnTreeStage(TextView treeBar) {
        if (treeAtomic.get(2) == 5) {
            setupUIForFinalStage();
        } else {
            setupUIForGrowingStage(treeBar);
        }
    }

    private void setupUIForFinalStage() {
        progressBar.setVisibility(View.GONE);
        findViewById(R.id.treeBar).setVisibility(View.GONE);
        waterBtn.setEnabled(false);
        taskBtn.setEnabled(false);
        waterBtn.setImageResource(R.drawable.unclick_sprinkler);
        taskBtn.setImageResource(userAtomic.get(2) == 1 ? R.drawable.unclick_taskfinish : R.drawable.unclick_task);
        harvestBtn.setVisibility(View.VISIBLE);
    }

    private void setupUIForGrowingStage(TextView treeBar) {
        int barMax = function.getBarMax(treeAtomic.get(2), treeAtomic.get(1));
        treeBar.setText(treeAtomic.get(1) + " / " + barMax);
        progressBar.setMax(barMax);
        progressBar.setProgress(treeAtomic.get(1));
        if (userAtomic.get(2) == 1) {
            taskBtn.setImageResource(R.drawable.task_finish);
            taskBtn.setBackground(null);
        }
    }

    private void setupNavigationButtons() {
        allTag.setOnClickListener(view -> navigateTo(Tag.class));
        list.setOnClickListener(view -> navigateTo(list.class));
        map.setOnClickListener(view -> navigateTo(MainActivity.class));
        user.setOnClickListener(view -> navigateTo(user.class));
    }

    private void navigateTo(Class<?> cls) {
        Intent intent = new Intent(Tree.this, cls);
        intent.putExtra("userId", userId);
        intent.putExtra("user_lat", user_lat);
        intent.putExtra("user_lng", user_lng);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveDataToSQLite();
    }

    private void saveDataToSQLite() {
        dbHelper.updateDataInTransaction(userId, userName, userData[1], userAtomic.get(1), userAtomic.get(0), userAtomic.get(2), treeAtomic.get(0), treeAtomic.get(1), treeAtomic.get(2));
    }

    public void changeTreePhoto(int treeStage, ImageView treeView, View layout) {
        String background = "tree_background" + treeStage;
        int backgroundResourceId = getResources().getIdentifier(background, "drawable", getPackageName());
        layout.setBackgroundResource(backgroundResourceId);

        if (treeStage != 0) {
            String treeviewImg = "tree" + treeStage;
            int treeViewResourceId = getResources().getIdentifier(treeviewImg, "drawable", getPackageName());
            treeView.setImageResource(treeViewResourceId);
            int[] treeSize = function.treeViewSize(treeStage);
            ViewGroup.MarginLayoutParams treeParams = (ViewGroup.MarginLayoutParams) treeView.getLayoutParams();
            treeParams.width = treeSize[0];
            treeParams.height = treeSize[1];
            treeParams.setMargins(0, treeSize[3], 0, treeSize[4]);
            treeParams.setMarginStart(treeSize[2]);
            treeView.setLayoutParams(treeParams);
        } else {
            treeView.setImageResource(0);
        }
    }

    private void checkDataConsistency() {
        new Thread(() -> {
            int[] latestUserData = dbHelper.getUserData(userId);
            int[] latestTreeData = dbHelper.getTreeData(userId);

            runOnUiThread(() -> {
                if (latestUserData != null && latestTreeData != null) {
                    userAtomic.set(0, latestUserData[3]); // drop
                    treeAtomic.set(1, latestTreeData[1]); // tree water
                    updateUIBasedOnTreeStage((TextView) findViewById(R.id.treeBar));
                }
            });
        }).start();
    }
}
