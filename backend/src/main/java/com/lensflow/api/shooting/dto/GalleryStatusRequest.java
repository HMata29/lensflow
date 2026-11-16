
package com.lensflow.api.shooting.dto;

import com.lensflow.api.shooting.entity.GalleryStatus;
import jakarta.validation.constraints.NotNull;

public class GalleryStatusRequest {

    @NotNull(message = "Lo stato della galleria è obbligatorio")
    private GalleryStatus galleryStatus;

    public GalleryStatusRequest() {
    }

    public GalleryStatus getGalleryStatus() {
        return galleryStatus;
    }

    public void setGalleryStatus(GalleryStatus galleryStatus) {
        this.galleryStatus = galleryStatus;
    }
}
