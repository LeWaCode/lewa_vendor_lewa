package com.lewa.store.utils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.apache.http.util.EncodingUtils;

import com.lewa.store.pkg.PackageCommander;
import com.lewa.store.pkg.SuCommander;

import android.content.Context;
import android.util.Log;

public class FileHelper {

	private static String TAG = FileHelper.class.getSimpleName();

	private static String ENCODING = "utf-8";

	public static String getContentFromAssets(Context context, String fileName) {
		String result = "";
		InputStream inputStream=null;
		try {
			inputStream=context.getResources().getAssets()
					.open(fileName);
			int lenght = inputStream.available();
			byte[] buffer = new byte[lenght];
			inputStream.read(buffer);
			result = EncodingUtils.getString(buffer, ENCODING);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	// 创建目录
	public static boolean createDir(String destDirName) {
		boolean flag = false;
		File dir = new File(destDirName);
		if (dir.exists()) {
			// System.out.println("创建目录" + destDirName + "失败，目标目录已存在！");
			flag = false;
		}
		if (!destDirName.endsWith(File.separator)) {
			destDirName = destDirName + File.separator;
		}
		// 创建单个目录
		if (dir.mkdirs()) {
			// System.out.println("创建目录" + destDirName + "成功！");
			flag = true;
		} else {
			// Zhu.debug("创建目录"+destDirName, "失败");
			flag = false;
		}
		return flag;
	}

	/**
	 * 以root用户运行
	 * 
	 * @param command
	 * @return
	 */
	public static boolean runRootCommand(String command) {
		Process process = null;
		DataOutputStream os = null;
		try {
			process = Runtime.getRuntime().exec("su");
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes(command + "\n");
			os.writeBytes("exit\n");
			os.flush();
			process.waitFor();
		} catch (Exception e) {
			Log.d("*** DEBUG ***", "Unexpected error - Here is what I know: "
					+ e.getMessage());
			return false;
		} finally {
			try {
				if (os != null) {
					os.close();
				}
				process.destroy();
			} catch (Exception e) {
				// nothing
			}
		}
		return true;
	}

	/**
	 * chmod 777
	 * 
	 * @param absolutePath
	 */
	public static void rootFile(String absolutePath) {
		try {
			SuCommander su = new SuCommander();
			su.exec("chmod 777 " + absolutePath);
			// Runtime.getRuntime().exec("chmod 777 " + absolutePath);
		} catch (RuntimeException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void rootSingleFile(String absolutePath) {
		try {
			SuCommander su = new SuCommander();
			su.exec("chmod 777 " + absolutePath);
			// Runtime.getRuntime().exec("chmod 777 " + absolutePath);
		} catch (RuntimeException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void mount() {
		try {
			SuCommander su = new SuCommander();
			su.exec("busybox mount -o remount,rw /system");
			Log.d(TAG, "mount");
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "mount error==" + e.getMessage());
		}
	}

	public static void umount() {
		try {
			SuCommander su = new SuCommander();
			su.exec("busybox mount -o remount,ro /system");
			Log.d(TAG, "umount");
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "umount error==" + e.getMessage());
		}
	}

	public static String rootRename(String path, String newName) {
		try {
			String newPath = Constants.SD_PATH + newName;
			SuCommander su = new SuCommander();
			String commander = "mv " + path + " " + Constants.SD_PATH + newName;
			Log.d(TAG, "rename commander==" + commander);
			su.exec(commander);
			return newPath;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 
	 * @param path
	 *            localfile
	 * @param newName
	 *            new file name
	 */
	public static String rename(String path, String newName) {
		try {
			String newPath = Constants.SD_PATH + newName;
			File file = new File(path);
			File dest = new File(newPath);
			if (file.renameTo(dest)) {
				Log.e(TAG, "rename success");
				return newPath;
			}
			Log.e(TAG, "rename failed");
			return path;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return "";
	}

	/*
	 * public static void rootCopyFile(Context context,String oldPath, String
	 * dir,String filename){ try { mount(); String
	 * commandStr="cp  "+oldPath+" "+dir+filename; new
	 * PackageCommander(context).copy(commandStr);
	 * Log.i(TAG,"rootCopyFile,commander=="+commandStr); } catch (Exception e) {
	 * e.printStackTrace(); } }
	 */

	public static boolean suCopyFile(Context context, String oldPath,
			String dir, String filename) {
		boolean flag = false;
		try {
			mount();
			SuCommander su = new SuCommander();
			String commandStr = "cp  " + oldPath + " " + dir + filename;
			String output = su.exec_o(commandStr).trim();
			if (output == null || output.equals("")) {
				flag = true;
				Log.e(TAG, "cp output=" + output);
			}
			Log.i(TAG, "suCopyFile,commander==" + commandStr);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flag;
	}

	public static void chmodGeneralFile(String path) {
		try {
			String command = "chmod 644 " + path;
			Log.i(TAG, "command = " + command);
			Runtime runtime = Runtime.getRuntime();
			Process proc = runtime.exec(command);
			Log.d(TAG, "chmod general file");
		} catch (IOException e) {
			Log.i(TAG, "chmod fail!!!!");
			e.printStackTrace();
		}
	}

	public static void chmodSysApp(Context context, String filename) {
		try {
			mount();
			String commandStr = "chmod 644 " + filename;
			new PackageCommander(context).chmod(commandStr);
			Log.i(TAG, "chmodSysApp ok,filename==" + filename);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void chmodFile(Context context, String filename) {
		try {
			mount();
			SuCommander su = new SuCommander();
			String commandStr = "chmod 644 " + filename;
			su.exec(commandStr);
			Log.i(TAG, "chmodFile ok,filename==" + filename);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param oldPath
	 *            旧文件路径
	 * @param dir
	 *            新文件目录
	 * @param filename
	 *            新文件名称
	 */
	public static void copyFile(String oldPath, String dir, String filename) {
		int byteRead = 0;
		File oldfile = new File(oldPath);
		if (oldfile.exists()) {
			InputStream inStream = null;
			FileOutputStream fs = null;
			File newFile = new File(dir, filename);
			if (newFile.exists()) {
				newFile.delete();
				// Log.e(TAG, newFile.toString());
			}
			try {
				inStream = new FileInputStream(oldPath);
				fs = new FileOutputStream(newFile);
				byte[] buffer = new byte[2048];
				while ((byteRead = inStream.read(buffer)) != -1) {
					fs.write(buffer, 0, byteRead);
				}
				fs.flush();
				if (newFile.exists()) {
					rootSingleFile(newFile.getPath());
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (null != fs) {
						fs.close();
					}
					if (null != inStream) {
						inStream.close();
					}
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}
	}

	/**
	 * 创建目录
	 * 
	 * @param folderPath
	 *            如 c:/abc
	 * @return boolean
	 */
	public static void newFolder(String folderPath) {
		File myFilePath = new File(folderPath);
		if (!myFilePath.exists()) {
			myFilePath.mkdir();
		}
	}

	/**
	 * 创建文件
	 * 
	 * @param fileName文件路径及名称
	 *            如c:/test.txt
	 * @param fileContent文件内容
	 * @throws IOException
	 */
	public void newFile(String fileName, String fileContent) throws IOException {
		File myFilePath = new File(fileName);
		if (!myFilePath.exists()) {
			myFilePath.createNewFile();
		}
		FileWriter resultFile = new FileWriter(myFilePath);
		PrintWriter myFile = new PrintWriter(resultFile);
		String strContent = fileContent;
		myFile.println(strContent);
		resultFile.close();
	}

	public static void delFile(final String fileName) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mount();
                SuCommander su = null;
                try {
                    su = new SuCommander();
                    su.exec("rm  " + fileName);
                    Log.e(TAG, "delete filePath=="+fileName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
	}

	public static void rootDelFolder(final String folderPath) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mount();
                SuCommander su = null;
                try {
                    su = new SuCommander();
                    String commandStr = "rm  " + folderPath;
                    su.exec(commandStr);
                    Log.e(TAG, "rootDelFolder(),str==" + commandStr);
                    Log.e(TAG,"rootDelFolder:"+folderPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
	}

	/**
	 * 删除文件夹
	 * 
	 * @param filePathAndName
	 *            文件夹路径及名称 如c:/fqf
	 */
	public static void delFolder(String folderPath) {
		try {
			Log.e(TAG, "delete folderPath==" + folderPath);
			delAllFile(folderPath); // 先删除完里面所有内容
			File myFilePath = new File(folderPath);
			myFilePath.delete(); // 删除空文件夹
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "delete Folder error");
		}

	}

	/**
	 * 删除文件夹里面的所有文件
	 * 
	 * @param folderPath
	 *            文件夹路径 如 c:/abc
	 */
	public static void delAllFile(String folderPath) {
		File file = new File(folderPath);
		if (!file.exists()) {
			return;
		}
		if (!file.isDirectory()) {
			return;
		}

		String[] tempList = file.list();
		Log.e(TAG, "will delete folder file size==" + tempList.length);
		File temp = null;
		if (null != tempList) {
			int length = tempList.length;
			for (int i = 0; i < length; i++) {
				if (folderPath.endsWith(File.separator)) {
					temp = new File(folderPath + tempList[i]);
				} else {
					temp = new File(folderPath + File.separator + tempList[i]);
				}
				if (temp.isFile()) {
					temp.delete();
				}
				if (temp.isDirectory()) {
					delAllFile(folderPath + "/" + tempList[i]);// 先删除文件夹里面的文件
					delFolder(folderPath + "/" + tempList[i]);// 再删除空文件夹
				}
			}
			Log.e(TAG, "delete all folder file success");
		}
	}

	/**
	 * 复制整个文件夹内容
	 * 
	 * @param oldPath
	 *            原文件路径 如：c:/abc
	 * @param newPath
	 *            复制后路径 如：d:/abc/dd
	 * @throws Exception
	 */
	public void copyFolder(String oldPath, String newPath) throws Exception {
		(new File(newPath)).mkdirs(); // 如果文件夹不存在 则建立新文件夹
		File oFile = new File(oldPath);
		String[] file = oFile.list();
		File temp = null;
		int length = file.length;
		for (int i = 0; i < length; i++) {
			if (oldPath.endsWith(File.separator)) {
				temp = new File(oldPath + file[i]);
			} else {
				temp = new File(oldPath + File.separator + file[i]);
			}
			if (temp.isFile()) {
				FileInputStream input = new FileInputStream(temp);
				FileOutputStream output = new FileOutputStream(newPath + "/"
						+ (temp.getName()).toString());
				byte[] b = new byte[1024 * 5];
				int len;
				while ((len = input.read(b)) != -1) {
					output.write(b, 0, len);
				}
				output.flush();
				output.close();
				input.close();
			}
			if (temp.isDirectory()) {// 如果是子文件夹
				copyFolder(oldPath + "/" + file[i], newPath + "/" + file[i]);
			}
		}

	}

	/**
	 * 移动文件到指定目录
	 * 
	 * @param oldPath
	 *            如：c:/freezq.txt
	 * @param newPath
	 *            如：d:/freezq.txt
	 * @throws Exception
	 */
	public void moveFile(String oldPath, String newPath) throws Exception {
		// copyFile(oldPath, newPath);
		// delFile(oldPath);
	}

	/**
	 * 移动文件夹到指定目录
	 * 
	 * @param oldPath
	 *            如：c:/abc
	 * @param newPath
	 *            如：d:/abc
	 * @throws Exception
	 */
	public void moveFolder(String oldPath, String newPath) throws Exception {
		copyFolder(oldPath, newPath);
		delFolder(oldPath);
	}

	/*
	 * public static void main(String[] args) throws Exception { Shi os=new
	 * AccessFile(); //os.newFolder("c:/abc/n");//不能套着文件夹创建
	 * //os.copyFile("c:/abc/1.txt", "c:/abc/n/5.txt");
	 * //os.moveFile("c:/abc/2.txt", "c:/abc/n/1.txt");
	 * //os.moveFolder("c:/abc/n", "c:/abc/m"); os.copyFolder("c:/abc/n",
	 * "c:/"); //os.delFile("c:/abc/1.txt"); //os.delFolder("c:/abc");
	 * //os.newFile("c:\\1.txt", "this is a test!!!"); }
	 */
}
