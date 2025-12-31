package com.alfarays.image.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImageResponse {

    private Long id;
    private String path;
    private String filename;
    private String type;
    private long size;
    private String createdOn;
    private String createdBy;
    private String modifiedOn;
    private String modifiedBy;

}
