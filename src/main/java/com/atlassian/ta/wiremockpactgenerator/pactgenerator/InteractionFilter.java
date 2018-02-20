package com.atlassian.ta.wiremockpactgenerator.pactgenerator;

import java.util.List;
import java.util.regex.Pattern;

public class InteractionFilter {
    private final List<Pattern> requestPathWhitelist;
    private final List<Pattern> requestPathBlacklist;

    public InteractionFilter(final List<Pattern> requestPathWhitelist, final List<Pattern> requestPathBlacklist) {
        this.requestPathWhitelist = requestPathWhitelist;
        this.requestPathBlacklist = requestPathBlacklist;
    }

    public boolean isRequestAccepted(final PactGeneratorRequest request) {
        return (requestPathWhitelist.isEmpty() || pathMatchesAnyPattern(request.getPath(), requestPathWhitelist)) &&
                !pathMatchesAnyPattern(request.getPath(), requestPathBlacklist);
    }

    private static boolean pathMatchesAnyPattern(final String path, final List<Pattern> patternList) {
        return patternList.stream().anyMatch(pattern -> pattern.matcher(path).matches());
    }
}
