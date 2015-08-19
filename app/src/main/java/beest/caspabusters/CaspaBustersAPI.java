package beest.caspabusters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.ResponseCallback;
import retrofit.RestAdapter;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.mime.TypedByteArray;

/**
 * A wrapper for the CaspaBustersService interface that contains helper methods for each of the
 * service's API endpoints.
 */
public class CaspaBustersAPI {

//    private static final String API_URL = "https://caspabusters.herokuapp.com";
    private static final String API_URL = "http://10.0.2.2:5000";
    private static final RestAdapter.LogLevel LOG_LEVEL = RestAdapter.LogLevel.FULL;

    /**
     * The Retrofit service for the CASPABusters API
     */
    private interface CaspaBustersService {
        @GET("/request/all")
        void allRequests(Callback<ArrayList<Request>> callback);

        @GET("/request/available")
        void availableRequests(Callback<ArrayList<Request>> callback);

        @POST("/request/new")
        void createRequest(@Body Request request, Callback<ObjectId> callback);

        @POST("/request/delete")
        void deleteRequest(@Body ObjectId id, ResponseCallback callback);

        @POST("/request/verify/1")
        void verifyMathProblem(@Body ObjectId id, ResponseCallback callback);
    }

    private static Gson gson = new GsonBuilder()
            .setDateFormat(Request.dbDateFormat.toPattern())
            .create();

    private static RestAdapter restAdapter = new RestAdapter.Builder()
            .setConverter(new GsonConverter(gson))
            .setEndpoint(API_URL)
            .setLogLevel(LOG_LEVEL)
            .build();

    private static CaspaBustersService service = restAdapter.create(CaspaBustersService.class);

    public static void getAllRequests(Callback<ArrayList<Request>> callback) {
        service.allRequests(callback);
    }

    public static void getAvailableRequests(Callback<ArrayList<Request>> callback) {
        service.availableRequests(callback);
    }

    public static void postNewRequest(Request newRequest, Callback<ObjectId> callback) {
        service.createRequest(newRequest, callback);
    }

    public static void deleteRequest(ObjectId id, ResponseCallback callback) {
        service.deleteRequest(id, callback);
    }

    public static void verifyMathProblem(ObjectId id, ResponseCallback callback) {
        service.verifyMathProblem(id, callback);
    }

    public static String getResponseString(Response response) {
        return new String(((TypedByteArray) response.getBody()).getBytes());
    }
}
