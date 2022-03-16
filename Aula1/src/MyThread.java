import java.util.Scanner;

public class MyThread extends Thread {
    public void run() {
        try{
            System.out.println("Hello World");
            Thread.sleep(500);
            sleep(500);
            System.out.println("Hello World");
        }
        catch(InterruptedException ignored) {}
    }
}

//Outra maneira de criar threads 

class Myrunneble implements Runnable{
    public void run(){
        System.out.println("Runnable");
    }
}


class Counter{
   int value;
}

class Ex1 extends Thread {
    final int j;
    final Counter c;

    Ex1 (int k,Counter c){
        this.j = k;
        this.c = c;
    }

    public void run(){
        for (int i=0;i<j;i++){
            //System.out.println(i);
            synchronized (c) {
                c.value += 1;
            }
        }
    }
}

class ContadorA {
    int values = 0;
    synchronized void increment() {
        values += 1;
    }
    int value() {return values;}
}

class Main{
        public static void main(String[] args) throws InterruptedException{

            Counter c = new Counter();
            Scanner s = new Scanner(System.in);
            System.out.println("Numero de iteraçoes por Thread");
            int j = s.nextInt();
            System.out.println("Numero de Threads: ");
            int k = s.nextInt();
            s.close();

            Thread[] a = new Thread[k];
            for (int i=0;i<k;++i){
                a[i] = new Ex1(j,c);
            }
            for (int i=0;i<k;++i){
                a[i].start();
            }
            for (int i=0;i<k;++i){
                a[i].join();
            }

            System.out.println(c.value);
        }

}

