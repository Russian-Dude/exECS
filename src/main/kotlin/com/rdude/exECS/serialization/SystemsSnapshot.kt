package com.rdude.exECS.serialization

import com.rdude.exECS.system.System

class SystemsSnapshot(val subscriptions: LongArray, val systems: List<System>)