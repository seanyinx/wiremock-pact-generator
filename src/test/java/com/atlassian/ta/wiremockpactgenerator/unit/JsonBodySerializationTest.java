package com.atlassian.ta.wiremockpactgenerator.unit;

import com.atlassian.ta.wiremockpactgenerator.pactgenerator.FileSystem;
import com.atlassian.ta.wiremockpactgenerator.pactgenerator.IdGenerator;
import com.atlassian.ta.wiremockpactgenerator.pactgenerator.PactGeneratorRequest;
import com.atlassian.ta.wiremockpactgenerator.pactgenerator.PactGeneratorResponse;
import com.atlassian.ta.wiremockpactgenerator.unit.support.PactGeneratorInvocation;
import com.atlassian.ta.wiremockpactgenerator.unit.support.PactFileSpy;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

@RunWith(Parameterized.class)
public class JsonBodySerializationTest {
    enum HttpMessageType {REQUEST, RESPONSE}

    interface Function<T> {
        T apply();
    }

    @Parameterized.Parameters(name = "{0} body serialization")
    public static Collection<HttpMessageType> parameters() {
        return Arrays.asList(HttpMessageType.REQUEST, HttpMessageType.RESPONSE);
    }

    @Parameterized.Parameter
    public HttpMessageType httpMessageType;

    @Mock
    private FileSystem fileSystem;

    @Mock
    private IdGenerator idGenerator;

    private PactGeneratorInvocation pactGeneratorInvocation;
    private boolean isStrictApplicationJson;
    private PactFileSpy pactFileSpy;
    private Consumer<String> whenHttpMessageInInteractionContainsBody;
    private Function<JsonElement> getCapturedBody;

    @Before
    public void beforeEach() {
        MockitoAnnotations.initMocks(this);
        isStrictApplicationJson = true;
        pactGeneratorInvocation = new PactGeneratorInvocation(fileSystem, idGenerator);
        pactFileSpy = new PactFileSpy(fileSystem);
        setHelpersForHttpMessageType();
    }

    private void setHelpersForHttpMessageType() {
        if (httpMessageType == HttpMessageType.REQUEST) {
            setHelpersForRequest();
        } else {
            setHelpersForResponse();
        }
    }

    private void setHelpersForRequest() {
        whenHttpMessageInInteractionContainsBody = (body) ->
                pactGeneratorInvocation
                        .withStrictApplicationJson(isStrictApplicationJson)
                        .withRequest(
                                new PactGeneratorRequest.Builder()
                                        .withMethod("POST")
                                        .withUrl("/path")
                                        .withBody(body)
                                        .build())
                        .invokeProcess();
        getCapturedBody = () -> pactFileSpy.firstRequestBodyAsJson();
    }

    private void setHelpersForResponse() {
        whenHttpMessageInInteractionContainsBody = (body) ->
                pactGeneratorInvocation
                        .withStrictApplicationJson(isStrictApplicationJson)
                        .withResponse(
                                new PactGeneratorResponse.Builder()
                                        .withStatus(200)
                                        .withBody(body)
                                        .build())
                        .invokeProcess();
        getCapturedBody = () -> pactFileSpy.firstResponseBodyAsJson();
    }

    private void givenStrictApplicationJsonIsDisabled() {
        isStrictApplicationJson = false;
    }

    @Test
    public void shouldSerializeBodyAsJsonObject_whenBodyIsJsonObject() {
        whenHttpMessageInInteractionContainsBody("{}");

        assertThatIsJsonObject(getCapturedBody());
    }

    @Test
    public void shouldSerializeBodyAsJsonArray_whenTheBodyIsAnArray() {
        whenHttpMessageInInteractionContainsBody("[]");

        assertThatIsJsonArray(getCapturedBody());
    }

    @Test
    public void shouldKeepTheBodyAsAString_whenBodyIsNotValidJson() {
        whenHttpMessageInInteractionContainsBody("{{}");

        final JsonElement body = getCapturedBody();
        assertThatIsStringPrimitive(body);
        assertThat(body.getAsString(), equalTo("{{}"));
    }

    @Test
    public void shouldNotSerializeBodyAsJson_whenBodyIsAJsonNumber() {
        whenHttpMessageInInteractionContainsBody("33.3");

        final JsonElement body = getCapturedBody();
        assertThatIsStringPrimitive(body);
        assertThat(body.getAsString(), equalTo("33.3"));
    }

    @Test
    public void shouldNotSerializeBodyAsJson_whenBodyIsAJsonBoolean() {
        whenHttpMessageInInteractionContainsBody("true");

        final JsonElement body = getCapturedBody();
        assertThatIsStringPrimitive(body);
        assertThat(body.getAsString(), equalTo("true"));
    }

    @Test
    public void shouldNotSerializeBodyAsJson_wheBodyIsAJsonNull() {
        whenHttpMessageInInteractionContainsBody("null");

        final JsonElement body = getCapturedBody();
        assertThatIsStringPrimitive(body);
        assertThat(body.getAsString(), equalTo("null"));
    }

    @Test
    public void shouldNotSerializeBodyAsJsonString_whenBodyIsAJsonString() {
        whenHttpMessageInInteractionContainsBody("\"a quoted string\"");

        final JsonElement body = getCapturedBody();
        assertThatIsStringPrimitive(body);
        assertThat(body.getAsString(), equalTo("\"a quoted string\""));
    }

    @Test
    public void shouldSerializeBodyAsJson_whenBodyIsAJsonNumberAndStrictApplicationJsonIsDisabled() {
        givenStrictApplicationJsonIsDisabled();
        whenHttpMessageInInteractionContainsBody("33");

        final JsonElement body = getCapturedBody();
        assertThatIsNumberPrimitive(body);
        assertThat(body.getAsNumber().intValue(), equalTo(33));
    }

    @Test
    public void shouldSerializeBodyAsJson_whenBodyIsAJsonBooleanAndStrictApplicationJsonIsDisabled() {
        givenStrictApplicationJsonIsDisabled();
        whenHttpMessageInInteractionContainsBody("true");

        final JsonElement body = getCapturedBody();
        assertThatIsBooleanPrimitive(body);
        assertThat(body.getAsBoolean(), equalTo(true));
    }

    @Test
    public void shouldSerializeBodyAsJson_wheBodyIsAJsonNullAndStrictApplicationJsonIsDisabled() {
        givenStrictApplicationJsonIsDisabled();
        whenHttpMessageInInteractionContainsBody("null");

        final JsonElement body = getCapturedBody();
        assertThatIsNullPrimitive(body);
    }

    @Test
    public void shouldSerializeBodyAsJsonString_whenBodyIsAJsonStringAndStrictApplicationJsonIsDisabled() {
        givenStrictApplicationJsonIsDisabled();
        whenHttpMessageInInteractionContainsBody("\"a quoted string\"");

        final JsonElement body = getCapturedBody();
        assertThatIsStringPrimitive(body);
        assertThat(body.getAsString(), equalTo("a quoted string"));
    }

    @Test
    public void shouldSerializeBodyWithANullType() {
        whenHttpMessageInInteractionContainsBody("{\"nullValue\": null}");

        final JsonElement body = getCapturedBody();

        assertThatIsJsonObject(body);
        assertThatIsNullPrimitive(body.getAsJsonObject().get("nullValue"));
    }

    @Test
    public void shouldSerializeBodyWithANumberType() {
        whenHttpMessageInInteractionContainsBody("{\"value\": 42}");

        final JsonObject body = getCapturedBody().getAsJsonObject();

        assertThatIsNumberPrimitive(body.get("value"));
        assertThat(body.get("value").getAsDouble(), equalTo(42.0));
    }

    @Test
    public void shouldSerializeBodyWithABooleanType() {
        whenHttpMessageInInteractionContainsBody("{\"value\": true}");

        final JsonObject body = getCapturedBody().getAsJsonObject();

        assertThatIsBooleanPrimitive(body.get("value"));
        assertThat(body.get("value").getAsBoolean(), is(true));
    }

    @Test
    public void shouldSerializeBodyWithAStringType() {
        whenHttpMessageInInteractionContainsBody("{\"value\": \"a string\"}");

        final JsonObject body = getCapturedBody().getAsJsonObject();

        assertThatIsStringPrimitive(body.get("value"));
        assertThat(body.get("value").getAsString(), equalTo("a string"));
    }

    private void assertThatIsJsonObject(final JsonElement element) {
        assertThat("Element is an object", element.isJsonObject(), is(true));
    }

    private void assertThatIsJsonArray(final JsonElement element) {
        assertThat("Element is an array", element.isJsonArray(), is(true));
    }

    private void assertThatIsNullPrimitive(final JsonElement element) {
        assertThat("Element is a primitive", element.isJsonNull(), is(true));
    }

    private void assertThatIsStringPrimitive(final JsonElement element) {
        assertThat("Element is a primitive", element.isJsonPrimitive(), is(true));
        assertThat("Element is a string", element.getAsJsonPrimitive().isString(), is(true));
    }

    private void assertThatIsNumberPrimitive(final JsonElement element) {
        assertThat("Element is a primitive", element.isJsonPrimitive(), is(true));
        assertThat("Element is a number", element.getAsJsonPrimitive().isNumber(), is(true));
    }

    private void assertThatIsBooleanPrimitive(final JsonElement element) {
        assertThat("Element is a primitive", element.isJsonPrimitive(), is(true));
        assertThat("Element is a boolean", element.getAsJsonPrimitive().isBoolean(), is(true));
    }

    private JsonElement getCapturedBody() {
        return getCapturedBody.apply();
    }

    private void whenHttpMessageInInteractionContainsBody(final String body) {
        whenHttpMessageInInteractionContainsBody.accept(body);
    }
}
