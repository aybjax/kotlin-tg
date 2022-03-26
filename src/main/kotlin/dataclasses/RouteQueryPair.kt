package dataclasses

import routes.enums.Routes

data class RouteQueryPair(
    val route: Routes,
    val query: Map<String, String> = emptyMap()
) {
    public fun toQueryString(): String {
        val queries = query.toSortedMap().map {
            it.key + "=" + it.value
        }.joinToString("&")
        return route.toString() + "?" + queries

    }
}

public infix fun Routes.queries(map: Map<String, String>): RouteQueryPair = RouteQueryPair(this, map)
