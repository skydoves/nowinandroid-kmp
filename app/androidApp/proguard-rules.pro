# kotlinx.serialization resolves serializers reflectively by KType, so the generated companions
# and `serializer()` methods have to survive shrinking.
-keepclasseswithmembers class com.skydoves.nowinandroid.** {
    public static ** Companion;
}
-keepclasseswithmembers class com.skydoves.nowinandroid.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Enum names are the persisted form of the theme preferences.
-keepclassmembers enum com.skydoves.nowinandroid.core.model.data.** { *; }

# Ktor's JVM utilities reference a class that does not exist on Android.
-dontwarn java.lang.management.**
