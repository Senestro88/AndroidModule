# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**
-keep class com.google.gson.stream.** { *; }

# Prevent proguard from stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Prevent R8 from leaving Data object members always null
-keepclassmembers, allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Retain generic signatures of TypeToken and its subclasses with R8 version 3.0 and higher.
-keep, allowobfuscation, allowshrinking class com.google.gson.reflect.TypeToken
-keep, allowobfuscation, allowshrinking class * extends com.google.gson.reflect.TypeToken

-keep class io.** {*;}
-keepclassmembers class io.** {*;}
-keep class androidx.** {*;}
-keepclassmembers class androidx.** {*;}
-keep class android.** {*;}
-keepclassmembers class android.** {*;}
-keep class java.** {*;}
-keepclassmembers class java.** {*;}
-keep class org.** {*;}
-keepclassmembers class org.** {*;}
-keep class com.google.** {*;}
-keepclassmembers class com.google.** {*;}
-keep class com.squareup.** {*;}
-keepclassmembers class com.squareup.** {*;}
-keep class org.apache.commons.io.** {*;}
-keepclassmembers class org.apache.commons.io.** {*;}
-keep class com.bumptech.** {*;}
-keepclassmembers class com.bumptech.** {*;}
-keep class com.daimajia.** {*;}
-keepclassmembers class com.daimajia.** {*;}
-keep class com.journeyapps.** {*;}
-keepclassmembers class com.journeyapps.** {*;}

-keep class com.android.tools.profiler.** { *; }
-keep class com.android.tools.profiler.support.network.httpurl.** { *; }

-dontwarn org.apache.commons.lang.functor.Predicate
-dontwarn org.apache.commons.lang.functor.PredicateUtils
-dontwarn java.lang.invoke.MethodHandleProxies

# Enable optimization and remove unused classes and methods
-optimizations !code/simplification/arithmetic,!code/simplification/cast

-optimizationpasses 5
-dontusemixedcaseclassnames