interface BaseEvent :MutableMap<String,Any>{
}

interface GraphEvent : BaseEvent {
    val target: GraphNode<*>
}

interface GraphNodeEvent : BaseEvent {
    val target: GraphNode<*>
}

interface GraphEdgeEvent : BaseEvent {
    val target: GraphEdge<GraphNode<*>, GraphNode<*>>
}

typealias EventListener<E> = (event: E) -> Unit

open class EventDispatcher<T: MutableMap<String,Any>> {
    private val listeners: MutableMap<String, MutableList<EventListener<T>>> = mutableMapOf()

    fun addEventListener(type: String, listener: EventListener<T>): EventDispatcher<T> {
        val listeners = this.listeners
//        println("add event listener called ${ this.listeners}, $type, $listener")
//        println(listeners[type])
        if (listeners[type] === null) {
            listeners[type] = mutableListOf<EventListener<T>>()
        }
//        println(listeners)
        if (listeners[type]?.indexOf(listener)  == -1) {
            listeners[type]?.add(listener)
        }
//        println("add event listener called, ${this.listeners}")
        return this
    }
    fun removeEventListener(type: String, listener: EventListener<T>): EventDispatcher<T> {
        val listenerList = listeners.getOrPut(type) { mutableListOf() }.toMutableList()
        listenerList.remove(listener)
        return this
    }

    open fun dispatchEvent(event: MutableMap<String,Any>): EventDispatcher<T> {
        listeners[event["type"]]?.forEach { it(event as T) }
        return this
    }

    open fun dispose() {
//        println("super called ${this.listeners} $listeners")
        listeners.clear()
    }
}
