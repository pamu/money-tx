package com.moneytx.domain

// Validation error results in Bad request (400).
sealed class ValidationError(msg: String, cause: Exception? = null) : Exception(msg, cause)

object IllegalAmount : ValidationError("Money should be greater than zero.")

data class AccountAlreadyExists(val accId: AccountId) : ValidationError("Account: $accId already exists.")

data class AccountDoesNotExist(
        val accId: AccountId,
        val isPayee: Boolean = false) : ValidationError(
        "${if (isPayee) "Payee account" else "Account"}: $accId does not exist.")

data class InsufficientFunds(
        val accId: AccountId,
        val currentBalance: Money
) : ValidationError("Insufficient funds in account: $accId, current balance: ${currentBalance.value}.")

data class InvalidAccountId(val value: String, val pathParamName: String, override val cause: Exception?) :
        ValidationError("Value: $value for path param name: $pathParamName cannot be converted to AccountId(UUID).", cause)

object SelfTransfer : ValidationError(
        "Transferring into your own account is not allowed (Use Deposit)."
)

// Runtime Error results in Internal server error (500).
sealed class RuntimeError(msg: String) : Exception(msg)

object UnExpectedReturnType : RuntimeError("Unexpected return type")
object ExpectedAccountNotFound : RuntimeError("Expected account NOT FOUND (Could be state corruption).")