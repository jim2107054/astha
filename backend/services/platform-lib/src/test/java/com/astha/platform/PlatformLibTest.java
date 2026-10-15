package com.astha.platform;

import com.astha.platform.masking.PiiMasker;
import com.astha.platform.money.Money;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformLibTest {

    @Test
    void shouldFormatMoneyWithLakhAndCrore() {
        // 12,50,000.50 Taka = 125000050 paisa
        long paisa = 125000050L;
        String formatted = Money.formatTaka(paisa);
        assertThat(formatted).isEqualTo("৳12,50,000.50");

        String bnDigits = Money.toBengaliNumerals(formatted);
        assertThat(bnDigits).isEqualTo("৳১২,৫০,০০০.৫০");
    }

    @Test
    void shouldMaskPhoneAndNid() {
        String phone = "+8801712345678";
        assertThat(PiiMasker.maskPhone(phone)).isEqualTo("+8801****5678");

        String nid = "1990123456789";
        assertThat(PiiMasker.maskNid(nid)).isEqualTo("1990******6789");
    }
}
