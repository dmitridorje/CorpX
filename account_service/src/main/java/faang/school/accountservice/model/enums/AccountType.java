package faang.school.accountservice.model.enums;

import lombok.Getter;

@Getter
public enum AccountType {
    INDIVIDUAL(4200, 20),
    CORPORATE(4300, 20),
    SAVINGS(4400, 20),
    INVESTMENT(4500, 20),
    RETIREMENT(4600, 18),
    STUDENT(4700, 18),
    BUSINESS(4800, 16),
    PREPAID(4900, 16);

    private final int type;
    private final int length;

    AccountType(int type, int length) {
        this.type = type;
        this.length = length;
    }
}
