package com.moneytx.domain

// ADT to represent events produced after command validation.
sealed class Event {

    // Same properties as commands
    data class AccountCreated(val accId: AccountId) : Event()
    data class Deposited(val accId: AccountId, val amount: Money) : Event()
    data class Withdrawn(val accId: AccountId, val amount: Money) : Event()
    data class Transferred(
            val accId: AccountId,
            val payee: AccountId,
            val amount: Money) : Event()
}

