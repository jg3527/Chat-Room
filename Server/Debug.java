
public class Debug {
	public static void print(String s){
		System.out.println("Debug: " + Thread.currentThread().getName() + " " + s);
	}
}
