package com.lapis.mycharacter.util

import kotlin.collections.ArrayList

/**
 * Event to be handled by the dispatcher
 */
private class AsyncEvent(val event: ((AsyncDispatchQueue.AsyncDispatchHandler) -> Unit))
{
    internal fun execute(dispatch: AsyncDispatchQueue.AsyncDispatchHandler)
    {
        event(dispatch)
    }
}

/**
 * Dispatch queue for asynchronous events. Items in the queue are handled in order and can be finalized to lock adding
 */
class AsyncDispatchQueue(first: (AsyncDispatchHandler) -> Unit)
{
    private var eventSeqPointer = 0
    private var size = 1
    private var executing = false
    private var locked = false

    private val events = ArrayList<AsyncEvent>()
    private var finalEvent: AsyncEvent? = null

    val currentEventIndex: Int
        get() = eventSeqPointer

    val count: Int
        get() = size

    val isExecuting: Boolean
        get() = executing

    init
    {
        events.add(AsyncEvent(first))
    }

    /**
     * Begins execution of events in the dispatch queue.
     * Locks the queue from having further events added.
     */
    private fun beginDispatcher()
    {
        executing = true
        Thread {
            val handler = getHandler()

            while(eventSeqPointer < events.size && isExecuting)
            {
                events[eventSeqPointer].execute(handler)
                eventSeqPointer ++
            }
            executing = false
            eventSeqPointer = 0
            events.clear()

            if (handler.doFinally) finalEvent?.execute(handler)
            finalEvent = null
        }.start()
    }

    /**
     * Creates the Dispatch Handler for this Dispatch Queue
     */
    private fun getHandler(): AsyncDispatchHandler
        = AsyncDispatchHandler(this)

    /**
     * Adds an event to the dispatch queue.
     * @param event - Event to add to the dispatch queue
     * @throws error - When the thread is locked by [finally] or while execution is running
     */
    fun then(event: (AsyncDispatchHandler) -> Unit): AsyncDispatchQueue
    {
        when
        {
            executing -> error("Attempt to add async event while execution is running.")
            locked -> error("Attempt to add event to locked dispatch queue.")
            else ->
            {
                events.add(AsyncEvent(event))
                size ++
            }
        }

        return this
    }

    /**
     * Finalizes the DispatchQueue and returns a starter for starting the Dispatcher
     */
    fun finally(event: (AsyncDispatchHandler) -> Unit): AsyncDispatchStarter
    {
        finalEvent = AsyncEvent(event)
        locked = true
        return AsyncDispatchStarter(this)
    }

    /**
     * Starts the Dispatcher
     */
    fun start()
    {
        if (executing) error("Attempt to start already executing async event queue")
        beginDispatcher()
    }

    /**
     * Holds a finalized DispatchQueue until started
     */
    class AsyncDispatchStarter internal constructor(private val dispatcher: AsyncDispatchQueue)
    {
        /**
         * Begins execution of the dispatcher that created it
         */
        fun start()
        {
            dispatcher.start()
        }
    }

    /**
     * Controls the flow of the DispatchQueue
     */
    class AsyncDispatchHandler(private val dispatch: AsyncDispatchQueue)
    {
        internal var doFinally = true

        val currentEventIndex: Int
            get() = dispatch.currentEventIndex

        /**
         * Stops further execution of events after the current event and clears the dispatch queue.
         *
         * @param doFinally - Whether the 'finally' event in the dispatcher should still be handled executed
         */
        fun halt(doFinally: Boolean = false)
        {
            dispatch.executing = false
            this.doFinally = doFinally
        }
    }
}