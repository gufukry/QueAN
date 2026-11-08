plugins {
    // AGP 9 内置 Kotlin（AGP 9.4.1 自带 Kotlin 2.4），只需再挂 Compose 编译器插件
    id("com.android.application") version "9.4.1" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.20" apply false
}
