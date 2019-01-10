package com.revstar.aidldemo;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Create on 2019/1/9 18:19
 * author revstar
 * Email 1967919189@qq.com
 */
public class BookMangerService extends Service {

    private static final String TAG="BMS";
    private CopyOnWriteArrayList<Book>mBookList=new CopyOnWriteArrayList<>();
    private AtomicBoolean mIsServiceDestoryed=new AtomicBoolean(false);
    private RemoteCallbackList<IOnNewBookArrivedListener> mIOnNewBookArrivedListeners=new RemoteCallbackList<>();
    private Binder mBinder=new IBookManger.Stub() {
        @Override
        public List<Book> getBookList() throws RemoteException {
            return mBookList;
        }

        @Override
        public void addBook(Book book) throws RemoteException {
            mBookList.add(book);
        }

        @Override
        public void registerListener(IOnNewBookArrivedListener listener) throws RemoteException {
           mIOnNewBookArrivedListeners.register(listener);
           final  int  N=mIOnNewBookArrivedListeners.beginBroadcast();
           mIOnNewBookArrivedListeners.finishBroadcast();
           Log.d(TAG,"registerListener,current size:"+N);


        }

        @Override
        public void unregisterListener(IOnNewBookArrivedListener listener) throws RemoteException {
           boolean success= mIOnNewBookArrivedListeners.unregister(listener);
           if (success){
               Log.d(TAG,"unregister success.");

           }else {
               Log.d(TAG,"not found ,can not unregister");
           }
           final  int N =mIOnNewBookArrivedListeners.beginBroadcast();
           mIOnNewBookArrivedListeners.finishBroadcast();
           Log.d(TAG,"ungisterListener,current size:"+N);

        }

        @Override
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int check=checkCallingOrSelfPermission("com.revstar.aidldemo.permission.ACCESS_BOOK_SERVICE");
            Log.d(TAG, "check=" + check);
            if (check==PackageManager.PERMISSION_DENIED){
                return  false;
            }

            return super.onTransact(code, data, reply, flags);
        }
    };


    @androidx.annotation.Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mBookList.add(new Book(1,"Android开发艺术探索"));
        mBookList.add(new Book(2,"Android应用开发进阶"));
        new Thread(new ServiceWork()).start();
    }

    @Override
    public void onDestroy() {
        mIsServiceDestoryed.set(true);
        super.onDestroy();

    }


    private void onNewBookArrived(Book book) throws RemoteException {

        mBookList.add(book);

        final  int N=mIOnNewBookArrivedListeners.beginBroadcast();
        for (int i=0;i<N;i++){
            IOnNewBookArrivedListener listener=mIOnNewBookArrivedListeners.getBroadcastItem(i);
            if (listener!=null){
                try {
                    listener.onNewBookArrived(book);
                }catch (RemoteException e){
                    e.printStackTrace();
                }
            }
        }

        mIOnNewBookArrivedListeners.finishBroadcast();
    }

    private class ServiceWork implements Runnable{

        @Override
        public void run() {
            //do banckground processing here
            while (!mIsServiceDestoryed.get()){
                try {
                    Thread.sleep(5000);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int bookId=mBookList.size()+1;
                Book newBook=new Book(bookId,"new book#"+bookId);
                try {
                    onNewBookArrived(newBook);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
