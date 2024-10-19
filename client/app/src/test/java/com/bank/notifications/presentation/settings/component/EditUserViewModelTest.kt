package com.bank.notifications.presentation.settings.component

import com.bank.notifications.domain.ValidateUserData
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EditUserViewModelTest {
    private lateinit var viewModel: EditUserViewModel
    private lateinit var validateUserData: ValidateUserData

    @Before
    fun setup() {
        validateUserData = ValidateUserData.Base()
        viewModel = EditUserViewModel(validateUserData)
    }

    @Test
    fun `test isEmailValid with empty`() {
        val result = viewModel.validate(EditType.EMAIL, "")
        assertFalse(result)
    }

    @Test
    fun `test isEmailValid with valid email`() {
        val result = viewModel.validate(EditType.EMAIL, "valid_email@example.com")
        assertTrue(result)
    }

    @Test
    fun `test isEmailValid with invalid email`() {
        val result = viewModel.validate(EditType.EMAIL, "invalid_email@example")
        assertFalse(result)
    }

    @Test
    fun `test isPhoneValid with blank`() {
        val result = viewModel.validate(EditType.PHONE, "")
        assertFalse(result)
    }

    @Test
    fun `test isPhoneValid with valid phone`() {
        val result = viewModel.validate(EditType.PHONE, "1234567890")
        assertTrue(result)
    }

    @Test
    fun `test isPhoneValid with valid phone formatted`() {
        val result = viewModel.validate(EditType.PHONE, "+7 922 555-55-55")
        assertTrue(result)
    }

    @Test
    fun `test isPhoneValid with valid phone formatted with parentheses`() {
        val result = viewModel.validate(EditType.PHONE, "+7-(922)-555-55-55")
        assertTrue(result)
    }

    @Test
    fun `test isPhoneValid with invalid phone few digits`() {
        val result = viewModel.validate(EditType.PHONE, "123")
        assertFalse(result)
    }

    @Test
    fun `test isPhoneValid with invalid phone many digits`() {
        val result = viewModel.validate(EditType.PHONE, "123456789023445678965567")
        assertFalse(result)
    }

}