# Add project specific ProGuard rules here.

# Keep all app classes (safe baseline — can tighten later)
-keep class com.arise.habitquest.** { *; }
-keepattributes *Annotation*

# kotlinx-serialization
-keepattributes InnerClasses
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.arise.habitquest.**$$serializer { *; }

# Glance widget RemoteViews
-keep class androidx.glance.** { *; }
