# ATT2 fre Fabric

《Across The Time II — Time For Regrets》的客户端辅助模组修改版

基于 Poubone 的 ATT2 Mod 2.0.7，整合 fre 1.1.32 的界面与功能调整

使用chatgpt 6.1 sol修改

## 版本与依赖

| 项目 | 版本 |
| --- | --- |
| Minecraft | 1.21.11 |
| Fabric Loader | 0.18.4 或更新版本 |
| Fabric API | 对应 Minecraft 1.21.11 的版本 |
| Java | 21 或更新版本 |
| fre 修改版 | 1.1.32 |
| 官方基础版本 | 2.0.7 |

本项目仅面向 Fabric 客户端，不包含 NeoForge 版本，也不需要服务端安装本模组。
模组中的地图功能依赖 Across The Time II 的命令、物品和数据结构，不适用于普通生存世界。

## 功能概览

- 自定义 HUD、资源球、属性显示与界面排列。
- 任务书搜索、分类、置顶与快捷跟踪。
- 重制的商店、赌场、符文室、囚犯交易和属性面板。
- 法术轮盘、等级颜色、主副手支持与轮盘顺序保存。
- 配方追踪 HUD、材料高亮及符文包裹数量显示。
- 商店装备对比、品质筛选、法术隐藏与修理服务。
- 屏幕字幕接管、传送演出、战利品光柱及性能选项。
- 中英文等多语言设置与可选的 Mod Menu 设置入口。
- 可在“常规”中关闭模组界面的灰色背景，默认关闭该选项。

具体按键可在 Minecraft 的“选项 → 控制 → 按键绑定”中查看或修改。

## 从源码构建

需要 JDK 21 或更新版本和网络连接。项目自带 Gradle Wrapper，无需预先安装 Gradle。
首次构建会下载 Gradle、Minecraft 开发依赖、Fabric Loader 和 Fabric API。

Windows PowerShell：

```powershell
.\gradlew.bat clean build
```

Linux / macOS：

```bash
chmod +x gradlew
./gradlew clean build
```

构建产物位于 `build/libs/`：

- `ATT2_Project-2.0.7-custom-fre-1.1.32.jar`：用于游戏的 Fabric 模组。
- `ATT2_Project-2.0.7-custom-fre-1.1.32-sources.jar`：源码归档，不用于安装。

构建直接编译本仓库的 Java 源码和资源，不依赖历史修改版 JAR、已安装的 Minecraft 游戏目录或外部补丁脚本。

## 测试与开发

运行单元测试：

```powershell
.\gradlew.bat test
```

启动开发客户端：

```powershell
.\gradlew.bat runClient
```

`runClient` 使用独立的开发运行目录。需要自行准备合法取得的地图及其配套资源，仓库不包含地图存档或完整地图资源包。
单元测试不替代实际游戏中的界面、图形驱动、地图交互与其他模组兼容性测试。

VS Code 可直接打开项目根目录；安装 Java 和 Gradle 相关扩展后，导入 Gradle 工程即可开发。

## 目录结构

```text
src/main/java/          模组与集成代码
src/main/resources/     Fabric 元数据、Mixin 配置、语言和必要资源
src/test/java/          单元测试

gradle/wrapper/         Gradle Wrapper
build.gradle            构建配置
gradle.properties       版本与依赖配置
```

## 安装

安装对应版本的 Fabric Loader 与 Fabric API，将构建出的普通模组 JAR 放入 `mods` 文件夹。
升级时先移除旧 ATT2 版本，不要同时安装官方版和 fre 修改版。
Mod Menu 是可选集成，不是运行本模组的必需依赖。

## 来源与许可

- 上游：[Poubone/ATT2_Mod](https://github.com/Poubone/ATT2_Mod)。
- ATT2 代码遵循 MIT License，原作者版权声明见 `LICENSE`。
- 集成代码与第三方资源的来源、许可证见 `THIRD_PARTY_NOTICES.md` 及资源目录中保留的许可文件。
- 第三方美术与地图衍生资源不因包含于本项目而自动改用 MIT 许可。
- Minecraft、Fabric、Gradle 等开发依赖由构建工具获取，不随本源码仓库附带游戏二进制文件或完整映射文件。

本项目为社区修改版，与地图作者、上游模组作者及 Mojang / Microsoft 无官方隶属关系。
