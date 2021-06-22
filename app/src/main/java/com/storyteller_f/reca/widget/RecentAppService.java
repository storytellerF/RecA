package com.storyteller_f.reca.widget;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import androidx.annotation.RequiresApi;

import com.storyteller_f.reca.R;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * @author faber
 */
public class RecentAppService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RecentAppPresentFactory(getApplicationContext(), intent);
    }

    static class RecentAppPresentFactory implements RemoteViewsService.RemoteViewsFactory {
        private static final String TAG = "RecentAppPresentFactory";
        private final List<LabelItem> appList;
        private final Context context;
        private final List<DesktopApplication> desktopApplications;
        private final PackageManager packageManager;
        private final int totalCount;
        private final boolean isUsedTime;

        RecentAppPresentFactory(Context context, Intent intent) {
            appList = new ArrayList<>();
            desktopApplications = new ArrayList<>();
            this.context = context;
            packageManager = context.getPackageManager();
            this.totalCount = intent.getIntExtra("total", 20);
            isUsedTime = intent.getBooleanExtra("isUsedTime", false);
            Log.i(TAG, "RecentAppPresentFactory: total:" + totalCount);
        }

        public static Bitmap drawableToBitmap(Drawable drawable) {
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                    drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            return bitmap;
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
        private void getHistoryApps(Context context) {
            Calendar calendar = Calendar.getInstance();
            long endTime = calendar.getTimeInMillis();

            calendar.add(Calendar.DAY_OF_MONTH, -1);
            long startTime = calendar.getTimeInMillis();
            UsageStatsManager mUsageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            desktopApplications.clear();
            if (mUsageStatsManager != null) {
                extractList(endTime, startTime, mUsageStatsManager);
            }
        }

        private void extractList(long endTime, long startTime, UsageStatsManager mUsageStatsManager) {
            List<UsageStats> usageStatsList = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);
            if (usageStatsList != null && !usageStatsList.isEmpty()) {
                Field mLaunchCount = null;
                try {
                    mLaunchCount = UsageStats.class.getDeclaredField("mAppLaunchCount");
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
                Field finalMLaunchCount = mLaunchCount;
                Collections.sort(usageStatsList, (o1, o2) -> {
                    if (!isUsedTime) {
                        if (finalMLaunchCount != null) {
                            try {
                                int compare = Integer.compare(finalMLaunchCount.getInt(o2), finalMLaunchCount.getInt(o1));
                                if (compare == 0) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                        return Long.compare(o2.getTotalTimeVisible(), o1.getTotalTimeVisible());
                                    } else {
                                        return Long.compare(o2.getLastTimeUsed(), o1.getLastTimeUsed());
                                    }
                                }
                                return compare;
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        return Long.compare(o2.getTotalTimeVisible(), o1.getTotalTimeVisible());
                    } else {
                        return Long.compare(o2.getLastTimeUsed(), o1.getLastTimeUsed());
                    }
                });
                int count = 0;
                for (UsageStats usageStats : usageStatsList) {
                    if (count >= totalCount) {
                        break;
                    }
                    if (finalMLaunchCount != null) {
                        try {
                            Log.i(TAG, "getHistoryApps: " + usageStats.getPackageName() + " " + finalMLaunchCount.getInt(usageStats));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.i(TAG, "getHistoryApps: " + usageStats.getPackageName() + " " + usageStats.getTotalTimeVisible());
                    }
                    for (LabelItem p : appList) {//确保此应用在app settingItems 列表中
                        if (p instanceof Application) {
                            Application application = (Application) p;
                            if (usageStats.getPackageName().equals(application.getPackageName())) {
                                DesktopApplication e = new DesktopApplication(application.getImage(), application.getLabel(), usageStats.getPackageName(), null, 1);
                                if (isContainer(desktopApplications, application)) {//检查是否已经添加过
                                    count++;
                                    desktopApplications.add(e);
                                }
                            }
                        }
                        if (count >= totalCount) {
                            break;
                        }
                    }

                }
            }
        }

        /**
         * 找到了返回false，没找到返回true
         *
         * @param desktopApplications app settingItems
         * @param imageItem           resolve
         * @return 找到了返回false，没找到返回true
         */
        private boolean isContainer(List<? extends Application> desktopApplications, Application imageItem) {
            for (Application desktopApplication : desktopApplications) {
                if (desktopApplication.getPackageName().equals(imageItem.getPackageName())) {
                    return false;
                }
            }
            return true;
        }

        private void getAppList(Context context) {
            appList.clear();
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> resolve = context.getPackageManager().queryIntentActivities(mainIntent, 0);
            for (ResolveInfo r : resolve) {
                try {
                    Drawable applicationIcon = packageManager.getApplicationIcon(r.activityInfo.packageName);
                    Bitmap bitmap = drawableToBitmap(applicationIcon);
                    CharSequence applicationLabel = packageManager.getApplicationLabel(r.activityInfo.applicationInfo);
                    appList.add(new Application(bitmap, applicationLabel.toString(), r.activityInfo.packageName));
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onCreate() {
            Log.d(TAG, "onCreate() called");
//            getHistoryApps(context);
        }

        @Override
        public void onDataSetChanged() {
            getAppList(context);
            getHistoryApps(context);
        }

        @Override
        public void onDestroy() {
            Log.d(TAG, "onDestroy() called");
        }

        @Override
        public int getCount() {
            Log.d(TAG, "getCount() called");
            return desktopApplications.size();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.layout_application_recent_app_widget);
            DesktopApplication desktopApplication = desktopApplications.get(position);
            Bitmap image = desktopApplication.getImage();
            remoteViews.setImageViewBitmap(R.id.imageView, image);
            remoteViews.setTextViewText(R.id.textView, desktopApplication.getLabel());

            Bundle extras = new Bundle();
            extras.putInt(RecentAAppWidget.extra_index, position);
            extras.putString(RecentAAppWidget.packageName, desktopApplication.getPackageName());
            Intent fillInIntent = new Intent();
            fillInIntent.putExtras(extras);
            remoteViews.setOnClickFillInIntent(R.id.item, fillInIntent);
            return remoteViews;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }
    }
}
