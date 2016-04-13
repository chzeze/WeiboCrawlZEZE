package step0;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import util.FileReadUtil;
import util.FileWriteUtil;




/**
 * 
 * @ClassName: a04SubgraphProcess
 * @Description: 根据MegerPair中获取的用户关系对，生成用户对编号：user_id_pair.txt,计算用户总数
 *               输入(目录：FilterPairUID):userid_pair.csv
 *               输出(目录：SubGraphProcess1):user_id_pair.csv
 *                                       user_name_id.csv; 
                                         usercount.csv
 * @author zeze
 * @date 2016年3月9日 下午4:02:14
 *
 */
public class SubgraphProcess {
	
	public static void main(String[] args) {
		new SubgraphProcess().Step1_SubgraphProcess();
	}
	
	public void Step1_SubgraphProcess(){
		String path = "F:/WeiBo/Exp/FilterPairUID/";
		File file1 = new File(path);
		File[] files1 = file1.listFiles();
		for (File f1 : files1) {
			if (f1.isDirectory()) {
				SubgraphProcess_function(f1.getName());
			}
		}
		System.out.println("SubgraphProcess已完成");
	}
	
	public static void SubgraphProcess_function(String filename){
		
		String Dir="F:/WeiBo/Exp";
		String SrcDir=Dir+"/FilterPairUID/"+filename+"/"+filename+"_pair.csv";
		
		String DestDir=Dir+"/SubgraphProcess/"+filename;
		String destFileName1 = DestDir+"/user_id_pair.csv";
		String destFileName2 = DestDir+"/user_name_id.csv";
		String destFileName3 = DestDir+"/usercount.csv";
		
		File file = new File(DestDir);
		if(file.exists())
			deleteDir(file);
		if (!file.exists())
			file.mkdirs();// 创建新目录
		
		BufferedReader br = null;
		FileReader reader = null;
		Map<String, Long> maps = new LinkedHashMap<String, Long>();
		StringBuilder sBuilderIdPair = new StringBuilder();
		StringBuilder sBuilderIdName = new StringBuilder();
				
		long total = 0;
		try {
			reader = new FileReader(new File(SrcDir));
			br = FileReadUtil.getReadStream(reader);

			String s = null;
			long cnt = 0;
			while ((s = br.readLine()) != null) {
				String[] temp = s.split(",");
				long x, y;
				if (!maps.containsKey(temp[0])) {
					maps.put(temp[0], ++cnt);//将新用户放在maps表中
					x = cnt;
				}
				else {
					x = maps.get(temp[0]);//得到用户新ID
				}
				if (!maps.containsKey(temp[1])) {
					maps.put(temp[1], ++cnt);
					y = cnt;
				}
				else {
					y = maps.get(temp[1]);
				}
				sBuilderIdPair.append(x + "," + y + "\r\n");
			}
			// 遍历maps
			for (Map.Entry<String, Long> entry : maps.entrySet()) {//遍历所有用户列表，打印用户新编号和用户名
				sBuilderIdName.append(entry.getKey() + "," + entry.getValue() + "\r\n");
				
			}
			total = maps.size();
			maps.clear();
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
		FileWriteUtil.WriteDocument(destFileName1, sBuilderIdPair.toString());
		FileWriteUtil.WriteDocument(destFileName2, sBuilderIdName.toString());
		FileWriteUtil.WriteDocument(destFileName3, String.valueOf(total));
		System.out.println("写入："+destFileName1);
		System.out.println("写入："+destFileName2);
		System.out.println("写入："+destFileName3);
		System.out.println("用户总数："+total);
		System.out.println("写入成功！");
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


}
