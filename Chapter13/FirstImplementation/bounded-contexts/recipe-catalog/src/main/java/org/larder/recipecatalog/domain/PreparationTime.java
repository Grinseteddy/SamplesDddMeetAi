package org.larder.recipecatalog.domain;

import java.util.regex.Pattern;

/** Total time needed to prepare a recipe, written as hours and minutes {@code HH:MM}, e.g. {@code 02:00}. */
public record PreparationTime(int hours, int minutes) {

    private static final Pattern FORMAT = Pattern.compile("^([0-9]{2}):([0-5][0-9])$");

    public PreparationTime {
        if (hours < 0 || hours > 99 || minutes < 0 || minutes > 59) {
            throw RecipeRuleViolationException.invalid(
                    "A preparation time has 0 to 99 hours and 0 to 59 minutes, not " + hours + ":" + minutes);
        }
    }

    public static PreparationTime parse(String text) {
        var matcher = text == null ? null : FORMAT.matcher(text);
        if (matcher == null || !matcher.matches()) {
            throw RecipeRuleViolationException.invalid("A preparation time is written as HH:MM, not '" + text + "'");
        }
        return new PreparationTime(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
    }

    public static PreparationTime ofMinutes(int totalMinutes) {
        return new PreparationTime(totalMinutes / 60, totalMinutes % 60);
    }

    public int totalMinutes() {
        return hours * 60 + minutes;
    }

    @Override
    public String toString() {
        return "%02d:%02d".formatted(hours, minutes);
    }
}
