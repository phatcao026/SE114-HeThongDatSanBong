package com.example.timsanbong.ui.customer;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.timsanbong.R;
import com.example.timsanbong.ui.profile.ProfileFragment;
import com.example.timsanbong.utils.NavBarManager;
import com.example.timsanbong.utils.PushNotificationManager;

public class CustomerMainActivity extends AppCompatActivity {

    private int currentTab = -1;
    private NotificationViewModel notificationViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_main_container);
        PushNotificationManager.prepareForAuthenticatedUser(this);

        notificationViewModel = new ViewModelProvider(this).get(NotificationViewModel.class);
        observeNotifications();

        int startTab = NavBarManager.ITEM_HOME;
        if (getIntent() != null) {
            startTab = getIntent().getIntExtra("SELECT_TAB", NavBarManager.ITEM_HOME);
        }

        if (savedInstanceState == null) {
            switchToTab(startTab);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent != null) {
            int selectTab = intent.getIntExtra("SELECT_TAB", -1);
            if (selectTab != -1) {
                switchToTab(selectTab);
            }
        }
    }

    public void switchToTab(int tabIndex) {
        if (currentTab == tabIndex) {
            return;
        }
        currentTab = tabIndex;

        Fragment fragment;
        switch (tabIndex) {
            case NavBarManager.ITEM_HOME:
                fragment = new CustomerHomeFragment();
                break;
            case NavBarManager.ITEM_MATCHMAKING:
                fragment = new FindOpponentFragment();
                break;
            case NavBarManager.ITEM_SEARCH:
                fragment = new FindPitchFragment();
                break;
            case NavBarManager.ITEM_BOOKINGS:
                fragment = new MyBookingsFragment();
                break;
            case NavBarManager.ITEM_NOTIFICATIONS:
                fragment = new NotificationsFragment();
                break;
            case NavBarManager.ITEM_PROFILE:
                fragment = new ProfileFragment();
                break;
            default:
                fragment = new CustomerHomeFragment();
                tabIndex = NavBarManager.ITEM_HOME;
                break;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.customerFragmentContainer, fragment);
        transaction.commit();

        new NavBarManager(this, tabIndex).setup();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (notificationViewModel != null) {
            notificationViewModel.loadUnreadCount();
        }
    }

    private void observeNotifications() {
        notificationViewModel.unreadCountState.observe(this, resource -> {
            if (resource == null || resource.status != com.example.timsanbong.utils.Resource.Status.SUCCESS
                    || resource.data == null) {
                return;
            }
            int count = resource.data;
            android.view.View dot = findViewById(R.id.viewNavNotificationDot);
            if (dot != null) {
                dot.setVisibility(count > 0 ? android.view.View.VISIBLE : android.view.View.GONE);
            }
        });
    }
}
