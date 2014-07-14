package net.firsp.amber.util;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Serializer {

    public static Object read(Context context, String name) {
        FileInputStream fis = null;
        Object data = null;
        try {
            fis = context.openFileInput(name);
            ObjectInputStream ois = new ObjectInputStream(fis);
            data = ois.readObject();
            ois.close();
            fis.close();
        } catch (Exception e) {
            try {
                fis.close();
            } catch (Exception e1) {
            }
        }
        return data;
    }

    public static Object read(File file) {
        FileInputStream fis = null;
        Object data = null;
        try {
            fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            data = ois.readObject();
            ois.close();
            fis.close();
        } catch (Exception e) {
            try {
                fis.close();
            } catch (Exception e1) {
            }
        }
        return data;
    }

    public static void write(Context context, String name, Object data) {
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(name, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(data);
            oos.close();
            fos.close();
        } catch (Exception e) {
            try {
                fos.close();
            } catch (Exception e1) {
            }
        }
    }

    public static void write(File file, Object data) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(data);
            oos.close();
            fos.close();
        } catch (Exception e) {
        }
    }
}
