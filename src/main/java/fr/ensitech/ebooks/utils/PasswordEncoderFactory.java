package fr.ensitech.ebooks.utils;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordEncoderFactory {

    private static final BCryptPasswordEncoder BCRYPT_ENCODER = new BCryptPasswordEncoder();
    private static final Argon2PasswordEncoder ARGON2_ENCODER =
            new Argon2PasswordEncoder(16, 32, 1, 4096, 3);

    public static PasswordEncoder getBCryptEncoder() {
        return BCRYPT_ENCODER;
    }

    public static PasswordEncoder getArgon2Encoder() {
        return ARGON2_ENCODER;
    }
}
