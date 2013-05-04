/* This file is part of the PlusMinusTimesDivide product, copyright Eric Lavarde <android@lavar.de>.
 * Trademarks (the product name, in English and translated, artwork like icons, and the domains
 * lavar.de and lavarde.eu - also reversed as package name) are properties of the copyright owner
 * and shall not be used by anyone else without explicit authorisation.

The code itself is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

PlusMinusTimesDivide is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with PlusMinusTimesDivide.  If not, see <http://www.gnu.org/licenses/>.
*/
/* Most of the code has been shamelessly stolen from the Notepad tutorial 
 * under http://developer.android.com/training/notepad/index.html
 * Copyright (C) 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package eu.lavarde.pmtd;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * Simple challenges database access helper class. Defines the basic CRUD operations
 * for the challenges database, and gives the ability to list all challenges as well as
 * retrieve, delete or modify a specific challenge.
 */
public class ProgressDbAdapter {

		// TODO found + missed = rounds - hence duplicate data actually... How to handle?
    public static final String KEY_ID = "_id"; // Long/Integer - a unique ID
    public static final String KEY_CHALLENGE = "challenge_id"; // Long/Integer - ID of challenge played
    public static final String KEY_USER = "user_id"; // Long/Integer - ID of user who played the challenge
    public static final String KEY_TRIES = "tries"; // Integer - tries needed to finish the challenge
    public static final String KEY_DURATION = "duration"; // Time (TODO correct type) - time needed to finish the challenge
    public static final String KEY_FOUND = "found"; // Integer - number of correctly found results
    public static final String KEY_MISSED = "missed"; // Integer - number of not found results
    public static final String KEY_SCORE = "score"; // Integer - score reached
    public static final String KEY_WHEN = "when"; // TimeDate (TODO correct type) - when was the challenge played?
    public static final String KEY_SUMMARY = "summary"; // Integer - how many challenges are summarised in this entry?
    	// TODO write the summary function to compress multiple old entries into one entry
    
    private PmtdDbHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */

    private static final String DATABASE_TABLE = "progress";

    private final Context mCtx;

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public ProgressDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the challenges database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public ProgressDbAdapter open() throws SQLException {
        mDbHelper = new PmtdDbHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    public long createEntry(String name, long userId, int rounds, int operation, int max,
    		boolean isMaxSmall, int places, int table, boolean bool1, boolean bool2) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_USER, userId);
        initialValues.put(KEY_ROUNDS, rounds);
        initialValues.put(KEY_OPERATION, operation);
        initialValues.put(KEY_MAX, max);
        initialValues.put(KEY_WHICHMAX, isMaxSmall);
        initialValues.put(KEY_PLACES, places);
        initialValues.put(KEY_TABLE, table);
        initialValues.put(KEY_BOOL1, bool1);
        initialValues.put(KEY_BOOL2, bool2);

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the challenge with the given rowId
     * 
     * @param rowId id of challenge to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteChallenge(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all challenges in the database
     * 
     * @return Cursor over all challenges
     */
    public Cursor fetchAllChallenges() { // TODO: could the function return a cursor of ChallengePrefs?

        return mDb.query(DATABASE_TABLE, new String[] {KEY_ID, KEY_NAME, KEY_ROUNDS, KEY_OPERATION,
        		KEY_MAX, KEY_WHICHMAX, KEY_PLACES, KEY_TABLE, KEY_BOOL1, KEY_BOOL2},
        		null, null, null, null, KEY_NAME);
    }
    
    /**
     * Return a Cursor over the list of all challenges in the database created by a given user
     * @param userId the id of the user
     * @return Cursor over all challenges of the user
     */
    public Cursor fetchAllChallenges(long userId) { // TODO: could the function return a cursor of ChallengePrefs?

        return mDb.query(DATABASE_TABLE, new String[] {KEY_ID, KEY_NAME, KEY_ROUNDS, KEY_OPERATION,
        		KEY_MAX, KEY_WHICHMAX, KEY_PLACES, KEY_TABLE, KEY_BOOL1, KEY_BOOL2},
        		KEY_USER + "=" + userId, null, null, null, KEY_NAME);
    }
    
    /**
     * Return a Cursor positioned at the challenge that matches the given rowId
     * 
     * @param rowId id of challenge to retrieve
     * @return Cursor positioned to matching challenge, if found
     * @throws SQLException if challenge could not be found/retrieved
     */
    public Cursor fetchChallenge(long rowId) throws SQLException { // TODO: could the function return a cursor of ChallengePrefs?

        Cursor mCursor =
            mDb.query(true, DATABASE_TABLE, new String[] {KEY_ID, KEY_NAME, KEY_ROUNDS, KEY_OPERATION,
            		KEY_MAX, KEY_WHICHMAX, KEY_PLACES, KEY_TABLE, KEY_BOOL1, KEY_BOOL2},
            		KEY_ID + "=" + rowId, null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    public ChallengePrefs fetchChallengePrefs(long rowId) throws SQLException {

        Cursor mCursor =
            mDb.query(true, DATABASE_TABLE, new String[] {KEY_ID, KEY_NAME, KEY_ROUNDS, KEY_OPERATION,
            		KEY_MAX, KEY_WHICHMAX, KEY_PLACES, KEY_TABLE, KEY_BOOL1, KEY_BOOL2},
            		KEY_ID + "=" + rowId, null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
            ChallengePrefs chlg = new ChallengePrefs();
            chlg.setId(mCursor.getLong(mCursor.getColumnIndex(ProgressDbAdapter.KEY_ID)));
            chlg.setName(mCursor.getString(mCursor.getColumnIndex(ProgressDbAdapter.KEY_NAME)));
            chlg.setRounds(mCursor.getInt(mCursor.getColumnIndex(ProgressDbAdapter.KEY_ROUNDS)));
            chlg.setOperation(mCursor.getInt(mCursor.getColumnIndex(ProgressDbAdapter.KEY_OPERATION)));
            chlg.setMaxValue(mCursor.getInt(mCursor.getColumnIndex(ProgressDbAdapter.KEY_MAX)));
            chlg.setSmallNumbersMax(mCursor.getInt(mCursor.getColumnIndex(ProgressDbAdapter.KEY_WHICHMAX))>0);
            chlg.setDecimalPlaces(mCursor.getInt(mCursor.getColumnIndex(ProgressDbAdapter.KEY_PLACES)));
            chlg.setTableTraining(mCursor.getInt(mCursor.getColumnIndex(ProgressDbAdapter.KEY_TABLE)));
            chlg.setBool1(mCursor.getInt(mCursor.getColumnIndex(ProgressDbAdapter.KEY_BOOL1))>0);
            chlg.setBool2(mCursor.getInt(mCursor.getColumnIndex(ProgressDbAdapter.KEY_BOOL2))>0);
            return chlg;
        }
        return null;

    }
   /**
     * Update the challenge using the details provided. The challenge to be updated is
     * specified using the rowId, and it is altered to use the title values passed in
     * It is noted that, once created, a challenge can't be modified (only its name).
     * 
     * @param rowId id of challenge to update
     * @param title value to set challenge title to
     * @param body value to set challenge body to
     * @return true if the challenge was successfully updated, false otherwise
     */
    public boolean updateChallenge(long rowId, String name) {
        ContentValues args = new ContentValues();
        args.put(KEY_NAME, name);

        return mDb.update(DATABASE_TABLE, args, KEY_ID + "=" + rowId, null) > 0;
    }
}
