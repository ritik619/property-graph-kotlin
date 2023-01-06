interface BaseEvent {
    val type: String
    operator fun get(attachment: String): Any?
}

interface GraphEvent : BaseEvent {
    val target: GraphNode
}

interface GraphNodeEvent : BaseEvent {
    val target: GraphNode
}

interface GraphEdgeEvent : BaseEvent {
    val target: GraphEdge<GraphNode, GraphNode>
}

typealias EventListener<E> = (event: E) -> Unit

open class EventDispatcher<T: BaseEvent> {
    private val listeners: MutableMap<String, List<EventListener<T>>> = mutableMapOf()

    fun addEventListener(type: String, listener: EventListener<T>): EventDispatcher<T> {
        val listenerList = listeners.getOrPut(type) { mutableListOf() }.toMutableList()
        if (listener !in listenerList) {
            listenerList+=listener
        }
        return this
    }
    fun removeEventListener(type: String, listener: EventListener<T>): EventDispatcher<T> {
        val listenerList = listeners.getOrPut(type) { mutableListOf() }.toMutableList()
        listenerList.remove(listener)
        return this
    }

    open fun dispatchEvent(event: BaseEvent): EventDispatcher<T> {
        listeners[event.type]?.forEach { it(event as T) }
        return this
    }

    open fun dispose() {
        listeners.clear()
    }
}
