package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
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
import com.sun.jna.Native.ffi_callback;

import util.FileWriteUtil;
import util.Id2MidUtil;

/**
 * 
 * @ClassName: SourceWeiboCrawler
 * @Description: 原始微博采集
 * @author Zeze
 * @date 2016年4月10日 上午10:39:50
 *
 */
public class CrawlInit {

	private static int NumCookies = 0;// cookies数目

	private static Logger logger = Logger.getLogger(CrawlInit.class);
	private static String cookiePath = "F:/WeiBo/cookie/cookie.file";// cookie目录
	private static String outputpath = "F:/WeiBo/Data/";// 输出目录
	private static String destfile = "F:/WeiBo/Data/";// 采集保存目录
	private static int cnt = 0;

	public static void main(String[] args) {
		String url = "http://weibo.com/1751675285/DmdEt5gOS?type=comment#_rnd1460425883518";
		String mid = GetMid(url);// D8hxnrQdM
		String uid = GetUid(url);// 1713926427
		int numCookies = 7;// cookies数目
		CrawlerInit(mid, uid, numCookies);
	}

	public static String CrawlerInit(String mid, String uid, int numCookies) {

		NumCookies = numCookies;// cookies数目
		
		SimpleDateFormat dayformat = new SimpleDateFormat("yyyyMMddHHmmss");
		long start = System.currentTimeMillis();
		start = System.currentTimeMillis();
		
		outputpath = outputpath + mid + "/"+dayformat.format(start)+"/";// 输出目录
		destfile = outputpath+ "/msgid_" + mid+"_"+dayformat.format(start)+ ".txt";// 采集保存目录

		File file2 = new File(outputpath);
		if (!file2.exists())
			file2.mkdirs();
		String wString = "tzmid|zid|zname|zmid|mid|ztext|zurl|zsource|zzan|ztime|deep|rt|comment";
		StringBuffer sBuilder = new StringBuffer();
		sBuilder.append(wString + "\r\n");
		FileWriteUtil.WriteDocument(destfile, sBuilder.toString());
		int status=CrawlBozhuPage(mid, uid);
		if(status==0)
			return "error";
		
		return outputpath;
	}

	public static int CrawlBozhuPage(String mid, String uid) {

		String url = "http://weibo.cn/repost/" + mid + "?uid=" + uid;
		System.out.println("Parser Url:" + url);

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

		int status=parserInitPage(page, mid, uid);// 解析微博
		if(status==0)
			return 0;

		String path = null;
		path = new String(outputpath + "/" + System.currentTimeMillis() + "Page_Init" + ".html");
		File file2 = null;
		file2 = new File(outputpath);
		if (!file2.exists())
			file2.mkdirs();

		SavePage(page, path);// 保存文文件
		webClient.closeAllWindows();
		logger.info("execution: Crawl Init Success");
		return 1;
	}

	// 解析第一页
	public static int parserInitPage(HtmlPage page, String mid, String uid) {
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
			return 0;
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
		String zurl="http://weibo.cn/repost/"+mid+"?uid="+uid;
		// 消息ID,用户ID,用户名,Urlid,转发消息ID,消息内容,消息URL,来源,赞数,发布时间,层数,转发数，评论数
		// tzmid,zid,zname,zmid,mid,ztext,zurl,zsource,zzan,ztime,deep
		cnt++;
		String wString = Nummid + "|" + uid + "|" + name + "|" + mid + "|"
		              + "" + "|" + text + "|" + zurl + "|"
		              + "来源" + "|" + zanNum + "|" + timeStr + "|" + "0"+"|"+ZhuanFaNum+"|"+PinlunNum;
		System.out.println(cnt + ":" + wString);

		StringBuffer sBuilder = new StringBuffer();
		sBuilder.append(wString + "\r\n");
		FileWriteUtil.WriteDocument(destfile, sBuilder.toString());

		return 1;
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
