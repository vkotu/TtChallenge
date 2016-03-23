package com.tt.challenge;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        //Instantiate a Database class
        Database db = new Database();

        //check for input file
        if (args.length == 1) {
            BufferedReader br = null;
            try {
                String sCurrentLine;
                br = new BufferedReader(new FileReader(args[0]));
                //read line by line from file and perform operation
                while ((sCurrentLine = br.readLine()) != null && !sCurrentLine.toUpperCase().equals(CommandsEnum.END.toString())) {
                    sCurrentLine = sCurrentLine.trim();
                    db.performOperation(sCurrentLine);
                }
            } catch (Exception e) {
                System.out.println(e);
            } finally {
                try {
                    if (br != null) br.close();
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        } else {
            Scanner reader = new Scanner(System.in);
            try {
                System.out.println("Enter a Command: \n");
                //read the user command
                String inp = reader.nextLine();
                inp = inp.trim();
                //perform operation till the user hits end
                while (!inp.toUpperCase().equals(CommandsEnum.END.toString())) {
                    db.performOperation(inp);
                    inp = reader.nextLine();
                }
            } catch (Exception e) {
                System.out.println(e);
            } finally {
                if (reader != null) reader.close();
            }
        }
    }
}
