package com.example.connectiondemo;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.R.integer;
import android.R.string;
import android.app.Activity;
import android.content.ContentValues;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {

	Button btn_1;
	Button btn_2;
	Button btn_3;
	Button btn_4;
	//Button btn_5;
	Handler handler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
		handler=new Handler(){
			
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					String data=(String)msg.obj;
					try {
						parseXml(data);
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				case 2:
					String data2=(String)msg.obj;
                    parseJson(data2);

					break;

				default:
					break;
				}
			}
		};
	}

	public void initView(){
		btn_1=(Button)findViewById(R.id.button1);
		btn_2=(Button)findViewById(R.id.button2);
		btn_3=(Button)findViewById(R.id.button3);
		btn_4=(Button)findViewById(R.id.button4);
		//btn_5=(Button)findViewById(R.id.button5);
		
		//http xml

		btn_1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String xml=httpRequest("http://101.200.164.87:8080/visa/xml/china.xml",1);
				
			}
		});
		
		//http json

		btn_2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String xml=httpRequest("http://101.200.164.87:8080/visa/xml/version.json",2);
				
			}
		});
		
		btn_3.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						Log.e("Socket", "start");
						
						try {
							requestSocketTcp("101.200.164.87",111);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						Log.e("Socket", "end");
					}
				}).start();
			}
		});
        
	}
	
	private void requestSocketTcp(String url, int port) throws IOException{
		StringBuilder sb = null;
		OutputStream out = null;
		InputStream in = null;
		try {
			Socket socket=new Socket(url, port);
			JSONObject jsonObject=new JSONObject();
			jsonObject.put("id", "admin");
			jsonObject.put("psd", "123456");
			out=socket.getOutputStream();
			in=socket.getInputStream();
			BufferedReader br=new BufferedReader(new InputStreamReader(in));
			sb=new StringBuilder();
			String line;
			while((line=br.readLine())!=null){
				sb.append(line);
			}
			out.write(jsonObject.toString().getBytes());
			out.flush();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(in!=null){
				in.close();
			}
			if(out!=null){
				out.close();
			}			
			
		}
		//return sb.toString();
		
	}
	
	private void requestUdp(){
		
	}
	
	private String httpRequest(final String s,final int type){
		Log.e("http", "start");
		final StringBuilder sb = new StringBuilder();;
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				HttpURLConnection connection = null;
				try {
					URL url=new URL(s);
					connection=(HttpURLConnection)url.openConnection();
					connection.setRequestMethod("GET");
					connection.setReadTimeout(3000);
					connection.setConnectTimeout(3000);
					connection.setDoInput(true);
					connection.setDoOutput(true);
					Log.e("http", ""+connection.getContentLength());;
					if(connection.getResponseCode()==200){
						InputStream in=connection.getInputStream();
						BufferedReader reader=new BufferedReader(new InputStreamReader(in));
						String line;
						while((line=reader.readLine())!=null){
							sb.append(line);
							Log.e("http", line);
						}
						Message msg=new Message();
						msg.obj=sb.toString();
						msg.what=type;
						handler.sendMessage(msg);
													
					}
					else{
						Log.e("http", "connection failed");
					}

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally{
					if(connection!=null){
						connection.disconnect();
					}
					
				}
				
			}
		}).start();
		return sb.toString();
	}
	
	private void parseXml(String s) throws IOException{
		try {
			Log.e("XML", "start");
			Log.e("XML", s);
			XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
			XmlPullParser parser=factory.newPullParser();
			InputStream input=new ByteArrayInputStream(s.getBytes());
			parser.setInput(input,"utf-8");
			int eventType=parser.getEventType();
			while (eventType!=XmlPullParser.END_DOCUMENT) {
				String node=parser.getName();
				switch (eventType) {
				case XmlPullParser.START_TAG:
					if(node.equals("city")){
						Log.e("XML", parser.getAttributeValue(0));
					}
					break;

				case XmlPullParser.END_TAG:
					break;
					
				default:
					break;
				}
				eventType=parser.next();
			}
			
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	private void parseJson(String s){
		try {			
			JSONObject jsonObject=new JSONObject(s);
			Log.e("JSON",jsonObject.getString("url") );
			JSONObject object=new JSONObject();
			object.put("id", "1");
			object.put("name", "liu");
			Log.e("JSON",object.toString() );
					
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
