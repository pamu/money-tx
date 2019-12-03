package com.moneytx

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import com.moneytx.domain.*
import io.javalin.Javalin
import io.javalin.http.BadRequestResponse
import org.eclipse.jetty.http.HttpStatus
import org.slf4j.LoggerFactory
import java.util.*
import akka.actor.PoisonPill
import akka.pattern.BackoffOpts
import akka.pattern.BackoffSupervisor
import com.moneytx.logic.CommandActor
import java.time.Duration


fun main(args: Array<String>) {
    val PORT = if (args.isEmpty()) 8080 else args[0].toInt()
    MoneyTxApp().createApp().start(PORT)
}

class MoneyTxApp {
    fun createApp(): Javalin {

        val logger = LoggerFactory.getLogger("Main")

        // Initialize actorSystem and actor supervisor
        val system = ActorSystem.create("moneytx")
        val cmdActorSupervisor = system.actorOf(
                actorPropsWithSupervision(),
                "CommandActorSupervisor"
        )

        // Init controller
        val controller = Controller(cmdActorSupervisor)

        // Start server
        val server = Javalin.create()

        // Exception handlers
        server.exception(ValidationError::class.java) { e, ctx ->
            val errMsg = "Validation error occurred"
            logger.info(e.message ?: errMsg, e)
            ctx.status(HttpStatus.BAD_REQUEST_400)
            ctx.json(HttpError(e.message ?: errMsg))
        }

        server.exception(BadRequestResponse::class.java) { e, ctx ->
            val errMsg = "Request body validation failed"
            logger.info(e.message ?: errMsg, e)
            ctx.status(HttpStatus.BAD_REQUEST_400)
            ctx.json(HttpError(e.message ?: errMsg))
        }

        server.exception(RuntimeError::class.java) { e, ctx ->
            val errMsg = "Internal server error"
            logger.error(e.message ?: errMsg, e)
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
            ctx.json(HttpError(errMsg))
        }


        server.exception(Exception::class.java) { e, ctx ->
            val errMsg = "Unknown error occurred"
            logger.error(errMsg, e)
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
            ctx.json(HttpError(errMsg))
        }

        // Routes
        server.get("/createAccount") {
            it.json(controller.createAccount())
        }

        server.post("/deposit") {
            it.json(controller.deposit(it.body<Command.Deposit>()))
        }

        server.post("/withdraw") {
            it.json(controller.withdraw(it.body<Command.Withdraw>()))
        }

        server.post("/transfer") {
            it.json(controller.transfer(it.body<Command.Transfer>()))
        }

        val accIdPathParam = "accId"
        server.get("/currentBalance/:$accIdPathParam") {
            val value = it.pathParam(accIdPathParam)
            try {
                val id = AccountId(UUID.fromString(it.pathParam(accIdPathParam)))
                it.json(controller.currentBalance(id))
            } catch (ex: IllegalArgumentException) {
                // Wrap into meaningful error
                throw InvalidAccountId(value, accIdPathParam, ex)
            }
        }

        // Graceful shutdown
        Runtime.getRuntime().addShutdownHook(Thread {
            cmdActorSupervisor.tell(PoisonPill.getInstance(), ActorRef.noSender())
            server.stop()
            system.terminate()
        })

        return server
    }


    // Supervision for command actor
    private fun actorPropsWithSupervision(): Props {
        val cmdActorProps = Props.create(CommandActor::class.java)
        return BackoffSupervisor.props(
                BackoffOpts.onFailure(
                        cmdActorProps,
                        "CommandActor",
                        Duration.ofSeconds(1),
                        Duration.ofSeconds(10),
                        0.2))
    }
}
