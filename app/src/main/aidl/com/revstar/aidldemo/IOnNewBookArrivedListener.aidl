// IOnNewBookArrivedListener.aidl
package com.revstar.aidldemo;

// Declare any non-default types here with import statements
import  com.revstar.aidldemo.Book;
interface IOnNewBookArrivedListener {

     void onNewBookArrived(in Book newBook);

}
