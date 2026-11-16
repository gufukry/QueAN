# 保留 Compose / miuix 的反射与资源引用
-keep class androidx.compose.** { *; }
-keep class top.yukonga.miuix.** { *; }
-dontwarn androidx.compose.**
-dontwarn top.yukonga.**

# JNI 与反射入口（若后续接入 so）
-keepclasseswithmembernames class * { native <methods>; }
