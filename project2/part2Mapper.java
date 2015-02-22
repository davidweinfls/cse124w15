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

/**
 * This class assumes it is reading a datafile from the Netflix Prize dataset.
 * They have the following format:
 * 
 * First line is the movie ID, followed by a colon
 * Every other line is in the format: "UserID,RatingValue,RatingDate"
 * UserID is an integer. RatingValue is an integer, 1-5. RatingDate is
 *  YYYY-MM-DD
 * 
 * This Mapper class emits the intermediate pair: (UserID, {sum, count}).
 * UserID is an IntWritable object, and {sum, count} is an IntArrayWritable
 * of two IntWritable objects. 
 * 
 * @author Daniel Jackson, Scott Griffin
 *
 */
public class part2Mapper extends MapReduceBase
implements Mapper<WritableComparable,Writable,WritableComparable,Writable>,
Reducer<WritableComparable,Writable,WritableComparable,Writable> {
	
	private static Pattern userRatingDate = Pattern.compile("^(\\d+),(\\d+),(\\d{4}-\\d{2}-\\d{2})$");
    private static Pattern year_pattern = Pattern.compile("^(\\d{4})......");

    public String getYear(Text date) {
        String line = ((Text)date).toString();
        Matcher year_matcher = year_pattern.matcher(line);
        String year = "";

        if (year_matcher.matches()) {
            year = Integer.toString(Integer.parseInt(year_matcher.group(1)));
        } else {
            // year not match
        }
        return year;
    }
	
	/**
	 * Given a line of input, if it is a UserID,RatingValue,RatingDate line,
	 * it extracts the UserID and RatingValue, and emits (UserID, {RatingValue, 1})
	 */
	public void map(WritableComparable key, Writable values,
			OutputCollector output, Reporter reporter) throws IOException {
		
		/* key is line number(?). But I don't care */
		/* values is the line of the file */
		String line = ((Text)values).toString();
		/* Use a full blown Matcher, so I can pull out the grouped ID and Rating */
		Matcher ratingDate = userRatingDate.matcher(line);
		/* Apparently only need one instance, the OutputCollector will create
		 * its own copies of these objects.
		 */
		Text date = new Text();
		Text key_output = null;
		IntWritable rating_star = new IntWritable();
		IntWritable count = new IntWritable(1);
		
		if(line.matches("^\\d+:$")) {
			/* This is the Movie ID line. Ignore it */
		} else if (ratingDate.matches()) {
			date.set(ratingDate.group(3));

            int rate = Integer.parseInt(ratingDate.group(2));
            String year_star = getYear(date) + "_" + Integer.toString(rate);

            key_output = new Text(year_star);
			
			/* Add them to the output */
			output.collect(key_output, count);
			
		} else {
			/* Should not occur. The input is in an invalid format, or
			 * my regex is wrong.
			 */
		}
	}

	/**
	 * Combine function!
	 * 
	 * Assumes that the values iterator returns {IntWritable, IntWritable} 
	 * array, and simply sums the two values, and emits (key, {sum of 1, sum of 2})
	 */
	public void reduce(WritableComparable key, Iterator values, 
			OutputCollector output, Reporter reporter) throws IOException {

			int count = 0;
			IntWritable input = null, count_output;
			
			while(values.hasNext()) {

				input = (IntWritable)values.next();;
				
				count += input.get();
			}
			
			count_output = new IntWritable(count);
			
			output.collect(key, count_output);
		
	}

}
