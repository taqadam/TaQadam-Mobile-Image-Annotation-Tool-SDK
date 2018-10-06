package com.recoded.taqadam;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.recoded.taqadam.databinding.ActivityProfileBinding;
import com.recoded.taqadam.models.User;
import com.recoded.taqadam.models.auth.UserAuthHandler;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends BaseActivity {
    private ActivityProfileBinding binding;
    private User user;
    private TextView selectedTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile);
        user = UserAuthHandler.getInstance().getCurrentUser();
        if(user == null) finish();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.setUser(user);

        indicateVerificationStatus();

        binding.fabEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ProfileActivity.this, ConfirmProfileActivity.class);
                i.putExtra("EDIT_MODE", true);
                startActivity(i);
                finish();
            }
        });

        Picasso.with(this).load(user.getProfile().getAvatar()).into(binding.ivDisplayImage);

        binding.agreementCard.setWebViewClient(new WebViewClient());
        binding.agreementCard.loadUrl("https://www.taqadam.io/docs/TrainerAgreement");

        setImageViewClickListener();
        setPagerTabs();
    }

    private void setPagerTabs() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedTab != null && selectedTab.getId() == v.getId()) {
                    return;
                }
                int drawableId = 0;
                if (v.getId() == R.id.b_about) {
                    binding.agreementCard.setVisibility(View.GONE);
                    binding.aboutCard.setVisibility(View.VISIBLE);
                    drawableId = R.drawable.ic_person_purple_48dp;
                } else if (v.getId() == R.id.b_agreement) {
                    binding.agreementCard.setVisibility(View.VISIBLE);
                    binding.aboutCard.setVisibility(View.GONE);
                    drawableId = R.drawable.ic_agreement_purple_48dp;
                }

                if (selectedTab != null && selectedTab.getTag() != null) {
                    selectedTab.setCompoundDrawablesWithIntrinsicBounds(null, (Drawable) selectedTab.getTag(), null, null);
                }
                selectedTab = (TextView) v;
                selectedTab.setTag(selectedTab.getCompoundDrawables()[1]);
                selectedTab.setCompoundDrawablesWithIntrinsicBounds(0, drawableId, 0, 0);
            }
        };
        binding.bAbout.setOnClickListener(listener);
        binding.bAgreement.setOnClickListener(listener);
        binding.bAbout.performClick();
    }

    private void setImageViewClickListener() {
        binding.ivDisplayImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageViewerFragment frag = new ImageViewerFragment();
                frag.show(getSupportFragmentManager(), ImageViewerFragment.class.getSimpleName());
            }
        });
    }

    private void indicateVerificationStatus() {
        if (user.getIsApproved()) {
            binding.tvVerifiedIndicator.setText(R.string.verified);
            binding.tvVerifiedIndicator.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_verified_user_green, 0);
        }
        if (user.getIsEmailVerified()) {
            binding.ivEmailVerified.setImageResource(R.drawable.ic_check_green);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                binding.ivEmailVerified.setTooltipText(getString(R.string.verified_contact_method));
            }
        }
        /*if (user.isPhoneNumberVerified()) {
            binding.ivPhoneVerified.setImageResource(R.drawable.ic_check_green);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                binding.ivPhoneVerified.setTooltipText(getString(R.string.verified_contact_method));
            }
        }*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_discussion) {
            startActivity(new Intent(this, PostsFeedActivity.class));
            return true;
        } else if (id == R.id.action_notification) {
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public static class ImageViewerFragment extends DialogFragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_image_viewer, container, false);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            ImageView iv = view.findViewById(R.id.iv_image);
            Picasso.with(getActivity()).load(UserAuthHandler.getInstance().getCurrentUser().getProfile().getAvatar()).into(iv);
        }
    }
}
