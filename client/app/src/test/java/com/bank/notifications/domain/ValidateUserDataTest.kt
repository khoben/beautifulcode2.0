package com.bank.notifications.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ValidateUserDataTest {

    private lateinit var validateUserData: ValidateUserData

    @Before
    fun setUp() {
        validateUserData = ValidateUserData.Base()
    }

    @Test
    fun `test isEmailValid with null input`() {
        val result = validateUserData.isEmailValid(null)
        assertFalse(result)
    }

    @Test
    fun `test isEmailValid with blank input`() {
        val result = validateUserData.isEmailValid("")
        assertFalse(result)
    }

    @Test
    fun `test isEmailValid with valid email`() {
        val result = validateUserData.isEmailValid("valid_email@example.com")
        assertTrue(result)
    }

    @Test
    fun `test isEmailValid with invalid email`() {
        val result = validateUserData.isEmailValid("invalid_email")
        assertFalse(result)
    }

    @Test
    fun `test isPhoneValid with null input`() {
        val result = validateUserData.isPhoneValid(null)
        assertFalse(result)
    }

    @Test
    fun `test isPhoneValid with blank input`() {
        val result = validateUserData.isPhoneValid("")
        assertFalse(result)
    }

    @Test
    fun `test isPhoneValid with valid phone`() {
        val result = validateUserData.isPhoneValid("1234567890")
        assertTrue(result)
    }

    @Test
    fun `test isPhoneValid with valid phone format#1`() {
        val result = validateUserData.isPhoneValid("+7 922 555-55-55")
        assertTrue(result)
    }

    @Test
    fun `test isPhoneValid with valid phone format#2`() {
        val result = validateUserData.isPhoneValid("+7-(922)-555-55-55")
        assertTrue(result)
    }

    @Test
    fun `test isPhoneValid with invalid phone`() {
        val result = validateUserData.isPhoneValid("123")
        assertFalse(result)
    }
}
