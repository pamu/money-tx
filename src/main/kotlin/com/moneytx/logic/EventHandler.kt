package com.moneytx.logic

import com.moneytx.domain.Event
import com.moneytx.domain.Money

interface EventHandler {
    fun handleEvent(event: Event): Unit
}

class EventHandlerImpl(private val state: AggregateState) : EventHandler {

    override fun handleEvent(event: Event): Unit = when (event) {
        is Event.AccountCreated ->
            state.addNewAccount(event.accId)
        is Event.Deposited ->
            state.changeAmount(event.accId, event.amount, Money::plus)
        is Event.Withdrawn ->
            state.changeAmount(event.accId, event.amount, Money::minus)
        is Event.Transferred -> {
            state.changeAmount(event.accId, event.amount, Money::minus)
            state.changeAmount(event.payee, event.amount, Money::plus)
        }
    }
}