package httputils;

import okhttp3.*;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class HttpClientUtil {

    private final static SimpleCookieManager simpleCookieManager = new SimpleCookieManager();
    private final static OkHttpClient HTTP_CLIENT =
            new OkHttpClient.Builder()
                    .cookieJar(simpleCookieManager)
                    .followRedirects(false)
                    .build();

    public static void setCookieManagerLoggingFacility(Consumer<String> logConsumer) {
        simpleCookieManager.setLogData(logConsumer);
    }

    public static void removeCookiesOf(String domain) {
        simpleCookieManager.removeCookiesOf(domain);
    }

    public static void runAsync(String finalUrl, Callback callback) {
        Request request = new Request.Builder()
                .url(finalUrl)
                .build();

        Call call = HttpClientUtil.HTTP_CLIENT.newCall(request);
        call.enqueue(callback);
    }

    public static void shutdown() {
        System.out.println("Shutting down HTTP CLIENT");
        HTTP_CLIENT.dispatcher().executorService().shutdown();
        HTTP_CLIENT.connectionPool().evictAll();
    }

    // New method to upload files asynchronously
    public static void uploadFileAsync(String url, File file, String filePath, String uploaderName, Callback callback) {
        // Create a request body with file and media type
        RequestBody fileBody = RequestBody.create(file, MediaType.parse("application/xml"));

        // Create a multipart body, including filePath and uploaderName
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), fileBody)
                .addFormDataPart("filePath", filePath)  // Include the file path
                .addFormDataPart("uploaderName", uploaderName)  // Include the uploader's name
                .build();

        // Create a POST request
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        // Send the request asynchronously
        Call call = HTTP_CLIENT.newCall(request);
        call.enqueue(callback);
    }

}
