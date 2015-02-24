
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.io.Text;

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
		if(args.length != 1 && args.length != 2 && args.length != 3) {
			System.err.println("Error! Usage: \n" +
					"HadoopDriver <input dir> <output dir> <part number>\n" +
					"HadoopDriver <job.xml>");
			System.exit(1);
		}
		
		JobClient client = new JobClient();
		JobConf conf = null;
		
		if(args.length == 3) {
			conf = new JobConf(HadoopDriver.class);
            int part = Integer.parseInt(args[2]);
            conf.setInputFormat(TextInputFormat.class);
            conf.setOutputFormat(TextOutputFormat.class);
            FileOutputFormat.setOutputPath(conf, new Path(args[1]));

            switch (part) {

                case 0: 
                /* UserRatingMapper outputs (IntWritable, IntArrayWritable(Writable[2])) */
                conf.setMapOutputKeyClass(IntWritable.class);
                conf.setMapOutputValueClass(IntArrayWritable.class);

                /* Set to use Mapper and Reducer classes */
                conf.setMapperClass(UserRatingMapper.class);
                conf.setCombinerClass(UserRatingMapper.class);
                conf.setReducerClass(AverageValueReducer.class);

                /* AverageValueReducer outputs (IntWritable, FloatWritable) */
                conf.setOutputKeyClass(IntWritable.class);
                conf.setOutputValueClass(FloatWritable.class);
                FileInputFormat.setInputPaths(conf, new Path(args[0]));

                    break;

                case 1:
                // DateRatingMapper outputs (Text, IntWritable)
                conf.setMapOutputKeyClass(Text.class);
                conf.setMapOutputValueClass(IntWritable.class);

                conf.setMapperClass(DateRatingMapper.class);
                conf.setCombinerClass(DateRatingMapper.class);
                conf.setReducerClass(RatingsPerDateReducer.class);

                conf.setOutputKeyClass(Text.class);
                conf.setOutputValueClass(IntWritable.class);
                FileInputFormat.setInputPaths(conf, new Path(args[0]));

                    break;

                case 2:
                conf.setMapOutputKeyClass(Text.class);
                conf.setMapOutputValueClass(IntWritable.class);

                conf.setMapperClass(part2Mapper.class);
                conf.setCombinerClass(part2Mapper.class);
                conf.setReducerClass(part2Reducer.class);

                conf.setOutputKeyClass(Text.class);
                conf.setOutputValueClass(IntWritable.class);
                FileInputFormat.setInputPaths(conf, new Path(args[0]));

                    break;

                case 3:
                JobConf prejobConf = new JobConf(HadoopDriver.class);
                prejobConf.setMapOutputKeyClass(IntWritable.class);
                prejobConf.setMapOutputValueClass(IntWritable.class);

                prejobConf.setMapperClass(RatingsPerUserMapper.class);
                prejobConf.setCombinerClass(RatingsPerUserMapper.class);
                prejobConf.setReducerClass(RatingsPerUserReducer.class);

                prejobConf.setOutputKeyClass(IntWritable.class);
                prejobConf.setOutputValueClass(IntWritable.class);

                prejobConf.setInputFormat(TextInputFormat.class);
                prejobConf.setOutputFormat(TextOutputFormat.class);
                FileInputFormat.setInputPaths(prejobConf, new Path(args[0]));
                FileOutputFormat.setOutputPath(prejobConf, new Path("part3_user_to_rank_map"));

                client.setConf(prejobConf);
                try {
                    JobClient.runJob(prejobConf);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                conf.setMapOutputKeyClass(IntWritable.class);
                conf.setMapOutputValueClass(IntWritable.class);

                conf.setMapperClass(UsersPerRatingCountMapper.class);
                conf.setCombinerClass(UsersPerRatingCountMapper.class);
                conf.setReducerClass(UsersPerRatingCountReducer.class);

                conf.setOutputKeyClass(IntWritable.class);
                conf.setOutputValueClass(IntWritable.class);
                FileInputFormat.setInputPaths(conf, new Path("part3_user_to_rank_map"));

                    break;

                case 4:
                conf.setMapOutputKeyClass(Text.class);
                conf.setMapOutputValueClass(IntWritable.class);

                conf.setMapperClass(part4Mapper.class);
                conf.setCombinerClass(part4Mapper.class);
                conf.setReducerClass(part4Reducer.class);

                conf.setOutputKeyClass(Text.class);
                conf.setOutputValueClass(IntWritable.class);
                FileInputFormat.setInputPaths(conf, new Path(args[0]));

                    break;
            }
	

	
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

