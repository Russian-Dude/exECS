# exECS
Event-based Entity-Component-System library for Kotlin JVM. It aims to be feature-rich, provide clean and simple-to-use API and be as performant as possible.

ExECS consists of two parts: the main library (this repo) and the [compiler plugin](https://github.com/Russian-Dude/execs-plugin) which helps to increase performance.

Detailed information about all features can be found on the [wiki](https://github.com/Russian-Dude/exECS/wiki).

The [simple example](https://github.com/Russian-Dude/exECS/wiki/Simple-example) page will help you familiarize yourself with the basic concepts of the exECS.

# Requirements
Because Kotlin API for creating compiler plugins is not yet stable only 1.6.21 and 1.7.10 versions of Kotlin are supported.

# External dependencies
ExECS uses [ronmamo reflections library](https://github.com/ronmamo/reflections)

# Installation
ExECS is available by Jitpack.

Gradle:
```kotlin
repositories {
    maven(uri("https://jitpack.io"))
}
buildscript {
    repositories {
        this.maven("https://jitpack.io")
    }
    dependencies {
        classpath("com.github.Russian-Dude:execs-plugin:1.5.0-1")
        // use "com.github.Russian-Dude:execs-plugin:1.5.0-k1.6.21-1" with Kotlin 1.6.21
    }
}
dependencies {
    implementation("com.github.Russian-Dude:exECS:1.5.0")
}
apply(plugin = "execs-plugin")
```
