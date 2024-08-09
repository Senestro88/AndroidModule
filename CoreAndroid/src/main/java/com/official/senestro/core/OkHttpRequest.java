package com.official.senestro.core;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.official.senestro.core.callbacks.interfaces.OkHttpRequestCallback;
import com.official.senestro.core.utils.AdvanceUtils;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpRequest {
    // PRIVATE //
    private final Activity activity;
    private HashMap<String, Object> params = new HashMap<>();
    private HashMap<String, String> headers = new HashMap<>();
    private RequestMethod requestMethod;
    private String[] allowedRequestMethods = {"POST", "GET"};
    private String url;
    private static final int SOCKET_TIMEOUT = 15000;
    private static final int READ_TIMEOUT = 25000;
    private OkHttpClient client;
    private OkHttpRequestCallback callback;

    // PUBLIC //
    public OkHttpRequest(@NonNull Activity activity) {
        this.activity = activity;
    }

    public void setHeaders(@NonNull HashMap<String, String> headers) {
        this.headers = headers;
    }

    public void addHeaders(@NonNull String key, @NonNull String value) {
        this.headers.put(key, value);
    }

    public void setRequestMethod(@NonNull RequestMethod requestMethod) {
        this.requestMethod = requestMethod;
    }

    public void setParams(@NonNull HashMap<String, Object> params) {
        this.params = params;
    }

    public void addParams(@NonNull String key, @NonNull Object value) {
        this.params.put(key, value);
    }

    public HashMap<String, Object> getParams() {
        return params;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public Activity getActivity() {
        return activity;
    }

    public String getRequestMethod() {
        return convertRequestMethod();
    }

    public void execute(@NonNull RequestMethod requestMethod, @NonNull String url, @NonNull String tag, @Nullable OkHttpRequestCallback callback) {
        this.requestMethod = requestMethod;
        this.url = url;
        this.callback = callback;
        if (!isRequestMethodValid()) {
            postToCallback(listener -> listener.onErrorResponse(tag, "The request method is not valid"));
        } else {
            OkHttpClient client = getRequestClient(url);
            if (client == null) {
                postToCallback(listener -> listener.onErrorResponse(tag, "Failed to get OkHttpClient"));
            } else {
                Request.Builder requestBuilder = new Request.Builder();
                Headers.Builder headerBuilder = new Headers.Builder();
                // Set header
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    headerBuilder.add(entry.getKey(), String.valueOf(entry.getValue()));
                }
                try {
                    if (requestMethod == RequestMethod.GET) {
                        HttpUrl httpUrl = HttpUrl.parse(url);
                        if (AdvanceUtils.notNull(httpUrl)) {
                            HttpUrl.Builder httpUrlBuilder = httpUrl.newBuilder();
                            if (AdvanceUtils.notNull(httpUrlBuilder)) {
                                for (Map.Entry<String, Object> entry : params.entrySet()) {
                                    httpUrlBuilder.addQueryParameter(entry.getKey(), String.valueOf(entry.getValue()));
                                }
                                requestBuilder.url(httpUrlBuilder.build()).headers(headerBuilder.build()).get();
                            }
                        }
                    } else {
                        FormBody.Builder form = new FormBody.Builder();
                        // Set POST body
                        for (Map.Entry<String, Object> entry : params.entrySet()) {
                            form.add(entry.getKey(), String.valueOf(entry.getValue()));
                        }
                        RequestBody requestBody = form.build();
                        // Ensure headerBuilder includes "Content-Type" for POST
                        requestBuilder.url(url).headers(headerBuilder.build()).method(convertRequestMethod(), requestBody);
                    }
                    Request req = requestBuilder.build();
                    client.newCall(req).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull final IOException e) {
                            postToCallback(listener -> listener.onErrorResponse(tag, e.getMessage()));
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull final Response response) {
                            try {
                                String responseBody = response.body().string().trim();
                                Headers responseHeaders = response.headers();
                                HashMap<String, Object> headersMap = new HashMap<>();
                                for (String headerName : responseHeaders.names()) {
                                    headersMap.put(headerName, responseHeaders.get(headerName) != null ? responseHeaders.get(headerName) : "");
                                }
                                postToCallback(listener -> listener.onResponse(tag, responseBody, headersMap));
                            } catch (IOException e) {
                                postToCallback(listener -> listener.onErrorResponse(tag, e.getMessage()));
                            }
                        }
                    });
                } catch (Throwable e) {
                    postToCallback(listener -> listener.onErrorResponse(tag, e.getMessage()));
                }
            }
        }
    }

    // PRIVATE
    private void postToMainThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    private void postToCallback(CallbackExecutor executor) {
        if (callback != null) {
            postToMainThread(() -> executor.execute(callback));
        }
    }

    @SuppressLint({"CustomX509TrustManager", "TrustAllX509TrustManager"})
    private X509TrustManager x509TrustManager() throws IllegalStateException {
        TrustManager[] trustManagers = new TrustManager[]{new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[]{};
            }
        }};
        if (!(trustManagers[0] instanceof X509TrustManager)) {
            throw new IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
        }
        return (X509TrustManager) trustManagers[0];
    }

    private OkHttpClient getRequestClient(@NonNull String url) {
        if (client == null) {
            try {
                System.setProperty("javax.net.debug", "ssl");
                OkHttpClient.Builder builder = new OkHttpClient.Builder();
                X509TrustManager trustManager = x509TrustManager();
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, new TrustManager[]{trustManager}, new SecureRandom());
                SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
                builder.sslSocketFactory(sslSocketFactory, trustManager);
                builder.connectTimeout(SOCKET_TIMEOUT, TimeUnit.MILLISECONDS);
                builder.readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS);
                builder.writeTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS);
                builder.addInterceptor(new RequestInterceptor(url));
                builder.hostnameVerifier((hostname, session) -> true);
                client = builder.build();
            } catch (Exception e) {
                e.printStackTrace();
                client = null;
            }
        }
        return client;
    }

    private boolean isRequestMethodValid() {
        for (String element : this.allowedRequestMethods) {
            if (element.equals(convertRequestMethod())) {
                return true;
            }
        }
        return false;
    }

    private String convertRequestMethod() {
        if (requestMethod == RequestMethod.GET) {
            return "GET";
        } else {
            return "POST";
        }
    }

    private String inlineRequestParams() {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            result.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        if (result.length() > 0) {
            result.deleteCharAt(result.length() - 1); // Remove trailing '&'
        }
        return result.toString();
    }

    // PRIVATE CLASS
    private static class RequestInterceptor implements Interceptor {
        private final String url;

        private RequestInterceptor(@NonNull String url) {
            this.url = url;
        }

        @NonNull
        @Override
        public Response intercept(@NonNull Chain chain) throws IOException {
            // You can access and manipulate the request here
            Request originalRequest = chain.request();
            return chain.proceed(originalRequest);
        }
    }

    // PRIVATE INTERFACE
    @FunctionalInterface
    private interface CallbackExecutor {
        void execute(OkHttpRequestCallback callbacks);
    }

    // PUBLIC ENUM
    public enum RequestMethod {
        GET, POST
    }
}