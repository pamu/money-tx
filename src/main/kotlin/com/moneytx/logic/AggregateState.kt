package com.moneytx.logic

import com.moneytx.domain.*

class AggregateState {

    private var accounts = mutableMapOf<AccountId, Account>()

    fun state(): ReadOnlyState = ReadOnlyState(accounts.toMap())

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