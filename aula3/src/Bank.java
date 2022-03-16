import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.locks.*;


class NotEnoughFunds extends  Exception{}
class InvalidAccount extends  Exception{}

class Bank {

    public static class Account {
        int balance;

        public synchronized int balance() {
            return balance;
        }

        public synchronized void deposit(int val) {
            balance += val;
        }

        public synchronized void withdraw(int val) throws NotEnoughFunds {
            if (balance < val) throw new NotEnoughFunds();
            balance -= val;
        }

    }

    HashMap<Integer, Account> accounts = new HashMap<>();

    Lock l = new ReentrantLock();
    int lastId = 0;

    int createAccount(int balance) {
        Account c = new Account();
        c.deposit(balance);
        l.lock();
        try {
            lastId += 1;
            int id = lastId;
            c.deposit(balance);
            accounts.put(lastId, c);
            return id;
        } finally {
            l.unlock();
        }
    }

    public void deposit(int id, int val) throws InvalidAccount {
        l.lock();
        try {
            Account c = accounts.get(id);
            if (c == null) throw new InvalidAccount();
            c.deposit(val);
        } finally {
            l.unlock();
        }
    }

    public void withdraw(int id, int val) throws InvalidAccount {
        l.lock();
        try {
            Account c = accounts.get(id);
            if (c == null) throw new InvalidAccount();
            c.withdraw(val);
        } catch (NotEnoughFunds notEnoughFunds) {
            notEnoughFunds.printStackTrace();
        } finally {
            l.unlock();
        }
    }

    public Bank(int n) {

        for (int i = 0; i < n; i++) {
            Account conta = new Account();
            accounts.put(i, conta);
        }
    }

    public int totalBalance(int[] ids) throws InvalidAccount {
        l.lock();
        int total = 0;
        try {
            for (int id : ids) {
                total += accounts.get(id).balance;
            }
        } finally {
            l.unlock();
        }
        return total;
    }

    public void transfer(int from, int to, int val) throws InvalidAccount, NotEnoughFunds {
        l.lock();
        try {
            if (from == to) return;
            Account cfrom = accounts.get(from);
            Account cTo = accounts.get(to);
            l.lock();
            try {
                cfrom.withdraw(val);
                cTo.deposit(val);
            } catch (NotEnoughFunds notEnoughFunds) {
                notEnoughFunds.printStackTrace();
            } finally {
                l.unlock();
            }

        } finally {
            l.unlock();
        }
    }

    int closeAccount(int id) throws InvalidAccount{
        int saldo = 0;
        l.lock();
        try{
            Account c = accounts.get(id);
            if (c==null) throw new InvalidAccount();
            saldo = c.balance;
            accounts.remove(id);
        }
        finally {
            l.unlock();
        }
        return saldo;
    }
}

    class Closer extends Thread{
        final int iterations;
        final Bank b;

        Closer(int iterations,Bank b){
            this.iterations = iterations;
            this.b = b;
        }

        public void run(){
            Random r = new Random();
            for (int i = 0; i<iterations;i++){
                try{
                    int conta = i;
                    int c = b.closeAccount(conta);
                    System.out.println(c);
                } catch (InvalidAccount invalidAccount) {
                    invalidAccount.printStackTrace();
                }
            }
        }
    }


    class Transferer extends Thread {
        final int iterations;
        final Bank b;

        Transferer(int iterations, Bank b) {
            this.iterations = iterations;
            this.b = b;
        }

        public void run() {
            Random r = new Random();
            for (int i = 0; i < iterations; i++) {
                try {
                    int from = r.nextInt(b.accounts.values().size());
                    int to = r.nextInt(b.accounts.values().size());
                    b.transfer(from, to, 1);

                } catch (InvalidAccount | NotEnoughFunds e) {
                    e.printStackTrace();
                }
            }
        }
    }


    class Depositor extends Thread {
        final int iterations;
        final Bank b;

        Depositor(int iterations, Bank b) {
            this.iterations = iterations;
            this.b = b;
        }

        public void run() {
            for (int i = 0; i < iterations; i++) {
                try {
                    b.deposit(i % b.accounts.values().size(), 1);
                } catch (InvalidAccount e) {
                    e.printStackTrace();
                }
            }
        }
    }


    class Observer extends Thread {
        final int iterations;
        final Bank b;

        Observer(int iterations, Bank b) {
            this.iterations = iterations;
            this.b = b;
        }

        public void run() {
            try {
                int NC = 10;
                int[] todasContas = new int[NC];
                for (int i = 0; i < iterations; ++i) {
                    int balance = b.totalBalance(todasContas);
                    if (balance != NC * 1000000) {
                        //System.out.println("saldo errado: " + balance);
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    class Main5 {
        public static void main(String[] args) throws InterruptedException, InvalidAccount {
            final int N = Integer.parseInt(args[0]);
            final int NC = Integer.parseInt(args[1]);
            final int I = Integer.parseInt(args[2]);

            Bank b = new Bank(NC);
            Thread[] a = new Thread[N];

            int[] todasContas = new int[NC];

            for (int i = 0; i < NC; i++) {
                todasContas[i] = i;
            }
            for (int i = 0; i < NC; i++) {
                b.deposit(i, 1000000);
            }

            for (int i = 0; i < N; i++) {
                a[i] = new Transferer(I, b);
            }

            new Observer(I, b).start();

            for (int i = 0; i < N; i++) {
                a[i].start();
            }

            for (int i = 0; i < N; i++) {
                a[i].join();
            }

            System.out.println(b.totalBalance(todasContas));
        }
    }
