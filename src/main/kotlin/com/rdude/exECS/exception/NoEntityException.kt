package com.rdude.exECS.exception

import com.rdude.exECS.entity.Entity

/** Thrown when trying to change [Entity.NO_ENTITY] state.*/
class NoEntityException(message: String) : ExEcsException(message)