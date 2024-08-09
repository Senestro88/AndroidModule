package com.official.senestro.core;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class AndroidBuildDetails {
    private ArrayList<HashMap<String, Object>> androidBuildDetails = new ArrayList<>();

    public AndroidBuildDetails() {
        addAllAndroidBuildDetails();
    }

    public ArrayList<HashMap<String, Object>> getAllAndroidBuildDetails() {
        return androidBuildDetails;
    }

    public String getVersionNameFromApiLevel(int apiLevel) {
        return getFromApiLevel(apiLevel, "versionName");
    }

    public String getVersionCodeFromApiLevel(int apiLevel) {
        return getFromApiLevel(apiLevel, "versionCode");
    }

    public String getCodeNameFromApiLevel(int apiLevel) {
        return getFromApiLevel(apiLevel, "codeName");
    }

    public int getYearFromApiLevel(int apiLevel) {
        return Integer.parseInt(getFromApiLevel(apiLevel, "year"));
    }

    // PRIVATE
    public String getFromApiLevel(int apiLevel, @NonNull String field) {
        try {
            for (HashMap<String, Object> map : androidBuildDetails) {
                int $apiLevel = (int) Objects.requireNonNull(map.get("apiLevel"));
                if (apiLevel == $apiLevel) {
                    return (String) Objects.requireNonNull(map.get(field));
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    private void addAllAndroidBuildDetails() {
        addBuildDetail(1, "Android 1.0", "Base", "null", 2008);
        addBuildDetail(2, "Android 1.1", "BASE_1_1", "Petit Four", 2009);
        addBuildDetail(3, "Android 1.5", "CUPCAKE", "Cupcake", 2009);
        addBuildDetail(4, "Android 1.6", "DONUT", "Donut", 2009);
        addBuildDetail(5, "Android 2.0", "ECLAIR", "Eclair", 2009);
        addBuildDetail(6, "Android 2.0.1", "ECLAIR_0_1", "Eclair", 2009);
        addBuildDetail(7, "Android 2.1", "ECLAIR_MR1", "Eclair", 2010);
        addBuildDetail(8, "Android 2.2", "FROYO", "Froyo", 2010);
        addBuildDetail(9, "Android 2.3.0 – 2.3.2", "GINGERBREAD", "Gingerbread", 2010);
        addBuildDetail(10, "Android 2.3.3 – 2.3.7", "GINGERBREAD_MR1", "Gingerbread", 2011);
        addBuildDetail(11, "Android 3.0", "HONEYCOMB", "Honeycomb", 2011);
        addBuildDetail(12, "Android 3.1", "HONEYCOMB_MR1", "Honeycomb", 2011);
        addBuildDetail(13, "Android 3.2", "HONEYCOMB_MR2", "Honeycomb", 2011);
        addBuildDetail(14, "Android 4.0 – 4.0.2", "ICE_CREAM_SANDWICH", "Ice Cream Sandwich", 2011);
        addBuildDetail(15, "Android 4.0.3 – 4.0.4", "ICE_CREAM_SANDWICH_MR1", "Ice Cream Sandwich", 2011);
        addBuildDetail(16, "Android 4.1", "JELLY_BEAN", "Jelly Bean", 2012);
        addBuildDetail(17, "Android 4.2", "JELLY_BEAN_MR1", "Jelly Bean", 2012);
        addBuildDetail(18, "Android 4.3", "JELLY_BEAN_MR2", "Jelly Bean", 2013);
        addBuildDetail(19, "Android 4.4", "KITKAT", "KitKat", 2013);
        addBuildDetail(20, "Android 4.4W", "KITKAT_WATCH", "KitKat", 2014);
        addBuildDetail(21, "Android 5.0", "LOLLIPOP", "Lollipop", 2014);
        addBuildDetail(22, "Android 5.1", "LOLLIPOP_MR1", "Lollipop", 2015);
        addBuildDetail(23, "Android 6.0", "M", "Marshmallow", 2015);
        addBuildDetail(24, "Android 7.0", "N", "Nougat", 2016);
        addBuildDetail(25, "Android 7.1", "N_MR1", "Nougat", 2016);
        addBuildDetail(26, "Android 8.0", "O", "Oreo", 2017);
        addBuildDetail(27, "Android 8.1", "O_MR1", "Oreo", 2017);
        addBuildDetail(28, "Android 9.0", "P", "Pie", 2018);
        addBuildDetail(29, "Android 10", "Q", "Quince Tart", 2019);
        addBuildDetail(30, "Android 11", "R", "Red Velvet Cake", 2020);
        addBuildDetail(31, "Android 12", "S", "Snow Cone", 2021);
        addBuildDetail(32, "Android 12L", "S_V2", "Snow Cone", 2022);
        addBuildDetail(33, "Android 13", "TIRAMISU", "Tiramisu", 2022);
        addBuildDetail(34, "Android 14", "UPSIDE_DOWN_CAKE", "Upside Down Cake", 2023);
    }

    private void addBuildDetail(int apiLevel, @NonNull String versionName, @NonNull String versionCode, @NonNull String codeName, int year) {
        HashMap<String, Object> buildDetails = new HashMap<>();
        buildDetails.put("apiLevel", apiLevel);
        buildDetails.put("versionName", versionName);
        buildDetails.put("versionCode", versionCode);
        buildDetails.put("codeName", codeName);
        buildDetails.put("year", year);
        androidBuildDetails.add(buildDetails);
    }
}