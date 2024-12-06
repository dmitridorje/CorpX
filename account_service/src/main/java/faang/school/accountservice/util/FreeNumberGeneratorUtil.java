package faang.school.accountservice.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class FreeNumberGeneratorUtil {

    public static String generateAccountNumber(int accountCode, int accountLength, long count) {
        String codeString = String.format("%04d", accountCode);
        String countString = String.format("%0" + (accountLength - 4) + "d", count);

        return String.join("", codeString, countString).trim();
    }
}
