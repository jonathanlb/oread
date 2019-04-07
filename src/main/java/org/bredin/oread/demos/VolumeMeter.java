package org.bredin.oread.demos;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bredin.oread.*;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.SwingWrapper;

import javax.sound.sampled.LineUnavailableException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Plot a bar graph representing volume/signal-strength changing over time.
 *
 * To poll microphone demo, run with:
 * mvn exec:java -Dexec.mainClass=org.bredin.oread.demos.VolumeMeter
 */
public class VolumeMeter {
	private static Logger log = LogManager.getLogger();

	private static final int numSamples = 100;
	private static final int height = 600;
	private static final int width = 800;
	private static final int MS_SAMPLE_PERIOD = 10;

	private final LinkedList<Double> times;
	private final LinkedList<Double> volumes;
	private final double[] errors; // unused placeholder
	private final CategoryChart chart;

	private SwingWrapper<CategoryChart> sw;
	private Disposable dispose;

	public VolumeMeter() {
		chart = buildChart();
		volumes = new LinkedList<>();
		times = new LinkedList<>();
		errors = new double[numSamples];
		initVolume(chart);
	}

	private CategoryChart buildChart() {
		CategoryChart chart =
			new CategoryChartBuilder()
				.width(width)
				.height(height)
				.title("Volume")
				.xAxisTitle("Time")
				.yAxisTitle("Volume")
				.build();

		chart.getStyler().setYAxisMax(1.1);
		chart.getStyler().setYAxisMin(0.0);
		chart.getStyler().setXAxisTicksVisible(false);
		chart.getStyler().setLegendVisible(false);
		chart.getStyler().setHasAnnotations(false);
		chart.getStyler().setPlotGridLinesVisible(false);

		return chart;
	}

	private void initVolume(CategoryChart chart) {
		for (int i = numSamples; i > 0; --i) {
			// errors.add(0.0);
			volumes.add(0.0);
			times.add(-1.0 * i);
		}
		chart.addSeries("Mic", times, volumes);
	}

	/**
	 * Run with the input sequence.
	 * @param samples the sequence to plot
	 */
	void start(Flowable<SamplePacket> samples) {
		sw = new SwingWrapper<>(chart);
		sw.displayChart();
		dispose = samples.subscribe(this::updateChart);
	}

	/**
	 * Use the default microphone and 10ms sampling period.
	 * @throws LineUnavailableException if we cannot find the default mic.
	 */
	void start() throws LineUnavailableException {
		Flowable<TimePacket> time = TimePacket.clockTime(MS_SAMPLE_PERIOD, TimeUnit.MILLISECONDS);
		Flowable<LPCMPacket> mic = MicSource.mic(time);
		Flowable<SamplePacket> micSamples = LPCMToSamples.LPCMToSamples(mic);
		start(micSamples);
	}

	void stop() {
		dispose.dispose();
	}

	private static double[] toArray(List<Double> list) {
		double[] result = new double[list.size()];
		int i = 0;
		for (Double v : list) {
			result[i++] = v;
		}
		return result;
	}

	/**
	 * Redraw the volume chart given a new sample.
	 * @param p the new sample.
	 */
	private void updateChart(SamplePacket p) {
		double v = 0.0;
		double[] vs = p.getData();
		for (int i = vs.length - 1; i >=0; --i) {
			v = Math.max(v, Math.abs(vs[i]));
		}
		volumes.removeFirst();
		volumes.add(v);

		long t = p.getEnd();
		times.removeFirst();
		times.add((double)t);

		log.debug("Update {} {} ()", t, v, vs.length);

		// CategoryChart requires non-mutating list or array.  At 10 updates/second or
		// faster we're in danger of getting a ConcurrentModificationException in the
		// first second or two of operation.  Copy to fresh arrays...
		double[] timesArray = toArray(times);
		double[] volumesArray = toArray(volumes);
		chart.updateCategorySeries( "Mic", timesArray, volumesArray, errors);
		sw.repaintChart();
	}

	public static void main(String[] args) throws Exception {
		VolumeMeter vm = new VolumeMeter();
		vm.start();
		// Thread.sleep(10000);
		// vm.stop();
	}
}

