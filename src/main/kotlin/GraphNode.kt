typealias GraphNodeAttributesInternal<Parent extends GraphNode, Attributes extends {}> = {
    [Key in keyof Attributes]: Attributes[Key] extends GraphNode
    ? GraphEdge<Parent, Attributes[Key]>
    : Attributes[Key] extends GraphNode[]
    ? GraphEdge<Parent, Attributes[Key][number]>[]
    : Attributes[Key] extends { [key: string]: GraphNode }
    ? Record<string, GraphEdge<Parent, Attributes[Key][string]>>
    : Attributes[Key]
}

const val $attributes = Symbol('attributes')
const val $immutableKeys = Symbol('immutableKeys')

abstract class GraphNode<Attributes : Map<String, Any> = emptyMap()> : EventDispatcher<GraphNodeEvent> {
//    abstract class GraphNode<Attributes : Map<String, Any> = emptyMap()> : EventDispatcher<GraphNodeEvent> {

    private var disposed = false
    protected val graph: Graph<GraphNode>
    protected val attributes: GraphNodeAttributesInternal<GraphNode<Attributes>, Attributes>
    protected val immutableKeys: MutableSet<String>

    constructor(graph: Graph<GraphNode>) {
        this.graph = graph
        this.immutableKeys = mutableSetOf()
        this.attributes = this._createAttributes()
    }

    protected abstract fun getDefaults(): Map<String, Any>?

    private fun _createAttributes(): GraphNodeAttributesInternal<GraphNode<Attributes>, Attributes> {
        val defaultAttributes = this.getDefaults()
        val attributes = mutableMapOf<String, Any>()
        for (key in defaultAttributes!!.keys) {
            val value = defaultAttributes[key] as Any
            if (value is GraphNode) {
                val ref = this.graph.createEdge(key, this, value)
                ref.addEventListener("dispose", { value.dispose() })
                this.immutableKeys.add(key)
                attributes[key] = ref as Any
            } else {
                attributes[key] = value as Any
            }
        }
        return attributes as GraphNodeAttributesInternal<GraphNode<Attributes>, Attributes>
    }

    fun isOnGraph(other: GraphNode<*>): Boolean {
        return this.graph === other.graph
    }

    fun isDisposed(): Boolean {
        return this.disposed
    }

    fun dispose() {
        if (this.disposed) return
        this.graph.listChildEdges(this).forEach { it.dispose() }
        this.graph.disconnectParents(this)
        this.disposed = true
        this.dispatchEvent(GraphNodeEvent("dispose"))
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
                        val ref = value as Ref<Any>
                        if (ref.getChild() === old) {
                            this.setRef(attribute as String, replacement as Any, ref.getAttributes())
                        }
                    }
                    isRefList(value) -> {
                        val refs = value as List<Ref<Any>>
                        val ref = refs.find { it.getChild() === old }
                        if (ref != null) {
                            val refAttributes = ref.getAttributes()
                            this.removeRef(attribute as String, old as Any).addRef(attribute as String, replacement as Any, refAttributes)
                        }
                    }
                    isRefMap(value) -> {
                        val refMap = value as RefMap<Any>
                        for (key in refMap.keys) {
                            val ref = refMap[key]
                            if (ref.getChild() === old) {
                                this.setRefMap(attribute as String, key, replacement as Any, ref.getAttributes())
                            }
                        }
                    }
                }
            }
            return this
        }

    fun <K : String> get(attribute: K): Attributes[K] {
        return this.attributes[attribute] as Attributes[K]
    }

    protected fun <K : String> set(attribute: K, value: Attributes[K]): GraphNode<Attributes> {
        (this.attributes[attribute] as Attributes[K]) = value
        return this.dispatchEvent(GraphNodeEvent("change", attribute))
    }

    protected fun <K : String> getRef(attribute: K): (GraphNode<*> & Attributes[K])? {
            val ref = this.attributes[attribute] as Ref<Any>
            return ref?.getChild() as GraphNode<*> & Attributes[K]
    }

    protected fun <K : String> setRef(
        attribute: K,
        value: GraphNode<*>? = null,
        attributes: Map<String, Any>? = null
    ): GraphNode<Attributes> {
        if (this.immutableKeys.contains(attribute)) {
            throw IllegalStateException("Cannot overwrite immutable attribute, \"$attribute\".")
        }

        val prevRef = this.attributes[attribute] as Ref<Any>
        if (prevRef != null) prevRef.dispose() // TODO(cleanup): Possible duplicate event.

        if (value == null) return this

        val ref = this.graph.createEdge(attribute, this, value, attributes)
        ref.addEventListener("dispose") {
            this.attributes.remove(attribute)
            this.dispatchEvent(GraphNodeEvent("change", attribute))
        }
        this.attributes[attribute] = ref

        return this.dispatchEvent(GraphNodeEvent("change", attribute))
    }

    protected fun <K : String> listRefs(attribute: K): List<GraphNode<*>> {
        val refs = this.attributes[attribute] as List<Ref<Any>>
        return refs.map { it.child }
    }

    protected fun <K : String> addRef(
        attribute: K,
        value: GraphNode<*>,
        attributes: Map<String, Any>? = null
    ): GraphNode<Attributes> {
        val ref = this.graph.createEdge(attribute, this, value, attributes)

        val refs = this.attributes[attribute] as List<Ref<Any>>
        refs.add(ref)

        ref.addEventListener("dispose") {
            val retained = refs.filter { it != ref }
            refs.clear()
            refs.addAll(retained)
            this.dispatchEvent(GraphNodeEvent("change", attribute))
        }

        return this.dispatchEvent(GraphNodeEvent("change", attribute))
    }

    protected fun <K : String> removeRef(
        attribute: K,
        value: GraphNode<*>
    ): GraphNode<Attributes> {
        val refs = this.attributes[attribute] as List<Ref<Any>>
        val pruned = refs.filter { it.child == value }
        pruned.forEach { it.dispose() } // TODO(cleanup): Possible duplicate event.
        return this
    }

    fun <K : RefMapKeys<Attributes>> listRefMapKeys(key: K): Array<String> {
        return Object.keys(this[$attributes][key] as Any) as Array<String>
    }

    /** @hidden */
    fun <K : RefMapKeys<Attributes>> listRefMapValues(key: K): List<GraphNode> {
        return Object.values(this[$attributes][key] as Any).map { it.getChild() }
    }
    fun <K : RefMapKeys<Attributes>, SK : keyof Attributes[K]> getRefMap(attribute: K, key: SK): GraphNode? {
        val refMap = this[$attributes][attribute] as Any
        return refMap[key]?.getChild()
    }

    /** @hidden */
    fun <K : RefMapKeys<Attributes>, SK : keyof Attributes[K]> setRefMap(
    attribute: K,
    key: SK,
    value: GraphNode?,
    metadata: Map<String, *>? = null
    ): GraphNode {
        val refMap = this[$attributes][attribute] as Any
        val prevRef = refMap[key]
        if (prevRef != null) prevRef.dispose() // TODO(cleanup): Possible duplicate event.
        if (value == null) return this
        metadata = metadata ?: mapOf()
        val ref = graph.createEdge(attribute.toString(), this, value, metadata + mapOf("key" to key))
        ref.addEventListener("dispose") {
            refMap.remove(key)
            dispatchEvent(mapOf("type" to "change", "attribute" to attribute, "key" to key))
        }
        refMap[key] = ref
        return dispatchEvent(mapOf("type" to "change", "attribute" to attribute, "key" to key))
    }

    /**********************************************************************************************
     * Events.
     */

    fun dispatchEvent(event: Map<String, *>): GraphNode {
        super.dispatchEvent(event + mapOf("target" to this))
        graph.dispatchEvent(event + mapOf("target" to this, "type" to "node:${event["type"]}"))
        return this
    }


}
