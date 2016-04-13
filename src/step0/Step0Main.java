package step0;

import java.util.Date;


public class Step0Main {
	
public static void main(String[] args) {
		
		long nd = 1000 * 24 * 60 * 60;
		long nh = 1000 * 60 * 60;
		long nm = 1000 * 60;
		long ns = 1000;
		// 获取两个时间的毫秒差异
		System.out.println("开始运行");
		Date nowDate = new Date();
				
		System.out.println("运行FilterRepeat");//1
		new FilterRepeat().Step0_FilterRepeat();
		
		System.out.println("运行FilterPair");//2
		new FilterPair().Step0_FilterPair();
		
		System.out.println("运行MergePair");//3
		new MergePair().Step0_MergePair();
		
		System.out.println("运行SubgraphProcess");//4
		new SubgraphProcess().Step1_SubgraphProcess();
		
		System.out.println("运行CntMsgRt");//5
		new CntMsgRt().CntMsgRt_func();
			
		System.out.println("已完成");
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

}
