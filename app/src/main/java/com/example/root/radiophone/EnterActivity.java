package com.example.root.radiophone;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EnterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter);
        Button button= (Button) findViewById(R.id.submitbutton);
        final EditText editText = (EditText) findViewById(R.id.input);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!editText.getText().toString().matches(""))
                {
                    Intent intent = new Intent(v.getContext(), DevicesList.class);
                    Intent intent1 = new Intent(getBaseContext(), EnterActivity.class);
                    intent.putExtra("NickName", editText.getText().toString());

                    startActivity(intent);
                    startActivityForResult(intent, 1);
                    finish();
                }
                else
                {
                    Toast.makeText(v.getContext(), "Enter your nickname, stooopid", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
