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

# Code examples

<details> 
  <summary>Starting ExECS</summary>
  
   ```kotlin
   fun main() {

        // Worlds are main entries
        val world = World()

        // Register all Systems available in the classpath (can be done manually)
        // Auto-registration can be customized
        world.autoRegisterSystems()
        
        // Create Entity. Can be done inside Systems
        world.createEntity(
            NameComponent("Julia"),
            GenderComponent.FEMALE,
            fromPool<AgeComponent> { value = 26 }
        )

        // Process the World by calling the `act` method whenever needed
        while (true) {
            world.act()
        }
    }
   ```
   
</details>

<details> 
  <summary>Components</summary>
  
   ```kotlin
// Use simple Components
class NameComponent(val name: String) : Component

// Or combine different interfaces to make advanced Components
class AgeComponent : ObservableIntComponent(), RichComponent, Poolable

enum class GenderComponent : ImmutableComponent {
        MALE, FEMALE
}
   ```
   
</details>

<details> 
  <summary>Events</summary>
  
   ```kotlin
// Subscriptions to Events are polymorphic

interface HolidayEvent : Event

class WomenDayEvent : HolidayEvent, Poolable
   ```
   
</details>

<details> 
  <summary>Systems</summary>
  
   ```kotlin
    // This System acts every tick
    class PrintTickSystem : ActingSystem() {

        override fun act() {
            println("Tick!")
        }
    }

    // This System acts every time HolidayEvent or its ancestor is fired
    class PrintItsHolidaySystem : EventSystem<HolidayEvent>() {

        override fun eventFired(event: HolidayEvent) {
            println("Wow, such a good day to be a holiday!")
        }
    }

    // This System acts every tick and iterates through the Entities with NameComponent
    class PrintHelloSystem : IterableActingSystem(only = NameComponent::class) {

        override fun act(entity: Entity) {
            val name = entity<NameComponent>()!!.name
            println("Hello, $name!")
        }
    }

    // This System acts every time WomenDayEvent is fired and iterates through the Entities
    // that are females, with age over or equals 18, and have a name
    class CongratsAdultWomenSystem : IterableEventSystem<WomenDayEvent>(
        allOf = NameComponent::class and GenderComponent.FEMALE and AgeComponent::class { value >= 18 }
    ) {

        override fun eventFired(entity: Entity, event: WomenDayEvent) {
            println("Happy International Women's Day, ${entity<NameComponent>()!!.name}!")
        }
    }
   ```
   
</details>

<details> 
  <summary>Entity Blueprints</summary>
  
   ```kotlin
    // Simple blueprint
    val justManBP = EntityBlueprint {
        withComponent(GenderComponent.MALE)
    }

    // Blueprint that allows to customize an Entity on creation
    class PersonBPConfig(var name: String, var age: Int, var gender: GenderComponent) : EntityBlueprintConfiguration(), Poolable

    val personBP = EntityBlueprint<PersonBPConfig> { config ->
        withComponent(NameComponent(config.name))
        withComponent(config.gender)
        withComponent<AgeComponent> { value = config.age }
    }


    // Creating Entities somewhere inside System:

    createEntity(justManBP)

    createEntity(personBP) {
        name = "Winston"
        gender = GenderComponent.MALE
        age = 39
    }
   ```
   
</details>

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
    implementation("com.github.Russian-Dude:exECS:1.5.2")
}

apply(plugin = "execs-plugin")
```
