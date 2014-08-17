package com.mifos.mifosxdroid.online;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.mifos.mifosxdroid.R;
import com.mifos.mifosxdroid.fragments.AgentAccountDetailsFragment;
import com.mifos.mifosxdroid.fragments.AgentDetailsFragment;
import com.mifos.mifosxdroid.fragments.ClientSearchFragment;
import com.mifos.utils.FragmentConstants;

import butterknife.ButterKnife;


public class ClientSearchActivity extends ActionBarActivity implements AgentDetailsFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_search);
        ButterKnife.inject(this);
        FragmentTransaction fragmentTransaction =  getSupportFragmentManager().beginTransaction();
        ClientSearchFragment clientSearchFragment = new ClientSearchFragment();
        fragmentTransaction.replace(R.id.search_activity_container,clientSearchFragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.client_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void loadSavingsAccountSummary(int savingsAccountNumber) {

        AgentAccountDetailsFragment agentAccountDetailsFragment
                = AgentAccountDetailsFragment.newInstance(savingsAccountNumber);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.addToBackStack(FragmentConstants.FRAG_CLIENT_DETAILS);
        fragmentTransaction.replace(R.id.global_container, agentAccountDetailsFragment).commit();

    }

}
