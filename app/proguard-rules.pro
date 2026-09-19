-keep class com.breez.notes.** { *; }
-dontwarn com.breez.notes.**

-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep class androidx.work.** { *; }
-keep class dagger.hilt.** { *; }
-keep class * extends androidx.work.ListenableWorker { *; }

-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
