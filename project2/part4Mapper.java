import java.io.IOException;
import java.io.*;
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
public class part4Mapper extends MapReduceBase
implements Mapper<WritableComparable,Writable,WritableComparable,Writable>,
Reducer<WritableComparable,Writable,WritableComparable,Writable> {
	
	private static Pattern userRatingDate = Pattern.compile("^(\\d+),(\\d+),\\d{4}-\\d{2}-\\d{2}$");
    private String movie_id = "";
	
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
		Matcher rating_matcher = userRatingDate.matcher(line);
		/* Apparently only need one instance, the OutputCollector will create
		 * its own copies of these objects.
		 */
//		IntWritable rating = new IntWritable();

		if(line.matches("^(\\d+):$")) {
            movie_id = line.substring(0, line.length()-1);
		} else if (rating_matcher.matches()) {
			int rating = Integer.parseInt(rating_matcher.group(2));

            if (rating == 5) {
                String s = movie_id + "_5";
                Text movie_rating = new Text(s);
                IntWritable count = new IntWritable(1);
                output.collect(movie_rating, count);
            } else if (rating == 1) {
                String s = movie_id + "_1";
                Text movie_rating = new Text(s);
                IntWritable count = new IntWritable(1);
                output.collect(movie_rating, count);
            } else {
                String s1 = movie_id + "_5";
                String s2 = movie_id + "_1";
                Text movie_rating1 = new Text(s1);
                Text movie_rating2 = new Text(s2);
                IntWritable count1 = new IntWritable(0);
                IntWritable count2 = new IntWritable(0);
                output.collect(movie_rating1, count1);
                output.collect(movie_rating2, count2);
            }
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
			IntWritable ratingInput = null, ratingOutput;
			
			while(values.hasNext()) {

				ratingInput = (IntWritable)values.next();;
				count += ratingInput.get();; 
			}
			ratingOutput = new IntWritable(count);
			
			output.collect(key, ratingOutput);
	}

}
