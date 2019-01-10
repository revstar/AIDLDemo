package com.revstar.aidldemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String TAG="MainActivity";
    public static final int MESSAGE_NEW_BOOK_ARRIVED=1;
    private IBookManger mRemoteBookManger;
    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what){
                case MESSAGE_NEW_BOOK_ARRIVED:
                    Log.d(TAG,"receive new book:"+msg.obj);
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private ServiceConnection mConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            IBookManger bookManger=IBookManger.Stub.asInterface(service);
            try {
                mRemoteBookManger=bookManger;
                List<Book>list=bookManger.getBookList();
                Log.i(TAG,"query book list"+list.toString());
                Book newBook=new Book(3,"Android进阶之光");
                bookManger.addBook(newBook);
                Log.i(TAG,"add book:"+newBook);
                List<Book>newList=bookManger.getBookList();
                Log.i(TAG,"query book list"+newList.toString());
                bookManger.registerListener(mIOnNewBookArrivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mRemoteBookManger=null;
            Log.e(TAG,"ninder died.");

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent=new Intent(this,BookMangerService.class);
        bindService(intent,mConnection,Context.BIND_AUTO_CREATE);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        if (mRemoteBookManger!=null){
                            try {
                                List<Book>newList=mRemoteBookManger.getBookList();
                                Log.d(TAG,"获取到的新书"+newList.toString());
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                }).start();

            }
        });

    }

    @Override
    protected void onDestroy() {
        Toast.makeText(MainActivity.this,"onDestory",Toast.LENGTH_SHORT).show();
        if (mRemoteBookManger!=null&&
                mRemoteBookManger.asBinder().isBinderAlive()){
            try {
                Log.i(TAG,"unregister listener:"+mIOnNewBookArrivedListener);
                mRemoteBookManger.unregisterListener(mIOnNewBookArrivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        unbindService(mConnection);
        super.onDestroy();

    }


    private IOnNewBookArrivedListener mIOnNewBookArrivedListener=new IOnNewBookArrivedListener.Stub() {
        @Override
        public void onNewBookArrived(Book newBook) throws RemoteException {
            mHandler.obtainMessage(MESSAGE_NEW_BOOK_ARRIVED,newBook)
                    .sendToTarget();
        }
    };

}
