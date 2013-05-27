package com.lewatek.swapper;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

public class SwapperCommands extends Thread {
    static class command {

        String title;
        String command;

        public command(String title, String command) {
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

    private static final String TAG = "Swapper";

    public static final boolean DBUG = true;

    public final static String SDCARD_SWAPFILE = "/sdcard/swap.img";
    public final static String SYSTEM_SWAPFILE = "/system/swap.img";
    public final static String CATPROCSWAPFILE = "/proc/swaps";

    public final static int SYSTEMFILE_METHOD = 3;
    public final static int PARTITION_METHOD  = 2;
    public final static int SDCARDFILE_METHOD = 1;
    
    public final static int SYSTEMFILE_CLOSE_METHOD = -2;
    public final static int SDCARDFILE_CLOSE_METHOD = -3;
    public final static int PARTITION_CLOSE_METHOD  = -4;
    
    public final static int INVALID_METHOD = -1;

    public final static String BLOCK_DIR = "/dev/block/loop7";
    public final static String SYSTEM_DIR = "/system/";

    private final static String busybox = "busybox";

    public final static int MSG_OPDONE = 1;
    public final static int MSG_OPFAIL = 2;

    public final static int MSG_NOSWAPON = 10;
    public final static int MSG_NOPARTITION = 11;
    public final static int MSG_NOSDCARD = 12;
    public final static int MSG_SDCARDNOSPACE = 13;
    public final static int MSG_NOSYSTEMFILE = 14;

    private final static int SD_SWAPPINESS = 60;
    private final static int FLASH_SWAPPINESS = 99;

    protected static SuCommander su;
    private static ArrayList<command> commands;
    private static Thread t;

    private static Handler handler = null;
    private static boolean done = true;
    private static Properties properties;

    private Context mContext;

    public SwapperCommands(Context c) {
        init_commands(c, (Handler) null);
    }

    public SwapperCommands(Context c, Handler h) {
        init_commands(c, h);
    }

    private void init_commands(Context c, Handler h) {
        if (null != h)
            handler = h;

        Log.i(TAG, "handler = " + handler);

        if (SwapperCommands.commands == null) {
            SwapperCommands.commands = new ArrayList<command>();
        }
        if (SwapperCommands.su == null) {
            try {
                SwapperCommands.su = new SuCommander();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (SwapperCommands.t == null) {
            SwapperCommands.t = new Thread(this);
            SwapperCommands.t.start();
        }
    }

    public void setHandler(Handler _handler) {
        handler = _handler;
        if (SwapperCommands.DBUG) {
            Log.i(TAG, "setHandler handler = " + handler);
        }
    }

    public boolean isRunning() {
        if (SwapperCommands.commands == null) {
            return false;
        }
        return !SwapperCommands.commands.isEmpty();
    }

    @Override
    public void run() {
        try {
            boolean done = true;
            while (SwapperCommands.t != null) {
                while (!SwapperCommands.su.isReady()) {
                    Thread.sleep(100);
                }

                if (!SwapperCommands.commands.isEmpty()) {
                    done = false;

                    // excute command
                    command c = SwapperCommands.commands.remove(0);
                    SwapperCommands.su.exec(c.getCommand());
                    while (!SwapperCommands.su.isReady()) {
                        Thread.sleep(100);
                    }
                    Log.i(TAG, "command = " + c.getCommand());
                    // check result
                    if (handler != null) {
                        Log.i(TAG, handler.toString() + " EXEC " + c.getTitle() + " RESULT "
                                + String.valueOf(SwapperCommands.su.isSuccess()));
                    } else {
                        Log.i(TAG,
                                "H=null EXEC " + c.getTitle() + " RESULT "
                                        + String.valueOf(SwapperCommands.su.isSuccess()));
                    }

                    String output = SwapperCommands.su.getOutput();

                    if (output != null) {
                        output = output.trim().replace("\n", "");
                        if (output.length() > 2) {
                            Log.d(TAG, "output: " + output);
                        }
                    }
                    String errors = SwapperCommands.su.getErrors();
                    if (errors != null) {
                        Log.e(TAG, "error: "+ errors);
                    }
                } else {
                    if ((handler != null) && done == false) {
                        Message m = Message.obtain();
                        m.what = MSG_OPDONE;
                        handler.sendMessage(m);
                    }

                    if (!done) {
                        done = true;
                        Log.i(TAG, "cmd queue done");
                    }
                }
                Thread.sleep(500);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void swapOn(boolean bBoot, int method, int swapSize) {
        File swapFile = null;
        // which mode
        switch (method) {
            case SDCARDFILE_METHOD:// fileModeOff
                Log.i(TAG, "SDCARDFILE_METHOD");
                swapFile = new File(SDCARD_SWAPFILE);
                if (swapFile.exists()) {
                    if (swapFile.length() == swapSize * 1024 * 1024) {
                        fileSwapOn(false, swapSize);
                    } else {
                        fileSwapOff(false);
                        fileSwapOn(true, swapSize);
                    }
                } else {
                    fileSwapOn(true, swapSize);
                }
                break;
            case PARTITION_METHOD:// partitionSwapOff()
                Log.i(TAG, "PARTITION_METHOD");
                swapFile = new File(SwapperCommands.getSDSwapBlock());
                if (swapFile.exists()) {
                    partitionSwapOn();
                }
                break;
            case SYSTEMFILE_METHOD:// systemSwapOff()
                Log.i(TAG, "SYSTEMFILE_METHOD");
                swapSize = 32;
                swapFile = new File(SYSTEM_SWAPFILE);
                if (swapFile.exists()) {
                    if (swapFile.length() == swapSize * 1024 * 1024) {
                        systemSwapOn(false, swapSize);
                    } else {
                        systemSwapOff();
                        systemSwapOn(true, swapSize);
                    }
                } else {
                    systemSwapOn(true, swapSize);
                }
                break;
            default:
                break;
        }
        swapFile = null;
    }

    public void swapOff(int method, boolean amount) {
        File swapFile = null;
        // which mode
        switch (method) {
            case SDCARDFILE_METHOD:// fileModeOff
                swapFile = new File(SDCARD_SWAPFILE);
                if (swapFile.exists()) {
                    fileSwapOff(amount);
                }
                // fileSwapOff(amount);
                break;
            case PARTITION_METHOD:// partitionSwapOff()
                swapFile = new File(SwapperCommands.getSDSwapBlock());
                if (swapFile.exists()) {
                    partitionSwapOff();
                }
                break;
            case SYSTEMFILE_METHOD:// systemSwapOff()
                swapFile = new File(SYSTEM_SWAPFILE);
                if (swapFile.exists()) {
                    systemSwapOff();
                }
                break;
            case INVALID_METHOD:
                // nothing
                break;
            default:
                break;
        }

        swapFile = null;
    }

    /**
     * swapPlace= /sdcard/ file mode on
     * 
     */
    private void fileSwapOn(boolean bReCreate, int swapSize) {
        // create swap file
        if (bReCreate) {
            SwapperCommands.commands.add(new command("", "dd if=/dev/zero of=" + SDCARD_SWAPFILE
                    + " bs=1048576 count=" + swapSize));
        }

        SwapperCommands.commands.add(new command("", busybox + " mkswap " + SDCARD_SWAPFILE));

        SwapperCommands.commands.add(new command("", busybox + " chmod 704 " + SDCARD_SWAPFILE));

        SwapperCommands.commands.add(new command("", busybox + " swapon " + SDCARD_SWAPFILE));

        // TODO:
        String swappiness = SystemProperties.get("ro.lewa.swapper.sd_swappiness");
        swappiness = swappiness.equals("") ? "" + SD_SWAPPINESS : swappiness;
        setSwappiness(Integer.parseInt(swappiness));

    }

    /**
     * swapPlace= /sdcard/ file mode off
     * 
     */
    private void fileSwapOff(boolean amount) {
        try {
            if (amount) {
                SwapperCommands.su.exec(busybox + " swapoff " + SDCARD_SWAPFILE);
            } else {
                SwapperCommands.commands.add(new command("", busybox + " swapoff "
                        + SDCARD_SWAPFILE));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!amount) {
            SwapperCommands.commands.add(new command("", busybox + " rm " + SDCARD_SWAPFILE));
        }
    }

    /**
     * swapPartPlace=dev/block/mmcblk0p3 partition mode on
     * 
     */
    private void partitionSwapOn() {
        SwapperCommands.commands.add(new command("", busybox + " mkswap " + getSDSwapBlock()));
        SwapperCommands.commands.add(new command("", busybox + " swapon " + getSDSwapBlock()));

        // TODO:
        String swappiness = SystemProperties.get("ro.lewa.swapper.sd_swappiness");
        swappiness = swappiness.equals("") ? SDCARD_SWAPFILE : swappiness;
        setSwappiness(Integer.parseInt(swappiness));
    }

    /**
     * partition mode off
     */
    private void partitionSwapOff() {
        // close partitioin'swap mode
        SwapperCommands.commands.add(new command("", busybox + " swapoff " + getSDSwapBlock()));
    }

    /**
     * system mode on
     */
    private void systemSwapOn(boolean bReCreate, int swapSize) {
        // create swap file
        SwapperCommands.commands.add(new command("", busybox + " mount -o remount,rw /system"));

        if (bReCreate) {
            SwapperCommands.commands.add(new command("", "dd if=/dev/zero of=" + SYSTEM_SWAPFILE
                    + " bs=1048576 count=" + swapSize));
        }

        SwapperCommands.commands.add(new command("", busybox + " chmod 704 " + SYSTEM_SWAPFILE));

        // open system mode
        SwapperCommands.commands.add(new command("", busybox + " losetup " + BLOCK_DIR + " "
                + SYSTEM_SWAPFILE));
        // ...
        SwapperCommands.commands.add(new command("mkswap", busybox + " mkswap " + BLOCK_DIR));
        // open system mode
        SwapperCommands.commands.add(new command("", busybox + " swapon " + BLOCK_DIR));

        // XXX: any problem
        SwapperCommands.commands.add(new command("", busybox + " mount -o remount,ro /system"));

        // TODO:
        String swappiness = SystemProperties.get("ro.lewa.swapper.flash_swappiness");
        swappiness = swappiness.equals("") ? "" + FLASH_SWAPPINESS : swappiness;
        setSwappiness(Integer.parseInt(swappiness));
    }

    private void setSwappiness(int swappiness) {
        SwapperCommands.commands.add(new command("Setting swappiness", "echo " + swappiness
                + " > /proc/sys/vm/swappiness"));
    }

    /**
     * system mode off
     */
    private void systemSwapOff() {
        // XXX: any problem?
        SwapperCommands.commands.add(new command("", busybox
                + new String(" mount -o remount,rw \\/system\\/")));

        // changing permission
        SwapperCommands.commands
                .add(new command("", busybox + " mount -o remount,rw \\/system\\/"));
        // close system mode
        SwapperCommands.commands.add(new command("", busybox + " swapoff " + BLOCK_DIR));
        // delete system swap file
        SwapperCommands.commands.add(new command("", busybox + " rm " + SYSTEM_SWAPFILE));
    }

    /**
     * 
     * @return xx MB
     */
    protected static long readSystemAvailSize() {
        File root = Environment.getRootDirectory();
        StatFs sf = new StatFs(root.getPath());
        long blockSize = sf.getBlockSize();
        long blockCount = sf.getBlockCount();
        long availCount = sf.getAvailableBlocks();
        long freeCount = sf.getFreeBlocks();

        Log.d("Swapper", "block size:" + blockSize + ",block avail:" + blockCount + ",total size:"
                + blockSize * blockCount / 1024 + "KB");
        Log.d("Swapper", "block num:" + availCount + ",avail size:" + availCount * blockSize / 1024
                + "KB" + "Free size:" + freeCount);

        return (availCount * blockSize) / (1024 * 1024);
    }

    protected static long readSdCardAvailSize() {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();

        return (availableBlocks * blockSize) / (1024 * 1024);
    }

    protected static int readHowSwap(Context context, SharedPreferences settings) {
        int howswap = Integer.parseInt(settings.getString("howswap", ""
                + SwapperCommands.SDCARDFILE_METHOD));
        if (howswap > SwapperCommands.PARTITION_METHOD) {
            howswap = SwapperCommands.SDCARDFILE_METHOD;
            Editor editor = settings.edit();
            editor.putString("howswap", "" + howswap);
            editor.commit();
        }

        Settings.System.putInt(context.getContentResolver(), Settings.System.SWAPPER_HOWSWAP,
                howswap);

        return howswap;
    }

    protected static int readSwapSize(Context context, SharedPreferences settings, int howSwap) {
        int swapSize = 32;
        if (howSwap == SDCARDFILE_METHOD) {
            swapSize = Integer.parseInt(settings.getString("swapSize", "32"));
        }
        Settings.System.putInt(context.getContentResolver(), Settings.System.SWAPPER_HOWSWAPSIZE,
                swapSize);
        return swapSize;
    }

    protected static String getSDSwapBlock() {
        String partPath = SystemProperties.get("ro.lewa.swapper.part_path");
        if (android.text.TextUtils.isEmpty(partPath)) {
            partPath = "/dev/block/mmcblk0p3";
        }
        return partPath;
    }

    protected static boolean existSDcard() {
        if (android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment
                .getExternalStorageState())) {
            return true;
        }
        return false;
    }

    public static boolean getSwapFileState(int method) {
        boolean exists = false;
        File swapFile = null;
        switch (method) {
            case SDCARDFILE_METHOD:// fileModeOff
                swapFile = new File(SDCARD_SWAPFILE);
                break;
            case PARTITION_METHOD:// partitionSwapOff()
                swapFile = new File(SwapperCommands.getSDSwapBlock());
                break;
            case SYSTEMFILE_METHOD:// systemSwapOff()
                swapFile = new File(SYSTEM_SWAPFILE);
                break;
            case INVALID_METHOD:
                // nothing
                break;
            default:
                break;
        }

        if (null != swapFile) {
            exists = swapFile.exists();
        }
        swapFile = null;
        return exists;
    }

    public static boolean sdcardIsExists() {
        return (android.os.Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED));
    }
}
