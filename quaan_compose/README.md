# QueAN 启动器（Compose + 液态玻璃）

Kotlin + Jetpack Compose + Miuix，底栏使用 Kyant0/AndroidLiquidGlass 的 AGSL 折射着色器
（经 compose-miuix 示例镜像）。

构建：Android Studio / GitHub Actions（需 Gradle + Android SDK，AIDE 不支持 Compose）

- 玻璃组件来源：
  - `me/weishu/kernelsu/ui/component/liquid/*` — Kyant0/AndroidLiquidGlass (Apache-2.0)
  - `me/weishu/kernelsu/ui/component/FloatingBottomBar.kt` — compose-miuix-ui 示例 (Apache-2.0)
  - `me/weishu/kernelsu/ui/component/miuix/animation/*` — 同上

minSdk 33（blur/RuntimeShader 需要），targetSdk 35。
