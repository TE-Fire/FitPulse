# Retrofit
-keep class com.fitpulse.app.data.remote.dto.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# Room
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
