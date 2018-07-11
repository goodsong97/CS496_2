package com.example.cs496_2;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.widget.AdapterView.OnItemClickListener;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Stack;

import static android.app.Activity.RESULT_OK;

public class Tab1_Fragment extends Fragment {
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private int REQ_CODE_SELECT_IMAGE = 1;
    ListView Contact_lv;
    private ListAdapter mAdapter;
    public TextView pbContack;
    public static String PBCONTACT;
    public static final int ACTIVITY_EDIT=1;
    private static final int ACTIVITY_CREATE=0;

    int checkcheck;
    ListView list;
    MyAdapter adapter;
    ArrayList<MyData> arrData = new ArrayList<MyData>();

    boolean isUploadServerContact = false;
    boolean isBackupServerContact = false;
    boolean isUploadPhoneContact =false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {
                int position = data.getIntExtra("position",0);
                String name = data.getStringExtra("contact_name");
                String PH = data.getStringExtra("PH");
                Bitmap bit = (Bitmap) data.getExtras().get("image");
                String email = data.getStringExtra("email");
                int pos= data.getIntExtra("pos",0);
                String modified = data.getStringExtra("modified");

                MyData Data = new MyData(new BitmapDrawable(getResources(), bit),name,PH,email,bit);
                Data.modifydata(pos,modified);


                ((MyData)adapter.getItem(position)).modify(Data.getImage(),Data.getName(),Data.getTel(),Data.getEmail(),Data.getBitmap());
                adapter.notifyDataSetChanged();

                Toast.makeText(getContext(),"Modified!",Toast.LENGTH_SHORT).show();
            }
            if (resultCode == Activity.RESULT_CANCELED) {

            }
        }}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab1_fragment, container,false);
        list = (ListView)view.findViewById(R.id.contact);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 3);
        } else{
            if(!isUploadPhoneContact){
                setData();
                isUploadPhoneContact = true;
            }
        }
        setAdapter();

        TextView del_but = (TextView) view.findViewById(R.id.delbtn);
        Button backup = (Button) view.findViewById(R.id.BACKUP);
        Button add_but = (Button) view.findViewById(R.id.add);

        add_but.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v){
                adapter.addItem(ContextCompat.getDrawable(getActivity(),R.drawable.user),"Name","Phone number");
                list.setAdapter(adapter);
                Toast.makeText(getContext(),"added",Toast.LENGTH_SHORT).show();
            }
        });
        backup.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v){
                //new Task("Eee").execute("http://52.231.67.72:8080/contact/backup");
                new Task("Eee").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                Toast.makeText(getContext(),"backup",Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }

    private void setData(){
        Stack<String> strData = new Stack<>();
        JSONObject wrapObject = new JSONObject();
        Bitmap bitmap;

        Cursor c = getActivity().getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                null, null, null);
        //initialize adapter
        JSONArray jsonArray = new JSONArray();
        while(c.moveToNext())
        {
            Drawable icon = getResources().getDrawable(
                    R.drawable.user);
            bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.user);
            String image_uri = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
            if(image_uri !=null) {
                Bitmap img;
                try {
                    img = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(),
                            Uri.parse(image_uri));
                    icon = new BitmapDrawable(img);
                    bitmap = img;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            String contactName = c
                    .getString(c
                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phNumber = c
                    .getString(c
                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            Cursor emailCursor =
                    getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                            new String[]{ContactsContract.CommonDataKinds.Email.DATA, ContactsContract.CommonDataKinds.Email.TYPE},
                            ContactsContract.CommonDataKinds.Email.CONTACT_ID+"='"+String.valueOf(contactName)+"'",
                            null, null);
            String email;
            while(emailCursor.moveToNext()) {
                email = emailCursor.getString(
                        emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                strData.add(email);
            }
            if(strData.size()>0){
                email = strData.pop();}
            else {email=null;}

            MyData data = new MyData(icon, contactName, phNumber, email, bitmap);
            try{JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("contact", contactName);
                jsonObject.accumulate("PH", phNumber);
                jsonObject.accumulate("email", email);
                jsonArray.put(jsonObject);
                wrapObject.put("list",jsonArray);
                System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                System.out.println(wrapObject);
            }catch(Exception e){e.getStackTrace();}
            arrData.add(data);

        }
        c.close();
        // Contact_lv.setAdapter(adapter);
        new JSONTask((wrapObject.toString())).execute("http://52.231.67.72:8080/contact");
        Toast.makeText(getContext(),"loaded",Toast.LENGTH_SHORT).show();
    }

    public void setAdapter(){
        adapter = new MyAdapter(getActivity(), arrData, new BtnClickListener(){
            @Override
            public void onBtnClick(int position) {
                // TODO Auto-generated method stub
                // Call your function which creates and shows the dialog here
                adapter.removeItem(position);
                adapter.notifyDataSetChanged();
            }
        });

        list.setAdapter(adapter);

        list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Intent i = new Intent(getActivity(), Tab1_contact.class);
                MyData data = arrData.get(position);
                Bitmap bit = data.getBitmap();
                Bitmap resized = Bitmap.createScaledBitmap(bit, 150, 150, true);
                i.putExtra("position",position);
                i.putExtra("contact_name", data.getName());
                i.putExtra("PH", data.getTel());
                i.putExtra("image", resized);
                i.putExtra("email", data.getEmail());
                startActivityForResult(i,1);
            }
        });
    }

    public class JSONTask extends AsyncTask<String, String, String> {

        String dataObject;
        String result = "";

        public JSONTask(String s){
            this.dataObject = s;
        }

        @Override
        protected String doInBackground(String... urls) {
            if(!isUploadServerContact){
                try {
                    isUploadServerContact = true;
                    //JSONObject를 만들고 key value 형식으로 값을 저장해준다.
                    JSONObject wrapObject = new JSONObject(dataObject);
                    JSONArray jsonArray = new JSONArray(wrapObject.getString("list"));
                    HttpURLConnection con = null;
                    BufferedReader reader = null;

                    try{
                        //URL url = new URL("http://192.168.25.16:3000/users");
                        URL url = new URL(urls[0]);
                        //연결을 함
                        con = (HttpURLConnection) url.openConnection();

                        con.setRequestMethod("POST");//POST방식으로 보냄
                        con.setRequestProperty("Cache-Control", "no-cache");//캐시 설정
                        con.setRequestProperty("Content-Type", "application/json");//application JSON 형식으로 전송
                        con.setRequestProperty("Accept", "text/html");//서버에 response 데이터를 html로 받음
                        con.setDoOutput(true);//Outstream으로 post 데이터를 넘겨주겠다는 의미
                        con.setDoInput(true);//Inputstream으로 서버로부터 응답을 받겠다는 의미
                        con.connect();

                        //서버로 보내기위해서 스트림 만듬
                        OutputStream outStream = con.getOutputStream();
                        //버퍼를 생성하고 넣음
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));
                        //for(int i = 0; i < jsonArray.length(); i++){
                        //JSONObject jObject = jsonArray.getJSONObject(0);
                        writer.write(jsonArray.toString());
                        writer.flush();
                        writer.close();//버퍼를 받아줌
                        //}

                        //서버로 부터 데이터를 받음
                        InputStream stream = con.getInputStream();

                        reader = new BufferedReader(new InputStreamReader(stream));

                        StringBuffer buffer = new StringBuffer();

                        String line = "";
                        while((line = reader.readLine()) != null){
                            buffer.append(line);
                        }
                        result = buffer.toString();
                        Toast.makeText(getContext(),"loaded",Toast.LENGTH_SHORT).show();

                        //return buffer.toString();//서버로 부터 받은 값을 리턴해줌 아마 OK!!가 들어올것임

                    } catch (MalformedURLException e){
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if(con != null){
                            con.disconnect();
                        }
                        try {
                            if(reader != null){
                                reader.close();//버퍼를 닫아줌
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
    }
    public class Task extends AsyncTask<String, String, String> {

        String dataObject;
        String result = "";
        public Task(String s){
            this.dataObject = s;
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                //JSONObject를 만들고 key value 형식으로 값을 저장해준다.
                HttpURLConnection con = null;
                BufferedReader reader = null;
                URL url = new URL("http://52.231.67.72:8080/contact/backup");
                //URL url = new URL(urls[0]);
                //연결을 함
                con = (HttpURLConnection) url.openConnection();

                con.setRequestMethod("POST");//POST방식으로 보냄
                con.setRequestProperty("Cache-Control", "no-cache");//캐시 설정
                con.setRequestProperty("Content-Type", "application/json");//application JSON 형식으로 전송
                con.setRequestProperty("Accept", "text/html");//서버에 response 데이터를 html로 받음
                con.setDoOutput(true);//Outstream으로 post 데이터를 넘겨주겠다는 의미
                con.setDoInput(true);//Inputstream으로 서버로부터 응답을 받겠다는 의미
                con.connect();

                //서버로 보내기위해서 스트림 만듬
                OutputStream outStream = con.getOutputStream();
                //버퍼를 생성하고 넣음
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));
                //for(int i = 0; i < jsonArray.length(); i++){
                //JSONObject jObject = jsonArray.getJSONObject(0);
                writer.write(dataObject.toString());
                writer.flush();
                writer.close();//버퍼를 받아줌
                //}

                //서버로 부터 데이터를 받음
                InputStream stream = con.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();

                String line = "";
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                result = buffer.toString();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            adapter.clear();
            arrData.clear();
            MyData newdata;
            JSONArray res = new JSONArray();
            JSONObject obj;
            try {
                res = new JSONArray(result);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try{
                for (int i = 0; i <res.length(); i ++){
                obj = res.getJSONObject(i);
                newdata = new MyData(ContextCompat.getDrawable(getActivity(),R.drawable.user),
                        obj.get("contact").toString(), obj.get("PH").toString(), " ",BitmapFactory.decodeResource(getActivity().getResources(),
                        R.drawable.user));
                    arrData.add(newdata);
            }}catch (JSONException e){e.printStackTrace();}
            adapter.modifyto(arrData);
            adapter.notifyDataSetChanged();
        }
    }

}

class MyAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<MyData> arrData;
    private LayoutInflater inflater;
    private int pos;
    private BtnClickListener mClickListener;

    public MyAdapter(Context c, ArrayList<MyData> arr, BtnClickListener listener ) {
        this.context = c;
        this.arrData = arr;
        inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mClickListener = listener;
    }

    public void modifyItem(int position, MyData mydata){
        arrData.set(position, mydata);
    }

    public int getCount() {
        return arrData.size();
    }

    public Object getItem(int position) {
        return arrData.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public void removeItem(int index){
        arrData.remove(index);
    }

    public void addItem(Drawable icon, String Name, String Ph_num) {
        MyData item = new MyData(icon, Name, Ph_num, "", BitmapFactory.decodeResource(context.getResources(),
                R.drawable.user));
        arrData.add(item);
    }

    public void clear(){
        arrData.clear();
    }

    public View getView(final int position,View convertView, ViewGroup parent) {

        if(convertView == null){
            convertView = inflater.inflate(R.layout.tab1_list, parent, false);
        }

        ImageView image = (ImageView)convertView.findViewById(R.id.image);

        Drawable draw = arrData.get(position).getImage();
        image.setImageDrawable(draw);

        TextView name = (TextView)convertView.findViewById(R.id.name);
        name.setText(arrData.get(position).getName());

        TextView delBtn = (TextView) convertView.findViewById(R.id.delbtn);
        delBtn.setTag(position);
        delBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(mClickListener != null)
                    mClickListener.onBtnClick((Integer) v.getTag());
            }
        });
        return convertView;
    }
    public void modifyto(ArrayList<MyData> newarrData){
        this.arrData = newarrData;
    }
}

class MyData {

    private Drawable image;
    private String name;
    private String tel;
    private String email;
    private Bitmap bitmap;

    public MyData(Drawable image, String name, String tel, String email, Bitmap bitmap){
        this.image = image;
        this.name = name;
        this.tel = tel;
        this.email = email;
        this.bitmap = bitmap;
    }

    public void modifydata(int i,String s){
        if (i==0){
            name =s;
        }
        if(i==1){
            tel =s;
        }
        if(i==2){
            email = s;
        }
    }

    public Drawable getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    public String getTel() {
        return tel;
    }

    public String getEmail() {
        return email;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setImage(Drawable im) {
        image = im;
    }

    public void setName(String n) {
        name = n;
    }

    public void setTel(String t) {
        tel = t;
    }

    public void setEmail(String e)
    {
        email = e;
    }

    public void setBitmap(Bitmap b)
    {
        bitmap = b;
    }

    public void modify(Drawable i, String n, String t, String e, Bitmap b){
        image =i;
        name =n;
        tel = t;
        email = e;
        bitmap = b;
    }
}

interface BtnClickListener {
    public abstract void onBtnClick(int position);
}

class contact extends Activity implements View.OnClickListener {

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
                final Modify_dialog dialog = new Modify_dialog(contact.this);
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

class Modify_dialog extends Dialog implements View.OnClickListener{
    private static final int LAYOUT = R.layout.tab1_modify_dialog;
    private Context context;
    private String name;
    private String pn;
    private Drawable icon;
    private EditText edit_text;
    private EditText ph;
    private int PICK_IMAGE_REQUEST=1;
    Button submit;
    Button get_user_img;

    public Modify_dialog(Context context) {
        super(context);
        this.context = context;
        this.icon = icon;
        this.name = "aaaaa";
        this.pn = pn;
    }

    public String getResult() {
        return name;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT);

        edit_text = (EditText)findViewById(R.id.edit_ph);
        //ph = (EditText)findViewById(R.id.edit_ph);
        Button submit = (Button) findViewById(R.id.submit);
        Button quit = (Button) findViewById(R.id.quit);
        submit.setOnClickListener(this);
        quit.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.submit :
                String name_str = edit_text.getText().toString();
                //String ph_str = ph.getText().toString();
                //result.setIconDrawable(icon);
                name = name_str;
                // result.setPh_num(ph_str);
                dismiss();
                break;
            case R.id.quit:
                dismiss();
                break;
        }
    }
}