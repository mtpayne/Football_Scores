/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package barqsoft.footballscores.service;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utility;
import barqsoft.footballscores.scoresAdapter;

/**
 * RemoteViewsService controlling the cursor being shown in the scrollable football scores widget.
 * Code is slightly modified version of
 * https://github.com/udacity/Advanced_Android_Development/blob/1086d9b1c447017b86806f7cf0a401cfa241ea73/app/src/main/java/com/example/android/sunshine/app/widget/DetailWidgetRemoteViewsService.java
 */
public class ScoresWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        return new RemoteViewsFactory() {

            private Cursor cursor = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (cursor != null) {
                    cursor.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // cursor. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                Uri uri = DatabaseContract.scores_table.buildScoreWithDate();
                cursor =  getContentResolver().query(uri,
                        null,
                        null,
                        new String[]{Utility.getCurrentDate()},
                        null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (cursor != null) {
                    cursor.close();
                    cursor = null;
                }
            }

            @Override
            public int getCount() {
                return cursor == null ? 0 : cursor.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        cursor == null || !cursor.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views =
                        new RemoteViews(getPackageName(), R.layout.widget_scores_list_item);

                views.setTextViewText(R.id.home_name, cursor.getString(scoresAdapter.COL_HOME));
                views.setTextViewText(R.id.away_name, cursor.getString(scoresAdapter.COL_AWAY));
                views.setTextViewText(R.id.data_textview,
                        cursor.getString(scoresAdapter.COL_MATCHTIME));
                views.setTextViewText(R.id.score_textview,
                        Utility.getScores(cursor.getInt(scoresAdapter.COL_HOME_GOALS),
                                cursor.getInt(scoresAdapter.COL_AWAY_GOALS)));
                views.setImageViewResource(R.id.home_crest,
                        Utility.getTeamCrestByTeamName(cursor.getString(scoresAdapter.COL_HOME)));
                views.setImageViewResource(R.id.away_crest,
                        Utility.getTeamCrestByTeamName(cursor.getString(scoresAdapter.COL_AWAY)));

                return views;
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
                return true;
            }

        };
    }

}