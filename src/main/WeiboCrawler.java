package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Random;

import org.apache.http.client.CookieStore;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.Cookie;

import util.FileWriteUtil;
import util.Id2MidUtil;

/**
 * 
 * @ClassName: WeiboCrawler
 * @Description: 微博转发采集
 * @author zeze
 * @date 2016年4月12日 下午12:05:39
 *
 */
public class WeiboCrawler {

	private static String Url = null;
	private static int CrawlDeep = 0;// 采集深度
	private static int SleepTime = 0;// 采集间隔时间
	private static int NumCookies = 0;// cookies数目

	private static Logger logger = Logger.getLogger(WeiboCrawler.class);
	private static String cookiePath = "F:/WeiBo/cookie/cookie.file";// cookie目录
	private static String outputpath = "F:/WeiBo/Data/";// 输出目录
	private static String destfile = "F:/WeiBo/Data/";// 采集保存目录
	private static int cnt = 0;

	public static void main(String[] args) {
		
		String url=null;
		url = "http://weibo.com/3942244083/DqGls0t3C?type=comment";		
		int deep = 18;// 采集深度
		int sleepTime = 2000;// 采集间隔时间
		int numCookies = 7;// cookies数目
		Crawler(url, sleepTime, deep, numCookies);
		//Crawler(args[0], sleepTime, deep, numCookies);
		System.out.println("采集结束："+url);
		System.out.println("保存目录："+destfile);
		
	}

	public static void Crawler(String url, int sleepTime, int deep, int numCookies) {
		Url = url;
		SleepTime = sleepTime;// 采集间隔时间
		CrawlDeep = deep;// 采集深度
		NumCookies = numCookies;// cookies数目

		String mid = GetMid(Url);// D8hxnrQdM
		String uid = GetUid(Url);// 1713926427

		outputpath = new CrawlInit().CrawlerInit(mid, uid, numCookies);// 首页信息,返回
		while (outputpath.equals("error")){
			outputpath = new CrawlInit().CrawlerInit(mid, uid, numCookies);// 首页信息,返回
			if(!outputpath.equals("error"))break;
			try {
				Thread.sleep(5000);
				System.out.println("等待5s重新开始采集...");
			}
			catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		int index = outputpath.indexOf(mid) + 10;
		String ctime = outputpath.substring(index, index + 14);
		destfile = outputpath + "/msgid_" + mid + "_" + ctime + ".txt";// 采集保存目录
		System.out.println("输出目录:"+destfile);// 输出目录
		System.out.println("初始化结束,等待5s开始采集...");
		try {
			Thread.sleep(5000);
		}
		catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}																
		
		

		// 获得页数
		int PageNum = GetPageNum(mid, uid, 0, CrawlDeep);

		for (int i = 1; i <= PageNum; i++) {
			CrawlRTPage(mid, uid, Integer.toString(i), 1, CrawlDeep);
			try {// 采集间隔
				Thread.sleep(SleepTime);
			}
			catch (InterruptedException e) {
				logger.error(e);
				return;
			}
		}
	}

	public static int GetPageNum(String mid, String uid, int deep, int CrawlDeep) {
		int Num = 0;
		String url = "http://weibo.cn/repost/" + mid + "?uid=" + uid;
		System.out.println("Parser Url:" + url);
		logger.info("Parser Url:" + url);

		WebClient webClient = new WebClient(BrowserVersion.FIREFOX_17);
		webClient.getCookieManager().setCookiesEnabled(true);

		// 获取cookies
		CookieStore cookieStore = GetCookieStore();
		List<org.apache.http.cookie.Cookie> l = cookieStore.getCookies();
		for (org.apache.http.cookie.Cookie temp : l) {
			Cookie cookie = new Cookie(temp.getDomain(), temp.getName(), temp.getValue(), temp.getPath(), temp.getExpiryDate(), false);
			webClient.getCookieManager().addCookie(cookie);
		}

		HtmlPage page = null;
		try {
			page = webClient.getPage(url);
		}
		catch (FailingHttpStatusCodeException e) {
			logger.error(e);
		}
		catch (MalformedURLException e) {
			logger.error(e);
		}
		catch (IOException e) {
			logger.error(e);
		}

		String html = page.getWebResponse().getContentAsString();
		Document doc = Jsoup.parse(html);

		// 没有转发，直接返回
		Elements rt = doc.select("div").select("span[id=rt]");// span:contains(转发)
		if (!rt.text().contains("[")) {
			System.out.println("没有转发");
			try {// 采集间隔
				Thread.sleep(SleepTime);
			}
			catch (InterruptedException e) {
				logger.error(e);
			}
			return 0;
		}

		// 有转发
		Elements RTList = doc.select("div[class =c]");
		if (doc.select("[id=pagelist]").text().contains("页")) {// 存在翻页
			String pnum = doc.select("[id=pagelist]").get(0).text();
			pnum = pnum.substring(pnum.indexOf("/") + 1).replace("页", "");
			Num = Integer.parseInt(pnum);
			return Num;

		}
		else {
			// System.err.println("GetPageNum() 转发数小于10");
		}
		if (Num == 0) {
			String path = null;
			path = new String(outputpath + "/" + mid + "/" + System.currentTimeMillis() + "Page_M" + "Deep_" + deep + ".html");
			File file2 = null;
			file2 = new File(outputpath + "/" + mid);
			if (!file2.exists())
				file2.mkdirs();
			SavePage(page, path);// 保存文件

			String zname = null;// 转发的用户名
			String zid = null;// 转发的用户ID
			String zzan = null;// 点赞数
			String zmid = null;// 转发的消息id
			String ztime = null;// 转发时间
			String zsource = null;// 来源
			String ztext = null;// 转发的内容
			String zurl = null;
			mid = new Id2MidUtil().Uid2Mid(mid);// 消息ID

			for (Element result : RTList) {// 解析列表

				// 点赞数
				zzan = result.select("span[class=cc]").text();
				if (zzan.equals("")) {// 过滤没有点赞标签
					continue;
				}

				zzan = zzan.trim().substring(1).replace("[", "").replace("]", "");

				if (result.select("a").size() > 0) {
					zname = result.select("a").get(0).text();// 转发的用户名
					zid = result.select("a").get(0).toString();// 转发的用户id
					if (zid.indexOf("u/") == 10) {// 正常的用户id
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
					ztext = ztext.substring(ztext.indexOf(":") + 1, ztext.indexOf("//@"));
				}
				else {
					ztext = ztext.substring(ztext.indexOf(":") + 1, ztext.indexOf("赞"));
				}

				zurl = "http://weibo.cn/repost/" + zmid + "?uid=" + zid;

				String tzmid = new Id2MidUtil().Uid2Mid(zmid);

				// 消息ID,用户ID,用户名,屏幕名,转发消息ID,消息内容,消息URL,来源,赞数,发布时间,层数
				// tzmid,zid,zname,zmid,mid,ztext,zurl,zsource,zzan,ztime,deep
				cnt++;
				String wString = tzmid + "|" + zid + "|" + zname + "|" + zmid + "|" + mid + "|" + ztext + "|" + zurl + "|" + zsource + "|" + zzan + "|" + ztime + "|" + deep + "|0|0";
				System.out.println(cnt + ":" + wString);

				StringBuffer sBuilder = new StringBuffer();
				sBuilder.append(wString + "\r\n");
				FileWriteUtil.WriteDocument(destfile, sBuilder.toString());

				if (deep <= CrawlDeep) {// 采集深度
					try {// 采集间隔1s
						Thread.sleep(SleepTime);
					}
					catch (InterruptedException e) {
						logger.error(e);
					}
					int PageNum = GetPageNum(zmid, zid, deep + 1, CrawlDeep);
					for (int i = 1; i <= PageNum; i++) {
						CrawlRTPage(zmid, zid, Integer.toString(i), deep + 1, CrawlDeep);
						try {// 采集间隔1s
							Thread.sleep(SleepTime);
						}
						catch (InterruptedException e) {
							logger.error(e);
						}
					}
				}

			}
		}
		webClient.closeAllWindows();
		return Num;
	}

	public static void CrawlRTPage(String mid, String uid, String indexpage, int deep, int crawldeep) {

		String url = "http://weibo.cn/repost/" + mid + "?uid=" + uid + "&&page=" + indexpage;
		System.out.println("Parser Url:" + url);
		logger.info("Parser Url:" + url);

		WebClient webClient = new WebClient(BrowserVersion.FIREFOX_17);
		webClient.getCookieManager().setCookiesEnabled(true);

		// 获取cookies
		CookieStore cookieStore = GetCookieStore();

		List<org.apache.http.cookie.Cookie> l = cookieStore.getCookies();
		for (org.apache.http.cookie.Cookie temp : l) {
			Cookie cookie = new Cookie(temp.getDomain(), temp.getName(), temp.getValue(), temp.getPath(), temp.getExpiryDate(), false);
			webClient.getCookieManager().addCookie(cookie);
		}

		HtmlPage page = null;
		try {
			page = webClient.getPage(url);
		}
		catch (FailingHttpStatusCodeException e) {
			logger.error(e);
		}
		catch (MalformedURLException e) {
			logger.error(e);
		}
		catch (IOException e) {
			logger.error(e);
		}

		if (Integer.parseInt(indexpage) == 1)
			parserFirstPage(page, mid, uid, deep, crawldeep);// 解析微博
		else
			parserPage(page, mid, uid, deep, crawldeep);

		String path = null;
		path = new String(outputpath + "/" + mid + "/" + System.currentTimeMillis() + "Page_" + indexpage + "Deep_" + deep + ".html");

		File file2 = null;
		file2 = new File(outputpath + "/" + mid);
		if (!file2.exists())
			file2.mkdirs();
		// System.out.println(page.getWebResponse().toString());
		SavePage(page, path);// 保存文件

		webClient.closeAllWindows();
		logger.info("execution: Crawl Success ");
		return;
	}

	// 解析第一页
	public static void parserFirstPage(HtmlPage page, String mid, String uid, int deep, int crawldeep) {
		String Nummid = new Id2MidUtil().Uid2Mid(mid);// 数字消息ID
		String text = null;
		String name = null;
		String timeStr = null;
		String ZhuanFaNum = null;
		String PinlunNum = null;
		String zanNum = null;

		String html = page.getWebResponse().getContentAsString();
		Document doc = Jsoup.parse(html);
		Elements info = doc.select("[class =c]").select("[id= M_]");

		// 博主
		Elements BoZhu = info.select("a");
		if (BoZhu.text().equals("")) {
			System.err.println("异常页面");
			logger.error("异常页面:mid=" + mid + " uid=" + uid);
			return;
		}

		// 转发内容
		if (info.select("span[class=cmt]").text().equals("")) {// 不是转发的内容
			text = info.select("span[class=ctt]").text().substring(1);// 正文
		}
		else {
			text = info.text();// 转发理由
			if (text.contains("//@")) {
				text = text.substring(text.indexOf("转发理由:") + 5);
				text = text.substring(0, text.indexOf("//@"));
			}
			else {
				text = info.toString().substring(info.toString().indexOf("转发理由:") + 12, info.toString().indexOf("<!-- 是否进行翻译 -->"));
			}
		}
		// 时间
		Elements time = info.select("span[class=ct]");
		// 转发
		Elements rt = doc.select("div").select("span[id=rt]");
		// 评论
		Elements ct = doc.select("div").select("span:contains(评论)");
		// 赞
		Elements zan = doc.select("div").select("span:contains(赞)");

		text = text.trim();
		name = BoZhu.get(0).text().trim();
		timeStr = time.text().trim();
		ZhuanFaNum = rt.text().trim().substring(2).replace("[", "").replace("]", "");
		PinlunNum = ct.text().trim().substring(3).replace("[", "").replace("]", "");
		zanNum = zan.get(0).text().trim().substring(2).replace("[", "").replace("]", "");

		if (!rt.text().contains("["))// 判断是否有转发
			ZhuanFaNum = "0";
		if (!ct.text().contains("["))
			PinlunNum = "0";

		System.out.println("英文消息ID： " + mid);
		System.out.println("数字消息ID： " + Nummid);
		System.out.println("用户ID： " + uid);
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
			try {// 采集间隔1s
				Thread.sleep(SleepTime);
			}
			catch (InterruptedException e) {
				logger.error(e);
			}
			return;
		}

		if (doc.select("[id=pagelist]").text().contains("页")) {// 转发页数
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

			zzan = zzan.trim().substring(1).replace("[", "").replace("]", "");

			if (result.select("a").size() > 0) {
				zname = result.select("a").get(0).text();// 转发的用户名
				zid = result.select("a").get(0).toString();// 转发的用户id
				if (zid.indexOf("u/") == 10) {// 正常的用户id
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
				ztext = ztext.substring(ztext.indexOf(":") + 1, ztext.indexOf("//@"));
			}
			else {
				ztext = ztext.substring(ztext.indexOf(":") + 1, ztext.indexOf("赞"));
			}

			zurl = "http://weibo.cn/repost/" + zmid + "?uid=" + zid;
			String tzmid = new Id2MidUtil().Uid2Mid(zmid);

			// 消息ID,用户ID,用户名,屏幕名,转发消息ID,消息内容,消息URL,来源,赞数,发布时间,层数
			// tzmid,zid,zname,zmid,mid,ztext,zurl,zsource,zzan,ztime,deep
			cnt++;
			String wString = tzmid + "|" + zid + "|" + zname + "|" + zmid + "|" + Nummid + "|" + ztext + "|" + zurl + "|"
					+ zsource + "|" + zzan + "|" + ztime + "|" + deep+"|0|0";
			System.out.println(cnt + ":" + wString);

			StringBuffer sBuilder = new StringBuffer();
			sBuilder.append(wString + "\r\n");
			FileWriteUtil.WriteDocument(destfile, sBuilder.toString());

			if (deep <= crawldeep) {// 采集深度
				int PageNum = GetPageNum(zmid, zid, deep + 1, crawldeep);
				for (int i = 1; i <= PageNum; i++) {
					CrawlRTPage(zmid, zid, Integer.toString(i), deep + 1, crawldeep);
					try {// 采集间隔1s
						Thread.sleep(SleepTime);
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

		String zname = null;// 转发的用户名
		String zid = null;// 转发的用户ID
		String zzan = null;// 点赞数
		String zmid = null;// 转发的消息id
		String ztime = null;// 转发时间
		String zsource = null;// 来源
		String ztext = null;// 转发的内容
		String zurl = null;

		mid = new Id2MidUtil().Uid2Mid(mid);// 消息ID
		Elements RTList = doc.select("div[class =c]");
		for (Element result : RTList) {
			// 点赞数
			zzan = result.select("span[class=cc]").text();
			if (zzan.equals("")) {// 过滤没有点赞标签
				continue;
			}

			zzan = zzan.trim().substring(1).replace("[", "").replace("]", "");

			if (result.select("a").size() > 0) {
				zname = result.select("a").get(0).text();// 转发的用户名
				zid = result.select("a").get(0).toString();// 转发的用户id
				if (zid.indexOf("u/") == 10) {// 正常的用户id
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
			try {
				if (ztext.contains("//@")) {
					ztext = ztext.substring(ztext.indexOf(":") + 1, ztext.indexOf("//@"));
				}
				else if (ztext.contains(":") && ztext.contains("赞")) {

					ztext = ztext.substring(ztext.indexOf(":") + 1, ztext.indexOf("赞"));
				}
			}
			catch (Exception e) {
				// TODO: handle exception
				System.err.println("Parser Err:" + ztext);
				logger.error("Parser Err:" + ztext);
			}

			// zmid+name+mid+URLid+uid+text+time+source+zan
			// 消息id+用户名+原始ID+Urlid+用户ID+内容+时间+来源+点赞数
			String tzmid = new Id2MidUtil().Uid2Mid(zmid);
			zurl = "http://weibo.cn/repost/" + zmid + "?uid=" + zid;

			cnt++;
			String wString = tzmid + "|" + zid + "|" + zname + "|" + zmid + "|" + mid + "|" + ztext + "|" + zurl + "|"
					+ zsource + "|" + zzan + "|" + ztime + "|" + deep+"|0|0";
			System.out.println(cnt + ":" + wString);

			StringBuffer sBuilder = new StringBuffer();
			sBuilder.append(wString + "\r\n");
			FileWriteUtil.WriteDocument(destfile, sBuilder.toString());

			if (deep <= crawldeep) {// 采集深度
				int PageNum = GetPageNum(zmid, zid, deep + 1, crawldeep);
				for (int i = 1; i <= PageNum; i++) {
					System.out.println("当前采集深度" + deep);
					CrawlRTPage(zmid, zid, Integer.toString(i), deep + 1, crawldeep);
					try {// 采集间隔1s
						Thread.sleep(SleepTime);
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

	// 写入文件
	public static void SavePage(HtmlPage page, String path) {

		File file2 = null;
		file2 = new File(path);

		if (file2.exists())
			logger.warn("outfile exit!");
		else {
			FileOutputStream outputStream;
			try {
				outputStream = new FileOutputStream(file2);
				outputStream.write(page.getWebResponse().getContentAsString().getBytes());
				outputStream.close();
			}
			catch (FileNotFoundException e) {
				logger.error(e);
			}
			catch (IOException e) {
				logger.error(e);
			}
		}
	}

	// 随机获取cookies
	public static CookieStore GetCookieStore() {
		CookieStore cookieStore = null;
		File file = new File(cookiePathAppendRandom());
		if (file.exists()) {
			FileInputStream fin = null;
			try {
				fin = new FileInputStream(file);
			}
			catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			ObjectInputStream in;
			try {
				in = new ObjectInputStream(fin);
				cookieStore = (CookieStore) in.readObject();
				in.close();
			}
			catch (IOException e) {
				logger.error(e);
			}
			catch (ClassNotFoundException e) {
				logger.error(e);
			}
		}
		else {
			logger.warn("CookiePath doesn`t exit !!!");
		}
		return cookieStore;
	}

	private static String cookiePathAppendRandom() {
		Random random = new Random();
		return cookiePath + random.nextInt(NumCookies);
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
