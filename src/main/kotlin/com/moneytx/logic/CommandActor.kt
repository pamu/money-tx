package com.moneytx.logic

import akka.actor.AbstractActor
import akka.actor.ActorRef
import akka.pattern.Patterns
import com.moneytx.domain.*
import java.time.Duration
import java.util.concurrent.CompletableFuture


sealed class CommandActorRequest {
    data class HandleCommand(val cmd: Command) : CommandActorRequest()
    data class GetAccountInfo(val accId: AccountId) : CommandActorRequest()
}


sealed class CommandActorResponse {
    data class CommandAccepted(val account: Account?) : CommandActorResponse()
    data class CommandRejected(val err: ValidationError) : CommandActorResponse()
    data class AccountInfo(val account: Account?) : CommandActorResponse()
}


class CommandActor : AbstractActor() {

    private val current = AggregateState()
    private val eventHandler = EventHandlerImpl(current)

    override fun createReceive(): Receive =
            receiveBuilder()
                    .match(CommandActorRequest.HandleCommand::class.java) {
                        processCommand(it.cmd)
                    }.match(CommandActorRequest.GetAccountInfo::class.java) {
                        val payload = CommandActorResponse.AccountInfo(current.state().accounts[it.accId])
                        sender.tell(payload, self)
                    }.build()

    private fun processCommand(cmd: Command): Unit {
        val error = current.state().validateCommand(cmd)
        if (error != null) {
            sender.tell(CommandActorResponse.CommandRejected(error), self)
            return
        }
        val event = getEvent(cmd)
        eventHandler.handleEvent(event)
        val payload = CommandActorResponse.CommandAccepted(current.state().accounts[cmd.accountId])
        sender.tell(payload, self)
    }

    private fun getEvent(cmd: Command): Event = when (cmd) {
        is Command.CreateAccount -> Event.AccountCreated(cmd.accountId)
        is Command.Deposit -> Event.Deposited(cmd.accountId, cmd.amount)
        is Command.Withdraw -> Event.Withdrawn(cmd.accountId, cmd.amount)
        is Command.Transfer -> Event.Transferred(cmd.accountId, cmd.payee, cmd.amount)
    }

    companion object {

        fun getAccountInfo(
                cmdActor: ActorRef,
                accId: AccountId
        ): CompletableFuture<Account?> =
                Patterns.ask(cmdActor, CommandActorRequest.GetAccountInfo(accId), Duration.ofMillis(100))
                        .toCompletableFuture().thenApply {
                            when (it) {
                                is CommandActorResponse.AccountInfo -> it.account
                                else -> throw UnExpectedReturnType
                            }
                        }

        fun handleCommandAndGetAccountInfo(cmdActor: ActorRef, cmd: Command): CompletableFuture<Account?> =
                Patterns.ask(cmdActor, CommandActorRequest.HandleCommand(cmd), Duration.ofMillis(100))
                        .toCompletableFuture().thenApply {
                            when (it) {
                                is CommandActorResponse.CommandAccepted -> it.account
                                is CommandActorResponse.CommandRejected -> throw it.err
                                else -> throw UnExpectedReturnType
                            }
                        }
    }

}
