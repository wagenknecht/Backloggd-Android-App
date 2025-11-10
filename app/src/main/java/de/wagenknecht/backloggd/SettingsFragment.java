package de.wagenknecht.backloggd;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import de.wagenknecht.backloggd.worker.NotificationCheckWorker;
import de.wagenknecht.backloggd.worker.WishlistCheckerWorker;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewCompat.setOnApplyWindowInsetsListener(getListView(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        Preference dailyNotificationTime = findPreference("daily_notification_time");
        if (dailyNotificationTime != null) {
            updateDailyNotificationTimeSummary(dailyNotificationTime);

            dailyNotificationTime.setOnPreferenceClickListener(preference -> {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
                int hour = prefs.getInt("daily_notification_hour", 9);
                int minute = prefs.getInt("daily_notification_minute", 0);

                new TimePickerDialog(getContext(), (timePickerView, hourOfDay, minuteOfHour) -> {
                    prefs.edit()
                            .putInt("daily_notification_hour", hourOfDay)
                            .putInt("daily_notification_minute", minuteOfHour)
                            .apply();
                    updateDailyNotificationTimeSummary(preference);
                    WishlistCheckerWorker.scheduleNextWorker(requireContext());
                }, hour, minute, true).show();
                return true;
            });
        }

        ListPreference notificationInterval = findPreference("notification_interval");
        if (notificationInterval != null) {
            notificationInterval.setOnPreferenceChangeListener((preference, newValue) -> {
                long interval = Long.parseLong((String) newValue);
                scheduleNotificationCheckWorker(requireContext(), interval);
                return true;
            });
        }

        Preference developerLink = findPreference("developer_link");
        if (developerLink != null) {
            developerLink.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(requireContext(), MainActivity.class);
                intent.putExtra("urlToLoad", "https://backloggd.com/u/Tysk/");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                requireActivity().finish();
                return true;
            });
        }
    }

    private void updateDailyNotificationTimeSummary(Preference preference) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        int hour = prefs.getInt("daily_notification_hour", 9);
        int minute = prefs.getInt("daily_notification_minute", 0);
        preference.setSummary(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
    }

    private void scheduleNotificationCheckWorker(Context context, long interval) {
        if (interval == -1) {
            WorkManager.getInstance(context).cancelUniqueWork("NotificationCheck");
            Log.d("SettingsFragment", "Notification worker cancelled by user setting.");
            return;
        }

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest notificationWorkRequest =
                new PeriodicWorkRequest.Builder(NotificationCheckWorker.class, interval, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "NotificationCheck",
                ExistingPeriodicWorkPolicy.UPDATE,
                notificationWorkRequest);

        Log.d("SettingsFragment", "Notification worker scheduled for every " + interval + " minutes.");
    }
}
