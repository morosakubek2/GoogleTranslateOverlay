# Zachowaj klasę TranslateActivity i jej metody
-keep class com.google.android.apps.translate.TranslateActivity { *; }

# Zachowaj wszystkie klasy dziedziczące po android.app.Activity
-keep public class * extends android.app.Activity { *; }

# Zachowaj metody związane z Intentami
-keepclassmembers class * {
    void onCreate(android.os.Bundle);
    void onNewIntent(android.content.Intent);
}

# Zapobiegaj usuwaniu klas i metod z androidx.appcompat
-keep class androidx.appcompat.** { *; }
-dontwarn androidx.appcompat.**

# Zachowaj klasy i metody związane z android.content.Intent
-keep class android.content.Intent { *; }
-keepclassmembers class android.content.Intent { *; }

# Opcjonalnie: Ignoruj ostrzeżenia dla Android API
-dontwarn android.**

# Zapobiegaj optymalizacji i usuwaniu logów (dla debugowania)
-keep class android.util.Log { *; }
-dontoptimize
-dontpreverify

# Standardowe reguły dla minimalizacji
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose
