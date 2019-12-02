package com.moneytx

import com.moneytx.logic.AggregateState
import com.moneytx.logic.EventHandlerImpl
import io.kotlintest.specs.StringSpec

class EventHandlerSpec: StringSpec({

    val state = AggregateState()
    val handler = EventHandlerImpl(state)

    "Account created event should add empty account to aggregate state" {

    }

})