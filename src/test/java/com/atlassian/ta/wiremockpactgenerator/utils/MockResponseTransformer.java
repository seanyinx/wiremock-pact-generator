package com.atlassian.ta.wiremockpactgenerator.utils;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;

import java.util.*;


public class MockResponseTransformer extends ResponseTransformer {
    private String body;
    private int status;
    private Map<String, HttpHeader> addHeaders;
    private List<String> removeHeaders;

    public MockResponseTransformer(){
        reset();
    }

    public MockResponseTransformer reset(){
        body =  null;
        status = 0;
        addHeaders = new HashMap<>();
        removeHeaders = new ArrayList<>();
        return this;
    }

    public MockResponseTransformer addHeader(String name, String value){
        String headerName = name.toLowerCase(Locale.ENGLISH);
        addHeaders.put(headerName, new HttpHeader(headerName, value));
        return this;
    }

    public MockResponseTransformer removeHeader(String name){
        removeHeaders.add(name.toLowerCase(Locale.ENGLISH));
        return this;
    }

    public MockResponseTransformer changeBodyTo(String newBody){
        body = newBody;
        return this;
    }

    public MockResponseTransformer changeStatusTo(int newStatusCode){
        status = newStatusCode;
        return this;
    }

    @Override
    public boolean applyGlobally() {
        return true;
    }

    @Override
    public Response transform(Request request, Response response, FileSource files, Parameters parameters) {
        Response.Builder builder =  Response.Builder.like(response).but();
        if(body != null){
            builder.body(body);
        }
        if (status != 0){
            builder.status(status);
        }

        List<HttpHeader> existingHeaders = new ArrayList<>(response.getHeaders().all());
        HttpHeaders newHeaders = new HttpHeaders();
        for (HttpHeader existingHeader: existingHeaders){
            String headerName = existingHeader.caseInsensitiveKey().value().toLowerCase(Locale.ENGLISH);
            if (addHeaders.containsKey(headerName) || removeHeaders.contains(headerName)){
                continue;
            }
            newHeaders = newHeaders.plus(existingHeader);
        }
        for (HttpHeader newHeader: addHeaders.values()){
            newHeaders = newHeaders.plus(newHeader);
        }
        builder.headers(newHeaders);
        return builder.build();
    }

    @Override
    public String getName() {
        return "mock-response-transformer";
    }
}
