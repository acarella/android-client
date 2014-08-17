package com.mifos.mifosxdroid.online;

/**
 * Created by antoniocarella on 5/30/14.
 * Uses code from AgentActivity.java
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.mifos.mifosxdroid.R;
import com.mifos.mifosxdroid.fragments.AccountTransferFragment;
import com.mifos.mifosxdroid.fragments.ClientAccountDetailsFragment;
import com.mifos.mifosxdroid.fragments.ClientDetailsFragment;
import com.mifos.objects.accounts.savings.SavingsAccountWithAssociations;
import com.mifos.utils.Constants;
import com.mifos.utils.FragmentConstants;

import butterknife.ButterKnife;
<<<<<<< HEAD
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ClientActivity extends ActionBarActivity implements ClientDetailsFragment.OnFragmentInteractionListener,
                                                                 SavingsAccountSummaryFragment.OnFragmentInteractionListener,
                                                                 AccountTransferFragment.OnFragmentInteractionListener,
                                                                 GooglePlayServicesClient.ConnectionCallbacks,
                                                                 GooglePlayServicesClient.OnConnectionFailedListener {
    /**
     * Static Variables for Inflation of Menu and Submenus
     */

    private static final int MENU_ITEM_SAVE_LOCATION = 1000;
<<<<<<< HEAD
    private static final int MENU_ITEM_DATA_TABLES = 1001;
=======
    private static final int MENU_ITEM_NEW_CLIENT = 1001;
    private static final int MENU_ITEM_DATA_TABLES = 1002;
    private static final int MENU_ITEM_LOGOUT = 1003;
>>>>>>> demo

    /**
     * Control Menu Changes from Fragments
     * change this Variable to True in the Fragment and Magic
     * Happens in onPrepareOptionsMenu Method Below
     */
    public static Boolean didMenuDataChange = Boolean.FALSE;
    public static Boolean shouldAddDataTables = Boolean.FALSE;
=======
>>>>>>> demo

public class ClientActivity extends ActionBarActivity implements
        ClientDetailsFragment.OnFragmentInteractionListener,
        ClientAccountDetailsFragment.OnFragmentInteractionListener{

    public final static String TAG = ClientActivity.class.getSimpleName();
    //TODO Hardcoding this for now, need to change when goes to production
    private int agentId = 1223;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        setContentView(R.layout.activity_global_container_layout);
        ButterKnife.inject(this);
        final int clientId = getIntent().getExtras().getInt(Constants.CLIENT_ID);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        ClientDetailsFragment clientDetailsFragment = ClientDetailsFragment.newInstance(clientId, true);
        fragmentTransaction.replace(R.id.global_container, clientDetailsFragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.client, menu);
        return true;
    }

<<<<<<< HEAD
    /**
     *
     * This method is called EVERY TIME the menu button is pressed
     * on the action bar. So All dynamic changes in the menu are
     * done here.
     *
     * @param menu Current Menu in the Layout
     * @return true if the menu was successfully prepared
     */

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if(didMenuDataChange)
        {
            menu.clear();

            // This is a static menu item that will be added on the first position
            menu.add(Menu.NONE,MENU_ITEM_SAVE_LOCATION,Menu.NONE, "Save Location");

<<<<<<< HEAD
=======
            // This is a static menu item that will be added on the second position
            menu.add(Menu.NONE,MENU_ITEM_NEW_CLIENT,Menu.NONE, "New Client");

>>>>>>> demo
            // If the client request fetched data tables this will be true
            if(shouldAddDataTables)
            {
                // Just another check to make sure the clientDataTableMenuItems list is not empty
                if(clientDataTableMenuItems.size()>0)
                {
                    /*
                        Now that we have the list, lets add an Option for users to see the sub menu
                        of all data tables available
                     */
                    //TODO Make the name of this dynamic based on clients, loans and savings
                    menu.addSubMenu(Menu.NONE,MENU_ITEM_DATA_TABLES,Menu.NONE,"Additional Client Details");

                    // This is the ID of Each data table which will be used in onOptionsItemSelected Method
                    int SUBMENU_ITEM_ID = 0;

                    // Create a Sub Menu that holds a link to all data tables
<<<<<<< HEAD
                    SubMenu dataTableSubMenu = menu.getItem(1).getSubMenu();
=======
                    SubMenu dataTableSubMenu = menu.getItem(2).getSubMenu();
>>>>>>> demo
                    Iterator<String> stringIterator = clientDataTableMenuItems.iterator();
                    while(stringIterator.hasNext())
                    {
                        dataTableSubMenu.add(Menu.NONE,SUBMENU_ITEM_ID,Menu.NONE,stringIterator.next().toString());
                        SUBMENU_ITEM_ID++;
                    }
                }

                shouldAddDataTables = Boolean.FALSE;
            }
            didMenuDataChange = Boolean.FALSE;
<<<<<<< HEAD
=======

            // This is a static menu item that will be added on the second position
            menu.add(Menu.NONE,MENU_ITEM_LOGOUT,Menu.NONE, "LogOut");
>>>>>>> demo
        }

        return super.onPrepareOptionsMenu(menu);
    }


=======
>>>>>>> demo
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.


<<<<<<< HEAD
                            @Override
                            public void failure(RetrofitError retrofitError) {
                                Log.d("online/ClientActivity", "Failed to save GPS coordinates");
                            }
                        });
                ;
            } else {
                // Display the connection status
                Toast.makeText(this, "Location not available",
                        Toast.LENGTH_SHORT).show();
                Log.w(this.getLocalClassName(), "Location not available");
            }
            return true;
        }

<<<<<<< HEAD
=======
        if (id == MENU_ITEM_NEW_CLIENT){
            Intent intent = new Intent(ClientActivity.this, PGSAgentLoginActivity.class);
            startActivity(intent);
        }

        if (id == MENU_ITEM_LOGOUT){
            Intent intent = new Intent(ClientActivity.this, LogoutActivity.class);
            startActivity(intent);
        }

>>>>>>> demo
        if(id >= 0 && id < clientDataTableMenuItems.size())
        {
            loadDataTableFragment(id);
        }

        return super.onOptionsItemSelected(item);
    }

    public void loadDataTableFragment(int dataTablePostionInTheList) {
<<<<<<< HEAD

        //TODO Add a detailed implementation

        DataTableFragment dataTableFragment = DataTableFragment.newInstance(ClientDetailsFragment.clientDataTables.get(dataTablePostionInTheList));
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.addToBackStack(FragmentConstants.FRAG_CLIENT_DETAILS);
        fragmentTransaction.replace(R.id.global_container,dataTableFragment).commit();

    }


    /*
     * Called when a Loan Account is Selected
     * from the list of Loan Accounts on Client Details Fragment
     * It displays the summary of the Selected Loan Account
     */

    @Override
    public void loadLoanAccountSummary(int loanAccountNumber) {
=======
>>>>>>> demo
=======
        switch (item.getItemId()) {
            case R.id.agent_home:
                Intent intent = new Intent(ClientActivity.this, AgentActivity.class);
                intent.putExtra(Constants.CLIENT_ID, agentId);
                startActivity(intent);
                break;
            case R.id.action_new_client:
                startActivity(new Intent(ClientActivity.this, NewClientCreationLoginActivity.class));
                break;

            case R.id.logout:
                startActivity(new Intent(ClientActivity.this, LogoutActivity.class));
                break;
            default: //DO NOTHING
                break;
        }

        return super.onOptionsItemSelected(item);
>>>>>>> demo

    }

<<<<<<< HEAD
<<<<<<< HEAD
=======


>>>>>>> demo
    /*
     * Called when a Savings Account is Selected
     * from the list of Savings Accounts on Client Details Fragment
     *
     * It displays the summary of the Selected Savings Account
     */

=======
>>>>>>> demo
    @Override
    public void loadSavingsAccountSummary(int savingsAccountNumber) {
        ClientAccountDetailsFragment clientAccountDetailsFragment
                = ClientAccountDetailsFragment.newInstance(savingsAccountNumber);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.addToBackStack(FragmentConstants.FRAG_CLIENT_DETAILS);
<<<<<<< HEAD
        fragmentTransaction.replace(R.id.global_container,savingsAccountSummaryFragment).commit();
    }

<<<<<<< HEAD
    /*
     * Called when the make the make repayment button is clicked
     * in the Loan Account Summary Fragment.
     *
     * It will display the Loan Repayment Fragment where
     * the Information of the repayment has to be filled in.
     */
    @Override
    public void makeRepayment(Loan loan) {

        LoanRepaymentFragment loanRepaymentFragment = LoanRepaymentFragment.newInstance(loan);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.addToBackStack(FragmentConstants.FRAG_LOAN_ACCOUNT_SUMMARY);
        fragmentTransaction.replace(R.id.global_container, loanRepaymentFragment).commit();
    }
=======
>>>>>>> demo

    /*
     * Called when the make the make deposit button is clicked
     * in the Savings Account Summary Fragment.
     *
     * It will display the Transaction Fragment where the information
     * of the transaction has to be filled in.
     *
     * The transactionType defines if the transaction is a Deposit
     *
    */
    @Override
    public void makeDeposit(SavingsAccountWithAssociations savingsAccountWithAssociations, String transactionType) {

        SavingsAccountTransactionFragment savingsAccountTransactionFragment =
                SavingsAccountTransactionFragment.newInstance(savingsAccountWithAssociations, transactionType);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.addToBackStack(FragmentConstants.FRAG_SAVINGS_ACCOUNT_SUMMARY);
        fragmentTransaction.replace(R.id.global_container, savingsAccountTransactionFragment).commit();

=======
        fragmentTransaction.replace(R.id.global_container, clientAccountDetailsFragment).commit();
>>>>>>> demo
    }

    @Override
    public void makeTransfer(SavingsAccountWithAssociations savingsAccountWithAssociations, String transactionType) {
        AccountTransferFragment accountTransferFragment = AccountTransferFragment.newInstance(savingsAccountWithAssociations, transactionType);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.addToBackStack(FragmentConstants.FRAG_SAVINGS_ACCOUNT_SUMMARY);
        fragmentTransaction.replace(R.id.global_container, accountTransferFragment).commit();
    }
}
