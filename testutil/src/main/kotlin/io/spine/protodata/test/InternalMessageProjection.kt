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

package io.spine.protodata.test

import io.spine.core.External
import io.spine.core.Subscribe
import io.spine.protodata.TypeEntered
import io.spine.protodata.TypeName
import io.spine.server.projection.Projection
import io.spine.server.projection.ProjectionRepository
import io.spine.server.route.EventRoute.withId
import io.spine.server.route.EventRouting

public class InternalMessageProjection
    : Projection<TypeName, InternalType, InternalType.Builder>() {

    @Subscribe
    internal fun on(@External e: TypeEntered) {
        builder().name = e.type.name
    }
}

public class InternalMessageRepository :
    ProjectionRepository<TypeName, InternalMessageProjection, InternalType>() {

    protected override fun setupEventRouting(routing: EventRouting<TypeName>) {
        super.setupEventRouting(routing)
        routing.route(TypeEntered::class.java) { event, _ ->
            withId(event.type.name)
        }
    }
}
