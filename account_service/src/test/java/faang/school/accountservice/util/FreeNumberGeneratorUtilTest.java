package faang.school.accountservice.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FreeNumberGeneratorUtilTest {

    @ParameterizedTest
    @CsvSource({
            "1234, 12, 108, 123400000108",
            "1234, 15, 4321, 123400000004321",
            "1234, 20, 100500, 12340000000000100500",
            "5678, 12, 1, 567800000001",
            "4321, 14, 99, 43210000000099",
            "9999, 20, 999999999, 99990000000999999999"
    })
    public void testGenerateAccountNumber(int accountCode, int accountLength, long count, String expected) {
        String actual = FreeNumberGeneratorUtil.generateAccountNumber(accountCode, accountLength, count);

        assertEquals(expected, actual);
    }
}
