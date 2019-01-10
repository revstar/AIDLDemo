// IBookManger.aidl
package com.revstar.aidldemo;

// Declare any non-default types here with import statements
import com.revstar.aidldemo.Book;
import  com.revstar.aidldemo.IOnNewBookArrivedListener;

interface IBookManger {

   List<Book>getBookList();
   void addBook(in Book book);
   void registerListener(IOnNewBookArrivedListener listener);
   void unregisterListener(IOnNewBookArrivedListener listener);


}
