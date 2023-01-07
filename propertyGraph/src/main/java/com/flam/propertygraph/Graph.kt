

/**
 * A graph manages a network of [GraphNode] nodes, connected
 * by [GraphEdge] edges.
 *
 * @category Graph
 */
class Graph : EventDispatcher<BaseEvent>() {
    private val _emptySet: MutableSet<Ref> = mutableSetOf()
    private val _edges: MutableSet<GraphEdge<GraphNode<*>, GraphNode<*>>> = mutableSetOf()
    private var _parentEdges: MutableMap<Any, MutableSet<Ref>> = mutableMapOf()
    private val _childEdges: MutableMap<Any, MutableSet<Ref>> = mutableMapOf()

    /** Returns a list of all parent->child edges on this graph. */
    fun listEdges(): List<Ref> {
        return _edges.toList()
    }

    /** Returns a list of all edges on the graph having the given node as their child. */
    fun listParentEdges(node: Any): MutableList<GraphEdge<GraphNode<*>,GraphNode<*>>> {
        return _childEdges[node]?.toMutableList() ?: _emptySet.toMutableList()
    }

    /** Returns a list of parent nodes for the given child node. */
    fun listParents(node: Any): List<Any> {
        return listParentEdges(node).map { it.getParent() }
    }

    /** Returns a list of all edges on the graph having the given node as their parent. */
    fun listChildEdges(node: Any): Set<Ref> {
        return _parentEdges[node] ?: _emptySet
    }

    /** Returns a list of child nodes for the given parent node. */
    fun listChildren(node: Any): List<Any> {
        return listChildEdges(node).map { it.getChild() }
    }

    fun disconnectParents(node: GraphNode<*>, filter: ((Any) -> Boolean)? = null): Graph {
        var edges = listParentEdges(node)
//        println("dc parent edge $edges")
        if (filter != null) {
            edges = edges.filter { filter(it.getParent()) }.toMutableList()
        }
        edges.forEach { it.dispose() }
//        println("dc parent edge $edges")
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
        a:GraphNode<*>,
        b: GraphNode<*>,
        attributes: MutableMap<String, Any>? = null
    ): GraphEdge<GraphNode<*>,GraphNode<*>> {
        if(attributes !=null)
        {
            val edge=  GraphEdge(name,a ,b, attributes)
            return _registerEdge(edge)

        }
        else {
            val edge=  GraphEdge(name,a ,b)
            return _registerEdge(edge)
        }}

    private fun _registerEdge(edge: GraphEdge<GraphNode<*>, GraphNode<*>>): GraphEdge<GraphNode<*>,GraphNode<*>> {
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
    private fun _removeEdge(edge: Ref): Graph {
        _edges.remove(edge)
        _parentEdges[edge.getParent()]?.remove(edge)
        _childEdges[edge.getChild()]?.remove(edge)
        return this
    }
}
