package fr.ensitech.ebooks.email;

public class EmailContext {
    private EmailStrategy strategy;

    public void setStrategy(EmailStrategy strategy) {
        this.strategy = strategy;
    }

    public void executeStrategy(String to, Object... params) {
        strategy.sendEmail(to, params);
    }
}
