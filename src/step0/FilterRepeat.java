package step0;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.bcel.generic.AALOAD;

import util.FileReadUtil;

/**
 * 
 * @ClassName: FilterPair3
 * @Description: (从数据集中分离屏幕名发布的消息ID，分离屏幕名转发的消息的ID) 输入(目录：Source)： 原始数据集.txt
 *               输出(目录：FilterRepeat)：msg_XXX_2016XXXXX.txt
 * @author zeze
 * @date 2016年3月16日 上午10:39:19
 *
 */

public class FilterRepeat {
	private static String messDir = "F:/WeiBo/Exp/Source";
	private static String destDir = "F:/WeiBo/Exp/FilterRepeat/";
	private static String destFile = null;
	private static String Mid = null;
	private static Map<String, Integer> uidMap = new LinkedHashMap<String, Integer>();

	private static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();// 递归删除目录中的子目录下
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		// 目录此时为空，可以删除
		return dir.delete();
	}

	public static void Step0_FilterRepeat() {
		File file = new File(destDir);
		if(file.exists())
			deleteDir(file);
		if (!file.exists())
			file.mkdirs();// 创建新目录

		BufferedReader br = null;
		FileReader reader = null;

		FileWriter fw = null;
		BufferedWriter bw = null;
		PrintWriter pw = null;

		File file1 = new File(messDir);

		File[] files1 = file1.listFiles();
		for (File f1 : files1) {
			if (f1.getName().endsWith(".txt")) {

				String fileName = f1.getPath();// messDir+f1.getName();
				int index = fileName.indexOf("msgid_") + 6;
				Mid = fileName.substring(index, index + 24);
				destFile = destDir + "msg_" + Mid + ".txt";
				System.out.println(destFile);

				try {
					reader = new FileReader(new File(fileName));
					br = FileReadUtil.getReadStream(reader);

					String str = null;
					String[] ss = null;
					String msgid = null;
					int deep = 0;
					int cnt = 0;
					while ((str = br.readLine()) != null) {
						cnt++;
						if (cnt == 1)
							continue;
						ss = str.split("\\|");
						msgid = ss[0];
						try {
							deep = Integer.parseInt(ss[10]);
							// 获取每条消息的最大深度
							if (uidMap.containsKey(msgid) && deep > uidMap.get(msgid)) {
								uidMap.put(msgid, deep);
								// System.out.println(msgid+"||"+deep);

							}
							else {
								uidMap.put(msgid, deep);
							}
						}
						catch (Exception e) {
							// TODO: handle exception
							System.out.println("异常信息："+str);
						}
					}
					System.out.println("过滤前数量：" + cnt);

				}
				catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				try {
					reader = new FileReader(new File(fileName));
					br = FileReadUtil.getReadStream(reader);

					fw = new FileWriter(new File(destFile), true);
					bw = new BufferedWriter(fw);
					pw = new PrintWriter(bw);

					String str = null;
					String[] ss = null;
					String msgid = null;
					int deep = 0;
					int cnt = 0;
					int flag = 0;
					String wString = "tzmid|zid|zname|zmid|mid|ztext|zurl|zsource|zzan|ztime|deep|rt|comment";
					pw.write(wString + "\r\n");
					while ((str = br.readLine()) != null) {
						flag++;
						if (flag == 1)
							continue;
						ss = str.split("\\|");
						msgid = ss[0];
						try {
							deep = Integer.parseInt(ss[10]);
							// 查重，判断采集深度的大小
							if (uidMap.containsKey(msgid) && deep == uidMap.get(msgid)) {
								pw.write(str + "\r\n");
								cnt++;
							}
						}
						catch (Exception e) {
							// TODO: handle exception
							System.out.println("异常信息："+str);
						}
					}
					cnt++;
					System.out.println("过滤后数量:" + cnt);

				}
				catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				finally {
					if (pw != null) {
						pw.close();
					}
					if (bw != null) {
						try {
							bw.close();
						}
						catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					if (fw != null) {
						try {
							fw.close();
						}
						catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}

		}
		System.out.println("End!");

	}

	public static void main(String[] args) {
		Step0_FilterRepeat();
	}
}