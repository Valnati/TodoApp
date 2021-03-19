package com.codinginflow.mvvmtodo.util

//this generic extension will return the same object
//it doesn't functionally do enything except turning a statement into an exception
//exhaustive is used to make when statements compile safe
val <T> T.exhaustive: T
    get() = this