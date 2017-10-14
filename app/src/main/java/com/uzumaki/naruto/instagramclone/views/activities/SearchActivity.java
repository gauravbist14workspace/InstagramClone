package com.uzumaki.naruto.instagramclone.views.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.uzumaki.naruto.instagramclone.R;
import com.uzumaki.naruto.instagramclone.models.UserPrivate;
import com.uzumaki.naruto.instagramclone.utils.BottomNavigationViewHelper;
import com.uzumaki.naruto.instagramclone.utils.Constants;
import com.uzumaki.naruto.instagramclone.views.adapters.UserListAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Gaurav Bist on 04-07-2017.
 */

public class SearchActivity extends AppCompatActivity {
    private static final String TAG = "SearchActivity";
    private static final int ACTVITY_NUMBER = 1;
    private Context mContext;

    // widgets
    EditText mSearchParams;
    ListView listView;

    // vars
    private List<UserPrivate> mUserList;
    private UserListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        bindViews();
        init();

        hideSoftKeyboard();
        setupBottomNavigationView();
    }

    private void bindViews() {
        mSearchParams = (EditText) findViewById(R.id.search);
        listView = (ListView) findViewById(R.id.listView);
    }

    private void init() {
        mContext = this;
        mUserList = new ArrayList<>();

        adapter = new UserListAdapter(mContext, R.layout.item_user_list, mUserList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                Log.d(TAG, "onItemClick: Selected user: " + mUserList.get(pos).toString());

                // navigating to profile activity
                startActivity(new Intent(SearchActivity.this, ProfileActivity.class)
                        .putExtra(getString(R.string.calling_activity), getString(R.string.search_activity))
                        .putExtra(getString(R.string.intent_user), mUserList.get(pos)));
            }
        });

        mSearchParams.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String userName = mSearchParams.getText().toString().toLowerCase(Locale.getDefault());
                searchForMatch(userName);
            }
        });
    }

    private void searchForMatch(String keyWord) {
        mUserList.clear();

        if (keyWord.length() == 0) {

        } else {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

            Query query = reference.child(Constants.USER_PRIVATE_INFO)
                    .orderByChild(getString(R.string.firebaseUsername))
                    .equalTo(keyWord);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        Log.d(TAG, "onDataChange: Username found." + singleSnapshot.getValue(UserPrivate.class).toString());

                        mUserList.add(singleSnapshot.getValue(UserPrivate.class));
                    }
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void hideSoftKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    /* BottomNavigationView Setup */
    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: Setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, this, bottomNavigationViewEx);

        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTVITY_NUMBER);
        menuItem.setChecked(true);
    }
}
