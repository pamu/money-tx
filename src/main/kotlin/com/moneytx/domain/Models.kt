package com.moneytx.domain

import java.math.BigDecimal
import java.util.UUID

/**
 * Money representation.
 * @property value Amount of money.
 */
data class Money(val value: BigDecimal) {
    operator fun plus(other: Money): Money = Money(this.value + other.value)
    operator fun minus(other: Money): Money = Money(this.value - other.value)

    companion object {
        fun zero(): Money = Money(0.toBigDecimal())
    }
}

/**
 * User account representation.
 * @property id             UUID to uniquely identity user account.
 * @property currentBalance Current balance in the account.
 */
data class Account(
        val id: UUID,
        val currentBalance: Money) {
    companion object {
        fun zeroBalanceAccount(
                accId: AccountId): Account =
                Account(id = accId.value, currentBalance = Money.zero())
    }
}

/**
 * Represents account id. Wrapper around UUID for type-safety.
 * @property value         UUID to Uniquely identify account.
 */
data class AccountId(val value: UUID) {
    override fun toString(): String = "$value"

    companion object {
        fun generate(): AccountId = AccountId(UUID.randomUUID())
    }
}

/**
 * Class to hold readonly aggregate state
 * @property accounts   Map to hold user accounts.
 *
 */
data class ReadOnlyState(val accounts: Map<AccountId, Account>) {
    companion object {
        fun empty() = ReadOnlyState(emptyMap())
    }
}


/**
 * Represents HTTP error format.
 * @property errMsg   Error msg.
 */
data class HttpError(val errMsg: String)
