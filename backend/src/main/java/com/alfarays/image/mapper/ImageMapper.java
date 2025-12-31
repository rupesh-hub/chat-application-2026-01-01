package com.alfarays.image.mapper;

import com.alfarays.image.entity.Image;
import com.alfarays.image.model.ImageResponse;

import java.util.List;
import java.util.stream.Collectors;

public final class ImageMapper {

    private ImageMapper() {
    }

    public static ImageResponse toResponse(Image image) {
        return ImageResponse.builder()
                .id(image.getId())
                .path(image.getPath())
                .filename(image.getFilename())
                .type(image.getType())
                .size(image.getSize())
                .createdOn(image.getCreatedOn().toString())
                .createdBy(image.getCreatedBy())
                .modifiedOn(image.getModifiedOn().toString())
                .modifiedBy(image.getModifiedBy())
                .build();
    }

    public static List<ImageResponse> toResponse(List<Image> images) {
        return images.stream()
                .map(ImageMapper::toResponse)
                .collect(Collectors.toList());
    }

}