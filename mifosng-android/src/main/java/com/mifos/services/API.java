package com.mifos.services;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.mifos.objects.SearchedEntity;
import com.mifos.objects.User;
import com.mifos.objects.accountTransfer.AccountTransferRequest;
import com.mifos.objects.accountTransfer.AccountTransferResponse;
import com.mifos.objects.accountTransfer.AccountTransferTemplateRequest;
import com.mifos.objects.accountTransfer.AccountTransferTemplateResponse;
import com.mifos.objects.accounts.ClientAccounts;
import com.mifos.objects.accounts.pgs.ServiceAccountRequest;
import com.mifos.objects.accounts.pgs.ServiceAccountResponse;
import com.mifos.objects.accounts.savings.SavingsAccountTransactionRequest;
import com.mifos.objects.accounts.savings.SavingsAccountTransactionResponse;
import com.mifos.objects.accounts.savings.SavingsAccountWithAssociations;
import com.mifos.objects.client.Client;
import com.mifos.objects.client.CreateClientTransactionRequest;
import com.mifos.objects.client.CreateClientTransactionResponse;
import com.mifos.objects.client.Page;
import com.mifos.objects.db.CollectionSheet;
import com.mifos.objects.noncore.DataTable;
import com.mifos.objects.templates.clients.NewClientTemplate;
import com.mifos.objects.templates.pgs.ServiceAccountTemplate;
import com.mifos.objects.templates.savings.SavingsAccountTransactionTemplate;
import com.mifos.services.data.CollectionSheetPayload;
import com.mifos.services.data.GpsCoordinatesRequest;
import com.mifos.services.data.GpsCoordinatesResponse;
import com.mifos.services.data.Payload;
import com.mifos.services.data.SaveResponse;
import com.mifos.utils.Constants;

import java.util.Iterator;
import java.util.List;

import retrofit.Callback;
import retrofit.ErrorHandler;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.mime.TypedFile;

public class API {

    // Mifos instance
    public static String mMifosInstanceUrl = "https://demo.openmf.org/mifosng-provider/api/v1";
    public static final String ACCEPT_JSON = "Accept: application/json";
    public static final String CONTENT_TYPE_JSON = "Content-Type: application/json";

    // PGS instance
    public static String mPGSInstanceUrl = "https://10.0.0.6:8443/mifosng-provider/api/v1/";

    /*
        As Mifos is a multi-tenant platform, all requests require you to specify a tenant
        as a header in each request.
     */
    public static final String HEADER_MIFOS_TENANT_ID = "X-Mifos-Platform-TenantId";

    public static final String HEADER_AUTHORIZATION = "Authorization";

    static RestAdapter sRestAdapter;
    public static CenterService centerService;
    public static ClientAccountsService clientAccountsService;
    public static ClientService clientService;
    public static SavingsAccountService savingsAccountService;
    public static SearchService searchService;
    public static UserAuthService userAuthService;
    public static AccountTransfersService accountTransfersService;
    public static PGSServiceAccountService pgsServiceAccountService;
    // TODO: this service is not done yet!
    public static GpsCoordinatesService gpsCoordinatesService;

    static {
        init();
    }

    private static synchronized void init() {
        sRestAdapter = createRestAdapter(getInstanceUrl());
        centerService = sRestAdapter.create(CenterService.class);
        clientAccountsService = sRestAdapter.create(ClientAccountsService.class);
        clientService = sRestAdapter.create(ClientService.class);
        savingsAccountService = sRestAdapter.create(SavingsAccountService.class);
        searchService = sRestAdapter.create(SearchService.class);
        userAuthService = sRestAdapter.create(UserAuthService.class);
        gpsCoordinatesService = sRestAdapter.create(GpsCoordinatesService.class);
        accountTransfersService = sRestAdapter.create(AccountTransfersService.class);

    }

    private static RestAdapter createRestAdapter(final String url)  {

        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(url)
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade request) {
                        if (url.contains("developer")) {
                            request.addHeader(HEADER_MIFOS_TENANT_ID, "developer");
                        } else {
                            request.addHeader(HEADER_MIFOS_TENANT_ID, "default");
                        }

                        /*
                            Look for the Auth token in the shared preferences
                            and add it to the request. Because it is mandatory to
                            supply the Authorization Header in every request
                        */

                        SharedPreferences pref = PreferenceManager
                                .getDefaultSharedPreferences(Constants.applicationContext);
                        String authToken = pref.getString(User.AUTHENTICATION_KEY, "NA");

                        if (authToken != null && !"NA".equals(authToken)) {
                            request.addHeader(HEADER_AUTHORIZATION, authToken);
                        }

                    }
                })
                .setErrorHandler(new MifosRestErrorHandler())
                .build();
        // TODO: This logging is sometimes excessive, e.g. for client image requests.
        restAdapter.setLogLevel(RestAdapter.LogLevel.FULL);
        return restAdapter;
    }

    public static void changeRestAdapterLogLevel(RestAdapter.LogLevel logLevel) {
        sRestAdapter.setLogLevel(logLevel);
    }

    static class MifosRestErrorHandler implements ErrorHandler {
        @Override
        public Throwable handleError(RetrofitError retrofitError) {

            Response r = retrofitError.getResponse();
            if (r != null && r.getStatus() == 401) {
                Log.e("Status", "Authentication Error.");


            }else if(r.getStatus() == 400){
                Log.d("Status","Bad Request - Invalid Parameter or Data Integrity Issue.");
                Log.d("URL", r.getUrl());
                List<retrofit.client.Header> headersList = r.getHeaders();
                Iterator<retrofit.client.Header> iterator = headersList.iterator();
                while(iterator.hasNext())
                {    retrofit.client.Header header = iterator.next();
                    Log.d("Header ",header.toString());
                }
            }

            return retrofitError;
        }

    }

    public interface CenterService {

        @Headers({ACCEPT_JSON, CONTENT_TYPE_JSON})
        @GET("/centers")
        public void getAllCenters(Callback<List<com.mifos.objects.Center>> callback);

        @Headers({ACCEPT_JSON, CONTENT_TYPE_JSON})
        @POST("/centers/2026?command=generateCollectionSheet")
        public void getCenter(@Body Payload payload, Callback<CollectionSheet> callback);

        @Headers({ACCEPT_JSON, CONTENT_TYPE_JSON})
        @POST("/centers/2026?command=saveCollectionSheet")
        public SaveResponse saveCollectionSheet(@Body CollectionSheetPayload collectionSheetPayload);

    }

    public interface ClientAccountsService {

        @GET("/clients/template")
        public void getClientDetailsTemplate(Callback<NewClientTemplate> clientTemplateCallback);

        @Headers({ACCEPT_JSON, CONTENT_TYPE_JSON})
        @GET("/clients/{clientId}/accounts")
        public void getAllAccountsOfClient(@Path("clientId") int clientId, Callback<ClientAccounts> callback);

        @GET("/clients/{clientId}/accounts")
        public void getClientsAccountsByType(@Path("clientId") int clientId, @Query("fields") String accountsType, Callback<ClientAccounts> callback);
    }

    public interface ClientService {

        @Headers({ACCEPT_JSON, CONTENT_TYPE_JSON})
        @GET("/clients")
        public void listAllClients(Callback<Page<Client>> callback);

        @Headers({ACCEPT_JSON, CONTENT_TYPE_JSON})
        @GET("/clients/{clientId}")
        public void getClient(@Path("clientId") int clientId, Callback<Client> callback);

        @Multipart
        @POST("/clients/{clientId}/images")
        public void uploadClientImage(@Path("clientId") int clientId,
                                      @Part("file") TypedFile file,
                                      Callback<Response> callback);

        @POST("/clients")
        public void createNewClient(@Body CreateClientTransactionRequest createClientTransactionRequest,
                                    Callback<CreateClientTransactionResponse> clientTransactionResponseCallback);

        @Headers({ACCEPT_JSON, CONTENT_TYPE_JSON})
        @DELETE("/clients/{clientId}/images")
        void deleteClientImage(@Path("clientId") int clientId, Callback<Response> callback);

        @Headers({"Accept: application/octet-stream", CONTENT_TYPE_JSON})
        @GET("/clients/{clientId}/images")
        public void getClientImage(@Path("clientId") int clientId, Callback<TypedFile> callback);

        @Headers({ACCEPT_JSON, CONTENT_TYPE_JSON})
        @GET("/datatables?apptable=m_client")
        public void getDatatablesOfClient(Callback<List<DataTable>> callback);

        @GET("/clients")
        public void listClientsFilteredByFirstName(@Query("firstName") String firstName, Callback<Page<Client>> callback);

    }

    public interface SearchService {

        @Headers({ACCEPT_JSON, CONTENT_TYPE_JSON})
        @GET("/search?resource=clients")
        public void searchClientsByName(@Query("query") String clientName, Callback<List<SearchedEntity>> callback);

    }

    public interface SavingsAccountService {

        /**
         *
         * @param savingsAccountId - savingsAccountId for which information is requested
         * @param association - Mention Type of Association Needed, Like :- all, transactions etc.
         * @param savingsAccountWithAssociationsCallback - callback to receive the response
         *
         * Use this method to retrieve the Savings Account With Associations
         */
        @Headers({ACCEPT_JSON, CONTENT_TYPE_JSON})
        @GET("/savingsaccounts/{savingsAccountId}")
        public void getSavingsAccountWithAssociations(@Path("savingsAccountId") int savingsAccountId,
                                                      @Query("associations") String association,
                                                      Callback<SavingsAccountWithAssociations> savingsAccountWithAssociationsCallback);

        /**
         *
         * @param savingsAccountId - savingsAccountId for which information is requested
         * @param savingsAccountTransactionTemplateCallback - Savings Account Transaction Template Callback
         *
         * Use this method to retrieve the Savings Account Transaction Template
         */
        @Headers({ACCEPT_JSON, CONTENT_TYPE_JSON})
        @GET("/savingsaccounts/{savingsAccountId}/transactions/template")
        public void getSavingsAccountTransactionTemplate(@Path("savingsAccountId") int savingsAccountId, Callback<SavingsAccountTransactionTemplate> savingsAccountTransactionTemplateCallback);

        @Headers({ACCEPT_JSON, CONTENT_TYPE_JSON})
        @POST("/savingsaccounts/{savingsAccountId}/transactions")
        public void processTransaction(@Path("savingsAccountId") int savingsAccountId,
                                       @Query("command") String transactionType,
                                       @Body SavingsAccountTransactionRequest savingsAccountTransactionRequest,
                                       Callback<SavingsAccountTransactionResponse> savingsAccountTransactionResponseCallback);

    }

    public interface AccountTransfersService{

        @Headers({ACCEPT_JSON, CONTENT_TYPE_JSON})
        @GET("/template")
        public void retrieveTemplate(@Body AccountTransferTemplateRequest accountTransferTemplateRequest,
                                     Callback<AccountTransferTemplateResponse> callback);

        @Headers({ACCEPT_JSON, CONTENT_TYPE_JSON})
        @POST("/accounttransfers")
        public void createTransfer(@Body AccountTransferRequest accountTransferRequest,
                                   Callback<AccountTransferResponse> callback);

    }

    public interface PGSServiceAccountService{

        @Headers({ACCEPT_JSON, CONTENT_TYPE_JSON})
        @GET("/serviceaccount/{serviceAccountId}")
        public void retrieveTemplate(@Path("serviceAccountId") int serviceAccountId,
                                     Callback<ServiceAccountTemplate> serviceAccountTemplateCallback);

        @Headers({ACCEPT_JSON, CONTENT_TYPE_JSON})
        @POST("/serviceaccount")
        public void createServiceAccount(@Body ServiceAccountRequest serviceAccountRequest,
                                         Callback<ServiceAccountResponse> callback);

    }

    /**
     * Service for authenticating users.
     * No other service can be used without authentication.
     */

    public interface UserAuthService {

        @Headers({ACCEPT_JSON, CONTENT_TYPE_JSON})
        @POST("/authentication")
        public void authenticate(@Query("username") String username, @Query("password") String password, Callback<User> userCallback);

    }

    /**
     * Service for getting and retrieving GPS coordinates for a client's location, stored
     * in a custom data table.
     * TODOs:
     * getGpsCoordinates needs to be added.
     * setGpsCoordinates is not working yet - currently there is something wrong with the formatting of the request.
     */
    public interface GpsCoordinatesService {

        @POST("/datatables/gps_coordinates/{clientId}?genericResultSet=true")
        public void setGpsCoordinates(@Path("clientId") int clientId,
                                      @Body GpsCoordinatesRequest coordinates,
                                      Callback<GpsCoordinatesResponse> callback);
    }

    public static <T> Callback<T> getCallback(T t) {
        Callback<T> cb = new Callback<T>() {
            @Override
            public void success(T o, Response response) {
                System.out.println("Object " + o);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                System.out.println("Error: " + retrofitError);
            }
        };

        return cb;
    }

    public static <T> Callback<List<T>> getCallbackList(List<T> t) {
        Callback<List<T>> cb = new Callback<List<T>>() {
            @Override
            public void success(List<T> o, Response response) {
                System.out.println("Object " + o);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                System.out.println("Error: " + retrofitError);
            }
        };

        return cb;
    }

    public static synchronized void setInstanceUrl(String url) {
        mMifosInstanceUrl = url;
        init();
    }

    public static synchronized String getInstanceUrl() {
        return mMifosInstanceUrl;
    }

    /**
     * Method to chose which instance's API you wish to access. Choices are:
     * 1 = mifos instance URL
     * 2 = PGS Instance URL
     * @param choice
     */
    public static synchronized void chooseInstanceUrl(int choice) {
        if (choice == 1) {
            setInstanceUrl(mMifosInstanceUrl);
        } else if (choice == 2) {
            setInstanceUrl(mPGSInstanceUrl);
        }
    }
}