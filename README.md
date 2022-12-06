# Overview
ExECS is an Event-based entity component system library for Kotlin JVM. It aims to be feature-rich, provide clean and simple-to-use API and be as performant as possible.

ExECS consists of two parts: the main library (this repo) and the [compiler plugin](https://github.com/Russian-Dude/execs-plugin) which helps to increase performance.

Detailed information about all features can be found on the [wiki](https://github.com/Russian-Dude/exECS/wiki).

The [simple example](https://github.com/Russian-Dude/exECS/wiki/Simple-example) page will help you familiarize yourself with the basic concepts of the exECS.

# Features

* **No boilerplate** - ExECS is designed to be convenient and simple to use

* **Event based** - create more flexible Systems by combining subscriptions to Entities with subscriptions to Events

* **Compatible** - all main elements of the ExECS are presented as interfaces so they can be combined with classes from other libraries and frameworks if necessary

* **Declarative without sacrificing performance** - the ExECS compiler [plugin](https://github.com/Russian-Dude/execs-plugin) makes *slight* transformations to the code that leads to a *significant* performance boost

* **Flexible subscriptions by Systems** - Systems can subscribe not just to Component types but to advanced Component [conditions](https://github.com/Russian-Dude/exECS/wiki/System#subscribing-to-component-composition-conditions). This helps to avoid unnecessary iterations and leads to a clearer code

* **Observable Components** - ExECS will take care of sending notifications about data changes in [Observable Components](https://github.com/Russian-Dude/exECS/wiki/Component#observable-components)

* **Easy to use Pools** - ExECS will automatically create a default Pool for every [Poolable](https://github.com/Russian-Dude/exECS/wiki/Poolable) type. Additionally, Events can be automatically returned to the Pool after they are fired and Components after they are no longer plugged into an Entity

* **Entity Blueprints** - DSL to create reusable [blueprints](https://github.com/Russian-Dude/exECS/wiki/Entity#blueprints) of Entities

* **Parent-Child relations of Entities**

* **UI logic friendly** - ever heard that the ECS approach is not great for creating UI logic? Forget it!

* **Polymorphic subscriptions to Events**

* **Easy to serialize**

# Requirements
To use ExECS with the compiler plugin, Kotlin version 1.7.21 is required.

To use ExECS without the compiler plugin, at least Kotlin version 1.6.21 is required. Note that ExECS can work without the plugin, but it is an important part of the library and it's highly recommended to use it.

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
        classpath("com.github.Russian-Dude:execs-plugin:1.5.1-1")
    }
}

dependencies {
    implementation("com.github.Russian-Dude:exECS:1.5.1")
}

apply(plugin = "execs-plugin")
```
