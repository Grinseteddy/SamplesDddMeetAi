package org.larder.platform.web;

import java.net.URI;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** Links returned by create and change operations of the contracts. */
public final class Links {

    private Links() {
    }

    /** Link to a resource created below the current collection URL, e.g. {@code POST /consents} → {@code /consents/{id}}. */
    public static URI below(Object id) {
        return ServletUriComponentsBuilder.fromCurrentRequestUri().path("/{id}").buildAndExpand(id).toUri();
    }

    /** Link to the current request URL, e.g. after {@code PATCH /cooks/{id}}. */
    public static URI current() {
        return ServletUriComponentsBuilder.fromCurrentRequestUri().build().toUri();
    }
}
