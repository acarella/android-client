package com.mifos.mifosxdroid.online;

/**
 * Created by antoniocarella on 5/30/14.
 * Uses code from ClientActivity.java
 */
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.mifos.mifosxdroid.R;
import com.mifos.objects.accounts.savings.SavingsAccountWithAssociations;
import com.mifos.utils.Constants;
import com.mifos.utils.FragmentConstants;

import butterknife.ButterKnife;

public class PGSClientActivity extends ActionBarActivity implements
        PGSAccountSummaryFragment.OnFragmentInteractionListener  {

    //TODO Hardcoding this for now, need to change when goes to production
    private int agentId = 1223;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global_container_layout);
        ButterKnife.inject(this);
        final int clientId = getIntent().getExtras().getInt(Constants.CLIENT_ID);
        final int pgsAccountNumber = getIntent().getExtras().getInt(Constants.PGS_ACCOUNT_NUMBER);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        PGSAccountSummaryFragment pgsAccountSummaryFragment = PGSAccountSummaryFragment.newInstance(clientId, pgsAccountNumber);
        fragmentTransaction.replace(R.id.global_container, pgsAccountSummaryFragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.client, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.


        switch (item.getItemId()) {
            case R.id.agent_account:
                Intent intent = new Intent(PGSClientActivity.this, PGSClientActivity.class);
                intent.putExtra(Constants.CLIENT_ID, agentId);
                intent.putExtra(Constants.PGS_ACCOUNT_NUMBER, 357);
                startActivity(intent);
                break;
            case R.id.mItem_search:
                startActivity(new Intent(PGSClientActivity.this, ClientSearchActivity.class));
                break;
            case R.id.logout:
                startActivity(new Intent(PGSClientActivity.this, LogoutActivity.class));
                break;
            default: //DO NOTHING
                break;
        }

        return super.onOptionsItemSelected(item);

    }

    public void makeDeposit(SavingsAccountWithAssociations savingsAccountWithAssociations) {
        PGSPaymentFragment pgsPaymentFragment = PGSPaymentFragment.newInstance(savingsAccountWithAssociations);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.addToBackStack(FragmentConstants.FRAG_PGS_ACCOUNT_SUMMARY);
        fragmentTransaction.replace(R.id.global_container, pgsPaymentFragment).commit();
    }




}
