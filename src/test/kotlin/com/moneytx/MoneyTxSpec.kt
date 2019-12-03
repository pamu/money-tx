package com.moneytx

import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import io.javalin.Javalin
import io.kotlintest.Spec
import io.kotlintest.specs.StringSpec
import com.github.kittinunf.fuel.jackson.responseObject
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.moneytx.domain.Account
import io.kotlintest.shouldBe
import com.moneytx.domain.AccountId
import com.moneytx.domain.Command
import com.moneytx.domain.Money
import java.util.*

class MoneyTxSpec : StringSpec() {

    private lateinit var app: Javalin

    override fun beforeSpec(spec: Spec) {
        super.beforeSpec(spec)
        app = MoneyTxApp().createApp()
        app.start(9000)
    }

    val base = "http://localhost:9000"
    val createAccountEndPoint = "$base/createAccount"
    val depositEndPoint = "$base/deposit"
    val withdrawEndPoint = "$base/withdraw"
    val transferEndPoint = "$base/transfer"
    fun currentBalanceEndPoint(id: UUID) = "$base/currentBalance/$id"

    init {
        "New account can be created" {
            createNewAccount().currentBalance shouldBe Money.zero()
        }

        "Deposit can be made into account" {
            val acc = createNewAccount()
            val amount = Money(20.toBigDecimal())

            val accAfterDeposit = deposit(acc.id, amount)

            accAfterDeposit.id shouldBe acc.id
            accAfterDeposit.currentBalance shouldBe amount
        }

        "Money can be withdrawn" {
            val acc = createNewAccount()
            val amount = Money(20.toBigDecimal())

            val accAfterDeposit = deposit(acc.id, amount)
            accAfterDeposit.currentBalance shouldBe amount

            val accAfterWithdraw = withdraw(acc.id, amount)

            accAfterWithdraw.id shouldBe acc.id
            accAfterWithdraw.currentBalance shouldBe Money.zero()
        }

        "Transfer can be made" {
            val acc = createNewAccount()
            val payee = createNewAccount()
            val amount = Money(200.toBigDecimal())

            val afterDeposit = deposit(acc.id, amount)
            afterDeposit.currentBalance shouldBe amount
            payee.currentBalance shouldBe Money.zero()

            val afterTransfer = transfer(acc.id, payee.id, amount)
            afterTransfer.id shouldBe acc.id
            afterTransfer.currentBalance shouldBe Money.zero()

            val afterTransferPayee = currentBalance(payee.id)
            afterTransferPayee.currentBalance shouldBe amount
        }

        "Current balance can be requested" {
            val acc = createNewAccount()
            val amount = Money(200.toBigDecimal())
            val afterDeposit = deposit(acc.id, amount)

            afterDeposit shouldBe acc.copy(currentBalance = amount)
        }
    }

    private fun createNewAccount(): Account {
        val (_, res, result) = createAccountEndPoint.httpGet().responseObject<Account>()
        res.statusCode shouldBe 200
        return result.get()
    }

    private fun deposit(accountId: UUID, amount: Money): Account {
        val (_, res, result) = depositEndPoint.httpPost().jsonBody(
                jacksonObjectMapper().writeValueAsString(Command.Deposit(
                        accountId = AccountId(accountId),
                        amount = amount
                ))).responseObject<Account>()
        res.statusCode shouldBe 200
        return result.get()
    }

    private fun withdraw(accountId: UUID, amount: Money): Account {
        val (_, res, result) = withdrawEndPoint.httpPost().jsonBody(
                jacksonObjectMapper().writeValueAsString(Command.Withdraw(
                        accountId = AccountId(accountId),
                        amount = amount
                ))).responseObject<Account>()
        res.statusCode shouldBe 200
        return result.get()
    }

    private fun transfer(accountId: UUID, payee: UUID, amount: Money): Account {
        val (_, res, result) = transferEndPoint.httpPost().jsonBody(
                jacksonObjectMapper().writeValueAsString(Command.Transfer(
                        accountId = AccountId(accountId),
                        payee = AccountId(payee),
                        amount = amount
                ))).responseObject<Account>()
        res.statusCode shouldBe 200
        return result.get()
    }

    private fun currentBalance(accountId: UUID): Account {
        val (_, res, result) = currentBalanceEndPoint(accountId)
                .httpGet()
                .responseObject<Account>()
        res.statusCode shouldBe 200
        return result.get()
    }


    override fun afterSpec(spec: Spec) {
        super.afterSpec(spec)
        app.stop()
    }
}