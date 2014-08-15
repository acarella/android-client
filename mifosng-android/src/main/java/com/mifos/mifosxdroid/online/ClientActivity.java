package com.mifos.mifosxdroid.online;

import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.mifos.mifosxdroid.R;
import com.mifos.objects.accounts.savings.SavingsAccountWithAssociations;
import com.mifos.services.API;
import com.mifos.services.data.GpsCoordinatesRequest;
import com.mifos.services.data.GpsCoordinatesResponse;
import com.mifos.utils.Constants;
import com.mifos.utils.FragmentConstants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import butterknife.ButterKnife;
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
    private static final int MENU_ITEM_NEW_CLIENT = 1001;
    private static final int MENU_ITEM_DATA_TABLES = 1002;
    private static final int MENU_ITEM_LOGOUT = 1003;

    /**
     * Control Menu Changes from Fragments
     * change this Variable to True in the Fragment and Magic
     * Happens in onPrepareOptionsMenu Method Below
     */
    public static Boolean didMenuDataChange = Boolean.FALSE;
    public static Boolean shouldAddDataTables = Boolean.FALSE;

    /**
     * Property to identify the type of data tables to be shown.
     */
    public static int idOfDataTableToBeShownInMenu = -1;

    /**
     * This list will contain list of data tables
     * and will be used to inflate the Submenu Datatables
     */
    public static List<String> clientDataTableMenuItems = new ArrayList<String>();


    // Null if play services are not available.
    private LocationClient mLocationClient;
    // True if play services are available and location services are connected.
    private AtomicBoolean locationAvailable = new AtomicBoolean(false);
    private int clientId;
    private int mifosClientId;

    //TODO Try to shorten the code, this activity contains too much of repeated code
    //Implement DRY - Don't Repeat Yourself Approach Here

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global_container_layout);
        ButterKnife.inject(this);
        //TODO figure out login activity for PayGoSol agent so this doesn't need to be hardcoded
        clientId = 1223;
        boolean workaround = getIntent().getBooleanExtra(Constants.DID_USE_WORKAROUND, false);
        clientId = getIntent().getExtras().getInt(Constants.CLIENT_ID);
        FragmentTransaction fragmentTransaction =  getSupportFragmentManager().beginTransaction();
        ClientDetailsFragment clientDetailsFragment = ClientDetailsFragment.newInstance(clientId, workaround);
        fragmentTransaction.replace(R.id.global_container, clientDetailsFragment).commit();

        // Initialize location client only if play services are available.
        if (servicesConnected()) {
            mLocationClient = new LocationClient(this, this, this);
        } else {
            mLocationClient = null;
            locationAvailable.set(false);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        if (mLocationClient != null) {
            mLocationClient.connect();
        }
    }

    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        if (mLocationClient != null) {
            mLocationClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.client, menu);
        return true;
    }

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

            // This is a static menu item that will be added on the second position
            menu.add(Menu.NONE,MENU_ITEM_NEW_CLIENT,Menu.NONE, "New Client");

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
                    SubMenu dataTableSubMenu = menu.getItem(2).getSubMenu();
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

            // This is a static menu item that will be added on the second position
            menu.add(Menu.NONE,MENU_ITEM_LOGOUT,Menu.NONE, "LogOut");
        }

        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // TODO: The REST API for this is NOT WORKING YET!
        // Currently it will show a toast, but will not actually save anything to the data table.
        if (id == MENU_ITEM_SAVE_LOCATION) {
            if (locationAvailable.get()) {
                Location location = mLocationClient.getLastLocation();
                Toast.makeText(this, "Current location NOT being saved yet: "
                        + location.toString(), Toast.LENGTH_SHORT).show();

                API.gpsCoordinatesService.setGpsCoordinates(clientId,
                        new GpsCoordinatesRequest(clientId, location.getLatitude(), location.getLongitude()),
                        new Callback<GpsCoordinatesResponse>(){
                            @Override
                            public void success(GpsCoordinatesResponse gpsCoordinatesResponse, Response response) {
                                Log.d("online/ClientActivity", "Successfully saved GPS coordinates");
                            }

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

        if (id == MENU_ITEM_NEW_CLIENT){
            Intent intent = new Intent(ClientActivity.this, PGSAgentLoginActivity.class);
            startActivity(intent);
        }

        if (id == MENU_ITEM_LOGOUT){
            Intent intent = new Intent(ClientActivity.this, LogoutActivity.class);
            startActivity(intent);
        }

        if(id >= 0 && id < clientDataTableMenuItems.size())
        {
            loadDataTableFragment(id);
        }

        return super.onOptionsItemSelected(item);
    }

    public void loadDataTableFragment(int dataTablePostionInTheList) {

        //TODO Add a detailed implementation

        DataTableFragment dataTableFragment = DataTableFragment.newInstance(ClientDetailsFragment.clientDataTables.get(dataTablePostionInTheList));
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.addToBackStack(FragmentConstants.FRAG_CLIENT_DETAILS);
        fragmentTransaction.replace(R.id.global_container,dataTableFragment).commit();
    }



    /*
     * Called when a Savings Account is Selected
     * from the list of Savings Accounts on Client Details Fragment
     *
     * It displays the summary of the Selected Savings Account
     */

    @Override
    public void loadSavingsAccountSummary(int savingsAccountNumber) {
        SavingsAccountSummaryFragment savingsAccountSummaryFragment
                = SavingsAccountSummaryFragment.newInstance(savingsAccountNumber);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.addToBackStack(FragmentConstants.FRAG_CLIENT_DETAILS);
        fragmentTransaction.replace(R.id.global_container,savingsAccountSummaryFragment).commit();
    }


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

    }
    /*
     * Called when the make the make withdrawal button is clicked
     * in the Savings Account Summary Fragment
     *
     * It will display the Transaction Fragment where the information
     * of the transaction has to be filled in.
     *
     * The transactionType defines if the transaction is a Deposit
    */

    @Override
    public void makeWithdrawal(SavingsAccountWithAssociations savingsAccountWithAssociations, String transactionType) {

        SavingsAccountTransactionFragment savingsAccountTransactionFragment =
                SavingsAccountTransactionFragment.newInstance(savingsAccountWithAssociations, transactionType);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.addToBackStack(FragmentConstants.FRAG_SAVINGS_ACCOUNT_SUMMARY);
        fragmentTransaction.replace(R.id.global_container, savingsAccountTransactionFragment).commit();
    }

    @Override
    public void makeTransfer(SavingsAccountWithAssociations savingsAccountWithAssociations, String transactionType){
        AccountTransferFragment accountTransferFragment = AccountTransferFragment.newInstance(savingsAccountWithAssociations, transactionType);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.addToBackStack(FragmentConstants.FRAG_SAVINGS_ACCOUNT_SUMMARY);
        fragmentTransaction.replace(R.id.global_container, accountTransferFragment).commit();

    }

    /**
     * Returns true if Google Play services is available, otherwise false.
     */
    boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            Log.d(this.getLocalClassName(), "Google Play Services connected");
            return true;
        // Google Play services was not available for some reason
        } else {
            Log.w(this.getLocalClassName(), "Google Play Services not available");
            Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates.
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        locationAvailable.set(true);
        Log.d(this.getLocalClassName(), "Connected to location services");
        //Log.d(this.getLocalClassName(), "Current location: "
        //        + mLocationClient.getLastLocation().toString());
    }

    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onDisconnected() {
        locationAvailable.set(false);
        Log.d(this.getLocalClassName(), "Disconnected from location services");
    }

    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        locationAvailable.set(false);
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);

                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
                Log.e(this.getLocalClassName(),
                        "Connection to location services failed" + connectionResult.getErrorCode());
                Toast.makeText(this, "Connection to location services failed.",
                        Toast.LENGTH_SHORT).show();
            }
        } else { // No resolution available.
            Log.e(this.getLocalClassName(),
                    "Connection to location services failed" + connectionResult.getErrorCode());
            Toast.makeText(this, "Connection to location services failed.",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
