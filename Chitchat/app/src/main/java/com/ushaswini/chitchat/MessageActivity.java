package com.ushaswini.chitchat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MessageActivity extends AppCompatActivity {

    Channel channel;
    /*SharedPreferences.Editor editor;
    String session;*/
    private OkHttpClient client;

    ArrayList<Message> messageList;
    MessageAdapter adapter;
    String token = "";

    TextView channelName;
    EditText editText;
    Button btn_send;
    ListView listView;

    final String MESSAGE_TEXT_KEY = "msg_text";
    final static String MESSAGE_TIME_KEY = "msg_time";
    final static String CHANNEL_ID = "channel_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        client  = new OkHttpClient();


        channelName = (TextView) findViewById(R.id.textView_name);
        editText = (EditText) findViewById(R.id.editText_input);
        btn_send = (Button) findViewById(R.id.button_send);
        listView = (ListView) findViewById(R.id.list_channel_body);

        /*SharedPreferences sessions = getSharedPreferences("com.ushaswini.chitchat", MODE_PRIVATE);
        editor = sessions.edit();
        session = sessions.getString(MainActivity.PREF_TAG, "");*/
        //Log.d("session",session + "is null");

        if(getIntent().getExtras().containsKey(ChannelsArrayAdapter.CHANNEL_ID)){
            channel = (Channel) getIntent().getExtras().getSerializable(ChannelsArrayAdapter.CHANNEL_ID);
            channelName.setText(channel.getChannel_name());
            if(getIntent().getExtras().containsKey(MainActivity.TOKEN_TAG)){
                token = (String) getIntent().getExtras().get(MainActivity.TOKEN_TAG);
                Log.d("token after geting ",token + " this");
            }
            getMessages(channel);
        }


        messageList = new ArrayList<>();
        adapter = new MessageAdapter(MessageActivity.this, R.layout.custom_row_message, messageList);
        listView.setAdapter(adapter);
        adapter.setNotifyOnChange(true);

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Log.d("token in btn_send",token);
                String channel_id = channel.getChannel_id();
                Log.d("channel id",channel_id);
                Log.d("edittext ",editText.getText().toString());
                Log.d("time",Calendar.getInstance().getTime().toString());

                RequestBody formBody = new FormBody.Builder()
                        .add(MESSAGE_TEXT_KEY, editText.getText().toString())
                        .add(CHANNEL_ID, channel_id)
                        .add(MESSAGE_TIME_KEY,Calendar.getInstance().toString())
                        .build();

                Request request = new Request.Builder()
                        .url(MainActivity.POST_MESSAGE_URL)
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .header("Authorization", "BEARER " + token)
                        .post(formBody)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                        try{
                            final ResponseDecoded decoded = ResponseJSONUtil.ResponseJSONParser.parseResponse(response.body().string());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    if(decoded.getStatus().equals("1")){

                                        ArrayList<Message> messages = new ArrayList<Message>();
                                        messages.addAll(messageList);
                                        Message message = new Message();
                                        message.setMsgtext(editText.getText().toString());
                                        Log.d("messages",messageList.toString());


                                        messages.add(message);
                                        messageList.clear();
                                        messageList.addAll(messages);
                                        adapter.notifyDataSetChanged();
                                        Toast.makeText(MessageActivity.this,"Operation successful",Toast.LENGTH_SHORT).show();

                                    }else{
                                        Log.d("error",decoded.getMessage()+decoded.getStatus()+decoded.getToken());
                                        Toast.makeText(MessageActivity.this,"Operation unsuccessful",Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private void getMessages(Channel channel){
        //System.out.print(MainActivity.GET_MESSAGE_URL + channel.getChannel_id());
        Log.d("token in getMessages",token);

        final Request request = new Request.Builder()
                .url(MainActivity.GET_MESSAGE_URL + channel.getChannel_id())
                .header("Authorization", "BEARER " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if(response.isSuccessful()){
                        final ArrayList<Message> messages = ResponseJSONUtil.ResponseJSONParser.parseMessages(response.body().string());

                        Log.d("messages",messages.toString());
                        if(messages.size() >0){
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    messageList.clear();
                                    messageList.addAll(messages);
                                    adapter.notifyDataSetChanged();
                                }

                            });

                        }else{
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    Toast.makeText(MessageActivity.this, "No messages found", Toast.LENGTH_SHORT).show();
                                }

                            });
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.custom_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_refresh:
                break;
            case R.id.action_logout:

                final SharedPreferences myPrefs = getSharedPreferences("com.ushaswini.inclass09", MODE_PRIVATE);
                final SharedPreferences.Editor prefsEditor = myPrefs.edit();
                prefsEditor.clear().commit();

                Intent intent = new Intent(MessageActivity.this,MainActivity.class);
                startActivity(intent);

                break;
        }

        return true;
    }
}
