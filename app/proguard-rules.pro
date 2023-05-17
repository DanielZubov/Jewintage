-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-keep class com.google.firebase.auth.** { *; }
-keep class com.stato.jewintage.model.** { *; }


-keepclassmembers class com.stato.jewintage.** {
   public <init>();
}

-keepattributes Signature

-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.OpenSSLProvider
