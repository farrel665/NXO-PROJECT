-optimizationpasses 5
-optimizations !code/simplification/arithmetic
-overloadaggressively
-allowaccessmodification
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose
-ignorewarnings
-dontnote
-dontwarn **
-repackageclasses 'ancore'

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
-keep class com.google.android.material.** { *; }

-keep public class * extends androidx.fragment.app.Fragment
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
