package step0;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import util.FileWriteUtil;

/**
 * 
 * @ClassName: TestDisjoint
 * @Description:并查集算法生成子图
 *              输入(目录：SubgraphProcess)：user_id_pair.txt
 *              输出(目录：DisJoint)：belong.txt(每个节点对应子图的根节点)
 *                                subgraph.txt(子图的根节点、该子图点的数目)
 * @author zeze
 * @date 2016年3月11日 下午2:22:47
 *
 */
class SubGraph {// 定义子图的根节点和大小
	int root_id;
	long num_of_nodes;
}

public class Disjoint {

	public static int N = 1000000;// 100万用户
	public static String Dir="F:/WeiBo/Exp";
	public static String path=Dir+"/SubgraphProcess";
	public static String dir=Dir+"/DisJoint";

	public int findRoot(int x, int rp[]) {// 查找根节点
		return rp[x] == x ? rp[x] : (rp[x] = findRoot(rp[x], rp));
	}

	// 定义排序规则，以子图所含点的数目从大到小排序
	public boolean compare(SubGraph s, SubGraph v) {
		return s.num_of_nodes > v.num_of_nodes;
	}

	public void Step1_TestDisjoint() {
		
		File fileEx=new File(dir);
		if (!fileEx.exists())
			fileEx.mkdirs();
		SubGraph[] subGraphs = new SubGraph[N];
		int[] root_of_parents = new int[N];
		long[] subgraph_nodes_num = new long[N];
		for (int i = 0; i < N; i++) {
			subGraphs[i] = new SubGraph();
			subGraphs[i].root_id = 0;// 初始化子图的跟节点和数目大小
			subGraphs[i].num_of_nodes = 0;
			root_of_parents[i] = i;//并查集初始化，父节点为本身
			subgraph_nodes_num[i]=0;//初始化每一个子图的数目为零
		}
		
		File file = new File(path + "/user_id_pair.csv");
		FileReader reader = null;
		BufferedReader br = null;
		int u,v;//v,u表示相连接的两点
		int num_of_nodes=0;//记录总共包含的顶点数目，初始化为0
		StringBuffer sBufferBelong=new StringBuffer();
		StringBuffer sBufferBeSubgraph=new StringBuffer();
		try {
			reader = new FileReader(file);
			br = new BufferedReader(reader);

			String str = null;
			int cnt = 0;
			while ((str = br.readLine()) != null) {//构建并查集
				String[] ss=str.split(",");
				u=Integer.parseInt(ss[0]);
				v=Integer.parseInt(ss[1]);
				int x=findRoot(u, root_of_parents);
				int y=findRoot(v, root_of_parents);
				if(x==y)//根节点相同的话，在同一个集合中
					continue;
				if(x<y)
					root_of_parents[y]=x;
				else
					root_of_parents[x]=y;
				num_of_nodes=Math.max(u, num_of_nodes);
				num_of_nodes=Math.max(v, num_of_nodes);
			}//num_of_nodes记录所有点的总数目，建立好并查集，并且子图以最小的点表示父节点，最终结果是root_of_parent[i]记录的是父节点
			System.out.println("总共节点数为：" + num_of_nodes);
			
			int num_of_subgraph=0;//表示子图的数目，初始化为0
			
			for(int i=1;i<=num_of_nodes;i++){
				int t=findRoot(i, root_of_parents);//返回该点所属子图的父根节点
				sBufferBelong.append(t+"\n");
				if(i==t)//根节点是本身，则新增一个子图
					subGraphs[num_of_subgraph++].root_id=t;//第num_of_subgraph个子图的根节点是t
				// 存储子图信息，所有子图都用最小点的root_id表示，
				// 并用num_of_subgraph来记录是第几个子图,相等表示增加了一个新的子图
				subgraph_nodes_num[t]++;//对应子图的所有点数目增加一个
			}
			System.out.println("子图总数："+num_of_subgraph);
			// 此时num_of_subgraph表示子图的总数目
			for(int i=0;i<num_of_subgraph;i++)
				subGraphs[i].num_of_nodes=subgraph_nodes_num[subGraphs[i].root_id];
			// subgraph[i]表示第i个子图的信息，subgraph[i].root_id表示第i个子图的父根节点，
			// subgraph_nodes_num[subgraph[i].root_id]表示第i个子图所含的点数
			
			//Arrys.sort(subgraph,subgraph+num_of_subgraph,compare);
			//按照所有子图所含的点数排序，从大到小
			Arrays.sort(subGraphs, 0, num_of_subgraph, new numComparetor());
			System.out.println("最大子图用户数："+subGraphs[0].num_of_nodes);
			for(int i=0;i<num_of_subgraph;i++){
				sBufferBeSubgraph.append(subGraphs[i].root_id+"||"+subGraphs[i].num_of_nodes+"\n");
			}
			//保存到subgraph.txt文件中，格式为子图的跟节点，该子图的数目
			
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		finally {
			if (br != null) {
				try {
					br.close();
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
		String file2 = dir + "/belong.txt";		
		FileWriteUtil.WriteDocument(file2, sBufferBelong.toString());
		String file3=dir+"/subgraph.txt";
		FileWriteUtil.WriteDocument(file3, sBufferBeSubgraph.toString());
		
		
	}
	
	//从小到大排序
	static class numComparetor implements Comparator{
		public int compare(Object object1,Object object2){//实现接口中的方法
			SubGraph p1=(SubGraph)object1;//强制转换
			SubGraph p2=(SubGraph)object2;
			return new Long(p2.num_of_nodes).compareTo(new Long(p1.num_of_nodes));
			
		}
		
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		long nd = 1000 * 24 * 60 * 60;
		long nh = 1000 * 60 * 60;
		long nm = 1000 * 60;
		long ns = 1000;
		// 获得两个时间的毫秒时间差异
		System.out.println("开始运行");
		Date nowDate = new Date();
		new Disjoint().Step1_TestDisjoint();
		;
		// System.out.println("已完成");
		Date endDate = new Date();
		long diff = endDate.getTime() - nowDate.getTime();// getTime返回的是一个long型的毫秒数
		// 计算差多少天
		long day = diff / nd;
		// 计算差多少小时
		long hour = diff % nd / nh;
		// 计算差多少分钟
		long min = diff % nd % nh / nm;
		// 计算差多少秒//输出结果
		long sec = diff % nd % nh % nm / ns;
		// 计算多少毫秒
		long ms = diff % nd % nh % nm % ns;
		System.out.println(day + "天" + hour + "小时" + min + "分钟" + sec + "秒" + ms + "毫秒");

	}

}
