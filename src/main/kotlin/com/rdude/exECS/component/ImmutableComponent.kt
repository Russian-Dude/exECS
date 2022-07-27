package com.rdude.exECS.component

import com.rdude.exECS.aspect.AspectEntryElement
import com.rdude.exECS.system.System

/** [Systems][System] can subscribe directly to the instances of ImmutableComponents.
 *
 *  ```
 *  enum class Color : ImmutableComponent { RED, GREEN, BLUE } // creating component class
 *
 *  class MySystem : IterableActingSystem(only = Color.RED) // subscribing to the instance
 *  ```*/
interface ImmutableComponent : Component, AspectEntryElement