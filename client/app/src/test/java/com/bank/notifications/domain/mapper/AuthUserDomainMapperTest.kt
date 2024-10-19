package com.bank.notifications.domain.mapper

import com.bank.notifications.common.mapper.MapperResult
import com.bank.notifications.data.cloud.UserAuthedResponse
import com.bank.notifications.domain.model.UserTokenDomain
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthUserDomainMapperTest {

    private lateinit var authUserDomainMapper: AuthUserDomainMapper

    @Before
    fun setUp() {
        authUserDomainMapper = AuthUserDomainMapper()
    }

    @Test
    fun `test map with valid data`() {
        val mockResponse = UserAuthedResponse(token = "test_token")
        val result = authUserDomainMapper.map(mockResponse)
        val expected = UserTokenDomain(token = "test_token")
        assertTrue(result is MapperResult.Success && result.data == expected)
    }

    @Test
    fun `test map with invalid data`() {
        val mockResponse = UserAuthedResponse(token = null)
        val result = authUserDomainMapper.map(mockResponse)
        assertTrue(result is MapperResult.InvalidDataError)
    }
}

