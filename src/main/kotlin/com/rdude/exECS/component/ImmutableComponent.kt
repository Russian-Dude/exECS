package com.rdude.exECS.component

import com.rdude.exECS.aspect.AspectEntryElement
import com.rdude.exECS.system.IterableEventSystem

/** Component with data that is assumed to be immutable.
 * Enums and Kotlin data classes are suited well to be Immutable Components.
 *
 * [IterableSystems][IterableEventSystem] can subscribe to Immutable Component conditions.
 *
 * ```
 * ```
 * *Example 1 - subscribing to the Immutable Component equality:*
 *  ```
 *  enum class Color : ImmutableComponent { RED, GREEN, BLUE } // creating component class
 *
 *  class MySystem : IterableActingSystem(only = Color.RED) // subscribing to the instance
 *  ```
 *  *Example 2 - subscribing to the Immutable Component condition:*
 *  ```
 *  data class AgeComponent(val value: Int) : ImmutableComponent // creating component class
 *
 *  class MySystem : IterableActingSystem(only = AgeComponent::class { value > 18 }) // subscribing to the component condition
 *  ```
 *  @see ObservableComponent
 *  @see RichComponent
 *  @see UniqueComponent
 *  */
interface ImmutableComponent : Component, AspectEntryElement