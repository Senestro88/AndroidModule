package com.official.senestro.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.official.senestro.core.utils.AdvanceUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.logging.Logger;

/**
 * A utility class for handling JSON data with safe exception handling.
 * This class provides methods to retrieve and manipulate JSON values without
 * throwing unchecked exceptions. All exceptions are logged using a logger.
 */
public class AdvanceJson {
    private final String TAG = AdvanceJson.class.getName(); // Tag for logging purposes.
    private JSONObject json = null; // The internal JSONObject instance.

    /**
     * Constructor to initialize the Json object with a JSON string.
     *
     * @param string A JSON string. If null or invalid, the internal JSONObject will remain uninitialized.
     */
    public AdvanceJson(@Nullable String string) {
        try {
            this.json = new JSONObject(string);
        } catch (Throwable ignored) {
            // If the string is invalid JSON, the json object will remain null.
        }
    }

    /**
     * Retrieves a boolean value from the JSON object.
     *
     * @param name The key of the boolean value.
     * @return The boolean value, or false if the key does not exist or an error occurs.
     */
    public boolean getBoolean(@NonNull String name) {
        try {
            return json.getBoolean(name);
        } catch (Throwable throwable) {
            logger().warning(AdvanceUtils.getMessage(throwable));
        }
        return false;
    }

    /**
     * Retrieves a JSONObject value from the JSON object.
     *
     * @param name The key of the JSONObject.
     * @return The JSONObject, or null if the key does not exist or an error occurs.
     */
    public JSONObject getJSONObject(@NonNull String name) {
        try {
            return json.getJSONObject(name);
        } catch (Throwable throwable) {
            logger().warning(AdvanceUtils.getMessage(throwable));
        }
        return null;
    }

    /**
     * Retrieves a JSONArray value from the JSON object.
     *
     * @param name The key of the JSONArray.
     * @return The JSONArray, or null if the key does not exist or an error occurs.
     */
    public JSONArray getJSONArray(@NonNull String name) {
        try {
            return json.getJSONArray(name);
        } catch (Throwable throwable) {
            logger().warning(AdvanceUtils.getMessage(throwable));
        }
        return null;
    }

    /**
     * Retrieves a string value from the JSON object.
     *
     * @param name The key of the string value.
     * @return The string value, or an empty string if the key does not exist or an error occurs.
     */
    public String getString(@NonNull String name) {
        try {
            return json.getString(name);
        } catch (Throwable throwable) {
            logger().warning(AdvanceUtils.getMessage(throwable));
        }
        return "";
    }

    /**
     * Retrieves a double value from the JSON object.
     *
     * @param name The key of the double value.
     * @return The double value, or -1 if the key does not exist or an error occurs.
     */
    public double getDouble(@NonNull String name) {
        try {
            return json.getDouble(name);
        } catch (Throwable throwable) {
            logger().warning(AdvanceUtils.getMessage(throwable));
        }
        return -1;
    }

    /**
     * Retrieves a long value from the JSON object.
     *
     * @param name The key of the long value.
     * @return The long value, or -1 if the key does not exist or an error occurs.
     */
    public long getLong(@NonNull String name) {
        try {
            return json.getLong(name);
        } catch (Throwable throwable) {
            logger().warning(AdvanceUtils.getMessage(throwable));
        }
        return -1;
    }

    /**
     * Retrieves an integer value from the JSON object.
     *
     * @param name The key of the integer value.
     * @return The integer value, or -1 if the key does not exist or an error occurs.
     */
    public int getInt(@NonNull String name) {
        try {
            return json.getInt(name);
        } catch (Throwable throwable) {
            logger().warning(AdvanceUtils.getMessage(throwable));
        }
        return -1;
    }

    /**
     * Retrieves an object value from the JSON object.
     *
     * @param name The key of the object value.
     * @return The object value, or null if the key does not exist or an error occurs.
     */
    public Object get(@NonNull String name) {
        try {
            return json.get(name);
        } catch (Throwable throwable) {
            logger().warning(AdvanceUtils.getMessage(throwable));
        }
        return null;
    }

    /**
     * Adds or updates a key-value pair in the JSON object.
     *
     * @param key   The key to add or update.
     * @param value The value to associate with the key.
     * @return The updated JSONObject or null if the "this.json is null"
     */
    public JSONObject put(@NonNull String key, @NonNull Object value) {
        try {
            json.put(key, value);
        } catch (Throwable throwable) {
            logger().warning(AdvanceUtils.getMessage(throwable));
        }
        return json;
    }

    /**
     * Overloaded method to add or update a key-value pair with an integer value.
     */
    public JSONObject put(@NonNull String key, int value) {
        try {
            json.put(key, value);
        } catch (Throwable throwable) {
            logger().warning(AdvanceUtils.getMessage(throwable));
        }
        return json;
    }

    /**
     * Overloaded method to add or update a key-value pair with a double value.
     */
    public JSONObject put(@NonNull String key, double value) {
        try {
            json.put(key, value);
        } catch (Throwable throwable) {
            logger().warning(AdvanceUtils.getMessage(throwable));
        }
        return json;
    }

    /**
     * Overloaded method to add or update a key-value pair with a long value.
     */
    public JSONObject put(@NonNull String key, long value) {
        try {
            json.put(key, value);
        } catch (Throwable throwable) {
            logger().warning(AdvanceUtils.getMessage(throwable));
        }
        return json;
    }

    /**
     * Overloaded method to add or update a key-value pair with a boolean value.
     */
    public JSONObject put(@NonNull String key, boolean value) {
        try {
            json.put(key, value);
        } catch (Throwable throwable) {
            logger().warning(AdvanceUtils.getMessage(throwable));
        }
        return json;
    }

    /**
     * Checks whether the JSON object is valid and initialized.
     *
     * @return {@code true} if the internal JSONObject is not null, otherwise {@code false}.
     */
    public boolean valid() {
        return AdvanceUtils.notNull(json);
    }

    /**
     * Returns an iterator over the keys of the JSON object.
     *
     * @return an {@code Iterator<String>} containing the keys of the JSON object,
     * or {@code null} if the JSON object is {@code null}.
     */
    public Iterator<String> keys() {
        return AdvanceUtils.notNull(json) ? json.keys() : null;
    }

    /**
     * Checks if the JSON object contains the specified key.
     *
     * @param key the key to check for, must not be null.
     * @return {@code true} if the key exists and the JSON object is not null, otherwise {@code false}.
     */
    public Boolean has(@NonNull String key) {
        return AdvanceUtils.notNull(json) && json.has(key);
    }

    /**
     * Checks if the value associated with the specified key is null.
     *
     * @param key the key to check, must not be null.
     * @return {@code true} if the value is null and the JSON object is not null, otherwise {@code false}.
     */
    public Boolean isNull(@NonNull String key) {
        return AdvanceUtils.notNull(json) && json.isNull(key);
    }

    /**
     * Returns the number of key-value mappings in the JSON object.
     *
     * @return the number of mappings if the JSON object is not null, otherwise 0.
     */
    public int length() {
        return AdvanceUtils.notNull(json) ? json.length() : 0;
    }

    /**
     * Returns a {@code JSONArray} containing the names of the keys in the JSON object.
     *
     * @return a {@code JSONArray} of key names, or {@code null} if the JSON object is null.
     */
    public JSONArray names() {
        return AdvanceUtils.notNull(json) ? json.names() : null;
    }

    /**
     * Removes the specified key from the JSON object.
     *
     * @param key the key to remove, must not be null.
     * @return the value previously associated with the key, or {@code null} if the key does not exist
     * or the JSON object is null.
     */
    public Object remove(@NonNull String key) {
        return AdvanceUtils.notNull(json) ? json.remove(key) : null;
    }

    /**
     * Converts the JSON object to its string representation.
     *
     * @return a string representation of the JSON object. If an exception occurs, logs the error
     * and returns an empty string.
     */
    @NonNull
    public String toString() {
        try {
            return json.toString();
        } catch (Throwable throwable) {
            logger().warning(AdvanceUtils.getMessage(throwable));
        }
        return "";
    }

    /**
     * Converts the JSON object to its string representation with indentation.
     *
     * @param spaces the number of spaces to use for indentation.
     * @return a formatted string representation of the JSON object. If an exception occurs, logs the error
     * and returns an empty string.
     */
    public String toString(int spaces) {
        try {
            return json.toString(spaces);
        } catch (Throwable throwable) {
            logger().warning(AdvanceUtils.getMessage(throwable));
        }
        return "";
    }

    // PRIVATE METHODS

    /**
     * A private helper method to retrieve the logger instance.
     *
     * @return The logger instance for this class.
     */
    private Logger logger() {
        return Logger.getLogger(TAG);
    }
}