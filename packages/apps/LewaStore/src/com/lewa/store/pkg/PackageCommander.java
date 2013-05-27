package com.lewa.store.pkg;

import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class PackageCommander extends Thread {

	public static ArrayList<Command> commands;
	public static Handler handler;
	public static SuCommander su;
	public static Thread t;
	public Context context;

	public PackageCommander(Context paramContext) {
		init_commands(paramContext, null);
	}

	public PackageCommander(Context paramContext, Handler paramHandler) {
		init_commands(paramContext, paramHandler);
	}

	private void init_commands(Context context, Handler h) {
		PackageCommander.handler = h;
		if (PackageCommander.commands == null) {
			PackageCommander.commands = new ArrayList<Command>();
		}
		if (PackageCommander.su == null) {
			try {
				PackageCommander.su = new SuCommander();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		if (PackageCommander.t == null) {
			PackageCommander.t = new Thread(this);
			PackageCommander.t.start();
		}
		try {
			PackageCommander.su = new SuCommander();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void run() {
		try {
			boolean done = false;
			while (PackageCommander.t != null) {
				while (!PackageCommander.su.isReady()) {
					Thread.sleep(100);
				}
				if (!PackageCommander.commands.isEmpty()) {
					done = true;
					Command c = PackageCommander.commands.remove(0);
					PackageCommander.su.exec(c.getCommand());
					while (!PackageCommander.su.isReady()) {
						Thread.sleep(100);
					}
					Message m = Message.obtain();
					if (PackageCommander.handler != null) {
						// Message m = Message.obtain();
						if (PackageCommander.su.isSuccess()) {
							m.obj = c.getTitle() + " OK";
						} else {
							m.obj = c.getTitle() + " FAIL";
						}
						PackageCommander.handler.sendMessage(m);
					}
					if (PackageCommander.handler != null) {
						Log.i("LewaUpdater",
								PackageCommander.handler.toString()
										+ " EXEC "
										+ c.getTitle()
										+ " RESULT "
										+ String.valueOf(PackageCommander.su
												.isSuccess()));
					} else {
						Log.i("LewaUpdater",
								"H=null EXEC "
										+ c.getTitle()
										+ " RESULT "
										+ String.valueOf(PackageCommander.su
												.isSuccess()));
					}
					String output = PackageCommander.su.getOutput();

					if (output != null) {
						output = output.trim().replace("\n", "");
						if (output.length() > 2) {
							m.obj = "output:" + output;
							Log.d("LewaUpdater", output);
						}

					}
					String errors = PackageCommander.su.getErrors();
					if (errors != null) {
						m.obj = "errors:" + errors;
						Log.e("LewaUpdater", errors);
					}

				} else {
					if ((PackageCommander.handler != null) && done) {
						done = false;
						Message m = Message.obtain();
						m.obj = "All done!";
						PackageCommander.handler.sendMessage(m);
					}
				}
				Thread.sleep(1000);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	static class Command {

		String title;
		String command;

		public Command(String title, String command) {
			super();
			this.title = title;
			this.command = command;
		}

		public String getCommand() {
			return command;
		}

		public String getTitle() {
			return title;
		}

		public void setCommand(String command) {
			this.command = command;
		}

		public void setTitle(String title) {
			this.title = title;
		}
	}
	
	public void copy(String commandStr){
		PackageCommander.commands.add(new Command("cp",commandStr));
	}
	
	public void chmod(String commanderStr){
		PackageCommander.commands.add(new Command("chmod",commanderStr));
	}

	public void update(String updateZipPath) {
		PackageCommander.commands.add(new Command("mkdir",
				"mkdir -p /cache/recovery"));

		PackageCommander.commands.add(new Command("echo",
				"echo 'boot-recovery' >> /cache/recovery/command"));
		PackageCommander.commands.add(new Command("echo",
				"echo '--update_package=" + updateZipPath
						+ "' >> /cache/recovery/command"));
		Log.i("LewaUpdater", "updateZipPath=" + updateZipPath);
		PackageCommander.commands.add(new Command("reboot", "reboot recovery"));
	}
}