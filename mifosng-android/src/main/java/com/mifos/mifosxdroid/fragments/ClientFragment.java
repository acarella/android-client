package com.mifos.mifosxdroid.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.mifos.mifosxdroid.LoanActivity;
import com.mifos.mifosxdroid.R;
import com.mifos.mifosxdroid.adapters.ClientListAdapter;
import com.mifos.objects.db.Client;
import com.mifos.objects.db.Loan;
import com.mifos.objects.db.RepaymentTransaction;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.util.*;


public class ClientFragment extends Fragment implements AdapterView.OnItemClickListener {

    @InjectView(R.id.lv_clients)
    ListView lv_clients;

    private ClientListAdapter adapter = null;
    private long groupId;
    private List<Client> clientsInTheGroup = new ArrayList<Client>();
    final private String tag = getClass().getSimpleName();

    @InjectView(R.id.tv_total_amt_paid)
    TextView tv_total_amt_paid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_offline_client, null);
        ButterKnife.inject(this, view);
        setHasOptionsMenu(true);
        groupId = getArguments().getLong("group_id", 0);
        setAdapter();
        calculateTotalDueAmount();
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_client, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            hideKeyboard();
            updateTotalDue();
            saveUpdatedLoanToDB();
        }
        return super.onOptionsItemSelected(item);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
    }

    private void saveUpdatedLoanToDB() {
        Map<Loan, Integer> mapDue = adapter.getUpdatedDueList();

        Set<Loan> loans = mapDue.keySet();
        for(Loan loan : loans) {
            int updatedDue = mapDue.get(loan);
            new RepaymentTransaction(loan, updatedDue).save();
        }
    }

    private void updateTotalDue() {
        Map<Loan, Integer> mapDue = adapter.getUpdatedDueList();

        int totalAmountDue = 0;

        Set<Loan> loans = mapDue.keySet();

        for (Loan loan : loans) {
            totalAmountDue += mapDue.get(loan);
        }

        tv_total_amt_paid.setText(String.valueOf(totalAmountDue));
    }

    private void setAdapter() {
        clientsInTheGroup = Select.from(Client.class).where(Condition.prop("mifos_group").eq(groupId)).list();
        if (adapter == null)
            adapter = new ClientListAdapter(getActivity(), clientsInTheGroup);
        lv_clients.setAdapter(adapter);
        lv_clients.setOnItemClickListener(this);
    }

    private void calculateTotalDueAmount() {
        int totalAmountDue = 0;

        final Map<Loan, Integer> listPaidAmount = new LinkedHashMap<Loan, Integer>();

        List<Loan> loans = Select.from(Loan.class).list();
        for (Loan loan : loans) {
            if (loan.getClient().getMifosGroup().getId() == groupId) {
                listPaidAmount.put(loan, loan.chargesDue);
                totalAmountDue += loan.chargesDue;
            }
        }
        tv_total_amt_paid.setText(String.valueOf(totalAmountDue));
        adapter.setPaidAmount(listPaidAmount);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        final int clientId = clientsInTheGroup.get(i).getClientId();
        Log.i(tag, "onItemClick:-clientId:" + clientId);

        Intent intent = new Intent(getActivity(), LoanActivity.class);
        intent.putExtra("clientId", clientId);
        startActivity(intent);
    }
}