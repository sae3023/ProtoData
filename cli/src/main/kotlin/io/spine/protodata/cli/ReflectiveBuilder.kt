/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.spine.protodata.cli

import kotlin.reflect.KClass
import kotlin.reflect.KVisibility

/**
 * A builder for creating instances of classes defined by the users of this library.
 *
 * The class is loaded via a `ClassLoader` and an instance is created. It is expected that
 * the class has a `public` constructor with no parameters.
 */
internal open class ReflectiveBuilder<T: Any> {

    /**
     * Creates instances of `T` from the givne class names.
     *
     * It is necessary that the classes defined by the [classNames] parameter are subtypes of `T`.
     * Otherwise, a casting error occurs.
     *
     * @param classNames
     *     names of concrete classes to instantiate
     * @param classLoader
     *     the [ClassLoader] to load the class by its name
     * @see createByName
     */
    fun createAll(classNames: List<String>, classLoader: ClassLoader) =
        classNames.map {
            createByName(it, classLoader)
        }

    /**
     * Creates an instance of `T`.
     *
     * It is necessary that the class defined by the [className] parameter is a subtype of `T`.
     * Otherwise, a casting error occurs.
     *
     * @param className name of the concrete class to instantiate
     * @param classLoader the [ClassLoader] to load the class by its name
     */
    private fun createByName(className: String, classLoader: ClassLoader): T {
        val cls = classLoader.loadClass(className).kotlin
        @Suppress("UNCHECKED_CAST")
        val tClass = cls as KClass<T>
        return create(tClass)
    }

    private fun create(cls: KClass<T>) : T {
        val ctor = cls.constructors.find {
            it.visibility == KVisibility.PUBLIC && it.parameters.isEmpty()
        } ?: throw IllegalStateException(
            "Class `${cls.qualifiedName} should have a public zero-parameter constructor.`"
        )
        val instance = ctor.call()
        prepareInstance(instance)
        return instance
    }

    /**
     * Initializes the [instance] after creation.
     *
     * This preparation is executed before the instance is returned to the caller of
     * [createByName].
     */
    protected open fun prepareInstance(instance: T) {
    }
}
