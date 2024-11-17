package pl.pomoku.mcframework.utils;

import pl.pomoku.mcframework.command.ArgumentMatcher;

import java.util.Collections;
import java.util.List;

public class MatcherUtil {
    public static List<String> getMatchingStrings(List<String> tabCompletions, String arg, ArgumentMatcher argumentMatcher) {
        if (tabCompletions == null || arg == null) return Collections.emptyList();
        List<String> result = argumentMatcher.filter(tabCompletions, arg);
        Collections.sort(result);
        return result;
    }
}
