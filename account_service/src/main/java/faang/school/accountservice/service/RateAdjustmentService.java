package faang.school.accountservice.service;

import java.math.BigDecimal;

public interface RateAdjustmentService {
    boolean adjustRate(long userId, BigDecimal rateChange);
}
