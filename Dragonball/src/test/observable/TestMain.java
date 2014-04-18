package test.observable;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;

import rx.observers.Observers;
import rx.subjects.ReplaySubject;
import rx.subjects.Subject;

public class TestMain {


	
	public static void main(String[] args) {
		
		
/*		ReplaySubject<String> replaySubject = ReplaySubject.create();
		Observer<String> observer = new ObserverImplementation();
		replaySubject.subscribe(observer);
		replaySubject.onNext("1");
		replaySubject.onNext("2");
		replaySubject.onNext("3");
		replaySubject.onCompleted();*/
		
		
		
		
		
		
		
		
		Thread thread= new Thread(new Runnable(){

			@Override
			public void run() {
				System.out.println("Entering to Thread"); 
				
			//	System.out.println("Starting testing powers");
			//	int power = (int) Math.pow((double)2,(double)0/2);
			
		//		System.out.println("Power is : "+ power);
				
				//ObservableTest observableTest=new ObservableTest();
				//ObserverImplementation observerTest= new ObserverImplementation();
				
				
				Observer<Integer> adjustSecondMouseObserver = new Observer<Integer>() {
	
					public void onCompleted(){
						System.out.println("Adjusting mouse completed");
						mouseX=0; //initializing mouse position
					}
					public void onError(Throwable err){
						System.err.println("Error on Mouse events");
						mouseX=0;
					}
					public void onNext(Integer mouseX){
						System.out.println("Adjusting to second screen");
						adjustMouseSecondScreen(mouseX);
					}
				};
				
				Observer<Integer> logObserver = new Observer<Integer>() {
					
					public void onCompleted() {
						System.out.println("Writing to Log completed");
						//close file
					}
					public void onError(Throwable err) {
						System.err.println("Error on Mouse events");
						//close file
					}
					public void onNext(Integer mouseX) {
						System.out.println("mouseX: "+ mouseX);
						//writer is a BufferedWriter instance
						writer.write(mouseX);
					}
				};
	
				//create an Subject which is the Observable
				ReplaySubject<Integer> mouseObservable = ReplaySubject.create();
				//subscribe the two Observers to the Observable
				mouseObservable.subscribe(adjustMouseObserver);
				mouseObservable.subscribe(logObserver);
				
				for(int i=0; i < 10; i++){
					//emit mouse positions to the observers
					//Integer mouseX = getMousePositionX();
					mouseObservable.onNext(i);
				}
				//notify the completion of the emission
				mouseObservable.onCompleted();
				
				
				
				
				//observerTest.runInWhile();
			//	observableTest.makeSubscription(observerTest);
			//	System.out.println("Observertest subscribed");
				
				
			//	observableTest.startStreaming();
				
				
			//	ObserverTest observerTest2= new ObserverTest();
				
			//	observerTest2.runInWhile();
			//	observableTest.makeSubscription(observerTest2);

				
			/*       Observable<String> s = Observable.create(new OnSubscribe<String>() {

			            @Override
			            public void call(Subscriber<? super String> o) {
			                o.onNext("Hi");
			                o.onCompleted();
			            }

			        });
			
			       s.subscribe(observerTest);
			     */
			//	observableTest.subscribe(observerTest);
			//	observableTest.hello();
				
		/*		 AsyncSubject<String> subject = AsyncSubject.create();
				 subject.subscribe(observerTest);
				 subject.onNext("one");
				 subject.onNext("two");
				 subject.onNext("three");
				 subject.onCompleted();
				*/
			//	Observable realObservable= new Observable();
			//	realObservable.
			//	observable.subscribe();
				System.out.println("Ending the Thread");
				
			}
			
		});
		thread.start();
		
		System.out.println("Ending Main");

	}




}
