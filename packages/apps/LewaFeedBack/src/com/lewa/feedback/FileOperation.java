package com.lewa.feedback;

import java.io.File;
import java.util.List;

//�ļ�����
public class FileOperation {
	
	private static final long UNIT_LEN_KB = 1024;
	
	public static int countFileLenByKB(String filePath)	//��KBΪ��λ�����ļ��ĳ���
	{
		File file = new File(filePath);
		long fileLen = 0;
		if(file.exists())
		{
			fileLen = file.length();
		}
		
		return Integer.parseInt(String.valueOf((fileLen / UNIT_LEN_KB)));
	}
	
	public static File[] getFilesFromPaths(List<String> paths)
	{
		int size = paths.size();
		File[] files = new File[size];
		
		for(int i = 0; i < size; i++)
		{
			files[i] = new File(paths.get(i).substring(5));
		}
		
		return files;
	}
}
