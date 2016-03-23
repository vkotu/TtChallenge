package com.tt.challenge;

import java.util.HashMap;
import java.util.Stack;

public class Database {

    Stack<String> cmdStack; //to hold user commands
    HashMap<String, Stack<String>> variablesMap; //to hold variable values
    HashMap<String, Integer> countMap; //to hold count of values set to variables in variablesMap
    int transactionCount; //to hold active transactions count

    /*
     * Constructor with variables initialization
     */
    public Database() {
        cmdStack = new Stack<String>();
        variablesMap = new HashMap<String, Stack<String>>();
        countMap = new HashMap<String, Integer>();
        transactionCount = 0;
    }

    /*
     * method to perform user commands
     */
    public void performOperation(String inputCommand) {

        //validate the command line
        if (inputCommand == null || inputCommand.isEmpty()) {
            logError();
            return;
        }

        inputCommand = inputCommand.trim();
        String[] inputArray = inputCommand.split("\\s+"); //split the command by space

        //validate the command
        try {
            CommandsEnum.valueOf(inputArray[0].toUpperCase());
        } catch (Exception e) {
            logError();
            return;
        }

        //perform operations based on user command
        switch (CommandsEnum.valueOf(inputArray[0].toUpperCase())) {

            case SET:
                if (inputArray.length == 3) performSet(inputCommand, inputArray);
                else logError();
                break;

            case UNSET:
                if (inputArray.length == 2) performUnset(inputCommand, inputArray);
                else logError();
                break;

            case GET:
                if (inputArray.length == 2) performGet(inputArray);
                else logError();
                break;

            case NUMEQUALTO:
                Integer value = 0;
                if (inputArray.length == 2) {
                    if (countMap.containsKey(inputArray[1]))
                        value = countMap.get(inputArray[1]);
                    System.out.println(value);
                } else logError();
                break;

            case BEGIN:
                if (inputArray.length == 1) {
                    transactionCount++;
                    cmdStack.push(inputCommand);
                } else {
                    logError();
                }
                break;

            case ROLLBACK:
                if (inputArray.length == 1) performRollback();
                else logError();
                break;

            case COMMIT:
                performCommit();
                break;

            default:
                logError();
        }
    }

    //default error log
    private void logError() {
        System.out.println("Not a valid command. Valid formats are 'SET a 10', 'GET a', 'UNSET', 'NUMEQUALTO', " +
                "'BEGIN', 'ROLLBACK', 'COMMIT' and 'END'");
    }

    //increment or set the count of the new value that is set to a variable
    private void incrementOrSetValCount(String val) {
        if (countMap.containsKey(val)) countMap.put(val, countMap.get(val) + 1);
        else countMap.put(val, 1);
    }

    //push the value for the respective variable stack
    private void performSet(String inpCommand, String[] inpArray) {
        String key = inpArray[1];
        String val = inpArray[2];
        //push the current command in to the command's stack
        cmdStack.push(inpCommand);
        if (variablesMap.containsKey(key)) {
            String prevValue = !variablesMap.get(key).isEmpty() ? variablesMap.get(key).peek() : null;
            //decrement the count for the value that current variable has.
            if (prevValue != null && countMap.containsKey(prevValue))
                countMap.put(prevValue, countMap.get(prevValue) - 1);
            variablesMap.get(key).push(val);
            incrementOrSetValCount(val);
        } else {
            //if variable is not set before, initialize a new stack for it and push the val
            Stack<String> varStack = new Stack<>();
            varStack.push(val);
            variablesMap.put(key, varStack);
            incrementOrSetValCount(val);
        }
    }

    //push null value into the variable stack
    private void performUnset(String inpCommand, String[] inpArray) {
        if (variablesMap.containsKey(inpArray[1]) && !variablesMap.get(inpArray[1]).isEmpty()) {
            cmdStack.push(inpCommand);
            //decrement the  count associated to that value before un setting
            String val = variablesMap.get(inpArray[1]).peek();
            if (countMap.containsKey(val)) countMap.put(val, countMap.get(val) - 1);
            //push null into the current variable stack
            variablesMap.get(inpArray[1]).push(null);
        }
    }

    //loop through the commands stack and pop the commands and the values from the variables stack until the previous begin is found
    private void performRollback() {
        if (transactionCount <= 0) {
            System.out.println("No Transaction");
        } else {
            String previousCommand = cmdStack.pop();
            boolean beginFound = false;
            while (!beginFound) {
                if (previousCommand.toUpperCase().equals(CommandsEnum.BEGIN.toString())) {
                    transactionCount--;
                    beginFound = true;
                } else {
                    String[] cmdArray = previousCommand.split("\\s+");
                    //pop the value form the respective variable stack and reduce the count associated for that value
                    String poppedVal = variablesMap.get(cmdArray[1]).pop();
                    if (poppedVal != null) countMap.put(poppedVal, countMap.get(poppedVal) - 1);
                    //after popping, see if previous stack value is present and not null, if yes increment the count for that value
                    if (!variablesMap.get(cmdArray[1]).empty()) {
                        String currentVal = variablesMap.get(cmdArray[1]).peek();
                        if (currentVal != null)
                            countMap.put(currentVal, countMap.get(variablesMap.get(cmdArray[1]).peek()) + 1);
                    }
                    previousCommand = cmdStack.pop();
                }
            }

        }
    }

    //retrieves the current value of the variable
    private void performGet(String[] inpArray) {
        System.out.println((variablesMap.containsKey(inpArray[1]) && !variablesMap.get(inpArray[1]).isEmpty())
                ? variablesMap.get(inpArray[1]).peek() : null);
    }

    //commit all previous commands until begin
    private void performCommit() {
        if (transactionCount > 0) {
            cmdStack = new Stack<String>();
            transactionCount = 0;
            for (String key : variablesMap.keySet()) {
                if (variablesMap.get(key).isEmpty()) continue;
                String temp = variablesMap.get(key).peek();
                Stack<String> tempStack = new Stack<String>();
                tempStack.push(temp);
                variablesMap.put(key, tempStack);
            }
        } else System.out.println("No transaction to commit");
    }

}
