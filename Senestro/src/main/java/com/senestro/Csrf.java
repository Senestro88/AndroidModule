package com.senestro;

import com.google.gson.Gson;
import com.senestro.annotations.NonNull;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * A utility class for generating and validating CSRF tokens using AES encryption and HMAC hashing.
 * This class cannot be instantiated.
 *
 * <p>Tokens include a unique identifier, a salt, and an expiration time. They are securely encrypted
 * and validated using a symmetric key.</p>
 *
 * @author Senestro
 */
public class Csrf {

    // Symmetric key used for token encryption and HMAC hashing
    private static final String KEY = "1291707447071921";
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    /**
     * Private constructor to prevent instantiation.
     */
    private Csrf() {
    }

    /**
     * Generates a CSRF token with the specified expiration time.
     *
     * @param minutes The number of minutes the token will remain valid.
     * @return A {@link TokenResult} object containing the encrypted token.
     */
    public static TokenResult generateToken(int minutes) {
        TokenResult result = null;
        try {
            byte[] keyBytes = KEY.getBytes(CHARSET);
            String hex = Utils.newHex(32); // Generate a random 32-character hex string
            byte[] hexBytes = hex.getBytes(CHARSET);

            // Generate a HMAC-based salt
            String salt = Utils.hashHmac(hexBytes, keyBytes);

            // Calculate the token's expiration time
            long exp = setFutureMinutes(minutes);

            // Create the token payload
            JSONObject json = new JSONObject();
            json.put("data", hex);
            json.put("salt", salt);
            json.put("exp", exp);

            // Encrypt the payload
            byte[] dataBytes = json.toString().getBytes(CHARSET);
            Aes.EncryptResult resultData = Aes.encrypt(dataBytes, keyBytes);

            // Wrap the encryption result in a TokenResult
            result = new TokenResult(resultData);
        } catch (Exception exception) {
            System.err.println("Error generating CSRF token: " + exception.getMessage());
        }
        return result;
    }

    /**
     * Validates a CSRF token based on its encrypted data and initialization vector.
     *
     * @param dataBytes The encrypted token data.
     * @param ivBytes The initialization vector used during encryption.
     * @return {@code true} if the token is valid; {@code false} otherwise.
     */
    public static boolean validToken(@NonNull byte[] dataBytes, @NonNull byte[] ivBytes) {
        TokenResult tokenResult = new TokenResult(new Aes.EncryptResult(ivBytes, dataBytes));
        return validToken(tokenResult);
    }

    /**
     * Validates a CSRF token represented by a {@link TokenResult} object.
     *
     * @param tokenResult The {@link TokenResult} object containing the token data.
     * @return {@code true} if the token is valid; {@code false} otherwise.
     */
    public static boolean validToken(@NonNull TokenResult tokenResult) {
        try {
            // Extract the encrypted data and IV from the token
            byte[] dataBytes = tokenResult.getResultBytes();
            byte[] ivBytes = tokenResult.getIvBytes();
            byte[] keyBytes = KEY.getBytes(CHARSET);

            // Decrypt the token payload
            Aes.DecryptResult result = Aes.decrypt(dataBytes, keyBytes, ivBytes);
            String resultData = new String(result.getResultBytes(), CHARSET);

            // Deserialize the payload
            TokenObject object = new Gson().fromJson(resultData, TokenObject.class);

            // Recompute the salt and validate it
            String salt = Utils.hashHmac(object.data.getBytes(CHARSET), keyBytes);
            if (MessageDigest.isEqual(salt.getBytes(), object.salt.getBytes())) {
                // Check if the token is still within its validity period
                return isMinuteInFuture(object.exp);
            }
        } catch (Exception exception) {
            System.err.println("Error validating CSRF token: " + exception.getMessage());
        }
        return false;
    }

    /**
     * Gets the current time in minutes since the Unix epoch.
     *
     * @return The current time in minutes.
     */
    private static long getMinutesFromTime() {
        return System.currentTimeMillis() / 60000;
    }

    /**
     * Calculates a future time in minutes by adding the specified number of minutes to the current time.
     *
     * @param minutes The number of minutes to add.
     * @return The future time in minutes.
     */
    private static long setFutureMinutes(int minutes) {
        return getMinutesFromTime() + minutes;
    }

    /**
     * Checks if a given time in minutes is in the future compared to the current time.
     *
     * @param minutes The time to check, in minutes.
     * @return {@code true} if the time is in the future; {@code false} otherwise.
     */
    private static boolean isMinuteInFuture(long minutes) {
        return minutes > getMinutesFromTime();
    }

    /**
     * A private class representing the decrypted contents of a token.
     */
    private static class TokenObject {
        private String data; // Unique identifier
        private String salt; // HMAC-based salt
        private long exp;    // Expiration time in minutes
    }

    /**
     * A public class representing an encrypted CSRF token.
     */
    public static class TokenResult {
        private final Aes.EncryptResult result;

        /**
         * Constructs a new {@link TokenResult}.
         *
         * @param result The encryption result containing the token data.
         */
        public TokenResult(@NonNull Aes.EncryptResult result) {
            this.result = result;
        }

        /**
         * Gets the initialization vector used for the token encryption.
         *
         * @return The initialization vector as a byte array.
         */
        public byte[] getIvBytes() {
            return result.getIvBytes();
        }

        /**
         * Gets the encrypted token data.
         *
         * @return The encrypted token data as a byte array.
         */
        public byte[] getResultBytes() {
            return result.getResultBytes();
        }
    }
}
