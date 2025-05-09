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
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class com.hchen.clipboardlist.HookMain
-keep class * extends com.hchen.hooktool.BaseHC
-keep class com.hchen.clipboardlist.hook.**
-keep class com.hchen.clipboardlist.hook.**$*

-keep class com.hchen.clipboardlist.hook.clipboard.data.ContentModel {*;}
-keep class com.hchen.clipboardlist.hook.clipboard.RestoreContentModel {*;}
-keep class  com.hchen.hooktool.HCState {
        static boolean isEnabled;
        static java.lang.String mFramework;
        static int  mVersion;
 }
-keep class * implements android.os.Parcelable {
        public static ** CREATOR;
}