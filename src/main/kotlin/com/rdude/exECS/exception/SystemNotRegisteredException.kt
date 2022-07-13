package com.rdude.exECS.exception

import com.rdude.exECS.system.System

class SystemNotRegisteredException(system: System) : ExEcsException(
    "Can not access World from System ${system::class}. System is not registered in a World")