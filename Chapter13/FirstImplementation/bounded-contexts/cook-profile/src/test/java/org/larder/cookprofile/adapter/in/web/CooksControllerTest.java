package org.larder.cookprofile.adapter.in.web;

import static org.hamcrest.Matchers.endsWith;
import static org.larder.cookprofile.TestData.COOK;
import static org.larder.cookprofile.TestData.JOE;
import static org.larder.cookprofile.TestData.JOE_EMAIL;
import static org.larder.cookprofile.TestData.OTHER_COOK;
import static org.larder.cookprofile.TestData.joe;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.larder.cookprofile.application.AlreadyRegisteredException;
import org.larder.cookprofile.application.CookService;
import org.larder.cookprofile.application.NotFoundException;
import org.larder.cookprofile.application.NotPermittedException;
import org.larder.cookprofile.domain.CookChange;
import org.larder.cookprofile.domain.CookStatus;
import org.larder.cookprofile.domain.PersonName;
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

@WebMvcTest({CooksController.class, CookProfileErrorAdvice.class})
@Import({LarderSecurityConfiguration.class, LarderWebConfiguration.class})
class CooksControllerTest {

    private static final String JOE_JSON = """
            {"email":"joe.doe@larder.org","name":"Joe","givenName":"Doe"}""";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CookService service;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private static RequestPostProcessor cook(String... scopes) {
        return TestTokens.cook(COOK.value(), scopes);
    }

    @Test
    void listsCooksInTheContractShape() throws Exception {
        given(service.cooks(JOE, JOE_EMAIL)).willReturn(List.of(joe()));

        mvc.perform(get("/cook-profile/cooks").param("name", "Joe").param("email", "joe.doe@larder.org")
                        .header("version", "1.0.0").with(cook("cook:read")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cookId").value(COOK.value().toString()))
                .andExpect(jsonPath("$[0].email").value("joe.doe@larder.org"))
                .andExpect(jsonPath("$[0].name").value("Joe"))
                .andExpect(jsonPath("$[0].givenName").value("Doe"))
                .andExpect(jsonPath("$[0].memberSince").value("2026-09-21"))
                .andExpect(jsonPath("$[0].status").value("active"));
    }

    @Test
    void listingWithoutFiltersListsAllCooks() throws Exception {
        given(service.cooks(null, null)).willReturn(List.of(joe()));

        mvc.perform(get("/cook-profile/cooks").header("version", "1.0.0").with(cook("cook:write")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getsACookById() throws Exception {
        given(service.cook(COOK)).willReturn(joe());

        mvc.perform(get("/cook-profile/cooks/" + COOK.value()).header("version", "1.0.0").with(cook("cook:read")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cookId").value(COOK.value().toString()));
    }

    @Test
    void registersTheCallingUserAndLinksToTheCook() throws Exception {
        given(service.register(eq(COOK), any(), any(), any())).willReturn(joe());

        mvc.perform(post("/cook-profile/cooks").header("version", "1.0.0").with(cook("cook:write"))
                        .contentType(MediaType.APPLICATION_JSON).content(JOE_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/cook-profile/cooks/" + COOK.value())))
                .andExpect(jsonPath("$.cookLink").value(endsWith("/cook-profile/cooks/" + COOK.value())));
    }

    @Test
    void registeringTwiceIsABadRequest() throws Exception {
        given(service.register(eq(COOK), any(), any(), any())).willThrow(new AlreadyRegisteredException("again"));

        mvc.perform(post("/cook-profile/cooks").header("version", "1.0.0").with(cook("cook:write"))
                        .contentType(MediaType.APPLICATION_JSON).content(JOE_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ALREADY_REGISTERED"));
    }

    @Test
    void writingNeedsTheWriteScope() throws Exception {
        mvc.perform(post("/cook-profile/cooks").header("version", "1.0.0").with(cook("cook:read"))
                        .contentType(MediaType.APPLICATION_JSON).content(JOE_JSON))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/cook-profile/cooks/" + COOK.value()).header("version", "1.0.0").with(cook("cook:read")))
                .andExpect(status().isForbidden());
    }

    @Test
    void readingNeedsAToken() throws Exception {
        mvc.perform(get("/cook-profile/cooks").header("version", "1.0.0"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void malformedRequestsAreBadRequests() throws Exception {
        mvc.perform(post("/cook-profile/cooks").header("version", "1.0.0").with(cook("cook:write"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"joe.doe@larder.org\",\"name\":\"Joe\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
        mvc.perform(post("/cook-profile/cooks").header("version", "1.0.0").with(cook("cook:write"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"no-address\",\"name\":\"Joe\",\"givenName\":\"Doe\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/cook-profile/cooks").param("email", "no-address").header("version", "1.0.0").with(cook("cook:read")))
                .andExpect(status().isBadRequest());
        mvc.perform(patch("/cook-profile/cooks/" + COOK.value()).header("version", "1.0.0").with(cook("cook:write"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"gold\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void anEmptyChangeIsABadRequest() throws Exception {
        mvc.perform(patch("/cook-profile/cooks/" + COOK.value()).header("version", "1.0.0").with(cook("cook:write"))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_COOK"));
    }

    @Test
    void changesTheOwnProfileAndLinksToIt() throws Exception {
        given(service.change(any(), any(), any())).willReturn(joe());

        mvc.perform(patch("/cook-profile/cooks/" + COOK.value()).header("version", "1.0.0").with(cook("cook:write"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Jane\",\"status\":\"inActive\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cookLink").value(endsWith("/cook-profile/cooks/" + COOK.value())));

        then(service).should().change(COOK, COOK, new CookChange(null, new PersonName("Jane"), null, CookStatus.INACTIVE));
    }

    @Test
    void changingSomebodyElsesProfileIsForbidden() throws Exception {
        given(service.change(eq(COOK), eq(OTHER_COOK), any())).willThrow(new NotPermittedException("not yours"));

        mvc.perform(patch("/cook-profile/cooks/" + OTHER_COOK.value()).header("version", "1.0.0").with(cook("cook:write"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Joe\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("NOT_PERMITTED"));
    }

    @Test
    void deregisteringSomebodyElseIsForbidden() throws Exception {
        willThrow(new NotPermittedException("not yours")).given(service).deregister(COOK, OTHER_COOK);

        mvc.perform(delete("/cook-profile/cooks/" + OTHER_COOK.value()).header("version", "1.0.0").with(cook("cook:write")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("NOT_PERMITTED"));
    }

    @Test
    void deregisteringReturnsNoContent() throws Exception {
        mvc.perform(delete("/cook-profile/cooks/" + COOK.value()).header("version", "1.0.0").with(cook("cook:write")))
                .andExpect(status().isNoContent());

        then(service).should().deregister(COOK, COOK);
    }

    @Test
    void anUnknownCookIsNotFound() throws Exception {
        given(service.cook(OTHER_COOK)).willThrow(new NotFoundException("missing"));

        mvc.perform(get("/cook-profile/cooks/" + OTHER_COOK.value()).header("version", "1.0.0").with(cook("cook:read")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}
