import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.io.Text;

public class RatingsPerDateReducer extends MapReduceBase
implements Reducer<WritableComparable,Writable,WritableComparable,Writable>
{

	public void reduce(WritableComparable key, Iterator values,
			OutputCollector output, Reporter reporter) throws IOException {
		int total_rating = 0;
		IntWritable ratingInput = null;
		
		while(values.hasNext()) {
			ratingInput = (IntWritable)values.next();;
            total_rating += ratingInput.get();
		}
		output.collect(key, new IntWritable(total_rating));
	}

}
