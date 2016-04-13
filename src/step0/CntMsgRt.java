package step0;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedHashMap;
import java.util.Map;

import util.FileWriteUtil;

/**
 * 
 * @ClassName: CntUserFriends
 * @Description:计算每条消息的转发数
 *              输入(目录：FilterPairUID)：userid_pair.csv
                                               输出(目录：DisJoint)：user_friends.txt（uid+friends）

 * @author zeze
 * @date 2016年3月18日 下午12:59:04
 *
 */
public class CntMsgRt {
	private static String path = "F:/WeiBo/Exp/FilterPairUID/";
	
	public static void main(String[] args) {
		new CntMsgRt().CntMsgRt_func();
	}
	
	public void CntMsgRt_func(){
		File file1 = new File(path);
		File[] files1 = file1.listFiles();
		for (File f1 : files1) {
			if (f1.isDirectory()) {
				new CntMsgRt().cnt(f1.getName());
			}
		}
		System.out.println("CntMsgRt已完成");
	}
	
	public void cnt(String filename) {
		BufferedReader bReader = null;
		FileReader reader = null;
		
		String Dir = path +filename+ "/";
		String messDir =Dir+ "/"+filename+"_pair.csv";
		String destfile=Dir+"/"+filename+"_UserRt.csv";
		File file=new File(destfile);
		if(file.exists())
			file.delete();
		Map<String, Integer> UserCnt = new LinkedHashMap<String, Integer>();
		StringBuffer sBuilder = new StringBuffer();
		try {
			reader = new FileReader(new File(messDir));
			bReader = new BufferedReader(reader);
			String str = null;
			String[] ss = null;
			int tempCnt;
			int cnt=0;
			while ((str = bReader.readLine()) != null) {
				cnt++;
				ss = str.split(",");
				if(cnt==1){
					UserCnt.put(ss[1], 1);//第一行
					UserCnt.put(ss[0], 0);
					continue;
				}			
				if (UserCnt.containsKey(ss[1])) {//存在用户名
					tempCnt = UserCnt.get(ss[1]) + 1;
					UserCnt.put(ss[1], tempCnt);// 数目加1
					if(!UserCnt.containsKey(ss[0])){
						UserCnt.put(ss[0], 0);
					}
				}
				
				
			}
			// 遍历HashMap
			for (Map.Entry<String, Integer> entry : UserCnt.entrySet()) {// 遍历所有用户列表，打印用户新编号和Post数目
				//System.out.println(entry.getKey() + "||" + entry.getValue());
				sBuilder.append(entry.getKey() + "," + entry.getValue() + "\n");
			}
			FileWriteUtil.WriteDocument(destfile, sBuilder.toString());
			System.out.println(filename+":用户总数："+UserCnt.size());
			UserCnt.clear();

		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		finally {
			if (bReader != null) {
				try {
					bReader.close();
				}
				catch (Exception e2) {
					// TODO: handle exception
					e2.printStackTrace();
				}
			}
			if (reader != null) {
				try {
					reader.close();
				}
				catch (Exception e2) {
					// TODO: handle exception
					e2.printStackTrace();
				}
			}
		}
	}
}
