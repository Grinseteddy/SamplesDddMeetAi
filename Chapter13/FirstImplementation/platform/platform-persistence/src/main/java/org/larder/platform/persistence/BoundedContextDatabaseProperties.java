package org.larder.platform.persistence;

/**
 * Connection settings of one Bounded Context, bound from
 * {@code larder.<bounded-context>.database.*}.
 *
 * @param poolSize maximum connections of this context's pool. All contexts share one
 *                 database instance (ADR0002), so the pools must fit its connection limit
 *                 together. Defaults to {@value #DEFAULT_POOL_SIZE}.
 */
public record BoundedContextDatabaseProperties(String url, String schema, String username, String password,
        Integer poolSize) {

    public static final int DEFAULT_POOL_SIZE = 3;

    public BoundedContextDatabaseProperties {
        if (poolSize == null) {
            poolSize = DEFAULT_POOL_SIZE;
        }
    }
}
