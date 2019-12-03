package com.moneytx

import akka.actor.ActorRef
import com.moneytx.domain.*
import com.moneytx.logic.CommandActor
import java.util.concurrent.CompletableFuture

/**
 * Controller for routes declared in MoneyTxApp.kt.
 * All the Commands and Queries are sent to command actor for results.
 * @property cmdActor Command actor to send commands to.
 */
class Controller(private val cmdActor: ActorRef) {

    fun createAccount(): CompletableFuture<Account> =
            processCommandAndReturnAccount(Command.CreateAccount(AccountId.generate()))

    fun deposit(req: Command.Deposit): CompletableFuture<Account> =
            processCommandAndReturnAccount(req)

    fun withdraw(req: Command.Withdraw): CompletableFuture<Account> =
            processCommandAndReturnAccount(req)

    fun transfer(req: Command.Transfer): CompletableFuture<Account> =
            processCommandAndReturnAccount(req)

    fun currentBalance(accId: AccountId): CompletableFuture<Account> =
            getAccount(accId)

    private fun processCommandAndReturnAccount(req: Command): CompletableFuture<Account> =
            CommandActor.handleCommandAndGetAccountInfo(cmdActor, req).thenApply {
                it ?: throw ExpectedAccountNotFound
            }

    private fun getAccount(accId: AccountId): CompletableFuture<Account> =
            CommandActor.getAccountInfo(cmdActor, accId).thenApply {
                it ?: throw ExpectedAccountNotFound
            }

}