package com.rdude.exECS.component

interface Component {

    companion object : Component {

        val DUMMY_COMPONENT = this

    }

}