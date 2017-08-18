package com.atlassian.ta.wiremockpactgenerator.unit;

import com.atlassian.ta.wiremockpactgenerator.PactGeneratorRequest;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

@RunWith(Parameterized.class)
public class JsonBodySerializationTest extends BasePactGeneratorTest {

    public enum HttpMessageType {REQUEST, RESPONSE}

    private final HttpMessageType httpMessageType;

    @Parameterized.Parameters(name = "{0} body serialization")
    public static Collection<HttpMessageType> parameters() {
        return Arrays.asList(HttpMessageType.REQUEST, HttpMessageType.RESPONSE);
    }

    public JsonBodySerializationTest(final HttpMessageType httpMessageType) {
        this.httpMessageType = httpMessageType;
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
        switch (httpMessageType){
            case REQUEST:
                return getBody("request");
            case RESPONSE:
                return getBody("response");
            default:
                throw new RuntimeException("httpMessageType is null. this should never happen");
        }
    }

    private void whenHttpMessageInInteractionContainsBody(final String body) {
        switch (httpMessageType) {
            case REQUEST:
                whenTheInteractionIsInvoked(
                        aPostRequest()
                                .withBody(body)
                                .build()
                );
                break;
            case RESPONSE:
                whenTheInteractionIsInvoked(
                        aDefaultResponse()
                                .withBody(body)
                                .build()
                );
                break;
            default:
                throw new RuntimeException("httpMessageType is null. this should never happen");
        }
    }

    private JsonElement getBody(final String requestOrResponse) {
        final String raw = getRawSavedPact();
        final JsonParser parser = new JsonParser();
        final JsonObject object = parser.parse(raw).getAsJsonObject();
        return object
                .getAsJsonArray("interactions")
                .get(0).getAsJsonObject()
                .getAsJsonObject(requestOrResponse)
                .get("body");
    }

    private PactGeneratorRequest.Builder aPostRequest() {
        return aDefaultRequest()
                .withMethod("POST");
    }
}
