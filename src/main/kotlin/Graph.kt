

/**
 * A graph manages a network of [GraphNode] nodes, connected
 * by [GraphEdge] edges.
 *
 * @category Graph
 */
class Graph<GraphNode : Map<String,Any>> : EventDispatcher<BaseEvent>() {
    private val _emptySet: MutableSet<Ref<Map<String,Any>>> = mutableSetOf()
    private val _edges: MutableSet<Ref<Map<String,Any>>> = mutableSetOf()
    private var _parentEdges: MutableMap<Any, MutableSet<Ref<Map<String,Any>>>> = mutableMapOf()
    private val _childEdges: MutableMap<Any, MutableSet<Ref<Map<String,Any>>>> = mutableMapOf()

    /** Returns a list of all parent->child edges on this graph. */
    fun listEdges(): List<Ref<Map<String,Any>>> {
        return _edges.toList()
    }

    /** Returns a list of all edges on the graph having the given node as their child. */
    private fun listParentEdges(node: Any): Set<Ref<Map<String,Any>>> {
        return _childEdges[node] ?: _emptySet
    }

    /** Returns a list of parent nodes for the given child node. */
    fun listParents(node: Any): List<Any> {
        return listParentEdges(node).map { it.getParent() }
    }

    /** Returns a list of all edges on the graph having the given node as their parent. */
    fun listChildEdges(node: Any): Set<Ref<Map<String,Any>>> {
        return _parentEdges[node] ?: _emptySet
    }

    /** Returns a list of child nodes for the given parent node. */
    fun listChildren(node: Any): List<Any> {
        return listChildEdges(node).map { it.getChild() }
    }

    fun disconnectParents(node: Any, filter: ((Any) -> Boolean)? = null): Graph<GraphNode> {
        var edges = listParentEdges(node)
        if (filter != null) {
            edges = edges.filter { filter(it.getParent()) }.toSet()
        }
        edges.forEach { it.dispose() }
        return this
    }

    /**
     * Creates a [GraphEdge] connecting two [GraphNode] instances. Edge is returned
     * for the caller to store.
     * @param a Owner
     * @param b Resource
     */
    fun createEdge(
        name: String,
        a:Any,
        b: Any,
        attributes: MutableMap<String, Any>? = null
    ): GraphEdge<*,*> {
        val edge= attributes?.let { GraphEdge(name,a,b, it) }
        return _registerEdge(edge)
    }

    private fun _registerEdge(edge: Ref<Map<String,Any>>): GraphEdge<*,*> {
        _edges.add(edge)

        val parent = edge.getParent()
        if (!_parentEdges.containsKey(parent)) {
            _parentEdges[parent] = mutableSetOf()
        }
        _parentEdges[parent]?.add(edge)

        val child = edge.getChild()
        if (!_childEdges.containsKey(child)) _childEdges[child] = mutableSetOf()
        _childEdges[child]?.add(edge)

        edge.addEventListener("dispose") { _removeEdge(edge) }
        return edge
    }

    /**
     * Removes the [GraphEdge] from the [Graph]. This method should only
     * be invoked by the onDispose() listener created in [_registerEdge]. The
     * public method of removing an edge is [GraphEdge.dispose].
     */
    private fun _removeEdge(edge: Ref<Map<String,Any>>): Graph<GraphNode> {
        _edges.remove(edge)
        _parentEdges[edge.getParent()]?.remove(edge)
        _childEdges[edge.getChild()]?.remove(edge)
        return this
    }
}
