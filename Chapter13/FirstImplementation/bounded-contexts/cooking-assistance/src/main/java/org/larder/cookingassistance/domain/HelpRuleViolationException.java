package org.larder.cookingassistance.domain;

/** A change would break one of the rules of help requests and helps; nothing is changed. */
public class HelpRuleViolationException extends RuntimeException {

    /** Title, description or preferred providers outside what the published language allows. */
    public static final String INVALID_HELP_REQUEST = "INVALID_HELP_REQUEST";
    /** A recipe is mandatory for Preparation Step Explanation and Ingredient Substitute. */
    public static final String RECIPE_REQUIRED = "RECIPE_REQUIRED";
    /** A Preparation Step Explanation names the unclear step. */
    public static final String HOW_TO_STEP_REQUIRED = "HOW_TO_STEP_REQUIRED";
    /** A howToStep is only allowed for Preparation Step Explanation. */
    public static final String HOW_TO_STEP_NOT_ALLOWED = "HOW_TO_STEP_NOT_ALLOWED";
    /** An Ingredient Substitute names at least one ingredient. */
    public static final String INGREDIENTS_REQUIRED = "INGREDIENTS_REQUIRED";
    /** Ingredients are only allowed for Ingredient Substitute. */
    public static final String INGREDIENTS_NOT_ALLOWED = "INGREDIENTS_NOT_ALLOWED";
    /** Chef support is only available for Menu proposal. */
    public static final String CHEF_ONLY_FOR_MENU_PROPOSAL = "CHEF_ONLY_FOR_MENU_PROPOSAL";
    /** Chef help is provided exclusively - Chef cannot be combined with another provider. */
    public static final String CHEF_EXCLUSIVE = "CHEF_EXCLUSIVE";
    /** Status Answered needs at least one help answering the request. */
    public static final String ANSWERED_WITHOUT_HELP = "ANSWERED_WITHOUT_HELP";
    /** An answered request keeps its references, providers and status; only title and description change. */
    public static final String HELP_REQUEST_ANSWERED = "HELP_REQUEST_ANSWERED";
    /** Only an open request can be withdrawn. */
    public static final String HELP_REQUEST_NOT_OPEN = "HELP_REQUEST_NOT_OPEN";
    /** The answerType must be the same as the type of the help request. */
    public static final String ANSWER_TYPE_MISMATCH = "ANSWER_TYPE_MISMATCH";
    /** recipe, howToStep and ingredients of the answer must be the same as in the help request. */
    public static final String ANSWER_REFERENCE_MISMATCH = "ANSWER_REFERENCE_MISMATCH";
    /** An answer body outside what the published language allows (empty text, no courses, ...). */
    public static final String INVALID_ANSWER = "INVALID_ANSWER";
    /** The help provider type is not one of the providers the requester selected. */
    public static final String PROVIDER_NOT_PREFERRED = "PROVIDER_NOT_PREFERRED";
    /** A cook can answer their own request - except for Chef requests. */
    public static final String OWN_CHEF_REQUEST = "OWN_CHEF_REQUEST";
    /** Chef and community help name the person who gave it; only the Grandma Avatar's help has none. */
    public static final String INVALID_HELP_PROVIDER = "INVALID_HELP_PROVIDER";

    private final String code;

    public HelpRuleViolationException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
