package crawl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

import org.apache.http.client.CookieStore;
import org.apache.log4j.Logger;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.Cookie;


public class SinaSearchCrawlerCommand implements Callable<Object> {
	private static Logger logger = Logger.getLogger(SinaSearchCrawlerCommand.class);
	private static String word = "和颐酒店";// 和颐酒店,如家
	private static String cookiePath = "E:\\学习\\微博爬虫\\cookie\\cookie.file";
	private static String outputpath = "E:\\学习\\微博爬虫\\";

	// public Object call(){
	public static void main(String[] args) {
		try {
			word = java.net.URLEncoder.encode(word, "utf-8");
		} catch (UnsupportedEncodingException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		WebClient webClient = new WebClient(BrowserVersion.FIREFOX_17);
		webClient.getCookieManager().setCookiesEnabled(true);
		for (int i = 1; i <= 100; i++) {
			System.out.println(cookiePathAppendRandom());
			File file = new File(cookiePathAppendRandom());
			if (file.exists()) {
				FileInputStream fin = null;
				try {
					fin = new FileInputStream(file);
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
				CookieStore cookieStore = null;
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

				List<org.apache.http.cookie.Cookie> l = cookieStore.getCookies();
				for (org.apache.http.cookie.Cookie temp : l) {
					Cookie cookie = new Cookie(temp.getDomain(), temp.getName(), temp.getValue(), temp.getPath(),
							temp.getExpiryDate(), false);
					webClient.getCookieManager().addCookie(cookie);
				}

				HtmlPage page = null;
				try {
					page = webClient
							.getPage("http://weibo.cn/search/mblog?hideSearchFrame=&keyword=" + word + "&page=" + i);
				} catch (FailingHttpStatusCodeException e) {
					logger.error(e);
				} catch (MalformedURLException e) {
					logger.error(e);
				} catch (IOException e) {
					logger.error(e);
				}

				SimpleDateFormat dayformat = new SimpleDateFormat("yyyyMMdd");
				long start = System.currentTimeMillis();
				start = System.currentTimeMillis();
				String path = null;
				File file2 = null;
				path = new String(outputpath + "/" + dayformat.format(start) + "/" + System.currentTimeMillis()
						+ file.getName() + ".html");
				file2 = new File(outputpath + "/" + dayformat.format(start));
				if (!file2.exists())
					file2.mkdirs();
				file2 = new File(path);
				System.out.println("当前页" + i + ",采集至" + path);
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
				webClient.closeAllWindows();
			} else {
				logger.warn("CookiePath doesn`t exit !!!");
			}

			logger.info("execution:Success Page="+i+" Crawl keyword:"+word);

			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				logger.error(e);
				return;
			}
		}
		return;

	}

	private static String cookiePathAppendRandom() {
		Random random = new Random();
		return cookiePath + random.nextInt(7);
	}

	public SinaSearchCrawlerCommand(String word, String cookiePath, String outputpath) {
		if (word.contains("&")) {
			word = word.replace("&", " ");
		}
		this.word = word;
		this.cookiePath = cookiePath;
		this.outputpath = outputpath;
	}

	@Override
	public String toString() {
		return "SinaSearchCrawlerCommand [word=" + word + ", outputpath=" + outputpath + "]";
	}

	@Override
	public Object call() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
