package com.mifos.mifosxdroid.online;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.jakewharton.fliptables.FlipTable;
import com.mifos.mifosxdroid.R;
import com.mifos.objects.accounts.savings.SavingsAccount;
import com.mifos.objects.accounts.savings.SavingsAccountWithAssociations;
import com.mifos.objects.accounts.savings.SavingsDepositRequest;
import com.mifos.objects.accounts.savings.SavingsDepositResponse;
import com.mifos.services.API;
import com.mifos.utils.Constants;
import com.mifos.utils.SafeUIBlockingUtility;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by antoniocarella on 5/30/14.
 */
public class PGSPaymentFragment extends Fragment{

//TODO Test and Remove Amount Due and Fees Due from Instance Method (Don't Pass'em as arguments)

    View rootView;

    SafeUIBlockingUtility safeUIBlockingUtility;

    ActionBarActivity activity;

    SharedPreferences sharedPreferences;

    ActionBar actionBar;

    // Arguments Passed From the Loan Account Summary Fragment
    String clientName;
    String pgsAccountNumber;

    @InjectView(R.id.tv_clientName) TextView tv_clientName;
    @InjectView(R.id.et_pgs_payment_date) EditText et_paymentDate;
    @InjectView(R.id.et_pgs_payment_time) EditText et_paymentTime;
    @InjectView(R.id.et_pgs_payment_amount) EditText et_amount;
    @InjectView(R.id.bt_paynow) Button bt_paynow;
    @InjectView(R.id.bt_cancelPayment) Button bt_cancel;

    private OnFragmentInteractionListener mListener;

    public PGSPaymentFragment() {
        // Required empty public constructor
    }

    public static PGSPaymentFragment newInstance(SavingsAccountWithAssociations pgsAccount) {
        PGSPaymentFragment fragment = new PGSPaymentFragment();
        Bundle args = new Bundle();
        if(pgsAccount != null)
        {
            args.putString(Constants.CLIENT_NAME, pgsAccount.getClientName());
            args.putString(Constants.SAVINGS_PRODUCT_NAME, pgsAccount.getSavingsProductName());
            args.putString(Constants.SAVINGS_ACCOUNT_NUMBER, pgsAccount.getAccountNo());
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

            clientName = getArguments().getString(Constants.CLIENT_NAME);
            pgsAccountNumber = getArguments().getString(Constants.SAVINGS_ACCOUNT_NUMBER);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        rootView = inflater.inflate(R.layout.pgs_payment, container, false);
        activity = (ActionBarActivity) getActivity();
        safeUIBlockingUtility = new SafeUIBlockingUtility(PGSPaymentFragment.this.getActivity());
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        actionBar = activity.getSupportActionBar();
        actionBar.setTitle("PayGoSol Payment");
        ButterKnife.inject(this, rootView);

        inflateUI();

        return rootView;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void inflateUI(){
        tv_clientName.setText(clientName);
        et_amount.setText("0.0");
        inflatePaymentDate();
    }

    public interface OnFragmentInteractionListener {

    }

    public void inflatePaymentDate(){

        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        et_paymentDate.setText(new StringBuilder().append(day)
                .append(" - ").append(month).append(" - ").append(year));

        /*
            TODO Add Validation to make sure :
            1. Date Is in Correct Format
            2. Date Entered is not greater than Date Today i.e Date is not in future
         */

    }

    @OnClick(R.id.bt_paynow)
    public void reviewPaymentDetails(){

        String[] headers = {"Field", "Value"};
        String[][] data = {
                {"Payment Date", et_paymentDate.getText().toString()},
                {"Payment Time", et_paymentTime.getText().toString()},
                {"Amount", et_amount.getText().toString()}
        };

        System.out.println(FlipTable.of(headers, data));

        String formReviewString = new StringBuilder().append(data[0][0] + " : " + data[0][1])
                .append("\n")
                .append(data[1][0] + " : " + data[1][1])
                .append("\n")
                .append(data[2][0] + " : " + data[2][1]).toString();


        AlertDialog confirmPaymentDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Confirm Payment?")
                .setMessage(formReviewString)
                .setPositiveButton("Pay Now", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        submitPayment();
                    }
                })
                .setNegativeButton("Back", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();

    }

    @OnClick(R.id.bt_cancelPayment)
    public void cancelPayment(){
        getActivity().getSupportFragmentManager().popBackStackImmediate();
    }

    public void submitPayment(){
        //TODO Implement a proper builder method here

        String dateString = et_paymentDate.getEditableText().toString().replace(" - ", " ");

        final SavingsDepositRequest pgsPaymentRequest = new SavingsDepositRequest();
        //TODO Decide if agents will handle different payment methods and if so, handle them correctly
        pgsPaymentRequest.setAccountNumber(pgsAccountNumber);
        pgsPaymentRequest.setPaymentTypeId("1");
        pgsPaymentRequest.setLocale("en");
        pgsPaymentRequest.setTransactionAmount(et_amount.getText().toString());
        pgsPaymentRequest.setDateFormat("dd MM yyyy");
        pgsPaymentRequest.setTransactionDate(dateString);
        String builtRequest = new Gson().toJson(pgsPaymentRequest);
        Log.i("TAG", builtRequest);

        safeUIBlockingUtility.safelyBlockUI();
        API.savingsAccountService.submitDeposit(Integer.parseInt(pgsAccountNumber),
                pgsPaymentRequest,
                new Callback<SavingsDepositResponse>() {
                    @Override
                    public void success(SavingsDepositResponse pgsPaymentResponse, Response response) {

                        if (pgsPaymentResponse != null) {
                            Toast.makeText(getActivity(), "Payment Successful, Transaction ID = " + pgsPaymentResponse.getResourceId(),
                                    Toast.LENGTH_LONG).show();
                        }
                        safeUIBlockingUtility.safelyUnBlockUI();
                        getActivity().getSupportFragmentManager().popBackStackImmediate();
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        Toast.makeText(getActivity(), "Payment Failed", Toast.LENGTH_SHORT).show();
                        safeUIBlockingUtility.safelyUnBlockUI();
                    }
                }
        );
    }
}

