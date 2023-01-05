typealias GraphNodeAttributesInternal<T> = MutableMap<String, Any>

//abstract class GraphNode<Attributes : Map<String, Any>> = emptyMap()> : EventDispatcher<GraphNodeEvent> {
//    abstract class GraphNode<Attributes : Map<String, Any> = emptyMap()> : EventDispatcher<GraphNodeEvent> {




abstract  class  GraphNode<Attributes:Map<String,Any>> :EventDispatcher<GraphNodeEvent>{
    private var disposed = false
    private val graph: Graph<Map<String,Any>>
    private val attributes: GraphNodeAttributesInternal<Attributes>
    private val immutableKeys: MutableSet<String>

    constructor(graph: Graph<Map<String,Any>>) {
        this.graph = graph
        this.immutableKeys = mutableSetOf()
        this.attributes = this._createAttributes()
    }

    protected abstract fun getDefaults(): Map<String, Any>?

    private fun _createAttributes(): GraphNodeAttributesInternal<Attributes> {
        val defaultAttributes = this.getDefaults()
        val attributes = mutableMapOf<String, Any>()
        for (key in defaultAttributes!!.keys) {
            val value = defaultAttributes[key] as Any
            if (value is GraphNode<*>) {
                val ref = this.graph.createEdge(key, this , value)
                ref.addEventListener("dispose") { value.dispose() }
                this.immutableKeys.add(key)
                attributes[key] = ref as Any
            } else {
                attributes[key] = value as Any
            }
        }
        return attributes as GraphNodeAttributesInternal<Attributes>
    }

    fun isOnGraph(other: GraphNode<*>): Boolean {
        return this.graph === other.graph
    }

    fun isDisposed(): Boolean {
        return this.disposed
    }

    override fun dispose() {
        if (this.disposed) return
        this.graph.listChildEdges(this).forEach { it.dispose() }
        this.graph.disconnectParents(this)
        this.disposed = true
        this.dispatchEvent("dispose" as BaseEvent)
    }

    fun detach(): GraphNode<Attributes> {
        this.graph.disconnectParents(this)
        return this
    }


    fun swap(old: GraphNode<*>, replacement: GraphNode<*>): GraphNode<Attributes> {
            for (attribute in this.attributes.keys) {
                val value = this.attributes[attribute] as Any
                when {
                    isRef(value) -> {
                        val ref = value as GraphEdge<*, *>
                        if (ref.getChild() === old) {
                            this.setRef(attribute as String, replacement, ref.getAttributes())
                        }
                    }
                    isRefList(value) -> {
                        val refs = value as Array<GraphEdge<*,*>>
                        val ref = refs.find { it.getChild() === old }
                        if (ref != null) {
                            val refAttributes = ref.getAttributes()
                            this.removeRef(attribute as String, old ).addRef(attribute as String, replacement , refAttributes)
                        }
                    }
                    isRefMap(value) -> {
                        val refMap = value as RefMap<Map<String, Any>>
                        for (key in refMap.keys) {
                            val ref = refMap[key]
                            if (ref?.getChild() === old) {
                                this.setRefMap(attribute as String, key, replacement, ref.getAttributes())
                            }
                        }
                    }
                }
            }
            return this
        }

    operator fun  get(attribute: String): Any? {
        return this.attributes[attribute]
    }

    protected fun <K : String> set(attribute: K, value: Attributes): GraphNode<Attributes> {
        this.attributes[attribute]  = value
        return this.dispatchEvent(mapOf("change" to attribute) as BaseEvent)
    }

//    protected fun <K : String> getRef(attribute: K): (GraphNode<*> & Attributes[K])? {
//            val ref = this.attributes[attribute] as Ref<Any>
//            return ref?.getChild() as GraphNode<*> & Attributes[K]
//    }
    protected fun getRef(attribute: String): (GraphNode<*>)? {
        val ref = this.attributes[attribute] as Ref<Map<String,Any>>
        return ref.getChild()
    }

    protected fun <K : String> setRef(
        attribute: K,
        value: GraphNode<*>? = null,
        attributes: MutableMap<String, Any>? = null
    ): GraphNode<Attributes> {
        if (this.immutableKeys.contains(attribute)) {
            throw IllegalStateException("Cannot overwrite immutable attribute, \"$attribute\".")
        }

        val prevRef = this.attributes[attribute] as Ref<Map<String,Any>>
        if (prevRef != null) prevRef.dispose() // TODO(cleanup): Possible duplicate event.

        if (value == null) return this

        val ref = this.graph.createEdge(attribute, this, value, attributes)
        ref.addEventListener("dispose") {
            this.attributes.remove(attribute)
            this.dispatchEvent(mapOf("change" to attribute) as BaseEvent)
        }
        this.attributes[attribute] = ref

        return this.dispatchEvent(mapOf("change" to attribute) as BaseEvent)
    }

    protected fun <K : String> listRefs(attribute: K): List<GraphNode<*>> {
        val refs = this.attributes[attribute] as MutableList<GraphEdge<*,*>>
        return refs.map { it.child }
    }

    protected fun <K : String> addRef(
        attribute: K,
        value: GraphNode<*>,
        attributes: MutableMap<String, Any>? = null
    ): GraphNode<Attributes> {
        val ref = this.graph.createEdge(attribute, this, value, attributes)

        val refs = this.attributes[attribute] as MutableList<GraphEdge<*,*>>
        refs.add(ref)

        ref.addEventListener("dispose") {
            val retained = refs.filter { it != ref }
            refs.clear()
            refs.addAll(retained)
            this.dispatchEvent(mapOf("change" to attribute) as BaseEvent)
        }

        return this.dispatchEvent(mapOf("change" to attribute) as BaseEvent)
    }

    protected fun <K : String> removeRef(
        attribute: K,
        value: GraphNode<*>
    ): GraphNode<Attributes> {
        val refs = this.attributes[attribute] as MutableList<GraphEdge<*,*>>
        val pruned = refs.filter { it.child == value }
        pruned.forEach { it.dispose() } // TODO(cleanup): Possible duplicate event.
        return this
    }

//    fun <K : RefMapKeys<Attributes>> listRefMapKeys(key: K): Array<String> {
//        return Object.keys(this[attributes][key] as Any) as Array<String>
//    }

    /** @hidden */
//    fun <K : RefMapKeys<Attributes>> listRefMapValues(key: K): List<GraphNode> {
//        return Object.values(this[attributes][key] as Any).map { it.getChild() }
//    }
//    fun
////            <K : RefMapKeys<Attributes>, SK : keyof Attributes[K]>
//    getRefMap(attribute: K, key: SK): GraphNode? {
//        val refMap = this[attributes][attribute] as Any
//        return refMap[key]?.getChild()
//    }

    /** @hidden */

    private fun
//            <K : RefMapKeys<Attributes>, SK : keyof Attributes[K]>
    setRefMap(
    attribute: Any,
    key: String,
    value: GraphNode<*>?,
    metadata: MutableMap<String, Any>? = null
    ): GraphNode<*> {
        val refMap = this.attributes
//            . attribute as Array<Any>
        val prevRef = refMap[key] as Ref<Map<String,Any>>
        if (prevRef != null) prevRef.dispose() // TODO(cleanup): Possible duplicate event.
        if (value == null) return this
        var metadata = metadata ?: mutableMapOf()
        val ref = graph.createEdge(attribute.toString(), this, value, (metadata + mutableMapOf("key" to key)) as MutableMap<String, Any>)
        ref.addEventListener("dispose") {
            refMap.remove(key)
            dispatchEvent(mapOf("type" to "change", "attribute" to attribute, "key" to key) as BaseEvent)
        }
        refMap[key] = ref
        return dispatchEvent(mapOf("type" to "change", "attribute" to attribute, "key" to key)as BaseEvent)
    }

    /**********************************************************************************************
     * Events.
     */

    override fun dispatchEvent(event: BaseEvent): GraphNode<Attributes> {
        super.dispatchEvent(event + mutableMapOf("target" to this) as BaseEvent)
        graph.dispatchEvent(event + mutableMapOf("target" to this, "type" to "node:${event.type}"))
        return this
    }


}
