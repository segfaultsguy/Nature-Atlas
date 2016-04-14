package com.example.brandan.natureatlas;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;


/*
    What this class does:
    This class is the view for the login / register page. It's still under construction...
*/
public class loginregister extends AppCompatActivity {
    JSONArray loginArray = new JSONArray();
    //JSONArray registerArray = new JSONArray();
    RadioButton register;
    RadioButton signIn;
    EditText email, confirmPass, name, pass;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginregister);

        pass = (EditText)findViewById(R.id.passWord);
        name = (EditText)findViewById(R.id.name);
        confirmPass= (EditText) findViewById(R.id.confirmPass);
        email= (EditText) findViewById(R.id.email);
        signIn = (RadioButton) findViewById(R.id.signInButton);
        register = (RadioButton) findViewById(R.id.registerButton);

        final Button button = (Button)findViewById(R.id.button);



        signIn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    register.setChecked(false);
                    button.setText("Log In");
                    name.setVisibility(View.INVISIBLE);
                    confirmPass.setVisibility(View.INVISIBLE);
                }
                Log.d("SignIn: ", Boolean.toString(signIn.isChecked()));
                Log.d("Register: ", Boolean.toString(register.isChecked()));
            }
        });

        register.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                if (b)
                {
                    signIn.setChecked(false);
                    name.setVisibility(View.VISIBLE);
                    confirmPass.setVisibility(View.VISIBLE);
                    button.setText("Submit");
                    Log.d("SignIn: ", Boolean.toString(signIn.isChecked()));
                    Log.d("Register: ", Boolean.toString(register.isChecked()));
                }

            }
        });


    }

    public void Submit(View view)
    {



        Log.d("Email: ", email.getText().toString());

            if (signIn.isChecked())
            {
               LoginRetriever lR = new LoginRetriever(email.getText().toString(), pass.getText().toString(), this);
                loginArray = lR.GetJSON();
                    for(int i = 0; i < loginArray.length(); i++)
                    {
                        try {
                            Log.d("loginArray", loginArray.getString(i));
                        }
                        catch(JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                    try
                    {
                        if (loginArray.getString(0).equals("login successful"))
                        {
                            Globals g = Globals.getInstance();
                            g.SetSignedIn(true);
                            Log.d("Name", loginArray.getString(1));
                            SharedPreferences.Editor editPrefs = getSharedPreferences("userName", MODE_PRIVATE).edit();
                            editPrefs.putString("usersName", loginArray.getString(1));
                            editPrefs.putString("email", email.getText().toString());
                            editPrefs.apply();
                            Toast.makeText(getApplicationContext(), "Successfully logged in.", Toast.LENGTH_SHORT).show();

                            this.finish();

                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "Login was not successful.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    catch(JSONException je)
                    {
                        je.printStackTrace();
                    }

            }
        if(register.isChecked())
        {
            if (pass.getText().toString().equals(confirmPass.getText().toString()))
            {

                RegisterRetriever rr = new RegisterRetriever(email.getText().toString(), confirmPass.getText().toString(), name.getText().toString(), this);


            }
            else
            {
                Toast.makeText(getBaseContext(), "The passwords do not match.", Toast.LENGTH_SHORT).show();
            }
        }


    }
}
