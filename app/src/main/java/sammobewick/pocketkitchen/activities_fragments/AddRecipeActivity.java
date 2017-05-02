package sammobewick.pocketkitchen.activities_fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sammobewick.pocketkitchen.R;
import sammobewick.pocketkitchen.adapters.RvA_Custom_Ingredients;
import sammobewick.pocketkitchen.aws_intents.DownloadAWSImageAsync;
import sammobewick.pocketkitchen.aws_intents.Dynamo_Delete_Json;
import sammobewick.pocketkitchen.aws_intents.Dynamo_Upload_Json;
import sammobewick.pocketkitchen.aws_intents.S3_Delete_Image;
import sammobewick.pocketkitchen.aws_intents.S3_Upload_Image;
import sammobewick.pocketkitchen.data_objects.Ingredient;
import sammobewick.pocketkitchen.data_objects.PocketKitchenData;
import sammobewick.pocketkitchen.data_objects.Recipe_Full;
import sammobewick.pocketkitchen.data_objects.Recipe_Short;
import sammobewick.pocketkitchen.supporting.ActivityHelper;
import sammobewick.pocketkitchen.supporting.Constants;
import sammobewick.pocketkitchen.supporting.LocalFileHelper;

/**
 * Activity for adding a custom recipe to Amazon Dynamo.
 * Provides the interface functionality and sets up the views we are using.
 * Will also communicate with Amazon to send the Recipe up to the database.
 */
public class AddRecipeActivity extends AppCompatActivity implements View.OnClickListener {
    //********************************************************************************************//
    //  VARIABLES / HANDLERS FOR THIS ACTIVITY:                                                   //
    //********************************************************************************************//
    private static final String TAG = "AddRecipeActivity";

    // Recipe identifier:
    private Recipe_Short recipe;

    // Details from sharedPreferences about user:
    private String  user_id;
    private String  user_name;

    // RecyclerView + Adapter for the custom Ingredients:
    private RecyclerView                    mRecyclerView;
    private RvA_Custom_Ingredients          mRvAdapter;

    // Counter for returned requests from AWS:
    private int successCount;

    private boolean editing;

    /**
     * OnCreate for this Activity.
     * Performs general setup of the XML and sets OnClickListeners for the areas we want.
     * @param savedInstanceState Bundle - saved state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        // Get the details from the sharedPreferences:
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(AddRecipeActivity.this);

        if (sharedPreferences.contains("user_id")) {
            user_id = sharedPreferences.getString("user_id", "");
        }

        if (sharedPreferences.contains("user_name"))
            user_name = sharedPreferences.getString("user_name", "unknown");

        // Set up intent filters for getting Broadcasts back from AWS intent-services:
        IntentFilter filter = new IntentFilter(Constants.BC_UPLOAD_NAME);
        LocalBroadcastManager.getInstance(this).registerReceiver(new UploadReceiver(), filter);

        if (editing) {
            IntentFilter filter1 = new IntentFilter(Constants.BC_DELETE_NAME);
            LocalBroadcastManager.getInstance(this).registerReceiver(new DeleteReceiver(), filter1);
        }

        // Set up the RecyclerView:
        mRecyclerView   = (RecyclerView) findViewById(R.id.rv_add_custom_recipe_ingredients);
        mRvAdapter      = new RvA_Custom_Ingredients(this);
        mRecyclerView.setAdapter(mRvAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Get the details from the bundle (for editing):
        editing = false;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Recipe_Full rf  = null;
            Recipe_Short rs = null;

            if (extras.containsKey("recipe_short")) {
                rs = (Recipe_Short) extras.get("recipe_short");
            }
            if (extras.containsKey("view_single_recipe")) {
                rf = (Recipe_Full) extras.get("view_single_recipe");
            }

            if (rf != null & rs != null) {
                populateFields(rf,rs);
            } else {
                // Error message - should never occur:
                ActivityHelper.displayKnownError(AddRecipeActivity.this,
                        "Oops, looks like there was an issue sending the recipe data to this activity!");
            }
        }

        // Set up OnClickListeners:
        findViewById(R.id.btn_add_custom_recipe).setOnClickListener(this);
        findViewById(R.id.lbl_add_custom_recipe_ingredients).setOnClickListener(this);
        findViewById(R.id.lbl_add_custom_recipe_instructions).setOnClickListener(this);
        findViewById(R.id.lbl_add_custom_recipe_dietary).setOnClickListener(this);
        findViewById(R.id.btn_custom_recipe_ing).setOnClickListener(this);
        findViewById(R.id.img_custom_recipe).setOnClickListener(this);

        // Show keyboard (fixes a glitch with this view):
        findViewById(R.id.edit_add_custom_recipe_instructions).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                InputMethodManager mngr = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                if (hasFocus)
                    mngr.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        // Hide progress bar:
        setProgressBar(false);
    }

    /**
     * Creates our options menu using menu resource file.
     * @param menu Menu - menu
     * @return boolean - result
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (editing) {
            Log.i(TAG, "Inflating options menu...");
            // Inflate the menu; this adds items to the action bar if it is present.

            // Disabled - wasn't quite working as intended as we are deleting what we are viewing!
            // Also somewhat useless as you would press Edit if you wanted to delete!
            //getMenuInflater().inflate(R.menu.menu_custom_recipe, menu);
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Enables use of options menu. The original purpose is to allow deletion from inside of this
     * window. However, this might not be applicable so can be toggled on/off by inflating the menu.
     * @param item MenuItem - the menu item pressed.
     * @return boolean - as in parent method.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_delete_custom:
                successCount = 0;
                Intent delJson = new Intent(AddRecipeActivity.this, Dynamo_Delete_Json.class);
                startService(delJson);

                Intent delImg = new Intent(AddRecipeActivity.this, S3_Delete_Image.class);
                startService(delImg);
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Loads data into the fields + checkboxes.
     * @param rf Recipe_short - part 1 of data we use.
     * @param rs Recipe_Full - part 2 of data we use.
     */
    private void populateFields(Recipe_Full rf, Recipe_Short rs) {
        Log.i(TAG, "Populating with RS: " + rs.toString());
        Log.i(TAG, "Populating with RF: " + rf.toString());
        editing = true;
        recipe = rs;
        ImageView recipeImg = ((ImageView)findViewById(R.id.img_custom_recipe));
        new DownloadAWSImageAsync(AddRecipeActivity.this, recipeImg).execute(rs.getImage());

        mRvAdapter.setData(rf.getExtendedIngredients());

        ((EditText)findViewById(R.id.edit_add_custom_recipe_title)).setText(rf.getTitle());
        ((EditText)findViewById(R.id.edit_add_custom_recipe_servings)).setText(String.valueOf(rf.getServings()));
        ((EditText)findViewById(R.id.edit_add_custom_recipe_minutes)).setText(String.valueOf(rf.getReadyInMinutes()));
        ((EditText)findViewById(R.id.edit_add_custom_recipe_instructions)).setText(rf.getInstructions());

        ((CheckBox)findViewById(R.id.check_cr_diet_dairy)).setChecked(rf.isDairyFree());
        ((CheckBox)findViewById(R.id.check_cr_diet_gluten)).setChecked(rf.isGlutenFree());
        ((CheckBox)findViewById(R.id.check_cr_diet_vegan)).setChecked(rf.isVegan());
        ((CheckBox)findViewById(R.id.check_cr_diet_vegetarian)).setChecked(rf.isVegetarian());

        ((CheckBox)findViewById(R.id.check_cr_diet_eggs)).setChecked(false); // NOT SAVED
        ((CheckBox)findViewById(R.id.check_cr_diet_nuts)).setChecked(false); // NOT SAVED
        ((CheckBox)findViewById(R.id.check_cr_diet_seafood)).setChecked(false); // NOT SAVED
        ((CheckBox)findViewById(R.id.check_cr_diet_shellfish)).setChecked(false); // NOT SAVED
        ((CheckBox)findViewById(R.id.check_cr_diet_soy)).setChecked(false); // NOT SAVED
    }

    /**
     * Method to run once we have confirmed successful creation.
     */
    private void saveSuccessful() {
        setProgressBar(false);
        new AlertDialog.Builder(new ContextThemeWrapper(AddRecipeActivity.this, R.style.myDialog))
                .setTitle("Operation Successful")
                .setIcon(R.drawable.ic_done)
                .setMessage(R.string.feedback_added_recipe)
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AddRecipeActivity.super.onBackPressed();
                    }
                })
                .show();
    }

    /**
     * Method to inform the user of a problem from Amazon Intent-Services.
     */
    private void saveFailed() {
        setProgressBar(false);
        ActivityHelper.displayUnknownError(AddRecipeActivity.this,
                getString(R.string.feedback_unknown_problem_add_recipe));
    }

    /**
     * Counts a success from Amazon. Just need to count two but they can be in either order!
     */
    private void reportSuccess() {
        successCount++;

        if (successCount > 1) {
            saveSuccessful();
        }
        /* DEBUG:
        System.out.println("SuccessCount: " + successCount);
        /* END-DEBUG */
    }

    /**
     * Counts a success from Amazon for deleting the recipe. Just clears up the data and informs the
     * user.
     */
    private void reportDeleteSuccess() {
        successCount++;

        if (successCount > 1) {
            setProgressBar(false);
            ActivityHelper.displaySnackBarNoAction(AddRecipeActivity.this,
                    R.layout.activity_add_recipe, R.string.feedback_deleted_change);

            // TODO: clear data from fields.
        }
    }

    /**
     * Reports a suspected failure from AWS for deleting. Displays a dialog asking the user what
     * they want to do.
     */
    private void reportDeleteFailed() {
        setProgressBar(false);
        // TODO: Show a dialog with the option to keep the recipe or delete from local.
    }

    /**
     * This method performs our save by reading through the elements on screen, creating Recipe_Full
     * and Recipe_Short from that, and then calling Amazon Intent Services to upload the data.
     */
    private void saveRecipe() {
        successCount = 0;
        String error_msg = "";
        List<Ingredient> ingredients = getRecipeIngredients();

        // We first check ingredients, as this would be the most time consuming!
        if (ingredients != null) {
            // Fetch checkbox data:
            boolean vegan           = ((CheckBox)findViewById(R.id.check_cr_diet_vegan)).isChecked();
            boolean vegetarian      = ((CheckBox)findViewById(R.id.check_cr_diet_vegetarian)).isChecked();
            boolean gluten          = ((CheckBox)findViewById(R.id.check_cr_diet_gluten)).isChecked();
            boolean dairyFree       = ((CheckBox)findViewById(R.id.check_cr_diet_dairy)).isChecked();

            boolean eggs        = ((CheckBox)findViewById(R.id.check_cr_diet_eggs)).isChecked();
            boolean nuts        = ((CheckBox)findViewById(R.id.check_cr_diet_nuts)).isChecked();
            boolean soy         = ((CheckBox)findViewById(R.id.check_cr_diet_soy)).isChecked();
            boolean shellfish   = ((CheckBox)findViewById(R.id.check_cr_diet_shellfish)).isChecked();
            boolean seafood     = ((CheckBox)findViewById(R.id.check_cr_diet_seafood)).isChecked();

            // Strings:
            String  title           = ((EditText)findViewById(R.id.edit_add_custom_recipe_title)).getText().toString();
            String  instructions    = ((EditText)findViewById(R.id.edit_add_custom_recipe_instructions)).getText().toString();

            if (title.length() < 5)
                error_msg += "Error: You need to provide a descriptive TITLE!\n";

            if (instructions.length() < 5)
                error_msg += "Error: You need to provide some instructions!\n";

            // Integers:
            int     servings        = 1;
            String  servings_s      = ((EditText)findViewById(R.id.edit_add_custom_recipe_servings)).getText().toString();
            if (servings_s.length() > 0) {
                try {
                    servings = Integer.valueOf(servings_s);
                } catch (NumberFormatException ex) {
                    Log.e(TAG, ex.getLocalizedMessage());
                    error_msg += "Error: We don't recognise SERVINGS as a whole number!\n";
                }
            } else { error_msg += "Error: We need to know how many SERVINGS this recipe makes!\n"; }

            // Checking for ReadyInMinutes given + of correct format:
            int     readyInMinutes  = 0;
            String  readyIn_s       = ((EditText)findViewById(R.id.edit_add_custom_recipe_minutes)).getText().toString();
            if (readyIn_s.length() > 0) {
                try {
                    readyInMinutes = Integer.valueOf(readyIn_s);
                } catch (NumberFormatException ex) {
                    Log.e(TAG, ex.getLocalizedMessage());
                    error_msg += "Error: We don't recognise TIME REQUIRED as a number of minutes!\n";
                }
            } else { error_msg += "Error: We need to know how long TIME REQUIRED to cook is!\n"; }

            // Check for any errors encountered so far before attempting to upload:
            if (error_msg.length() == 0) {
                // Create our ID using Title + User:
                int id = (user_id + title).hashCode();

                /* DEBUG: */ Log.d(TAG, "ADD-RECIPE ID: " + id); /* END-DEBUG */

                // Create the recipe objects [cheap, popular,
                Recipe_Full rf = new Recipe_Full(
                        false,
                        dairyFree,
                        ingredients,
                        gluten,
                        id,    // We are using USER_ID here for the ID.
                        instructions,
                        readyInMinutes,
                        servings,
                        getString(R.string.custom_recipe_source_url),   // No need for SourceURL.
                        user_name,  // We are using USER_NAME here for the CreditText.
                        title,
                        vegan,
                        vegetarian,
                        false,
                        false);

                rf.setContEggs(eggs);
                rf.setContNuts(nuts);
                rf.setContSeafood(seafood);
                rf.setContShellfish(shellfish);
                rf.setContSoy(soy);

                Recipe_Short rs = new Recipe_Short(
                        id,                                             // id
                        Constants.INTENT_CUSTOM_ID,                     // image string
                        null,                                           // list of strings for urls
                        readyInMinutes,                                 // ready in minutes
                        title                                           // title
                );

                // Add the Recipe_Short to our list:
                PocketKitchenData pkData = PocketKitchenData.getInstance();
                pkData.addToMyRecipes(rs);

                // Set up a bundle for the JSON to be uploaded to DynamoDB:
                Intent json_intent = new Intent(AddRecipeActivity.this, Dynamo_Upload_Json.class);
                json_intent.putExtra(Constants.JSON_DYNAMO_KEY, String.valueOf(id));
                json_intent.putExtra(Constants.JSON_DYNAMO, rf.getJson());
                this.startService(json_intent);

                // Set up a bundle for the Image to be uploaded to S3:
                Intent s3_intent = new Intent(AddRecipeActivity.this, S3_Upload_Image.class);
                s3_intent.putExtra(Constants.S3_OBJECT_KEY, String.valueOf(id));
                s3_intent.putExtra(Constants.INTENT_S3_FILE, getImageFile());
                this.startService(s3_intent);

                LocalFileHelper helper = new LocalFileHelper(AddRecipeActivity.this);
                helper.saveAll();

            } else {
                // Display error due to previous checks & remove the progress spinner.
                ActivityHelper.displayKnownError(AddRecipeActivity.this, error_msg);
                setProgressBar(false);
            }
        } else {
            // Display error due to ingredients table & remove the progress spinner.
            error_msg = "Error: There appears to be a problem with the Ingredients table. Please ensure everything is populated!";
            ActivityHelper.displayKnownError(AddRecipeActivity.this, error_msg);
            setProgressBar(false);
        }
    }

    /**
     * Helper method to read through the RecyclerView for custom ingredients.
     * When using this, we should make sure to check for the null return and handle that as an error!
     * @return List<Ingredient> - being the list of ingredients entered into the RecyclerView.
     */
    private List<Ingredient> getRecipeIngredients() {
        // Prepare a list for our Ingredients:
        List<Ingredient> ingredientList = new ArrayList<>();

        // Loop through RecyclerView and get the Ingredient details:
        for (int i=0; i < mRecyclerView.getChildCount(); i++) {
            RvA_Custom_Ingredients.ViewHolder vh = (RvA_Custom_Ingredients.ViewHolder) mRecyclerView.findViewHolderForLayoutPosition(i);

            Ingredient ingredient = vh.getIngredient();

            if (ingredient == null) {
                return null;
            }

            ingredientList.add(ingredient);
            /* DEBUG: */ Log.d(TAG, "Added Ingredient: " + ingredient.toString()); /* END-DEBUG */
        }
        return  ingredientList;
    }

    /**
     * This activity is an OnClickListener, and must implement this method.
     * We use it to handle the events we are listening for!
     * @param v View - the view that was clicked upon.
     */
    @Override
    public void onClick(View v) {
        View gist;
        int id  = v.getId();

        // Switch out id to establish which was clicked:
        switch (id) {
            // ADD BLANK ITEM
            case R.id.lbl_add_custom_recipe_ingredients:
                mRvAdapter.addBlankItem();
                break;
            // HIDE GIST - RECIPE INGREDIENTS
            case R.id.btn_custom_recipe_ing:
                gist = findViewById(R.id.rv_add_custom_recipe_ingredients);
                if (gist.isShown()) {
                    ((ImageButton) v).setImageResource(R.drawable.ic_arrow_up);
                    gist.setVisibility(View.GONE);
                } else {
                    ((ImageButton) v).setImageResource(R.drawable.ic_arrow_down);
                    gist.setVisibility(View.VISIBLE);
                }
                break;
            // HIDE GIST - RECIPE INSTRUCTIONS
            case R.id.lbl_add_custom_recipe_instructions:
                gist = findViewById(R.id.edit_add_custom_recipe_instructions);
                if (gist.isShown()) {
                    ((TextView) v).setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_up, 0);
                    gist.setVisibility(View.GONE);
                } else {
                    ((TextView) v).setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0);
                    gist.setVisibility(View.VISIBLE);
                }
                break;
            // HIDE GIST - DIETARY INFORMATION
            case R.id.lbl_add_custom_recipe_dietary:
                gist = findViewById(R.id.grid_custom_recipe_dietary);
                if (gist.isShown()) {
                    ((TextView) v).setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_up, 0);
                    gist.setVisibility(View.GONE);
                } else {
                    ((TextView) v).setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0);
                    gist.setVisibility(View.VISIBLE);
                }
                break;
            // ATTEMPT TO SAVE RECIPE:
            case R.id.btn_add_custom_recipe:
                if (!ActivityHelper.isConnected(AddRecipeActivity.this)) {
                    ActivityHelper.displaySnackBarNoAction(AddRecipeActivity.this, R.id.rl_add_custom_recipe, R.string.wifi_warning_short);
                } else { setProgressBar(true); saveRecipe(); }
                break;

            // IMAGE CAPTURING:
            case R.id.img_custom_recipe:
                Intent getPicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                getPicture.putExtra(MediaStore.EXTRA_OUTPUT, Constants.TEMP_IMG_NAME);
                startActivityForResult(getPicture, 0);
                break;
        }
    }

    /**
     * Helper method to save a Bitmap to a temporary file. We need the context to do this.
     * @param image Bitmap - being the image to temporarily save.
     */
    private void saveImage(Bitmap image) {
        ((ImageView) findViewById(R.id.img_custom_recipe)).setImageBitmap(image);

        // Overwrite any existing temporary image:
        try {
            FileOutputStream fos = this.openFileOutput(Constants.TEMP_IMG_NAME, Context.MODE_PRIVATE);
            image.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();

        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Helper method to return the file representing the Image for the recipe.
     * This is taken by the camera intent, but also referenced when uploading to S3.
     * @return File - being the description of the image we captured.
     */
    public File getImageFile() {
        String filename = this.getFilesDir() + "/" + Constants.TEMP_IMG_NAME;
        return new File(filename);
    }

    /**
     * Implemented due to camera usage.
     * @param requestCode int - the request code
     * @param resultCode int - the result code
     * @param data Intent - the data thats come back
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // We could handle more requests here but there isn't any other ActivityResults to handle.
        switch (requestCode){
            case 0: // Our default ImageResultCode
                if (resultCode == RESULT_OK) {
                    Bitmap fromCamera = (Bitmap) data.getExtras().get("data");
                    saveImage(fromCamera);
                }
                break;
        }
    }

    /**
     * Helper method which displays a progress spinner + enables/disables the Activity:
     * @param visible boolean - is the spinner visible?
     */
    private void setProgressBar(boolean visible) {
        if (visible) {
            findViewById(R.id.save_recipe_progress).setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else {
            findViewById(R.id.save_recipe_progress).setVisibility(View.GONE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    /**
     * Private inner class to get Broadcast responses for uploading back into this Activity.
     * We register this in the OnCreate but basically this just handles unpacking the intent.
     */
    private class UploadReceiver extends BroadcastReceiver {

        // Prevent instantiation:
        private UploadReceiver() { /* UPLOAD RECEIVER */ }

        @Override
        public void onReceive(Context context, Intent intent) {
            // Unpack Intent + pass to Activity:
            boolean result = intent.getExtras().getBoolean(Constants.BC_UPLOAD_ID);

            if (result)
                reportSuccess();

            // Check for failure (accounting for other IntentService):
            else if (successCount > 0)
                saveFailed();
        }
    }

    /**
     * Private inner class to get Broadcast responses for deletion back into this Activity.
     */
    private class DeleteReceiver extends BroadcastReceiver {

        // Prevent instantiation:
        private DeleteReceiver() { /* DELETE RECEIVER */ }

        @Override
        public void onReceive(Context context, Intent intent) {
            // Unpack Intent + pass to Activity:
            boolean result = intent.getExtras().getBoolean(Constants.BC_DELETE_ID);

            if (result)
                reportDeleteSuccess();

            else if (successCount > 0)
                reportDeleteFailed();
        }
    }
}