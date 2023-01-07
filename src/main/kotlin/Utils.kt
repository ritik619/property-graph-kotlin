package utils
import GraphEdge
fun isRef(value: Any): Boolean {
    return value is GraphEdge<*,*>
}

fun isRefList(value: Any): Boolean {
    return value is List<*> && value[0] is GraphEdge<*,*>
}

fun isRefMap(value: Any): Boolean {
    return value is Map<*, *> && value.values.first() is GraphEdge<*,*>
}
