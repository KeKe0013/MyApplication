package com.example.myapplication;

import android.util.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class res_SQL {
    private Connection con; // 資料庫連接

    public res_SQL(Connection connection) {
        this.con = connection; // 在建構子中設置資料庫連接
    }

    public ArrayList<RestaurantData> getData(int today) {
        ArrayList<RestaurantData> dataList = new ArrayList<>();
        try {
            String sql = "SELECT r.*, bh.week, bh.open_time FROM restaurant r JOIN business_hours bh ON r.restaurant_id = bh.restaurant_id WHERE bh.week = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, today);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                int restaurant_id = rs.getInt("restaurant_id");
                String restaurant_name = rs.getString("restaurant_name");
                String city = rs.getString("city");
                String district = rs.getString("district");
                String restaurant_phone = rs.getString("restaurant_phone");
                String address = rs.getString("address");
                Double lat = rs.getDouble("lat");
                Double lng = rs.getDouble("lng");
                String menu = rs.getString("menu");
                String logo = rs.getString("logo");
                // 獲取 business_hours 表的數據
                String week = rs.getString("week");
                String openTime = rs.getString("open_time");

                // 創建 RestaurantData 對象並添加到數據列表
                RestaurantData restaurant = new RestaurantData(restaurant_id, restaurant_name, city, district, restaurant_phone, address, lat, lng, menu, logo, openTime);
                dataList.add(restaurant);
            }
            pst.close();
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e("res_SQL", "SQL Exception: " + e.getMessage());
        }
        return dataList;
    }

    public ResultSet executeQuery(String sql, int restaurantId) throws SQLException {
        PreparedStatement pst = con.prepareStatement(sql);
        pst.setInt(1, restaurantId); // 將餐廳ID設置到 SQL 查詢中的參數位置
        return pst.executeQuery();
    }

    public ArrayList<RestaurantData> getTagRestaurantData(int user_id, int tag_id, int today) {
        ArrayList<RestaurantData> result = new ArrayList<>();
        try {
            String sql = "SELECT restaurant.restaurant_id, restaurant.restaurant_name, restaurant.city, restaurant.district, restaurant.restaurant_phone, restaurant.address, restaurant.lat, restaurant.lng, restaurant.menu, restaurant.logo, business_hours.week, business_hours.open_time, " +
                    "(SELECT COUNT(*) FROM list WHERE restaurant_id = restaurant.restaurant_id AND state = 1) AS like_count, " +
                    "(SELECT COUNT(*) FROM list WHERE restaurant_id = restaurant.restaurant_id AND state = -1) AS unlike_count, " +
                    "(SELECT state FROM list WHERE restaurant_id = restaurant.restaurant_id AND user_id = ?) AS user_state " +
                    "FROM restaurant " +
                    "INNER JOIN business_hours ON restaurant.restaurant_id = business_hours.restaurant_id " +
                    "INNER JOIN restaurant_tag ON restaurant.restaurant_id = restaurant_tag.restaurant_id " +
                    "WHERE restaurant_tag.tag_id = ? " +
                    "AND (business_hours.week = ?)";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, user_id);
                ps.setInt(2, tag_id);
                ps.setInt(3, today);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int restaurant_id = rs.getInt("restaurant_id");
                        String restaurant_name = rs.getString("restaurant_name");
                        String city = rs.getString("city");
                        String district = rs.getString("district");
                        String restaurant_phone = rs.getString("restaurant_phone");
                        String address = rs.getString("address");
                        Double lat = rs.getDouble("lat");
                        Double lng = rs.getDouble("lng");
                        String menu = rs.getString("menu");
                        String logo = rs.getString("logo");
                        String week = rs.getString("week");
                        String openTime = rs.getString("open_time");
                        int likeCount = rs.getInt("like_count");
                        int unlikeCount = rs.getInt("unlike_count");
                        int state = rs.getInt("user_state");

                        RestaurantData data = new RestaurantData(restaurant_id, restaurant_name, city, district, restaurant_phone, address, lat, lng, menu, logo, openTime);
                        data.setLikeCount(likeCount);
                        data.setUnlikeCount(unlikeCount);
                        data.setState(state);
                        result.add(data);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public ArrayList<RestaurantData> getListRestaurant(int user_id, int state, int today) {
        ArrayList<RestaurantData> result = new ArrayList<>();
        try {
            String sql = "SELECT r.restaurant_id, r.restaurant_name, r.city, r.district, r.restaurant_phone, r.address, r.lat, r.lng, r.menu, r.logo, bh.week, bh.open_time, " +
                    "(SELECT COUNT(*) FROM list WHERE restaurant_id = r.restaurant_id AND state = 1) AS like_count, " +
                    "(SELECT COUNT(*) FROM list WHERE restaurant_id = r.restaurant_id AND state = -1) AS unlike_count " +
                    "FROM restaurant r " +
                    "INNER JOIN business_hours bh ON r.restaurant_id = bh.restaurant_id " +
                    "INNER JOIN list l ON r.restaurant_id = l.restaurant_id " +
                    "WHERE l.user_id = ? AND l.state = ? AND bh.week = ?";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, user_id);
                ps.setInt(2, state);
                ps.setInt(3, today);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int restaurant_id = rs.getInt("restaurant_id");
                        String restaurant_name = rs.getString("restaurant_name");
                        String city = rs.getString("city");
                        String district = rs.getString("district");
                        String restaurant_phone = rs.getString("restaurant_phone");
                        String address = rs.getString("address");
                        Double lat = rs.getDouble("lat");
                        Double lng = rs.getDouble("lng");
                        String menu = rs.getString("menu");
                        String logo = rs.getString("logo");
                        String week = rs.getString("week");
                        String openTime = rs.getString("open_time");
                        int likeCount = rs.getInt("like_count");
                        int unlikeCount = rs.getInt("unlike_count");

                        RestaurantData data = new RestaurantData(restaurant_id, restaurant_name, city, district, restaurant_phone, address, lat, lng, menu, logo, openTime);
                        data.setLikeCount(likeCount);
                        data.setUnlikeCount(unlikeCount);
                        result.add(data);
                    }
                }
            }
        } catch (SQLException e) {
            Log.e("Database", "SQL error in getListRestaurant: " + e.getMessage(), e);
            e.printStackTrace();
        }
        return result;
    }

    public ArrayList<RestaurantData> getCheckInRestaurant(int user_id, int today) {
        ArrayList<RestaurantData> result = new ArrayList<>();
        try {
            String sql = "SELECT restaurant.restaurant_id, restaurant.restaurant_name, restaurant.city, restaurant.district, restaurant.restaurant_phone, restaurant.address, restaurant.lat, restaurant.lng, restaurant.menu, restaurant.logo, business_hours.week, business_hours.open_time, " +
                    "(SELECT COUNT(*) FROM list WHERE restaurant_id = restaurant.restaurant_id AND state = 1) AS like_count, " +
                    "(SELECT COUNT(*) FROM list WHERE restaurant_id = restaurant.restaurant_id AND state = -1) AS unlike_count, " +
                    "(SELECT state FROM list WHERE restaurant_id = restaurant.restaurant_id AND user_id = ?) AS user_state " +
                    "FROM restaurant " +
                    "INNER JOIN business_hours ON restaurant.restaurant_id = business_hours.restaurant_id " +
                    "INNER JOIN check_in_record ON restaurant.restaurant_id = check_in_record.restaurant_id " +
                    "WHERE check_in_record.user_id = ? " +
                    "AND (business_hours.week = ?)" +
                    "AND (check_in_record.first_flag = 1)";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, user_id);
                ps.setInt(2, user_id);
                ps.setInt(3, today);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int restaurant_id = rs.getInt("restaurant_id");
                        String restaurant_name = rs.getString("restaurant_name");
                        String city = rs.getString("city");
                        String district = rs.getString("district");
                        String restaurant_phone = rs.getString("restaurant_phone");
                        String address = rs.getString("address");
                        Double lat = rs.getDouble("lat");
                        Double lng = rs.getDouble("lng");
                        String menu = rs.getString("menu");
                        String logo = rs.getString("logo");
                        String week = rs.getString("week");
                        String openTime = rs.getString("open_time");
                        int likeCount = rs.getInt("like_count");
                        int unlikeCount = rs.getInt("unlike_count");
                        int state = rs.getInt("user_state");

                        RestaurantData data = new RestaurantData(restaurant_id, restaurant_name, city, district, restaurant_phone, address, lat, lng, menu, logo, openTime);
                        data.setLikeCount(likeCount);
                        data.setUnlikeCount(unlikeCount);
                        data.setState(state);
                        result.add(data);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
    public ArrayList<RestaurantData> getMemoRestaurant(int user_id, int today) {
        ArrayList<RestaurantData> result = new ArrayList<>();
        try {
            String sql = "SELECT DISTINCT r.*, bh.week, bh.open_time, m.memo_id, m.memo_time, m.content, " +
                    "t.tag_name1, t.tag_name2, t.tag_name3 " +
                    "FROM memo m " +
                    "JOIN restaurant r ON m.restaurant_id = r.restaurant_id " +
                    "LEFT JOIN business_hours bh ON r.restaurant_id = bh.restaurant_id AND bh.week = ? " +
                    "LEFT JOIN (SELECT rt.restaurant_id, " +
                    "    MAX(CASE WHEN rn = 1 THEN t.tag_name END) AS tag_name1, " +
                    "    MAX(CASE WHEN rn = 2 THEN t.tag_name END) AS tag_name2, " +
                    "    MAX(CASE WHEN rn = 3 THEN t.tag_name END) AS tag_name3 " +
                    "    FROM (SELECT restaurant_id, tag_id, " +
                    "        ROW_NUMBER() OVER (PARTITION BY restaurant_id ORDER BY tag_id) AS rn " +
                    "        FROM restaurant_tag) rt " +
                    "    JOIN tag t ON rt.tag_id = t.tag_id " +
                    "    GROUP BY rt.restaurant_id) t ON r.restaurant_id = t.restaurant_id " +
                    "WHERE m.user_id = ? " +
                    "ORDER BY m.memo_time DESC";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, today);
                ps.setInt(2, user_id);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int restaurant_id = rs.getInt("restaurant_id");
                        String restaurant_name = rs.getString("restaurant_name");
                        String city = rs.getString("city");
                        String district = rs.getString("district");
                        String restaurant_phone = rs.getString("restaurant_phone");
                        String address = rs.getString("address");
                        Double lat = rs.getDouble("lat");
                        Double lng = rs.getDouble("lng");
                        String menu = rs.getString("menu");
                        String logo = rs.getString("logo");
                        String week = rs.getString("week");
                        String openTime = rs.getString("open_time");
                        int memo_id = rs.getInt("memo_id");
                        String memo_time = rs.getString("memo_time");
                        String content = rs.getString("content");

                        RestaurantData data = new RestaurantData(restaurant_id, restaurant_name, city, district, restaurant_phone, address, lat, lng, menu, logo, openTime);
                        data.setMemo_id(memo_id);
                        data.setMemo_time(memo_time);
                        data.setMemo_content(content);

                        List<String> tags = new ArrayList<>();
                        addTagNameIfNotNull(tags, rs, "tag_name1");
                        addTagNameIfNotNull(tags, rs, "tag_name2");
                        addTagNameIfNotNull(tags, rs, "tag_name3");
                        data.setTags(tags);

                        result.add(data);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    private void addTagNameIfNotNull(List<String> tags, ResultSet rs, String columnName) throws SQLException {
        String tagName = rs.getString(columnName);
        if (tagName != null && !tagName.isEmpty()) {
            tags.add(tagName);
        }
    }

    public List<Map<String, String>> getBusinessHours() {
        return getData("SELECT * FROM business_hours");
    }

    public List<Map<String, String>> getRestaurants() {
        return getData("SELECT * FROM restaurant");
    }

    public List<Map<String, String>> getRestaurantTags() {
        return getData("SELECT * FROM restaurant_tag");
    }

    public List<Map<String, String>> getRestaurantGreenBehaviors() {
        return getData("SELECT * FROM restaurant_greenBehavior");
    }

    public List<Map<String, String>> getGreenBehaviors() {
        return getData("SELECT * FROM green_behavior");
    }

    public List<Map<String, String>> getTags() {
        return getData("SELECT * FROM tag");
    }

    public List<Map<String, String>> getMenu() {
        return getData("SELECT * FROM menu");
    }

    public List<Map<String, String>> getPictures() {
        return getData("SELECT * FROM picture");
    }

    private List<Map<String, String>> getData(String query) {
        List<Map<String, String>> data = new ArrayList<>();
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Map<String, String> row = new HashMap<>();
                int columnCount = rs.getMetaData().getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(rs.getMetaData().getColumnName(i), rs.getString(i));
                }
                data.add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

}