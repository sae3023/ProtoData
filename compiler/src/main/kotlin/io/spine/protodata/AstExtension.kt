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

package io.spine.protodata

import com.google.protobuf.Descriptors.FieldDescriptor
import com.google.protobuf.Descriptors.OneofDescriptor

/**
 * Obtains the package and the name of the type.
 */
public fun MessageType.qualifiedName(): String = name.qualifiedName()

/**
 * Obtains the type URl of the type.
 *
 * A type URL contains the type URL prefix and the qualified name of the type separated by
 * the slash (`/`) symbol. See the docs of `google.protobuf.Any.type_url` for more info.
 *
 * @see MessageType.qualifiedName
 * @see TypeName.typeUrl
 */
public fun MessageType.typeUrl(): String = name.typeUrl()

/**
 * Obtains the package and the name from this `TypeName`.
 */
public fun TypeName.qualifiedName(): String = "${packageName}.${simpleName}"

/**
 * Obtains the type URl from this `TypeName`.
 *
 * A type URL contains the type URL prefix and the qualified name of the type separated by
 * the slash (`/`) symbol. See the docs of `google.protobuf.Any.type_url` for more info.
 *
 * @see TypeName.qualifiedName
 * @see MessageType.typeUrl
 */
public fun TypeName.typeUrl(): String = "${typeUrlPrefix}/${qualifiedName()}"

/**
 * Shows if this field is a `map`.
 *
 * If the field is a `map`, the `Field.type` contains the type of the value, and
 * the `Field.map.key_type` contains the type the the map key.
 */
public fun Field.isMap(): Boolean = hasMap()

/**
 * Shows if this field is a list.
 *
 * In Protobuf `repeated` keyword denotes a sequence of values for a field. However, a map is also
 * treated as a repeated field for serialization reasons. We use the term "list" for repeated fields
 * which are not maps.
 */
public fun Field.isList(): Boolean = hasList()

/**
 * Shows if this field repeated.
 *
 * Can be declared in Protobuf either as a `map` or a `repeated` field.
 */
public fun Field.isRepeated(): Boolean = isMap() || isList()

/**
 * Shows if this field is a part of a `oneof` group.
 *
 * If the field is a part of a `oneof`, the `Field.oneof_name` contains the name of that `oneof`.
 */
public fun Field.isPartOfOneof(): Boolean = hasOneofName()

internal fun OneofDescriptor.name(): OneofName =
    OneofName.newBuilder()
        .setValue(name)
        .build()

internal fun FieldDescriptor.name(): FieldName =
    FieldName.newBuilder()
        .setValue(name)
        .build()

internal fun PrimitiveType.asType() : Type {
    return Type.newBuilder()
        .setPrimitive(this)
        .build()
}
