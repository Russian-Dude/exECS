package com.rdude.exECS.component

/** States are components that systems can subscribe to instances of.
 *  Therefore, it is assumed that the states are immutable. */
interface State : Component