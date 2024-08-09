package com.official.senestro.core.callbacks.interfaces;

import java.util.HashMap;

public interface OkHttpRequestCallback {
		void onResponse(String tag, String response, HashMap<String, Object> responseHeaders);
		void onErrorResponse(String tag, String message);
}