package com.coditory.gradle.integration.shared

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class SemVersionTest {
    @Test
    fun `should parse sem version`() {
        val version = SemVersion.parse("1.2.3")
        assertThat(version.major).isEqualTo(1)
        assertThat(version.minor).isEqualTo(2)
        assertThat(version.patch).isEqualTo(3)
    }

    @Test
    fun `should parse sem version without patch`() {
        val version = SemVersion.parse("1.2")
        assertThat(version.major).isEqualTo(1)
        assertThat(version.minor).isEqualTo(2)
        assertThat(version.patch).isEqualTo(0)
    }

    @Test
    fun `should parse sem version with major only`() {
        val version = SemVersion.parse("1")
        assertThat(version.major).isEqualTo(1)
        assertThat(version.minor).isEqualTo(0)
        assertThat(version.patch).isEqualTo(0)
    }

    @Test
    fun `should compare two sem versions`() {
        assertThat(SemVersion.parse("1.2.3"))
            .isLessThan(SemVersion.parse("1.2.4"))
        assertThat(SemVersion.parse("1.2.3"))
            .isLessThan(SemVersion.parse("1.3.0"))
        assertThat(SemVersion.parse("1.2.3"))
            .isLessThan(SemVersion.parse("2.0.0"))
        assertThat(SemVersion.parse("1.2.3"))
            .isEqualTo(SemVersion.parse("1.2.3"))
    }

    @Test
    fun `should throw InvalidArgumentException for invalid sem version`() {
        assertThrows(IllegalArgumentException::class.java) { SemVersion.parse("WTF") }
        assertThrows(IllegalArgumentException::class.java) { SemVersion.parse("") }
        assertThrows(IllegalArgumentException::class.java) { SemVersion.parse("1.x.3") }
    }

    @Test
    fun `should parse sem version with prefix and suffix`() {
        val version = SemVersion.parse("v1.2.3-RELEASE")
        assertThat(version.major).isEqualTo(1)
        assertThat(version.minor).isEqualTo(2)
        assertThat(version.patch).isEqualTo(3)
    }

    @Test
    fun `should parse sem version for gradle release candidate`() {
        val version = SemVersion.parse("7.1-rc-1")
        assertThat(version.major).isEqualTo(7)
        assertThat(version.minor).isEqualTo(1)
        assertThat(version.patch).isEqualTo(0)
    }
}
