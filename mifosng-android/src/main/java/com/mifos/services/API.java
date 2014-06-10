package com.mifos.services;

import android.util.Log;
import com.mifos.objects.SearchedEntity;
import com.mifos.objects.User;
import com.mifos.objects.accountTransfer.AccountTransferRequest;
import com.mifos.objects.accountTransfer.AccountTransferResponse;
import com.mifos.objects.accountTransfer.AccountTransferTemplateRequest;
import com.mifos.objects.accountTransfer.AccountTransferTemplateResponse;
import com.mifos.objects.accounts.ClientAccounts;
import com.mifos.objects.accounts.loan.LoanRepaymentRequest;
import com.mifos.objects.accounts.loan.LoanRepaymentResponse;
import com.mifos.objects.accounts.savings.SavingsAccount;
import com.mifos.objects.accounts.savings.SavingsAccountWithAssociations;
import com.mifos.objects.accounts.savings.SavingsDepositRequest;
import com.mifos.objects.accounts.savings.SavingsDepositResponse;
import com.mifos.objects.client.Client;
import com.mifos.objects.client.Page;
import com.mifos.objects.db.CollectionSheet;
import com.mifos.objects.templates.loans.LoanRepaymentTemplate;
import com.mifos.services.data.Payload;
import com.mifos.services.data.CollectionSheetPayload;
import com.mifos.services.data.SaveResponse;
import retrofit.*;
import retrofit.client.Response;
import retrofit.http.*;

import java.util.Iterator;
import java.util.List;

public class API {

    //This instance has more Data for Testing
    public static String url = "https://demo.openmf.org/mifosng-provider/api/v1";

    //public static String url = "https://demo2.openmf.org/mifosng-provider/api/v1";
    static RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(url)
            .setRequestInterceptor(new RequestInterceptor() {
                @Override
                public void intercept(RequestFacade request) {
                    request.addHeader("Accept", "application/json");
                    request.addHeader("Content-Type", "application/json");
                    request.addHeader("X-Mifos-Platform-TenantId", "default");
                    request.addHeader("Authorization", "Basic bWlmb3M6cGFzc3dvcmQ=");
                }
            })
            .setErrorHandler(new MifosRestErrorHandler())
            .build();

    static {
        restAdapter.setLogLevel(RestAdapter.LogLevel.FULL);

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

        @GET("/centers")
        public void getAllCenters(Callback<List<com.mifos.objects.Center>> callback);
        @POST("/centers/2?command=generateCollectionSheet")
        public void getCenter(@Body Payload payload, Callback<CollectionSheet> callback);
        @POST("/centers/2?command=saveCollectionSheet")
        public SaveResponse saveCollectionSheet(@Body CollectionSheetPayload collectionSheetPayload);
    }


    public static CenterService centerService = restAdapter.create(CenterService.class);

    public interface ClientAccountsService {

        @GET("/clients/{clientId}/accounts")
        public void getAllAccountsOfClient(@Path("clientId") int clientId, Callback<ClientAccounts> callback);

        @GET("/clients/{clientId}/accounts")
        public void getClientsAccountsByType(@Path("clientId") int clientId, @Query("fields") String accountsType, Callback<ClientAccounts> callback);
    }

    public static ClientAccountsService clientAccountsService = restAdapter.create(ClientAccountsService.class);

    public interface ClientService {

        @GET("/clients")
        public void listAllClients(Callback<Page<Client>> callback);

        @GET("/clients/{clientId}")
        public void getClient(@Path("clientId") int clientId, Callback<Client> callback);

        @GET("/clients")
        public void listClientsFilteredByFirstName(@Query("firstName") String firstName, Callback<Page<Client>> callback);

    }

    public static ClientService clientService = restAdapter.create(ClientService.class);

    public interface SearchService {

        @GET("/search?resource=clients")
        public void searchClientsByName(@Query("query") String clientName, Callback<List<SearchedEntity>> callback);


    }

    public static LoanService loanService = restAdapter.create(LoanService.class);
    public interface LoanService {

        @GET("/loans/{loanId}")
        public void getLoanById(@Path("loanId") int loanId, Callback<com.mifos.objects.accounts.loan.Loan> callback);

        @GET("/loans/{loanId}/transactions/template?command=repayment")
        public void getLoanRepaymentTemplate(@Path("loanId") int loanId, Callback<LoanRepaymentTemplate> callback);

        @POST("/loans/{loanId}/transactions?command=repayment")
        public void submitPayment(@Path("loanId") int loanId,
                                  @Body LoanRepaymentRequest loanRepaymentRequest,
                                  Callback<LoanRepaymentResponse> loanRepaymentResponseCallback);

    }

    public static SavingsAccountService savingsAccountService = restAdapter.create(SavingsAccountService.class);
    public interface SavingsAccountService {

        /**
         *
         * @param savingsAccountId - savingsAccountId for which information is requested
         * @param association - Mention Type of Association Needed, Like :- all, transactions etc.
         * @param savingsAccountWithAssociationsCallback - callback to receive the response
         */
        @GET("/savingsaccounts/{savingsAccountId}")
        public void getSavingsAccountWithAssociations(@Path("savingsAccountId") int savingsAccountId,
                                                      @Query("associations") String association,
                                                      Callback<SavingsAccountWithAssociations> savingsAccountWithAssociationsCallback);

        @POST("/savingsaccounts/{accountsId}/transactions?command=deposit")
        public void submitDeposit(@Path("accountsId") int accountId,
                                  @Body SavingsDepositRequest SavingsDepositRequest,
                                  Callback<SavingsDepositResponse> SavingsDepositResponseCallback);

    }

    public static AccountTransfersService accountTransfersService = restAdapter.create(AccountTransfersService.class);

    public interface AccountTransfersService{

        @GET("/template")
        public void retrieveTemplate(@Body AccountTransferTemplateRequest accountTransferTemplateRequest,
                                     Callback<AccountTransferTemplateResponse> callback);

        @POST("/accounttransfers")
        public void createTransfer(@Body AccountTransferRequest accountTransferRequest,
                                   Callback<AccountTransferResponse> callback);

    }

    public static SearchService searchService = restAdapter.create(SearchService.class);

    public interface UserAuthService {

        @POST("/authentication")
        public void authenticate(@Query("username") String username, @Query("password") String password, Callback<User> userCallback);
    }

    public static UserAuthService userAuthService = restAdapter.create(UserAuthService.class);


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
}

