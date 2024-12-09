    package com.example.myapplication;

    import java.io.Serializable;
    import java.util.List;

    public class RestaurantData implements Serializable {
        private final int restaurant_id;
        private final String restaurant_name;
        private final String city;
        private final String district;
        private final String restaurant_phone;
        private final String address;
        private final Double lat;
        private final Double lng;
        private final String menu;
        private final String logo;
        private final String openTime;
        private int likeCount;
        private int unlikeCount;
        private int state;
        private int memo_id;
        private String memo_time;
        private String memo_content;
        private List<String> tags;


        public RestaurantData(int restaurant_id, String restaurant_name, String city, String district, String restaurant_phone, String address, Double lat, Double lng, String menu, String logo, String openTime) {
            this.restaurant_id = restaurant_id;
            this.restaurant_name = restaurant_name;
            this.city = city;
            this.district = district;
            this.restaurant_phone = restaurant_phone;
            this.address = address;
            this.lat = lat;
            this.lng = lng;
            this.menu = menu;
            this.logo = logo;
            this.openTime = openTime;
        }

        public int getId() {
            return restaurant_id;
        }

        public String getName() {
            return restaurant_name;
        }

        public String getCity() {
            return city;
        }

        public String getDistrict() {
            return district;
        }

        public String getPhone() {
            return restaurant_phone;
        }

        public String getAddress() {
            return address;
        }

        public Double getLatitude() {
            return lat;
        }

        public Double getLongitude() {
            return lng;
        }

        public String getMenu() {
            return menu;
        }

        public String getLogo() {
            return logo;
        }


        public String getOpenTime() {
            return openTime;
        }

        public void setLikeCount(int likeCount) {
            this.likeCount = likeCount;
        }

        public int getLikeCount() {
            return this.likeCount;
        }

        public void setUnlikeCount(int unlikeCount) {
            this.unlikeCount = unlikeCount;
        }

        public int getUnlikeCount() {
            return this.unlikeCount;
        }
        public void setState(int state){
            this.state = state;
        }
        public int getState(){
            return this.state;
        }

        public void setMemo_id(int memo_id) {
            this.memo_id = memo_id;
        }

        public int getMemo_id() {
            return this.memo_id;
        }
        public void setMemo_time(String memo_time) {
            this.memo_time = memo_time;
        }

        public String getMemo_time() {
            return this.memo_time;
        }

        public void setMemo_content(String memo_content) {
            this.memo_content = memo_content;
        }

        public String getMemo_content() {
            return this.memo_content;
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }

        public List<String> getTags() {
            return tags;
        }
    }
