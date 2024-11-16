package com.lbtmo.ticketingsystem;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.util.HashMap;
import java.util.Map;

public class VolleyMultipartRequest extends Request<NetworkResponse> {
    private final Response.Listener<NetworkResponse> listener;
    private final Map<String, String> params;
    private final Map<String, DataPart> byteData;

    public VolleyMultipartRequest(int method, String url,
                                  Response.Listener<NetworkResponse> listener,
                                  Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.listener = listener;
        this.params = new HashMap<>();
        this.byteData = new HashMap<>();
    }

    @Override
    protected Map<String, String> getParams() {
        return params;
    }

    protected Map<String, DataPart> getByteData() {
        return byteData;
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        return Response.success(response, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        listener.onResponse(response);
    }

    public void addByteData(String key, DataPart dataPart) {
        byteData.put(key, dataPart);
    }

    public static class DataPart {
        String fileName;
        byte[] content;
        String type;

        public DataPart(String fileName, byte[] content, String type) {
            this.fileName = fileName;
            this.content = content;
            this.type = type;
        }
    }
}