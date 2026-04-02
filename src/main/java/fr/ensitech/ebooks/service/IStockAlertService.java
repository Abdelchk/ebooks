package fr.ensitech.ebooks.service;

import fr.ensitech.ebooks.entity.StockAlert;
import java.util.List;

public interface IStockAlertService {
    StockAlert createAlert(Long userId, Long bookId) throws Exception;
    StockAlert cancelAlert(Long alertId, Long userId) throws Exception;
    List<StockAlert> getUserAlerts(Long userId) throws Exception;
    void notifyUsersForBook(Long bookId) throws Exception;
}

