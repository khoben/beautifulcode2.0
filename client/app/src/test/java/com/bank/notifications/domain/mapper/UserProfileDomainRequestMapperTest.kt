package com.bank.notifications.domain.mapper

import com.bank.notifications.common.mapper.MapperResult
import com.bank.notifications.data.cloud.UserProfileRequest
import com.bank.notifications.domain.model.UserProfileDomainRequest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UserProfileDomainRequestMapperTest {
    private lateinit var userProfileDomainRequestMapper: UserProfileDomainRequestMapper

    @Before
    fun setUp() {
        userProfileDomainRequestMapper = UserProfileDomainRequestMapper()
    }

    @Test
    fun `test map with valid data`() {
        val mockRequest = UserProfileDomainRequest(email = "test@test.com", phone = "test_phone")
        val result = userProfileDomainRequestMapper.map(mockRequest)
        val expected = UserProfileRequest(email = "test@test.com", phone = "test_phone")
        assertTrue(result is MapperResult.Success && result.data == expected)
    }
}