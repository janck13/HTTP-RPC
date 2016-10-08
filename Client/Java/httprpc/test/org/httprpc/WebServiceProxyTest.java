/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.httprpc;

import java.net.PasswordAuthentication;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.httprpc.WebServiceProxy;

import static org.httprpc.WebServiceProxy.listOf;
import static org.httprpc.WebServiceProxy.mapOf;
import static org.httprpc.WebServiceProxy.entry;
import static org.httprpc.WebServiceProxy.valueAt;

public class WebServiceProxyTest {
    public static void main(String[] args) throws Exception {
        // Allow self-signed certificates for testing purposes
        X509TrustManager trustManager = new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
                // No-op
            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
                // No-op
            }
        };

        SSLContext sslContext = SSLContext.getInstance("SSL");

        sslContext.init(null, new TrustManager[] {trustManager}, new SecureRandom());

        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, sslSession) -> {
            return true;
        });

        // Create service proxy
        ExecutorService threadPool = Executors.newFixedThreadPool(10);

        WebServiceProxy serviceProxy = new WebServiceProxy(new URL("https://localhost:8443"), threadPool, 3000, 3000);

        // Set credentials
        serviceProxy.setAuthorization(new PasswordAuthentication("tomcat", "tomcat".toCharArray()));

        // Test GET
        // TODO Arguments
        // - number
        // - number list
        // - boolean
        // - boolean list
        // - date
        // - date list
        // - local date
        // - local date list
        serviceProxy.invoke("GET", "/httprpc-server/test", mapOf(
            entry("string", "héllo"),
            entry("strings", listOf("a", "b", "c"))),
            (Map<String, ?> result, Exception exception) -> {
            // TODO Test return values
            // - etc.
            // - locale code
            // - user name
            // - missing value (null)
            validate(exception == null
                && valueAt(result, "string").equals("héllo")
                && valueAt(result, "strings").equals(listOf("a", "b", "c")));
        });

        // Test POST
        // TODO Arguments
        // - string list
        // - number
        // - number list
        // - boolean
        // - boolean list
        // - date
        // - date list
        // - local date
        // - local date list
        // - URL
        // - URL list
        serviceProxy.invoke("POST", "/httprpc-server/test", mapOf(
            entry("string", "héllo")),
            (Map<String, ?> result, Exception exception) -> {
            // TODO Test return values
            // - etc.
            // - locale code
            // - user name
            // - missing value (null)
            // - attachment info
            validate(exception == null && result != null);
        });

        // Test PUT
        serviceProxy.invoke("PUT", "/httprpc-server/test", mapOf(
            entry("text", "héllo")),
            (String result, Exception exception) -> {
            validate(exception == null && result != null && result.equals("göodbye"));
        });

        // Test DELETE
        serviceProxy.invoke("DELETE", "/httprpc-server/test", mapOf(
            entry("id", 101)),
            (Boolean result, Exception exception) -> {
            validate(exception == null && result != null && result.equals(true));
        });

        // Test void
        serviceProxy.invoke("GET", "/httprpc-server/test/void", (result, exception) -> {
            validate(exception == null && result == null);
        });

        // TODO
        /*
        // Test long list
        Future<?> future = serviceProxy.invoke("GET", "/httprpc-server/test/longList", (result, exception) -> {
            // No-op
        });

        Timer timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                validate(future.cancel(true));
            }
        }, 1000);

        // Test delayed result
        serviceProxy.invoke("GET", "/httprpc-server/test/delayedResult", mapOf(entry("result", "abcdefg"), entry("delay", 9000)), (result, exception) -> {
            validate(exception instanceof SocketTimeoutException);
        });

        // Test parallel operations
        Future<Number> sum1 = serviceProxy.invoke("GET", "/httprpc-server/test/sum", mapOf(entry("a", 1), entry("b", 2)), null);
        Future<Number> sum2 = serviceProxy.invoke("GET", "/httprpc-server/test/sum", mapOf(entry("a", 2), entry("b", 4)), null);
        Future<Number> sum3 = serviceProxy.invoke("GET", "/httprpc-server/test/sum", mapOf(entry("a", 3), entry("b", 6)), null);

        validate(sum1.get().equals(3) && sum2.get().equals(6) && sum3.get().equals(9));
        */

        // Shut down thread pool
        threadPool.shutdown();
    }

    private static void validate(boolean condition) {
        System.out.println(condition ? "OK" : "FAIL");
    }
}