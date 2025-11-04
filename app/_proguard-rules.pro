# Zachowaj wszystko z Twojej aplikacji
-keep class com.google.android.apps.translate.** { *; }

# Zachowaj Activity i Service
-keep class * extends android.app.Activity
-keep class * extends android.app.Service

# Zachowaj intent-filtery
-keepattributes *Annotation*
-keepattributes Signature
