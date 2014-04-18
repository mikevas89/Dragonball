package test.observable;

import rx.Observable;

import rx.Observable.OnSubscribe;
import rx.Observer;
import rx.Scheduler;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.AsyncSubject;
import rx.subjects.ReplaySubject;
import rx.subjects.Subject;


public class ObservableTest {

	public String[] names={"George", "dante"}; 
//	static Subscriber<? super String> subo;
	//private static rx.Observable.OnSubscribe<String> onSubscribe;
	public Observable<String> observabletest;
//	public Subject<String,String> subject;
	public static ReplaySubject<String> asyncSubject;
	//public Observable observable;

	private Observable<String> subscribeTest(Subscriber<String> obver){
		
		obver.onNext("Starting listening to mouse");
		
		return observabletest;
		
	}
	
	
	
	protected ObservableTest() {
		super();
		
		asyncSubject= ReplaySubject.create();
		Observable.create((new OnSubscribe<String>){

			public void call(Subscriber<? super String> s) {
				System.out.println("h");
				
			} 
		});			
				
		//subject=Subject.create();
	}
	


	private OnSubscribe<String> call() {
		// TODO Auto-generated method stub
		return null;
	}



	private int create() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Observable<String> hello() {
		return asyncSubject;
	//	return observable;
       // return Observable.from(this.names).observeOn(Schedulers.newThread()).doOnTerminate(renull;{System.out.println("Helooo");});
        		
        		
	}
	
	
	public void makeSubscription(Observer<String> observer){
		ObservableTest.asyncSubject.subscribe(observer);
	}
	
	public void startStreaming(){
		//this.asyncSubject.onNext("--Hi");
	//	System.out.println("HI message was sent");
		//this.asyncSubject.onNext("Georgios");
		//System.out.println("Georgios message was sent");
		for(int i=0;i<10000;i++){
			
		//	Thread threadi = new Thread(new Runnable()
		//		public void run(){
				this.asyncSubject.onNext(String.valueOf(i));
				System.out.println("Message "+i +" will be sent");
		//	});
		//	threadi.start();
			
		}
	//	System.out.println("Succedeed message will be sent");
		this.asyncSubject.onNext("Succedeed Streaming");
		this.asyncSubject.onCompleted();
	}

		

	
	
	
	
}
        		
        		
        		
        		
        		
        		
        		
        		