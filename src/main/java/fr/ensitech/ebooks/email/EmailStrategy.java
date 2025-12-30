package fr.ensitech.ebooks.email;

public interface EmailStrategy {
    void sendEmail(String to, Object... params);
}
