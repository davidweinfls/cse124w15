
import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Writable;

/**
 * Must subclass ArrayWritable if it is to be the input to a Reduce function
 * because the valueClass is not written to the output. Wish there was
 * some documentation which said that...
 * 
 * @author Daniel
 *
 */
public class IntArrayWritable extends ArrayWritable {

	public IntArrayWritable(Writable[] values) {
		super(IntWritable.class, values);
	}

	public IntArrayWritable() {
		super(IntWritable.class);
	}

	public IntArrayWritable(Class valueClass, Writable[] values) {
		super(IntWritable.class, values);
	}

	public IntArrayWritable(Class valueClass) {
		super(IntWritable.class);
	}

	public IntArrayWritable(String[] strings) {
		super(strings);
	}

}
