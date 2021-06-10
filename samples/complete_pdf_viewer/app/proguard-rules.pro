# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\develop_tools\android_studio_tools\Android\Android\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-keep class com.google.android.material.** {*;}
-keep class androidx.** {*;}
-keep public class * extends androidx.**
-keep interface androidx.** {*;}
-dontwarn com.google.android.material.**
-dontnote com.google.android.material.**
-dontwarn androidx.**

-dontwarn com.foxit.sdk.**
-keep class com.foxit.sdk.**{ *;}

-dontwarn com.microsoft.rightsmanagement.**
-keep class com.microsoft.rightsmanagement.** {*;}

-keep public class com.microsoft.rightsmanagement.R$* {
    public static final int *;
}

-dontwarn com.microsoft.aad.adal.**
-keep class com.microsoft.aad.adal.** {*;}

-dontwarn com.edmodo.cropper.**
-keep class com.edmodo.cropper.** {*;}

-dontwarn org.bouncycastle.**
-keep class org.bouncycastle.** {*;}

-dontwarn com.foxitsoftware.mobile.**
-keep class com.foxitsoftware.mobile.** {*;}
-keep class com.luratech.** {*;}