package com.moneytx

import com.moneytx.domain.*
import io.kotlintest.*
import io.kotlintest.specs.StringSpec

class ValidationSpec : StringSpec({

    "Valid money should be > 0" {
        // null means no errors
        validMoney(Money(222.339.toBigDecimal())) shouldBe null
    }

    "Zero is invalid money" {
        shouldThrow<IllegalAmount> {
            validMoney(Money(0.toBigDecimal()))?.let {
                throw it
            }
        }
    }

    "Negative money is invalid" {
        shouldThrow<IllegalAmount> {
            validMoney(Money((-1.3).toBigDecimal()))?.let {
                throw it
            }
        }
    }

    "Valid account if account id exists in memory" {
        val accId = AccountId.generate()
        val acc = Account(accId.value, Money(1.toBigDecimal()))
        val state = AllAccounts(mapOf(accId to acc))
        // null means no error
        state.validAccount(accId) shouldBe null
    }

    "Unknown account if account if does not exist in memory" {
        val emptyState = AllAccounts.empty()
        shouldThrow<AccountDoesNotExist> {
            emptyState.validAccount(AccountId.generate())?.let {
                throw it
            }
        }
    }

    "Checks if account has sufficient funds" {
        val accId = AccountId.generate()
        val acc = Account(accId.value, Money(10.toBigDecimal()))
        val state = AllAccounts(mapOf(accId to acc))
        // null means no errors
        state.hasSufficientFunds(accId, Money(10.toBigDecimal())) shouldBe null

        // No sufficient funds
        shouldThrow<InsufficientFunds> {
            state.hasSufficientFunds(accId, Money(11.toBigDecimal()))?.let {
                throw it
            }
        }
    }


})

