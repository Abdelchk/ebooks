package fr.ensitech.ebooks.enums;

public enum SecurityQuestionEnum {
    MOTHER_MAIDEN_NAME("Quel est le nom de jeune fille de votre mère ?"),
    FIRST_PET_NAME("Quel était le nom de votre premier animal de compagnie ?"),
    BIRTH_CITY("Quelle est votre ville de naissance ?"),
    PRIMARY_SCHOOL("Quel était le nom de votre école primaire ?"),
    FAVORITE_DISH("Quel est votre plat préféré ?");

    private final String question;

    SecurityQuestionEnum(String question) {
        this.question = question;
    }

    public String getQuestion() {
        return question;
    }
}
