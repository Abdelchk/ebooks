package fr.ensitech.ebooks.dto;

import lombok.Getter;

@Getter
public class BookDto {
    private final Long id;
    private final String title;
    private final String author;
    private final String category;
    private final int quantity;

    public BookDto(Long id, String title, String author, String category, int quantity) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.category = category;
        this.quantity = quantity;
    }

}