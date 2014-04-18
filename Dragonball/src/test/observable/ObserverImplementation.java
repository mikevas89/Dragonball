package test.observable;

import rx.Observer;

public class ObserverImplementation implements Observer<String>{

	@Override
	public void onCompleted() {
		System.out.println("Streaming Completed");
	}

	@Override
	public void onError(Throwable th) {
		
	}

	@Override
	public void onNext(String s) {
		System.out.println("onNext :"+ s);
		System.out.println("the integer representation is "+ Integer.parseInt(s));
		if(Integer.parseInt(s) % 50 ==5){
			int sum=0;
				for(int i=0;i<500000;i++){
					//sum*=i;
					System.out.print("");
				}
				//wait(100); //TODO: doesn't wake up
				
		/*	try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}*/
			System.out.println("Leaving from sleep");
		
		}
	}
	
	public void sleepForMore(){
		System.out.println("More sleep");
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	public void runInWhile(){
		Thread thread = new Thread(new Runnable(){
			
			public void run(){
		System.out.println("Entering runInWhile");
		while(true){}
			}
		});
		
		thread.start();
		
	}

}
