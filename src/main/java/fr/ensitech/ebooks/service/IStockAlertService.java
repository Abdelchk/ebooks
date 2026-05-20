package fr.ensitech.ebooks.service;

import fr.ensitech.ebooks.entity.StockAlert;
import java.util.List;

public interface IStockAlertService {
    StockAlert createAlert(Long userId, Long bookId);
    StockAlert cancelAlert(Long alertId, Long userId);
    List<StockAlert> getUserAlerts(Long userId);
    void notifyUsersForBook(Long bookId);
}
