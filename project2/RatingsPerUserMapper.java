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
 * Mapper (1/2) for part 3. Creates map of users to number of ratings.
 * 
 * @author Daniel Jackson, Scott Griffin
 *
 */
public class RatingsPerUserMapper extends MapReduceBase
implements Mapper<WritableComparable,Writable,WritableComparable,Writable>,
Reducer<WritableComparable,Writable,WritableComparable,Writable> {

	private static Pattern userRatingDate = Pattern.compile("^(\\d+),(\\d+),(\\d{4}-\\d{2}-\\d{2})$");


    /**
     * Given a line of input, if it is a UserID,RatingValue,RatingDate line,
     * it emits (UserID, 1)
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
		IntWritable count = new IntWritable(1);
		
		if(line.matches("^\\d+:$")) {
			/* This is the Movie ID line. Ignore it */
		} else if (userRating.matches()) {
			userId.set(Integer.parseInt(userRating.group(1)));
			
			/* Add them to the output */
			output.collect(userId, count);
		} else {
			/* Should not occur. The input is in an invalid format, or
			 * my regex is wrong.
			 */
		}
	}

	/**
	 * Combine function!
	 * 
	 * Assumes that the values iterator returns IntWritable
	 * array, and simply sums the two values, and emits (UserID, # of ratings)
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
