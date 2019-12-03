package com.moneytx.logic

import com.moneytx.domain.*

class AggregateState {

    private var accounts = mutableMapOf<AccountId, Account>()

    fun currState(): AllAccounts = AllAccounts(accounts.toMap())

    /**
     * Add or subtract amount from account amount.
     * @param accId  Account whose amount has to be changed.
     * @param amount Amount to be added or subtracted
     * @param reduce Reduction function (one of Money::plus, Money::minus)
     */
    fun changeAmount(accId: AccountId, amount: Money, reduce: (Money, Money) -> Money): Unit {
        val account = accounts[accId]
        if (account != null) {
            val newAccount = account.copy(currentBalance = reduce(account.currentBalance, amount))
            accounts[accId] = newAccount
        }
    }

    fun addNewAccount(accId: AccountId): Unit {
        accounts[accId] = Account.zeroBalanceAccount(accId)
    }
}