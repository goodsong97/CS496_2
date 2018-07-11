package com.example.cs496_2;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;

public class Tab1_contact extends Activity implements OnClickListener {

    private Bitmap bitmap;
    private String name;
    private String PH;
    private String email;
    private int position;
    private int pos;
    private String modified;
    private ListView listview;
    private ArrayList<String> LIST_MENU;
    private ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab1_contact_info);

        Intent i = getIntent();
        Bundle extras = i.getExtras();
        name = extras.getString("contact_name");
        PH = extras.getString("PH");
        bitmap = (Bitmap)i.getParcelableExtra("image");
        email = extras.getString("email");
        position = extras.getInt("position");
        if (email==null){
            email =" ";
        }

        LIST_MENU = new ArrayList<>();
        LIST_MENU.add(name);
        LIST_MENU.add(PH);
        LIST_MENU.add(email);

        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, LIST_MENU) ;

        listview = (ListView) findViewById(R.id.contact) ;
        listview.setAdapter(adapter) ;

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                final Modify_dialog dialog = new Modify_dialog(Tab1_contact.this);
                pos = position;
                dialog.show();
                dialog.setCanceledOnTouchOutside(false);
                dialog.setCancelable(false);
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        modified = dialog.getResult();
                        LIST_MENU.set(pos,modified);
                        adapter.notifyDataSetChanged();
                    }
                });

            }
        });

        ImageView iv = (ImageView)findViewById(R.id.imageView);
        //Bitmap resized = Bitmap.createScaledBitmap(bitmap, iv.getWidth(), iv.getHeight(), true);
        iv.setImageBitmap(bitmap);

        Button btn = (Button)findViewById(R.id.button);
        Button btn2 = (Button)findViewById(R.id.modify);
        btn.setOnClickListener(this);
        btn2.setOnClickListener(this);
        }
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.button:
                finish();
                break;
            case R.id.modify:
                Intent intent = new Intent();
                intent.putExtra("contact_name", name);
                intent.putExtra("PH", PH);
                intent.putExtra("image", bitmap);
                intent.putExtra("email",email);
                intent.putExtra("position",position);
                intent.putExtra("pos",pos);
                intent.putExtra("modified",modified);
                setResult(RESULT_OK, intent);
                finish();
                break;

        }
    }



}
