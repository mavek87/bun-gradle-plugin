# Bun Gradle Plugin

Simple Gradle plugin to interact with Bun.

This plugin is not cross-platform, it is being tested only on Ubuntu.

bash and curl need to be installed in your system to work correctly.

### How to import

**settings.gradle**
```gradle
pluginManagement {
    resolutionStrategy {
        eachPlugin { requested ->
            if (requested.target.id.toString().startsWith("com.github.")) {
                def (com, github, user, name) = requested.target.id.toString().split("\\.")
                useModule("com.github.$user:$name:${requested.target.version}")
            }
        }
    }
    repositories {
        gradlePluginPortal()
        maven { url "https://jitpack.io" }
    }
```

**build.gradle**
```gradle
plugins {
    id "com.github.mavek87.bun-gradle-plugin" version "8644649ff1"
}
```

### How to use

You can create a Gradle task for each Bun script in your package.json

### Example

**package.json**
```json
{
  "name": "example-app",
  "private": true,
  "version": "1.0.0",
  "type": "module",
  "scripts": {
    "clean": "rm -rf build",
    "build": "vite build",
    "check": "bun run test && bun run lint",
    "check:build": "bun run check && bun run build"
  }
}
```


**build.gradle**

```gradle
bun {
    package_json {
        commands = [
                "bun run clean",
                "bun run build",
                "bun run check",
                "bun run check:build"
        ]
    }
}
```