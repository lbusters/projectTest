package groupEntity;

import java.io.Serializable;
import java.util.Calendar;

public class ChartsView implements Serializable{

	private String dateName;
	private String extraDate;
	private String dateParam;
	
	public String getMonth() {
		//得到上个月
		int month = Calendar.getInstance().get(Calendar.MONTH);
		return Integer.toString(month);
	}

	public String getDateName() {
		return dateName;
	}

	public void setDateName(String dateName) {
		this.dateName = dateName;
	}

	public String getExtraDate() {
		return extraDate;
	}

	public void setExtraDate(String extraDate) {
		this.extraDate = extraDate;
	}

	public String getDateParam() {
		return dateParam;
	}

	public void setDateParam(String dateParam) {
		this.dateParam = dateParam;
	}

	
	
	
	
}
