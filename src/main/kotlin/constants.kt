/** TypeScript utility for nullable types. */
typealias Nullable<T> = Map<String, Any?>

/** Abstraction representing a typed array class. */
typealias TypedArray<T> = Array<T>

typealias Literal =Any?

//typealias LiteralKeys<T> = T where T : Literal
//typealias RefKeys<T> = T where T : GraphNode
//typealias RefListKeys<T> = T where T : List<GraphNode>
//typealias RefMapKeys<T> = T where T : Map<String, GraphNode>
// TODO reaplce with proper type
typealias LiteralKeys = Any
typealias RefKeys= Any
typealias RefListKeys = Any
//typealias RefMapKeys = Any

typealias Ref = GraphEdge<GraphNode<*>, GraphNode<*>>
typealias RefMap = Map<String, Ref>
typealias UnknownRef = Any

