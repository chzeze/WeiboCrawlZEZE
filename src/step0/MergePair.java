package step0;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import util.FileReadUtil;

/**
 * 
 * @ClassName: MergePair
 * @Description:获取有转发关系的用户ID对
 *               输入(目录：FilterPairUID)：msgid_uid.csv(消息ID}||}用户ID);
 *                            uid_msgid.csv(用户ID}||}转发消息)
 *               输出(目录：FilterPairUID)：pair.csv(有转发关系的用户)
 *                            插入数据库表：pair
 * @author zeze
 * @date 2016年3月23日 下午4:44:36
 *
 */
public class MergePair {
	public static int cnt = 0;
	public static int cnt1 = 0;

	public void Step0_MergePair() {

		String path = "F:/WeiBo/Exp/FilterPairUID/";
		File file1 = new File(path);
		File[] files1 = file1.listFiles();
		for (File f1 : files1) {
			if (f1.isDirectory()) {
				new MergePair().pairs(f1.getPath());
			}
		}
		System.out.println("MergePair已完成");

	}

	public static void main(String[] args) {
		long nd = 1000 * 24 * 60 * 60;
		long nh = 1000 * 60 * 60;
		long nm = 1000 * 60;
		long ns = 1000;
		// 获取两个时间的毫秒差异
		System.out.println("开始运行");
		Date nowDate = new Date();

		new MergePair().Step0_MergePair();

		Date enDate = new Date();
		long diff = enDate.getTime() - nowDate.getTime();
		// 计算差多少天
		long day = diff / nd;
		// 计算差多少小时
		long hour = diff % nd / nh;
		// 计算差多少分钟
		long min = diff % nd % nh / nm;
		// 计算差多少秒
		long sec = diff % nd % nh % nm / ns;
		// 计算差多少毫秒
		long ms = diff % nd % nh % nm % ns;
		System.out.println(day + " 天  " + hour + " 小时  " + min + " 分钟  " + sec + " 秒 " + ms + " 毫秒");
	}

	public void pairs(String Dir) {
		BufferedReader br = null;
		FileReader reader = null;

		String destFile1 = Dir + "/msgid_uid.csv";
		String destFile2 = Dir + "/uid_msgid.csv";
		
		Map<String, String> msgIdUsernameMaps = new LinkedHashMap<String, String>();
		Map<String, Integer> PairMap = new LinkedHashMap<String, Integer>();
		
		try {
			reader = new FileReader(new File(destFile1));
			br = FileReadUtil.getReadStream(reader);
			String str = null;
			str = br.readLine();

			String[] ss = null;
			cnt1=0;
			System.out.println("开始读取：" + destFile1);
			while ((str = br.readLine()) != null) {
				ss = str.split("\\}\\|\\|\\}");
				// 消息id与用户名
				//System.out.println(str);
				msgIdUsernameMaps.put(ss[0], ss[1]);
				cnt1++;
			}
			br.close();
			System.out.println("读取数据条数：" + cnt1);
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

		try {
			reader = new FileReader(new File(destFile2));
			br = FileReadUtil.getReadStream(reader);

			String str = null;
			while ((str = br.readLine()) != null) {
				String[] ss = str.split("\\}\\|\\|\\}");
				String username = ss[0];
				String followId = ss[1];
				if (msgIdUsernameMaps.containsKey(followId)) {// 用户转发关系列表中找原消息博主
					String pairMap = username + "||" + msgIdUsernameMaps.get(followId);
					PairMap.put(pairMap, 1);// 添加进入hashMap保证关系对的唯一性

				}
			}
			
			FileWriter fw = null;
			BufferedWriter bw = null;
			PrintWriter pw = null;
			try {
				int index=Dir.indexOf("FilterPairUID")+13;
				String mid=Dir.substring(index);
				System.out.println("开始写入文件：pair.txt;");
				File file=new File(Dir + "/"+mid+"_pair.csv");
				if(file.exists())
					file.delete();
				fw = new FileWriter(file, true);
				bw = new BufferedWriter(fw);
				pw = new PrintWriter(bw);
				cnt=0;
				for (Map.Entry<String, Integer> entry : PairMap.entrySet()) {// 遍历所有用户列表，打印用户新编号和Post数目
					cnt++;
					String[] ss = entry.getKey().split("\\|\\|");
					if(!ss[0].equals(ss[1])){//过滤自己转发自己的用户
						pw.write(ss[0] + "," + ss[1]+"\r\n");
					}

				}
				System.out.println("写入结束！用户关系总数：" + cnt);
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally {
				if (pw != null) {
					pw.close();
					// pw1.close();
				}
				if (bw != null) {
					try {
						bw.close();
						// bw1.close();
					}
					catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (fw != null) {
					try {
						fw.close();
						// fw1.close();
					}
					catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}			
			msgIdUsernameMaps.clear();
			PairMap.clear();
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
