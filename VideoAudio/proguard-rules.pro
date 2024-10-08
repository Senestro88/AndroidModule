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

-keep class io.** {*;}
-keepclassmembers class io.** {*;}
-keep class androidx.** {*;}
-keepclassmembers class androidx.** {*;}
-keep class android.** {*;}
-keepclassmembers class android.** {*;}
-keep class java.** {*;}
-keepclassmembers class java.** {*;}

-keep class com.android.tools.profiler.** { *; }
-keep class com.android.tools.profiler.support.network.httpurl.** { *; }

-dontwarn java.lang.invoke.MethodHandleProxies

# Enable optimization and remove unused classes and methods
-optimizations !code/simplification/arithmetic,!code/simplification/cast

-optimizationpasses 5
-dontusemixedcaseclassnames