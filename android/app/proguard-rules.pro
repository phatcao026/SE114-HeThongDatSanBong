# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }

# Gson
-keep class com.google.gson.** { *; }
-keep class com.example.fieldbooking.model.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
