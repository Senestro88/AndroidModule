package com.official.senestro.core;

import android.util.Log;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class BuildDetails {
    private static final String tag = BuildDetails.class.getName();

    private ArrayList<HashMap<String, Object>> androidBuildDetails = new ArrayList<>();

    public BuildDetails() {
        addBuildDetails();
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
            Log.e(tag, e.getMessage(), e);
        }
        return null;
    }

    private void addBuildDetails() {
        addBuildDetails(1, "Android 1.0", "Base", "null", 2008);
        addBuildDetails(2, "Android 1.1", "BASE_1_1", "Petit Four", 2009);
        addBuildDetails(3, "Android 1.5", "CUPCAKE", "Cupcake", 2009);
        addBuildDetails(4, "Android 1.6", "DONUT", "Donut", 2009);
        addBuildDetails(5, "Android 2.0", "ECLAIR", "Eclair", 2009);
        addBuildDetails(6, "Android 2.0.1", "ECLAIR_0_1", "Eclair", 2009);
        addBuildDetails(7, "Android 2.1", "ECLAIR_MR1", "Eclair", 2010);
        addBuildDetails(8, "Android 2.2", "FROYO", "Froyo", 2010);
        addBuildDetails(9, "Android 2.3.0 – 2.3.2", "GINGERBREAD", "Gingerbread", 2010);
        addBuildDetails(10, "Android 2.3.3 – 2.3.7", "GINGERBREAD_MR1", "Gingerbread", 2011);
        addBuildDetails(11, "Android 3.0", "HONEYCOMB", "Honeycomb", 2011);
        addBuildDetails(12, "Android 3.1", "HONEYCOMB_MR1", "Honeycomb", 2011);
        addBuildDetails(13, "Android 3.2", "HONEYCOMB_MR2", "Honeycomb", 2011);
        addBuildDetails(14, "Android 4.0 – 4.0.2", "ICE_CREAM_SANDWICH", "Ice Cream Sandwich", 2011);
        addBuildDetails(15, "Android 4.0.3 – 4.0.4", "ICE_CREAM_SANDWICH_MR1", "Ice Cream Sandwich", 2011);
        addBuildDetails(16, "Android 4.1", "JELLY_BEAN", "Jelly Bean", 2012);
        addBuildDetails(17, "Android 4.2", "JELLY_BEAN_MR1", "Jelly Bean", 2012);
        addBuildDetails(18, "Android 4.3", "JELLY_BEAN_MR2", "Jelly Bean", 2013);
        addBuildDetails(19, "Android 4.4", "KITKAT", "KitKat", 2013);
        addBuildDetails(20, "Android 4.4W", "KITKAT_WATCH", "KitKat", 2014);
        addBuildDetails(21, "Android 5.0", "LOLLIPOP", "Lollipop", 2014);
        addBuildDetails(22, "Android 5.1", "LOLLIPOP_MR1", "Lollipop", 2015);
        addBuildDetails(23, "Android 6.0", "M", "Marshmallow", 2015);
        addBuildDetails(24, "Android 7.0", "N", "Nougat", 2016);
        addBuildDetails(25, "Android 7.1", "N_MR1", "Nougat", 2016);
        addBuildDetails(26, "Android 8.0", "O", "Oreo", 2017);
        addBuildDetails(27, "Android 8.1", "O_MR1", "Oreo", 2017);
        addBuildDetails(28, "Android 9.0", "P", "Pie", 2018);
        addBuildDetails(29, "Android 10", "Q", "Quince Tart", 2019);
        addBuildDetails(30, "Android 11", "R", "Red Velvet Cake", 2020);
        addBuildDetails(31, "Android 12", "S", "Snow Cone", 2021);
        addBuildDetails(32, "Android 12L", "S_V2", "Snow Cone", 2022);
        addBuildDetails(33, "Android 13", "TIRAMISU", "Tiramisu", 2022);
        addBuildDetails(34, "Android 14", "UPSIDE_DOWN_CAKE", "Upside Down Cake", 2023);
    }

    private void addBuildDetails(int apiLevel, @NonNull String versionName, @NonNull String versionCode, @NonNull String codeName, int year) {
        HashMap<String, Object> buildDetails = new HashMap<>();
        buildDetails.put("apiLevel", apiLevel);
        buildDetails.put("versionName", versionName);
        buildDetails.put("versionCode", versionCode);
        buildDetails.put("codeName", codeName);
        buildDetails.put("year", year);
        androidBuildDetails.add(buildDetails);
    }
}