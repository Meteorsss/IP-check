# IP Check

一款 Android 公网 IP 查询应用，界面采用 **iOS 26 液态玻璃（Liquid Glass）** 风格。打开即自动从多个数据源查询你的公网 IP，并保存历史记录。

## ✨ 功能

- **打开即查**：启动 App 自动并发查询公网 IP
- **多数据源**：同时从三个来源获取并对比
  - [ping0.cc](https://ping0.cc/)
  - [ippure.com](https://ippure.com/)
  - [ipdata.co](https://ipdata.co/)（需在设置中填写免费 API Key）
- **历史记录**：每次查询自动存档，可查看/清空（最多保留 100 条）
- **液态玻璃 UI**：深色渐变 + 漂浮光晕 + 半透明磨砂玻璃卡片，纯 Jetpack Compose 绘制
- **一键复制**：点击即可复制当前 IP

## 📱 兼容性

| 项目 | 版本 |
|------|------|
| 最低系统 | Android 10（API 29） |
| 目标系统 | Android 15/16（API 35） |
| 语言 | Kotlin |
| UI | Jetpack Compose + Material 3 |

## 🔨 构建（无需本地环境）

本仓库已配置 **GitHub Actions**，推送后会自动在云端编译并产出 APK：

1. 打开仓库的 **Actions** 标签页
2. 选择最近一次 **Build APK** 工作流
3. 在 **Artifacts** 中下载 `IP-Check-debug-apk`
4. 解压后将 `.apk` 传到手机安装（需允许「未知来源」安装）

### 本地构建（可选）

```bash
gradle wrapper --gradle-version 8.10.2
./gradlew assembleDebug
# 产物：app/build/outputs/apk/debug/app-debug.apk
```

## ⚙️ 关于 ipdata.co

ipdata.co 是需要 API Key 的商业接口。前往 [ipdata.co](https://ipdata.co/) 免费注册获取 Key，在 App 右上角 **设置** 中填入即可启用该数据源；留空则自动跳过。

## 📁 项目结构

```
app/src/main/java/com/ipcheck/app/
├── MainActivity.kt            # 入口，单 Activity + Compose
├── MainViewModel.kt           # 并发查询、状态与历史管理
├── data/
│   ├── IpResult.kt            # 数据模型
│   ├── providers/             # 三个数据源实现
│   └── history/               # DataStore 持久化
├── network/HttpClient.kt      # OkHttp 客户端
└── ui/                        # 液态玻璃组件与页面
```

## 📝 说明

各数据源页面结构可能随时调整，解析采用宽松正则并对失败做了容错——单个源失败不影响其它源展示。
