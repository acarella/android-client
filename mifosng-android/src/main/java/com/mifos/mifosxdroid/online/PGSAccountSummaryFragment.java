package com.mifos.mifosxdroid.online;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.QuickContactBadge;
import android.widget.TextView;
import android.widget.Toast;

import com.mifos.mifosxdroid.R;
import com.mifos.mifosxdroid.adapters.PGSAccountTransactionsListAdapter;
import com.mifos.mifosxdroid.adapters.SavingsAccountTransactionsListAdapter;
import com.mifos.objects.accounts.savings.SavingsAccount;
import com.mifos.objects.accounts.savings.SavingsAccountWithAssociations;
import com.mifos.objects.accounts.savings.Transaction;
import com.mifos.services.API;
import com.mifos.utils.Constants;
import com.mifos.utils.SafeUIBlockingUtility;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by antoniocarella on 5/30/14.
 * Uses code from SavingsAccountSummaryFragment
 */
public class PGSAccountSummaryFragment extends Fragment{

    private OnFragmentInteractionListener mListener;

    private PGSAccountTransactionsListAdapter pgsAccountTransactionsListAdapter;

    private SavingsAccountWithAssociations pgsAccount;

    int savingsAccountNumber;

    View rootView;

    SafeUIBlockingUtility safeUIBlockingUtility;

    ActionBarActivity activity;

    SharedPreferences sharedPreferences;

    ActionBar actionBar;

    Boolean areOnlyDepositsShowing = false;
    List<Transaction> pgsTransactionsList = new ArrayList<Transaction>();

    @InjectView(R.id.tv_clientName)TextView tv_clientName;
    @InjectView(R.id.quickContactBadge_client) QuickContactBadge quickContactBadge;
    @InjectView(R.id.tv_savings_product_short_name) TextView tv_savingsProductName;
    @InjectView(R.id.tv_savingsAccountNumber) TextView tv_savingsAccountNumber;
    @InjectView(R.id.tv_savings_account_balance) TextView tv_savingsAccountBalance;
    @InjectView(R.id.tv_total_deposits) TextView tv_totalDeposits;
    @InjectView(R.id.tv_total_withdrawals) TextView tv_totalWithdrawals;
    @InjectView(R.id.lv_recent_pgs_transactions) ListView lv_recentPGSTransactions;
    @InjectView(R.id.bt_pgs_hide_withdrawal) Button bt_hideWithdrawal;
    @InjectView(R.id.bt_pgs_make_deposit) Button bt_makeDeposit;

    public static PGSAccountSummaryFragment newInstance(int savingsAccountNumber) {
        PGSAccountSummaryFragment fragment = new PGSAccountSummaryFragment();
        Bundle args = new Bundle();
        args.putInt(Constants.SAVINGS_ACCOUNT_NUMBER, savingsAccountNumber);
        fragment.setArguments(args);
        return fragment;
    }
    public PGSAccountSummaryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            savingsAccountNumber = getArguments().getInt(Constants.SAVINGS_ACCOUNT_NUMBER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_pgs_account_summary, container, false);
        activity = (ActionBarActivity) getActivity();
        safeUIBlockingUtility = new SafeUIBlockingUtility(PGSAccountSummaryFragment.this.getActivity());
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        actionBar = activity.getSupportActionBar();
        ButterKnife.inject(this, rootView);

        inflateSavingsAccountSummary();

        bt_hideWithdrawal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!areOnlyDepositsShowing) {
                    bt_hideWithdrawal.setText("Show all");
                    areOnlyDepositsShowing = true;
                    pgsAccountTransactionsListAdapter.getFilter().filter("Deposit");
                } else {
                    bt_hideWithdrawal.setText("Show Deposits Only");
                    areOnlyDepositsShowing = false;

                    pgsAccountTransactionsListAdapter = new PGSAccountTransactionsListAdapter(getActivity().getApplicationContext(),
                            pgsTransactionsList);
                    lv_recentPGSTransactions.setAdapter(pgsAccountTransactionsListAdapter);
                }
            }
        });

        return rootView;
    }

    public void inflateSavingsAccountSummary(){

        safeUIBlockingUtility.safelyBlockUI();

        actionBar.setTitle(getResources().getString(R.string.pgsAccountSummary));
        /**
         * This Method will hits end point ?associations=transactions
         */
        API.savingsAccountService.getSavingsAccountWithAssociations(savingsAccountNumber,
                "transactions", new Callback<SavingsAccountWithAssociations>() {
                    @Override
                    public void success(SavingsAccountWithAssociations pgsAccountWithAssociations, Response response) {

                        if(pgsAccountWithAssociations!=null) {

                            pgsAccount = pgsAccountWithAssociations;

                            tv_clientName.setText(pgsAccountWithAssociations.getClientName());
                            tv_savingsProductName.setText(pgsAccountWithAssociations.getSavingsProductName());
                            tv_savingsAccountNumber.setText(pgsAccountWithAssociations.getAccountNo());
                            tv_savingsAccountBalance.setText(String.valueOf(pgsAccountWithAssociations.getSummary().getAccountBalance()));
                            tv_totalDeposits.setText(String.valueOf(pgsAccountWithAssociations.getSummary().getTotalDeposits()));
                            tv_totalWithdrawals.setText(String.valueOf(pgsAccountWithAssociations.getSummary().getTotalWithdrawals()));

                            //Storing this in order to load it when user wishes to see complete list
                            // of transactions after having filtered it.
                            pgsTransactionsList = pgsAccountWithAssociations.getTransactions();

                            pgsAccountTransactionsListAdapter
                                    = new PGSAccountTransactionsListAdapter(getActivity().getApplicationContext(),
                                    pgsAccountWithAssociations.getTransactions());
                            lv_recentPGSTransactions.setAdapter(pgsAccountTransactionsListAdapter);

                            bt_makeDeposit.setEnabled(true);

                            safeUIBlockingUtility.safelyUnBlockUI();

                        }
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {

                        Log.i(getActivity().getLocalClassName(), retrofitError.getLocalizedMessage());

                        Toast.makeText(activity, "PayGoSol Account not found.", Toast.LENGTH_SHORT).show();
                        safeUIBlockingUtility.safelyUnBlockUI();
                    }
                });
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @OnClick(R.id.bt_pgs_make_deposit)
    public void makeDepositButtonClicked(){
        mListener.makeDeposit(pgsAccount);
    }

    public interface OnFragmentInteractionListener {

        public void makeDeposit(SavingsAccountWithAssociations savingsAccountWithAssociations);
    }

}

