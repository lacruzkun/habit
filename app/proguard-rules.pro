# FocusFlow keeps serialized export models and Room-generated code.
-keepclassmembers class com.focusflow.app.data.export.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepattributes *Annotation*
