package com.rentivo.backend.common.web;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/** Builds bounded page requests so clients can never ask for unbounded result sets. */
public final class Paging {

    public static final int MAX_SIZE = 50;

    private Paging() {
    }

    public static Pageable of(int page, int size, Sort sort) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), MAX_SIZE);
        return PageRequest.of(safePage, safeSize, sort);
    }
}
