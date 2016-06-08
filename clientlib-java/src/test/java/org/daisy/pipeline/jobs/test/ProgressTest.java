package org.daisy.pipeline.jobs.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.daisy.pipeline.client.models.Job;
import org.daisy.pipeline.client.models.Job.Status;
import org.daisy.pipeline.client.models.Message;
import org.junit.Test;

public class ProgressTest {
	
	private int sequence = 0;
	private void addMessage(Job job, Long time, String text) {
		Message m = new Message();
		m.timeStamp = time;
		m.text = text;
		m.level = Message.Level.DEBUG;
		m.sequence = sequence++;
		
		List<Message> messages = job.getMessages();
		if (messages == null) {
			messages = new ArrayList<Message>();
		}
		messages.add(m);
		job.setMessages(messages);
	}
	
	final double delta = 0.01;

	@Test
	public void testProgressForStatesAndNoMessages() {
		
		Job job = new Job();
		
		job.setStatus(null);
		assertEquals(0.0, job.getProgressFrom(), delta);
		assertEquals(100.0, job.getProgressTo(), delta);
		assertEquals(0.0, job.getProgressEstimate(), delta);
		
		job.setStatus(Status.IDLE);
		assertEquals(0.0, job.getProgressFrom(), delta);
		assertEquals(100.0, job.getProgressTo(), delta);
		assertEquals(0.0, job.getProgressEstimate(), delta);
		
		job.setStatus(Status.RUNNING);
		assertEquals(0.0, job.getProgressFrom(), delta);
		assertEquals(100.0, job.getProgressTo(), delta);
		assertEquals(0.0, job.getProgressEstimate(0L), delta);
		
		job.setStatus(Status.DONE);
		assertEquals(0.0, job.getProgressFrom(), delta);
		assertEquals(100.0, job.getProgressTo(), delta);
		assertEquals(100.0, job.getProgressEstimate(0L), delta);
		
		job.setStatus(Status.ERROR);
		assertEquals(0.0, job.getProgressFrom(), delta);
		assertEquals(100.0, job.getProgressTo(), delta);
		assertEquals(100.0, job.getProgressEstimate(0L), delta);
		
		job.setStatus(Status.VALIDATION_FAIL);
		assertEquals(0.0, job.getProgressFrom(), delta);
		assertEquals(100.0, job.getProgressTo(), delta);
		assertEquals(100.0, job.getProgressEstimate(0L), delta);
	}

	@Test
	public void testProgressForASimulatedJob() {
		Job job = new Job();
		
		// job queued
		job.setStatus(Status.IDLE);
		assertEquals(0.0, job.getProgressFrom(), delta);
		assertEquals(100.0, job.getProgressTo(), delta);
		assertEquals(0.0, job.getProgressEstimate(), delta);
		
		job.setStatus(Status.RUNNING);
		addMessage(job, 0L, "[progress 0-100] ...");
		assertEquals("[progress 0-100] ...", job.getMessages().get(job.getMessages().size()-1).text);
		assertEquals("...", job.getMessages().get(job.getMessages().size()-1).getText());
		assertEquals(0.0, job.getProgressFrom(), delta);
		assertEquals(100.0, job.getProgressTo(), delta);
		assertEquals(0.0, job.getProgressEstimate(0L), delta);
		assertEquals(95.02, job.getProgressEstimate(60000L), delta);
		
		addMessage(job, 5000L, "[progress 5-100] ...");
		assertEquals("[progress 5-100] ...", job.getMessages().get(job.getMessages().size()-1).text);
		assertEquals("...", job.getMessages().get(job.getMessages().size()-1).getText());
		assertEquals(5.0, job.getProgressFrom(), delta);
		assertEquals(100.0, job.getProgressTo(), delta);
		assertEquals(5.0, job.getProgressEstimate(5000L), delta);
		
		addMessage(job, 10000L, "[progress 10-20] ...");
		assertEquals(10.0, job.getProgressFrom(), delta);
		assertEquals(20.0, job.getProgressTo(), delta);
		assertEquals(10.0, job.getProgressEstimate(10000L), delta);
		assertEquals(19.5, job.getProgressEstimate(20000L), delta);
		
		addMessage(job, 20000L, "[progress 20-70] ...");
		assertEquals(20.0, job.getProgressFrom(), delta);
		assertEquals(70.0, job.getProgressTo(), delta);
		assertEquals(20.0, job.getProgressEstimate(20000L), delta);
		
		addMessage(job, 70000L, "[progress 70-100] ...");
		assertEquals(70.0, job.getProgressFrom(), delta);
		assertEquals(100.0, job.getProgressTo(), delta);
		assertEquals(70.0, job.getProgressEstimate(70000L), delta);
		assertEquals(98.5, job.getProgressEstimate(100000L), delta);
		
		job.setStatus(Status.DONE);
		assertEquals(100.0, job.getProgressEstimate(), delta);
		job.setStatus(Status.ERROR);
		assertEquals(100.0, job.getProgressEstimate(), delta);
		job.setStatus(Status.VALIDATION_FAIL);
		assertEquals(100.0, job.getProgressEstimate(), delta);
		job.setStatus(null);
		assertEquals(0.0, job.getProgressEstimate(), delta);
	}
	
	@Test
	public void testProgressNamesAndSubProgresses() {
		
		// Step type as name
		
		Job job = new Job();
		job.setStatus(Status.RUNNING);
		addMessage(job, 0L, "[progress 0]");
		
		addMessage(job, 5000L, "[progress 10-30 px:a-to-b.convert] Step type as name");
		assertEquals("Step type as name",job.getMessages().get(job.getMessages().size()-1).getText());
		assertEquals(10.0, job.getProgressFrom(), delta);
		assertEquals(30.0, job.getProgressTo(), delta);
		assertEquals(10.0, job.getProgressEstimate(5000L), delta);
		
		addMessage(job, 10000L, "[progress px:a-to-b.convert 50-100 px:a-to-b.store] Step type as name");
		assertEquals("Step type as name",job.getMessages().get(job.getMessages().size()-1).getText());
		assertEquals(20.0, job.getProgressFrom(), delta);
		assertEquals(30.0, job.getProgressTo(), delta);
		assertEquals(20.0, job.getProgressEstimate(10000L), delta);
		
		addMessage(job, 15000L, "[progress px:a-to-b.store 50-100 px:a-to-b.foo] Step type as name");
		assertEquals("Step type as name",job.getMessages().get(job.getMessages().size()-1).getText());
		assertEquals(25.0, job.getProgressFrom(), delta);
		assertEquals(30.0, job.getProgressTo(), delta);
		assertEquals(25.0, job.getProgressEstimate(15000L), delta);
		
		addMessage(job, 18000L, "[progress wrong-name 50-75] Progress info to be ignored");
		assertEquals("Progress info to be ignored",job.getMessages().get(job.getMessages().size()-1).getText());
		assertEquals(25.0, job.getProgressFrom(), delta); // would be either 27.5 or 50 if progress info were not ignored
		assertEquals(30.0, job.getProgressTo(), delta); // would be either 28.75 or 75 if progress info were not ignored
		assertEquals(29.75, job.getProgressEstimate(18000L), delta); // would be either 27.5 or 50 if progress info were not ignored
		
		// URI as name
		
		job = new Job();
		job.setStatus(Status.RUNNING);
		addMessage(job, 0L, "[progress 0]");
		
		addMessage(job, 5000L, "[progress 10-30 http://example.com/a-to-b.convert.xpl] URI as name");
		assertEquals("URI as name",job.getMessages().get(job.getMessages().size()-1).getText());
		assertEquals(10.0, job.getProgressFrom(), delta);
		assertEquals(30.0, job.getProgressTo(), delta);
		assertEquals(10.0, job.getProgressEstimate(5000L), delta);
		
		addMessage(job, 10000L, "[progress http://example.com/a-to-b.convert.xpl 50-100 http://example.com/a-to-b.store.xsl] URI as name");
		assertEquals("URI as name",job.getMessages().get(job.getMessages().size()-1).getText());
		assertEquals(20.0, job.getProgressFrom(), delta);
		assertEquals(30.0, job.getProgressTo(), delta);
		assertEquals(20.0, job.getProgressEstimate(10000L), delta);
		
		addMessage(job, 15000L, "[progress http://example.com/a-to-b.store.xsl 50-100 http://example.com/a-to-b.foo.xsl] URI as name");
		assertEquals("URI as name",job.getMessages().get(job.getMessages().size()-1).getText());
		assertEquals(25.0, job.getProgressFrom(), delta);
		assertEquals(30.0, job.getProgressTo(), delta);
		assertEquals(25.0, job.getProgressEstimate(15000L), delta);
		
		addMessage(job, 20000L, "[progress http://example.com/a-to-b.store.xsl 75-75 http://example.com/a-to-b.foo.xsl] Progress with from=to");
		assertEquals("Progress with from=to",job.getMessages().get(job.getMessages().size()-1).getText());
		assertEquals(27.5, job.getProgressFrom(), delta);
		assertEquals(27.5, job.getProgressTo(), delta);
		assertEquals(27.5, job.getProgressEstimate(10000L), delta);
		assertEquals(27.5, job.getProgressEstimate(20000L), delta);
		assertEquals(27.5, job.getProgressEstimate(30000L), delta);

	}
	
	@Test
	public void testCumulativeProgress() {
		
		Job job = new Job();
		job.setStatus(Status.RUNNING);
		
		assertEquals(0.0, job.getProgressEstimate(0L), delta);
		assertEquals(0.0, job.getProgressFrom(), delta);
		assertEquals(100.0, job.getProgressTo(), delta);
		
		addMessage(job, 1000L, "[progress 1]");
		assertEquals(0.0, job.getProgressEstimate(1000L), delta);
		assertEquals(0.0, job.getProgressFrom(), delta);
		assertEquals(1.0, job.getProgressTo(), delta);
		
		addMessage(job, 2000L, "[progress 1]");
		assertEquals(1.0, job.getProgressEstimate(2000L), delta);
		assertEquals(1.0, job.getProgressFrom(), delta);
		assertEquals(2.0, job.getProgressTo(), delta);
		
		addMessage(job, 3000L, "[progress 2]");
		assertEquals(2.0, job.getProgressEstimate(3000L), delta);
		assertEquals(2.0, job.getProgressFrom(), delta);
		assertEquals(4.0, job.getProgressTo(), delta);
		
		addMessage(job, 4000L, "[progress 4]");
		assertEquals(4.0, job.getProgressEstimate(4000L), delta);
		assertEquals(7.8, job.getProgressEstimate(7000L), delta);
		assertEquals(4.0, job.getProgressFrom(), delta);
		assertEquals(8.0, job.getProgressTo(), delta);
		
		addMessage(job, 5000L, "[progress 2]");
		assertEquals(8.0, job.getProgressEstimate(5000L), delta);
		assertEquals(8.0, job.getProgressFrom(), delta);
		assertEquals(10.0, job.getProgressTo(), delta);
		
		addMessage(job, 6000L, "[progress 60 ranged-substep]");
		assertEquals(10.0, job.getProgressEstimate(6000L), delta);
		assertEquals(10.0, job.getProgressFrom(), delta);
		assertEquals(70.0, job.getProgressTo(), delta);
		
		addMessage(job, 7000L, "[progress ranged-substep 0-10]");
		assertEquals(10.0, job.getProgressEstimate(7000L), delta);
		assertEquals(10.0, job.getProgressFrom(), delta);
		assertEquals(16.0, job.getProgressTo(), delta);
		
		addMessage(job, 8000L, "[progress ranged-substep 10-50]");
		assertEquals(16.0, job.getProgressEstimate(8000L), delta);
		assertEquals(16.0, job.getProgressFrom(), delta);
		assertEquals(40.0, job.getProgressTo(), delta);
		
		addMessage(job, 9000L, "[progress ranged-substep 50-100 cumulative-substep]");
		assertEquals(40.0, job.getProgressEstimate(9000L), delta);
		assertEquals(40.0, job.getProgressFrom(), delta);
		assertEquals(70.0, job.getProgressTo(), delta);
		
		addMessage(job, 10000L, "[progress cumulative-substep 10]");
		assertEquals(40.0, job.getProgressEstimate(10000L), delta);
		assertEquals(40.0, job.getProgressFrom(), delta);
		assertEquals(43.0, job.getProgressTo(), delta);
		
		addMessage(job, 11000L, "[progress cumulative-substep 30]");
		assertEquals(43.0, job.getProgressEstimate(11000L), delta);
		assertEquals(43.0, job.getProgressFrom(), delta);
		assertEquals(52.0, job.getProgressTo(), delta);
		
		addMessage(job, 12000L, "[progress cumulative-substep 60]");
		assertEquals(52.0, job.getProgressEstimate(12000L), delta);
		assertEquals(52.0, job.getProgressFrom(), delta);
		assertEquals(70.0, job.getProgressTo(), delta);

	}
	
	@Test
	public void testFractionedProgress() {
		
		Job job = new Job();
		job.setStatus(Status.RUNNING);
		
		assertEquals(0.0, job.getProgressEstimate(0L), delta);
		assertEquals(0.0, job.getProgressFrom(), delta);
		assertEquals(100.0, job.getProgressTo(), delta);
		
		addMessage(job, 1000L, "[progress 1/50]");
		assertEquals(0.0, job.getProgressEstimate(1000L), delta);
		assertEquals(0.0, job.getProgressFrom(), delta);
		assertEquals(2.0, job.getProgressTo(), delta);
		
		addMessage(job, 2000L, "[progress 1/50]");
		assertEquals(2.0, job.getProgressEstimate(2000L), delta);
		assertEquals(2.0, job.getProgressFrom(), delta);
		assertEquals(4.0, job.getProgressTo(), delta);
		
		// cumulative combined with fractioned
		addMessage(job, 3000L, "[progress 2]");
		assertEquals(4.0, job.getProgressEstimate(3000L), delta);
		assertEquals(4.0, job.getProgressFrom(), delta);
		assertEquals(6.0, job.getProgressTo(), delta);
		
		addMessage(job, 4000L, "[progress 2/50]");
		assertEquals(6.0, job.getProgressEstimate(4000L), delta);
		assertEquals(9.8, job.getProgressEstimate(6000L), delta);
		assertEquals(6.0, job.getProgressFrom(), delta);
		assertEquals(10.0, job.getProgressTo(), delta);
		
		addMessage(job, 5000L, "[progress 5/50]");
		assertEquals(10.0, job.getProgressEstimate(5000L), delta);
		assertEquals(10.0, job.getProgressFrom(), delta);
		assertEquals(20.0, job.getProgressTo(), delta);
		
		addMessage(job, 6000L, "[progress 25 substep]");
		assertEquals(20.0, job.getProgressEstimate(6000L), delta);
		assertEquals(20.0, job.getProgressFrom(), delta);
		assertEquals(45.0, job.getProgressTo(), delta);
		
		addMessage(job, 7000L, "[progress substep 10]");
		assertEquals(20.0, job.getProgressEstimate(7000L), delta);
		assertEquals(20.0, job.getProgressFrom(), delta);
		assertEquals(22.5, job.getProgressTo(), delta);
		
		addMessage(job, 8000L, "[progress substep 40]");
		assertEquals(22.5, job.getProgressEstimate(8000L), delta);
		assertEquals(22.5, job.getProgressFrom(), delta);
		assertEquals(32.5, job.getProgressTo(), delta);
		
		addMessage(job, 9000L, "[progress substep 50]");
		assertEquals(32.5, job.getProgressEstimate(9000L), delta);
		assertEquals(32.5, job.getProgressFrom(), delta);
		assertEquals(45.0, job.getProgressTo(), delta);

	}
	
	@Test
	public void testMessageDepth() {
		
		Job job = new Job();
		job.setStatus(Status.RUNNING);
		
		addMessage(job, 3500L, "First message without progress");
		addMessage(job, 1000L, "[progress 1/50]");
		addMessage(job, 2000L, "[progress 1/50] Progress message with text");
		addMessage(job, 3000L, "[progress 2]");
		addMessage(job, 3500L, "Message without progress");
		addMessage(job, 4000L, "[progress 2/50]    ");
		addMessage(job, 5000L, "[progress 5/50]");
		addMessage(job, 6000L, "[progress 25 substep]");
		addMessage(job, 3500L, "Another message without progress");
		addMessage(job, 7000L, "[progress substep 0]");
		addMessage(job, 3500L, "Message without progress");
		addMessage(job, 3500L, "Message without progress");
		addMessage(job, 7000L, "[progress foo 0] Message with invalid name");
		addMessage(job, 7000L, "[progress substep 10]");
		addMessage(job, 3500L, "Yet another message without progress");
		addMessage(job, 8000L, "[progress substep 40 subsubstep]");
		addMessage(job, 8000L, "[progress subsubstep 100] Subsubstep");
		addMessage(job, 9000L, "[progress substep 50] Another progress message with text");
		
		assertEquals(true, job.getMessages().get(0).getText().length() > 0);
		assertEquals(false, job.getMessages().get(1).getText().length() > 0);
		assertEquals(true, job.getMessages().get(2).getText().length() > 0);
		assertEquals(false, job.getMessages().get(3).getText().length() > 0);
		assertEquals(true, job.getMessages().get(4).getText().length() > 0);
		assertEquals(false, job.getMessages().get(5).getText().length() > 0);
		assertEquals(false, job.getMessages().get(6).getText().length() > 0);
		assertEquals(false, job.getMessages().get(7).getText().length() > 0);
		assertEquals(true, job.getMessages().get(8).getText().length() > 0);
		assertEquals(false, job.getMessages().get(9).getText().length() > 0);
		assertEquals(true, job.getMessages().get(10).getText().length() > 0);
		assertEquals(true, job.getMessages().get(11).getText().length() > 0);
		assertEquals(true, job.getMessages().get(12).getText().length() > 0);
		assertEquals(false, job.getMessages().get(13).getText().length() > 0);
		assertEquals(true, job.getMessages().get(14).getText().length() > 0);
		assertEquals(false, job.getMessages().get(15).getText().length() > 0);
		assertEquals(true, job.getMessages().get(16).getText().length() > 0);
		assertEquals(true, job.getMessages().get(17).getText().length() > 0);
		
		assertEquals(18, job.getMessages().size());
		assertEquals(1, job.getMessages().get(0).depth.intValue());
		assertEquals(0, job.getMessages().get(1).depth.intValue());
		assertEquals(0, job.getMessages().get(2).depth.intValue());
		assertEquals(0, job.getMessages().get(3).depth.intValue());
		assertEquals(1, job.getMessages().get(4).depth.intValue());
		assertEquals(0, job.getMessages().get(5).depth.intValue());
		assertEquals(0, job.getMessages().get(6).depth.intValue());
		assertEquals(0, job.getMessages().get(7).depth.intValue());
		assertEquals(1, job.getMessages().get(8).depth.intValue());
		assertEquals(1, job.getMessages().get(9).depth.intValue());
		assertEquals(2, job.getMessages().get(10).depth.intValue());
		assertEquals(2, job.getMessages().get(11).depth.intValue());
		assertEquals(2, job.getMessages().get(12).depth.intValue());
		assertEquals(1, job.getMessages().get(13).depth.intValue());
		assertEquals(2, job.getMessages().get(14).depth.intValue());
		assertEquals(1, job.getMessages().get(15).depth.intValue());
		assertEquals(2, job.getMessages().get(16).depth.intValue());
		assertEquals(1, job.getMessages().get(17).depth.intValue());
		
		// filter on depth 0, 1, 2 and 3
		assertEquals(6, job.getMessages(0).size());
		assertEquals(0, job.getMessages(0).get(0).depth.intValue());
		assertEquals(0, job.getMessages(0).get(1).depth.intValue());
		assertEquals(0, job.getMessages(0).get(2).depth.intValue());
		assertEquals(0, job.getMessages(0).get(3).depth.intValue());
		assertEquals(0, job.getMessages(0).get(4).depth.intValue());
		assertEquals(0, job.getMessages(0).get(5).depth.intValue());
		
		assertEquals(13, job.getMessages(1).size());
		assertEquals(1, job.getMessages(1).get(0).depth.intValue());
		assertEquals(0, job.getMessages(1).get(1).depth.intValue());
		assertEquals(0, job.getMessages(1).get(2).depth.intValue());
		assertEquals(0, job.getMessages(1).get(3).depth.intValue());
		assertEquals(1, job.getMessages(1).get(4).depth.intValue());
		assertEquals(0, job.getMessages(1).get(5).depth.intValue());
		assertEquals(0, job.getMessages(1).get(6).depth.intValue());
		assertEquals(0, job.getMessages(1).get(7).depth.intValue());
		assertEquals(1, job.getMessages(1).get(8).depth.intValue());
		assertEquals(1, job.getMessages(1).get(9).depth.intValue());
		assertEquals(1, job.getMessages(1).get(10).depth.intValue());
		assertEquals(1, job.getMessages(1).get(11).depth.intValue());
		assertEquals(1, job.getMessages(1).get(12).depth.intValue());
		
		assertEquals(18, job.getMessages(2).size());
		assertEquals(18, job.getMessages(3).size());
	}
	
}
