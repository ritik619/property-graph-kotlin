/**
 * Represents a connection between two {@link GraphNode} resources in a {@link Graph}.
 *
 * The left node is considered the owner, and the right node the resource. The
 * owner is responsible for being able to find and remove a reference to a resource, given
 * that link. The resource does not hold a reference to the link or to the owner,
 * although that reverse lookup can be done on the graph.
 *
 * @category Graph
 */
open class GraphEdge<Parent : GraphNode<Map<String,Any>>, Child : GraphNode<Map<String,Any>>> (
    private val name: String,
    private val parent: Parent,
    var child: Child,
    private var attributes: MutableMap<String, Any> = mutableMapOf()
) : EventDispatcher<GraphEdgeEvent>() {

    private var disposed = false

    init {
        if (!parent.isOnGraph(child)) {
            throw Error("Cannot connect disconnected graphs.")
        }
    }

    /** Name. */
    fun getName(): String {
        return this.name
    }

    /** Owner node. */
    fun getParent(): Parent {
        return this.parent
    }

    /** Resource node. */
    fun getChild(): Child {
        return this.child
    }

    /**
     * Sets the child node.
     *
     * @internal Only {@link Graph} implementations may safely call this method directly. Use
     * 	{@link Property.swap} or {@link Graph.swapChild} instead.
     */
    fun setChild(child: Child): GraphEdge<Parent, Child> {
        this.child = child
        return this
    }

    /** Attributes of the graph node relationship. */
    fun getAttributes(): MutableMap<String, Any> {
        return this.attributes
    }

    /** Destroys a (currently intact) edge, updating both the graph and the owner. */
    override fun dispose() {
        if (this.disposed) return
        this.disposed = true
        this.dispatchEvent("dispose" as BaseEvent)
        super.dispose()
    }

    /** Whether this link has been destroyed. */
    fun isDisposed(): Boolean {
        return this.disposed
    }
}
