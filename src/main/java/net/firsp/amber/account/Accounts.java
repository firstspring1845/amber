package net.firsp.amber.account;

import android.content.Context;

import net.firsp.amber.util.Serializer;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class Accounts implements Serializable {

    private static final long serialVersionUID = 5119823266428618752L;

    private static Accounts instance;

    public static void initialize(Context c) {
        load(c);
    }

    public static Accounts getInstance() {
        if (instance == null) {
            instance = new Accounts();
        }
        return instance;
    }

    private Accounts() {
    }

    private transient Context context;

    private Map<Long, Account> accounts = new TreeMap<Long, Account>();

    private long defaultAccount;

    public void putAccount(Account account) {
        accounts.put(account.getId(), account);
        save();
    }

    public Account getAccount(long id) {
        return accounts.get(id);
    }

    public Account[] getAccounts() {
        return accounts.values().toArray(new Account[accounts.size()]);
    }

    public void setDefaultAccount(Account account) {
        defaultAccount = account.getId();
        save();
    }

    public Account getDefaultAccount() {
        return accounts.get(defaultAccount);
    }

    private static void load(Context context) {
        Object data = Serializer.read(context, "accounts.dat");
        if (data instanceof Accounts) {
            instance = (Accounts) data;
        } else {
            instance = new Accounts();
        }
        instance.context = context;
    }

    private void save() {
        Accounts accounts = new Accounts();
        for (Account account : getAccounts()) {
            accounts.accounts.put(account.getId(), account);
        }
        accounts.defaultAccount = defaultAccount;
        Serializer.write(context, "accounts.dat", accounts);
    }

}
