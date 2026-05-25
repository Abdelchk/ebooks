package fr.ensitech.ebooks.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class ChatResponse {
    private final String reply;
    private final List<BookDto> books;

    public ChatResponse(String reply, List<BookDto> books) {
        this.reply = reply;
        this.books = books;
    }

}