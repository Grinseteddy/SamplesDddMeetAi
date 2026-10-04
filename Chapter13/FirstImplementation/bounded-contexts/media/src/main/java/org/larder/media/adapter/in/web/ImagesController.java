package org.larder.media.adapter.in.web;

import java.util.List;
import java.util.UUID;

import org.larder.media.adapter.in.web.api.ImagesApi;
import org.larder.media.adapter.in.web.model.ImageLink;
import org.larder.media.adapter.in.web.model.LinkType;
import org.larder.media.adapter.in.web.model.Media;
import org.larder.media.adapter.in.web.model.MediaCreate;
import org.larder.media.application.MediaService;
import org.larder.media.domain.BusinessObjectId;
import org.larder.media.domain.MediaId;
import org.larder.media.domain.UploaderId;
import org.larder.platform.security.CurrentCook;
import org.larder.platform.web.Links;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST adapter for {@code contracts/openapi/media.openapi.yaml}, tag Images. */
@RestController("mediaImagesController")
@RequestMapping("/media")
class ImagesController implements ImagesApi {

    private final MediaService service;

    ImagesController(MediaService service) {
        this.service = service;
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SCOPE_media:read', 'SCOPE_media:write')")
    public ResponseEntity<List<Media>> listImages(UUID businessObjectId, LinkType businessObjectType, String version) {
        return ResponseEntity.ok(service.imagesOf(MediaMapper.toDomain(businessObjectType), new BusinessObjectId(businessObjectId))
                .stream().map(MediaMapper::toApi).toList());
    }

    @Override
    @PreAuthorize("hasAuthority('SCOPE_media:write')")
    public ResponseEntity<ImageLink> createImage(String version, MediaCreate request) {
        var media = service.upload(caller(), MediaMapper.toDomain(request.getMedia()), MediaMapper.toDomain(request.getLinks()));
        var link = Links.below(media.id().value());
        return ResponseEntity.created(link).body(new ImageLink(link.toString()));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SCOPE_media:read', 'SCOPE_media:write')")
    public ResponseEntity<Media> getImageById(UUID mediaId, String version) {
        return ResponseEntity.ok(MediaMapper.toApi(service.image(new MediaId(mediaId))));
    }

    @Override
    @PreAuthorize("hasAuthority('SCOPE_media:write')")
    public ResponseEntity<Void> deleteImage(UUID mediaId, String version) {
        service.delete(caller(), new MediaId(mediaId));
        return ResponseEntity.noContent().build();
    }

    private static UploaderId caller() {
        return new UploaderId(CurrentCook.require().value());
    }
}
