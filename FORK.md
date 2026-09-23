# Cresto 定制版（happy-everyday-everyweek fork）

本仓库是 [Nevodev/Cresto](https://github.com/Nevodev/Cresto) 的定制分支，在保持与上游同步的前提下，维护若干自用功能，并自动发布可直接安装的构建。

## 与上游的关系

上游不接收这里的改动，所以这些功能在本仓库单独维护：上游代码持续同步进来，我们的改动单独放在 `custom` 分支上，二者通过合并保持共存。

## 分支约定

- `master`：与上游 `Nevodev/Cresto` 的 master 保持同步，另外承载本仓库的 CI/CD 工作流与说明文档。`Sync upstream` 工作流每天自动把上游提交合并进来。
- `custom`：我们的定制版本，内容为 master 加上下面的定制功能。发布 tag 都打在这个分支上。
- `feat/*`：单个功能的开发分支。

## 定制功能

- 首页下拉进入沉浸模式：在原地隐藏顶栏（搜索、排序、加号）、列表标题行与分组选择行、底部标签栏与渐变遮罩；横屏时同一批卡片按两列排列；按返回键退出。
- AI 增强（对应上游 issue #33 的第 1、4 条）：创建待办时可选择让 AI 读取分组与标记；AI 设置页提供 API 地址预设与“获取模型列表”按钮。
- 修复 API 地址拼接：地址中版本段不在末尾时（例如 `https://api.commandcode.ai/provider/v1`），原先会被拼成 `/v1/v1/chat/completions` 导致请求失败，现已修正。
- 应用内更新检查：改为读取本仓库最新 Release 的更新清单，只会提示定制版的新版本，不再提示上游版本。

## 版本号与发布

定制版本的 tag 形如 `v1.0-alpha1084-custom.1`。其中 `alpha1084` 是写入 APK 的 versionCode，后缀 `custom.N` 表示第 N 次定制发布。versionCode 保持大于上游当前版本，方便直接覆盖安装。

发布流程是在 `custom` 分支上打 tag 并推送，`Release` 工作流会自动构建 APK、生成 SHA256 校验文件并创建 GitHub Release：

```bash
git checkout custom
git pull
git tag v1.0-alpha1085-custom.1
git push origin v1.0-alpha1085-custom.1
```

也可以在 Actions 页面手动触发 `Release` 工作流并填入 tag 名。

## 工作流

- `CI`：在 master、custom、feat/* 的推送与 PR 上编译 Kotlin 源码并运行单元测试。
- `Release`：tag 推送或手动触发，构建 APK 并创建 Release。
- `Sync upstream`：每天定时把上游 master 合并进本仓库 master，再把 master 合并进 custom。发生冲突时不推送，并自动开 Issue 提醒手动处理。

## 安装说明

发布附件是 debug 签名的 APK，可以直接安装。由于签名与上游官方版不同，从上游版本切换过来时需要先卸载再安装。若希望发布正式签名包，可在仓库 Secrets 中配置 `MOMENTO_SIGNING_STORE_FILE`、`MOMENTO_SIGNING_STORE_PASSWORD`、`MOMENTO_SIGNING_KEY_ALIAS`、`MOMENTO_SIGNING_KEY_PASSWORD`，再把 `Release` 工作流改用 `assembleRelease`。

应用内的“检查更新”读取的是本仓库最新 Release 里的更新清单（`https://github.com/happy-everyday-everyweek/Cresto/releases/latest/download/latest.json`），该文件由 `Release` 工作流在发布时自动生成并上传，所以在 custom 分支构建的版本只会提示本仓库的新版本，不会提示上游版本。

## 本地构建

```bash
./gradlew :app:assembleDebug
```

需要 JDK 17 与 Android SDK platform 37（compileSdk 37、minSdk 30）。产物位于 `app/build/outputs/apk/debug/`。
