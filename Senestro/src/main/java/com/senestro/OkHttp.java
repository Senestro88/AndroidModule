package com.senestro;

import com.senestro.annotations.NonNull;
import com.senestro.interfaces.OkHttpCallback;
import okhttp3.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class OkHttp {
    private static final int SOCKET_TIMEOUT = 15000;
    private static final int READ_TIMEOUT = 25000;
    private final String TAG = OkHttp.class.getName();
    private final String url;

    // PRIVATE //
    private HashMap<String, Object> params = new HashMap<>();
    private HashMap<String, String> headers = new HashMap<>();
    private RequestMethod method = RequestMethod.GET;
    private final String[] methods = {"POST", "GET", "HEAD"};
    private OkHttpClient client;
    private OkHttpCallback callback;

    // PUBLIC //
    public OkHttp(@NonNull String url) {
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

     public void execute(@NonNull OkHttpCallback callback){

     }

    // PRIVATE
    private void setHeaders(Headers.Builder headersBuilder) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            headersBuilder.add(entry.getKey(), String.valueOf(entry.getValue()));
        }
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
        return method == RequestMethod.GET ? "GET" : (method == RequestMethod.POST ? "POST" : "HEAD");
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
        void execute(OkHttpCallback callbacks);
    }

    // PUBLIC ENUM
    public enum RequestMethod {
        GET, POST, HEAD
    }
}
