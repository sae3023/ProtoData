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

package io.spine.protodata.renderer

import io.spine.base.EntityState
import io.spine.protodata.QueryBuilder
import io.spine.protodata.subscriber.CodeEnhancement
import io.spine.server.BoundedContext

/**
 * A `Renderer` takes an existing source set, modifies it with a number of
 * [enhancements][CodeEnhancement], including changing the contents of existing source files or
 * creating new ones, and renders the resulting code into a [SourceSet].
 *
 * Instances of `Renderer`s are created via reflection. It is required that the concrete classes
 * have a `public` no-argument constructor.
 */
public abstract class Renderer {

    /**
     * The code enhancements to apply to the source files.
     */
    public var enhancements: List<CodeEnhancement> = listOf()
        set(value) {
            if (field.isNotEmpty()) {
                throw IllegalStateException("Cannot reassign `enhancements`.")
            }
            field = value
        }

    internal lateinit var protoDataContext: BoundedContext

    /**
     * Processes the given `sources` in accordance with the [enhancements].
     *
     * If a file is present in the input source set but not the output, the file is left untouched.
     * If a file is present in the output source set but not the input, the file created.
     * If a file is present is both the input and the output source sets, the file is overridden.
     */
    public abstract fun render(sources: SourceSet): SourceSet


    /**
     * Creates a [QueryBuilder] to find projections.
     *
     * If any extra context is needed, additionally to the data provided in the event, projections
     * are the way to assemble such context.
     *
     * Projections are built on top of the same events that are given for processing to
     * the subscribers. However, guaranteed to be built completely, i.e. receive all the events,
     * before the subscribers start receiving the events. This means that the subscribers can see
     * the "full picture" via projections.
     *
     * Users may create their own projections and register them in
     * the [Pipeline][io.spine.protodata.Pipeline] via their repositories.
     */
    protected fun <P : EntityState> select(type: Class<P>): QueryBuilder<P> {
        return QueryBuilder(protoDataContext, type, javaClass.name)
    }
}
