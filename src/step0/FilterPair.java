package step0;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import util.FileReadUtil;

/**
 * 
 * @ClassName: FilterPair3
 * @Description: (从数据集中分离屏幕名发布的消息ID，分离屏幕名转发的消息的ID) 输入(目录：FilterRepeat)：
 *               msg_XXX_2016XXXXX.txt
 *               输出(目录：FilterPairUID)：msgiduserid_name.csv(消息ID}||}用户ID);
 *               usernameid_msgid.csv(用户ID}||}转发消息)
 * @author zeze
 * @date 2016年3月16日 上午10:39:19
 *
 */

public class FilterPair {

	public void Step0_FilterPair() {
		String messDir = "F:/WeiBo/Exp/FilterRepeat";
		String destDir = "F:/WeiBo/Exp/FilterPairUID/";

		BufferedReader br = null;
		FileReader reader = null;
		FileWriter fw1 = null;
		BufferedWriter bw1 = null;
		PrintWriter pw1 = null;
		FileWriter fw2 = null;
		BufferedWriter bw2 = null;
		PrintWriter pw2 = null;

		File file1 = new File(messDir);
		int cnt = 1;
		int cntpair0 = 0;
		int cntpair1 = 0;

		File[] files1 = file1.listFiles();
		for (File f1 : files1) {
			if (f1.getName().endsWith(".txt")) {
				destDir = "F:/WeiBo/Exp/FilterPairUID/";
				String fileName = f1.getPath();// messDir+f1.getName();
				System.out.println(cnt + " : " + fileName);
				
				int index=fileName.indexOf("msg_")+4;
				destDir+=fileName.substring(index,index+24)+"/";
				File file = new File(destDir);
				deleteDir(file);
				if (!file.exists())
					file.mkdirs();
				
				String destFile1 = destDir + "msgid_uid.csv";
				String destFile2 = destDir + "uid_msgid.csv";
				
				cnt++;
				try {
					reader = new FileReader(new File(fileName));
					br = FileReadUtil.getReadStream(reader);
					fw1 = new FileWriter(new File(destFile1), true);
					bw1 = new BufferedWriter(fw1);
					pw1 = new PrintWriter(bw1);
					fw2 = new FileWriter(new File(destFile2), true);
					bw2 = new BufferedWriter(fw2);
					pw2 = new PrintWriter(bw2);

					String str = null;

					while ((str = br.readLine()) != null) {
						String[] ss = str.split("\\|");
						// 消息ID}||}屏幕名
						if (!"".equals(ss[0])&&!"".equals(ss[1])) {// 屏幕名不为空
							pw1.write(ss[0] + "}||}" + ss[1] + "\n");
							cntpair0++;
						}
						// 屏幕名}||}转发消息
						if (!"".equals(ss[1])&&!"".equals(ss[4])) {// 转发ID不为空
							pw2.write(ss[1] + "}||}" + ss[4] + "\n");
							cntpair1++;
						}
						else{
							System.out.println(str);
						}
					}
					System.out.println("消息数据总数：" + cntpair0);
					System.out.println("转发消息总数：" + cntpair1);

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
					if (pw1 != null) {
						pw1.close();
					}
					if (bw1 != null) {
						try {
							bw1.close();
						}
						catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					if (fw1 != null) {
						try {
							fw1.close();
						}
						catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (pw2 != null) {
							pw2.close();
						}
						if (bw2 != null) {
							try {
								bw2.close();
							}
							catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						if (fw2 != null) {
							try {
								fw2.close();
							}
							catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							if (br != null)
								try {
									br.close();
								}
								catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							if (reader != null)
								try {
									reader.close();
								}
								catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
						}
					}
				}
			}

		}

	}

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

	public static void main(String[] args) {

		new FilterPair().Step0_FilterPair();
	}
}