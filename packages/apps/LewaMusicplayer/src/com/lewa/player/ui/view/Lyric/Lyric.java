/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.player.ui.view.Lyric;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ��ʾһ�׸�ĸ�ʶ���,��������ĳ�ַ�ʽ�����Լ�
 * 
 * @author hadeslee
 */
public class Lyric implements Serializable {

	private static final long serialVersionUID = 20071125L;
	private String HOME = "/sdcard/UmilePlayer/music/";
	private static Logger log = Logger.getLogger(Lyric.class.getName());
	private int width;// ��ʾ��ʵ���ʾ����Ŀ��
	private int height;// ��ʾ��ʵ���ʾ����ĸ߶�
	private long time;// ��ʾ��ǰ��ʱ���Ƕ����ˡ��Ժ���Ϊ��λ
	private long tempTime;// ��ʾһ����ʱ��ʱ��,�����϶���ʱ��,ȷ��Ӧ�õ�����
	public List<Sentence> list = new ArrayList<Sentence>();// ����װ�������еľ���
	private boolean isMoving;// �Ƿ����ڱ��϶�
	private int currentIndex;// ��ǰ������ʾ�ĸ�ʵ��±�
	private boolean initDone;// �Ƿ��ʼ�������
	private transient PlayListItem info;// �й������׸����Ϣ
	private transient File file;// �ø���������ļ�
	private boolean enabled = true;// �Ƿ������˸ö���,Ĭ�������õ�
	private long during = Integer.MAX_VALUE;// ���׸�ĳ���
	private int offset;// ���׸��ƫ����
	private long mTotalTime;
	// ���ڻ����һ��������ʽ����
	private static final Pattern pattern = Pattern
			.compile("(?<=\\[).*?(?=\\])");

	/**
	 * ��ID3V1��ǩ���ֽں͸�������ʼ����� ��ʽ��Զ��ڱ��ػ���������������صĸ�ʲ���������
	 * ����������Ӳ����Ϊuser.home�ļ��������Lyrics�ļ��� �Ժ��Ϊ�����ֶ�����.
	 * 
	 * @param songName
	 *            ����
	 * @param data
	 *            ID3V1������
	 */
	public Lyric(final PlayListItem info) {
		this.offset = info.getOffset();
		this.info = info;
		// this.during = info.getDuration();
		this.file = info.getLyricFile();
		log.info("�������ĸ�����:" + info.toString());
		// ֻҪ�й������˵ģ��Ͳ���������ֱ���þ�����
		if (file != null && file.exists()) {
			log.log(Level.INFO, "�������ˣ�ֱ�ӹ������ĸ���ǣ�" + file);
			init(file);
			initDone = true;
			return;
		} else {
			// �������һ���߳�ȥ���ˣ����Ǳ����ң�Ȼ��������������
			new Thread() {

				public void run() {
					doInit(info);
					initDone = true;
				}
			}.start();
		}

	}

	/**
	 * ��ȡĳ��ָ���ĸ���ļ�,������캯��һ������ �ϷŸ���ļ�����ʴ���ʱ���õ�,�Ϸ��Ժ�,�����Զ�����
	 * 
	 * @param file
	 *            ����ļ�
	 * @param info
	 *            ������Ϣ
	 */
	public Lyric(File file, PlayListItem info, long totalTime) {
		System.out.println(" Lyric file" + file);
		this.offset = info.getOffset();
		this.file = file;
		this.info = info;
		this.mTotalTime = totalTime;
		init(file);
		initDone = true;
	}

	/**
	 * ���ݸ�����ݺͲ������һ�� ��ʶ���
	 * 
	 * @param lyric
	 *            �������
	 * @param info
	 *            ������
	 */
	public Lyric(String lyric, PlayListItem info) {
		this.offset = info.getOffset();
		this.info = info;
		this.init(lyric);
		initDone = true;
	}

	private void doInit(PlayListItem info) {
		init(info);

		Sentence temp = null;
		// ���ʱ���Ҫȥ����������
		if (list.size() == 1) {
			temp = list.remove(0);
			String lyric = "";
			if (lyric != null) {
				init(lyric);
				// saveLyric(lyric, info);
			} else {// �������Ҳû���ҵ�,��Ҫ�ӻ�ȥ��

			}
			list.add(temp);
		}
	}

	/**
	 * �����ص��ĸ�ʱ�������,����´���ȥ��
	 * 
	 * @param lyric
	 *            �������
	 * @param info
	 *            �����Ϣ
	 */
	private void saveLyric(String lyric, PlayListItem info) {
		try {
			// ������ֲ�Ϊ��,���Ը�����+������Ϊ������
			String name = info.getFormattedName() + ".lrc";
			File dir = new File(HOME, "Lyrics" + File.separator);
			// File dir = Config.getConfig().getSaveLyricDir();
			dir.mkdirs();
			file = new File(dir, name);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file), "GBK"));
			bw.write(lyric);
			bw.close();
			info.setLyricFile(file);
			log.info("�������,������:" + file);
		} catch (Exception exe) {
			log.log(Level.SEVERE, "�����ʳ���", exe);
		}
	}

	/**
	 * ���ô˸���Ƿ�������,����Ͳ�����
	 * 
	 * @param b
	 *            �Ƿ�����
	 */
	public void setEnabled(boolean b) {
		this.enabled = b;
	}

	/**
	 * �õ��˸�ʱ���ĵط�
	 * 
	 * @return �ļ�
	 */
	public File getLyricFile() {
		return file;
	}

	/**
	 * ���������ʱ��,������ͳһ����� ���߸��ͳһ������,Ϊ��˵��Ҫ��,Ϊ��˵��Ҫ��
	 * 
	 * @param time
	 *            Ҫ����ʱ��,��λ�Ǻ���
	 */
	public void adjustTime(int time) {
		// �����ֻ��һ����ʾ��,�Ǿ�˵��û��ʲôЧ�Ե�������,ֱ�ӷ���
		if (list.size() == 1) {
			return;
		}
		offset += time;
		info.setOffset(offset);
	}

	/**
	 * ����һ���ļ���,��һ����������Ϣ �ӱ����ѵ���ƥ��ĸ��
	 * 
	 * @param dir
	 *            Ŀ¼
	 * @param info
	 *            ������Ϣ
	 * @return ����ļ�
	 */
	private File getMathedLyricFile(File dir, PlayListItem info) {
		File matched = null;// �Ѿ�ƥ����ļ�
		File[] fs = dir.listFiles(new FileFilter() {

			public boolean accept(File pathname) {
				return pathname.getName().toLowerCase().endsWith(".lrc");
			}
		});
		for (File f : fs) {
			// ȫ��ƥ����߲���ƥ�䶼��
			if (matchAll(info, f) || matchSongName(info, f)) {
				matched = f;
				break;
			}
		}
		return matched;
	}

	/**
	 * ���ݸ����Ϣȥ��ʼ��,���ʱ�� �����ڱ����ҵ�����ļ�,Ҳ����Ҫȥ������������
	 * 
	 * @param info
	 *            ������Ϣ
	 */
	private void init(PlayListItem info) {
		File matched = null;
		// �õ�������Ϣ��,������HOME�ļ���
		// ����������ڵĻ�,�ǽ�һ��Ŀ¼,Ȼ��ֱ���˳�������

		File dir = new File(HOME, "Lyrics" + File.separator);
		if (!dir.exists()) {
			dir.mkdirs();
			// }
			matched = getMathedLyricFile(dir, info);
		}
		log.info("�ҵ�����:" + matched);
		if (matched != null && matched.exists()) {
			info.setLyricFile(matched);
			file = matched;
			init(matched);
		} else {
			init("");
		}
	}

	/**
	 * �����ļ�����ʼ��
	 * 
	 * @param file
	 *            �ļ�
	 */
	private void init(File file) {
		BufferedReader br = null;
		try {
            InputStream ios = new java.io.FileInputStream(file);
            byte[] b = new byte[3];
            ios.read(b);
            ios.close();
            if (b[0] == -17 && b[1] == -69 && b[2] == -65) {
                br = new BufferedReader(new InputStreamReader(
                        new FileInputStream(file), "UTF-8"));
            } else {
                br = new BufferedReader(new InputStreamReader(
                        new FileInputStream(file), "GBK"));
            }
                
			StringBuilder sb = new StringBuilder();
			String temp = null;
			while ((temp = br.readLine()) != null) {

				sb.append(temp).append("\n");
			}
			init(sb.toString());
		} catch (Exception ex) {
			Logger.getLogger(Lyric.class.getName()).log(Level.SEVERE, null, ex);

		} finally {
			try {
				br.close();
			} catch (Exception ex) {
				Logger.getLogger(Lyric.class.getName()).log(Level.SEVERE, null,
						ex);
			}
		}
	}

	/**
	 * �Ƿ���ȫƥ��,��ȫƥ����ֱָ�Ӷ�Ӧ��ID3V1�ı�ǩ, ���һ��,����ȫƥ����,��ȫƥ���LRC���ļ���ʽ��: ��ľ - ��һ�ְ��з���.lrc
	 * 
	 * @param info
	 *            ������Ϣ
	 * @param file
	 *            ��ѡ�ļ�
	 * @return �Ƿ�ϸ�
	 */
	private boolean matchAll(PlayListItem info, File file) {
		String name = info.getFormattedName();
		String fn = file.getName()
				.substring(0, file.getName().lastIndexOf("."));
		if (name.equals(fn)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * �Ƿ�ƥ���˸�����
	 * 
	 * @param info
	 *            ������Ϣ
	 * @param file
	 *            ��ѡ�ļ�
	 * @return �Ƿ�ϸ�
	 */
	private boolean matchSongName(PlayListItem info, File file) {
		String name = info.getFormattedName();
		String rn = file.getName()
				.substring(0, file.getName().lastIndexOf("."));
		if (name.equalsIgnoreCase(rn) || info.getTitle().equalsIgnoreCase(rn)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * ����Ҫ��һ�������������ݶ����ĸ������ ���г�ʼ��������Ѹ��һ��һ��ֿ��������ʱ��
	 * 
	 * @param content
	 *            �������
	 */
	private void init(String content) {
		// �����ʵ�����Ϊ��,�����Ͳ���ִ����
		// ֱ����ʾ�������Ϳ�����
		if (content == null || content.trim().equals("")) {
			list.add(new Sentence(info.getFormattedName(), Integer.MIN_VALUE,
					Integer.MAX_VALUE));
			return;
		}
		try {
			BufferedReader br = new BufferedReader(new StringReader(content));
			String temp = null;
			while ((temp = br.readLine()) != null) {
				parseLine(temp.trim());
			}
			br.close();
			// �������Ժ��������
			Collections.sort(list, new Comparator<Sentence>() {

				public int compare(Sentence o1, Sentence o2) {
					return (int) (o1.getFromTime() - o2.getFromTime());
				}
			});
			// �����һ���ʵ���ʼ���,������ô��,���ϸ�����Ϊ��һ����,��������
			// ��βΪ������һ���ʵĿ�ʼ
			if (list.size() == 0) {
				list.add(new Sentence(info.getFormattedName(), 0,
						Integer.MAX_VALUE));
				return;
			} else {
				Sentence first = list.get(0);
				list.add(
						0,
						new Sentence(info.getFormattedName(), 0, first
								.getFromTime()));
			}

			int size = list.size();
			for (int i = 0; i < size; i++) {
				Sentence next = null;
				if (i + 1 < size) {
					next = list.get(i + 1);
				}
				Sentence now = list.get(i);
				if (next != null) {
					now.setToTime(next.getFromTime() - 1);
				}
			}
			// �������û����ô��,�Ǿ�ֻ��ʾһ�������
			if (list.size() == 1) {
				list.get(0).setToTime(Integer.MAX_VALUE);
			} else {
				Sentence last = list.get(list.size() - 1);
//				last.setToTime(info == null ? Integer.MAX_VALUE : info
//						.getLength() * 1000 + 1000);
				last.setToTime(mTotalTime);
			}
		} catch (Exception ex) {
			Logger.getLogger(Lyric.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * �����������ƫ����
	 * 
	 * @param str
	 *            �������ݵ��ַ���
	 * @return ƫ���������������������򷵻���������
	 */
	private int parseOffset(String str) {
		String[] ss = str.split("\\:");
		if (ss.length == 2) {
			if (ss[0].equalsIgnoreCase("offset")) {
				int os = Integer.parseInt(ss[1]);
				System.err.println("�����ƫ������" + os);
				return os;
			} else {
				return Integer.MAX_VALUE;
			}
		} else {
			return Integer.MAX_VALUE;
		}
	}

	/**
	 * ������һ�е����ݣ����������� �Լ���ǩ�������������ɸ�Sentence���� �������е�ʱ���ǩ�ֲ�����һ��ʱ��ҲҪ�ܷ������� ���Ը�����һЩʵ��
	 * 20080824����
	 * 
	 * @param line
	 *            ��һ��
	 */
	private void parseLine(String line) {
		if (line.equals("")) {
			return;
		}
		Matcher matcher = pattern.matcher(line);
		List<String> temp = new ArrayList<String>();
		int lastIndex = -1;// ���һ��ʱ���ǩ���±�
		int lastLength = -1;// ���һ��ʱ���ǩ�ĳ���
		while (matcher.find()) {
			String s = matcher.group();
			int index = line.indexOf("[" + s + "]");
			if (lastIndex != -1 && index - lastIndex > lastLength + 2) {
				// ��������ϴεĴ�С�����м���˱������������
				// ���ʱ���Ҫ�ֶ���
				String content = line.substring(lastIndex + lastLength + 2,
						index);
				for (String str : temp) {
					long t = parseTime(str);
					if (t != -1) {
						System.out.println("content = " + content);
						System.out.println("t = " + t);
						list.add(new Sentence(content, t));
					}
				}
				temp.clear();
			}
			temp.add(s);
			lastIndex = index;
			lastLength = s.length();
		}
		// ����б�Ϊ�գ����ʾ����û�з������κα�ǩ
		if (temp.isEmpty()) {
			return;
		}
		try {
			int length = lastLength + 2 + lastIndex;
			String content = line.substring(length > line.length() ? line
					.length() : length);
			// if (Config.getConfig().isCutBlankChars()) {
			// content = content.trim();
			// }
			// ���Ѿ�����ƫ������ʱ�򣬾Ͳ��ٷ�����
			if (content.equals("") && offset == 0) {
				for (String s : temp) {
					int of = parseOffset(s);
					if (of != Integer.MAX_VALUE) {
						offset = of;
						info.setOffset(offset);
						break;// ֻ����һ��
					}
				}
				return;
			}
			for (String s : temp) {
				long t = parseTime(s);
				if (t != -1) {
					list.add(new Sentence(content, t));
					System.out.println("content = " + content);
					System.out.println("t = " + t);
				}
			}
		} catch (Exception exe) {
		}
	}

	/**
	 * ����00:00.00�������ַ���ת���� ��������ʱ�䣬���� 01:10.34����һ���Ӽ���10���ټ���340���� Ҳ���Ƿ���70340����
	 * 
	 * @param time
	 *            �ַ�����ʱ��
	 * @return ��ʱ���ʾ�ĺ���
	 */
	private long parseTime(String time) {
		String[] ss = time.split("\\:|\\.");
		// ��� ����λ�Ժ󣬾ͷǷ���
		if (ss.length < 2) {
			return -1;
		} else if (ss.length == 2) {// ���������λ���������
			try {
				// �ȿ���û��һ���Ǽ�¼������ƫ������
				if (offset == 0 && ss[0].equalsIgnoreCase("offset")) {
					offset = Integer.parseInt(ss[1]);
					info.setOffset(offset);
					System.err.println("�����ƫ������" + offset);
					return -1;
				}
				int min = Integer.parseInt(ss[0]);
				int sec = Integer.parseInt(ss[1]);
				if (min < 0 || sec < 0 || sec >= 60) {
					throw new RuntimeException("���ֲ��Ϸ�!");
				}
				// System.out.println("time" + (min * 60 + sec) * 1000L);
				return (min * 60 + sec) * 1000L;
			} catch (Exception exe) {
				return -1;
			}
		} else if (ss.length == 3) {// ���������λ��������룬ʮ����
			try {
				int min = Integer.parseInt(ss[0]);
				int sec = Integer.parseInt(ss[1]);
				int mm = Integer.parseInt(ss[2]);
				if (min < 0 || sec < 0 || sec >= 60 || mm < 0 || mm > 99) {
					throw new RuntimeException("���ֲ��Ϸ�!");
				}
				// System.out.println("time" + (min * 60 + sec) * 1000L + mm *
				// 10);
				return (min * 60 + sec) * 1000L + mm * 10;
			} catch (Exception exe) {
				return -1;
			}
		} else {// ����Ҳ�Ƿ�
			return -1;
		}
	}

	/**
	 * ��������ʾ����ĸ߶�
	 * 
	 * @param height
	 *            �߶�
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * ��������ʾ����Ŀ��
	 * 
	 * @param width
	 *            ���
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * ����ʱ��,���õ�ʱ��Ҫ�������ƫ��ʱ������
	 * 
	 * @param time
	 *            ʱ��
	 */
	public void setTime(long time) {
		if (!isMoving) {
			tempTime = this.time = time + offset;
		}
	}

	/**
	 * �õ��Ƿ��ʼ�������
	 * 
	 * @return �Ƿ����
	 */
	public boolean isInitDone() {
		return initDone;
	}

	/**
	 * �õ���ǰ���ڲ��ŵ���һ����±� �������Ҳ�������Ϊ�ͷҪ��һ�� �Լ��ľ��� �����Լ����Ժ�Ͳ������Ҳ�����
	 * 
	 * @return �±�
	 */
	int getNowSentenceIndex(long t) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).isInTime(t)) {
				return i;
			}
		}
		// throw new RuntimeException("��Ȼ�������Ҳ����������");
		return -1;
	}

	/**
	 * �Ƿ����϶�,ֻ���и�ʲſ��Ա��϶�,����û��������
	 * 
	 * @return �ܷ��϶�
	 */
	public boolean canMove() {
		return list.size() > 1 && enabled;
	}

	/**
	 * �õ���ǰ��ʱ��,һ��������ʾ�����õ�
	 */
	public long getTime() {
		return tempTime;
	}

	/**
	 * �ڶ�tempTime���˸ı�֮��,���һ������ ֵ,���ǲ�������Ч�ķ�Χ֮��
	 */
	private void checkTempTime() {
		if (tempTime < 0) {
			tempTime = 0;
		} else if (tempTime > during) {
			tempTime = during;
		}
	}

	/**
	 * ���߸��,Ҫ��ʼ�ƶ���, �ڴ��ڼ�,���жԸ�ʵ�ֱ�ӵ�ʱ�����ö������
	 */
	public void startMove() {
		isMoving = true;
	}

	/**
	 * ���߸���϶�����,���ʱ���ʱ��� ��Ҫ���,��������
	 */
	public void stopMove() {
		isMoving = false;
	}

}
