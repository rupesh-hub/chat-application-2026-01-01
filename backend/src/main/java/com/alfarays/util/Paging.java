package com.alfarays.util;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Paging {

    private int page;
    private int size;
    private boolean isFirst;
    private boolean isLast;
    private long totalElements;
    private long totalPages;

}