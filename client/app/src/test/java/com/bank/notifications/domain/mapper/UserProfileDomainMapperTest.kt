package com.bank.notifications.domain.mapper

import com.bank.notifications.common.mapper.MapperResult
import com.bank.notifications.data.cloud.UserProfileResponse
import com.bank.notifications.domain.model.UserProfileDomain
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UserProfileDomainMapperTest {
    private lateinit var userProfileDomainMapper: UserProfileDomainMapper

    @Before
    fun setUp() {
        userProfileDomainMapper = UserProfileDomainMapper()
    }

    @Test
    fun `test map with valid data`() {
        val mockResponse = UserProfileResponse(
            email = "test@test.com",
            phone = "test_phone"
        )
        val result = userProfileDomainMapper.map(mockResponse)
        val expected = UserProfileDomain(email = "test@test.com", phone = "test_phone")
        assertTrue(result is MapperResult.Success && result.data == expected)
    }

    @Test
    fun `test map with invalid data`() {
        var mockResponse = UserProfileResponse()
        var result = userProfileDomainMapper.map(mockResponse)
        assertTrue(result is MapperResult.InvalidDataError)

        mockResponse = UserProfileResponse(email = "test@test.com")
        result = userProfileDomainMapper.map(mockResponse)
        assertTrue(result is MapperResult.InvalidDataError)

        mockResponse = UserProfileResponse(phone = "test_phone")
        result = userProfileDomainMapper.map(mockResponse)
        assertTrue(result is MapperResult.InvalidDataError)
    }
}