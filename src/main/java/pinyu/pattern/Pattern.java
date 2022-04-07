package pinyu.pattern;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Pattern {
	protected static AtomicBoolean stopSignal = new AtomicBoolean();
	
	public static void stop() {
		stopSignal.set(true);
	}
}
