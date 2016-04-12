package GetCookies;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class GetMultiCookieMSinaTimely {
	private static String cookieFile;
	private static String chromedriverPath;
	private static String cookieSavePath;
	private int cookieNumber;
	public static void main(String[] args) throws Exception {
		/*if(args.length == 3) {
			GetMultiCookieMSinaTimely.cookieFile = args[0];
			GetMultiCookieMSinaTimely.chromedriverPath = args[1];
			GetMultiCookieMSinaTimely.cookieSavePath = args[2];
		} else {
			System.out.println(new Date() + "three args:cookieFile path,chromedriver path,cookie save path");
			System.exit(0);
		}*/
		GetMultiCookieMSinaTimely.cookieFile = "E:\\学习\\微博爬虫\\cookie\\user.txt";
		GetMultiCookieMSinaTimely.chromedriverPath = "E:\\学习\\微博爬虫\\cookie\\chromedriver.exe";
		GetMultiCookieMSinaTimely.cookieSavePath = "E:\\学习\\微博爬虫\\cookie\\";
		if(new File(GetMultiCookieMSinaTimely.cookieFile).isFile()) {
			GetMultiCookieMSinaTimely getCookies = new GetMultiCookieMSinaTimely();
			getCookies.getCookie();
		} else {
			System.err.println(new Date() + "args is not a file!");
		}
		
    }
	
	
	
	public GetMultiCookieMSinaTimely() {
	}
	
	public void getCookie() throws IOException, InterruptedException {
		if(cookieFile == null || cookieFile.equals("")) {
			System.err.println(new Date() + "no emailpw File!");
			return;
		}
		cookieNumber=0;
		File loginFile = new File(cookieFile);
		FileInputStream fis=new FileInputStream(loginFile);
        InputStreamReader isr=new InputStreamReader(fis, "UTF-8");
        BufferedReader br = new BufferedReader(isr);
        String temp;

        ChromeOptions options = new ChromeOptions();
        options.addArguments("test-type");
		System.setProperty("webdriver.chrome.driver", chromedriverPath);

//        ChromeDriverService service = new ChromeDriverService.Builder()
//        		.usingDriverExecutable(new File(chromedriverPath)).usingAnyFreePort().build();
//        service.start();
		
		List<String> uselessList = new LinkedList<String>();

        while((temp = br.readLine()) != null) {
        	oneCookie(temp, uselessList);
        }
		List<String> tempUselessList = new LinkedList<String>();
        while(uselessList.size()>0){
        	tempUselessList.clear();
        	for(String s:uselessList) {
        		oneCookie(s, tempUselessList);
        	}
        	uselessList.clear();
        	uselessList.addAll(tempUselessList);
        }
        br.close();
		// 关闭 ChromeDriver 接口
//		service.stop();
//        System.out.println("ok!");
	}
	
	private void oneCookie(String temp, List<String> uselessList) {
		try {
			String[] username_password = temp.split("\\|");
			WebDriver driver = new ChromeDriver();
			driver.manage().timeouts().pageLoadTimeout(100, TimeUnit.SECONDS);
			System.out.println(new Date() + "logging... "+ temp);
	        driver.get("https://passport.weibo.cn/signin/login");
			
			try {
				(new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
		            public Boolean apply(WebDriver d) {
		                return  d.findElement(By.id("loginName")) != null;
		            }
		        });
			}catch(TimeoutException e) {
				System.out.println(new Date() + "TimeoutException,add "+temp+" to uselessList!");
				uselessList.add(temp);
				driver.quit();
				return;
			}
			Thread.sleep(1500);
			
			WebElement username = driver.findElement(By.id("loginName"));
			username.click();
			username.clear();
			username.sendKeys(username_password[1]);
			WebElement password = driver.findElement(By.id("loginPassword"));
			password.click();
			password.clear();
			password.sendKeys(username_password[2]);
			WebElement submit = driver.findElement(By.id("loginAction"));
			submit.click();
			try {
			(new WebDriverWait(driver, 5)).until(new ExpectedCondition<Boolean>() {
	            public Boolean apply(WebDriver d) {
	                return d.getTitle().toLowerCase().startsWith("微博");
	            }
	        });
			}catch(TimeoutException e) {
				System.out.println(new Date() + "useless account,add "+temp+" to uselessList!");
				uselessList.add(temp);
				driver.quit();
				return;
			}
	//		System.out.println(cookieNumber);
	//		System.out.println("success");
			Set<Cookie> allCookies = driver.manage().getCookies();
	
			try {
				CookieStore cookiestore = new BasicCookieStore();
				for (@SuppressWarnings("rawtypes")
				Iterator iterator = allCookies.iterator(); iterator.hasNext();) {
					Cookie cookie = (Cookie) iterator.next();
					BasicClientCookie bcookie = new BasicClientCookie(cookie.getName(), cookie.getValue());
					bcookie.setDomain(cookie.getDomain());
					bcookie.setExpiryDate(cookie.getExpiry());
					bcookie.setPath(cookie.getPath());
					cookiestore.addCookie(bcookie);
				}
				
				new File(cookieSavePath).mkdirs();
				//FileWriter fWriter=null;
				//BufferedWriter bw=null;
				//PrintWriter pWriter=null;
				File file = new File(cookieSavePath+"/cookie.file" + cookieNumber++);
				//fWriter =new FileWriter(cookieSavePath+"/cookie"+cookieNumber++);
				//bw=new BufferedWriter(fWriter);
				//pWriter=new PrintWriter(bw);
				//pWriter.write(cookiestore.toString());
				FileOutputStream fos = new FileOutputStream(file);
				ObjectOutputStream oos = new ObjectOutputStream(fos);
				System.out.println("cookile:"+cookiestore);
				oos.writeObject(cookiestore);
				oos.close();
				fos.close();
			} catch (IOException e) {
				System.out.println("IOException,add "+temp+" to uselessList!");
				uselessList.add(temp);
			}
			driver.quit();
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("Exception,add "+temp+" to uselessList!");
			uselessList.add(temp);
			return;
		}
		System.out.println(new Date() + "logging succeed! "+ temp);
	}

}
