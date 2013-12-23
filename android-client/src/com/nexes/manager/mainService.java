package com.nexes.manager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.nexes.manager.EventHandler.scan_thread;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class mainService extends Service {
	private static final String TAG = "mainService";
	private DatabaseUtil mydatabase; 
	
	 @Override
	 public IBinder onBind(Intent intent) {
		 	Log.e(TAG, "start IBinder~~~");
	        return null;
	    }
	 
	 @Override
	 public void onCreate() {
	        //Toast.makeText(this, "My Service created", Toast.LENGTH_LONG).show();
	        Log.i(TAG, "����onCreate");
	        mydatabase = new DatabaseUtil(this); //���ݿ���ʶ���

	    }

	 @Override
	 public void onDestroy() {
	        //Toast.makeText(this, "��������...", Toast.LENGTH_LONG).show();
	        Log.i(TAG, "����onDestroy");
	        
	    }

	 @Override
	 public void onStart(Intent intent, int startid) {
	        //Toast.makeText(this, "������ط���...", Toast.LENGTH_LONG).show();
	        Log.i(TAG, "��̨��������");
	        
	        //�����ļ��¼����
	        //TODO :�ݹ��ÿ��Ŀ¼�����������
            setlistener_thread t1 = new setlistener_thread();
	        new Thread(t1).start();
	 }
	
	
    private Handler showhandler = new Handler(){
    	@Override
    	public void handleMessage(Message msg) {
    	    super.handleMessage(msg);
    	    
    	    String show = msg.getData().getString("show");

    	    switch (msg.what) {
    	        case 1: //�Զ����һ����Ϣ���Է�ʽ
    	            //Toast.makeText(getApplicationContext(), show, Toast.LENGTH_LONG).show();
    	        	Toast.makeText(mainService.this, show, Toast.LENGTH_LONG).show();
    	        break;
    	    }
    	}
    	}; 
    
    //������������Ե����߳�
    public void ShowResult(String str){
    	
    	Message msg = new Message();
    	msg.what = 1; //����ֻ������һ�ַ�ʽ
    	Bundle bundle = new Bundle(); //����bundle�������
	    bundle.putString("show", str);
	    msg.setData(bundle);
	    showhandler.sendMessage(msg);
	    //bundle.clear();
    	
    }

    
	//�����߳�����
	class setlistener_thread implements Runnable {

	    public void run() {
	    	SetListener("/sdcard"); //TODO :����sd��Ŀ¼��Ϊ����
	        }
	 }
    
	//�ݹ���������ļ�ϵͳ
	public void SetListener(String path)
	{
		if(isDirectory(path)) 
		{
			Log.d("�����ļ�����:",path);
	        FileListener listener = new FileListener(path);  
	        listener.startWatching();
			
			String[] list = list_file(path);
			if (list != null)
			{
				for(int i=0; i<list.length; i++)
				{
					SetListener(list[i]);
				}
			}
		}
	}
	
	//�г�ĳһĿ¼path�����е��ļ�
	public String[] list_file(String path) {
		
		File file = new File(path);
		if(file.canRead())
		{
			String[] list = file.list();
			for(int i=0;i<list.length;i++)
				list[i] = path + "/" +list[i];
			
			return list;
		}
		else
		{
			Log.w("�ļ����ɶ���",path); //ע�⣬����ļ�����Ȩ�����ⲻ�ɶ�����������null
		}
		return null;
	}
	
	//�ж�һ��path���ļ�����Ŀ¼
	private boolean isDirectory(String name) {
		return new File(name).isDirectory();
	}
	    
	
	
	//���ڲ������ڼ���ļ�
	class FileListener extends FileObserver{
		private String mAbsolutePath; //�������·��
	    	
	    	public FileListener(String path){
	    		super(path); //���������¼�����Ҫ�ص�onEvent
	    		mAbsolutePath = path;
	    		Log.d("��ʼ�ļ�����",path);
	    	}
	    	
	    	@Override
	    	public void onEvent(int event, String path){
	    		//�������Ҫ��������Ƚ϶࣬������߳�ȥ��
	    		switch(event){
	    			case FileObserver.CREATE:
	    				String newfile = mAbsolutePath + "/" + path;
	    				
	    				Log.d("������-�ļ�����-�¼�",newfile);
	    				auto_scan(newfile);
	    				
	    				break;
	    			case FileObserver.ALL_EVENTS:
	    				Log.d("other_file_event",path);
	    				break;
	    		}
	    	}
	}
	    
	//�Զ������ļ�
	public void auto_scan(String filepath){
			
			ShowResult("�������ļ�:"+filepath);
			Log.d("ɨ�����ļ�",filepath);
			
	    	File file = new File(filepath);
			
			String requestURL = getResources().getString(R.string.ip_address) + "md5handle/";
			String s = file.toString();
			String md5 = GetDigest.getMD5EncryptedString(s);
			
			if(mydatabase.IsExist(md5) == false) //�ҵ��ˣ����ذ�ȫ
			{
				Toast.makeText(this, "�ļ���ȫ", Toast.LENGTH_LONG).show();
				return;
	        }
			else
			{
					File tempfile;
					//tempfile = new File("/sdcard/LOST.DIR/" + file.getName() + "_md5.txt");
				try {
					File outputDir = getCacheDir();
					tempfile = File.createTempFile(file.getName(), null, outputDir);
					Log.d("������ʱ�ļ�:",tempfile.getAbsolutePath());
					
					FileOutputStream out = new FileOutputStream(tempfile);
		            PrintStream p= new PrintStream(out);
		            p.println(md5);
		            p.close();
		            out.close();

				
		            if(file!=null){
					
						//�ϴ�MD5û��ʹ���̣߳���Ϊ��Ϣ���ܶ�
						String result = UploadUtil.uploadFile(tempfile, requestURL);
				        tempfile.delete(); //ɾ����ʱ�ļ�
				        
				        if(result.equals("new_file"))
				        {	//������ƶ�û�е��ļ�����������ļ��ϴ�
				        	requestURL = getResources().getString(R.string.ip_address) + "filehandle/";
			                //upload_thr upthread = new upload_thr();
			    	        //upthread.file = file;
			    	        //upthread.requestURL = requestURL;
			    	        //new Thread(upthread).start();
				        	
				        	String result2 = "what?";
				        	result2 = UploadUtil.uploadFile(file, requestURL);
				        	ShowResult("�ļ�ɨ����:" + result2);
				        	if(result2=="safe")
				        	{
				        		mydatabase.add(md5);
				        		ShowResult("�ļ���ȫ");
				        	}
				        	else
				        	{
				        		file.delete();
				        		ShowResult("�ļ�ɨ����:" + result2 + " ��ɾ��");
				        	}
				        }
				        else
				        {
				        	ShowResult("ժҪ���ϴ�:" + result);
				        }
			        }
				} catch (FileNotFoundException e) {
					Log.w("error","�ļ���������FileNotFoundException");
					e.printStackTrace();
				} catch (IOException e) {
					Log.w("error","�ļ���������IOException");
					e.printStackTrace();
				}
			}
	}

}
