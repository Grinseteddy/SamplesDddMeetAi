package org.larder.media.adapter.in.web;

import static org.hamcrest.Matchers.endsWith;
import static org.larder.media.TestData.COOK;
import static org.larder.media.TestData.HELP_REQUEST;
import static org.larder.media.TestData.HELP_REQUEST_ID;
import static org.larder.media.TestData.NOW;
import static org.larder.media.TestData.PNG;
import static org.larder.media.TestData.PNG_BASE64;
import static org.larder.media.TestData.RECIPE;
import static org.larder.media.TestData.RECIPE_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.larder.media.application.Image;
import org.larder.media.application.ImageStoreException;
import org.larder.media.application.MediaService;
import org.larder.media.application.NotFoundException;
import org.larder.media.application.NotPermittedException;
import org.larder.media.domain.LinkType;
import org.larder.media.domain.Media;
import org.larder.media.domain.MediaId;
import org.larder.platform.security.LarderSecurityConfiguration;
import org.larder.platform.test.TestTokens;
import org.larder.platform.web.LarderWebConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest({ImagesController.class, MediaErrorAdvice.class})
@Import({LarderSecurityConfiguration.class, LarderWebConfiguration.class})
class ImagesControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private MediaService service;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private static RequestPostProcessor cook(String... scopes) {
        return TestTokens.cook(COOK.value(), scopes);
    }

    private static String upload(String base64, String links) {
        return """
                {"media":"%s","links":%s}""".formatted(base64, links);
    }

    private static final String RECIPE_LINK = """
            [{"type":"recipe","url":"%s"}]""".formatted(RECIPE.url());

    @Test
    void uploadsAnImageForTheCallingCookAndLinksToIt() throws Exception {
        Media media = Media.upload(COOK, PNG, List.of(RECIPE), NOW);
        given(service.upload(COOK, PNG, List.of(RECIPE))).willReturn(media);

        mvc.perform(post("/media/images").header("version", "1.0.0").with(cook("media:write"))
                        .contentType(MediaType.APPLICATION_JSON).content(upload(PNG_BASE64, RECIPE_LINK)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/media/images/" + media.id().value())))
                .andExpect(jsonPath("$.imageLink").value(endsWith("/media/images/" + media.id().value())));
    }

    @Test
    void linksMayBeOmitted() throws Exception {
        Media media = Media.upload(COOK, PNG, List.of(), NOW);
        given(service.upload(COOK, PNG, List.of())).willReturn(media);

        mvc.perform(post("/media/images").header("version", "1.0.0").with(cook("media:write"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"media\":\"%s\"}".formatted(PNG_BASE64)))
                .andExpect(status().isCreated());
    }

    @Test
    void invalidImagesAreBadRequests() throws Exception {
        for (String base64 : List.of("not base64!", "JVBERi0xLjc=" /* a PDF header */, "")) {
            mvc.perform(post("/media/images").header("version", "1.0.0").with(cook("media:write"))
                            .contentType(MediaType.APPLICATION_JSON).content(upload(base64, "[]")))
                    .andExpect(status().isBadRequest());
        }
        mvc.perform(post("/media/images").header("version", "1.0.0").with(cook("media:write"))
                        .contentType(MediaType.APPLICATION_JSON).content(upload("not base64!", "[]")))
                .andExpect(jsonPath("$.code").value("INVALID_IMAGE"));
        then(service).should(never()).upload(any(), any(), any());
    }

    @Test
    void invalidLinksAreBadRequests() throws Exception {
        mvc.perform(post("/media/images").header("version", "1.0.0").with(cook("media:write"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(upload(PNG_BASE64, "[{\"type\":\"recipe\",\"url\":\"https://larder.org/recipe-catalog/recipes\"}]")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_LINK"));
        mvc.perform(post("/media/images").header("version", "1.0.0").with(cook("media:write"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(upload(PNG_BASE64, "[{\"type\":\"recipe\",\"url\":\"https://larder.org/a b/%s\"}]".formatted(RECIPE_ID.value()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_LINK"));
        mvc.perform(post("/media/images").header("version", "1.0.0").with(cook("media:write"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(upload(PNG_BASE64, "[{\"type\":\"recipe\",\"url\":\"https://larder.org/%s/%s\"}]".formatted("x".repeat(2048), RECIPE_ID.value()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
        mvc.perform(post("/media/images").header("version", "1.0.0").with(cook("media:write"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(upload(PNG_BASE64, "[{\"type\":\"video\",\"url\":\"%s\"}]".formatted(RECIPE.url()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void uploadingNeedsTheWriteScope() throws Exception {
        mvc.perform(post("/media/images").header("version", "1.0.0").with(cook("media:read"))
                        .contentType(MediaType.APPLICATION_JSON).content(upload(PNG_BASE64, RECIPE_LINK)))
                .andExpect(status().isForbidden());
    }

    @Test
    void readingNeedsAToken() throws Exception {
        mvc.perform(get("/media/images/" + MediaId.newId().value()).header("version", "1.0.0"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void returnsAnImageBase64EncodedWithItsLinks() throws Exception {
        Media media = Media.upload(COOK, PNG, List.of(RECIPE, HELP_REQUEST), NOW);
        given(service.image(media.id())).willReturn(new Image(media, PNG));

        mvc.perform(get("/media/images/" + media.id().value()).header("version", "1.0.0").with(cook("media:read")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mediaId").value(media.id().value().toString()))
                .andExpect(jsonPath("$.media").value(PNG_BASE64))
                .andExpect(jsonPath("$.links[0].type").value("recipe"))
                .andExpect(jsonPath("$.links[0].url").value(RECIPE.url().toString()))
                .andExpect(jsonPath("$.links[1].type").value("helpRequest"));
    }

    @Test
    void listsTheImagesOfABusinessObjectInTheContractSpelling() throws Exception {
        Media media = Media.upload(COOK, PNG, List.of(HELP_REQUEST), NOW);
        given(service.imagesOf(eq(LinkType.HELP_REQUEST), eq(HELP_REQUEST_ID))).willReturn(List.of(new Image(media, PNG)));

        mvc.perform(get("/media/images").param("businessObjectId", HELP_REQUEST_ID.value().toString())
                        .param("businessObjectType", "helpRequest").header("version", "1.0.0").with(cook("media:read")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].mediaId").value(media.id().value().toString()))
                .andExpect(jsonPath("$[0].links[0].type").value("helpRequest"));
    }

    @Test
    void aBusinessObjectWithoutMediaYieldsAnEmptyList() throws Exception {
        mvc.perform(get("/media/images").param("businessObjectId", HELP_REQUEST_ID.value().toString())
                        .param("businessObjectType", "thanks").header("version", "1.0.0").with(cook("media:write")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void listingNeedsIdAndKnownType() throws Exception {
        mvc.perform(get("/media/images").param("businessObjectType", "recipe").header("version", "1.0.0").with(cook("media:read")))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/media/images").param("businessObjectId", HELP_REQUEST_ID.value().toString())
                        .param("businessObjectType", "video").header("version", "1.0.0").with(cook("media:read")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void anUnknownMediaIsNotFound() throws Exception {
        MediaId id = MediaId.newId();
        given(service.image(id)).willThrow(new NotFoundException("missing"));

        mvc.perform(get("/media/images/" + id.value()).header("version", "1.0.0").with(cook("media:read")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void deletingSomebodyElsesMediaIsForbidden() throws Exception {
        MediaId id = MediaId.newId();
        willThrow(new NotPermittedException("not yours")).given(service).delete(COOK, id);

        mvc.perform(delete("/media/images/" + id.value()).header("version", "1.0.0").with(cook("media:write")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("NOT_PERMITTED"));
    }

    @Test
    void deletingReturnsNoContentAndNeedsTheWriteScope() throws Exception {
        MediaId id = MediaId.newId();
        mvc.perform(delete("/media/images/" + id.value()).header("version", "1.0.0").with(cook("media:write")))
                .andExpect(status().isNoContent());
        then(service).should().delete(COOK, id);

        mvc.perform(delete("/media/images/" + id.value()).header("version", "1.0.0").with(cook("media:read")))
                .andExpect(status().isForbidden());
    }

    @Test
    void anUnreachableBucketIsAServerError() throws Exception {
        MediaId id = MediaId.newId();
        given(service.image(id)).willThrow(new ImageStoreException("bucket down"));

        mvc.perform(get("/media/images/" + id.value()).header("version", "1.0.0").with(cook("media:read")))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("STORAGE_UNAVAILABLE"));
    }
}
