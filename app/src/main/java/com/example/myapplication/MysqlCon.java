package com.example.myapplication;

import android.util.Log;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MysqlCon {

    // 資料庫定義
    String mysql_ip = "db4free.net";
    int mysql_port = 3306; // Port 預設為 3306
    String db_name = "sqlsql";
    String url = "jdbc:mysql://" + mysql_ip + ":" + mysql_port + "/" + db_name;
    String db_user = "a1103353";
    String db_password = "l20200103";

    public void run() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Log.v("DB","加載驅動成功");
        }catch( ClassNotFoundException e) {
            Log.e("DB","加載驅動失敗");
            return;
        }

        // 連接資料庫
        try {
            Connection con = DriverManager.getConnection(url,db_user,db_password);
            Log.v("DB","遠端連接成功");
        }catch(SQLException e) {
            Log.e("DB","遠端連接失敗");
            Log.e("DB", e.toString());
        }
    }

    /**取得當天營業時間**/
    public String[][] getBusinessTime(int restaurant_id,int dayOfWeek) {
        String result[][] = new String[7][2];
        String sql = "SELECT * FROM business_hours WHERE restaurant_id = ? " +
                     "ORDER BY " +
                     "  CASE " +
                     "    WHEN week > ? THEN week - ? " +
                     "    ELSE week + (7-?) " +
                     "  END";

        try (Connection con = DriverManager.getConnection(url, db_user, db_password);
             PreparedStatement preparedStatement = con.prepareStatement(sql)) {
            preparedStatement.setInt(1, restaurant_id);
            preparedStatement.setInt(2, dayOfWeek);
            preparedStatement.setInt(3, dayOfWeek);
            preparedStatement.setInt(4, dayOfWeek);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                int index = 0;
                while (rs.next()) {
                    result[index][0] = rs.getString("week");
                    result[index][1] = rs.getString("open_time");
                    index++;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**計算是否打卡過**/
    public int[] check_in_count(int u_id, int r_id) {
        int result[] = new int[2];
        String sql = "SELECT COUNT(*) AS count ,SUM(count) AS sum FROM check_in_record WHERE user_id = ? AND restaurant_id = ?";
        try (Connection con = DriverManager.getConnection(url, db_user, db_password);
             PreparedStatement preparedStatement = con.prepareStatement(sql)) {
            preparedStatement.setInt(1, u_id);
            preparedStatement.setInt(2, r_id);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    result[0] = rs.getInt("count");
                    if (rs.getObject("sum") != null) {
                        result[1] = rs.getInt("sum");
                    } else {
                        result[1] = 0;
                    }
                    Log.v("count", String.valueOf(result[0]));
                    Log.v("sum", String.valueOf(result[1]));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**更新打卡紀錄**/
    public void CheckInTime(String currentTime,int u_id,int r_id,int count,int sum){
        String sql = "";
        String aaa = "";
        try {
            Connection con = DriverManager.getConnection(url, db_user, db_password);
            if(count == 0){
                sql = "INSERT INTO check_in_record(user_id,restaurant_id,first_flag,time,count) VALUES ('" + u_id + "','" + r_id + "','" + 1 + "','" + currentTime + "','" + 1 +"' )";
                aaa = "第一次";
            }else if(count == 1){
                sql = "INSERT INTO check_in_record(user_id,restaurant_id,first_flag,time,count) VALUES ('" + u_id + "','" + r_id + "','" + 0 + "','" + currentTime + "','" + 1 +"' )";
                aaa = "第二次";
            }
            else {
                sql = " UPDATE check_in_record SET time = '" + currentTime + "' ,count = '" + sum + "' WHERE user_id = '" + u_id + "' AND restaurant_id ='" + r_id + "' AND first_flag = 0";
                aaa = "上次";
            }
            Statement st = con.createStatement();
            st.executeUpdate(sql);
            st.close();
            Log.v("DB", "寫入打卡紀錄完成："+aaa);
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e("DB", "寫入打卡紀錄失敗");
            Log.e("DB", e.toString());
        }
    }

    /**取得第一次、上次打卡時間**/
    public String[] getTime(int U_id, int R_id,int count) {
        String result[] = new String[2];
        String sql = "";
        if(count == 1){
            sql = "SELECT * FROM check_in_record WHERE user_id = ? AND restaurant_id = ? AND first_flag = 1";
        }else{
            sql = "SELECT * FROM check_in_record WHERE user_id = ? AND restaurant_id = ? AND first_flag = 0";
        }

        try (Connection con = DriverManager.getConnection(url, db_user, db_password);
             PreparedStatement preparedStatement = con.prepareStatement(sql)) {
            preparedStatement.setInt(1, U_id);
            preparedStatement.setInt(2, R_id);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    result[0] = rs.getString("time");
                    result[1] = rs.getString("count");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**取得餐廳經緯度**/
    public double[] getRLocation(int r_id){
        double result[] = new double[2];
        String sql = "SELECT * FROM restaurant WHERE restaurant_id = ?";
        try (Connection con = DriverManager.getConnection(url, db_user, db_password);
             PreparedStatement preparedStatement = con.prepareStatement(sql)) {
            preparedStatement.setInt(1, r_id);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    result[0] = rs.getDouble("lat");
                    result[1] = rs.getDouble("lng");;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**更新打卡成功後水滴數**/
    public void updateCheckinDrop(int u_id, int dropNum, int addNum) {
        String sql = "UPDATE user SET drop_num = ? WHERE user_id = ?";
        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = DriverManager.getConnection(url, db_user, db_password);
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, dropNum + addNum);
            pstmt.setInt(2, u_id);

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                Log.v("DB", "更新水滴量完成：" + dropNum + "+" + addNum);
            } else {
                Log.e("DB", "更新水滴量失敗，沒有更新任何記錄");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Log.e("DB", "更新水滴量失敗");
            Log.e("DB", e.toString());
        }
    }

    /**更新按鈕點擊數**/
    public void updateClick(int tag_id, int clickNUm) {
        String sql = "UPDATE tag SET click_num = ? + 1 WHERE tag_id = ?";
        try (Connection con = DriverManager.getConnection(url, db_user, db_password);
             PreparedStatement preparedStatement = con.prepareStatement(sql)) {
            preparedStatement.setInt(1, clickNUm);
            preparedStatement.setInt(2, tag_id);
            preparedStatement.executeUpdate();
            Log.v("DB", "更新資料完成："+tag_id);
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e("DB", "點擊次數更新資料失敗");
            Log.e("DB", e.toString());
        }
    }

    public class AllTag {
        private int tag_id;
        private String tag_name;
        private int clickNum;
        private String tag_photo;

        public AllTag(int tag_id, String tag_name,int clickNum,String tag_photo) {
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
        public int getClickNum(){
            return clickNum;
        }
        public String getTag_photo() {
            return tag_photo;
        }

    }

    /**取得所有分類標籤**/
    public ArrayList<AllTag> getAllTag(){
        ArrayList<AllTag> result = new ArrayList<AllTag>();
        try {Connection con = DriverManager.getConnection(url, db_user, db_password);
            String sql = "SELECT tag_id,tag_name,click_num,tag_picture FROM tag";
            Statement st = con.createStatement(); //發送SQL查詢
            ResultSet rs = st.executeQuery(sql); //執行查詢並返回結果

            while (rs.next()){
                int tag_id = rs.getInt("tag_id");
                String tag_name = rs.getString("tag_name");
                int clickNum = rs.getInt("click_num");
                String tag_photo = rs.getString("tag_picture");

                AllTag data = new AllTag(tag_id, tag_name,clickNum,tag_photo);
                result.add(data);
            }
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**確認收藏狀態**/
    public int isInList(int u_id,int r_id){
        int result = 0;
        String sql = "SELECT state FROM list WHERE user_id = ? AND restaurant_id = ?";
        try (Connection con = DriverManager.getConnection(url, db_user, db_password);
             PreparedStatement preparedStatement = con.prepareStatement(sql)) {
            preparedStatement.setInt(1, u_id);
            preparedStatement.setInt(2, r_id);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    result = rs.getInt("state");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**取得樹苗狀態**/
    public int[] tree(int u_id){
        int result[] = new int[3];
        String sql =
                "SELECT * FROM tree WHERE user_id = ? ORDER by tree_id DESC LIMIT 1";
        try (Connection con = DriverManager.getConnection(url, db_user, db_password);
             PreparedStatement preparedStatement = con.prepareStatement(sql)) {
            preparedStatement.setInt(1, u_id);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    result[0] = rs.getInt("tree_id");
                    result[1] = rs.getInt("tree_bar");
                    result[2] = rs.getInt("tree_stage");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public class User implements Serializable {
        private int user_id;
        private String user_name;
        private int tree_num;
        private int photo_id;
        private int signday;
        private int drop_num;
        private int sign_flag;


        public User(int user_id,String user_name,int photo_id,int signday,int drop_num,int sign_flag) {
            this.user_id = user_id;
            this.user_name = user_name;
            this.tree_num = tree_num;
            this.photo_id = photo_id;
            this.signday = signday;
            this.drop_num = drop_num;
            this.sign_flag = sign_flag;
        }
        public int getUser_id(){
            return user_id;
        }
        public String getUser_name() {
            return user_name;
        }
        public int getPhoto_id(){
            return photo_id;
        }
        public int getSignday(){
            return signday;
        }
        public int getDrop(){
            return drop_num;
        }
        public int getSign_flag(){
            return sign_flag;
        }
    }

    /**取得使用者資訊**/
    public User[] getUser(int u_id) {
        User[] result = new User[1];
        try {
            Connection con = DriverManager.getConnection(url, db_user, db_password);
            String sql = "SELECT * FROM user WHERE user_id = ?";
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setInt(1, u_id);
            ResultSet rs = preparedStatement.executeQuery(); // 执行查询并返回结果

            if (rs.next()) {
                int user_id = rs.getInt("user_id");
                String user_name = rs.getString("user_name");
                int photo_id = rs.getInt("picture_id");
                int sign_day = rs.getInt("sign_day");
                int drop_num = rs.getInt("drop_num");
                int sign_flag = rs.getInt("sign_flag");
                User user = new User(user_id, user_name, photo_id, sign_day, drop_num, sign_flag);
                result[0] = user;
            }
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**更新澆水後水滴數**/
    public void updateDrop(int u_id,int newDropNum) {
        String sql = "UPDATE user SET drop_num = ? WHERE user_id = ?";
        try (Connection con = DriverManager.getConnection(url, db_user, db_password);
             PreparedStatement preparedStatement = con.prepareStatement(sql)) {
            preparedStatement.setInt(1, newDropNum);
            preparedStatement.setInt(2, u_id);
            preparedStatement.executeUpdate();
            Log.v("resetDrop", "更新水滴資料完成:"+newDropNum);
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e("resetDrop", "更新水滴資料失敗");
            Log.e("resetDrop", e.toString());
        }
    }

    /**更新澆水後進度條**/
    public void updateTreebar(int u_id, int drop_num) {
        String selectSql = "SELECT * FROM tree WHERE user_id = ? ORDER BY tree_id DESC LIMIT 1";
        try (Connection con = DriverManager.getConnection(url, db_user, db_password)) {
            int tree_id = -1;
            int tree_bar = -1;

            try (PreparedStatement selectStmt = con.prepareStatement(selectSql)) {
                selectStmt.setInt(1, u_id);
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next()) {
                        tree_id = rs.getInt("tree_id");
                        tree_bar = rs.getInt("tree_bar");
                    }
                }
            }

            String updateSql = "UPDATE tree SET tree_bar = ? + ? WHERE tree_id = ?";
            if (tree_id != -1) {
                try (PreparedStatement updateStmt = con.prepareStatement(updateSql)) {
                    updateStmt.setInt(1, tree_bar);
                    updateStmt.setInt(2, drop_num);
                    updateStmt.setInt(3, tree_id);
                    updateStmt.executeUpdate();
                    Log.v("updateTreeBar", "更新進度條完成:"+String.valueOf(tree_bar + drop_num));
                }catch (SQLException e) {
                    e.printStackTrace();
                    Log.e("updateTreeBar", "更新進度條失敗");
                    Log.e("updateTreeBar", e.toString());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public class Memo {
        private int memo_id;
        private String memo_time;
        private String content;

        public Memo(int memo_id,String memo_time,String content) {
            this.memo_id = memo_id;
            this.memo_time = memo_time;
            this.content = content;
        }

        public int getMemo_id(){
            return memo_id;
        }
        public String getMemo_time() {
            return memo_time;
        }
        public String getMemo_content() {
            return content;
        }

    }

    /**取得筆記內容**/
    public ArrayList<Memo> getMemo(int u_id, int r_id) {
        ArrayList<Memo> result = new ArrayList<>();
        try {
            Connection con = DriverManager.getConnection(url, db_user, db_password);
            String sql = "SELECT memo_id,memo_time, content FROM memo WHERE user_id = ? AND restaurant_id = ? ORDER BY memo_time";

            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setInt(1, u_id);
            preparedStatement.setInt(2, r_id);

            ResultSet rs = preparedStatement.executeQuery(); // 執行查詢並返回結果

            while (rs.next()) {
                int memo_id = rs.getInt("memo_id");
                String memo_time = rs.getString("memo_time");
                String memo_content = rs.getString("content");
                Memo data = new Memo(memo_id,memo_time, memo_content);
                result.add(data);
            }
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**新增筆記**/
    public void addMemo(int u_id, int r_id,String memoTime,String memoText) {
        String sql = "INSERT INTO memo (user_id, restaurant_id, memo_time,content) VALUES (?,?,?,?)";
        try (Connection con = DriverManager.getConnection(url, db_user, db_password);
             PreparedStatement preparedStatement = con.prepareStatement(sql)) {
            preparedStatement.setInt(1, u_id);
            preparedStatement.setInt(2,r_id);
            preparedStatement.setString(3,memoTime);
            preparedStatement.setString(4,memoText);
            preparedStatement.executeUpdate();
            Log.v("addMemo", "新增資料完成："+memoText);
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e("addMemo", "失敗");
            Log.e("addMemo", e.toString());
        }
    }

    /**編輯筆記**/
    public void editMemo(int M_id, String memoText,String editTime) {
        String sql = "UPDATE memo SET content = ? ,memo_time = ? WHERE memo_id = ?";
        try (Connection con = DriverManager.getConnection(url, db_user, db_password);
             PreparedStatement preparedStatement = con.prepareStatement(sql)) {
            preparedStatement.setString(1, memoText);
            preparedStatement.setString(2, editTime);
            preparedStatement.setInt(3,M_id);
            preparedStatement.executeUpdate();
            Log.v("editMemo", "編輯資料完成："+memoText);
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e("editMemo", "失敗");
            Log.e("editMemo", e.toString());
        }
    }

    /**刪除筆記**/
    public void deleteMemo(int M_id) {
        String sql = "DELETE FROM memo WHERE memo_id = ?";
        try (Connection con = DriverManager.getConnection(url, db_user, db_password);
             PreparedStatement preparedStatement = con.prepareStatement(sql)) {
            preparedStatement.setInt(1,M_id);
            preparedStatement.executeUpdate();
            Log.v("deleteMemo", "刪除資料完成");
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e("deleteMemo", "失敗");
            Log.e("deleteMemo", e.toString());
        }
    }

    /**匿名捐贈**/
    public void anonymousDonate(int tree_id,String donateTime) {
        String sql = "INSERT INTO donate (tree_id, donate_name,donate_time) VALUES (?,null,?)";
        try (Connection con = DriverManager.getConnection(url, db_user, db_password);
             PreparedStatement preparedStatement = con.prepareStatement(sql)) {
            preparedStatement.setInt(1,tree_id);
            preparedStatement.setString(2,donateTime);
            preparedStatement.executeUpdate();
            Log.v("anonymousDonate", "匿名捐贈完成：tree_id = " + tree_id);
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e("anonymousDonate", "失敗");
            Log.e("anonymousDonate", e.toString());
        }
    }

    /**實名捐贈**/
    public void registeredDonate(int tree_id,String donateName,String donateTime) {
        String sql = "INSERT INTO donate (tree_id, donate_name,donate_time) VALUES (?,?,?)";
        try (Connection con = DriverManager.getConnection(url, db_user, db_password);
             PreparedStatement preparedStatement = con.prepareStatement(sql)) {
            preparedStatement.setInt(1,tree_id);
            preparedStatement.setString(2,donateName);
            preparedStatement.setString(3,donateTime);
            preparedStatement.executeUpdate();
            Log.v("registeredDonate", "實名捐贈完成：" + donateName);
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e("registeredDonate", "失敗");
            Log.e("registeredDonate", e.toString());
        }
    }

    /**新增樹苗**/
    public void insertTree(int user_id) {
        String sql = "INSERT INTO tree (user_id) VALUES (?)";
        try (Connection con = DriverManager.getConnection(url, db_user, db_password);
             PreparedStatement preparedStatement = con.prepareStatement(sql)) {
            preparedStatement.setInt(1,user_id);
            preparedStatement.executeUpdate();
            Log.v("insertTree", "新增樹苗完成：" + user_id);
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e("insertTree", "失敗");
            Log.e("insertTree", e.toString());
        }
    }

    /**更新簽到狀態**/
    public void updateCheckState(int user_id,int sign_day) {
        String sql = "UPDATE user SET sign_day = ? + 1 ,sign_flag = 1 WHERE user_id = ?";
        try (Connection con = DriverManager.getConnection(url, db_user, db_password);
             PreparedStatement preparedStatement = con.prepareStatement(sql)) {
            preparedStatement.setInt(1,sign_day);
            preparedStatement.setInt(2,user_id);
            preparedStatement.executeUpdate();
            Log.v("updateCheckState", "更新簽到狀態完成：user_id = " + user_id + ",sign_day = " + (sign_day + 1));
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e("updateCheckState", "失敗");
            Log.e("updateCheckState", e.toString());
        }
    }

    /**更新簽到完成後水滴數**/
    public void updateSignDrop(int user_id, int multiple) {
        String sql = "";
        String aaa = "";
        int drop = 0;
        int result = 0;
        try {
            Connection con = DriverManager.getConnection(url, db_user, db_password);
            sql = "SELECT drop_num From user WHERE user_id = '" + user_id + "'";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql); //執行查詢並返回結果

            if (rs.next()) {
                drop = rs.getInt("drop_num");
            }


            if (multiple == 1) {
                sql = "UPDATE user SET drop_num = '" + (drop + 5) + "' WHERE user_id = '" + user_id + "'";
                aaa = "+5";
            } else {
                sql = "UPDATE user SET drop_num = '" + (drop + 5 * multiple) + "' WHERE user_id = '" + user_id + "'";
                aaa = "+"+ 5 * multiple;
            }

            st.executeUpdate(sql);
            st.close();
            Log.v("DB", "更新簽到完成後水滴數完成：" + aaa);

        } catch (SQLException e) {
            e.printStackTrace();
            Log.e("DB", "更新簽到完成後水滴數失敗");
            Log.e("DB", e.toString());
        }
    }

    /**取得總捐贈人數**/
    public int totalDonateNum() {
        int result = 0;
        String sql = "SELECT COUNT(*) as num FROM donate";

        try (Connection con = DriverManager.getConnection(url, db_user, db_password);
             PreparedStatement preparedStatement = con.prepareStatement(sql);
             ResultSet rs = preparedStatement.executeQuery()) {

            if (rs.next()) {
                result = rs.getInt("num");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Log.v("donatePeople","total："+result);
        return result;
    }

    /**新增使用者**/
    public int insertNewUser() {
        int newUserId = getMaxUserId() + 1;
        String sql = "INSERT INTO user (user_id, user_name, picture_id, sign_day, drop_num, sign_flag) VALUES (?, 'User', 1, 0, 0, 0)";
        try (Connection con = DriverManager.getConnection(url, db_user, db_password);
             PreparedStatement preparedStatement = con.prepareStatement(sql)) {
            preparedStatement.setInt(1, newUserId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            Log.e("insertNewUser", "newUserId: " + newUserId + ", error: " + e);
        }
        return newUserId;
    }

    /**取得最大的使用者ID**/
    public int getMaxUserId() {
        int maxUserId = 0;
        String sql = "SELECT MAX(user_id) AS max_id FROM user";
        try (Connection con = DriverManager.getConnection(url, db_user, db_password);
             PreparedStatement preparedStatement = con.prepareStatement(sql)) {
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                maxUserId = rs.getInt("max_id");
            }
        } catch (SQLException e) {
            Log.e("getMaxUserId", "maxUserId: " + maxUserId + ", error: " + e);
        }
        return maxUserId;
    }

    /**刪除使用者的收藏狀態**/
    public void removeFromList(int userId, int restaurantId) {
        String sql = "DELETE FROM list WHERE user_id = ? AND restaurant_id = ?";
        try (Connection con = DriverManager.getConnection(url, db_user, db_password);
             PreparedStatement preparedStatement = con.prepareStatement(sql)) {

            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, restaurantId);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                Log.v("removeFromList", "成功從列表中刪除餐廳");
            } else {
                Log.v("removeFromList", "未找到符合條件的記錄");
            }
        } catch (SQLException e) {
            Log.e("removeFromList", "刪除失敗");
            Log.e("removeFromList", e.toString());
        }
    }

    /**取得打卡時間前10個字元**/
    public Map<Integer, String> getCheckInTimes(int userId, List<Integer> restaurantIds) {
        Map<Integer, String> checkInTimes = new HashMap<>();

        if (restaurantIds.isEmpty()) {
            return checkInTimes;
        }

        String placeholders = String.join(",", Collections.nCopies(restaurantIds.size(), "?"));
        String sql = "SELECT restaurant_id, SUBSTRING(time, 1, 10) AS time_substring " +
                "FROM check_in_record " +
                "WHERE user_id = ? AND restaurant_id IN (" + placeholders + ") AND first_flag = 1";

        try (Connection con = DriverManager.getConnection(url, db_user, db_password);
             PreparedStatement preparedStatement = con.prepareStatement(sql)) {

            preparedStatement.setInt(1, userId);
            for (int i = 0; i < restaurantIds.size(); i++) {
                preparedStatement.setInt(i + 2, restaurantIds.get(i));
            }

            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                int restaurantId = rs.getInt("restaurant_id");
                String checkInTime = rs.getString("time_substring");
                checkInTimes.put(restaurantId, checkInTime);
            }
        } catch (SQLException e) {
            Log.e("DB", "SQL异常: " + e.getMessage());
            Log.e("DB", "SQL状态: " + e.getSQLState());
            Log.e("DB", "錯誤代碼: " + e.getErrorCode());
            e.printStackTrace();
        } catch (Exception e) {
            Log.e("DB", "未预期的异常: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }

        return checkInTimes;
    }

    /**取得餐廳的收藏狀態**/
    public Map<Integer, Integer> getRestaurantStates(int userId, java.util.List<Integer> restaurantIds) {
        Map<Integer, Integer> states = new HashMap<>();
        String sql = "SELECT restaurant_id, state FROM list WHERE user_id = ? AND restaurant_id IN " +
                getInClause(restaurantIds.size());

        try (Connection con = DriverManager.getConnection(url, db_user, db_password);
             PreparedStatement preparedStatement = con.prepareStatement(sql)) {

            preparedStatement.setInt(1, userId);
            for (int i = 0; i < restaurantIds.size(); i++) {
                preparedStatement.setInt(i + 2, restaurantIds.get(i));
            }

            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    int restaurantId = rs.getInt("restaurant_id");
                    int state = rs.getInt("state");
                    states.put(restaurantId, state);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return states;
    }

    /**取得餐廳的收藏人數**/
    public Map<Integer, Integer> getLikeCounts(java.util.List<Integer> restaurantIds) {
        return getCountsByState(restaurantIds, 1);
    }

    /**取得餐廳的拉黑人數**/
    public Map<Integer, Integer> getUnlikeCounts(java.util.List<Integer> restaurantIds) {
        return getCountsByState(restaurantIds, -1);
    }

    /**計算收藏和拉黑人數的值**/
    private Map<Integer, Integer> getCountsByState(java.util.List<Integer> restaurantIds, int state) {
        Map<Integer, Integer> counts = new HashMap<>();
        String sql = "SELECT restaurant_id, COUNT(*) AS count FROM list WHERE restaurant_id IN " +
                getInClause(restaurantIds.size()) + " AND state = ? GROUP BY restaurant_id";

        try (Connection con = DriverManager.getConnection(url, db_user, db_password);
             PreparedStatement preparedStatement = con.prepareStatement(sql)) {

            for (int i = 0; i < restaurantIds.size(); i++) {
                preparedStatement.setInt(i + 1, restaurantIds.get(i));
            }
            preparedStatement.setInt(restaurantIds.size() + 1, state);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    int restaurantId = rs.getInt("restaurant_id");
                    int count = rs.getInt("count");
                    counts.put(restaurantId, count);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return counts;
    }

    /**排序陣列用的function**/
    private String getInClause(int size) {
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < size; i++) {
            sb.append(i == 0 ? "?" : ",?");
        }
        sb.append(")");
        return sb.toString();
    }
}

