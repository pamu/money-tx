package com.moneytx

import com.moneytx.domain.Account
import com.moneytx.domain.AccountId
import com.moneytx.domain.Event
import com.moneytx.domain.Money
import com.moneytx.logic.AggregateState
import com.moneytx.logic.EventHandlerImpl
import io.kotlintest.matchers.maps.shouldContain
import io.kotlintest.specs.StringSpec

class EventHandlerSpec: StringSpec({

    val state = AggregateState()
    val handler = EventHandlerImpl(state)

    "New account can be added" {
        val accId = AccountId.generate()
        handler.handleEvent(Event.AccountCreated(accId))
        state.currState().accounts.shouldContain(accId, Account(accId.value, Money.zero()))
    }

    "Money can be deposited into account" {
        val accId = AccountId.generate()
        handler.handleEvent(Event.AccountCreated(accId))
        handler.handleEvent(Event.Deposited(accId, Money(100.toBigDecimal())))

        state.currState().accounts.shouldContain(accId, Account(accId.value, Money(100.toBigDecimal())))
    }

    "Money can be withdrawn" {
        val accId = AccountId.generate()
        handler.handleEvent(Event.AccountCreated(accId))
        handler.handleEvent(Event.Deposited(accId, Money(100.toBigDecimal())))
        handler.handleEvent(Event.Withdrawn(accId, Money(10.toBigDecimal())))

        state.currState().accounts.shouldContain(accId, Account(accId.value, Money(90.toBigDecimal())))
    }

    "Money can be transferred" {
        val accId = AccountId.generate()
        val payee = AccountId.generate()

        handler.handleEvent(Event.AccountCreated(accId))
        handler.handleEvent(Event.AccountCreated(payee))
        handler.handleEvent(Event.Deposited(accId, Money(100.toBigDecimal())))
        handler.handleEvent(Event.Transferred(accId, payee, Money(10.toBigDecimal())))

        state.currState().accounts.shouldContain(accId, Account(accId.value, Money(90.toBigDecimal())))
        state.currState().accounts.shouldContain(payee, Account(payee.value, Money(10.toBigDecimal())))
    }

})