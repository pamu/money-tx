package com.moneytx

import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import io.javalin.Javalin
import io.kotlintest.specs.StringSpec
import com.github.kittinunf.fuel.jackson.responseObject
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.moneytx.domain.*
import io.kotlintest.*
import io.kotlintest.matchers.string.shouldContain
import java.util.*

@ExperimentalStdlibApi
class MoneyTxSpec : StringSpec() {

    private lateinit var app: Javalin

    override fun beforeSpec(spec: Spec) {
        super.beforeSpec(spec)
        app = MoneyTxApp().createApp()
        app.start(9000)
    }

    private val base = "http://localhost:9000"
    private val createAccountEndPoint = "$base/createAccount"
    private val depositEndPoint = "$base/deposit"
    private val withdrawEndPoint = "$base/withdraw"
    private val transferEndPoint = "$base/transfer"
    private fun currentBalanceEndPoint(id: UUID) = "$base/currentBalance/$id"

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

        "Deposit to non-existent account not allowed" {
            val id = UUID.randomUUID()
            try {
                depositExpectingFailure(id, Money(1.toBigDecimal()))
                fail("Failure expected")
            } catch (err: FuelError) {
                err.message shouldContain "400"
                err.errorData.decodeToString() shouldContain AccountDoesNotExist(AccountId(id)).message!!
            }
        }

        "Negative or zero deposits not allowed" {
            try {
                val acc = createNewAccount()
                depositExpectingFailure(acc.id, Money((-1).toBigDecimal()))
                fail("Failure expected")
            } catch (err: FuelError) {
                err.message shouldContain "400"
                err.errorData.decodeToString() shouldContain IllegalAmount.message!!
            }

            try {
                val acc = createNewAccount()
                depositExpectingFailure(acc.id, Money(0.toBigDecimal()))
                fail("Failure expected")
            } catch (err: FuelError) {
                err.message shouldContain "400"
                err.errorData.decodeToString() shouldContain IllegalAmount.message!!
            }
        }

        "Withdraw from non-existent account not allowed" {
            val id = UUID.randomUUID()
            try {
                withdrawExpectingFailure(id, Money(1.toBigDecimal()))
                fail("Failure expected")
            } catch (err: FuelError) {
                err.message shouldContain "400"
                err.errorData.decodeToString() shouldContain AccountDoesNotExist(AccountId(id)).message!!
            }
        }

        "Negative or zero withdraws not allowed" {
            try {
                val acc = createNewAccount()
                withdrawExpectingFailure(acc.id, Money((-1).toBigDecimal()))
                fail("Failure expected")
            } catch (err: FuelError) {
                err.message shouldContain "400"
                err.errorData.decodeToString() shouldContain IllegalAmount.message!!
            }

            try {
                val acc = createNewAccount()
                withdrawExpectingFailure(acc.id, Money(0.toBigDecimal()))
                fail("Failure expected")
            } catch (err: FuelError) {
                err.message shouldContain "400"
                err.errorData.decodeToString() shouldContain IllegalAmount.message!!
            }
        }

        "Withdraw can be done only with sufficient funds" {
            val acc = createNewAccount() // 0 funds
            try {
                withdrawExpectingFailure(acc.id, Money(100.toBigDecimal()))
                fail("Failure expected")
            } catch (err: FuelError) {
                err.message shouldContain "400"
                err.errorData.decodeToString() shouldContain InsufficientFunds(
                        AccountId(acc.id), acc.currentBalance).message!!
            }
        }

        "Transfer from/to non-existent account not allowed" {
            val acc = createNewAccount()
            deposit(acc.id, Money(100.toBigDecimal()))
            // Unknown payee
            val randomID = UUID.randomUUID()
            try {
                transferExpectingFailure(accountId = acc.id, payee = randomID, amount = Money((10).toBigDecimal()))
                fail("Failure expected")
            } catch (err: FuelError) {
                err.message shouldContain "400"
                err.errorData.decodeToString() shouldContain AccountDoesNotExist(
                        accId = AccountId(randomID), isPayee = true).message!!
            }

            // Unknown account
            try {
                transferExpectingFailure(accountId = randomID, payee = acc.id, amount = Money((10).toBigDecimal()))
                fail("Failure expected")
            } catch (err: FuelError) {
                err.message shouldContain "400"
                err.errorData.decodeToString() shouldContain AccountDoesNotExist(accId = AccountId(randomID)).message!!
            }
        }

        "Negative or zero transfers not allowed" {
            val acc = createNewAccount()
            deposit(acc.id, Money(100.toBigDecimal()))
            val payee = createNewAccount()
            try {
                transferExpectingFailure(acc.id, payee = payee.id, amount = Money((-1).toBigDecimal()))
                fail("Failure expected")
            } catch (err: FuelError) {
                err.message shouldContain "400"
                err.errorData.decodeToString() shouldContain IllegalAmount.message!!
            }

            try {
                transferExpectingFailure(acc.id, payee = payee.id, amount = Money((-1).toBigDecimal()))
                fail("Failure expected")
            } catch (err: FuelError) {
                err.message shouldContain "400"
                err.errorData.decodeToString() shouldContain IllegalAmount.message!!
            }
        }

        "Transfer can be done only with sufficient funds" {
            val acc = createNewAccount() // Has zero amount
            val payee = createNewAccount()
            try {
                transferExpectingFailure(acc.id, payee = payee.id, amount = Money(100.toBigDecimal()))
                fail("Failure expected")
            } catch (err: FuelError) {
                err.message shouldContain "400"
                err.errorData.decodeToString() shouldContain InsufficientFunds(
                        AccountId(acc.id), acc.currentBalance).message!!
            }
        }

        "Self transfer not allowed" {
            try {
                val acc = createNewAccount()
                transferExpectingFailure(acc.id, acc.id, Money(100.toBigDecimal()))
                fail("Failure expected")
            } catch (err: FuelError) {
                err.message shouldContain "400"
                err.errorData.decodeToString() shouldContain SelfTransfer.message!!
            }
        }

    }

    private fun createNewAccount(): Account {
        val (_, res, result) = createAccountEndPoint.httpGet().responseObject<Account>()
        res.statusCode shouldBe 200
        return result.get()
    }

    private fun depositReq(accountId: UUID, amount: Money): Request {
        return depositEndPoint.httpPost().jsonBody(
                jacksonObjectMapper().writeValueAsString(Command.Deposit(
                        accountId = AccountId(accountId),
                        amount = amount
                )))
    }

    private fun deposit(accountId: UUID, amount: Money): Account {
        val (_, res, result) = depositReq(accountId, amount).responseObject<Account>()
        res.statusCode shouldBe 200
        return result.get()
    }

    private fun depositExpectingFailure(accountId: UUID, amount: Money): Unit {
        val (_, res, result) = depositReq(accountId, amount)
                .responseString()
        res.statusCode shouldNotBe 200
        throw result.component2() ?: return
    }

    private fun withdrawReq(accountId: UUID, amount: Money): Request {
        return withdrawEndPoint.httpPost().jsonBody(
                jacksonObjectMapper().writeValueAsString(Command.Withdraw(
                        accountId = AccountId(accountId),
                        amount = amount
                )))
    }

    private fun withdraw(accountId: UUID, amount: Money): Account {
        val (_, res, result) = withdrawReq(accountId, amount).responseObject<Account>()
        res.statusCode shouldBe 200
        return result.get()
    }

    private fun withdrawExpectingFailure(accountId: UUID, amount: Money): Unit {
        val (_, res, result) = withdrawReq(accountId, amount)
                .responseString()
        res.statusCode shouldNotBe 200
        throw result.component2() ?: return
    }

    private fun transferReq(accountId: UUID, payee: UUID, amount: Money): Request {
        return transferEndPoint.httpPost().jsonBody(
                jacksonObjectMapper().writeValueAsString(Command.Transfer(
                        accountId = AccountId(accountId),
                        payee = AccountId(payee),
                        amount = amount
                )))
    }

    private fun transfer(accountId: UUID, payee: UUID, amount: Money): Account {
        val (_, res, result) = transferReq(accountId = accountId, payee = payee, amount = amount)
                .responseObject<Account>()
        res.statusCode shouldBe 200
        return result.get()
    }

    private fun transferExpectingFailure(accountId: UUID, payee: UUID, amount: Money): Unit {
        val (_, res, result) = transferReq(accountId = accountId, payee = payee, amount = amount)
                .responseString()
        res.statusCode shouldNotBe 200
        throw result.component2() ?: return
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