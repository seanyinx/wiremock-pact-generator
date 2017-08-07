package com.atlassian.ta.wiremockpactgenerator;

import com.atlassian.ta.wiremockpactgenerator.builders.OptionsBuilder;
import com.atlassian.ta.wiremockpactgenerator.models.Pact;
import com.atlassian.ta.wiremockpactgenerator.models.PactInteraction;
import com.atlassian.ta.wiremockpactgenerator.models.PactRequest;
import com.atlassian.ta.wiremockpactgenerator.models.PactResponse;
import com.github.tomakehurst.wiremock.http.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class PactGenerator implements RequestListener {
    private Pact pact;
    private Options options;

    public PactGenerator(Options options){
        this.options = options;
        pact = new Pact(options.consumerName(), options.providerName());
    }

    public PactGenerator(String consumer, String provider) {
        this(new OptionsBuilder().withConsumerName(consumer).withProviderName(provider).build());
    }

    @Override
    public void requestReceived(Request request, Response response) {
        boolean wasAdded = pact.addInteraction(
            new PactInteraction("", toPactRequest(request), toPactResponse(response))
        );

        if(options.pactSaver().autoSave() && wasAdded){
            doSave();
        }
    }

    public void save(){
        if(!options.pactSaver().autoSave()){
            doSave();
        }
    }

    private void doSave(){
        try {
            options.pactSaver().save(options.pactSerializer().toJson(pact), pact);
        }
        catch (IOException error) {
            throw new RuntimeException("Unable to save generated pact file", error);
        }
    }

    private Map<String, String> parseHeaders(HttpHeaders wiremockHeaders) {
        Map<String, String> headers = new HashMap<>();
        for (HttpHeader header : wiremockHeaders.all()){
            String key = header.caseInsensitiveKey().value().toLowerCase(Locale.ENGLISH);
            String value = String.join(",", header.values());
            headers.put(key, value);
        }
        return headers;
    }

    private PactRequest toPactRequest(Request wiremockRequest){
        String method = wiremockRequest.getMethod().value();
        String body = wiremockRequest.getBodyAsString();
        String path;
        String query;
        try {
            URL url = new URL(wiremockRequest.getAbsoluteUrl());
            path = url.getPath();
            query = url.getQuery();
        } catch (MalformedURLException e) {
            path = "/";
            query = null;
        }

        Map<String, String> headers = parseHeaders(wiremockRequest.getHeaders());
        return new PactRequest(method, path, query, headers, body);
    }

    private PactResponse toPactResponse(Response wiremockResponse){
        return new PactResponse(wiremockResponse.getStatus(),
                parseHeaders(wiremockResponse.getHeaders()),
                wiremockResponse.getBodyAsString());
    }
}