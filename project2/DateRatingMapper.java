import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

public class DateRatingMapper extends MapReduceBase
implements Mapper<WritableComparable,Writable,WritableComparable,Writable>,
Reducer<WritableComparable,Writable,WritableComparable,Writable> {

    private static Pattern userRatingDate = Pattern.compile("^(\\d+),(\\d+),(\\d{4}-\\d{2}-\\d{2})$");

    public void map(WritableComparable Key, Writable values, 
            OutputCollector output, Reporter reporter) throws IOException {
        
        String line = ((Text)values).toString();
        Matcher dateRating = userRatingDate.matcher(line);

        Text date = new Text();
        IntWritable num_of_rate = new IntWritable(1);

        if (line.matches("^\\d+:$")) {
            // ignore Move ID
        } else if (dateRating.matches()) {
            date.set(dateRating.group(3));
            System.out.println("date is: " + date);

            output.collect(date, num_of_rate);
        } else {
            // error
        }
    }

    public void reduce (WritableComparable key, Iterator values, 
            OutputCollector output, Reporter reporter) throws IOException {
        int total_rating = 0;
        IntWritable ratingInput = null, ratingOutput;

        while(values.hasNext()) {
            ratingInput = (IntWritable)values.next();
            total_rating += ratingInput.get();
        }
        ratingOutput = new IntWritable(total_rating);
        output.collect(key, ratingOutput);
    }
}
