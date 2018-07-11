package com.example.cs496_2;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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


public class Tab3_Fragment extends Fragment {
    public View view;
    public TextView text;
    public TextView text1;
    public ImageButton btn;
    public Button btn1;
    public String search_str;
    MyAdapter adapter;
    ArrayList<MyData> arrData = new ArrayList<MyData>();
    boolean isUploadServerContact = false;
    public String id;
    private boolean LOGIN= false;
    public ListView list;

    public Tab3_Fragment()
    {
        // required
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.tab3_fragment,
                container,false);
        text = (TextView) view.findViewById(R.id.textView2);
        text1 = (TextView) view.findViewById(R.id.textView3);
        list = (ListView)view.findViewById(R.id.contact);
        btn = (ImageButton)view.findViewById(R.id.imageButton);
        btn1 = (Button)view.findViewById(R.id.button3);
        btn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v){
                search_str = text.getText().toString();
                JSONObject jsonObject = new JSONObject();
                try{
                    jsonObject.accumulate("id", search_str);
                }catch(Exception e){e.getStackTrace();}
                isUploadServerContact = false;
                new JSONTask(jsonObject.toString()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                System.out.println("debug");
            }
        });
        btn1.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v){
                adapter.clear();
                loadMyContact();
                setAdapter();
            }
        });
        if(!LOGIN){
            Toast.makeText(getActivity(), "로그인하세요", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getContext(), LoginActivity.class);
            startActivityForResult(intent, 100);}
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100 && resultCode == RESULT_OK) {
            Toast.makeText(getActivity(), "로그인을 완료했습니다!", Toast.LENGTH_SHORT).show();

            id = data.getStringExtra("id");
            text1.setText("Hi      " +  id + "!");
            LOGIN = true;
            loadMyContact();
            setAdapter();

        }
    }

    public void loadMyContact(){
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
                wrapObject.put("id",id);
                System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                System.out.println(wrapObject);
            }catch(Exception e){e.getStackTrace();}
            arrData.add(data);

        }
        c.close();
        // Contact_lv.setAdapter(adapter);
        //new JSONTask("Eee").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        //new JSONTask((wrapObject.toString())).execute("http://52.231.67.72:8080/temp");
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

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

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
                    //JSONArray jsonArray = new JSONArray(wrapObject.getString("list"));
                    HttpURLConnection con = null;
                    BufferedReader reader = null;

                    try{
                        //URL url = new URL("http://192.168.25.16:3000/users");
                        URL url = new URL("http://52.231.67.72:8080/temp");
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
                        writer.write(wrapObject.toString());
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
            if(!result.contains("refused")){
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
            adapter.notifyDataSetChanged();}
            else{
                Toast.makeText(getActivity(), "없는 유저", Toast.LENGTH_SHORT).show();
            }
        }
        }
}