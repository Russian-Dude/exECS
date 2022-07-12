package com.rdude.exECS.component

import com.rdude.exECS.aspect.AspectEntryElement
import com.rdude.exECS.system.System

/** States are [Components][Component] that [Systems][System] can subscribe to the instances of.
 *  Therefore, it is assumed that the states are immutable.
 *
 *  ```
 *  enum class Color : State { RED, GREEN, BLUE } // creating State component class
 *
 *  class MySystem : ActingSystem(only = Color.RED) // subscribing to the instance
 *  ```*/
interface State : Component, AspectEntryElement