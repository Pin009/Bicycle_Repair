package com.example.bicyclecatalog;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddEditBicycle extends AppCompatActivity {

    DbHelper db;
    EditText edName=null, edDescription=null;
    Button btnSave;
    String rowId, mode, name, description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_bicycle);

        db = new DbHelper(this);

        edName = (EditText) findViewById(R.id.edName);
        edDescription = (EditText) findViewById(R.id.edDescription);
        btnSave = (Button) findViewById(R.id.btnSave);

        Intent intent = this.getIntent();
        this.mode = intent.getStringExtra("mode");

        if (!mode.equals("add")) {
            this.rowId = intent.getStringExtra("rowId");

            this.name = intent.getStringExtra("name");
            this.description = intent.getStringExtra("description");

            edName.setText(name);
            edDescription.setText(description);
        }
    }

    //обработчик кнопки добавления
    public void onClickBtn(View v) {

        if (v.getId()!=R.id.btnSave) return;

        String query = null, message;

        if (mode.equals("add")) {
            query = "insert into Инструкция(наименование,описание) values('" + edName.getText() + "','" +
                    edDescription.getText() + "');";
            message="Не возможно добавить данные в БД, повторите попытку!";
        }
        else {
            query = "update Инструкция set наименование='" + edName.getText() + "', описание='" +
                    edDescription.getText() + "' where _id=" + rowId + ";";
            message="Не возможно обновить данные в БД, повторите попытку!";
        }

        if (!db.sqlExec(query)) {
            Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}