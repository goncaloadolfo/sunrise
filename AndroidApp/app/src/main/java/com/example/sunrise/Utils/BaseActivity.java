package com.example.sunrise.Utils;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;

import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.sunrise.AdvancedSearch;
import com.example.sunrise.BeachListActivity.BeachList;
import com.example.sunrise.Login;
import com.example.sunrise.Profile;
import com.example.sunrise.R;
import com.example.sunrise.SuggestionsActivity.Suggestions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class BaseActivity extends AppCompatActivity {

    private boolean logoutConfirmation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        invalidateOptionsMenu();
        logoutConfirmation = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return createMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        adaptMenuItems(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        switch (item.getItemId()){

            // login item
            case R.id.menuItemLogin:
                intent = new Intent(this, Login.class);
                startActivity(intent);
                return true;

            // beach list item
            case R.id.menuItemBeachList:
                goToBeachList();
                return true;

            // logout item
            case R.id.menuItemLogout:

                if (logoutConfirmation) {
                    FirebaseAuth.getInstance().signOut();
                    invalidateOptionsMenu();
                    goToBeachList();
                }

                else{
                    Toast toast = Toast.makeText(this, getString(R.string.logoutConfirmation), Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP|Gravity.LEFT, 0, 0);
                    toast.show();
                    logoutConfirmation = true;
                }

                return true;

            case R.id.menuItemAdvancedSearch:
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    intent = new Intent(this, AdvancedSearch.class);
                    startActivity(intent);
                }

                else{
                    Toast.makeText(this, getString(R.string.loginNeeded), Toast.LENGTH_LONG).show();
                }
                return true;

            case R.id.menuItemProfile:
                intent = new Intent(this, Profile.class);
                intent.putExtra("email", FirebaseAuth.getInstance().getCurrentUser().getEmail());
                startActivity(intent);
                return true;

            case R.id.menuItemSuggestions:
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    intent = new Intent(this, Suggestions.class);
                    startActivity(intent);
                }

                else{
                    Toast.makeText(this, getString(R.string.loginNeeded), Toast.LENGTH_LONG).show();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressLint("RestrictedApi")
    public boolean createMenu(Menu menu){
        // create menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        // set visibilities according to login status
        adaptMenuItems(menu);

        // code needed to set icons visible
        if (menu instanceof MenuBuilder){
            MenuBuilder menuBuilder = (MenuBuilder) menu;
            menuBuilder.setOptionalIconsVisible(true);
        }

        return true;
    }

    private void adaptMenuItems(Menu menu){
        // adapt menu to login status
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // online user
        if (currentUser != null){
            // set visible false to login item
            setItemVisibility(menu, R.id.menuItemLogin, false);

            // set visible true on profile and logout items
            setItemVisibility(menu, R.id.menuItemProfile, true);
            setItemVisibility(menu, R.id.menuItemLogout, true);
        }

        // non online user
        else{
            // set visible false on profile and logout items
            setItemVisibility(menu, R.id.menuItemProfile, false);
            setItemVisibility(menu, R.id.menuItemLogout, false);

            // set visible true to login item
            setItemVisibility(menu, R.id.menuItemLogin, true);
        }
    }

    /**
     * Changes menu item visibility
     * @param menu
     * @param itemId
     * @param visibility
     */
    private void setItemVisibility(Menu menu, int itemId, boolean visibility){
        MenuItem item = menu.findItem(itemId);
        item.setVisible(visibility);
    }

    private void goToBeachList(){
        Intent intent = new Intent(this, BeachList.class);
        startActivity(intent);
    }

}
