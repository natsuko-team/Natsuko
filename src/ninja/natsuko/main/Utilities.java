package ninja.natsuko.main;

public class Utilities {
	public static String longMilisToTime(long ms) {
        long time = ms / 1000;
        long seconds = time % 60;
        time /= 60;
        long minutes = time % 60;
        time /= 60;
        long hours = time % 24;
        time /= 24;
        long days = time;
        
        String strseconds = Long.toString(seconds).length() == 1 ? '0' + Long.toString(Math.round(Math.floor(seconds))): Long.toString(Math.round(Math.floor(seconds)));
        String strminutes = Long.toString(minutes).length() == 1 ? '0' + Long.toString(Math.round(Math.floor(minutes))) : Long.toString(Math.round(Math.floor(minutes)));
        String strhours = Long.toString(hours).length() == 1 ? '0' + Long.toString(Math.round(Math.floor(hours))) : Long.toString(Math.round(Math.floor(hours)));

        return days+(days == 1 ? " Day, " : " Days, ")+strhours+":"+strminutes+":"+strseconds;
    }
}
