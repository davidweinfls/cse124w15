
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
public class UserRatingMapper extends MapReduceBase
implements Mapper<WritableComparable,Writable,WritableComparable,Writable>,
Reducer<WritableComparable,Writable,WritableComparable,Writable> {
	
	private static Pattern userRatingDate = Pattern.compile("^(\\d+),(\\d+),\\d{4}-\\d{2}-\\d{2}$");
	
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
		Matcher userRating = userRatingDate.matcher(line);
		/* Apparently only need one instance, the OutputCollector will create
		 * its own copies of these objects.
		 */
		IntWritable userId = new IntWritable();
		IntWritable ratingSum = new IntWritable();
		IntWritable ratingCount = new IntWritable(1);
		
		Writable[] writableArray = new IntWritable[2];
		
		IntArrayWritable ratingOutput = new IntArrayWritable(writableArray);

		if(line.matches("^\\d+:$")) {
			/* This is the Movie ID line. Ignore it */
		} else if (userRating.matches()) {
			/* It is a line to pull data from */
			/* 1st Regex Group is UserID */
			userId.set(Integer.parseInt(userRating.group(1)));
			/* 2nd Regex Group is Rating */
			ratingSum.set(Integer.parseInt(userRating.group(2)));
			
			writableArray[0] = ratingSum;
			writableArray[1] = ratingCount;
			
			/* Add them to the output */
			output.collect(userId, ratingOutput);
			
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

			int sum = 0, count = 0;
			IntArrayWritable ratingInput = null, ratingOutput;
			Writable[] inputArray = null;
			IntWritable[] outputArray = new IntWritable[2];
			
			while(values.hasNext()) {

				ratingInput = (IntArrayWritable)values.next();;
				inputArray = (Writable[])ratingInput.get();; 
				
				sum += ((IntWritable)inputArray[0]).get();
				count += ((IntWritable) inputArray[1]).get();
			}
			
			outputArray[0] = new IntWritable(sum);
			outputArray[1] = new IntWritable(count);
			ratingOutput = new IntArrayWritable(outputArray);
			
			output.collect(key, ratingOutput);
		
	}

}
