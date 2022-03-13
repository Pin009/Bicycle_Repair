package com.example.bicyclecatalog;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    DbHelper db;
    ListView lv;
    Button btnAdd;
    EditText edFind;
    String id;
    int positionSel;

    Cursor cr; //курсор на данные
    SimpleCursorAdapter sCr; //адаптер курсора для ListView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//ищем наши компоненты - списки вывода
        lv = (ListView) findViewById(R.id.lvList);
        edFind=(EditText) findViewById(R.id.edFind);
        btnAdd=(Button)  findViewById(R.id.btnAdd);

        //создаем обьект нашего класса для работы с бд
        db = new DbHelper(this);


        edFind.addTextChangedListener ( new TextWatcher() {

            public void afterTextChanged ( Editable s ) {
                //что-то делаем после изменения
            }

            public void beforeTextChanged ( CharSequence s, int start, int count, int after ) {
                //что-то делаем до изменения
            }

            //пока текст меняется - загружаем данные
            public void onTextChanged ( CharSequence s, int start, int before, int count ) {
                loadData(s.toString());
            }
        });

        //добавляем контекстное меню к списку
        registerForContextMenu(lv);
    }

    private void loadData(String strLike)
    {
        try {
            //получим курсор на выборку
            if (strLike == null || strLike.equals(""))
                cr = db.query("select _id, наименование, описание, ||', '||ifnull(описание,' ') as param2 from 'Инструкция'");
            else
                cr = db.query("select _id, наименование,  описание||', '||ifnull(описание,' ') as param2 from " +
                              "'Инструкция' where наименование LIKE '%\"+strLike+\"%'\"");

            //если курсор открыт
            if (cr != null) { //и есть в нем данные
                if (cr.getCount() > 0) {
                    cr.moveToFirst(); //передвинем указатель в начало, так как он в конце
                    //создадим адаптер данных из заголока и подзаголовка пункта списка
                    sCr = new SimpleCursorAdapter(this,
                            android.R.layout.simple_list_item_2,
                            cr, new String[]{"наименование", "param2"},
                            new int[]{android.R.id.text1, android.R.id.text2}, 1);
                    lv.setAdapter(sCr); //зададим этот адаптер для списка
                    //установим события при клике на пункт списка
                    lv.setOnItemClickListener(new ListView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapter_view, View v, int i, long l) {
                            positionSel=i;
                            //получаем необходимые данные из курсора
                            id = String.valueOf(l);
                            lv.showContextMenu();
                        }
                    });
                } //if cr1
                else
                    lv.setAdapter(null); //если данных в курсоре нет, то очистим список
            }
        }
        catch(Exception e)
        {
            Toast toast = new Toast(getApplicationContext());
            toast.setGravity(Gravity.BOTTOM, 0, 0);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setText(e.getMessage());
            toast.show();

        }
    }

    //перегруженный метод для создания выпадающего меню
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(Menu.NONE, 1, Menu.NONE, "Добавить");
        menu.add(Menu.NONE, 2, Menu.NONE, "Редактировать");
        menu.add(Menu.NONE, 3, Menu.NONE, "Удалить");
    }

    //обработчик событий выбора пунктов меню
    public boolean onContextItemSelected(MenuItem item) {
        // получаем из пункта контекстного меню данные по пункту списка
        AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch (item.getItemId())
        {
            case 1:
                this.onClickBtn(btnAdd);
                break;

            case 2:
                cr.moveToPosition(positionSel); //передвинем курсор на данные которые выбраны в списке
                //создадим намерение, будем переходить с этой активности на активность
                Intent intent = new Intent(MainActivity.this, AddEditBicycle.class);
                intent.putExtra("mode", "edit");
                //получаем необходимые данные из курсора
                id = cr.getString(cr.getColumnIndexOrThrow("_id"));
                String name = cr.getString(cr.getColumnIndexOrThrow("наименование"));
                String description = cr.getString(cr.getColumnIndexOrThrow("описание"));

                //добавляем данные в экстраДанные намерения, что бы передать их другой активности
                intent.putExtra("rowId", id);
                intent.putExtra("name", name);
                intent.putExtra("description", description);

                //вызываем активность с параметрами
                startActivity(intent);
                break;

            case 3:
                if (!db.sqlExec("delete from Инструкция where _id="+id)) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Не возможно удалить изображение в БД, повторите попытку!",
                            Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }

                loadData(edFind.getText().toString());
                break;

            default:break;
        }

        return super.onContextItemSelected(item);
    }

    //перегруженный метод активности - когда активность возобновляет работу
    @Override
    public void onResume(){
        super.onResume();
        loadData(edFind.getText().toString());
    }

    //обработчик кнопки добавления
    public void onClickBtn(View v) {
        if (v.getId() == R.id.btnAdd)
        {
            Intent intentAddEdit = new Intent(MainActivity.this, AddEditBicycle.class);
            intentAddEdit.putExtra("mode","add");
            startActivity(intentAddEdit);
        }
    }
}