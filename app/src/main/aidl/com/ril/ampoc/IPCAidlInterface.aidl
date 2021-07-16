// IPCAidlInterface.aidl
package com.ril.ampoc;

// Declare any non-default types here with import statements
import com.ril.ampoc.model.User;

interface IPCAidlInterface {

    int getPid();

    User getUser();

    void setUser(inout User user);

    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);
}