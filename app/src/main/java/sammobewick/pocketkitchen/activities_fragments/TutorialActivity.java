package sammobewick.pocketkitchen.activities_fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;

import com.github.paolorotolo.appintro.AppIntro2;

import sammobewick.pocketkitchen.R;

/**
 * This the tutorial activity for introducing concepts to the user. Most of the content is setup in
 * the XML files (6 of them), and the rest of the work is nicely handled by the library in use here.
 * Created by Sam on 06/04/2017.
 */
public class TutorialActivity extends AppIntro2 {

    private boolean requested;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey("requested")) {
                requested = extras.getBoolean("requested");
            }
        }

        // Add the slides:
        addSlide(SampleSlide.newInstance(R.layout.slide_intro_1));
        addSlide(SampleSlide.newInstance(R.layout.slide_intro_2));
        addSlide(SampleSlide.newInstance(R.layout.slide_intro_3));
        addSlide(SampleSlide.newInstance(R.layout.slide_intro_4));
        addSlide(SampleSlide.newInstance(R.layout.slide_intro_5));
        addSlide(SampleSlide.newInstance(R.layout.slide_intro_6));

        // Setup the details for the intro activity:
        showSkipButton(true);
        setProgressButtonEnabled(true);
        setVibrate(false);
        setFadeAnimation();
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);

        if (!requested) {
            new AlertDialog.Builder(new ContextThemeWrapper(TutorialActivity.this, R.style.myDialog))
                    .setTitle("Leaving Tutorial...")
                    .setMessage("Quick note: you can always revisit the tutorial from the Settings menu.")
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            onBackPressed();
                        }
                    })
                    .show();
        } else { onBackPressed(); }
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        super.onBackPressed();
    }
}
