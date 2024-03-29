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

    /**
     * Represents deposit.
     * @property accountId Depositor account id.
     * @property amount    Depositing amount.
     */
    data class Deposit(override val accountId: AccountId, val amount: Money) : Command()

    /**
     * Represents withdrawal.
     * @property accountId  Account owner account id.
     * @property amount     Withdraw amount.
     */
    data class Withdraw(override val accountId: AccountId, val amount: Money) : Command()

    /**
     * Represents transfer.
     * @property accountId  Account owner
     * @property payee      Account into which amount is being transferred.
     * @property amount     Amount to transfer
     * Note: Transfer can be seen as
     * 1. Withdraw from accountId (amount)
     * 2. Deposit into payee account
     */
    data class Transfer(
            override val accountId: AccountId,
            val payee: AccountId,
            val amount: Money) : Command()
}