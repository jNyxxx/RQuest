package com.example.ridequest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;
import java.util.ArrayList;
import java.util.List;

public class LandingActivity extends AppCompatActivity {

    private OnboardingAdapter onboardingAdapter;
    private LinearLayout layoutIndicators;
    private Button btnAction; // We keep the variable name 'btnAction' for logic, but link it to 'btnContinue'

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        layoutIndicators = findViewById(R.id.layoutIndicators);

        // --- ERROR FIX IS HERE ---
        // Your XML uses "btnContinue", so we must find that ID.
        btnAction = findViewById(R.id.btnContinue);
        // -------------------------

        setupOnboardingItems();

        final ViewPager2 onboardingViewPager = findViewById(R.id.viewPager);
        onboardingViewPager.setAdapter(onboardingAdapter);

        setupIndicators();
        setCurrentIndicator(0);

        onboardingViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setCurrentIndicator(position);

                // Change button text on the last slide
                if (position == onboardingAdapter.getItemCount() - 1) {
                    btnAction.setText("Get Started");
                } else {
                    btnAction.setText("Continue");
                }
            }
        });

        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onboardingViewPager.getCurrentItem() + 1 < onboardingAdapter.getItemCount()) {
                    onboardingViewPager.setCurrentItem(onboardingViewPager.getCurrentItem() + 1);
                } else {
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    finish();
                }
            }
        });
    }

    private void setupOnboardingItems() {
        List<OnboardingItem> onboardingItems = new ArrayList<>();

        // ITEM 1: Orange Car
        // Ensure you have renamed your image files in res/drawable to these names!
        int img1 = getResources().getIdentifier("ob_car_orange", "drawable", getPackageName());
        if(img1 == 0) img1 = android.R.drawable.ic_menu_gallery;

        OnboardingItem item1 = new OnboardingItem(
                img1,
                "Find Your Vehicle",
                "Find the perfect vehicle for every occasion!"
        );

        // ITEM 2: Blue Car
        int img2 = getResources().getIdentifier("ob_car_blue", "drawable", getPackageName());
        if(img2 == 0) img2 = android.R.drawable.ic_menu_gallery;

        OnboardingItem item2 = new OnboardingItem(
                img2,
                "Your dream Car",
                "Rent the car you've always wanted to drive."
        );

        // ITEM 3: Van
        int img3 = getResources().getIdentifier("ob_van", "drawable", getPackageName());
        if(img3 == 0) img3 = android.R.drawable.ic_menu_gallery;

        OnboardingItem item3 = new OnboardingItem(
                img3,
                "Large Ones too!",
                "Rent large vehicle for a big family or a long journey."
        );

        // ITEM 4: Money
        int img4 = getResources().getIdentifier("ob_money", "drawable", getPackageName());
        if(img4 == 0) img4 = android.R.drawable.ic_menu_gallery;

        OnboardingItem item4 = new OnboardingItem(
                img4,
                "Easier Payment",
                "Fast, secure payments so you can hit the road hassle-free"
        );

        onboardingItems.add(item1);
        onboardingItems.add(item2);
        onboardingItems.add(item3);
        onboardingItems.add(item4);

        onboardingAdapter = new OnboardingAdapter(onboardingItems);
    }

    private void setupIndicators() {
        ImageView[] indicators = new ImageView[onboardingAdapter.getItemCount()];
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(8, 0, 8, 0);

        for (int i = 0; i < indicators.length; i++) {
            indicators[i] = new ImageView(getApplicationContext());
            indicators[i].setImageDrawable(ContextCompat.getDrawable(
                    getApplicationContext(),
                    R.drawable.indicator_inactive
            ));
            indicators[i].setLayoutParams(layoutParams);
            layoutIndicators.addView(indicators[i]);
        }
    }

    private void setCurrentIndicator(int index) {
        int childCount = layoutIndicators.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ImageView imageView = (ImageView) layoutIndicators.getChildAt(i);
            if (i == index) {
                imageView.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.indicator_active));
            } else {
                imageView.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.indicator_inactive));
            }
        }
    }
}