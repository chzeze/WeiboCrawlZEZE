package Crawl;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import Util.FileWriteUtil;
import Util.Id2MidUtil;

/**
 * 
 * @ClassName: SourceWeiboCrawler
 * @Description: 一条微博信息采集，输入微博URL,根据mid和uid去采集页面
 * @author Zeze
 * @date 2016年4月10日 上午10:39:50
 *
 */
public class ParserOneWeibo {

	private static Logger logger = Logger.getLogger(ParserOneWeibo.class);
	private static int cnt = 0;
	private static String destfile = "F:/WeiBo/Data/RtList/msg.csv";

	public static void main(String[] args) {
		String url = "http://weibo.com/1713926427/D8hxnrQdM?type=repost#_rnd1460260008493";
		String mid = GetMid(url);
		String uid = GetUid(url);
		new Function().CrawlRTPage(mid, uid, "1", 0, 1);
	}

	// 解析第一页
	public static void parserFirstPage(HtmlPage page, String mid, String uid, int deep, int crawldeep) {

		System.out.println("消息ID： " + mid);
		mid = new Id2MidUtil().Uid2Mid(mid);// 消息ID
		System.out.println("消息ID： " + mid);
		System.out.println("用户ID： " + uid);
		
		String text=null;
		String name=null;
		String timeStr=null;
		String ZhuanFaNum=null;
		String PinlunNum=null;
		String zanNum=null;

		String html = page.getWebResponse().getContentAsString();
		Document doc = Jsoup.parse(html);
		Elements info = doc.select("[class =c]").select("[id= M_]");

		Elements BoZhu = info.select("a");// 博主
		Elements Text=null;
		
		if(info.select("span[class=cmt]").text().equals("")){//不是转发的内容
			text = info.select("span[class=ctt]").text();// 正文
		}
		else{
			text = info.text();//转发理由
			
			if(text.contains("//@")){
				text=text.substring(text.indexOf("转发理由:")+5);
				text=text.substring(0,text.indexOf("//@"));
			}
			else{
				//System.out.println();
				text=info.toString().substring(info.toString().indexOf("转发理由:")+12,info.toString().indexOf("<!-- 是否进行翻译 -->"));
			}
		}
		Elements time = info.select("span[class=ct]");// 时间

		Elements rt = doc.select("div").select("span[id=rt]");// span:contains(转发)
		Elements ct = doc.select("div").select("span:contains(评论)");
		Elements zan = doc.select("div").select("span:contains(赞)");

		if (BoZhu.text().equals("")) {
			System.err.println("异常页面");
			return;
		}
		
		text=text.trim();
		name=BoZhu.get(0).text().trim();
		timeStr=time.text().trim();
		ZhuanFaNum=rt.text().trim().substring(2).replace("[", "").replace("]", "");
		PinlunNum=ct.text().trim().substring(3).replace("[", "").replace("]", "");
		zanNum=zan.get(0).text().trim().substring(2).replace("[", "").replace("]", "");
		if(!rt.text().contains("["))
			ZhuanFaNum="0";
		if(!ct.text().contains("["))
			PinlunNum="0";
		
		System.out.println("博主: " + name);
		System.out.println("正文内容: " + text);
		System.out.println("发布时间: " + timeStr);
		System.out.println("转发数量: " + ZhuanFaNum);
		System.out.println("评论数量: " + PinlunNum);
		System.out.println("点赞数量: " + zanNum);

		Elements RTList = doc.select("div[class =c]");

		String zname = null;// 转发的用户名
		String zid = null;// 转发的用户ID
		String zzan = null;// 点赞数
		String zmid = null;// 转发的消息id
		String ztime = null;// 转发时间
		String zsource = null;// 来源
		String ztext = null;// 转发的内容
		String zurl = null;

		if (!rt.text().contains("[")) {
			System.out.println("没有转发");
			return;
		}

		if (RTList.size() > 10) {// 转发数量大于10才有翻页
			String pnum = doc.select("[id=pagelist]").get(0).text();
			pnum = pnum.substring(pnum.indexOf("/") + 1).replace("页", "");
			System.out.println("转发页数：" + pnum);
		}
		
		for (Element result : RTList) {// 解析列表

			// 点赞数
			zzan = result.select("span[class=cc]").text();
			if (zzan.equals("")) {// 过滤没有点赞标签
				continue;
			}
			cnt++;

			zzan = zzan.trim().substring(1).replace("[", "").replace("]", "");

			if (result.select("a").size() > 0) {
				zname = result.select("a").get(0).text();// 转发的用户名
				zid = result.select("a").get(0).toString();// 转发的用户id
				if (zid.indexOf("u") == 10) {// 正常的用户id
					zid = zid.substring(zid.indexOf("\">") - 10, zid.indexOf("\">"));
				}
				else {
					zid = zid.substring(zid.indexOf("/") + 1, zid.indexOf("\">"));
				}
			}
			if (result.text().contains("查看更多热门"))
				continue;

			// 转发时间和来源
			String tmp = result.select("span[class=ct]").text();
			ztime = tmp.substring(0, tmp.indexOf("来自"));
			zsource = tmp.substring(tmp.indexOf("来自") + 2);

			// 转发的消息id
			zmid = result.select("span[class=cc]").toString();
			zmid = zmid.substring(zmid.indexOf("attitude") + 9, zmid.indexOf("attitude") + 18);

			// 转发的内容
			ztext = result.text();
			if (ztext.contains("//@")) {
				ztext = ztext.substring(ztext.indexOf(":")+1, ztext.indexOf("//@"));
			}
			else {
				ztext = ztext.substring(ztext.indexOf(":")+1, ztext.indexOf("赞"));
			}

			zurl = "http://weibo.cn/repost/" + zmid + "?uid=" + zid;

			String tzmid = new Id2MidUtil().Uid2Mid(zmid);

			// 消息ID,用户ID,用户名,屏幕名,转发消息ID,消息内容,消息URL,来源,赞数,发布时间,层数
			// tzmid,zid,zname,zmid,mid,ztext,zurl,zsource,zzan,ztime,deep
			String wString = tzmid + "," + zid + "," + zname + "," + zmid + "," + mid + "," + ztext + "," + zurl + "," + zsource + "," + zzan + "," + ztime + "," + deep;
			System.out.println(cnt + ":" + wString);

			StringBuffer sBuilder = new StringBuffer();
			sBuilder.append(wString + "\n");
			FileWriteUtil.WriteDocument(destfile, sBuilder.toString());

			if (deep < crawldeep) {// 采集深度
				int PageNum = new Function().GetPageNum(zmid, zid,deep+1,crawldeep);
				for (int i = 1; i <= PageNum; i++) {
					new Function().CrawlRTPage(zmid, zid, Integer.toString(i), deep + 1, crawldeep);
					try {// 采集间隔1s
						Thread.sleep(2000);
					}
					catch (InterruptedException e) {
						logger.error(e);
						return;
					}
				}
			}

		}
		System.out.println("采集到的转发数目：" + cnt);
		return;
	}

	// 解析第二页开始
	public static void parserPage(HtmlPage page, String mid, String uid, int deep, int crawldeep) {
		String html = page.getWebResponse().getContentAsString();
		Document doc = Jsoup.parse(html);

		Elements RTList = doc.select("div[class =c]");

		String zname = null;// 转发的用户名
		String zid = null;// 转发的用户ID
		String zzan = null;// 点赞数
		String zmid = null;// 转发的消息id
		String ztime = null;// 转发时间
		String zsource = null;// 来源
		String ztext = null;// 转发的内容
		String zurl = null;
		mid = new Id2MidUtil().Uid2Mid(mid);// 消息ID

		for (Element result : RTList) {

			// 点赞数
			zzan = result.select("span[class=cc]").text();
			if (zzan.equals("")) {// 过滤没有点赞标签
				continue;
			}

			zzan = zzan.trim().substring(1).replace("[", "").replace("]", "");
			cnt++;

			if (result.select("a").size() > 0) {
				zname = result.select("a").get(0).text();// 转发的用户名
				zid = result.select("a").get(0).toString();// 转发的用户id
				if (zid.indexOf("u") == 10) {// 正常的用户id
					zid = zid.substring(zid.indexOf("\">") - 10, zid.indexOf("\">"));
				}
				else {
					zid = zid.substring(zid.indexOf("/") + 1, zid.indexOf("\">"));
				}
			}
			else {
				continue;
			}

			if (result.text().contains("查看更多热门"))
				continue;

			// 转发时间和来源
			String tmp = result.select("span[class=ct]").text();
			ztime = tmp.substring(0, tmp.indexOf("来自"));
			zsource = tmp.substring(tmp.indexOf("来自") + 2);

			// 转发的消息id
			zmid = result.select("span[class=cc]").toString();
			zmid = zmid.substring(zmid.indexOf("attitude") + 9, zmid.indexOf("attitude") + 18);

			// 转发的内容
			ztext = result.text();
			if (ztext.contains("//@")) {
				ztext = ztext.substring(ztext.indexOf(":") + 1, ztext.indexOf("//@"));
			}
			else {
				ztext = ztext.substring(ztext.indexOf(":") + 1, ztext.indexOf("赞"));
			}

			// zmid+name+mid+URLid+uid+text+time+source+zan
			// 消息id+用户名+原始ID+Urlid+用户ID+内容+时间+来源+点赞数
			String tzmid = new Id2MidUtil().Uid2Mid(zmid);
			zurl = "http://weibo.cn/repost/" + zmid + "?uid=" + zid;

			String wString = tzmid + "," + zid + "," + zname + "," + zmid + "," + mid + "," + ztext + "," + zurl + "," + zsource + "," + zzan + "," + ztime + "," + deep;
			System.out.println(cnt + ":" + wString);

			StringBuffer sBuilder = new StringBuffer();
			sBuilder.append(wString + "\n");
			FileWriteUtil.WriteDocument(destfile, sBuilder.toString());

			if (deep < crawldeep) {// 采集深度
				int PageNum = new Function().GetPageNum(zmid, zid,deep+1,crawldeep);
				for (int i = 1; i <= PageNum; i++) {
					new Function().CrawlRTPage(zmid, zid, Integer.toString(i), deep + 1, crawldeep);
					try {// 采集间隔1s
						Thread.sleep(2000);
					}
					catch (InterruptedException e) {
						logger.error(e);
						return;
					}
				}

			}

		}
		System.out.println("采集到的转发数目：" + cnt);
		return;
	}

	// url:http://weibo.com/1713926427/D8hxnrQdM?type=repost#_rnd1460261627854
	public static String GetUid(String url) {
		int index = url.indexOf("weibo.com") + 10;
		return url.substring(index, index + 10);
	}

	public static String GetMid(String url) {
		int index = url.indexOf("weibo.com") + 21;
		return url.substring(index, index + 9);
	}

}
