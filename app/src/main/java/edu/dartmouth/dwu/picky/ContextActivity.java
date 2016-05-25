package edu.dartmouth.dwu.picky;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ContextActivity extends AppCompatActivity {
    private static final String TAG = "Picky";

    private static final String contextValueDefault = "Type";
    private static final String contextMatchesDefault = "Comparator";
    private static final String contextMatchesValueDefault = "Value";
    private static final String actionMessageDefault = "Value";
    private static final String actionUidDefault = "App";
    private static final String actionActionDefault = "Action";

    private Spinner sContextValue, sContextMatches, sContextMatchesValue;
    private String[] contextValuesArray = {contextValueDefault, "Wifi state", "Wifi ssid", "Bluetooth state"};
    private String[] contextMatchesArray = {contextMatchesDefault, "Status", "Matches"};
    private String[] contextMatchesValueArray = {contextMatchesValueDefault, "On", "Off", "Enter value"};

    private Spinner sActionMessage, sActionUid, sActionAction;
    private ArrayList<String> actionMessagesArray = new ArrayList<>();
    private ArrayList<String> actionUidsArray = new ArrayList<>();
    private String[] actionActionsArray = {actionActionDefault, "Block", "Unblock", "Modify", "Unmodify"};

    private ArrayAdapter<String> contextDataAdapter;
    private final int positionOfCustomValue = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_context);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveRule();
            }
        });
        fab.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimaryDark));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        addSpinnerItems();
    }

    public void addSpinnerItems() {
        sContextValue = (Spinner) findViewById(R.id.spinnerContextValue);
        sContextMatches = (Spinner) findViewById(R.id.spinnerContextMatches);
        sContextMatchesValue = (Spinner) findViewById(R.id.spinnerContextMatchesValue);
        sActionMessage = (Spinner) findViewById(R.id.spinnerMessage);
        sActionUid = (Spinner) findViewById(R.id.spinnerUid);
        sActionAction = (Spinner) findViewById(R.id.spinnerAction);

        // context values
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                R.layout.spiner_tv_layout, contextValuesArray);
        dataAdapter.setDropDownViewResource(R.layout.spiner_tv_layout);
        sContextValue.setAdapter(dataAdapter);

        // context matches
        ArrayAdapter<String> dataAdapter2  = new ArrayAdapter<>(this,
                R.layout.spiner_tv_layout, contextMatchesArray);
        dataAdapter2.setDropDownViewResource(R.layout.spiner_tv_layout);
        sContextMatches.setAdapter(dataAdapter2);

        // context matches values
        contextDataAdapter = new ArrayAdapter<>(this,
                R.layout.spiner_tv_layout, contextMatchesValueArray);
        contextDataAdapter.setDropDownViewResource(R.layout.spiner_tv_layout);
        sContextMatchesValue.setAdapter(contextDataAdapter);

        // action messages
        actionMessagesArray.add(actionMessageDefault);
        for (PolicyMessage pm : Policy.messages) {
            actionMessagesArray.add(pm.displayMessage);
        }
        ArrayAdapter<String> dataAdapter4 = new ArrayAdapter<>(this,
                R.layout.spiner_tv_layout, actionMessagesArray);
        dataAdapter4.setDropDownViewResource(R.layout.spiner_tv_layout);
        sActionMessage.setAdapter(dataAdapter4);

        // action uids
        actionUidsArray.add(actionUidDefault);
        actionUidsArray.addAll(MainActivity.allApps);
        ArrayAdapter<String> dataAdapter5 = new ArrayAdapter<>(this,
                R.layout.spiner_tv_layout, actionUidsArray);
        dataAdapter5.setDropDownViewResource(R.layout.spiner_tv_layout);
        sActionUid.setAdapter(dataAdapter5);

        // action actions
        ArrayAdapter<String> dataAdapter6 = new ArrayAdapter<>(this,
                R.layout.spiner_tv_layout, actionActionsArray);
        dataAdapter6.setDropDownViewResource(R.layout.spiner_tv_layout);
        sActionAction.setAdapter(dataAdapter6);

        // custom string listener
        sContextMatchesValue.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == positionOfCustomValue) {
                    promptUserForString(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    public void promptUserForString(final int position) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle("Custom value:");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setGravity(Gravity.CENTER);
        dialog.setView(input);

        dialog.setPositiveButton("enter", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Spinner s = (Spinner) findViewById(R.id.spinnerContextMatchesValue);
                String val = input.getText().toString();
                if (val == null) {
                    val = "";
                }
                contextMatchesValueArray[positionOfCustomValue] = val;
                contextDataAdapter.notifyDataSetChanged();
                s.setSelection(positionOfCustomValue);
            }
        });
        dialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        dialog.show();
    }

    public void saveRule() {
        String contextType = sContextValue.getSelectedItem().toString();
        String contextComparator = sContextMatches.getSelectedItem().toString();
        String contextValue = sContextMatchesValue.getSelectedItem().toString();
        String actionValue = sActionMessage.getSelectedItem().toString();
        String actionApp = sActionUid.getSelectedItem().toString();
        String actionAction = sActionAction.getSelectedItem().toString();

        String ruleString = "When " + contextType.toLowerCase() + " " + contextComparator.toLowerCase() + " \"" +
                contextValue + "\", " + actionAction.toLowerCase() + " <" + actionValue + "> for app " + actionApp ;
        Log.i(TAG, "saveRule: " + ruleString);

        if (validateRule(contextType, contextComparator, contextValue,
                actionValue, actionApp, actionAction) == false) {
            return;
        }

        int intContext = 0, intContextIntOrString = 0, intContextIntValue = 0;
        String stringContextStringValue = "";
        if (contextType.equals("Wifi state")) {
            intContext = Policy.CONTEXT_WIFI_STATE;
            intContextIntOrString = Policy.CONTEXT_TYPE_INT;
        }
        if (contextType.equals("Wifi ssid")) {
            intContext = Policy.CONTEXT_WIFI_SSID;
            intContextIntOrString = Policy.CONTEXT_TYPE_STRING;
        }
        if (contextType.equals("Bluetooth state")) {
            intContext = Policy.CONTEXT_BT_STATE;
            intContextIntOrString = Policy.CONTEXT_TYPE_INT;
        }

        if (contextValue.equals("On")) {
            intContextIntValue = Policy.CONTEXT_STATE_ON;
        } else if (contextValue.equals("Off")) {
            intContextIntValue = Policy.CONTEXT_STATE_OFF;
        } else {
            stringContextStringValue = contextValue;
        }

        int intApp = MainActivity.nameToUid.get(actionApp);
        int intActionAction = -1;
        String stringActionMessage = "";

        for (int i=0; i<Policy.messages.length; i++) {
            PolicyMessage pm = Policy.messages[i];
            if (actionValue.equals(pm.displayMessage)) {
                stringActionMessage = pm.filterMessage;
            }
        }
        if (actionAction.equals("Block")) {intActionAction = Policy.BLOCK_ACTION;}
        if (actionAction.equals("Unblock")) {intActionAction = Policy.UNBLOCK_ACTION;}
        if (actionAction.equals("Modify")) {intActionAction = Policy.MODIFY_ACTION;}
        if (actionAction.equals("Unmodify")) {intActionAction = Policy.UNMODIFY_ACTION;}

        FilterLine filter = new FilterLine(intApp, intActionAction, stringActionMessage, "",
                intContext, intContextIntOrString, intContextIntValue, stringContextStringValue);
        Policy.setContextFilterLine(filter);

        MainActivity.savedRules.put(ruleString, filter);
        CustomTabFragment.adapter.notifyDataSetChanged();

        // go back to Custom Tab view
        finish();
    }

    // returns false on invalid rule
    private boolean validateRule(String contextType, String contextComparator, String contextValue,
                                        String actionValue, String actionApp, String actionAction) {
        boolean ret = true;
        StringBuilder retString = new StringBuilder();

        if (contextType.equals(contextValueDefault)) {
            retString.append("Context type cannot be set to default value.\n");
            ret = false;
        }
        if (contextComparator.equals(contextMatchesDefault)) {
            retString.append("Context comparator cannot be set to default value.\n");
            ret = false;
        }
        if (contextValue.equals(contextMatchesValueDefault)) {
            retString.append("Context value cannot be set to default value.\n");
            ret = false;
        }
        if (actionValue.equals(actionMessageDefault)) {
            retString.append("Action value cannot be set to default value.\n");
            ret = false;
        }
        if (actionApp.equals(actionUidDefault)) {
            retString.append("Action app cannot be set to default value.\n");
            ret = false;
        }
        if (actionAction.equals(actionActionDefault)) {
            retString.append("Action value cannot be set to default value.\n");
            ret = false;
        }

        // type and comparator
        if (contextType.equals("Wifi state") && contextComparator.equals("Matches")) {
            retString.append("Wifi state must correspond to \"Status\" comparator.\n");
            ret = false;
        }
        if (contextType.equals("Bluetooth state") && contextComparator.equals("Matches")) {
            retString.append("Bluetooth state must correspond to \"Status\" comparator.\n");
            ret = false;
        }
        if (contextType.equals("Wifi ssid") && contextComparator.equals("Status")) {
            retString.append("Wifi ssid must correspond to \"Matches\" comparator.\n");
            ret = false;
        }

        // type and value
        if (contextType.equals("Wifi state") && !contextValue.equals("On") && !contextValue.equals("Off")) {
            retString.append("Wifi state must correspond to On or Off value.\n");
            ret = false;
        }
        if (contextType.equals("Bluetooth state") && !contextValue.equals("On") && !contextValue.equals("Off")) {
            retString.append("Bluetooth state must correspond to On or Off value.\n");
            ret = false;
        }
        if (contextType.equals("Wifi ssid") && (contextValue.equals("On") || contextValue.equals("Off"))) {
            retString.append("Wifi ssid must correspond to custom value (Enter value).\n");
            ret = false;
        }

        // comparator and value
        if (contextComparator.equals("Status") && !(contextValue.equals("On") || contextValue.equals("Off"))) {
            retString.append("Status comparator must correspond to On or Off value.\n");
            ret = false;
        }
        if (contextComparator.equals("Matches") && (contextValue.equals("On") || contextValue.equals("Off"))) {
            retString.append("Matches comparator must correspond to custom value (Enter value).\n");
            ret = false;
        }

        if (contextType.equals("Wifi ssid") && contextType.length() > 32) {
            retString.append("Wifi ssid max length is specified to be 32 characters.\n");
            ret = false;
        }

        if (ret == false) {
            Toast.makeText(this, retString.toString(), Toast.LENGTH_LONG).show();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                MainActivity.viewPager.setCurrentItem(1, false);
                finish();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        MainActivity.viewPager.setCurrentItem(1,false);
        finish();
    }

}