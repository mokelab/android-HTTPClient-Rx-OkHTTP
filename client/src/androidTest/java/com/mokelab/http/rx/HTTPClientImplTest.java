package com.mokelab.http.rx;

import android.support.test.runner.AndroidJUnit4;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * test for {@link HTTPClientImpl}
 */
@RunWith(AndroidJUnit4.class)
public class HTTPClientImplTest {
    @Test
    public void send() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final Map<String, Object> result = new HashMap<>();

        HTTPClient client = new HTTPClientImpl(new OkHttpClient());
        client.send(Method.GET, "https://gae-echoserver.appspot.com/test", null, null)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<HTTPResponse>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(HTTPResponse response) {
                        result.put("response", response);
                        result.put("exception", null);
                        latch.countDown();
                    }

                    @Override
                    public void onError(Throwable e) {
                        result.put("response", null);
                        result.put("exception", e);
                        latch.countDown();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
        latch.await(3000, TimeUnit.MILLISECONDS);
        HTTPResponse response = (HTTPResponse) result.get("response");
        if (response == null) {
            Throwable e = (Throwable) result.get("exeption");
            fail("Failed to execute " + e);
            return;
        }
        assertEquals(200, response.status);
        JSONObject json = new JSONObject(response.body);
        assertEquals("/test", json.getString("url"));
        assertEquals("GET", json.getString("method"));
    }
}