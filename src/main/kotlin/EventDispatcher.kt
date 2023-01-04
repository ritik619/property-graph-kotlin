typealias EventListener<E> = (event: E) -> Unit

sealed class BaseEvent {
    abstract val type: String
    abstract val attachments: Map<String, Any>
}

class GraphEvent(val target: Graph<GraphNode<Map<String,Any>>>) : BaseEvent() {
    override val type: String = "GraphEvent"
    override val attachments: Map<String, Any> = emptyMap()
}

class GraphNodeEvent(val target: GraphNode<Map<String,Any>>) : BaseEvent() {
    override val type: String = "GraphNodeEvent"
    override val attachments: Map<String, Any> = emptyMap()
}

class GraphEdgeEvent(val target: GraphEdge<GraphNode<Map<String,Any>>, GraphNode<Map<String,Any>>>) : BaseEvent() {
    override val type: String = "GraphEdgeEvent"
    override val attachments: Map<String, Any> = emptyMap()
}

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

    fun dispatchEvent(event: T): EventDispatcher<T> {
        listeners[event.type]?.forEach { it(event) }
        return this
    }

    fun dispose() {
        listeners.clear()
    }
}
