package com.example.demo.utils;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class DiffTest {

    private static final Pattern EOL_SPLIT_KEEP_SEPARATORS = Pattern.compile("(?<=(\r\n|\n))|(?<=\r)(?=[^\n])");

    public static void main(String[] args) {
        String content1 = """
                package util;
                                
                import java.awt.datatransfer.DataFlavor;
                                
                public class U2 {
                                
                	class AA{
                		
                	}
                                
                	public static String calc() {
                                
                		return U2.class.getName();
                	}
                                
                	/**dqweqwe2
                	 *2ddfff23123eqwe234342daeqwe212
                	 * @param a
                	 * @param b
                	 * @param c
                	 */
                	public static void addAndPrint(int a,int b ,double c){
                		System.out.println(a+b+"a");
                		System.out.println(a+b+"c");
                		System.out.println(a+b+"b");
                		System.out.println(a+b+"d");
                	} 
                }
                """;
        String content2 = """
                package util;
                                
                import java.awt.datatransfer.DataFlavor;
                                
                public class U2 {
                                
                	class AA{
                		
                	}
                                
                	public static String calc() {
                                
                		System.out.println();
                		return U2.class.getName();
                	}
                }
                """;
        String fileName1 = "/src/main/java/util/U2.java";
        String fileName2 = "/src/main/java/util/U2.java";


        final List<String> lines1 = Arrays.asList(splitByLinesKeepSeparators(content1));
        final List<String> lines2 = Arrays.asList(splitByLinesKeepSeparators(content2));
        System.out.println("[DEBUG] lines1 size:" + lines1.size());
        System.out.println("[DEBUG] lines2 size:" + lines2.size());
        final Patch<String> patch = DiffUtils.diff(lines1, lines2);
        final List<String> unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(fileName1, fileName2, lines1, patch, 10000);
        System.out.println("[DEBUG] standard unifiedDiff size:" + unifiedDiff.size());
        final List<String> unifiedDiffError = UnifiedDiffUtils.generateUnifiedDiff(fileName1, fileName2, lines1, patch,  Integer.MAX_VALUE);
        System.out.println("[DEBUG] error unifiedDiff size:" + unifiedDiffError.size());

        for (int i = (Integer.MAX_VALUE - lines1.size()); i < Integer.MAX_VALUE; i++) {
            int size = UnifiedDiffUtils.generateUnifiedDiff(fileName1, fileName2, lines1, patch, i).size();
            System.out.println("[DEBUG] unifiedDiff size:" + size + "\t when contextSize=Integer.MAX_VALUE-" + (Integer.MAX_VALUE - i));
        }
    }

    /**
     * Splits string by lines, keeping all line separators at the line ends and in the empty lines.
     */
    private static String[] splitByLinesKeepSeparators(String string) {
        return EOL_SPLIT_KEEP_SEPARATORS.split(string);
    }
}
