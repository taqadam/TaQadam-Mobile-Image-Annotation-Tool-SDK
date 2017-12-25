package com.recoded.taqadam;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.recoded.taqadam.databinding.ActivityProfileBinding;
import com.recoded.taqadam.models.User;
import com.recoded.taqadam.models.auth.UserAuthHandler;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding binding;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile);
        user = UserAuthHandler.getInstance().getCurrentUser();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.setUser(user);

        indicateVerificationStatus();

        binding.fabEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, ConfirmProfileActivity.class));
                finish();
            }
        });

        Picasso.with(this).load(user.getPicturePath()).into(binding.ivDisplayImage);

        setImageViewClickListener();
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
        if (!user.isAccountApproved()) {
            binding.tvVerifiedIndicator.setText("Awaiting Verification");
            binding.tvVerifiedIndicator.setCompoundDrawables(null, null, null, null);
        }
        if (user.isEmailVerified()) {
            binding.ivEmailVerified.setImageResource(R.drawable.ic_check_circle);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                binding.ivEmailVerified.setTooltipText(getString(R.string.verified));
            }
        }
        if (user.isPhoneNumberVerified()) {
            binding.ivPhoneVerified.setImageResource(R.drawable.ic_check_circle);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                binding.ivPhoneVerified.setTooltipText(getString(R.string.verified));
            }
        }
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
            Picasso.with(getActivity()).load(UserAuthHandler.getInstance().getCurrentUser().getPicturePath()).into(iv);
        }
    }
}
