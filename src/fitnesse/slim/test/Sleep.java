package fitnesse.slim.test;

public class Sleep {
	private int timer;

	public Sleep(){
		// do nothing;
	}
	public Sleep(int milliseconds) throws InterruptedException {
		Thread.sleep(milliseconds);
	}

	public void setTimer(int milliseconds){
		this.timer = milliseconds;
	}

	public String doSleep() throws InterruptedException{
		Thread.sleep(this.timer);
		return "WakeUp " + this.timer;
	}

  public static void main(String[] args) throws InterruptedException {
    new Sleep(Integer.parseInt(args[0]) * 1000);
  }

}
