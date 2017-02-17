package sammobewick.pocketkitchen.core;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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

public class LoginActivity extends AppCompatActivity implements
        View.OnClickListener,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG         = "LoginActivity";
    private static final int    RC_SIGN_IN  = 9001;

    private GoogleApiClient mGoogleApiClient;
    private boolean         signedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey("signedIn")) {
                signedIn = extras.getBoolean("signedIn", false);

                updateUI(signedIn);
            }
        } else { signedIn = false; }

        ActivityHelper helper = new ActivityHelper(this);
        if (!helper.isConnected()) { helper.displayNetworkWarning(); }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.DRIVE_APPFOLDER))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        SignInButton signInButton = (SignInButton) findViewById(R.id.btn_google_sign_in);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setOnClickListener(this);

        Button signOutButton = (Button) findViewById(R.id.btn_google_sign_out);
        signOutButton.setOnClickListener(this);

        Button disconnectButton = (Button) findViewById(R.id.btn_google_disconnect);
        disconnectButton.setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_google_sign_in:
                signIn();
                break;
            case R.id.btn_google_sign_out:
                signOut();
                break;
            case R.id.btn_google_disconnect:
                disconnectAccount();
                break;
        }
    }

    private void signIn() {
        showProgressDialog();
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        showProgressDialog();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        updateUI(false);
                        hideProgressDialog();
                    }
                }
        );
    }

    private void disconnectAccount() {
        showProgressDialog();
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        updateUI(false);
                        hideProgressDialog();
                    }
                }
        );
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        hideProgressDialog();

        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            updateUI(true);

            Intent intent = new Intent(this, TabbedActivity.class);
            startActivity(intent);
        } else {
            // Signed out, show unauthenticated UI.
            updateUI(false);
        }
    }

    private void updateUI(boolean signedIn) {
        if (signedIn) {
            findViewById(R.id.btn_google_disconnect).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_google_sign_out).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_google_sign_in).setVisibility(View.GONE);
        } else {
            findViewById(R.id.btn_google_disconnect).setVisibility(View.GONE);
            findViewById(R.id.btn_google_sign_out).setVisibility(View.GONE);
            findViewById(R.id.btn_google_sign_in).setVisibility(View.VISIBLE);
        }
    }

    private void showProgressDialog() {
        // TODO:
    }

    private void hideProgressDialog() {
        // TODO:
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        // TODO: Show error message.
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (signedIn == false) {
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
                    public void onResult(GoogleSignInResult googleSignInResult) {
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
        ActivityHelper helper = new ActivityHelper(this);
        if (!helper.isConnected()) { helper.displayNetworkWarning(); }
    }
}
