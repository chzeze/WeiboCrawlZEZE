package crawl;

import org.apache.log4j.Logger;

import util.FileWriteUtil;

/**
 * 
 * @ClassName: SourceWeiboCrawler
 * @Description: 一条微博信息采集，输入微博URL
 * @author Zeze
 * @date 2016年4月10日 上午10:39:50
 *
 */
public class SourceWeiboCrawler {

	private static Logger logger = Logger.getLogger(SourceWeiboCrawler.class);
	private static String Url = "http://weibo.com/1764222885/Dqn3znIyR?type=comment";
	private static int CrawlDeep = 18;// 采集深度
	
	private static String destfile = "F:/WeiBo/Data/RtList/msg.csv";

	public static void main(String[] args) {

		String wString="tzmid,zid,zname,zmid,mid,ztext,zurl,zsource,zzan,ztime,deep";
		StringBuffer sBuilder = new StringBuffer();
		sBuilder.append(wString + "\r\n");
		FileWriteUtil.WriteDocument(destfile, sBuilder.toString());

		String mid = GetMid(Url);// D8hxnrQdM
		String uid = GetUid(Url);// 1713926427

		// 获得页数
		int PageNum = new Function().GetPageNum(mid, uid,0,CrawlDeep);

		for (int i = 1; i <= PageNum; i++) {
			new Function().CrawlRTPage(mid, uid, Integer.toString(i), 0, CrawlDeep);
			try {// 采集间隔1s
				Thread.sleep(2000);
			}
			catch (InterruptedException e) {
				logger.error(e);
				return;
			}
		}
	}

	// http://weibo.com/1713926427/D8hxnrQdM?type=repost#_rnd1460261627854
	public static String GetUid(String url) {
		int index = url.indexOf("weibo.com") + 10;
		return url.substring(index, index + 10);
	}

	public static String GetMid(String url) {
		int index = url.indexOf("weibo.com") + 21;
		return url.substring(index, index + 9);
	}

}
