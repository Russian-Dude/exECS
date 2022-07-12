package com.rdude.exECS.component

import com.rdude.exECS.aspect.Aspect
import com.rdude.exECS.system.System

/** To use [ComponentCondition] in [System]'s [Aspect], [Component] must know to which entities it is plugged into.*/
sealed interface CanBeObservedBySystem