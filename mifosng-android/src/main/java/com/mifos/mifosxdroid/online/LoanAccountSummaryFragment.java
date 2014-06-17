package com.mifos.mifosxdroid.online;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.QuickContactBadge;
import android.widget.TextView;
import android.widget.Toast;

import com.mifos.mifosxdroid.R;
import com.mifos.objects.accounts.loan.Loan;
import com.mifos.services.API;
import com.mifos.utils.Constants;
import com.mifos.utils.SafeUIBlockingUtility;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by ishankhanna on 09/05/14.
 */
public class LoanAccountSummaryFragment extends Fragment {

    /*
        Set of Actions and Transactions that can be performed depending on the status of the Loan
        Actions are performed to change the status of the loan
        Transactions are performed to do repayments
     */
    private static final int ACTION_NOT_SET = -1;
    private static final int ACTION_APPROVE_LOAN = 0;
    private static final int ACTION_DISBURSE_LOAN = 1;
    private static final int TRANSACTION_REPAYMENT = 2;

    // Action Identifier in the onProcessTransactionClicked Method
    private int processLoanTransactionAction = -1;

    private OnFragmentInteractionListener mListener;

    int loanAccountNumber;

    View rootView;

    SafeUIBlockingUtility safeUIBlockingUtility;

    ActionBarActivity activity;

    SharedPreferences sharedPreferences;

    ActionBar actionBar;

    @InjectView(R.id.tv_clientName)
    TextView tv_clientName;
    @InjectView(R.id.quickContactBadge_client)
    QuickContactBadge quickContactBadge;
    @InjectView(R.id.tv_loan_product_short_name)
    TextView tv_loan_product_short_name;
    @InjectView(R.id.tv_loanAccountNumber)
    TextView tv_loanAccountNumber;
    @InjectView(R.id.tv_loan_total_due)
    TextView tv_loan_total_due;
    @InjectView(R.id.tv_loan_account_status)
    TextView tv_loan_account_status;
    @InjectView(R.id.tv_in_arrears)
    TextView tv_in_arrears;
    @InjectView(R.id.tv_loan_officer)
    TextView tv_loan_officer;
    @InjectView(R.id.tv_principal)
    TextView tv_principal;
    @InjectView(R.id.tv_loan_principal_due)
    TextView tv_loan_principal_due;
    @InjectView(R.id.tv_loan_principal_paid)
    TextView tv_loan_principal_paid;
    @InjectView(R.id.tv_interest)
    TextView tv_interest;
    @InjectView(R.id.tv_loan_interest_due)
    TextView tv_loan_interest_due;
    @InjectView(R.id.tv_loan_interest_paid)
    TextView tv_loan_interest_paid;
    @InjectView(R.id.tv_fees)
    TextView tv_fees;
    @InjectView(R.id.tv_loan_fees_due)
    TextView tv_loan_fees_due;
    @InjectView(R.id.tv_loan_fees_paid)
    TextView tv_loan_fees_paid;
    @InjectView(R.id.tv_penalty)
    TextView tv_penalty;
    @InjectView(R.id.tv_loan_penalty_due)
    TextView tv_loan_penalty_due;
    @InjectView(R.id.tv_loan_penalty_paid)
    TextView tv_loan_penalty_paid;
    @InjectView(R.id.tv_total)
    TextView tv_total;
    @InjectView(R.id.tv_total_due)
    TextView tv_total_due;
    @InjectView(R.id.tv_total_paid)
    TextView tv_total_paid;
    @InjectView(R.id.bt_processLoanTransaction)
    Button bt_processLoanTransaction;

    private Loan clientLoan;

    public static LoanAccountSummaryFragment newInstance(int loanAccountNumber) {
        LoanAccountSummaryFragment fragment = new LoanAccountSummaryFragment();
        Bundle args = new Bundle();
        args.putInt(Constants.LOAN_ACCOUNT_NUMBER, loanAccountNumber);
        fragment.setArguments(args);
        return fragment;
    }
    public LoanAccountSummaryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            loanAccountNumber = getArguments().getInt(Constants.LOAN_ACCOUNT_NUMBER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        rootView = inflater.inflate(R.layout.fragment_loan_account_summary, container, false);
        activity = (ActionBarActivity) getActivity();
        safeUIBlockingUtility = new SafeUIBlockingUtility(LoanAccountSummaryFragment.this.getActivity());
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        actionBar = activity.getSupportActionBar();
        ButterKnife.inject(this, rootView);

        inflateLoanAccountSummary();

        return rootView;
    }

    private void inflateLoanAccountSummary(){

        safeUIBlockingUtility.safelyBlockUI();

        actionBar.setTitle(getResources().getString(R.string.loanAccountSummary));

        //TODO Implement cases to enable/disable repayment button
        bt_processLoanTransaction.setEnabled(false);

        API.loanService.getLoanById(loanAccountNumber, new Callback<Loan>() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void success(Loan loan, Response response) {
                clientLoan = loan;
                tv_clientName.setText(loan.getClientName());
                tv_loan_product_short_name.setText(loan.getLoanProductName());
                tv_loanAccountNumber.setText("#"+loan.getAccountNo());
                tv_loan_total_due.setText("-"+loan.getSummary().getPrincipalDisbursed());
                tv_loan_account_status.setText(loan.getStatus().getValue());
                tv_in_arrears.setText(String.valueOf(loan.getSummary().getTotalOverdue()));
                tv_loan_officer.setText(loan.getLoanOfficerName());
                //TODO Implement QuickContactBadge
                quickContactBadge.setImageToDefault();

                tv_principal.setText(String.valueOf(loan.getSummary().getPrincipalDisbursed()));
                tv_loan_principal_due.setText(String.valueOf(loan.getSummary().getPrincipalOutstanding()));
                tv_loan_principal_paid.setText(String.valueOf(loan.getSummary().getPrincipalPaid()));

                tv_interest.setText(String.valueOf(loan.getSummary().getInterestCharged()));
                tv_loan_interest_due.setText(String.valueOf(loan.getSummary().getInterestOutstanding()));
                tv_loan_interest_paid.setText(String.valueOf(loan.getSummary().getInterestPaid()));

                tv_fees.setText(String.valueOf(loan.getSummary().getFeeChargesCharged()));
                tv_loan_fees_due.setText(String.valueOf(loan.getSummary().getFeeChargesOutstanding()));
                tv_loan_fees_paid.setText(String.valueOf(loan.getSummary().getFeeChargesPaid()));

                tv_penalty.setText(String.valueOf(loan.getSummary().getPenaltyChargesCharged()));
                tv_loan_penalty_due.setText(String.valueOf(loan.getSummary().getPenaltyChargesOutstanding()));
                tv_loan_penalty_paid.setText(String.valueOf(loan.getSummary().getPenaltyChargesPaid()));

                tv_total.setText(String.valueOf(loan.getSummary().getTotalExpectedRepayment()));
                tv_total_due.setText(String.valueOf(loan.getSummary().getTotalOutstanding()));
                tv_total_paid.setText(String.valueOf(loan.getSummary().getTotalRepayment()));

                bt_processLoanTransaction.setEnabled(true);
                if(loan.getStatus().getActive())
                {
                    /*
                     *   if Loan is already active
                     *   the Transaction Would be Make Repayment
                     */
                    bt_processLoanTransaction.setText("Make Repayment");
                    processLoanTransactionAction = TRANSACTION_REPAYMENT;

                }else if(loan.getStatus().getPendingApproval()) {

                    /*
                     *  if Loan is Pending for Approval
                     *  the Action would be Approve Loan
                     */
                    bt_processLoanTransaction.setText("Approve Loan");
                    processLoanTransactionAction = ACTION_APPROVE_LOAN;
                }else if(loan.getStatus().getWaitingForDisbursal()) {
                    /*
                     *  if Loan is Waiting for Disbursal
                     *  the Action would be Disburse Loan
                     */
                    bt_processLoanTransaction.setText("Disburse Loan");
                    processLoanTransactionAction = ACTION_DISBURSE_LOAN;
                }else if(loan.getStatus().getClosedObligationsMet()){
                    //TODO Ask Vishwas about this status and getClosed Status' difference and what action to perform
                    /*
                     *  if Loan is Closed after the obligations are met
                     *  the make payment will be disabled so that no more payment can be collected
                     */
                    bt_processLoanTransaction.setEnabled(false);
                    bt_processLoanTransaction.setText("Make Repayment");
                }else {

                    //TODO Implement Actions for Other Status' as well
                    bt_processLoanTransaction.setEnabled(false);
                    bt_processLoanTransaction.setText("Make Repayment");
                }


                safeUIBlockingUtility.safelyUnBlockUI();
            }

            @Override
            public void failure(RetrofitError retrofitError) {

                Toast.makeText(activity, "Loan Account not found.", Toast.LENGTH_SHORT).show();
                safeUIBlockingUtility.safelyUnBlockUI();
            }
        });

    }

    @OnClick(R.id.bt_processLoanTransaction)
    public void onProcessTransactionClicked(){


        if(processLoanTransactionAction == TRANSACTION_REPAYMENT)
        {
            mListener.makeRepayment(clientLoan);
        }else if(processLoanTransactionAction == ACTION_APPROVE_LOAN) {
            //TODO mListener.approveLoan()
        }else if (processLoanTransactionAction == ACTION_DISBURSE_LOAN) {
            //TODO mListener.disburseLoan()
        }else {
            Log.i(getActivity().getLocalClassName(), "TRANSACTION ACTION NOT SET");
        }

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


    public interface OnFragmentInteractionListener {

        public void makeRepayment(Loan loan);
    }

}
