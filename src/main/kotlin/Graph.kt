

/**
 * A graph manages a network of [GraphNode] nodes, connected
 * by [GraphEdge] edges.
 *
 * @category Graph
 */
class Graph<GraphNode> : EventDispatcher<BaseEvent>() {
    private val _emptySet: Set<GraphEdge<T, T>> = setOf()
    private val _edges: Set<GraphEdge<T, T>> = setOf()
    private val _parentEdges: Map<T, Set<GraphEdge<T, T>>> = mapOf()
    private val _childEdges: Map<T, Set<GraphEdge<T, T>>> = mapOf()

    /** Returns a list of all parent->child edges on this graph. */
    fun listEdges(): List<GraphEdge<T, T>> {
        return _edges.toList()
    }

    /** Returns a list of all edges on the graph having the given node as their child. */
    fun listParentEdges(node: T): List<GraphEdge<T, T>> {
        return _childEdges[node] ?: _emptySet
    }

    /** Returns a list of parent nodes for the given child node. */
    fun listParents(node: T): List<T> {
        return listParentEdges(node).map { it.getParent() }
    }

    /** Returns a list of all edges on the graph having the given node as their parent. */
    fun listChildEdges(node: T): List<GraphEdge<T, T>> {
        return _parentEdges[node] ?: _emptySet
    }

    /** Returns a list of child nodes for the given parent node. */
    fun listChildren(node: T): List<T> {
        return listChildEdges(node).map { it.getChild() }
    }

    fun disconnectParents(node: T, filter: ((T) -> Boolean)? = null): Graph<T> {
        var edges = listParentEdges(node)
        if (filter != null) {
            edges = edges.filter { filter(it.getParent()) }
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
    fun <A : T, B : T> createEdge(
        name: String,
        a: A,
        b: B,
        attributes: Map<String, *>? = null
    ): GraphEdge<A, B> {
        return _registerEdge(GraphEdge(name, a, b, attributes)) as GraphEdge<A, B>
    }

    private fun _registerEdge(edge: GraphEdge<T, T>): GraphEdge<T, T> {
        _edges.add(edge)

        val parent = edge.getParent()
        if (!_parentEdges.containsKey(parent)) _parentEdges[parent] = setOf()
        _parentEdges[parent]?.add(edge)

        val child = edge.getChild()
        if (!_childEdges.containsKey(child)) _childEdges[child] = setOf()
        _childEdges[child]?.add(edge)

        edge.addEventListener("dispose") { _removeEdge(edge) }
        return edge
    }

    /**
     * Removes the [GraphEdge] from the [Graph]. This method should only
     * be invoked by the onDispose() listener created in [_registerEdge]. The
     * public method of removing an edge is [GraphEdge.dispose].
     */
    private fun _removeEdge(edge: GraphEdge<T, T>): Graph<T> {
        _edges.remove(edge)
        _parentEdges[edge.getParent()]?.remove(edge)
        _childEdges[edge.getChild()]?.remove(edge)
        return this
    }
}
