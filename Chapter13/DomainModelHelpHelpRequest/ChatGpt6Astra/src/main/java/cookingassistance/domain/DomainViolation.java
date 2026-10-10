package cookingassistance.domain;

public final class DomainViolation extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public DomainViolation(String rule) { super(rule); }
    static void require(boolean condition, String rule) {
        if (!condition) throw new DomainViolation(rule);
    }
    static <T> T required(T value, String name) {
        require(value != null, name + "IsRequired"); return value;
    }
}
