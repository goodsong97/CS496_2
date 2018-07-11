package com.example.cs496_2;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

public class Tab2_Fragment extends Fragment{
    private final static String TAG = "Tab2_Fragment";
    private static final int REQUEST_PERMISSION_KEY = 1;
    ArrayList<HashMap<String, String >> albumList = new ArrayList<>();
    ArrayList<ArrayList<HashMap<String, String >>> AllAlbum = new ArrayList<>();
    LoadAlbum loadAlbumTask;
    GridView tab2GridView;
    Context context;
    private DeliveryData deliveryData;
    //////
    String uploadFilePath = "/storage/emulated/0/DCIM/Screenshots/";
    String uploadFileName = "Screenshot_20180219-082344.png";
    String upLoadServerUri = null;
    int serverResponseCode = 0;
    boolean isUpload = false;
    ArrayList<String> serverImagePath = new ArrayList<>();
    ArrayList<String> serverImageName = new ArrayList<>();
    ArrayList<HashMap<String, String>> imageList = new ArrayList<>();
    downloadImagePath downTask;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.tab2_fragment,container,false);
        tab2GridView = view.findViewById(R.id.tab2_F_GridView);
        context = getActivity();
        setTab2GridView(view);

        String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if(!Function.hasPermissions(getContext(), PERMISSIONS)){
            ActivityCompat.requestPermissions(getActivity(), PERMISSIONS, REQUEST_PERMISSION_KEY);
        }

        ///
        upLoadServerUri = "http://52.231.65.0:8080/api/photo";
        ///

        return view;
    }

    public int uploadFile(String sourceFileUri) {

        String fileName = sourceFileUri;

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);

        if (!sourceFile.isFile()) {
            Log.e("uploadFile", "Source File not exist :" + uploadFilePath + "" + uploadFileName);
            return 0;
        } else {
            try {

                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(upLoadServerUri);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + fileName + "\"" + lineEnd);

                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

                if (serverResponseCode == 200) {
                    BufferedReader input = new BufferedReader(new InputStreamReader(conn.getInputStream()), 8192);
                    final StringBuilder response = new StringBuilder();
                    String strLine = null;
                    while ((strLine = input.readLine()) != null) {
                        response.append(strLine);
                    }
                    input.close();
                    fileInputStream.close();
                    dos.flush();
                    dos.close();
                }

            } catch (Exception e){
                e.printStackTrace();
            }
            return serverResponseCode;
        } // End else block
    }

    public void setTab2GridView(View view){

        int iDisplayWidth = getResources().getDisplayMetrics().widthPixels ;
        Resources resources = getContext().getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = iDisplayWidth / (metrics.densityDpi / 160f);
        if(dp < 360) {
            dp = (dp - 17) / 2;
            float px = Function.convertDpToPixel(dp, getContext());
            tab2GridView.setColumnWidth(Math.round(px));
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case REQUEST_PERMISSION_KEY: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadAlbumTask = new LoadAlbum();
                    loadAlbumTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    Toast.makeText(getContext(), "You must accept permissions.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void onResume() {
        super.onResume();
        String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if(!Function.hasPermissions(getContext(), PERMISSIONS)){
            ActivityCompat.requestPermissions(getActivity(), PERMISSIONS, REQUEST_PERMISSION_KEY);
        }else{
            loadAlbumTask = new LoadAlbum();
            loadAlbumTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof DeliveryData){
            deliveryData = (DeliveryData)context;
        }else {
            throw new RuntimeException(context.toString()+"must implement this interface");
        }
    }

    public void onDetach() {
        super.onDetach();
        deliveryData = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()){
            case R.id.download:
                downTask = new downloadImagePath();
                downTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                Toast.makeText(getActivity(), "download now", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    class downloadImagePath extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            getServerImagePath();
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

    class LoadAlbum extends AsyncTask<String, Void, String > {

        protected void onPreExecute() {
            super.onPreExecute();
            albumList.clear();
        }

        protected String doInBackground(String... args) {
            String xml = "";
            SearchAlbum();
            return xml;
        }

        protected void onPostExecute(String xml) {

            if(!isUpload){
                Toast.makeText(context, "File Upload Complete.", Toast.LENGTH_SHORT).show();
            }
            isUpload = true;


            GridViewAdapter adapter = new GridViewAdapter(context, R.layout.tab2_album_row, albumList);
            tab2GridView.setAdapter(adapter);
            tab2GridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                    //Intent intent = new Intent(getActivity(),Tab2AlbumActivity.class);
                    //intent.putExtra("name", albumList.get(+position).get(Function.KEY_ALBUM));
                    //startActivity(intent);
                    deliveryData.deliver(albumList,position);
                }
            });
        }
    }

    public interface DeliveryData{
        void deliver(ArrayList<HashMap<String, String>> data, int position);
    }

    public void SearchAlbum() {
        String path = null;
        String album = null;
        String timestamp = null;
        String countPhoto = null;
        Uri uriExternal = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Uri uriInternal = MediaStore.Images.Media.INTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.MediaColumns.DATE_MODIFIED};

        Cursor cursorExternal = getActivity().getContentResolver().query(uriExternal, projection,
                "_data IS NOT NULL) GROUP BY (bucket_display_name", null, null);
        Cursor cursorInternal = getActivity().getContentResolver().query(uriInternal, projection,
                "_data IS NOT NULL) GROUP BY (bucket_display_name", null, null);
        Cursor cursor = new MergeCursor(new Cursor[]{cursorExternal, cursorInternal});

        while (cursor.moveToNext()) {

            path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
            album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
            timestamp = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));
            countPhoto = Function.getCount(getActivity(), album);

            albumList.add(Function.mappingInbox(album, path, timestamp, Function.converToTime(timestamp), countPhoto));
        }
        cursor.close();
        Collections.sort(albumList, new MapComparator(Function.KEY_TIMESTAMP, "dsc"));

        for (int i = 0; i < albumList.size(); i++) {
            AllAlbum.add(searchImage(albumList.get(i).get(Function.KEY_ALBUM)));
        }
    }

    public ArrayList<HashMap<String, String>> searchImage(String album_name) {
        String xml = "";
        String path = null;
        String album = null;
        String timestamp = null;
        Uri uriExternal = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Uri uriInternal = MediaStore.Images.Media.INTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.MediaColumns.DATE_MODIFIED};
        Cursor cursorExternal = getActivity().getContentResolver().query(uriExternal, projection, "bucket_display_name = \"" + album_name + "\"", null, null);
        Cursor cursorInternal = getActivity().getContentResolver().query(uriInternal, projection, "bucket_display_name = \"" + album_name + "\"", null, null);
        Cursor cursor = new MergeCursor(new Cursor[]{cursorExternal, cursorInternal});
        while (cursor.moveToNext()) {
            path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
            album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
            timestamp = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));
            imageList.add(Function.mappingInbox(album, path, timestamp, Function.converToTime(timestamp), null));
        }
        cursor.close();
        Collections.sort(imageList, new MapComparator(Function.KEY_TIMESTAMP, "dsc"));
        if(!isUpload){
            for(int j=0;j<imageList.size();j++){
                System.out.println(imageList.get(j).get(Function.KEY_PATH));
                uploadFilePath = imageList.get(j).get(Function.KEY_PATH);
                uploadFile(uploadFilePath);
            }
        }
        return imageList;
    }

    public void getServerImagePath(){
        try{
            URL url = new URL("http://52.231.65.0:8080/api/photolist");
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setUseCaches(false);
            conn.setRequestMethod("GET");
            int response = conn.getResponseCode();
            if(response == HttpURLConnection.HTTP_OK){
                InputStream inputStream = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line = "";
                while((line = reader.readLine()) != null){
                    stringBuilder.append(line);
                }
                String result = stringBuilder.toString();
                JSONArray res = new JSONArray(result);
                JSONObject obj;
                for(int i=0;i<res.length();i++){
                    obj = res.getJSONObject(i);
                    serverImageName.add(obj.get("name").toString());
                    serverImagePath.add(obj.get("path").toString());
                }
            }
            for(int i=0;i<serverImagePath.size();i++){
                getImage("http://52.231.65.0:8080/api/getphotos", serverImageName.get(i),serverImagePath.get(i));
            }
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public void getImage(String url, String filename, String filepath){
        try{
            URL imgUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection)imgUrl.openConnection();
            conn.setConnectTimeout(10000);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            OutputStream wr = conn.getOutputStream();
            String body = "path="+filepath;
            wr.write(body.getBytes());
            wr.flush();
            wr.close();

            int response = conn.getResponseCode();
            if(response == HttpURLConnection.HTTP_OK){
                File file = new File("/storage/emulated/0/DCIM/Camera/NEW"+filename);
                InputStream inputStream = conn.getInputStream();
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                int downloadSize = 0;
                byte[] buffer = new byte[1024];
                int bufferLength = 0;
                while((bufferLength = inputStream.read(buffer)) > 0){
                    fileOutputStream.write(buffer, 0, bufferLength);
                    downloadSize += bufferLength;
                }
                fileOutputStream.close();
            }
            conn.disconnect();
        }catch (Exception e){}
    }
}

class GridViewAdapter extends BaseAdapter {
    Context context;
    int layout;
    ArrayList<HashMap<String, String>> data;
    LayoutInflater inf;

    public GridViewAdapter(Context context, int layout, ArrayList<HashMap<String, String>> data) {

        this.context = context;
        this.layout = layout;
        this.data = data;
        inf = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    public int getCount() {
        return data.size();
    }
    public Object getItem(int position) {
        return position;
    }
    public long getItemId(int position) {
        return position;
    }
    public View getView(int position, View convertView, ViewGroup parent){
        ViewHolder holder;
        if (convertView==null){
            holder = new ViewHolder();
            convertView = inf.inflate(layout, null);
            holder.gallery_image = convertView.findViewById(R.id.tab2_AR_image);
            holder.gallery_count = convertView.findViewById(R.id.tab2_AR_count);
            holder.gallery_title = convertView.findViewById(R.id.tab2_AR_title);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.gallery_image.setId(position);
        holder.gallery_count.setId(position);
        holder.gallery_title.setId(position);

        HashMap<String, String > song = data.get(position);
        try{
            holder.gallery_title.setText(song.get(Function.KEY_ALBUM));
            holder.gallery_count.setText(song.get(Function.KEY_COUNT));
            Glide.with(context).load(new File(song.get(Function.KEY_PATH))).into(holder.gallery_image);
        } catch (Exception e){}
        return convertView;
    }
}

class ViewHolder {
    ImageView gallery_image;
    TextView gallery_count, gallery_title;
}

class Function {

    static final String KEY_ALBUM = "album_name";
    static final String KEY_PATH = "path";
    static final String KEY_TIMESTAMP = "timestamp";
    static final String KEY_TIME = "date";
    static final String KEY_COUNT = "date";

    public static  boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public static HashMap<String, String> mappingInbox(String album, String path, String timestamp, String time, String count) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(KEY_ALBUM, album);
        map.put(KEY_PATH, path);
        map.put(KEY_TIMESTAMP, timestamp);
        map.put(KEY_TIME, time);
        map.put(KEY_COUNT, count);
        return map;
    }

    public static String getCount(Context c, String album_name)
    {
        Uri uriExternal = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Uri uriInternal = MediaStore.Images.Media.INTERNAL_CONTENT_URI;

        String[] projection = { MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.MediaColumns.DATE_MODIFIED };
        Cursor cursorExternal = c.getContentResolver().query(uriExternal, projection, "bucket_display_name = \""+album_name+"\"", null, null);
        Cursor cursorInternal = c.getContentResolver().query(uriInternal, projection, "bucket_display_name = \""+album_name+"\"", null, null);
        Cursor cursor = new MergeCursor(new Cursor[]{cursorExternal,cursorInternal});

        return cursor.getCount()+" Photos";
    }

    public static String converToTime(String timestamp)
    {
        long datetime = Long.parseLong(timestamp);
        Date date = new Date(datetime);
        DateFormat formatter = new SimpleDateFormat("dd/MM HH:mm");
        return formatter.format(date);
    }

    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }
}

class MapComparator implements Comparator<HashMap<String, String>> {
    private final String key;
    private final String order;

    public MapComparator(String key, String order)
    {
        this.key = key;
        this.order = order;
    }

    public int compare(HashMap<String, String> first,
                       HashMap<String, String> second)
    {
        // TODO: Null checking, both for maps and values
        String firstValue = first.get(key);
        String secondValue = second.get(key);
        if(this.order.toLowerCase().contentEquals("asc"))
        {
            return firstValue.compareTo(secondValue);
        }else{
            return secondValue.compareTo(firstValue);
        }
    }
}