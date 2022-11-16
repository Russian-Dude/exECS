# Overview
ExECS is Event-based Entity-Component-System library for Kotlin JVM. It aims to be feature-rich, provide clean and simple-to-use API and be as performant as possible.

ExECS consists of two parts: the main library (this repo) and the [compiler plugin](https://github.com/Russian-Dude/execs-plugin) which helps to increase performance.

Detailed information about all features can be found on the [wiki](https://github.com/Russian-Dude/exECS/wiki).

The [simple example](https://github.com/Russian-Dude/exECS/wiki/Simple-example) page will help you familiarize yourself with the basic concepts of the exECS.

# Features

* **Compatible** - all main elements of the ExECS are presented as interfaces so they can be combined with classes from other libraries and frameworks if necessary

* **Declarative without sacrificing performance** - the ExECS compiler [plugin](https://github.com/Russian-Dude/execs-plugin) makes *slight* transformations to the code that leads to a *significant* performance boost

* **Flexible subscriptions by Systems** - Systems can subscribe not just to Component types but to [Immutable Component](https://github.com/Russian-Dude/exECS/wiki/Component#immutable-components) instances and [Component conditions](https://github.com/Russian-Dude/exECS/wiki/System#subscribing-to-component-composition-conditions). This helps to avoid unnecessary iterations and leads to a clearer code

* **Observable Components** - ExECS will take care of notifications about data changes in [Observable Components](https://github.com/Russian-Dude/exECS/wiki/Component#observable-components)

* **Easy to use Pools** - ExECS will automatically create a default Pool for every [Poolable](https://github.com/Russian-Dude/exECS/wiki/Poolable) type. Additionally, Events can be automatically returned to the Pool after they are fired and Components after they are no longer plugged into an Entity

* **Entity Blueprints** - use DSL to create reusable [blueprints](https://github.com/Russian-Dude/exECS/wiki/Entity#blueprints) of Entities

* **Parent-Child relations of Entities**

* **polymorphic subscriptions to Events**

* **Easy to serialize**

# Requirements
Because Kotlin API for creating compiler plugins is not yet stable only 1.6.21 and 1.7.10 versions of Kotlin are supported.

# External dependencies
ExECS uses [ronmamo reflections library](https://github.com/ronmamo/reflections)

# Installation
ExECS is available via Jitpack.

Gradle:
```kotlin
repositories {
    maven("https://jitpack.io")
}
buildscript {
    repositories {
        maven("https://jitpack.io")
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
