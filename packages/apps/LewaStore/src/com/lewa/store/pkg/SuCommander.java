package com.lewa.store.pkg;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Random;


public class SuCommander implements Runnable {

	Random				random;	/* For random id to control execution */
	BufferedInputStream	err;
	private String		errors;	/* Saves error stream output */
	private String		output;	/* Saves error stream output */
	BufferedInputStream	reader;
	private boolean		ready;
	Thread				thread;
	OutputStream		writer;
	private boolean		success;	/* True if execution was successful */

	public SuCommander() throws IOException {
		random = new Random();
		Process p = null;
		try {
//			p = Runtime.getRuntime().exec("su");
			p = Runtime.getRuntime().exec("su0");//make this for lewa(only lewa use)
		} catch (Exception e) {
			e.printStackTrace();
		}
		writer = p.getOutputStream();
		reader = new BufferedInputStream(p.getInputStream());
		err = new BufferedInputStream(p.getErrorStream());
		setReady(true);
	}

	/**
	 * Places command to execution and waits for result
	 * 
	 * @param command
	 * @return true if placed, false if thread was not ready
	 * @throws IOException
	 */
	public boolean exec(String command) throws IOException {
		if (isReady()) {
			writer.write((command).getBytes("ASCII"));
			thread = new Thread(this);
			setReady(false);
			thread.start();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Executes command and blocks till get all output
	 * 
	 * @param command
	 * @return normal output of command (not error stream)
	 */
	public String exec_o(String command) {
		try {
			exec(command);
			while (!ready) {
				Thread.sleep(100);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// do nothing
		}
		return getOutput();
	}

	@Override
	protected void finalize() throws Throwable {
		reader.close();
		writer.close();
		err.close();
		super.finalize();
	}

	/**
	 * @param what
	 *            to wait for (if not there, endless loop will occur)
	 * @return Output till waited string
	 * @throws IOException
	 */
	private String get_output(String what) throws IOException {
		StringBuilder output = new StringBuilder();
		StringBuilder error = new StringBuilder();
		int read;
		do {
			while (reader.available() > 0) {
				read = reader.read();
				output.append((char) read);
			}
			while (err.available() > 0) {
				read = err.read();
				error.append((char) read);
			}
			if ((what != null) && output.toString().contains(what)) {
				output = new StringBuilder(output.toString().replace(what, ""));
				what = null;
			}
			if (what != null) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// do nothing
				}
			}
		} while (what != null);

		errors = error.toString();
		return output.toString();

	}

	/**
	 * @return the errors, null if no errors
	 */
	public String getErrors() {
		if (errors != null) {
			if (errors.length() == 0) {
				return null;
			}
		}
		return errors;
	}

	/**
	 * @return the output
	 */
	public String getOutput() {
		return output;
	}

	/**
	 * @return true if thread is ready for exec
	 */
	public boolean isReady() {
		return ready;
	}

	/**
	 * @return true if last command executed successful
	 */
	public boolean isSuccess() {
		return success;
	}

	/**
	 * @return randomized hash
	 */
	private String make_id() {
		long r1 = random.nextLong();
		long r2 = random.nextLong();
		String hash1 = Long.toHexString(r1);
		String hash2 = Long.toHexString(r2);
		return hash1 + hash2;

	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		setOutput("");
		String id = make_id();
		String success = make_id();
		try {
			writer.write((" && echo " + success + " \n \n echo " + id + "\n")
					.getBytes("ASCII"));
			String output = get_output(id);
			if (output.toString().contains(success)) {
				output = new StringBuilder(output.toString().replace(success,
						"")).toString();
				setSuccess(true);
			} else {
				setSuccess(false);
			}
			setOutput(output);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		setReady(true);
	}

	/**
	 * @param output
	 *            the output to set
	 */
	private void setOutput(String output) {
		this.output = output;
	}

	/**
	 * @param ready
	 *            the ready to set
	 */
	private void setReady(boolean ready) {
		this.ready = ready;
	}

	private void setSuccess(boolean success) {
		this.success = success;
	}
}
