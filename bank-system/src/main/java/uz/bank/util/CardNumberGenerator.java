package uz.bank.util;

import java.security.SecureRandom;

public final class CardNumberGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private CardNumberGenerator() {
    }

    public static String generate() {
        StringBuilder cardNumber = new StringBuilder("8600");

        for (int i = 0; i < 12; i++) {
            cardNumber.append(RANDOM.nextInt(10));
        }

        return cardNumber.toString();
    }
}