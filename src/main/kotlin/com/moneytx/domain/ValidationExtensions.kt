package com.moneytx.domain

// Validation either returns error or null value. Null means no error i.e valid.
// Complex data class validations are built by composing simpler validations (see below).
fun AllAccounts.validateCommand(cmd: Command): ValidationError? {
    when (cmd) {
        is Command.CreateAccount -> {
            // This case does not occur as new random UUID is generated every time.
            this.validAccount(cmd.accountId) ?: return AccountAlreadyExists(cmd.accountId)
        }
        is Command.Deposit -> {
            this.validAccount(cmd.accountId)?.let { return it }
            validMoney(cmd.amount)?.let { return it }
        }
        is Command.Withdraw -> this.hasSufficientFunds(
                    accId = cmd.accountId,
                    withdrawAmount = cmd.amount)?.let { return it }
        is Command.Transfer -> {
            if (cmd.payee == cmd.accountId) return SelfTransfer
            this.validAccount(cmd.payee, isPayee = true)?.let { return it }
            this.hasSufficientFunds(cmd.accountId, cmd.amount)?.let { return it }
        }
    }
    return null
}

// Better to use validation from arrow-kt (https://arrow-kt.io/docs/apidocs/arrow-validation/)
fun AllAccounts.hasSufficientFunds(accId: AccountId, withdrawAmount: Money): ValidationError? {
    this.validAccount(accId)?.let { return it }
    validMoney(withdrawAmount)?.let { return it }
    val account = this.accounts[accId]!!
    return when {
        account.currentBalance.value >= withdrawAmount.value -> null
        else -> InsufficientFunds(
                accId = accId,
                currentBalance = account.currentBalance)
    }
}



// Basic validations
// Validate account
fun AllAccounts.validAccount(accId: AccountId, isPayee: Boolean = false): ValidationError? =
        if (this.accounts.containsKey(accId)) null
        else AccountDoesNotExist(accId = accId, isPayee = isPayee)

fun validMoney(money: Money): ValidationError? =
        if (money.value > 0.toBigDecimal()) null
        else IllegalAmount