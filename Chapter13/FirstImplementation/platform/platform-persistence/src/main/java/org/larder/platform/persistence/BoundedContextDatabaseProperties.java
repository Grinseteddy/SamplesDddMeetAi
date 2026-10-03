package org.larder.platform.persistence;

/**
 * Connection settings of one Bounded Context, bound from
 * {@code larder.<bounded-context>.database.*}.
 */
public record BoundedContextDatabaseProperties(String url, String schema, String username, String password) {
}
