
typealias GraphNodeAttributesInternal = MutableMap<String, Any>

//abstract class GraphNode<Map<String,Any> : Map<String, Any>> = emptyMap()> : EventDispatcher<GraphNodeEvent> {
//    abstract class GraphNode<Map<String,Any> : Map<String, Any> = emptyMap()> : EventDispatcher<GraphNodeEvent> {



abstract class GraphNode<Attributes :Any>(private val graph: Graph) : EventDispatcher<GraphNodeEvent>() {
    private var disposed = false
    private val attributes: GraphNodeAttributesInternal
    private val immutableKeys: MutableSet<String> = mutableSetOf()

    init {
        this.attributes = this.createAttributes()
    }

    open fun getDefaults(): Map<String, Any>? {
        return mutableMapOf("nodes" to mutableListOf<GraphNode<*>>())
    }

    private fun createAttributes(): GraphNodeAttributesInternal {
        val defaultAttributes= this.getDefaults()
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
        return attributes as GraphNodeAttributesInternal
    }

    fun isOnGraph(other: GraphNode<*>): Boolean {
        return this.graph === other.graph
    }

    fun isDisposed(): Boolean {
        return this.disposed
    }

    override fun dispose() {
//        println("edege, ${this.graph.listEdges()}")

        if (this.disposed) return
        this.graph.listChildEdges(this).forEach { it.dispose() }
//        println("edege, ${this.graph.listEdges()}")
        this.graph.disconnectParents(this)
//        println("edege, ${this.graph.listEdges()}")

        this.disposed = true

        this.dispatchEvent(mutableMapOf("type" to "dispose"))
//        println("edege, ${this.graph.listEdges()}")
    }

    fun detach(): GraphNode<*> {
        this.graph.disconnectParents(this)
        return this
    }


    fun swap(old: GraphNode<*>, replacement: GraphNode<*>): GraphNode<*> {
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
                        this.removeRef(attribute , old ).addRef(attribute as String, replacement , refAttributes)
                    }
                }
                isRefMap(value) -> {
                    val refMap = value as RefMap
                    for (key in refMap.keys) {
                        val ref = refMap[key]
                        if (ref?.getChild() === old) {
                            if (ref != null) {
                                this.setRefMap(attribute as String, key, replacement, ref.getAttributes())
                            }
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

    protected fun <K : String> set(attribute: K, value: Map<String,Any>): GraphNode<*> {
        this.attributes[attribute]  = value
        return this.dispatchEvent(mutableMapOf("change" to attribute) )
    }

    //    protected fun <K : String> getRef(attribute: K): (GraphNode<*><*> & Map<String,Any>[K])? {
//            val ref = this.attributes[attribute] as Ref<Any>
//            return ref?.getChild() as GraphNode<*><*> & Map<String,Any>[K]
//    }
    protected fun getRef(attribute: String): (GraphNode<*>)? {
        val ref = this.attributes[attribute] as Ref
        return ref.getChild()
    }

    private fun <K : String> setRef(
        attribute: K,
        value: GraphNode<*>? = null,
        attributes: MutableMap<String, Any>? = null
    ): GraphNode<*> {
        if (this.immutableKeys.contains(attribute)) {
            throw IllegalStateException("Cannot overwrite immutable attribute, \"$attribute\".")
        }

        val prevRef = this.attributes[attribute] as Ref
        if (prevRef != null) prevRef.dispose() // TODO(cleanup): Possible duplicate event.

        if (value == null) return this

        val ref = this.graph.createEdge(attribute, this, value, attributes)
        ref.addEventListener("dispose") {
            this.attributes.remove(attribute)
            this.dispatchEvent(mutableMapOf("change" to attribute))
        }
        this.attributes[attribute] = ref

        return this.dispatchEvent(mutableMapOf("change" to attribute))
    }

    protected fun <K : String> listRefs(attribute: K): List<GraphNode<*>> {
        val refs = this.attributes[attribute] as MutableList<GraphEdge<*,*>>
        return refs.map { it.child }
    }

    protected fun addRef(
        attribute: String,
        value: GraphNode<*>,
        attrs: MutableMap<String, Any>? = null
    ): GraphNode<*>  {
        val ref = this.graph.createEdge(attribute, this, value, attrs)
        val refs = this.attributes[attribute] as MutableList<GraphEdge<GraphNode<*>,GraphNode<*>>>
        refs.add(ref)

        ref.addEventListener("dispose") {
            val retained = refs.filter { it != ref }
            refs.clear()
            refs?.addAll(retained)
            this.dispatchEvent(mutableMapOf("type" to "change" ,"attribute" to attribute))
        }
        return this.dispatchEvent(mutableMapOf("type" to "change" ,"attribute" to  attribute))
    }

    protected fun removeRef(
        attribute: String,
        value: GraphNode<*>
    ): GraphNode<*> {
//        println("beforeedges ${this.graph.listEdges()}")

        val refs = this.attributes[attribute] as MutableList<GraphEdge<*,*>>
        val pruned = refs.filter { it.child == value }
//        println("pruned $pruned")
        pruned.forEach { it.dispose() }
        // TODO(cleanup): Possible duplicate event.
//        println("afteredges ${this.graph.listEdges()}")
        return this
    }

//    fun <K : RefMapKeys<Map<String,Any>>> listRefMapKeys(key: K): Array<String> {
//        return Object.keys(this[attributes][key] as Any) as Array<String>
//    }

    /** @hidden */
//    fun <K : RefMapKeys<Map<String,Any>>> listRefMapValues(key: K): List<GraphNode> {
//        return Object.values(this[attributes][key] as Any).map { it.getChild() }
//    }
//    fun
////            <K : RefMapKeys<Map<String,Any>>, SK : keyof Map<String,Any>[K]>
//    getRefMap(attribute: K, key: SK): GraphNode? {
//        val refMap = this[attributes][attribute] as Any
//        return refMap[key]?.getChild()
//    }

    /** @hidden */

    private fun
//            <K : RefMapKeys<Map<String,Any>>, SK : keyof Map<String,Any>[K]>
            setRefMap(
        attribute: Any,
        key: String,
        value: GraphNode<*>?,
        metadata: MutableMap<String, Any>? = null
    ): GraphNode<*> {
        val refMap = this.attributes
//            . attribute as Array<Any>
        val prevRef = refMap[key] as Ref
        if (prevRef != null) prevRef.dispose() // TODO(cleanup): Possible duplicate event.
        if (value == null) return this
        val metadata = metadata ?: mutableMapOf()
        val ref = graph.createEdge(attribute.toString(), this, value, (metadata + mutableMapOf("key" to key)) as MutableMap<String, Any>)
        ref.addEventListener("dispose") {
            refMap.remove(key)
            dispatchEvent(mutableMapOf("type" to "change", "attribute" to attribute, "key" to key))
        }
        refMap[key] = ref
        return dispatchEvent(mutableMapOf("type" to "change", "attribute" to attribute, "key" to key))
    }

    /**********************************************************************************************
     * Events.
     */

    override fun dispatchEvent(event: MutableMap<String,Any>): GraphNode<*> {
        super.dispatchEvent(mutableMapOf("target" to this ,"type" to event))
        this.graph.dispatchEvent(mutableMapOf("target" to this, "type" to "node:${event["type"]}","type" to event))
        return this
    }



}
