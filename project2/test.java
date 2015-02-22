import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class test {

    public static void main (String[] args) {
        Pattern year_pattern = Pattern.compile("^(\\d{4})......");

        String line = "2015-02-05";
        Matcher year_matcher = year_pattern.matcher(line);

        if (year_matcher.matches()) {
            String year = year_matcher.group(1);
            System.out.println(year);
        } else {
            System.out.println("error");
        }
    }

}
