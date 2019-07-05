package ws;

import javax.xml.bind.DatatypeConverter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class WsDateTimeAdapter {
	
	public static Date parseDateTime(String dateTime) {
		return DatatypeConverter.parseDateTime(dateTime).getTime();
	}

	public static String printDateTime(Date dateTime) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(dateTime);
		return DatatypeConverter.printDateTime(cal);
	}
}