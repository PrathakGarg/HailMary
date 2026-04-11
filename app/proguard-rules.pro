# Add project specific ProGuard rules here.
-keep class com.arise.habitquest.** { *; }
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
