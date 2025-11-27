package fr.ensitech.ebooks.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

public abstract class Dates {

	private static SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

	public static final Date convertStringToDate(String dateStr) {
		Date date = null;
		try {
			date = formatter.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}

	public static final String convertDateToString(LocalDate localDate) {
		String dateStr = null;
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
			dateStr = dateFormat.format(localDate);
		} catch (Exception e){
			e.printStackTrace();
		}
		return dateStr;
	}

    
}

