package com.atlassian.ta.wiremockpactgenerator.utils;

import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestListener;
import com.github.tomakehurst.wiremock.http.Response;


public class RequestListenerProxy implements RequestListener {
    /**
     * Hack to allow a single WiremockServer instance in tests
     * to use different PactGenerators instances
     */
    private RequestListener delegate;

    public void delegateTo(RequestListener delegate){
        this.delegate = delegate;
    }

    @Override
    public void requestReceived(Request request, Response response) {
        if (delegate != null){
            delegate.requestReceived(request, response);
        }
    }
}
