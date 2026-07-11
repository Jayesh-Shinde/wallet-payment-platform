package com.jayeshshinde.walletpaymentplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication

public class WalletPaymentPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(WalletPaymentPlatformApplication.class, args);
    }

}
