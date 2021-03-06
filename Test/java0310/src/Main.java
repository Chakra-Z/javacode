//import java.util.Arrays;
//import java.util.Scanner;
//
//public class Main{
//    public static void main(String[] args){
//        Scanner scanner = new Scanner(System.in);
//        String[] string = scanner.nextLine().split(" ");
//        int[] array=new int[string.length-1];
//        for(int i=0;i<string.length-1;i++){
//            array[i]=Integer.parseInt(string[i]);
//        }
//        Arrays.sort(array);
//        for(int i=0;i<Integer.parseInt(string[string.length-1]);i++){
//            System.out.print(array[i]);
//            if(i!=Integer.parseInt(string[string.length-1])-1){
//                System.out.print(" ");
//            }
//        }
//    }
//}

import java.util.*;

public class Main{
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        while (sc.hasNext()) {
            int n = sc.nextInt();
            if (n > 1000) {
                n = 999;
            }
            List<Integer> list = new ArrayList<Integer>();
            for (int i = 0; i < n; i++) {
                list.add(i);
            }
            int i = 0;
            while (list.size() > 1) {
                i = (i + 2) % list.size();
                list.remove(i);
            }
            System.out.println(list.get(0));
        }
    }
}