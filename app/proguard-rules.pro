# Proguard rules for Minimal Widgets
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
-keep class com.jndapp.minimal.widgets.model.** { *; }
-keep class com.jndapp.minimal.widgets.models.** { *; }
