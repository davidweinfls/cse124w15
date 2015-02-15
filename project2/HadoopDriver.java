
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapred.*;

/**
 * Driver class for PolyHadoop project. Runs the Hadoop instance, setting
 * up the Map/Reduce classes, input/output paths, and the Key/Value classes.
 * 
 * This does not use a Combiner class - it would add complexity to reduce
 * (necessitating a weighted average), for little to no gain. In the common
 * case, a user will only rate a movie once, and each MapTask probably operates
 * on a single data file at a time.
 * 
 * Invoke with 2 arguments, or 1.
 * 
 * @author Daniel
 *
 */
public class HadoopDriver {

	public static void main(String[] args) {
		/* Require args to contain the paths */
		if(args.length != 1 && args.length != 2) {
			System.err.println("Error! Usage: \n" +
					"HadoopDriver <input dir> <output dir>\n" +
					"HadoopDriver <job.xml>");
			System.exit(1);
		}
		
		JobClient client = new JobClient();
		JobConf conf = null;
		
		if(args.length == 2) {
			conf = new JobConf(HadoopDriver.class);
			
			/* UserRatingMapper outputs (IntWritable, IntArrayWritable(Writable[2])) */
			conf.setMapOutputKeyClass(IntWritable.class);
			conf.setMapOutputValueClass(IntArrayWritable.class);
			
			/* AverageValueReducer outputs (IntWritable, FloatWritable) */
			conf.setOutputKeyClass(IntWritable.class);
			conf.setOutputValueClass(FloatWritable.class);
	
			/* Pull input and output Paths from the args */
			conf.setInputFormat(TextInputFormat.class);
			conf.setOutputFormat(TextOutputFormat.class);
			
			FileInputFormat.setInputPaths(conf, new Path(args[0]));
			FileOutputFormat.setOutputPath(conf, new Path(args[1]));
	
			/* Set to use Mapper and Reducer classes */
			conf.setMapperClass(UserRatingMapper.class);
			conf.setCombinerClass(UserRatingMapper.class);
			conf.setReducerClass(AverageValueReducer.class);
			
			conf.set("mapred.child.java.opts", "-Xmx2048m");
		} else {
			conf = new JobConf(args[0]);
		}
		
		client.setConf(conf);
		try {
			JobClient.runJob(conf);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

