package com.atlassian.ta.wiremockpactgenerator.pactgenerator;

import java.util.List;
import java.util.regex.Pattern;

class InteractionFilter {
    static boolean isRequestWhitelisted(final List<Pattern> requestPathWhitelist, final PactGeneratorRequest request) {
        return requestPathWhitelist.isEmpty() || pathMatchesWhitelist(request.getPath(), requestPathWhitelist);
    }

    private static boolean pathMatchesWhitelist(final String path, final List<Pattern> whitelist) {
        return whitelist.stream().anyMatch(pattern -> pattern.matcher(path).matches());
    }

    private InteractionFilter() {

    }
}
