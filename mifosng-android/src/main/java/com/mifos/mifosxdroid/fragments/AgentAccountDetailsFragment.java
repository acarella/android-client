package com.mifos.mifosxdroid.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.QuickContactBadge;
import android.widget.TextView;
import android.widget.Toast;

import com.mifos.mifosxdroid.R;
import com.mifos.mifosxdroid.adapters.SavingsAccountTransactionsListAdapter;
import com.mifos.objects.accounts.savings.SavingsAccountWithAssociations;
import com.mifos.services.API;
import com.mifos.utils.Constants;
import com.mifos.utils.SafeUIBlockingUtility;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class AgentAccountDetailsFragment extends Fragment {
    public final static String TAG = AgentAccountDetailsFragment.class.getSimpleName();
    @InjectView(R.id.tv_clientName) TextView tv_clientName;
    @InjectView(R.id.quickContactBadge_client) QuickContactBadge quickContactBadge;
    @InjectView(R.id.tv_savings_product_short_name) TextView tv_savingsProductName;
    @InjectView(R.id.tv_savingsAccountNumber) TextView tv_savingsAccountNumber;
    @InjectView(R.id.tv_savings_account_balance) TextView tv_savingsAccountBalance;
    @InjectView(R.id.tv_total_deposits) TextView tv_totalDeposits;
    @InjectView(R.id.tv_total_withdrawals) TextView tv_totalWithdrawals;
    @InjectView(R.id.lv_last_five_savings_transactions) ListView lv_lastFiveTransactions;
    @InjectView(R.id.bt_transfer) Button bt_transfer;


    private OnFragmentInteractionListener mListener;

    int savingsAccountNumber;

    /**
     * Static Variables for Inflation of Menu and Submenus
     */

    View rootView;

    SafeUIBlockingUtility safeUIBlockingUtility;

    ActionBarActivity activity;

    SharedPreferences sharedPreferences;

    ActionBar actionBar;

    SavingsAccountWithAssociations savingsAccountWithAssociations;
    public static AgentAccountDetailsFragment newInstance(int savingsAccountNumber) {
        AgentAccountDetailsFragment fragment = new AgentAccountDetailsFragment();
        Bundle args = new Bundle();
        args.putInt(Constants.SAVINGS_ACCOUNT_NUMBER, savingsAccountNumber);
        fragment.setArguments(args);
        return fragment;
    }
    public AgentAccountDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            savingsAccountNumber = getArguments().getInt(Constants.SAVINGS_ACCOUNT_NUMBER);
        }

        Log.d(TAG, "onCreate()");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_agent_account_summary, container, false);
        activity = (ActionBarActivity) getActivity();
        safeUIBlockingUtility = new SafeUIBlockingUtility(AgentAccountDetailsFragment.this.getActivity());
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        actionBar = activity.getSupportActionBar();
        ButterKnife.inject(this, rootView);

        inflateSavingsAccountSummary();
        setHasOptionsMenu(true);
        return rootView;
    }

    public void inflateSavingsAccountSummary(){

        safeUIBlockingUtility.safelyBlockUI("Retrieving account information.","Please wait");

        actionBar.setTitle(getResources().getString(R.string.accountSummary));
        /**
         * This Method will hit end point ?associations=transactions
         */
        API.savingsAccountService.getSavingsAccountWithAssociations(savingsAccountNumber,
                "transactions", new Callback<SavingsAccountWithAssociations>() {
                    @Override
                    public void success(SavingsAccountWithAssociations savingsAccountWithAssociations, Response response) {

                        if(savingsAccountWithAssociations!=null) {

                            AgentAccountDetailsFragment.this.savingsAccountWithAssociations = savingsAccountWithAssociations;

                            tv_clientName.setText(savingsAccountWithAssociations.getClientName());
                            tv_savingsProductName.setText(savingsAccountWithAssociations.getSavingsProductName());
                            tv_savingsAccountNumber.setText(savingsAccountWithAssociations.getAccountNo());
                            tv_savingsAccountBalance.setText(String.valueOf(savingsAccountWithAssociations.getSummary().getAccountBalance()));
                            tv_totalDeposits.setText(String.valueOf(savingsAccountWithAssociations.getSummary().getTotalDeposits()));
                            tv_totalWithdrawals.setText(String.valueOf(savingsAccountWithAssociations.getSummary().getTotalWithdrawals()));

                            SavingsAccountTransactionsListAdapter savingsAccountTransactionsListAdapter
                                    = new SavingsAccountTransactionsListAdapter(getActivity().getApplicationContext(), savingsAccountWithAssociations.getTransactions());
                            lv_lastFiveTransactions.setAdapter(savingsAccountTransactionsListAdapter);

                            safeUIBlockingUtility.safelyUnBlockUI();

                        }
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {

                        Log.i(getActivity().getLocalClassName(), retrofitError.getLocalizedMessage());

                        Toast.makeText(activity, "Savings Account not found.", Toast.LENGTH_SHORT).show();
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


    @OnClick(R.id.bt_transfer)
    public void onWithdrawalButtonClicked() {
        mListener.makeTransfer(savingsAccountWithAssociations, Constants.SAVINGS_ACCOUNT_TRANSACTION_WITHDRAWAL);
    }

    public interface OnFragmentInteractionListener {
        public void makeTransfer(SavingsAccountWithAssociations savingsAccountWithAssociations1, String transactionType);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.transfer, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.transfer:
                mListener.makeTransfer(savingsAccountWithAssociations, Constants.SAVINGS_ACCOUNT_TRANSACTION_TRANSFER);
                return true;
            default: //DO NOTHING
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
