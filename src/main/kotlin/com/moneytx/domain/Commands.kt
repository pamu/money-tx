package com.moneytx.domain

// ADT to represent different commands in the system.
sealed class Command {
    abstract val accountId: AccountId

    /**
     * Command to create account with given account ID (Account ID is not given by user).
     * @property accountId new accountId generated by the controller.
     * Note that [accountId] is NOT given by user. It is generated at controller.
     */
    data class CreateAccount(override val accountId: AccountId) : Command()

    data class Deposit(override val accountId: AccountId, val amount: Money) : Command()
    data class Withdraw(override val accountId: AccountId, val amount: Money) : Command()
    data class Transfer(
            override val accountId: AccountId,
            val amount: Money,
            val payee: AccountId) : Command()
}