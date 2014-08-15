package com.mifos.mifosxdroid.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.mifos.mifosxdroid.R;
import com.mifos.mifosxdroid.adapters.ClientListAdapter;
import com.mifos.objects.db.Client;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;


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
        }
        return super.onOptionsItemSelected(item);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
    }

    private void setAdapter() {
        clientsInTheGroup = Select.from(Client.class).where(Condition.prop("mifos_group").eq(groupId)).list();
        if (adapter == null)
            adapter = new ClientListAdapter(getActivity(), clientsInTheGroup);
        lv_clients.setAdapter(adapter);
        lv_clients.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }
}