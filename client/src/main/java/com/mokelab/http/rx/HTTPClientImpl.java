package com.mokelab.http.rx;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Implementation
 */
public class HTTPClientImpl implements HTTPClient {
    private static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain; charset=utf-8";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";

    private final OkHttpClient client;

    public HTTPClientImpl(OkHttpClient client) {
        this.client = client;
    }

    @Override
    public Single<HTTPResponse> send(final Method method, final String url, final Header header, final String body) {
        return Single.create(new SingleOnSubscribe<HTTPResponse>() {
            @Override
            public void subscribe(SingleEmitter<HTTPResponse> e) throws Exception {
                Request.Builder builder = new Request.Builder();
                switch (method) {
                case GET:
                    builder.get();
                    break;
                case POST:
                    builder.post(RequestBody.create(MediaType.parse(getContentType(header)), body));
                    break;
                case PUT:
                    builder.put(RequestBody.create(MediaType.parse(getContentType(header)), body));
                    break;
                case DELETE:
                    builder.delete();
                    break;
                }
                builder.url(url);
                setHeaders(builder, header);
                Request request = builder.build();

                Response response;
                try {
                    response = client.newCall(request).execute();

                    HTTPResponse httpResponse = new HTTPResponse();
                    httpResponse.status = response.code();
                    if (httpResponse.status == 204) {
                        httpResponse.body = "";
                    } else {
                        httpResponse.body = response.body().string();
                    }
                    httpResponse.header = new HeaderImpl(response.headers());

                    e.onSuccess(httpResponse);
                } catch (IOException e2) {
                    if (!e.isDisposed()) {
                        e.onError(new HTTPException(e2));
                    }
                }
            }
        });
    }

    private String getContentType(Header header) {
        if (header == null) {
            return CONTENT_TYPE_TEXT_PLAIN;
        }
        String type = header.get(HEADER_CONTENT_TYPE);
        if (type == null) {
            return CONTENT_TYPE_TEXT_PLAIN;
        }
        return type;
    }

    private void setHeaders(Request.Builder builder, Header header) {
        if (header == null) {
            return;
        }
        Set<Map.Entry<String, String>> entries = header.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }
    }
}
