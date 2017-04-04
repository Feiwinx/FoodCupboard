package sammobewick.pocketkitchen.activities_fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sammobewick.pocketkitchen.R;
import sammobewick.pocketkitchen.adapters.RvA_Custom_Ingredients;
import sammobewick.pocketkitchen.aws_intents.Dynamo_Upload_Json;
import sammobewick.pocketkitchen.aws_intents.S3_Upload_Image;
import sammobewick.pocketkitchen.data_objects.Ingredient;
import sammobewick.pocketkitchen.data_objects.PocketKitchenData;
import sammobewick.pocketkitchen.data_objects.Recipe_Full;
import sammobewick.pocketkitchen.data_objects.Recipe_Short;
import sammobewick.pocketkitchen.supporting.ActivityHelper;
import sammobewick.pocketkitchen.supporting.Constants;

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

    private String  user_id;
    private String  user_name;

    private RecyclerView                    mRecyclerView;
    private RvA_Custom_Ingredients          mRvAdapter;

    private int successCount;

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

        // Set up intent filters:
        IntentFilter filter = new IntentFilter(Constants.BC_UPLOAD_NAME);
        LocalBroadcastManager.getInstance(this).registerReceiver(new UploadReceiver(), filter);

        // Set up RecyclerView:
        mRecyclerView   = (RecyclerView) findViewById(R.id.rv_add_custom_recipe_ingredients);
        mRvAdapter      = new RvA_Custom_Ingredients(this);
        mRecyclerView.setAdapter(mRvAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set up OnClickListeners:
        findViewById(R.id.btn_add_custom_recipe).setOnClickListener(this);
        findViewById(R.id.lbl_add_custom_recipe_ingredients).setOnClickListener(this);
        findViewById(R.id.lbl_add_custom_recipe_instructions).setOnClickListener(this);
        findViewById(R.id.lbl_add_custom_recipe_dietary).setOnClickListener(this);
        findViewById(R.id.btn_custom_recipe_ing).setOnClickListener(this);
        findViewById(R.id.img_custom_recipe).setOnClickListener(this);

        setProgressBar(false);
    }

    private void saveSuccessful() {
        setProgressBar(false);
        // TODO: Inform user and clear the activity?
    }

    private void reportSuccess() {
        successCount++;

        if (successCount > 1) {
            saveSuccessful();
        }
        System.out.println("SuccessCount: " + successCount);
    }

    private void saveRecipe() {
        successCount = 0;
        String error_msg = "";
        List<Ingredient> ingredients = getRecipeIngredients();

        // We first check ingredients, as this would be the most time consuming!
        if (ingredients != null) {
            int id = 0;
            // Fetch checkbox data:
            boolean vegan           = ((CheckBox)findViewById(R.id.check_cr_diet_vegan)).isChecked();
            boolean vegetarian      = ((CheckBox)findViewById(R.id.check_cr_diet_vegetarian)).isChecked();
            boolean gluten          = ((CheckBox)findViewById(R.id.check_cr_diet_gluten)).isChecked();
            boolean dairyFree       = ((CheckBox)findViewById(R.id.check_cr_diet_dairy)).isChecked();

            // Unsure how to address these:
            boolean cheap           = false;
            boolean popular         = false;
            boolean healthy         = false;

            // TODO: the allergens are not stored!

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

            // Check for errors:
            if (error_msg.length() == 0) {
                // Create our ID using Title + User:
                id = (user_id + title).hashCode();
                System.out.println("TEST-ID: " + id);

                // Create the recipe objects:
                Recipe_Full rf = new Recipe_Full(
                        cheap,
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
                        healthy,
                        popular);

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

                // Set up a bundle for the JSON:
                Intent json_intent = new Intent(AddRecipeActivity.this, Dynamo_Upload_Json.class);
                json_intent.putExtra(Constants.JSON_DYNAMO_KEY, String.valueOf(id));
                json_intent.putExtra(Constants.JSON_DYNAMO, rf.getJson());
                this.startService(json_intent);

                // Set up a bundle for the Image:
                Intent s3_intent = new Intent(AddRecipeActivity.this, S3_Upload_Image.class);
                s3_intent.putExtra(Constants.S3_OBJECT_KEY, String.valueOf(id));
                s3_intent.putExtra(Constants.INTENT_S3_FILE, getImageFile());
                this.startService(s3_intent);

            } else {
                ActivityHelper.displayKnownError(AddRecipeActivity.this, error_msg);
                setProgressBar(false);
            }
        } else {
            error_msg = "Error: There appears to be a problem with the Ingredients table. Please ensure everything is populated!";
            ActivityHelper.displayKnownError(AddRecipeActivity.this, error_msg);
            setProgressBar(false);
        }
    }

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
            ///* DEBUG:
            Log.d(TAG, "Added Ingredient: " + ingredient.toString());
            /* END-DEBUG */
        }
        return  ingredientList;
    }

    @Override
    public void onClick(View v) {
        View gist;
        int id  = v.getId();

        // SWITCH VIEW ID:
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

    private void saveImage(Bitmap image) {
        ((ImageView) findViewById(R.id.img_custom_recipe)).setImageBitmap(image);

        // Overwrite any existing temporary image:
        try {
            FileOutputStream fos = this.openFileOutput(Constants.TEMP_IMG_NAME, Context.MODE_PRIVATE);
            image.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getImageFile() {
        String filename = this.getFilesDir() + "/" + Constants.TEMP_IMG_NAME;
        return new File(filename);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case 0: // ImageResultCode
                if (resultCode == RESULT_OK) {
                    Bitmap fromCamera = (Bitmap) data.getExtras().get("data");
                    saveImage(fromCamera);
                }
                break;
        }
    }

    /**
     * Helper method to control the window + progress bar:
     * @param visible
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

    private class UploadReceiver extends BroadcastReceiver {

        // Prevent instantiation:
        private UploadReceiver() { /* UPLOAD RECEIVER */ }

        @Override
        public void onReceive(Context context, Intent intent) {
            // Unpack Intent:
            /* Debug:
            System.out.println("Report: " + intent.getExtras().toString());
            System.out.println("Report: " + intent.getExtras().get(Constants.BC_UPLOAD_ID).toString());
            //*/

            boolean result = intent.getExtras().getBoolean(Constants.BC_UPLOAD_ID);
            if (result)
                reportSuccess();

            // else; reportFailire. // TODO: ...
        }
    }
}