-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose
-ignorewarnings
-dontwarn

-keep class com.ancore.AncoreApplication { *; }
-keep class com.ancore.MainActivity { *; }
-keep class com.ancore.DebugActivity { *; }

-keep class com.ancore.HomeFragmentActivity { *; }
-keep class com.ancore.MenuFragmentActivity { *; }
-keep class com.ancore.InfoFragmentActivity { *; }

-keep class com.ancore.AncoreUtil { *; }
-keep class com.ancore.BackgroundHelper { *; }
-keep class com.ancore.RoundedBottomNavigation { *; }
-keep class com.ancore.FileUtil { *; }

-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-dontwarn androidx.**

-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

-keep public class * extends androidx.fragment.app.Fragment
-keepclassmembers class * extends androidx.fragment.app.Fragment {
    public <init>(...);
}

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver

-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keep class org.json.** { *; }
-dontwarn org.json.**

-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}