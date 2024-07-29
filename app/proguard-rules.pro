# Add your ProGuard rules here
# For more information on how to configure ProGuard, see
# http://developer.android.com/guide/developing/tools/proguard.html

# Retain the names of classes that implement the Android Application subclass.
-keep class ** extends android.app.Application

# Retain class members named "describeContents" and "writeToParcel".
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
    public int describeContents();
    public void writeToParcel(android.os.Parcel, int);
}

# Retain names of classes required for serialization/deserialization.
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    private void readObjectNoData();
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}