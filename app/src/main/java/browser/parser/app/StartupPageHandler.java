package browser.parser.app;

import java.time.LocalDate;

public class StartupPageHandler {
    
    public final static String startupPagePath = "file://src/main/resources/html/startup_page.html";
  
    public static String populateHTML(String html) {
        LocalDate date = LocalDate.now();
        String dayName = date.getDayOfWeek().toString().toLowerCase();
        int day = date.getDayOfMonth();
        String month = date.getMonth().toString().toLowerCase();
        
        dayName = dayName.substring(0, 1).toUpperCase() + dayName.substring(1);
        month = month.substring(0, 1).toUpperCase() + month.substring(1);
        
        String dayPostfix = "th";
        if (("" + day).endsWith("1")) dayPostfix = "st";
        if (("" + day).endsWith("2")) dayPostfix = "nd";
        if (("" + day).endsWith("3")) dayPostfix = "rd";
        
        return html
                .replace("$DAY_NAME$", dayName)
                .replace("$DAY_DATE$", day + dayPostfix)
                .replace("$MONTH_NAME$", month);
    }

}
