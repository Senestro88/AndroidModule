package com.official.senestro.core.utils;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils {
    private Calendar calendar;
    private Date date;
    private TimeZone tz;
    private Locale locale;
    private SimpleDateFormat format;

    /**
     * The TimeZone default is: GMT.
     * The locale default is: ENGLISH.
     * And the calendar default is: GregorianCalendar and it based on TimeZone (GMT)
     */
    public DateUtils() {
        setDefaults();
    }

    /**
     * Sets the TimeZone base on the TimeZone string provided
     */
    public void setTimeZone(@NonNull String tz) {
        this.tz = TimeZone.getTimeZone(tz);
        this.calendar.setTimeZone(this.tz);
        this.date = this.calendar.getTime();
    }

    /**
     * Sets the TimeZone base on the TimeZone object provided
     */
    public void setTimeZone(@NonNull TimeZone tz) {
        this.tz = tz;
        this.calendar.setTimeZone(tz);
        this.date = calendar.getTime();
    }

    /**
     * Sets the Locale base on the language and country string provided
     */
    public void setLocale(@NonNull String language, @NonNull String country) {
        try {
            Locale locale = new Locale(language, country);
            this.locale = locale;
        } catch (Throwable e) {
            e.printStackTrace();
            this.locale = Locale.ENGLISH;
        }
    }

    /**
     * Sets the Locale base on the Locale object provided
     */
    public void setLocale(@NonNull Locale locale) {
        this.locale = locale;
    }

    /**
     * Sets the Calendar base on the Calendar object provided
     */
    public void setCalendar(@NonNull Calendar calendar) {
        this.tz = calendar.getTimeZone();
        this.calendar = calendar;
        this.date = calendar.getTime();
    }

    /**
     * Set the new time in milliseconds
     */
    public void setTimeInMillis(long timeStamp) {
        this.calendar.setTimeInMillis(timeStamp);
        this.date = this.calendar.getTime();
        this.tz = this.calendar.getTimeZone();
    }

    /**
     * Returns the current time in milliseconds.
     */
    public long getTimeInMillis() {
        return this.calendar.getTimeInMillis();
    }

    /**
     * Returns the current time in seconds.
     */
    public long getTimeInSeconds() {
        return getTimeInMillis() / 1000;
    }

    /**
     * Returns the current year, for example "1997"
     */
    public int getYear() {
        return Integer.parseInt(formatDate("yyyy"));
    }

    /**
     * Returns the current month, for example "07" (The 7th month - July)
     */
    public int getMonth() {
        return Integer.parseInt(formatDate("MM"));
    }

    /**
     * Returns the current month name, for example "Jul"
     */
    public String getMonthShortName() {
        return formatDate("MMM");
    }

    /**
     * Returns the current month name, for example "July"
     */
    public String getMonthLongName() {
        return formatDate("MMMM");
    }

    /**
     * Returns the week number within the current year, for example "20" (The 20th week in year)
     */
    public int getWeekInYear() {
        return this.calendar.get(Calendar.WEEK_OF_YEAR);
    }

    /**
     * Returns the week number within the current month, for example "3" (The 3rd week in month)
     */
    public int getWeekInMonth() {
        return this.calendar.get(Calendar.WEEK_OF_MONTH);
    }

    /**
     * Returns the number days within the current month, for example "14" (14 days spent in month)
     */
    public int getDayInMonth() {
        return Integer.parseInt(formatDate("dd"));
    }

    /**
     * Returns the number of days within the current year, for example "135" (135 days spent in year)
     */
    public int getDayInYear() {
        return this.calendar.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Returns the short day name within the current week,  for example "Tue"
     */
    public String getShortDayNameOfWeek() {
        return formatDate("E");
    }

    /**
     * Returns the long day name within the current week, for example "Tuesday"
     */
    public String getLongDayNameOfWeek() {
        return formatDate("EEEE");
    }

    /**
     * Returns the current day number within the current week, for example "1 = Monday, ..., 7 = Sunday"
     */
    public int getDayNumberOfWeek() {
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        // In Calendar, Sunday is 1 and Saturday is 7
        // Convert to 1 (Monday) to 7 (Sunday)
        return (dayOfWeek == Calendar.SUNDAY) ? 7 : dayOfWeek - 1;
    }

    /**
     * Returns AM/PM marker (e.g., PM)
     */
    public String getDayPeriod() {
        return formatDate("a");
    }

    /**
     * Returns hour is 24 hour format (E.g 14 for 2 PM). If exactFormat argument is true, it returns 01-24, else 0-23
     */
    public int get24Hour(boolean exactFormat) {
        return Integer.parseInt(exactFormat ? formatDate("HH") : formatDate("H"));
    }

    /**
     * Returns hour is 24 hour format (E.g 2 for 2 PM).  If exactFormat argument is true, it returns 01-12, else 0-11
     */
    public int get12Hour(boolean exactFormat) {
        return Integer.parseInt(exactFormat ? formatDate("hh") : formatDate("h"));
    }

    /**
     * Get the current minutes
     */
    public int getMinutes() {
        return Integer.parseInt(formatDate("mm"));
    }

    /**
     * Get the current seconds
     */
    public int getSeconds() {
        return Integer.parseInt(formatDate("ss"));
    }

    /**
     * Format a date according to the pattern provided
     */
    public String formatDate(@NonNull String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern, this.locale);
        return format.format(this.date);
    }


    // PRIVATE METHODS
    private boolean isValidTimeZone(@NonNull String tz) {
        // Get all available time zone IDs
        String[] availableIDs = TimeZone.getAvailableIDs();
        // Check if the given time zone is in the list of available time zone IDs
        return Arrays.asList(availableIDs).contains(tz);
    }

    private boolean isValidLocale(@NonNull String localeString) {
        try {
            // Split the localeString into language and country
            String[] parts = localeString.split("_");
            if (parts.length != 2) {
                return false;
            }
            String language = parts[0];
            String country = parts[1];
            // Create the locale
            Locale locale1 = new Locale(language, country);
            // Get all available locales
            Locale[] availableLocales = Locale.getAvailableLocales();
            // Check if the created localeString is in the list of available locales
            return Arrays.asList(availableLocales).contains(locale1);
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    private void setDefaults() {
        this.calendar = new GregorianCalendar();
        this.tz = TimeZone.getTimeZone("GMT");
        this.locale = Locale.ENGLISH;
        this.calendar.setTimeZone(this.tz);
        this.date = calendar.getTime();
    }
}