package org.larder.platform.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Malformed requests answer 400 with the contracts' Error body; links point below the current URL. */
class ApiErrorAdviceTest {

    record Body(@NotBlank String name) {
    }

    @RestController
    @Validated
    static class Probe {

        @PostMapping("/things")
        ResponseEntity<Void> create(@RequestHeader("version") String version, @Valid @RequestBody Body body) {
            URI link = Links.below(UUID.fromString("23a8eeed-35f6-460b-892e-7bb458a8fded"));
            return ResponseEntity.created(link).build();
        }

        @GetMapping("/things")
        String list(@RequestParam("id") UUID id) {
            return Links.current().toString();
        }
    }

    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new Probe())
            .setControllerAdvice(new ApiErrorAdvice()).build();

    @Test
    void invalidBodyIsABadRequest() throws Exception {
        mvc.perform(post("/things").header("version", "1.0.0").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void unreadableBodyIsABadRequest() throws Exception {
        mvc.perform(post("/things").header("version", "1.0.0").contentType(MediaType.APPLICATION_JSON).content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void missingHeaderOrParameterOrWrongTypeIsABadRequest() throws Exception {
        mvc.perform(post("/things").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Scones\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/things")).andExpect(status().isBadRequest());
        mvc.perform(get("/things").param("id", "not-a-uuid")).andExpect(status().isBadRequest());
    }

    @Test
    void createdResourcesAreLinkedBelowTheCollection() throws Exception {
        mvc.perform(post("/things").header("version", "1.0.0").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Scones\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/things/23a8eeed-35f6-460b-892e-7bb458a8fded"));
    }
}
