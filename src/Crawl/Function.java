package Crawl;

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

import Util.FileWriteUtil;
import Util.Id2MidUtil;

public class Function {
	private static Logger logger = Logger.getLogger(SourceWeiboCrawler.class);
	private static String cookiePath = "F:/WeiBo/cookie/cookie.file";
	private static String outputpath = "F:/WeiBo/Data/RtList";
	private static String destfile = "F:/WeiBo/Data/RtList/msg.csv";

	public int GetPageNum(String mid, String uid,int deep,int CrawlDeep) {
		int cnt=0;
		int Num = 0;
		String url = "http://weibo.cn/repost/" + mid + "?uid=" + uid;
		System.out.println("Parser Url:" + url);

		WebClient webClient = new WebClient(BrowserVersion.FIREFOX_17);
		webClient.getCookieManager().setCookiesEnabled(true);

		// 获取cookies
		CookieStore cookieStore = GetCookieStore();
		List<org.apache.http.cookie.Cookie> l = cookieStore.getCookies();
		for (org.apache.http.cookie.Cookie temp : l) {
			Cookie cookie = new Cookie(temp.getDomain(), temp.getName(), temp.getValue(), temp.getPath(),
					temp.getExpiryDate(), false);
			webClient.getCookieManager().addCookie(cookie);
		}

		HtmlPage page = null;
		try {
			page = webClient.getPage(url);
		} catch (FailingHttpStatusCodeException e) {
			logger.error(e);
		} catch (MalformedURLException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}

		String html = page.getWebResponse().getContentAsString();
		Document doc = Jsoup.parse(html);
		
		Elements rt = doc.select("div").select("span[id=rt]");// span:contains(转发)
		if (!rt.text().contains("[")) {
			System.out.println("没有转发");
			return 0;
		}
		
		Elements RTList = doc.select("div[class =c]");
		if (doc.select("[id=pagelist]").text().contains("页")) {// 存在翻页
			String pnum = doc.select("[id=pagelist]").get(0).text();
			pnum = pnum.substring(pnum.indexOf("/") + 1).replace("页", "");
			Num = Integer.parseInt(pnum);
			return Num;
			
		} else {
			//System.err.println("GetPageNum() 转发数小于10");
		}
		if(Num==0){
			
			String path = null;
			path = new String(outputpath + "/" + mid + "/" + System.currentTimeMillis() + "Page_M"+ "Deep_"+deep + ".html");

			File file2 = null;
			file2 = new File(outputpath + "/" + mid);
			if (!file2.exists())
				file2.mkdirs();
			// System.out.println(page.getWebResponse().toString());
			new Function().SavePage(page, path);// 保存文件
			
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

				if (deep < CrawlDeep) {// 采集深度
					int PageNum = new Function().GetPageNum(zmid, zid,deep+1,CrawlDeep);
					for (int i = 1; i <= PageNum; i++) {
						new Function().CrawlRTPage(zmid, zid, Integer.toString(i), deep + 1, CrawlDeep);
						try {// 采集间隔1s
							Thread.sleep(2000);
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

	public void CrawlRTPage(String mid, String uid, String indexpage, int deep,int crawldeep) {

		String url = "http://weibo.cn/repost/" + mid + "?uid=" + uid + "&&page=" + indexpage;
		System.out.println("Parser Url:" + url);

		WebClient webClient = new WebClient(BrowserVersion.FIREFOX_17);
		webClient.getCookieManager().setCookiesEnabled(true);

		// 获取cookies
		CookieStore cookieStore = new Function().GetCookieStore();

		List<org.apache.http.cookie.Cookie> l = cookieStore.getCookies();
		for (org.apache.http.cookie.Cookie temp : l) {
			Cookie cookie = new Cookie(temp.getDomain(), temp.getName(), temp.getValue(), temp.getPath(),
					temp.getExpiryDate(), false);
			webClient.getCookieManager().addCookie(cookie);
		}

		HtmlPage page = null;
		try {
			page = webClient.getPage(url);
		} catch (FailingHttpStatusCodeException e) {
			logger.error(e);
		} catch (MalformedURLException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
		
		if (Integer.parseInt(indexpage) == 1)
			new ParserOneWeibo().parserFirstPage(page, mid, uid, deep,crawldeep);// 解析微博
		else
			new ParserOneWeibo().parserPage(page, mid, uid, deep,crawldeep);
		
		String path = null;
		path = new String(outputpath + "/" + mid + "/" + System.currentTimeMillis() + "Page_" + indexpage+"Deep_"+deep + ".html");

		File file2 = null;
		file2 = new File(outputpath + "/" + mid);
		if (!file2.exists())
			file2.mkdirs();
		// System.out.println(page.getWebResponse().toString());
		new Function().SavePage(page, path);// 保存文件

		webClient.closeAllWindows();
		logger.info("execution: Crawl Success ");
		return;
	}

	// 写入文件
	public void SavePage(HtmlPage page, String path) {

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
			} catch (FileNotFoundException e) {
				logger.error(e);
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}

	// 随机获取cookies
	public CookieStore GetCookieStore() {
		CookieStore cookieStore = null;
		File file = new File(cookiePathAppendRandom());
		if (file.exists()) {
			FileInputStream fin = null;
			try {
				fin = new FileInputStream(file);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			ObjectInputStream in;
			try {
				in = new ObjectInputStream(fin);
				cookieStore = (CookieStore) in.readObject();
				in.close();
			} catch (IOException e) {
				logger.error(e);
			} catch (ClassNotFoundException e) {
				logger.error(e);
			}
		} else {
			logger.warn("CookiePath doesn`t exit !!!");
		}
		return cookieStore;
	}

	private static String cookiePathAppendRandom() {
		Random random = new Random();
		return cookiePath + random.nextInt(7);
	}
}
