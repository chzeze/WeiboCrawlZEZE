package util;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class FileReadUtil {

	public static BufferedReader getReadStream(FileReader reader) throws FileNotFoundException{
		BufferedReader br = null;
		br=new BufferedReader(reader);
		return br;
	}
	public static void close(FileReader reader){
		if(reader!=null)
			try {
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	public static void close(BufferedReader br){
		if(br!=null)
			try {
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
}
