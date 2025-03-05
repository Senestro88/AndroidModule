package com.official.senestro.core;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.official.senestro.core.callbacks.interfaces.OkHttpRequestCallback;
import com.official.senestro.core.utils.AdvanceUtils;
import okhttp3.*;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class OkHttpRequest {
    private final String tag = OkHttpRequest.class.getName();

    // PRIVATE //
    private HashMap<String, Object> params = new HashMap<>();
    private HashMap<String, String> headers = new HashMap<>();
    private RequestMethod method;
    private final String[] methods = {"POST", "GET"};
    private String url;
    private static final int SOCKET_TIMEOUT = 15000;
    private static final int READ_TIMEOUT = 25000;
    private OkHttpClient client;
    private OkHttpRequestCallback callback;

    // PUBLIC //
    public OkHttpRequest(@NonNull String url, @NonNull RequestMethod method) {
        this.method = method;
        this.url = url;
    }

    public void setHeaders(@NonNull HashMap<String, String> headers) {
        this.headers = headers;
    }

    public void setHeader(@NonNull String key, @NonNull String value) {
        this.headers.put(key, value);
    }

    public void setMethod(@NonNull RequestMethod method) {
        this.method = method;
    }

    public void setParams(@NonNull HashMap<String, Object> params) {
        this.params = params;
    }

    public void setParam(@NonNull String key, @NonNull Object value) {
        this.params.put(key, value);
    }

    public HashMap<String, Object> getParams() {
        return params;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public String getMethod() {
        return convertMethod();
    }

    public void execute(@NonNull String tag, @Nullable OkHttpRequestCallback callback) {
        this.callback = callback;
        if (!isMethodValid()) {
            postToCallback(listener -> listener.onErrorResponse(tag, "The request method is not valid"));
        } else {
            OkHttpClient client = getClient(url);
            if (AdvanceUtils.isNull(client)) {
                postToCallback(listener -> listener.onErrorResponse(tag, "Failed to get OkHttpClient"));
            } else {
                try {
                    Headers.Builder headersBuilder = new Headers.Builder();
                    setHeaders(headersBuilder);
                    Request.Builder requestBuilder = this.method == RequestMethod.GET ? createGetBuilder(headersBuilder) : createPostMultipartFormBuilder(headersBuilder);
                    Request request = requestBuilder.build();
                    client.newCall(request).enqueue(new Callback() {
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

    private void setHeaders(Headers.Builder headersBuilder) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            headersBuilder.add(entry.getKey(), String.valueOf(entry.getValue()));
        }
    }

    private Request.Builder createGetBuilder(Headers.Builder headersBuilder) {
        Request.Builder requestBuilder = new Request.Builder();
        HttpUrl parse = HttpUrl.parse(url);
        if (AdvanceUtils.notNull(parse)) {
            HttpUrl.Builder builder = parse.newBuilder();
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                builder.addQueryParameter(entry.getKey(), String.valueOf(entry.getValue()));
            }
            requestBuilder.url(builder.build()).headers(headersBuilder.build()).get();
        }
        return requestBuilder;
    }

    private Request.Builder createPostFormBuilder(Headers.Builder headersBuilder) {
        Request.Builder requestBuilder = new Request.Builder();
        FormBody.Builder builder = new FormBody.Builder();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            builder.add(entry.getKey(), String.valueOf(entry.getValue()));
        }
        requestBuilder.url(url).headers(headersBuilder.build()).post(builder.build());
        return requestBuilder;
    }

    private Request.Builder createPostMultipartFormBuilder(Headers.Builder headersBuilder) {
        Request.Builder requestBuilder = new Request.Builder();
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof File) {
                File file = ((File) value);
                builder.addFormDataPart(entry.getKey(), file.getName(), RequestBody.create(file, MediaType.parse("application/octet-stream")));
            } else {
                builder.addFormDataPart(entry.getKey(), value.toString());
            }
        }
        requestBuilder.url(url).headers(headersBuilder.build()).post(builder.build());
        return requestBuilder;
    }

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

    private OkHttpClient getClient(@NonNull String url) {
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
                Log.e(tag, e.getMessage(), e);
                client = null;
            }
        }
        return client;
    }

    private boolean isMethodValid() {
        for (String element : this.methods) {
            if (element.equals(convertMethod())) {
                return true;
            }
        }
        return false;
    }

    private String convertMethod() {
        if (method == RequestMethod.GET) {
            return "GET";
        } else {
            return "POST";
        }
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