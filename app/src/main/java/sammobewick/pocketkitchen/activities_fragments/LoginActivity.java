package sammobewick.pocketkitchen.activities_fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;

import sammobewick.pocketkitchen.R;
import sammobewick.pocketkitchen.supporting.ActivityHelper;
import sammobewick.pocketkitchen.supporting.LocalFileHelper;

/**
 * Login Activity. Utilises Google Sign In to track the user (this will be used in the Drive save/load
 * activities, and in the custom recipe creation). It also provides a nice introduction to the
 * application on first launch, but usually will utilise automatic sign in.
 *
 * Can be revisited from TabbedActivity to sign out / revoke account access.
 */
public class LoginActivity extends AppCompatActivity implements
        View.OnClickListener,
        GoogleApiClient.OnConnectionFailedListener {
    //********************************************************************************************//
    //  VARIABLES / HANDLERS FOR THIS ACTIVITY:                                                   //
    //********************************************************************************************//
    private static final String TAG         = "LoginActivity";
    private static final int    RC_SIGN_IN  = 9001;

    private GoogleApiClient     mGoogleApiClient;
    private SharedPreferences   sharedPreferences;
    private boolean             signedIn;
    private boolean             connected;

    /**
     * OnCreate method. Sets up our default values (i.e. UI, signedIn), and creates our sign-in client.
     * Also sets up our listeners for the buttons.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Grab the shared preferences:
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Get the bundled extras. These only exist when returning to this activity.
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey("signedIn")) {
                signedIn = extras.getBoolean("signedIn", false);

                //if (sharedPreferences.contains("user_name")) {
                    setWelcomeMessage(sharedPreferences.getString("user_name", "Guest"));
                //}
            }
        } else { signedIn = false; }

        Log.i(TAG, "Got signed-in argument of: " + signedIn);

        // Update UI:
        updateUI(signedIn);

        // Create Sign-In Options:
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.DRIVE_APPFOLDER))
                .requestScopes(new Scope(Scopes.DRIVE_FILE))
                .requestEmail()
                .build();

        // Build API Client:
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addScope(new Scope(Scopes.DRIVE_FILE))
                .addScope(new Scope(Scopes.DRIVE_APPFOLDER))
                .build();

        // Set up listeners for the buttons using this activity:
        SignInButton signInButton = (SignInButton) findViewById(R.id.btn_google_sign_in);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setOnClickListener(this);

        Button signOutButton = (Button) findViewById(R.id.btn_google_sign_out);
        signOutButton.setOnClickListener(this);

        Button disconnectButton = (Button) findViewById(R.id.btn_google_disconnect);
        disconnectButton.setOnClickListener(this);

        Button proceedButton = (Button) findViewById(R.id.btn_proceed);
        proceedButton.setOnClickListener(this);

        // Hide progress dialog:
        hideProgressDialog();
    }

    /**
     * Method required when launching an Activity for result. We use it here to deal with the
     * Google Sign In.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    /**
     * OnClick method for our buttons, as we have set our listener to be this class (rather than
     * creating a pre-defined listener for each).
     * @param v View - the view that was pressed.
     */
    @Override
    public void onClick(View v) {
        // Switch the view ID to establish which was pressed:
        switch (v.getId()) {
            case R.id.btn_google_sign_in:
                if (!ActivityHelper.isConnected(LoginActivity.this)) {
                    ActivityHelper.displayNetworkWarning(LoginActivity.this);
                } else { signIn(); }
                break;
            case R.id.btn_google_sign_out:
                signOut();
                break;
            case R.id.btn_google_disconnect:
                confirmDisconnectAccount();
                break;
            case R.id.btn_proceed:
                proceed();
                break;
        }
    }

    /**
     * This is simply the method to proceed to PocketKitchen.
     * Can be triggered in a few ways.
     */
    private void proceed() {
        Intent intent = new Intent(this, TabbedActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    /**
     * Shows the progress dialog and launches the sign-in intent.
     */
    private void signIn() {
        showProgressDialog();
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * Shows the progress dialog and launches the sign-out intent.
     */
    private void signOut() {
        showProgressDialog();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        // Set up the UI for signed out and action complete:
                        updateUI(false);
                        hideProgressDialog();

                        // TODO: something here
                        //LocalFileHelper helper = new LocalFileHelper(LoginActivity.this);
                        //helper.deleteAll(true);
                        //PocketKitchenData pkData = PocketKitchenData.getInstance();
                        //pkData.setRecipesDisplayed(null);
                    }
                }
        );
    }

    /**
     * Method to show a dialog to the user to confirm they want to disconnect the account,
     * resulting in deletion of local files.
     */
    private void confirmDisconnectAccount() {
        AlertDialog dialog = new AlertDialog.Builder(new ContextThemeWrapper(LoginActivity.this, R.style.myDialog)).create();

        dialog.setTitle(R.string.lbl_dialog_confirmation_title);
        dialog.setMessage(getString(R.string.lbl_dialog_confirmation_revoke));
        dialog.setCancelable(true);
        dialog.setIcon(android.R.drawable.ic_dialog_alert);

        // BUTTONS
        String lbl;
        lbl = getString(R.string.lbl_dialog_confirmation_yes);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, lbl, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                disconnectAccount();
            }
        });

        lbl = getString(R.string.lbl_dialog_confirmation_no);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, lbl, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.e(TAG, "Account revocation aborted");
            }
        });
        dialog.show();
    }

    /**
     * Shows the progress dialog, then launches the disconnect process.
     */
    private void disconnectAccount() {
        showProgressDialog();
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        // Only on success do we delete the data:
                        LocalFileHelper helper = new LocalFileHelper(LoginActivity.this);
                        helper.deleteAllNoDialog(false);

                        // Set up the UI for signed out and action complete:
                        updateUI(false);
                        hideProgressDialog();
                        ActivityHelper.displaySnackBarNoAction(LoginActivity.this, R.id.login_form_ll, R.string.snackbar_revoke_success);
                    }
                }
        );
    }

    /**
     * Handles the sign in result (called from our callback):
     * @param result GoogleSignInResult - passed result.
     */
    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        hideProgressDialog();

        /* TEST-DATA:
        for (int a = 0; a < 5; a++) {
            Ingredient ingredient = new Ingredient(
                    (float) a * 3,
                    "Test Name: " + a,
                    "U" + a
            );
            pkData.addCustomIngredient(ingredient);
        }
        // END-TEST-DATA */

        if (result.isSuccess()) {
            // We will want to reference their name/ID later so save them to the application:
            GoogleSignInAccount acct = result.getSignInAccount();

            setWelcomeMessage(acct != null ? acct.getDisplayName() : "Guest");

            sharedPreferences.edit()
                    .putString("user_id", acct != null ? acct.getId() : null)
                    .putString("user_name", acct != null ? acct.getDisplayName() : null)
                    .apply();

            // Update UI:
            updateUI(true);

            // Load local files:
            //LocalFileHelper helper = new LocalFileHelper(this);
            //helper.loadAll();

            // Automatically continue when signing in, but not when returning to this activity:
            if (!signedIn & connected) { this.proceed(); }
        } else {
            // Signed out, show unauthenticated UI.
            updateUI(false);
        }
    }

    /**
     * Sets up our welcome message once logged in.
     * @param name String - being the name to display.
     */
    private void setWelcomeMessage(String name) {
        TextView welcome    = (TextView) findViewById(R.id.login_welcome);
        String welcomeText  = String.format(getString(R.string.lbl_welcome_msg), name);
        welcome.setText(welcomeText);
    }

    /**
     * Updates our UI depending on signed in or out.
     * @param signedIn boolean - are we signed in?
     */
    private void updateUI(boolean signedIn) {
        if (signedIn) {
            findViewById(R.id.login_welcome).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_google_disconnect).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_google_sign_out).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_google_sign_in).setVisibility(View.GONE);
            findViewById(R.id.btn_proceed).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.login_welcome).setVisibility(View.GONE);
            findViewById(R.id.btn_google_disconnect).setVisibility(View.GONE);
            findViewById(R.id.btn_google_sign_out).setVisibility(View.GONE);
            findViewById(R.id.btn_google_sign_in).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_proceed).setVisibility(View.GONE);
        }
    }

    /**
     * Helper method to show the progress spinner.
     */
    private void showProgressDialog() {
        findViewById(R.id.login_progress).setVisibility(View.VISIBLE);
    }

    /**
     * Helper method to hide the progress spinner.
     */
    private void hideProgressDialog() {
        findViewById(R.id.login_progress).setVisibility(View.GONE);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.e(TAG, "onConnectionFailed:" + connectionResult);
        ActivityHelper.displayUnknownError(getApplicationContext(), connectionResult.getErrorMessage());
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!signedIn) {
            OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
            if (opr.isDone()) {
                // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
                // and the GoogleSignInResult will be available instantly.
                Log.d(TAG, "Got cached sign-in");
                GoogleSignInResult result = opr.get();
                handleSignInResult(result);
            } else {
                // If the user has not previously signed in on this device or the sign-in has expired,
                // this asynchronous branch will attempt to sign in the user silently.  Cross-device
                // single sign-on will occur in this branch.
                showProgressDialog();
                opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                    @Override
                    public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
                        hideProgressDialog();
                        handleSignInResult(googleSignInResult);
                    }
                });
            }
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        connected = ActivityHelper.isConnected(LoginActivity.this);
        if (!connected) {
            ActivityHelper.displayNetworkWarning(LoginActivity.this);
            Log.i(TAG, "No connection onPostResume!");
        }
    }
}
