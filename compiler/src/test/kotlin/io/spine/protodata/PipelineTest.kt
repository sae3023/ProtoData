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

import com.google.common.truth.Truth.assertThat
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest
import io.spine.protodata.renderer.SourceSet
import io.spine.protodata.renderer.codeLine
import io.spine.protodata.test.CatOutOfTheBoxEmancipator
import io.spine.protodata.test.DeletingRenderer
import io.spine.protodata.test.DoctorProto
import io.spine.protodata.test.GenericInsertionPoint
import io.spine.protodata.test.InternalAccessRenderer
import io.spine.protodata.test.JavaGenericInsertionPointPrinter
import io.spine.protodata.test.Journey
import io.spine.protodata.test.JsRenderer
import io.spine.protodata.test.KtRenderer
import io.spine.protodata.test.PrependingRenderer
import io.spine.protodata.test.TestPlugin
import io.spine.protodata.test.TestRenderer
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeBytes
import kotlin.io.path.writeText
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class `'Pipeline' should` {

    private lateinit var srcRoot : Path
    private lateinit var codegenRequestFile: Path
    private lateinit var sourceFile: Path
    private lateinit var request: CodeGeneratorRequest
    private lateinit var renderer: TestRenderer

    @BeforeEach
    fun prepareSources(@TempDir sandbox: Path) {
        srcRoot = sandbox.resolve("src")
        srcRoot.toFile().mkdirs()
        codegenRequestFile = sandbox.resolve("code-gen-request.bin")

        sourceFile = write("SourceCode.java", """
            ${Journey::class.simpleName} worth taking
        """.trimIndent())

        val protoFile = DoctorProto.getDescriptor()
        request = CodeGeneratorRequest
            .newBuilder()
            .addProtoFile(protoFile.toProto())
            .addFileToGenerate(protoFile.name)
            .build()
        codegenRequestFile.writeBytes(request.toByteArray())
        renderer = TestRenderer()
    }

    private fun write(path: String, code: String): Path {
        val file = srcRoot.resolve(path)
        file.parent.toFile().mkdirs()
        file.writeText(code)
        return file
    }

    @Test
    fun `render enhanced code`() {
        Pipeline(
            listOf(TestPlugin()),
            listOf(renderer),
            SourceSet.fromContentsOf(srcRoot),
            request
        )()
        assertThat(sourceFile.readText())
            .isEqualTo("_Journey worth taking")
    }

    @Test
    fun `generate new files`() {
        Pipeline(
            listOf(TestPlugin()),
            listOf(InternalAccessRenderer()),
            SourceSet.fromContentsOf(srcRoot),
            request
        )()
        val newClass = srcRoot.resolve("spine/protodata/test/JourneyInternal.java")
        assertThat(newClass.exists())
            .isTrue()
        assertThat(newClass.readText())
            .contains("class JourneyInternal")
    }

    @Test
    fun `delete files`() {
        val sourceFile = write("io/spine/protodata/test/_DeleteMe.java", "foo bar")
        Pipeline(
            listOf(TestPlugin()),
            listOf(DeletingRenderer()),
            SourceSet.fromContentsOf(srcRoot),
            request
        )()
        assertThat(sourceFile.exists())
            .isFalse()
    }

    @Test
    fun `write into insertion points`() {
        val initialContent = "foo bar"
        val sourceFile = write("io/spine/protodata/test/_DeleteMe.java", initialContent)
        val renderer = PrependingRenderer()
        Pipeline(
            listOf(TestPlugin()),
            listOf(JavaGenericInsertionPointPrinter(), renderer),
            SourceSet.fromContentsOf(srcRoot),
            request
        )()
        assertThat(sourceFile.readText())
            .isEqualTo("""
                // INSERT:'file_start'
                Hello from ${renderer.javaClass.name}
                $initialContent
                // INSERT:'file_end'
            """.trimIndent())
    }

    @Test
    fun `use different renderers for different files`() {
        val jsSource = write("test/source.js", "alert('Hello')")
        val ktSource = write("corp/acme/test/Source.kt", "println(\"Hello\")")
        Pipeline(
            listOf(TestPlugin()),
            listOf(JsRenderer(), KtRenderer()),
            SourceSet.fromContentsOf(srcRoot),
            request
        )()
        assertThat(jsSource.readText())
            .contains("Hello JavaScript")
        assertThat(ktSource.readText())
            .contains("Hello Kotlin")
    }

    @Test
    fun `add insertion points`() {
        Pipeline(
            listOf(TestPlugin()),
            listOf(JavaGenericInsertionPointPrinter(), CatOutOfTheBoxEmancipator()),
            SourceSet.fromContentsOf(srcRoot),
            request
        )()
        val assertCode = assertThat(sourceFile.readText())
        assertCode
            .startsWith("// ${GenericInsertionPoint.FILE_START.codeLine}")
        assertCode
            .endsWith("// ${GenericInsertionPoint.FILE_END.codeLine}")
        assertCode
            .doesNotContain(GenericInsertionPoint.OUTSIDE_FILE.codeLine)
    }

    @Test
    fun `not add insertion points if nobody touches the file contents`() {
        Pipeline(
            listOf(TestPlugin()),
            listOf(JavaGenericInsertionPointPrinter(), JsRenderer()),
            SourceSet.fromContentsOf(srcRoot),
            request
        )()
        val assertCode = assertThat(sourceFile.readText())
        assertCode
            .doesNotContain(GenericInsertionPoint.FILE_START.codeLine)
        assertCode
            .doesNotContain(GenericInsertionPoint.FILE_END.codeLine)
        assertCode
            .doesNotContain(GenericInsertionPoint.OUTSIDE_FILE.codeLine)
    }
}
