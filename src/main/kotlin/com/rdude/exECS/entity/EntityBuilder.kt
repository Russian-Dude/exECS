package com.rdude.exECS.entity

import com.rdude.exECS.component.Component
import com.rdude.exECS.pool.Pool
import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.pool.fromPool
import com.rdude.exECS.world.World
import kotlin.reflect.KClass

class EntityBuilder<T : EntityBlueprintConfiguration> @PublishedApi internal constructor() : Poolable {

    internal lateinit var world: World

    private var currentEntity = Entity.NO_ENTITY

    private lateinit var defaultConfig: () -> T

    private var lazyConfig: T? = null


    /** Adds [component] to the [Entity] that will be generated from the [EntityBlueprint] of the current scope.*/
    fun withComponent(component: Component) {
        val componentTypeId = component.getComponentTypeId()
        // component may be also presented if parent blueprint added it
        val alreadyPresented = world.entityMapper.componentMappers[componentTypeId].hasComponent(getCurrentEntityId())
        if (alreadyPresented) return
        world.entityMapper.componentMappers[componentTypeId].addComponentUnsafe(getCurrentEntityId(), component, world.configuration.queueComponentAddedWhenEntityAdded)
    }


    /** Adds Component from the default Pool to the [Entity]
     * that will be generated from the [EntityBlueprint] of the current scope.*/
    fun <T> withComponent(cl: KClass<T>) where T : Component, T : Poolable =
        withComponent(fromPool(cl))


    /** Adds customized by the [apply] function Component from the default Pool to the [Entity]
     * that will be generated from the [EntityBlueprint] of the current scope.*/
    inline fun <T> withComponent(cl: KClass<T>, apply: T.() -> Unit) where T : Component, T : Poolable {
        val component = fromPool(cl)
        component.apply()
        withComponent(component)
    }


    /** Adds Component from the default Pool to the [Entity]
     * that will be generated from the [EntityBlueprint] of the current scope.*/
    inline fun <reified T> withComponent() where T : Component, T : Poolable =
        withComponent(T::class)


    /** Adds customized by the [apply] function Component from the default Pool to the [Entity]
     * that will be generated from the [EntityBlueprint] of the current scope.*/
    inline fun <reified T> withComponent(apply: T.() -> Unit) where T : Component, T : Poolable =
        withComponent(T::class, apply)


    /** Creates an [Entity] from [this] customized blueprint
     * and adds it as a child to the [Entity] that will be created from the [EntityBlueprint] of the current scope. */
    @Suppress("UNCHECKED_CAST")
    operator fun <P : EntityBlueprintConfiguration> EntityBlueprint<P>.invoke(apply: EntityBuilder<P>.() -> Unit) {
        val builder = EntityBuilder.pool.obtain() as EntityBuilder<P>
        val childId = builder.startBuilding(world, this.defaultConfiguration)
        builder.apply()
        val config = builder.lazyConfig ?: this.defaultConfiguration.invoke()
        this.endBuildingFun.invoke(builder, config)
        builder.returnToPool()
        if (config is Poolable) config.returnToPool()
        world.entityMapper.addChildEntity(getCurrentEntityId(), childId)
    }


    /** Creates an [Entity] from [this] blueprint with default configuration
     * and adds it as a child to the [Entity] that will be created from the [EntityBlueprint] of the current scope.*/
    @Suppress("UNCHECKED_CAST")
    operator fun <P : EntityBlueprintConfiguration> EntityBlueprint<P>.invoke() {
        val builder = EntityBuilder.pool.obtain() as EntityBuilder<P>
        val config = this.defaultConfiguration.invoke()
        val childId = this.fullBuildingFun.invoke(world, builder, config)
        builder.returnToPool()
        if (config is Poolable) config.returnToPool()
        world.entityMapper.addChildEntity(getCurrentEntityId(), childId)
    }


    /** Creates an [Entity] from [this] blueprint with custom configuration
     * and adds it as a child to the [Entity] that will be created from the [EntityBlueprint] of the current scope.*/
    @Suppress("UNCHECKED_CAST")
    fun <P : EntityBlueprintConfiguration> EntityBlueprint<P>.withConfig(apply: P.() -> Unit) {
        val builder = EntityBuilder.pool.obtain() as EntityBuilder<P>
        val config = this.defaultConfiguration.invoke()
        config.apply()
        val childId = this.fullBuildingFun.invoke(world, builder, config)
        builder.returnToPool()
        if (config is Poolable) config.returnToPool()
        world.entityMapper.addChildEntity(getCurrentEntityId(), childId)
    }


    /** Creates an [Entity] from [this] blueprint, also adding [component] to it.
     * Adds created Entity as a child to the Entity that will be created from the [EntityBlueprint] of the current scope.*/
    @Suppress("UNCHECKED_CAST")
    fun <P : EntityBlueprintConfiguration> EntityBlueprint<P>.withComponent(component: Component) {
        val builder = EntityBuilder.pool.obtain() as EntityBuilder<P>
        val childId = builder.startBuilding(world, this.defaultConfiguration)
        builder.withComponent(component)
        val config = this.defaultConfiguration.invoke()
        this.endBuildingFun.invoke(builder, config)
        builder.returnToPool()
        if (config is Poolable) config.returnToPool()
        world.entityMapper.addChildEntity(getCurrentEntityId(), childId)
    }


    /** Creates an [Entity] from [this] blueprint, also adding component from the default Pool to it.
     * Adds created Entity as a child to the Entity that will be created from the [EntityBlueprint] of the current scope.*/
    fun <C> EntityBlueprint<*>.withComponent(componentCl: KClass<C>) where C : Component, C : Poolable =
        this.withComponent(fromPool(componentCl))


    /** Creates an [Entity] from [this] blueprint, also adding component from the default Pool to it.
     * Adds created Entity as a child to the Entity that will be created from the [EntityBlueprint] of the current scope.*/
    inline fun <reified C> EntityBlueprint<*>.withComponent() where C : Component, C : Poolable =
        this.withComponent(C::class)


    /** Creates an [Entity] from [this] blueprint, also adding configured component from the default Pool to it.
     * Adds created Entity as a child to the Entity that will be created from the [EntityBlueprint] of the current scope.*/
    inline fun <C> EntityBlueprint<*>.withComponent(componentCl: KClass<C>, apply: C.() -> Unit) where C : Component, C : Poolable =
        this.withComponent(fromPool(componentCl).apply(apply))


    /** Creates an [Entity] from [this] blueprint, also adding configured component from the default Pool to it.
     * Adds created Entity as a child to the Entity that will be created from the [EntityBlueprint] of the current scope.*/
    inline fun <reified C> EntityBlueprint<*>.withComponent(apply: C.() -> Unit) where C : Component, C : Poolable =
        this.withComponent(C::class, apply)


    /** Applies configuration customization to the [EntityBlueprint] of the current scope.*/
    fun withConfig(apply: T.() -> Unit) {
        val config = lazyConfig ?: defaultConfig.invoke().apply { this@EntityBuilder.lazyConfig = this }
        config.apply()
    }


    @PublishedApi
    internal fun startBuilding(world: World, defaultConfig: () -> T): Int {
        this.world = world
        this.defaultConfig = defaultConfig
        currentEntity = Entity(world.entityMapper.create())
        return currentEntity.id
    }

    @PublishedApi
    internal fun endBuilding(configuration: T, apply: EntityBuilder<T>.(properties: T) -> Unit) {
        this.apply(configuration)
    }

    internal fun endBuilding(apply: EntityBuilder<T>.() -> Unit) {
        this.apply()
    }

    @PublishedApi
    internal fun getCurrentEntityId(): Int = currentEntity.id

    override fun reset() {
        currentEntity = Entity.NO_ENTITY
        lazyConfig = null
    }

    @PublishedApi
    internal companion object {
        @JvmField
        @PublishedApi
        internal val pool = Pool { EntityBuilder<EntityBlueprintConfiguration>() }
    }

}