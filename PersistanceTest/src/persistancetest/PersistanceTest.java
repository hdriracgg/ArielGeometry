/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package persistancetest;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 *
 * @author hdrira
 */
public class PersistanceTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)  throws IOException {
        write();
        read();
    }
    
    
    public static void write() throws IOException {
        String msg = "hello you\nthis is just a test\n";
        Files.write(Paths.get("./duke.txt"), msg.getBytes());
    }
    
    public static void read() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get("./duke.txt"), Charset.defaultCharset());
        for (String line : lines) {
            System.out.println("line read: " + line);
        }
    }
    
}
