package com.rdude.exECS.exception

import com.rdude.exECS.aspect.Aspect
import com.rdude.exECS.aspect.AspectEntry

/** Signals that equal [AspectEntry] instances have been passed to the [Aspect] parameters.*/
class AspectComponentDuplicateException internal constructor(message: String) : ExEcsException(message)