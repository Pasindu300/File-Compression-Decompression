
package datacompression;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;



public class DataCompression {

   
    private static HashMap<Character,String> encoder;
    private static HashMap<String,Character> decoder;
    public static int frequencyHolder[];
    public static int totalFrequency = 0;
    public static int totalEncoded = 0;
    public static int totalDecoded = 0;
    private final String sourcePath = "D:\\Pasindu\\EEX4435\\EEX4435_MiniProject_2022\\file6.txt";
    private final String destinationPath = "D:\\Pasindu\\EEX4435\\EEX4435_MiniProject_2022";
    
    public static void main(String[] args) throws FileNotFoundException {
        
        DataCompression dc = new DataCompression();     
        dc.countFrequency();        
        Huffman root = dc.encode();
        dc.generateKey();
        dc.generateEncodedFile();        
        dc.decode(root);
        dc.checkFileSize();
    }
    
    private void countFrequency()
    {
        frequencyHolder = new int[256];        
        try(FileReader fr = new FileReader(sourcePath))
        {
            int c;           
            while((c = fr.read())!= -1)
                if(c<256)
                {
                    frequencyHolder[c]++;
                    totalFrequency++;
                }            
        }
        catch(IOException e)
        {
            System.out.println("IO error :"+e);
        }
    }
    
    private Huffman encode() throws FileNotFoundException
    {
        int n = frequencyHolder.length;
        
        PriorityQueue <Huffman> minheap= new PriorityQueue<>(n,FREQUENCY_COMPARATOR);
        
        char c;int a;
        for(int i=0;i<n;i++)
        {
            if(frequencyHolder[i]!=0)
                minheap.add(new Huffman((char)i,frequencyHolder[i]));
        }
        
        Huffman z=null;
        while(minheap.size()>1)
        {
            
            Huffman x=minheap.peek();
            minheap.poll();
            Huffman y=minheap.peek();
            minheap.poll();
            x.setCode("0");
            y.setCode("1");
            z=new Huffman();          
            z.setCharacter('\u0DB4');
            z.setFrequency(x.getFrequency()+y.getFrequency());
            z.setlChild(x);
            z.setrChild(y);
          
            minheap.add(z);            
        }
        
       
        
        encoder = new HashMap<>();
        
         PrintStream o = new PrintStream(new File("freqFile6.txt"));
 
        
        PrintStream console = System.out;
 
        
        System.setOut(o);
 
        Huffman root=z;
        traverse(root,"");
        
        System.out.println("");
        
        for(int i=0;i<n;i++)
           if(frequencyHolder[i]!=0)
            System.out.println((char)i+"\t"+frequencyHolder[i]+"\t"+encoder.get((char)i));
        return root;
        
    }
    
    private static final Comparator<Huffman> FREQUENCY_COMPARATOR = (Huffman o1, Huffman o2) -> (int) (o1.getFrequency()-o2.getFrequency());
    
    private void traverse(Huffman root,String s)throws NullPointerException
    {   
        if(root.getCode()!=null)
            s+=root.getCode();
        if(root.getlChild()==null && root.getrChild()==null && root.getCharacter()!='\u0DB4')
            {
                
                encoder.put(root.getCharacter(), s);
                return;
            }
        
         traverse(root.getlChild(), s);
         traverse(root.getrChild(), s);
    }
    
    private void generateKey()
    {
        
        Set<Map.Entry<Character,String>> set = encoder.entrySet();
        StringBuffer contents=new StringBuffer();
        for(Map.Entry<Character,String> me: set)
        {          
            contents.append(getEscapeSequence(me.getKey())).append("-").append(me.getValue()).append("\r\n");
        }
        
       
    }
   
    private static String getEscapeSequence(char h)
    {
        switch(h)
        {
            case '\n': return "\\n";
            case '\t': return "\\t";
           
            default: return Character.toString(h);
        }
        
    }
    /* Generates the encoded file */
    private void generateEncodedFile()
    {
        StringBuffer contents=new StringBuffer();
        
        try(FileReader fr = new FileReader(sourcePath);)
        {
            int c;
            while((c = fr.read())!= -1)
                if(c<256)
                    contents.append(encoder.get((char)c));  
        }
        catch(IOException e)
        {
            System.out.println("IO error :"+e);
        }
        
        try(FileWriter fw = new FileWriter(destinationPath+"\\encodedFile6.txt");)
        {
            fw.write(contents.toString());
        } catch (IOException ex) {
            Logger.getLogger(DataCompression.class.getName()).log(Level.SEVERE, null, ex);
        }
        
       
    }
    
     private double amountOfInformation()
    {
        double res=0;
        for(int i=0;i<frequencyHolder.length;i++)
           if(frequencyHolder[i]!=0)
           {
               double p = frequencyHolder[i]/(double)totalFrequency;
               res+=p*Math.log(p);
           }
        return -res;
    }
    
    /* Generates the decoded file */
    private void decode(Huffman root)
    {
        Huffman hRoot = root;
        StringBuffer contents = new StringBuffer();
        try(FileReader fr = new FileReader(destinationPath+"\\encodedFile6.txt");)
        {
            int c;
            while((c = fr.read())!= -1)
                if(c<256)
                {
                    contents.append((char)c);
                }   
        }
        catch(IOException e)
        {
            System.out.println("IO error :"+e);
        }
        StringBuffer output = new StringBuffer();
        for(int i=0;i<contents.length();i++)
        {
            char ch = contents.charAt(i);
            if(ch=='0')
                root =  root.getlChild();
            else if(ch=='1')
                root = root.getrChild();
            if(root.getrChild()==null && root.getlChild()==null)
            {               
                output.append(root.getCharacter());
                root=hRoot;
            }
        }
        totalDecoded = output.length();
        try(FileWriter fw = new FileWriter(destinationPath+"\\decodedFile6.txt");)
        {
            fw.write(output.toString());
        } catch (IOException ex) {
            Logger.getLogger(DataCompression.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
     private void checkFileSize()
    {
        System.out.println("Total  "+totalFrequency);
        
        
    }
   
}
