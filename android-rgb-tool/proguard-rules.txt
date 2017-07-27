# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Applications/Android Studio.app/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the ProGuard
# include property in project.properties.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html
#

# Application specific
-keepattributes *Annotation*

# Apache Commons-IO
-keep class org.apache.commons.io.**

# PhotoView
-keep class uk.co.senab.photoview.** { *; }
-keep interface uk.co.senab.photoview.** { *; }

# Picasso
-dontwarn com.squareup.okhttp.**

# Android v7 Support Library
-keep class android.support.v7.widget.ShareActionProvider { *; }

# Greenbot EventBus
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# RxAndroid
-dontwarn rx.internal.util.unsafe.**

# LeakCanary
-keep class org.eclipse.mat.** { *; }
-keep class com.squareup.leakcanary.** { *; }

# Android Support Design Library
-dontwarn android.support.design.**
-keep class android.support.design.** { *; }
-keep interface android.support.design.** { *; }

# Retrolambda
-dontwarn java.lang.invoke.*
-dontwarn **$$Lambda$*
